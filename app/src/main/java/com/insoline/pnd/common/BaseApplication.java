package com.insoline.pnd.common;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.insoline.pnd.BuildConfig;
import com.insoline.pnd.IntroActivity;
import com.insoline.pnd.R;
import com.insoline.pnd.SiteConstants;
import com.insoline.pnd.config.ConfigLoader;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.define.AppDefine;
import com.insoline.pnd.remote.manager.ATCommandManager;
import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.service.ScenarioService;
import com.insoline.pnd.utils.ContextProvider;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.PreferenceUtil;
import com.insoline.pnd.utils.StringUtil;
import com.insoline.pnd.view.LoginActivity;
import com.insoline.pnd.view.MainActivity;
import com.insoline.pnd.view.PopupActivity;
import com.thinkware.houston.externaldevice.service.IMainService;
import com.thinkware.houston.externaldevice.service.ITachoMeter;
import com.thinkware.houston.externaldevice.service.ITachoMeterCallback;
import com.thinkware.houston.externaldevice.service.IVacancyLight;
import com.thinkware.houston.externaldevice.service.IVacancyLightCallback;
import com.thinkware.houston.externaldevice.service.data.ServiceStatus;
import com.thinkware.houston.externaldevice.service.data.TachoMeterData;
import com.thinkware.houston.externaldevice.service.data.VacancyLightData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_CALL;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_DRIVING;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_EXTRA_CHARGE;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_PAYMENT;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_VACANCY;

public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final int PROGRESS_DIALOG_DISPLAY_TIME_MAX = 10000;
    private static final int MSG_CODE_RESTART_SERVICE = 9999;
    private ArrayList<Activity> activities;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private static ContextProvider mContextProvider; //일반 클래스에서 context 사용을 위함
    private ConfigLoader mConfigLoader;

    private static ConcurrentHashMap<String, Object> globalData = new ConcurrentHashMap<>();

    //service 관리
    private ScenarioService mScenarioService = null;
    private IMainService mExternalDeviceMainService = null;
    private ITachoMeter mTachometerService = null;
    private IVacancyLight mVacancyLightService = null;

    private String tachometerServiceStatus;
    private String vacancyLightServiceStatus;

    private boolean isConnectUart;  //true:Uart(Moose), false:USB(Kiev)
    private Packets.BoardType boardType = Packets.BoardType.Empty;

    //USB Device 관련
    private boolean isAttachedUsb;
//    private static final int USB_VENDOR_ID = SiteConstants.USB_VENDOR_ID; //EP-100 벤더 ID
//    private static final int USB_PRODUCT_ID = SiteConstants.USB_PRODUCT_ID; //EP-100 프로덕트 ID
//    public static final String SERIAL_PORT_USB = SiteConstants.SERIAL_PORT_USB;
//    public static final String SERIAL_PORT_UART = SiteConstants.SERIAL_PORT_UART;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        LogHelper.setTag(getString(R.string.app_name));
        LogHelper.enableDebug(BuildConfig.DEBUG);
    }

    @Override
    public void onCreate() {
        if (!BuildConfig.DEBUG) {
            uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerApplication());
        }
        super.onCreate();

        mConfigLoader = ConfigLoader.getInstance();
        mConfigLoader.initialize(getApplicationContext());

        mContextProvider = ContextProvider.getInstance(this);
        activities = new ArrayList<>();

        registerActivityLifecycleCallbacks(this);

        //usb device 사용 체크
        isConnectUart = mConfigLoader.isConnectUart();
        if (!isConnectUart) {
            registerUsbReceiver();
        }

        Log.d("BaseApplication", "onCreate");
        if (mConfigLoader.hasConfiguration()) {
            Log.d("BaseApplication", "hasConfiquration");    //얘가 실행 안됨
            bindAllServices();
        }
    }

    public static ContextProvider getContextProvider() {
        Log.d("BaseApplication", "BaseApplication - getContextProvider");
        return mContextProvider;
    }

    public ScenarioService getScenarioService() {
        Log.d("BaseApplication", "BaseApplication - getScenarioService");
        return mScenarioService;
    }

    public ConfigLoader getConfigLoader() {
        Log.d("BaseApplication", " - getConfigLoader");
        return mConfigLoader;
    }


    public void bindAllServices() {
        mScenarioService = null;
        mExternalDeviceMainService = null;
        mTachometerService = null;
        mVacancyLightService = null;

        try {
            unbindService(scenarioServiceConnection);
            unbindService(externalDeviceMainConnection);
            unbindService(tachometerConnection);
            unbindService(vacancyLightConnection);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
        }

        bindServices();
        bindCallbackServices();
    }

    private void bindServices() {
        LogHelper.d("bindServices()");
        if (mScenarioService == null) {
            Log.d("여기통과?", "BaseApplication - getConfigLoader - null");


            //dho ScenarioService 실행 안대.....
            Intent intent = new Intent(this, ScenarioService.class);
            startService(intent);
            bindService(intent, scenarioServiceConnection, Context.BIND_AUTO_CREATE);
        }

        if (mExternalDeviceMainService == null) {
            Intent intent = new Intent();
            intent.setAction(AppDefine.PKG_NAME_EXTERNAL_DEVICE_MAIN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                intent.setPackage(AppDefine.PKG_NAME_EXTERNAL_DEVICE);

            intent.putExtra(Constants.INTENT_KEY_PORT_PATH_VACANCY, SiteConstants.SERIAL_PORT_VACANCYLIGHT);
            intent.putExtra(Constants.INTENT_KEY_PORT_PATH_METER, SiteConstants.SERIAL_PORT_TACHOMETER);
            intent.putExtra(Constants.INTENT_KEY_METER_TYPE, mConfigLoader.getMeterDeviceType());

            startService(intent); //초기화 및 시작을 위한 스타티드 서비스 연결
            bindService(intent, externalDeviceMainConnection, BIND_AUTO_CREATE); //포트, 미터기 변경을 위한 위한 바운드 서비스 연결
        }
    }

    private void bindCallbackServices() {
        LogHelper.d("bindCallbackServices()");
        if (mTachometerService == null) {
            Intent intent = new Intent();
            intent.setAction(AppDefine.PKG_NAME_EXTERNAL_DEVICE_TACHOMETER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                intent.setPackage(AppDefine.PKG_NAME_EXTERNAL_DEVICE);
            bindService(intent, tachometerConnection, Context.BIND_AUTO_CREATE);
        }

        if (mVacancyLightService == null) {
            Intent intent = new Intent();
            intent.setAction(AppDefine.PKG_NAME_EXTERNAL_DEVICE_VACANCYLIGHT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                intent.setPackage(AppDefine.PKG_NAME_EXTERNAL_DEVICE);
            bindService(intent, vacancyLightConnection, Context.BIND_AUTO_CREATE);
        }
    }




    /**
     * ScenarioService
     */
    private ServiceConnection scenarioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            LogHelper.e("ScenarioService onServiceConnected()");
            ScenarioService.LocalBinder binder = (ScenarioService.LocalBinder) service;
            mScenarioService = binder.getService();
            if (mScenarioService != null) {
                mScenarioService.setTachometerServiceStatus(tachometerServiceStatus);
                mScenarioService.setVacancyLightServiceStatus(vacancyLightServiceStatus);
                startWakeUpService(mScenarioService, Constants.SERVICE_ID_SCENARIO);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogHelper.e("ScenarioService onServiceDisconnected()");
            if (mScenarioService != null) {
                stopWakUpService(mScenarioService, Constants.SERVICE_ID_SCENARIO);
                mScenarioService.setTachometerServiceStatus(tachometerServiceStatus);
                mScenarioService.setVacancyLightServiceStatus(vacancyLightServiceStatus);
                mScenarioService = null;
            }
            handler.sendEmptyMessageDelayed(MSG_CODE_RESTART_SERVICE, 2000);
        }
    };

    /**
     * ExternalDevices
     */
    private ServiceConnection externalDeviceMainConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogHelper.e("ExternalDevice MainService onServiceConnected()");
            mExternalDeviceMainService = IMainService.Stub.asInterface(iBinder);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogHelper.e("ExternalDevice MainService onServiceDisconnected()");
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService = null;
            }
            handler.sendEmptyMessageDelayed(MSG_CODE_RESTART_SERVICE, 2000);
        }
    };

    /**
     * ExternalDevices aidl 연동 외부 메서드
     */
    // 시리얼 포트 변경
    public void changeSerialPort(String serialPortPath){
        LogHelper.write("#### changeSerialPort");
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.changeSerialPortPath(serialPortPath);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    // 미터기 타입 변경
    public void changeTachometerType(String tachometerType){
        LogHelper.write("#### changeTachometerType");
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.changeTachometerType(tachometerType);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    // 미터기 데이터 요청
    public void requestTachometerData(){
        LogHelper.write("#### requestTachometerData");
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.sendInitDataToTachometer();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    // 빈차등 상태 변경
    public void setVacancyLightStatus(boolean isReservation){
        LogHelper.write("#### setVacancyLightStatus reservation ? : " + isReservation);
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.setVacancyLightStatus(isReservation);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    // Tachometer callback service
    private ServiceConnection tachometerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogHelper.e("tachometerConnection onServiceConnected()");
            if (service != null) {
                mTachometerService = ITachoMeter.Stub.asInterface(service);
                if (mTachometerService != null) {
                    try {
                        mTachometerService.registerCallback(tachometerCallback);
                        tachometerCallback.onServiceStatus(ServiceStatus.SERVICE_LAUNCHED);
                        if(mTachometerService.getCurrentData() != null) {
                            tachometerCallback.onReceive(mTachometerService.getCurrentData());
                        }
                        LogHelper.e("tachometer service callback registered");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogHelper.e("TachoMeterService onServiceDisconnected()");
            if (mTachometerService != null) {
                try {
                    tachometerCallback.onServiceStatus(ServiceStatus.SERVICE_NOT_LAUNCHED);
                    mTachometerService.unregisterCallback(tachometerCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    mTachometerService = null;
                    handler.sendEmptyMessageDelayed(MSG_CODE_RESTART_SERVICE, 2000);
                }
            } else {
                handler.sendEmptyMessageDelayed(MSG_CODE_RESTART_SERVICE, 2000);
            }
        }
    };

    // Tachometer callback
    private int previousTachometerStatus;
    private ITachoMeterCallback tachometerCallback = new ITachoMeterCallback.Stub() {
        @Override
        public void onReceive(final TachoMeterData data) {
            int status = data.getStatus();
            LogHelper.i("new tachometer data? : " + (previousTachometerStatus != status)
                    + " /  tachometer data : " + data.toString());

            if (previousTachometerStatus != status) {
                previousTachometerStatus = status;

                LogHelper.e("배차상태 변경을 미터기로 사용 : " + mConfigLoader.isVacancyLight());
                if (!mConfigLoader.isVacancyLight()) {
                    LogHelper.e("tacho on receive : " + (mScenarioService != null) ); //+ " / " + (mScenarioService.getRestType() != Packets.RestType.Rest));

                    if (mScenarioService != null && mScenarioService.hasCertification()) {
                        if ((status & STATUS_VACANCY) > 0) { // 빈차
                            LogHelper.write("@@ 미터기 : 빈차 (STATUS_VACANCY)");
                        } else if ((status & STATUS_DRIVING) > 0) { // 주행
                            LogHelper.write("@@ 미터기 : 주행 (STATUS_DRIVING)");
                        } else if ((status & STATUS_EXTRA_CHARGE) > 0) { // 할증
                            LogHelper.write("@@ 미터기 : 할증 (STATUS_EXTRA_CHARGE)");
                        } else if ((status & STATUS_PAYMENT) > 0) { // 지불
                            LogHelper.write("@@ 미터기 : 지불 (STATUS_PAYMENT)");
                        } else if ((status & STATUS_CALL) > 0) { // 호출
                            LogHelper.write("@@ 미터기 : 호출 (STATUS_CALL)");
                        }

                        if ((status & STATUS_VACANCY) > 0) { // 빈차
                            boardType = Packets.BoardType.Empty;
                        } else {
                            boardType = Packets.BoardType.Boarding;
                        }

                        if (boardType == Packets.BoardType.Boarding) {
                            mScenarioService.applyDriving(data.getFare(), data.getVacancyMileage());
                        } else if (boardType == Packets.BoardType.Empty) {
                            mScenarioService.applyVacancy(data.getFare(), data.getMileage());
                        }
                    }






//                    if (mRepository != null) {
//                        TachometerData tachometerData = new TachometerData(data);
//                        mRepository.upsertTachometerData(tachometerData);
//                    }
//
//                    if (scenarioService != null && scenarioService.getRestType() != Packets.RestType.Rest) {
//                        CallHistory call = mRepository.getCallInfo();
//                        call.setCallStatusWithTachometerStatus(status);
//                        LogHelper.e("tacho on receive : " + call);
//
//                        if (call.getCallStatus() == Constants.CALL_STATUS_BOARDED) {
//                            mRepository.changeCallStatus(Constants.CALL_STATUS_BOARDED);
//                            if (call.getCallNumber() != 0) {
//                                MainApplication mainApplication = (MainApplication) getApplication();
//                                PersonalConfiguration personalConfig = mConfigViewModel.getPersonalConfiguration();
//
//                                if(personalConfig.isUseAutoRoutingToDestination() && mainApplication.isBackground()) {
//                                    if (call.getDestinationLat() != 0 && call.getDestinationLong() != 0)
//                                        mainApplication.enterApplication();
//                                }
//                            }
//                        } else if(call.getCallStatus() == Constants.CALL_STATUS_VACANCY) {
//                            mRepository.changeCallStatus(Constants.CALL_STATUS_VACANCY);
//                        }
//                    } else if (scenarioService == null) {
//                        LogHelper.e("scenarioService is not ready");
//                        CallHistory call = mRepository.getCallInfo();
//                        call.setCallStatusWithTachometerStatus(status);
//                        LogHelper.e("call status : " + call.getCallStatus());
//                        mRepository.updateCallInfo(call);
//                    }
                }
            }
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d(">> 미터기 서비스 상태: " + status);
            if (status != ServiceStatus.NO_ERROR
                    && status != ServiceStatus.SERVICE_NOT_LAUNCHED
                    && status != ServiceStatus.SERVICE_LAUNCHED) {
                LogHelper.write("#### 미터기 ->  에러");
                tachometerServiceStatus = "서비스 중지됨";

                // 미터기 오류 발생시 휴식 패킷을 통해 오류를 리포트 한다.
                if (mScenarioService != null) {
                    mScenarioService.requestRest(Packets.RestType.TachoMeterError);
                }
            } else {
                tachometerServiceStatus = "서비스 시작됨";
            }

            if (mScenarioService != null)
                mScenarioService.setTachometerServiceStatus(tachometerServiceStatus);
        }
    };


    // Vacancy Light callback service
    private IVacancyLight vacancyLightService;
    private ServiceConnection vacancyLightConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                LogHelper.e("vacancyLightService onServiceConnected()");
                vacancyLightService = IVacancyLight.Stub.asInterface(service);
                try {
                    vacancyLightService.registerCallback(vacancyLightCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogHelper.e("VacancyLightService onServiceDisconnected()");
            if (vacancyLightService != null) {
                try {
                    vacancyLightService.unregisterCallback(vacancyLightCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                vacancyLightService = null;
            } else {
                handler.sendEmptyMessageDelayed(MSG_CODE_RESTART_SERVICE, 2000);
            }
        }
    };

    // VacancyLight callback
    private IVacancyLightCallback vacancyLightCallback = new IVacancyLightCallback.Stub() {
        @Override
        public void onReceive(VacancyLightData data) throws RemoteException {
            if (mConfigLoader != null) {
                LogHelper.e("빈차등 상태: " + data.getStatus());
                LogHelper.e("배차상태 변경을 빈차등으로 사용 : " + mConfigLoader.isVacancyLight());
                if (mConfigLoader.isVacancyLight()) {
                    int status = data.getStatus();
                    switch (status) {
                        case VacancyLightData.VACANCY: // 빈차
                            LogHelper.write("@@ 빈차등 : 빈차 (VACANCY)");
                            boardType = Packets.BoardType.Empty;
                            break;
                        case VacancyLightData.RIDDEN: // 승차
                            LogHelper.write("@@ 빈차등 : 승차 (RIDDEN)");
                            boardType = Packets.BoardType.Boarding;
                            break;
                        case VacancyLightData.RESERVATION: // 예약
                            LogHelper.write("@@ 빈차등 : 예약 (RESERVATION)");
                            break;
                        case VacancyLightData.DAY_OFF: // 휴무
                            LogHelper.write("@@ 빈차등 : 휴무 (DAY_OFF)");
                            break;
                        default:
                            break;
                    }

                    if (mScenarioService != null) {
                        if (boardType == Packets.BoardType.Boarding) {
                            mScenarioService.applyDriving(0, 0);
                        } else if (boardType == Packets.BoardType.Empty) {
                            mScenarioService.applyVacancy(0, 0);
                        }
                    }

//                    if (mRepository != null) {
//                        com.kiev.driver.pnd.model.entity.VacancyLightData vacancyLightData = new com.kiev.driver.pnd.model.entity.VacancyLightData(data);
//                        mRepository.upsertVacancyLightData(vacancyLightData);
//                    }
//
//                    if (scenarioService != null &&  scenarioService.getRestType() != Packets.RestType.Rest) {
//                        CallHistory call = mRepository.getCallInfo();
//                        call.setCallStatusWithVacancyLightStatus(status);
//
//                        if (call.getCallStatus() == Constants.CALL_STATUS_BOARDED) {
//                            mRepository.changeCallStatus(Constants.CALL_STATUS_BOARDED);
//                            if (call.getCallNumber() != 0) {
//                                MainApplication mainApplication = (MainApplication) getApplication();
//                                PersonalConfiguration personalConfig = mConfigViewModel.getPersonalConfiguration();
//
//                                if (personalConfig.isUseAutoRoutingToDestination() && mainApplication.isBackground()) {
//                                    if (call.getDestinationLat() != 0 && call.getDestinationLong() != 0)
//                                        mainApplication.enterApplication();
//                                }
//                            }
//                        } else {
//                            if(call.getCallStatus() == Constants.CALL_STATUS_VACANCY
//                                    || call.getCallStatus() == Constants.CALL_STATUS_RESTING) {
//                                mRepository.changeCallStatus(call.getCallStatus());
//                            }
//                        }
//                    }
                }
            }
        }
        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.e(">> 빈차등 서비스 상태: " + status);
            // FIXME: 2019-10-04 아래 주석 해제시 externalDevice NPE가 발생함. 원인파악 필요
            if (status != ServiceStatus.NO_ERROR
                    && status != ServiceStatus.SERVICE_NOT_LAUNCHED
                    && status != ServiceStatus.SERVICE_LAUNCHED
                    && status != 200) { //FINISHED_WRITE_DATA 데이터 쓰기 완료
                vacancyLightServiceStatus = "서비스 중지됨";

                // 빈차등 오류 발생시 휴식 패킷을 통해 오류를 리포트 한다.
                if (mScenarioService != null)
                    mScenarioService.requestRest(Packets.RestType.VacancyError);
            } else {
                vacancyLightServiceStatus = "서비스 시작됨";
            }

            if (mScenarioService != null)
                mScenarioService.setVacancyLightServiceStatus(vacancyLightServiceStatus);
        }
    };



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CODE_RESTART_SERVICE:
                    LogHelper.e("handleMessage : " + msg.what);
                    bindAllServices();
                    break;
            }
        }
    };




    /**
     * USB Device가 붙었다 끊어지는 이슈가 있어서 감지한다.
     */
    private void registerUsbReceiver() {
        LogHelper.write("#### registerUsbReceiver");
        isAttachedUsb = isAttachedUsb(SiteConstants.USB_VENDOR_ID, SiteConstants.USB_PRODUCT_ID);

        LogHelper.write("#### isAttachedUsb = " + isAttachedUsb);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    private boolean isAttachedUsb(int vendorId, int productId) {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if (deviceList != null) {
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                if (device != null) {
                    if (vendorId == device.getVendorId() && productId == device.getProductId()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogHelper.write("#### USB action = " + action);
            LogHelper.write("#### isAttachedUsb = " + isAttachedUsb);

            boolean isAttachedUsbChk = isAttachedUsb(SiteConstants.USB_VENDOR_ID, SiteConstants.USB_PRODUCT_ID);
            LogHelper.d("isAttachedUsbChk : " + isAttachedUsbChk);

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if (!isAttachedUsbChk) {
                    // USB 장치가 떨어져 있는 상태라면 다시 연결 한다.
                    isAttachedUsb = isAttachedUsb(SiteConstants.USB_VENDOR_ID, SiteConstants.USB_PRODUCT_ID);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                isAttachedUsb = isAttachedUsb(SiteConstants.USB_VENDOR_ID, SiteConstants.USB_PRODUCT_ID);
            }
        }
    };

    /**
     * 모뎀 전화번호를 가져온다.
     */
    public void requestModemNumber() {
        //초기화
        mConfigLoader.setModemNumber("");
        ATCommandManager.getInstance().request(
                ATCommandManager.CMD_MODEM_NO,
                new ATCommandManager.IModemListener() {
                    @Override
                    public void onModemResult(String result) {
                        if (result != null && result.contains(ATCommandManager.CMD_MODEM_NO_CONTAINS)) {
                            String modemNumber = ATCommandManager.getInstance().parseModemNumber(result);
                            modemNumber = StringUtil.changePhonePrefixToDomestic(modemNumber);
                            LogHelper.e("modem number : " + modemNumber);
                            mConfigLoader.setModemNumber(modemNumber);
//                            mConfigLoader.save();
                        }
                    }
                });
    }


    public static void setGlobalData(String key, Object value) {
        globalData.put(key, value);
    }

    public static Object getGlobalData(String key) {
        return globalData.get(key);
    }

    public static void removeGlobalData(String key) {
        globalData.remove(key);
    }


    /**
     * 서비스 알람 등록 : LMK에 의해 서비스가 죽는걸 방지하기 위함
     */
    public void startWakeUpService(Service service, int requestCode) {
        long intervalTime = 1000 * 300;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent wakeUpIntent = new Intent(this, service.getClass());
        PendingIntent wakeUpPendingIntent = PendingIntent.getService(this, requestCode, wakeUpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalTime, wakeUpPendingIntent);
    }

    /**
     * 서비스 알람 등록 해제
     */
    public void stopWakUpService(Service service, int requestCode) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent wakeUpIntent = new Intent(this, service.getClass());
        PendingIntent wakeUpPendingIntent = PendingIntent.getService(this, requestCode, wakeUpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(wakeUpPendingIntent);
        wakeUpPendingIntent.cancel();
    }

    public void resetServiceAndRestartApplication() {
        LogHelper.e("resetServiceAndRestartApplication()");
		if (mScenarioService != null ) {
            mScenarioService.reset();
		}
        restartApplication();
        closeApplication();
    }

    public void restartApplication() {
        LogHelper.e("restartApplication()");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 500,
                PendingIntent.getActivity(this, 123456, intent, PendingIntent.FLAG_CANCEL_CURRENT));
    }

    public void closeApplication() {
        LogHelper.e("closeApplication()");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }






    public Activity getActivity(Class<?> cls) {
        if (cls != null) {
            String name = cls.getSimpleName();
            for (Activity act : activities) {
                if (act.getClass().getSimpleName().equals(name)) {
                    return act;
                }
            }
        }
        return null;
    }

    public void setCurrentActivity(Activity lastActivity) {
        if (lastActivity == null)
            LogHelper.e("reset last Activity");

        if (!(lastActivity instanceof IntroActivity) && !(lastActivity instanceof PopupActivity))
            this.mCurrentActivity = lastActivity;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public enum AppStatus {
        BACKGROUND,                // app is background
        RETURNED_TO_FOREGROUND,    // app returned to foreground(or first launch)
        FOREGROUND;                // app is foreground
    }

    // running activity count
    private int running = 0;
    private Activity mCurrentActivity;
    private AppStatus appStatus;
    private boolean wasBackground;

    // check if app is return foreground
    public boolean isReturnedForground() {
        return appStatus.ordinal() == AppStatus.RETURNED_TO_FOREGROUND.ordinal();
    }

    public boolean isBackground() {
        return appStatus.ordinal() == AppStatus.BACKGROUND.ordinal();
    }

    public boolean wasBackground() {
        return wasBackground;
    }

    public void setWasBackground(boolean wasBackground) {
        this.wasBackground = wasBackground;
    }

    public void enterApplication() {
        LogHelper.e("enterApplication()");
        Intent intent;
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, currentActivity.getClass());
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        activities.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        LogHelper.e("onActivityStarted() : " + activity.getClass().getSimpleName());
        if (++running == 1) {
            // running activity is 1,
            // app must be returned from background just now (or first launch)
            appStatus = AppStatus.RETURNED_TO_FOREGROUND;
        } else if (running > 1) {
            // 2 or more running activities,
            // should be foreground already.
            appStatus = AppStatus.FOREGROUND;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
        //팝업 액티비티가 아닌 경우에만 mCurrentActivity 로 할당
        if (!(activity instanceof PopupActivity) && !(activity instanceof LoginActivity)) {
            mCurrentActivity = activity;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LogHelper.e("onActivityStopped() : " + activity.getClass().getSimpleName() + " / running : " + running);
        if (--running == 0) {
            appStatus = AppStatus.BACKGROUND;
            if (mCurrentActivity != null) {
                LogHelper.e("last activity : " + mCurrentActivity.getClass());
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activities.remove(activity);
    }


    private class UncaughtExceptionHandlerApplication implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // FATAL Exception 발생시 로그를 저장하고 앱을 재실행 한다.
            if (uncaughtExceptionHandler != null && ex != null) {
				if (mScenarioService != null) {
                    mScenarioService.reset();
				}

//                Intent adbIntent = new Intent(Constants.INTENT_ACTION_SERVICEX_ADB_COMMAND);
//                adbIntent.putExtra(Constants.INTENT_KEY_ADB_COMMAND, "setprop " + Constants.SP_KEY_LOGIN_STATUS + " " + Constants.SP_VAL_LOGIN_STATUS_LOGOUT);
//                sendBroadcast(adbIntent);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String date = dateFormat.format(Calendar.getInstance().getTime());

                StringBuffer sb = new StringBuffer();

                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                ex.printStackTrace(printWriter);

                Throwable cause = ex.getCause();
                while (cause != null) {
                    cause.printStackTrace(printWriter);
                    cause = cause.getCause();
                }

                printWriter.close();
                String result = writer.toString();
                sb.append(result);

                LogHelper.write("#### FATAL EXCEPTION");
                LogHelper.write(result);
                LogHelper.disableWriteLogFile();

                File directory = new File(getInternalDir());
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                try {
                    File file = new File(directory, "Fatal-" + date + "-log.txt");
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        fos.write(sb.toString().getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //uncaughtExceptionHandler.uncaughtException(thread, ex);
            }

            restartApplication();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    public String getInternalDir() {
        return getExternalFilesDir(null) + File.separator + "log";
    }

    public String getExternalDir() {
        File[] files = getExternalFilesDirs(null);
        if (files == null || files.length <= 1) {
            return getExternalFilesDir(null).getAbsolutePath() + File.separator + "log";
        } else {
            return files[1].getAbsolutePath() + File.separator + "log";
        }
    }


    private AppCompatDialog progressDialog;

    public void progressOn(Activity activity) {
        progressOn(activity, "");
    }

    public void progressOn(Activity activity, String message) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            setProgressMsg(message);
        } else {
            progressDialog = new AppCompatDialog(activity, R.style.ProgressDialogStyle);
            progressDialog.setCancelable(false);
            progressDialog.setContentView(R.layout.view_loading_progress);
            progressDialog.show();
            setProgressMsg(message);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressOff();
                }
            }, PROGRESS_DIALOG_DISPLAY_TIME_MAX);
        }
    }

    private void setProgressMsg(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        TextView tv_progress_message = progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressOff() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                Log.d("차량인증", "BaseApplication - progressOff 성공");
            }
            Log.d("차량인증", "BaseApplication - progressOff 실패");
        } catch (final IllegalArgumentException e) {
            Log.d("IllegalArgument", e.getMessage());
            e.printStackTrace();
        } catch (final Exception e) {
            LogHelper.e("Exception", e.getMessage());
        } finally {
           progressDialog = null;
        }
    }
}
