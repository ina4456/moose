package com.insoline.pnd.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.insoline.pnd.BuildConfig;
import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.common.BaseApplication;
import com.insoline.pnd.config.ConfigLoader;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.define.AppDefine;
import com.insoline.pnd.define.NetworkUrlDefine;
import com.insoline.pnd.define.ParamDefine;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.model.WaitingZone;
import com.insoline.pnd.network.NetworkData;
import com.insoline.pnd.network.NetworkLoader;
import com.insoline.pnd.network.listener.NetworkRunListener;
import com.insoline.pnd.remote.manager.NetworkListener;
import com.insoline.pnd.remote.manager.NetworkManager;
import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.remote.packets.RequestPacket;
import com.insoline.pnd.remote.packets.ResponsePacket;
import com.insoline.pnd.remote.packets.mdt2server.LivePacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestAckPacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestCallInfoPacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestCallOrderRealtimePacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestConfigPacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestMessagePacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestNoticePacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestPeriodSendingPacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestReportPacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestRestPacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestServicePacket;
import com.insoline.pnd.remote.packets.mdt2server.RequestWaitCallInfoPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseAckPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallInfoPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallOrderPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallOrderProcPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseConfigPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseMessagePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseNoticePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponsePeriodSendingPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseReportPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseRestPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseServicePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseWaitCallInfoPacket;
import com.insoline.pnd.utils.EncryptUtil;
import com.insoline.pnd.utils.GpsHelper;
import com.insoline.pnd.utils.INaviExecutor;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.PreferenceUtil;
import com.insoline.pnd.utils.StringUtil;
import com.insoline.pnd.utils.WavResourcePlayer;
import com.insoline.pnd.view.CallBoardingActivity;
import com.insoline.pnd.view.CallInfoActivity;
import com.insoline.pnd.view.CallReceiveActivity;
import com.insoline.pnd.view.LoginActivity;
import com.insoline.pnd.view.MainActivity;
import com.insoline.pnd.view.PopupActivity;
import com.insoline.pnd.view.custom.DebugWindow;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScenarioService extends Service {

    //----------------------------------------------------------------------------------------
    // fields
    //----------------------------------------------------------------------------------------
    public static final int MSG_LIVE = 1;
    public static final int MSG_SERVICE_ACK = 2;
    public static final int MSG_PERIOD = 3;
    public static final int MSG_ACK = 4;
    public static final int MSG_REPORT = 5;

//    public static final int MSG_AREA_CHECK = 6;
//    public static final int MSG_REQ_WAIT_AREA_STATE = 7;
//    public static final int MSG_DEVICE_WATCH = 8;
//    public static final int MSG_EMERGENCY = 9;

    private final IBinder binder = new ScenarioService.LocalBinder();
    private Context mContext;
    private ConfigLoader mConfigLoader;
    private NetworkManager mNetworkManager;
    private GpsHelper mGpsHelper;
    private PreferenceUtil mPreferenceUtil;
    private BaseApplication mBaseApplication;

    private boolean hasCertification; // 서비스 인증 성공 여부
    // 모바일 배차를 받고 승차보고가 올라가기 전까지는 주기 전송 시간을 cfgLoader.getRc()로 한다.
    // 모바일 배차 승차보고 후 주기 시간을 정상적으로 되돌리기 위해 사용 한다
    private int periodTerm;

    private String tachometerServiceStatus;
    private String vacancyLightServiceStatus;

    //    private boolean isAvailableNetwork, isValidPort; // DebugWindow에 네트워크 상태, Port 상태를 표시하기 위함
    private boolean isDestroyed;
    private boolean isUsedDestination; // 목적지 정보 사용여부

    private Packets.BoardType boardType; // 승차 상태
    private Packets.RestType restType; // 휴식 상태
    private int reportRetryCount, ackRetryCount;


//    private Packets.EmergencyType emergencyType; // 긴급상황 상태
    // 전체 화면 Activity 팝업(공지사항, 메시지 등)이 보여질 때 이전 상태가 Background 였는지 저장 한다.
    // 이전 상태가 Background 였다면 MainActivity가 보여지지 않도록 아이나비를 한번 더 호출해 준다.
    //private boolean isPrevStatusBackground;

//    private FragmentManager supportFragmentManager;
//    private TachoMeterService serviceTachoMeter;
//    private VacancyLightService serviceVacancyLight;


    public class LocalBinder extends Binder {
        public ScenarioService getService() {
            return ScenarioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        mConfigLoader = ConfigLoader.getInstance();
        periodTerm = mConfigLoader.getPst();

        mPreferenceUtil = new PreferenceUtil(mContext);
        mBaseApplication = (BaseApplication) this.getApplicationContext();

        if (mGpsHelper == null) {
            mGpsHelper = new GpsHelper(mContext);
        }

        if (mNetworkManager == null) {
            mNetworkManager = NetworkManager.getInstance();
            mNetworkManager.addNetworkListener(networkListener);
        }

        hasCertification = false;
        boardType = Packets.BoardType.Empty;
        restType = Packets.RestType.Working;
        reportRetryCount = 0;
        ackRetryCount = 0;
//        emergencyType = Packets.EmergencyType.End;
//        isAvailableNetwork = !mNetworkManager.isAvailableNetwork(mContext);
//        isValidPort = !isValidPort();

        isDestroyed = false;
//        pollingHandler.sendEmptyMessage(MSG_DEVICE_WATCH);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("ScenarioService", "onBind");//onBind()는 서비스와 클라이언트 사이의 인터페이스 역할을 하는 iBind를 반환.
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ScenarioService", "onDestroy");
        destroy();
    }

    public void destroy() {
        if (mNetworkManager != null) {
            mNetworkManager.removeNetworkListener(networkListener);
            mNetworkManager.disconnect();
        }
        if (mGpsHelper != null) {
            mGpsHelper.destroy();
        }

        isDestroyed = true;
        if (pollingHandler != null) {
            pollingHandler.removeMessages(MSG_LIVE);
            pollingHandler.removeMessages(MSG_SERVICE_ACK);
            pollingHandler.removeMessages(MSG_PERIOD);
            pollingHandler.removeMessages(MSG_ACK);
            pollingHandler.removeMessages(MSG_REPORT);

//            pollingHandler.removeMessages(MSG_AREA_CHECK);
//            pollingHandler.removeMessages(MSG_DEVICE_WATCH);
//            pollingHandler.removeMessages(MSG_EMERGENCY);
        }
    }

    // 서비스 인증 성공 여부
    public boolean hasCertification() {
        Log.d("ScenarioService", "hasCerification");//ok1
        return hasCertification;
    }

    public void setTachometerServiceStatus(String tachometerServiceStatus) {
        //Log.d("TachometerServiceStatus" , tachometerServiceStatus);
        this.tachometerServiceStatus = tachometerServiceStatus;
    }

    public String getTachometerServiceStatus() {
        LogHelper.e("getTachometerServiceStatus : " + tachometerServiceStatus);
        return tachometerServiceStatus;
    }

    public void setVacancyLightServiceStatus(String vacancyLightServiceStatus) {
        LogHelper.e("setVacancyLightServiceStatus : " + vacancyLightServiceStatus);
        this.vacancyLightServiceStatus = vacancyLightServiceStatus;
    }

    public String getVacancyLightServiceStatus() {
        LogHelper.e("getVacancyLightServiceStatus : " + vacancyLightServiceStatus);
        return vacancyLightServiceStatus;
    }


    //----------------------------------------------------------------------------------------
    // polling & timer
    //----------------------------------------------------------------------------------------
    @SuppressLint("HandlerLeak")
    private Handler pollingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isDestroyed) {
                return;
            }
            Log.d("ScenarioService", "Handler msg="+msg);//ok1

            Popup popup = null;
            Activity activity = null;
            switch (msg.what) {
                case MSG_LIVE:
                    requestLive();
                    pollingHandler.sendEmptyMessageDelayed(MSG_LIVE, mConfigLoader.getRt() * 1000);
                    break;

                case MSG_SERVICE_ACK: //인증 실패
                    LogHelper.d(">> Failed to certify in 3 sec.");
                    WavResourcePlayer.getInstance(mContext).play(R.raw.voice_103);
                    popup = new Popup
                            .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_LOGIN_ERROR)
                            .setTitle(getString(R.string.fail_cert))
                            .setContent(getString(R.string.popup_msg_login_failed_network_disconnected))
                            .build();

                    activity = mBaseApplication.getCurrentActivity();
                    if (activity != null)
                        ((BaseActivity) activity).showPopupDialog(popup);
                    break;

                case MSG_PERIOD:    //주기전송
                    requestPeriodSending();
                    int period = msg.arg1;

                    Message msgNew = obtainMessage();
                    msgNew.what = MSG_PERIOD;
                    msgNew.arg1 = period;
                    sendMessageDelayed(msgNew, period * 1000);
                    break;

                case MSG_ACK:
                    if (ackRetryCount >= 3) {
                        removeMessages(MSG_ACK);
                    } else {
                        RequestAckPacket packet = (RequestAckPacket) msg.obj;
                        request(packet);
                        ackRetryCount++;

                        Message newMsg = obtainMessage();
                        newMsg.what = MSG_ACK;
                        newMsg.obj = packet;
                        sendMessageDelayed(newMsg, 5000);
                    }
                    break;

                case MSG_REPORT:
                    RequestReportPacket sp = (RequestReportPacket) msg.obj;
                    if (reportRetryCount >= 3) {
                        if (sp.getReportKind() == Packets.ReportKind.Failed) {
                            refreshSavedPassengerInfo(sp.getCallNumber());
                        }
                        removeMessages(MSG_REPORT);
                    } else {
                        request(sp);
                        reportRetryCount++;

                        Message newMsg = obtainMessage();
                        newMsg.what = MSG_REPORT;
                        newMsg.obj = sp;
                        sendMessageDelayed(newMsg, 5000);
                    }
                    break;

//                case MSG_AREA_CHECK:
//                    LogHelper.d(">> Wait Area : Search");
//                    WaitingZone waitingZone = mPreferenceUtil.getWaitArea();
//                    LogHelper.d(">> Wait Area : Speed -> " + mGpsHelper.getSpeed());
//                    if (waitingZone != null && mGpsHelper.getSpeed() > 5) {
//                        float distance = mGpsHelper.getDistance(waitingZone.getLatitude(), waitingZone.getLongitude());
//                        LogHelper.d(">> Wait Area : distance -> " + distance + ". range -> " + waitingZone.getWaitRange());
//                        if (distance > waitingZone.getWaitRange()) {
//                            LogHelper.d(">> Wait Area : Out of area");
//                            removeMessages(MSG_AREA_CHECK);
//                            //대기 취소 API(?) 처리
////                            requestWaitCancel(waitingZone.getWaitingZoneId());
//                            popup = new Popup
//                                    .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_EXIT_WAITING_ZONE)
//                                    .setContent(getString(R.string.wz_msg_exit_waiting_zone))
//                                    .setBtnLabel(getString(R.string.popup_btn_confirm), null)
//                                    .setDismissSecond(3)
//                                    .build();
//
//                            activity = mBaseApplication.getCurrentActivity();
//                            if (activity != null)
//                                ((BaseActivity) activity).showPopupDialog(popup);
//                            return;
//                        }
//                    }
//                    sendEmptyMessageDelayed(MSG_AREA_CHECK, 5000);
//                    break;

//                case MSG_REQ_WAIT_AREA_STATE:
//                    requestWaitAreaStateInner();
//                    break;

//                case MSG_DEVICE_WATCH:
//                    watchDevice();
//                    sendEmptyMessageDelayed(MSG_DEVICE_WATCH, 1500);
//                    break;
//                case MSG_EMERGENCY:
//                    requestEmergency();
//                    sendEmptyMessageDelayed(MSG_EMERGENCY, mConfigLoader.getEmergencyPeriodTime() * 1000);
//                    break;
            }
        }
    };

    /**
     * NetworkManager Callback
     */
    private NetworkListener networkListener = new NetworkListener() {

        @Override
        public void onConnectedServer() {
            LogHelper.write("#### Socket Connected");
            if (hasCertification) {
                // 소켓 연결이 끊어진 후 재접속시 주기 전송을 한번 한다.
                requestPeriodSending();
            }
        }

        @Override
        public void onDisconnectedServer(ErrorCode code) {
            LogHelper.write("#### Connect Error : " + code);
        }

        @Override
        public void onReceivedPacket(@NonNull ResponsePacket response) {
            LogHelper.e(">> RES " + response);
            int messageType = response.getMessageType();
            Log.d("scenarioService", "ReceivedPacket-MessageType"+messageType);
            if (messageType != Packets.RESPONSE_SERVICE && !hasCertification) {
                LogHelper.d(">> Skip received packet. Invalid service certification.");
                return;
            }

            switch (messageType) {
                case Packets.RESPONSE_SERVICE: { // 서비스 요청 응답 (1112)
                    ResponseServicePacket responsePacket = (ResponseServicePacket) response;
                    responseService(responsePacket);
                }
                break;

                case Packets.RESPONSE_PERIOD_SENDING: { // 주기 전송 응답
                    ResponsePeriodSendingPacket responsePacket = (ResponsePeriodSendingPacket) response;
                    responsePeriodSending(responsePacket);
                }
                break;

                case Packets.RESPONSE_CONFIG: { // 환경 설정 응답
                    ResponseConfigPacket responsePacket = (ResponseConfigPacket) response;
                    responseConfig(responsePacket);
                }
                break;

                case Packets.RESPONSE_REST: { // 휴식/운행재개 응답
                    ResponseRestPacket responsePacket = (ResponseRestPacket) response;
                    responseRest(responsePacket);
                }
                break;

                case Packets.RESPONSE_ACK: { // ACK 응답
                    pollingHandler.removeMessages(MSG_ACK);
                }
                break;

                case Packets.RESPONSE_NOTICE: { // 공지사항 응답
                    ResponseNoticePacket responsePacket = (ResponseNoticePacket) response;
                    showNoticePopup(true, responsePacket.getNoticeTitle(), responsePacket.getNotice());
                }
                break;

                case Packets.RESPONSE_MESSAGE: { // 메시지 응답
                    ResponseMessagePacket responsePacket = (ResponseMessagePacket) response;
                    showNoticePopup(false, "", responsePacket.getMessage());
                }
                break;

                case Packets.RESPONSE_CALL_INFO: { // 배차고객정보 고객정보재전송 (목적지 추가)
                    ResponseCallInfoPacket responsePacket = (ResponseCallInfoPacket) response;
                    responseCallInfo(responsePacket, messageType);
                }
                break;

                case Packets.RESPONSE_CALL_ORDER: {  // 배차데이터 (목적지 추가)
                    //휴식 상태이면 콜 수신 무시
                    if (restType == Packets.RestType.Rest) {
                        LogHelper.e(">> Skip receive call broadcast.");
                        return;
                    }

                    ResponseCallOrderPacket responsePacket = (ResponseCallOrderPacket) response;
                    responseCallOrder(responsePacket);
                }
                break;

                case Packets.RESPONSE_CALL_ORDER_PROC: { // 배차데이터 처리 응답
                    ResponseCallOrderProcPacket responsePacket = (ResponseCallOrderProcPacket) response;
                    responseCallOrderProc(responsePacket, messageType);
                }
                break;

                case Packets.RESPONSE_REPORT: { // 운행보고 응답
                    ResponseReportPacket responsePacket = (ResponseReportPacket) response;
                    responseReport(responsePacket);
                }
                break;


//                case Packets.RESPONSE_WAIT_CALL_INFO: { // 대기배차고객정보 응답 (1518)
//                    ResponseWaitCallInfoPacket responsePacket = (ResponseWaitCallInfoPacket) response;
//                    if (responsePacket != null && responsePacket.getCallNumber() > 0) {
//                        WavResourcePlayer.getInstance(mContext).play(R.raw.voice_132);
//                        //예약등
//                        mBaseApplication.setVacancyLightStatus(true);
//
//                        // 서버에 Packet을 한번 요청하면 데이터가 초기화 되기 때문에 콜번호가 유효한 경우에만 저장을 한다.
//                        mPreferenceUtil.setCallInfoWait(responsePacket);
//
//                        // 대기 배차 완료시 서버의 대기목록에서 빠지므로 로컬 파일 지우도록 한다.
//                        mPreferenceUtil.clearWaitArea();
//
//                        requestAck(responsePacket.getMessageType(), mConfigLoader.getServiceNumber(), responsePacket.getCallNumber());
//
//                        //고객정보 화면
//                        showCallInfoActivity();
//
//                        LogHelper.write("#### 콜 수락(대기) -> callNo : " + responsePacket.getCallNumber());
//                    }
//                }
//                break;
//                case Packets.WAIT_PLACE_INFO: { // 대기지역 정보
//                    WaitPlaceInfoPacket packet = (WaitPlaceInfoPacket) response;
//                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
//                    if (f != null && f instanceof WaitCallFragment) {
//                        ((WaitCallFragment) f).apply(packet);
//                    }
//                }
//                break;
//                case Packets.RESPONSE_WAIT_DECISION: { // 대기결정응답
//                    ResponseWaitDecisionPacket packet = (ResponseWaitDecisionPacket) response;
//                    if (packet.getWaitProcType() == Packets.WaitProcType.Success) {
//                        PreferenceUtil.setWaitArea(context, packet);
//                        pollingCheckWaitRange(true);
//
//                        Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
//                        if (f != null && f instanceof WaitCallFragment) {
//                            ((WaitCallFragment) f).successWait();
//                        }
//                    } else {
//                        Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
//                        if (f != null && f instanceof WaitCallFragment) {
//                            ((WaitCallFragment) f).failWait();
//                        }
//                    }
//                }
//                break;
//                case Packets.RESPONSE_WAIT_CANCEL: { // 대기취소응답
//                    WavResourcePlayer.getInstance(mContext).play(R.raw.voice_140);
//                    PreferenceUtil.clearWaitArea(context);
//                    pollingCheckWaitRange(false);
//
//                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
//                    if (f != null && f instanceof WaitCallFragment) {
//                        ((WaitCallFragment) f).cancelWait();
//                    }
//                }
//                break;
//                case Packets.RESPONSE_WAIT_AREA_STATE: {
//                    ResponseWaitAreaStatePacket p = (ResponseWaitAreaStatePacket) response;
//                    WaitStateActivity activity = WaitStateActivity.getInstance();
//                    if (activity != null) {
//                        activity.apply(p);
//                    }
//                }
//                break;
//                case Packets.CANCEL_EMERGENCY: { // Emergency 응답
//                    emergencyType = Packets.EmergencyType.End;
//                    pollingHandler.removeMessages(MSG_EMERGENCY);
//
//                    if (serviceEmergency != null) {
//                        serviceEmergency.setEmergencyOff();
//                    }
//                    // 응급 상황 해제 후에 주기 전송을 재시작 한다.
//                    periodTerm = cfgLoader.getPst();
//                    WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
//                    OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
//                    if (wait != null
//                            && wait.getOrderKind() == Packets.OrderKind.Mobile
//                            && !wait.isReported()) {
//                        periodTerm = cfgLoader.getRc();
//                    } else if (normal != null
//                            && normal.getOrderKind() == Packets.OrderKind.Mobile
//                            && !normal.isReported()) {
//                        periodTerm = cfgLoader.getRc();
//                    }
//                    pollingPeriod(periodTerm);
//                }
//                break;
//                case Packets.RESPONSE_ACCOUNT: { // 콜정산정보 응답
//                    ResponseAccountPacket p = (ResponseAccountPacket) response;
//                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
//                    if (f != null && f instanceof QueryCallDetailFragment) {
//                        ((QueryCallDetailFragment) f).apply(p);
//                    }
//                }
//                break;
            }
        }
    };


    //----------------------------------------------------------------------------------------
    // request
    //----------------------------------------------------------------------------------------

    public String getServiceStatus(int status) {
        String ret = "알 수 없음";
//        switch (status) {
//            case ServiceStatus.UNKNOWN_ERROR:
//                ret = "알 수 없는 오류";
//                break;
//            case ServiceStatus.SERVICE_NOT_LAUNCHED:
//                ret = "서비스 기능이 시작되지 않음(초기상태)";
//                break;
//            case ServiceStatus.SERVICE_LAUNCHED:
//                ret = "서비스 기능이 시작됨";
//                break;
//            case ServiceStatus.FAILED_SET_HANDLER:
//                ret = "서비스에서 사용하는 handler 생성 오류";
//                break;
//            case ServiceStatus.FAILED_PORT_OPENED:
//                ret = "외부 포트 열기 실패";
//                break;
//            case ServiceStatus.FAILED_SET_PARSER:
//                ret = "내부 파서 생성 실패";
//                break;
//            case ServiceStatus.FAILED_SET_THREAD:
//                ret = "Thread 생성 실패";
//                break;
//            case ServiceStatus.FAILED_SET_QUEUE:
//                ret = "Queue 생성 실패";
//                break;
//            case ServiceStatus.FAILED_READ_DATA:
//                ret = "외부기기에서 데이터 읽기 실패";
//                break;
//            case ServiceStatus.FAILED_WRITE_DATA:
//                ret = "외부기기에 데이터 쓰기 실패";
//                break;
//            case ServiceStatus.FAILED_USE_NOT_CONNECTED:
//                ret = "포트가 물리적으로 연결되어 있지 않음";
//                break;
//        }
        return ret;
    }

    /**
     * 패킷을 요청한다.
     * Socket 접속이 끊어지는 경우를 방지하기 위해
     * 마지막 요청 이후 일정 시간 이내에 요청이 없을 경우
     * Live 패킷을 전송하여 Connection을 유지한다.
     */
    public void request(RequestPacket packet) {
        mNetworkManager.request(mContext, packet);

        // 마지막으로 요청 된 리퀘스트 이후 일정 시간 부터 라이브 전송 한다.
        pollingHandler.removeMessages(MSG_LIVE);
        pollingHandler.sendEmptyMessageDelayed(MSG_LIVE, mConfigLoader.getRt() * 1000);
    }

    /**
     * 라이브 패킷을 요청한다.
     */
    private void requestLive() {
        LivePacket packet = new LivePacket();
        packet.setCarId(mConfigLoader.getCarId());
        request(packet);
    }

    /**
     * 서비스요청(인증) 패킷을 요청 한다. (1111->1112)
     * 요청 후 3초 이내에 응답이 없을 경우 Error를 보여준다.
     *
     * @param driverNumber
     * @param withTimer
     */
    public void requestService(String driverNumber, boolean withTimer) {
        Log.d("ScenarioService", "requestService");//ok1
        driverNumber = driverNumber.replaceAll("-", "");

        RequestServicePacket packet = new RequestServicePacket();
        packet.setServiceNumber(mConfigLoader.getServiceNumber());
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        packet.setPhoneNumber(driverNumber);
        packet.setCorporationType(mConfigLoader.isCorporation() ? Packets.CorporationType.Corporation : Packets.CorporationType.Indivisual);
        packet.setProgramVersion(mConfigLoader.getAppVersion());
        packet.setModemNumber(mConfigLoader.getModemNumber());
        request(packet);

        pollingHandler.removeMessages(MSG_SERVICE_ACK);
        if (withTimer) {
            pollingHandler.sendEmptyMessageDelayed(MSG_SERVICE_ACK, 3000);
        }
    }

    /**
     * 인증요청 응답 처리
     *
     * @param responsePacket
     */
    private void responseService(ResponseServicePacket responsePacket) {
        pollingHandler.removeMessages(MSG_SERVICE_ACK);

        //로그인 화면에 인증 결과 전달
        Activity activity = mBaseApplication.getActivity(LoginActivity.class);
        if (activity != null) {
            Log.d("차량인증 responseService", responsePacket.getCertificationResult()+", "+responsePacket.getCertCode());
            ((LoginActivity) activity).applyCertificationResult(responsePacket.getCertificationResult(), responsePacket.getCertCode());
        }

        //인증 성공
        if (responsePacket.getCertificationResult() == Packets.CertificationResult.Success) {
            hasCertification = true;
            isUsedDestination = responsePacket.isUsedDestination();

            //동부교통(교통정보) 호출
//            callTrafficReport();

            //app 버전 체크 - updater 호출
//            callOTAUpdater();

            //아이나비 화면 호출
            INaviExecutor.run(mContext);

            //주기전송 시작 (20.06.24 김팀장님 요청 - 대기관련 제외)
//            ResponseWaitCallInfoPacket callInfoWait = mPreferenceUtil.getCallInfoWait();
//            ResponseCallOrderPacket callInfoNormal = mPreferenceUtil.getCallInfoNormal();
//            periodTerm = mConfigLoader.getPst();
//            if ((callInfoWait != null && !callInfoWait.isReported()
//                    && callInfoWait.getOrderKind() == Packets.OrderKind.Mobile)
//                    ||
//                    (callInfoNormal != null && !callInfoNormal.isReported()
//                            && callInfoNormal.getOrderKind() == Packets.OrderKind.Mobile)) {
//                periodTerm = mConfigLoader.getRc();
//            }

            ResponseCallOrderPacket callInfoNormal = mPreferenceUtil.getCallInfoNormal();
            periodTerm = mConfigLoader.getPst();
            if (callInfoNormal != null && !callInfoNormal.isReported() && callInfoNormal.getOrderKind() == Packets.OrderKind.Mobile) {
                periodTerm = mConfigLoader.getRc();
            }
            pollingPeriod(periodTerm);

            //환경설정 버전 체크
            if (responsePacket.getConfigurationVersion() > mConfigLoader.getConfigVersion()) {
                requestConfig();
            }

            //서비스 번호 체크
            if (mConfigLoader.getServiceNumber() != responsePacket.getServiceNumber()) {
                LogHelper.e("ServiceNumber : " + mConfigLoader.getServiceNumber() + " // " + responsePacket.getServiceNumber());
                mConfigLoader.setServiceNumber(responsePacket.getServiceNumber());
                mConfigLoader.save();
            }

            //공지사항 유무
            final int noticeCode = responsePacket.getNoticeCode();
            if (noticeCode > 0) {
                requestNotice();
            }

            //대기상태 체크 (대기배차는 추후 적용 - 20.06.24 김팀장님 요청 - 대기관련 제외)
//            if (mPreferenceUtil.getWaitArea() != null) {
//                pollingCheckWaitRange(true);
//            }

            //배차정보 or 저장된 배차정보 체크 (대기배차는 추후 적용 - 20.06.24 김팀장님 요청 - 대기관련 제외)
//            if (responsePacket.isWaiting()
//                    || (callInfoWait != null && !callInfoWait.isReported())
//                    || (callInfoNormal != null && !callInfoNormal.isReported())) {
//
//                //대기배차 체크
//                if (responsePacket.isWaiting() && callInfoWait == null) {
//                    //대기배차 고객정보 요청
//                    requestWaitCallInfo();
//                } else {
//                    if (callInfoWait != null) {
//                        WavResourcePlayer.getInstance(mContext).play(R.raw.voice_132);
//                    } else {
//                        WavResourcePlayer.getInstance(mContext).play(R.raw.voice_120);
//                    }
//                    //예약등
//                    mBaseApplication.setVacancyLightStatus(true);
//
//                    //고객정보 화면
//                    showCallInfoActivity();
//                }
//            }

            //배차정보 유무만 체크 (대기배차는 추후 적용 - 20.06.24 김팀장님 요청 - 대기관련 제외)
            if (callInfoNormal != null && !callInfoNormal.isReported()) {
                WavResourcePlayer.getInstance(mContext).play(R.raw.voice_120);
                //예약등
                mBaseApplication.setVacancyLightStatus(true);
                //고객정보 화면
                showCallInfoActivity();
            }

            //빈차등/미터기 상태 - 휴식패킷에 전송
            if (mConfigLoader.isVacancyLight()) {
                requestRest(Packets.RestType.Vacancy);
            } else {
                String deviceType = mConfigLoader.getMeterDeviceType();
                if ("금호".equals(deviceType)) {
                    requestRest(Packets.RestType.KumHo);
                } else if ("한국".equals(deviceType)) {
                    requestRest(Packets.RestType.Hankook);
                } else if ("광신".equals(deviceType)) {
                    requestRest(Packets.RestType.Kwangshin);
                } else if ("알파엠".equals(deviceType)) {
                    requestRest(Packets.RestType.AlphaM);
                } else if ("TIMS".equals(deviceType)) {
                    requestRest(Packets.RestType.TIMS);
                } else if ("중앙".equals(deviceType)) {
                    requestRest(Packets.RestType.Jungang);
                } else if ("서울통합DTG".equals(deviceType)) {
                    requestRest(Packets.RestType.SeoulDTG);
                }
            }
        }
    }


    /**
     * 주기전송 패킷을 요청한다. (1211 -> 1212)
     */
    private void requestPeriodSending() {
        RequestPeriodSendingPacket packet = new RequestPeriodSendingPacket();
        packet.setServiceNumber(mConfigLoader.getServiceNumber());
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        packet.setSendingTime(getCurrentTime());
        packet.setGpsTime(mGpsHelper.getTime());
        packet.setDirection(mGpsHelper.getBearing());
        packet.setLongitude(mGpsHelper.getLongitude());
        packet.setLatitude(mGpsHelper.getLatitude());
        packet.setSpeed(mGpsHelper.getSpeed());
        packet.setBoardState(boardType);
        packet.setRestState(restType);
        request(packet);
    }

    /**
     * 주기전송 응답 처리
     *
     * @param responsePacket
     */
    private void responsePeriodSending(ResponsePeriodSendingPacket responsePacket) {
        //메시지 체크
        if (responsePacket.hasMessage()) {
            requestMessage();
        }

        // 배차상태 & 저장된 고객정보가 없을 경우 배차정보요청 패킷(GT-1A21 -> 1A22) 전송
        if (responsePacket.hasOrder()) {
            ResponseCallOrderPacket callInfoNormal = mPreferenceUtil.getCallInfoNormal();
            if (callInfoNormal == null) {
                requestCallInfo(responsePacket.getCallNumber());
            }
        }
    }

    /**
     * 환경설정요청 패킷을 요청한다. (1115 -> 1116)
     */
    private void requestConfig() {
        RequestConfigPacket packet = new RequestConfigPacket();
        packet.setServiceNumber(mConfigLoader.getServiceNumber());
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        packet.setConfigurationCode(mConfigLoader.getConfigVersion());
        request(packet);
    }

    /**
     * 환경설정 응답 처리
     *
     * @param responsePacket
     */
    private void responseConfig(ResponseConfigPacket responsePacket) {
        mConfigLoader.write(responsePacket);

        // 환경 설정 아이피가 변경 되었으므로 변경된 아이피로 접속 한다.
        if (!mNetworkManager.getIp().equals(mConfigLoader.getCallServerIp())) {
            mNetworkManager.disconnect();
            mNetworkManager.connect(mConfigLoader.getCallServerIp(), mConfigLoader.getCallServerPort());
        }
    }

    /**
     * 휴식/운행재개 패킷을 요청한다. (1B11 -> 1B12)
     */
    public void requestRest(Packets.RestType restType) {
        RequestRestPacket packet = new RequestRestPacket();
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        packet.setRestType(restType);
        request(packet);
    }

    /**
     * 휴식/운행재개 응답 처리
     *
     * @param responsePacket
     */
    public void responseRest(ResponseRestPacket responsePacket) {
        Packets.RestType restType = responsePacket.getRestType();
        if (restType != null && (restType == Packets.RestType.Rest || restType == Packets.RestType.Working)) {
            boolean isRest = false;
            if (restType == Packets.RestType.Rest) {
                isRest = true;

                //대기 상태 취소 처리 (??)
            }

            Activity activity = mBaseApplication.getActivity(MainActivity.class);
            if (activity != null) {
                ((MainActivity) activity).checkRestView(isRest);
            }

            this.restType = restType;
        }
    }

    /**
     * Ack 패킷을 요청 한다. Retry로직이 포함되어 있다.(최대 3회 5초간격) (F111 -> FF11)
     */
    public void requestAck(final int messageType, final int serviceNo, final int callNo) {
        ackRetryCount = 0;

        RequestAckPacket packet = new RequestAckPacket();
        packet.setServiceNumber(serviceNo);
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        packet.setAckMessage(messageType);
        packet.setParameter(callNo);

        Message msg = pollingHandler.obtainMessage();
        msg.what = MSG_ACK;
        msg.obj = packet;

        pollingHandler.removeMessages(MSG_ACK);
        pollingHandler.sendMessage(msg);
    }

    /**
     * 공지사항요청 패킷을 요청한다.
     */
    private void requestNotice() {
        RequestNoticePacket packet = new RequestNoticePacket();
        packet.setServiceNumber(mConfigLoader.getServiceNumber());
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        request(packet);
    }

    /**
     * 메시지 요청 패킷을 요청한다.
     */
    private void requestMessage() {
        RequestMessagePacket packetMsg = new RequestMessagePacket();
        packetMsg.setServiceNumber(mConfigLoader.getServiceNumber());
        packetMsg.setCorporationCode(mConfigLoader.getCorporationCode());
        packetMsg.setCarId(mConfigLoader.getCarId());
        request(packetMsg);
    }

    /**
     * 배차상태 & 저장된 고객정보가 없을 경우 배차정보요청 패킷 전송 (1A21 -> 1A22)
     *
     * @param callNumber
     */
    public void requestCallInfo(int callNumber) {
        RequestCallInfoPacket packet = new RequestCallInfoPacket();
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        Calendar calendar = Calendar.getInstance();
        String today = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        packet.setCallReceiptDate(today);
        packet.setCallNumber(callNumber);
        request(packet);
    }

    /**
     * 배차고객정보 고객정보재전송 응답 처리
     *
     * @param responsePacket
     * @param messageType
     */
    public void responseCallInfo(ResponseCallInfoPacket responsePacket, int messageType) {
        ResponseCallOrderPacket callInfoTemp = mPreferenceUtil.getCallInfoTemp();

        // 임시 저장 패킷이 없는 경우 배차데이터 처리(1314)까지는 완료가 되었다는 의미이므로
        // 고객 정보가 있는 경우 고객 정보 팝업을 보여준다.
        if (callInfoTemp == null) {
            requestAck(messageType, mConfigLoader.getServiceNumber(), responsePacket.getCallNumber());

            ResponseCallOrderPacket callInfoNormal = mPreferenceUtil.getCallInfoNormal();
            ResponseCallOrderPacket callInfoGeton = mPreferenceUtil.getCallInfoGeton();
            ResponseCallOrderPacket callInfo = new ResponseCallOrderPacket(responsePacket);

            if (responsePacket.getOrderKind() == Packets.OrderKind.GetOnOrder) {
                if (callInfoGeton != null && callInfoNormal == null && boardType != Packets.BoardType.Boarding) {
                    mPreferenceUtil.setCallInfoNormal(callInfoGeton);
                    mPreferenceUtil.clearCallInfoGeton();
                } else if (callInfoGeton != null && callInfoGeton.getCallNumber() == responsePacket.getCallNumber()) {
                } else {
                    mPreferenceUtil.setCallInfoGeton(callInfo);
                }

            } else {
                if (callInfoNormal != null && callInfoNormal.getCallNumber() == responsePacket.getCallNumber()) {
                } else {
                    mPreferenceUtil.setCallInfoNormal(callInfo);
                }
            }
            showCallInfoActivity();

        } else {
            // 임시 저장 패킷이 남아 있는 경우 배차데이터 처리(1314)를 받지 못했다는 뜻이므로
            // 1314 수신과 동일하게 처리 한다. (Packets.ORDER_INFO_PROC)
            boolean isFailed = checkCallValidation(messageType, responsePacket.getCallNumber(), responsePacket.getCarId());
            processCallInfo(messageType, responsePacket.getCallNumber(), isFailed);
        }
    }

    /**
     * 콜정보 체크 (배차정보 요청 응답 / 배차 데이터 처리 응답)
     *
     * @param messageType
     * @param callNumber
     * @param carId
     * @return
     */
    private boolean checkCallValidation(int messageType, int callNumber, int carId) {
        ResponseCallOrderPacket callInfoTemp = mPreferenceUtil.getCallInfoTemp();

        boolean isFailed = false;
        if (callInfoTemp.getCallNumber() != callNumber) {
            // 실시간 위치 및 배차요청으로 올린 콜넘버와 응답의 콜넘버가 다른 겨우 서비스 넘버 97로 ACK
            requestAck(messageType, 97, callNumber);
            isFailed = true;
        } else if (callInfoTemp.getCarId() != carId) {
            // 실시간 위치 및 배차요청으로 올린 콜ID와 응답의 콜ID가 다른 겨우 서비스 넘버 98로 ACK
            requestAck(messageType, 98, callNumber);
            isFailed = true;
        } else if (callInfoTemp.getOrderKind() != Packets.OrderKind.GetOnOrder
                && boardType == Packets.BoardType.Boarding) {
            // 승차 중 인데 승차 중 배차가 아니면
            requestAck(messageType, 99, callNumber);
            isFailed = true;
        }

        return isFailed;
    }

    private void processCallInfo(int messageType, int callNumber, boolean isFailed) {
        ResponseCallOrderPacket callInfoTemp = mPreferenceUtil.getCallInfoTemp();

        if (isFailed) {
            WavResourcePlayer.getInstance(mContext).play(R.raw.voice_122);
            mPreferenceUtil.clearCallInfoTemp();

        } else {
            if (callInfoTemp.getOrderKind() == Packets.OrderKind.GetOnOrder) {
                mPreferenceUtil.setCallInfoGeton(callInfoTemp);
            } else {
                mPreferenceUtil.setCallInfoNormal(callInfoTemp);
            }

            // 모바일 배차 완료시 주기전송 간격을 8초로 변경 한다.
            // 승차신호가 올라오면 정상 주기로 다시 변경 한다.
            if (callInfoTemp.getOrderKind() == Packets.OrderKind.Mobile) {
                periodTerm = mConfigLoader.getRc();
                pollingPeriod(periodTerm);
            }

            // 승차 중이라면, 배차 완료 여부를 음성으로만 알려줌.
            // 승차 중이 아니라면 화면으로 알려줌.
            if (boardType != Packets.BoardType.Boarding) {
                mBaseApplication.setVacancyLightStatus(true);
                showCallInfoActivity();
            } else {
                WavResourcePlayer.getInstance(mContext).play(R.raw.voice_120);
            }

            requestAck(messageType, mConfigLoader.getServiceNumber(), callNumber);
            mPreferenceUtil.clearCallInfoTemp();
        }
    }

    /**
     * 배차데이터 콜 정보
     *
     * @param responsePacket
     */
    private void responseCallOrder(ResponseCallOrderPacket responsePacket) {

        mBaseApplication.setWasBackground(mBaseApplication.isBackground());
        int serviceNumber = mConfigLoader.getServiceNumber();

        ResponseCallOrderPacket callInfoNormal = mPreferenceUtil.getCallInfoNormal();
        ResponseCallOrderPacket callInfoGeton = mPreferenceUtil.getCallInfoGeton();

        if (callInfoNormal != null && callInfoGeton != null) {
            // - 0x14 : 배차가 2개 이상인지 (일반배차가 있는 상태에서 또 일반배차가 내려 올 경우)
            requestCallOrderRealtime(Packets.OrderDecisionType.MultipleOrder, responsePacket);

        } else if (callInfoNormal != null && boardType == Packets.BoardType.Empty) {
            // - 0x0D : 배차가 1개 일때 - 현재상태가 빈차일 경우 (운행보고 안함 : 콜받고 운행보고 안된상태에서 콜수신된경우)
            requestCallOrderRealtime(Packets.OrderDecisionType.AlreadyOrderd, responsePacket);

        } else if (boardType == Packets.BoardType.Boarding && responsePacket.getOrderKind() == Packets.OrderKind.Normal) {
            // - 0x0A : 주행중 일반콜 수신될 경우
            requestCallOrderRealtime(Packets.OrderDecisionType.Driving, responsePacket);

            // (20.06.24 김팀장님 요청 - 대기관련 제외)
//        } else if (waitArea != null && responsePacket.getOrderKind() == Packets.OrderKind.Normal) {
//            // - 0x0C : 대기배차 상태인데 일반콜 수신될 경우
//            requestCallOrderRealtime(Packets.OrderDecisionType.Waiting, responsePacket);
//
//        } else if (waitArea != null && responsePacket.getOrderKind() == Packets.OrderKind.WaitOrderTwoWay) {
//            // 대기배차 상태인데 양방향 대기배차 수신될 경우 (하남사용)
//            mPreferenceUtil.setCallInfoTemp(responsePacket);
//
//            //복지콜 추가로 인한 분기처리. - 서비스 번호
//            if (serviceNumber == Constants.AREA_SUNGNAM_BOKJI) {
//                requestCallOrderRealtime(Packets.OrderDecisionType.Request, responsePacket);
//            } else {
//                WavResourcePlayer.getInstance(mContext).play(R.raw.voice_170);
//                showCallReceiveActivity();
//            }

        } else {
            mPreferenceUtil.setCallInfoTemp(responsePacket);

            //복지콜 추가로 인한 분기처리. - 서비스 번호
            if (serviceNumber == Constants.AREA_SUNGNAM_BOKJI) {
                requestCallOrderRealtime(Packets.OrderDecisionType.Request, responsePacket);
            } else {
                WavResourcePlayer.getInstance(mContext).play(R.raw.voice_170);
                showCallReceiveActivity();
            }
        }
    }

    /**
     * 실시간 위치 및 배차결정 패킷을 요청한다.
     */
    public void requestCallOrderRealtime(Packets.OrderDecisionType type, ResponseCallOrderPacket info) {
        RequestCallOrderRealtimePacket packet = new RequestCallOrderRealtimePacket();
        packet.setServiceNumber(mConfigLoader.getServiceNumber());      //서비스 번호
        packet.setCorporationCode(mConfigLoader.getCorporationCode());  //회사코드
        packet.setCarId(mConfigLoader.getCarId());  //car Id
        packet.setPhoneNumber(mConfigLoader.getDriverPhoneNumber());    //기사 전화번호
        packet.setCallNumber(info.getCallNumber());     //고객번호
        packet.setCallReceiptDate(info.getCallReceiptDate());  //콜 접수일자
        packet.setDecisionType(type);
        packet.setSendTime(getCurrentTime());
        packet.setGpsTime(mGpsHelper.getTime());
        packet.setDirection(mGpsHelper.getBearing());
        packet.setLongitude(mGpsHelper.getLongitude());
        packet.setLatitude(mGpsHelper.getLatitude());
        packet.setSpeed(mGpsHelper.getSpeed());
        packet.setDistance(mGpsHelper.getDistance(info.getLatitude(), info.getLongitude()));
        packet.setOrderCount(info.getOrderCount());
        request(packet);
    }

    /**
     * 배차데이터 처리 응답
     *
     * @param responsePacket
     */
    private void responseCallOrderProc(ResponseCallOrderProcPacket responsePacket, int messageType) {

        //콜수신 화면에 응답 전달
        Activity activity = mBaseApplication.getActivity(CallReceiveActivity.class);
        if (activity != null) {
            ((CallReceiveActivity) activity).setHasGotResponse();
        }

        mBaseApplication.setWasBackground(mBaseApplication.isBackground());
        ResponseCallOrderPacket callInfoTemp = mPreferenceUtil.getCallInfoTemp();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        boolean isFailed;
        if (callInfoTemp == null || responsePacket.getOrderProcType() != Packets.OrderProcType.Display) {
            isFailed = true;
        } else {
            isFailed = checkCallValidation(messageType, responsePacket.getCallNumber(), responsePacket.getCarId());
        }

        // (20.06.24 김팀장님 요청 - 대기관련 제외)
//        if (callInfoTemp.getOrderKind() == Packets.OrderKind.WaitOrderTwoWay) {
//            //대기콜(하남)인 경우, 배차 처리되면 대기 상태 해제 한다. 그리고, 아이나비 화면으로 가기 때문에 부작용 없다고 생각한다.
//            mPreferenceUtil.clearWaitArea();
//        }

        processCallInfo(messageType, responsePacket.getCallNumber(), isFailed);
    }


    /**
     * 운행보고 패킷을 요청 한다. Retry 로직이 포함되어 있다. (최대 3회 5초 간격)
     */
    public void requestReport(final int callNo, final int orderCount, final Packets.OrderKind orderKind,
                              final String callDate, final Packets.ReportKind kind,
                              final int fare, final int mileage) {
        reportRetryCount = 0;

        RequestReportPacket packet = new RequestReportPacket();
        packet.setServiceNumber(mConfigLoader.getServiceNumber());
        packet.setCorporationCode(mConfigLoader.getCorporationCode());
        packet.setCarId(mConfigLoader.getCarId());
        packet.setPhoneNumber(mConfigLoader.getDriverPhoneNumber());
        packet.setCallNumber(callNo);
        packet.setOrderCount(orderCount);
        packet.setOrderKind(orderKind);
        packet.setCallReceiptDate(callDate);
        packet.setReportKind(kind);
        packet.setGpsTime(mGpsHelper.getTime());
        packet.setDirection(mGpsHelper.getBearing());
        packet.setLongitude(mGpsHelper.getLongitude());
        packet.setLatitude(mGpsHelper.getLatitude());
        packet.setSpeed(mGpsHelper.getSpeed());
        packet.setTaxiState(boardType);
        packet.setFare(fare);
        packet.setDistance(mileage);

        Message msg = pollingHandler.obtainMessage();
        msg.what = MSG_REPORT;
        msg.obj = packet;

        pollingHandler.removeMessages(MSG_REPORT);
        pollingHandler.sendMessage(msg);
    }

    /**
     * 운행보고 응답 처리
     *
     * @param responsePacket
     */
    private void responseReport(ResponseReportPacket responsePacket) {
        LogHelper.e("response service report : " + responsePacket.toString());

        Packets.ReportKind reportKind = responsePacket.getReportKind();
        if (reportKind == Packets.ReportKind.GetOff || reportKind == Packets.ReportKind.Failed) {

            //승차 실패 처리
            if (reportKind == Packets.ReportKind.Failed) {
                WavResourcePlayer.getInstance(mContext).play(R.raw.voice_151);
                mBaseApplication.setVacancyLightStatus(true);
            }
            mBaseApplication.setVacancyLightStatus(false);
            boardType = Packets.BoardType.Empty;

            Activity activity = mBaseApplication.getActivity(CallInfoActivity.class);
            if (activity != null)
                activity.finish();

            //하차 메시지
            if (reportKind == Packets.ReportKind.GetOff) {
                Popup popup = new Popup.Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_ALIGHTED)
                        .setTitle(getString(R.string.popup_title_alert))
                        .setContent(getString(R.string.alloc_msg_complete))
                        .setDismissSecond(3)
                        .build();

                activity = mBaseApplication.getCurrentActivity();
                if (activity != null)
                    ((BaseActivity) activity).showPopupDialog(popup);
            }

            refreshSavedPassengerInfo(responsePacket.getCallNumber());
            checkReservation();
        }

        if (reportKind == Packets.ReportKind.GetOn) {
            LogHelper.write("#### 승차 보고 -> callNo : " + responsePacket.getCallNumber());
        } else if (reportKind == Packets.ReportKind.GetOff) {
            LogHelper.write("#### 하차 보고 -> callNo : " + responsePacket.getCallNumber());
        } else if (reportKind == Packets.ReportKind.Failed) {
            LogHelper.write("#### 탑승 실패 보고 -> callNo : " + responsePacket.getCallNumber());
        }
    }

    /**
     * 배차 데이터를 아래의 순서로 업데이트 한다.
     * 1. 전달 받은 콜넘버와 저장 받은 배차데이터를 비교하여
     * 해당 사항이 있는 경우 배차데이터를 삭제 한다.
     * 2. 배차1이 없고 배차2가 존재한다면 배차2 -> 배차1로 이동 후
     * 배차2를 삭제 한다.
     *
     * @param callNo 콜번호
     */
    private void refreshSavedPassengerInfo(int callNo) {
        LogHelper.d(">> callNo : " + callNo);

//        ResponseWaitCallInfoPacket wait = mPreferenceUtil.getCallInfoWait();
//        if (wait != null && wait.getCallNumber() == callNo) {
//            mPreferenceUtil.clearCallInfoWait();
//            mPreferenceUtil.clearWaitArea();
//        }

        ResponseCallOrderPacket getOn = mPreferenceUtil.getCallInfoGeton();
        if (getOn != null && getOn.getCallNumber() == callNo) {
            mPreferenceUtil.clearCallInfoGeton();
        }

        ResponseCallOrderPacket normal = mPreferenceUtil.getCallInfoNormal();
        if (normal != null && normal.getCallNumber() == callNo) {
            mPreferenceUtil.clearCallInfoNormal();
        }

        getOn = mPreferenceUtil.getCallInfoGeton();
        normal = mPreferenceUtil.getCallInfoNormal();
        if (getOn != null && normal == null) {
            mPreferenceUtil.clearCallInfoNormal();
            mPreferenceUtil.setCallInfoNormal(getOn);
            mPreferenceUtil.clearCallInfoGeton();
        }
    }

    /**
     * 저장되어 있는 배차가 있다면 고객 정보팝업을 보여준다.
     */
    private void checkReservation() {
        LogHelper.d(">> Check reserved call info.");
        ResponseCallOrderPacket normal = mPreferenceUtil.getCallInfoNormal();
        if (normal != null) {
            LogHelper.d(">> has passenger info.");
            mBaseApplication.setVacancyLightStatus(true);
            showCallInfoActivity();
        }
    }

    /**
     * 승/빈차 신호를 서버에 전송 한다.
     * 저장된 배차 정보가 없을 경우 주기 전송 패킷을 요청한다.
     * 저장된 배차 정보가 있을 경우 운행 보고 패킷을 요청한다.
     */
    public void requestBoardState(Packets.ReportKind kind, int fare, int mileage) {
//        WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
//        OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
//        if (wait == null && normal == null) {
//            LogHelper.d(">> Not exist saved passenger info. request period sending");
//            requestPeriod();
//
//            // 2019. 01. 30 (용용)  콜없을때 : 주행/빈차시 길안내 취소 :  <메인화면>창 안닫히는 문제 + 메뉴바 안나타나는 문제개선
//            // 콜메뉴 전체화면 상태에서 네비길안내 취소를 호출하면 네비화면으로 바뀌면서 메뉴바 갱신이 됨. 메뉴바 사라졌을경우 미터기 주행/빈차 조작으로 나타나게할수도 있을듯함
//            INaviExecutor.cancelNavigation(context);
//
//            if (kind == Packets.ReportKind.GetOn) {
//                LogHelper.write("#### 승차 주기 보고");
//            } else {
//                LogHelper.write("#### 하차 주기 보고");
//            }
//
//            // 길에서 손님 태운 후 승차 상태에서 승차 중 배차를 받음 -> 하차 버튼 -> 고객 정보 창이 보여져야 함
//            if (kind == Packets.ReportKind.GetOff) {
//                refreshSavedPassengerInfo(0);
//                checkReservation();
//            }
//        } else {
//            if (wait != null) {
//                wait.setReported(true);
//                PreferenceUtil.setWaitOrderInfo(context, wait);
//                requestReport(
//                        wait.getCallNumber(), wait.getOrderCount(),
//                        wait.getOrderKind(), wait.getCallReceiptDate(),
//                        kind, fare, mileage);
//            } else if (normal != null) {
//                normal.setReported(true);
//                PreferenceUtil.setNormalCallInfo(context, normal);
//                requestReport(
//                        normal.getCallNumber(), normal.getOrderCount(),
//                        normal.getOrderKind(), normal.getCallReceiptDate(),
//                        kind, fare, mileage);
//
//                if (kind == Packets.ReportKind.GetOn) {
//                    if (normal.getDestLongitude() > 0 && normal.getDestLongitude() > 0) {
//                        INaviExecutor.startNavigationNow(context,
//                                TextUtils.isEmpty(normal.getDestName()) ? "" : normal.getDestName(),
//                                normal.getDestLatitude(),
//                                normal.getDestLongitude());
//                    } else {
//                        // 2019. 01. 30 (용용) 콜있을때 : 승차보고시 목적지 없을경우 길안내 취소 :  <메인화면>창 안닫히는 문제 + 메뉴바 안나타나는 문제개선
//                        INaviExecutor.cancelNavigation(context);
//                    }
//                } else {
//                    // 2019. 01. 30 (용용) 콜있을때 : 하차보고시 길안내 취소 :  목적지 길안내 취소 + <메인화면>창 안닫히는 문제 + 메뉴바 안나타나는 문제개선
//                    INaviExecutor.cancelNavigation(context);
//                }
//            }
//        }
    }

    /**
     * 대기배차고객정보 요청 패킷을 요청한다. (1517->1518)
     */
//    public void requestWaitCallInfo() {
//        RequestWaitCallInfoPacket packet = new RequestWaitCallInfoPacket();
//        packet.setServiceNumber(mConfigLoader.getServiceNumber());
//        packet.setCorporationCode(mConfigLoader.getCorporationCode());
//        packet.setCarId(mConfigLoader.getCarId());
//        request(packet);
//    }

    /**
     * Emergency 요청 패킷을 요청한다.
     */
//    private void requestEmergency() {
//        RequestEmergencyPacket packet = new RequestEmergencyPacket();
//        packet.setServiceNumber(cfgLoader.getServiceNumber());
//        packet.setCorporationCode(cfgLoader.getCorporationCode());
//        packet.setCarId(cfgLoader.getCarId());
//        packet.setEmergencyType(emergencyType);
//        packet.setGpsTime(gpsHelper.getTime());
//        packet.setDirection(gpsHelper.getBearing());
//        packet.setLongitude(gpsHelper.getLongitude());
//        packet.setLatitude(gpsHelper.getLatitude());
//        packet.setSpeed(gpsHelper.getSpeed());
//        packet.setTaxiState(boardType);
//        request(packet);
//    }

    /**
     * 콜정산 요청 패킷을 요청한다.
     */
//    public void requestAccount(String begin, String end) {
//        RequestAccountPacket packet = new RequestAccountPacket();
//        packet.setServiceNumber(cfgLoader.getServiceNumber());
//        packet.setCorporationCode(cfgLoader.getCorporationCode());
//        packet.setCarId(cfgLoader.getCarId());
//        packet.setPhoneNumber(cfgLoader.getDriverPhoneNumber());
//        packet.setAccountType(Packets.AccountType.Period);
//        packet.setBeginDate(begin);
//        packet.setEndDate(end);
//        request(packet);
//    }

    /**
     * 대기지역요청 패킷을 요청한다.
     */
//    public void requestWaitAreas() {
//        RequestWaitAreaPacket packet = new RequestWaitAreaPacket();
//        packet.setServiceNumber(cfgLoader.getServiceNumber());
//        packet.setCorporationCode(cfgLoader.getCorporationCode());
//        packet.setCarId(cfgLoader.getCarId());
//        packet.setGpsTime(gpsHelper.getTime());
//        packet.setLongitude(gpsHelper.getLongitude());
//        packet.setLatitude(gpsHelper.getLatitude());
//        packet.setTaxiState(getBoardType());
//        request(packet);
//    }

    /**
     * 대기결정 패킷을 요청한다.
     */
//    public void requestWait(String waitPlaceCode) {
//        WaitDecisionPacket packet = new WaitDecisionPacket();
//        packet.setServiceNumber(cfgLoader.getServiceNumber());
//        packet.setCorporationCode(cfgLoader.getCorporationCode());
//        packet.setCarId(cfgLoader.getCarId());
//        packet.setDriverNumber(cfgLoader.getDriverPhoneNumber());
//        packet.setGpsTime(gpsHelper.getTime());
//        packet.setLongitude(gpsHelper.getLongitude());
//        packet.setLatitude(gpsHelper.getLatitude());
//        packet.setDecisionAreaCode(waitPlaceCode);
//        request(packet);
//    }

    /**
     * 대기취소 패킷을 요청한다.
     */
//    public void requestWaitCancel(String waitPlaceCode) {
    // 저장된 대기지역이 있는데 대기지역을 다시 요청하는 경우는 취소의 케이스로 간주한다.
//        WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
//        if (wait != null) {
//            requestReport(
//                    wait.getCallNumber(),
//                    wait.getOrderCount(),
//                    wait.getOrderKind(),
//                    wait.getCallReceiptDate(),
//                    Packets.ReportKind.Failed, 0, 0);
//        }
//        WaitCancelPacket packet = new WaitCancelPacket();
//        packet.setServiceNumber(cfgLoader.getServiceNumber());
//        packet.setCorporationCode(cfgLoader.getCorporationCode());
//        packet.setCarId(cfgLoader.getCarId());
//        packet.setPhoneNumber(cfgLoader.getDriverPhoneNumber());
//        packet.setAreaCode(waitPlaceCode);
//        request(packet);
//    }

//    public void requestWaitAreaState() {
    //pollingHandler.removeMessages(MSG_REQ_WAIT_AREA_STATE);
    //pollingHandler.sendEmptyMessage(MSG_REQ_WAIT_AREA_STATE);
//        RequestWaitAreaStatePacket packet = new RequestWaitAreaStatePacket();
//        packet.setCarId(cfgLoader.getCarId());
//        packet.setCorporationCode(cfgLoader.getCorporationCode());
//        packet.setServiceNumber(cfgLoader.getServiceNumber());
//        LogHelper.write("requestWaitAreaState --- " + packet.toString());
//        request(packet);
//    }

//    private void requestWaitAreaStateInner() {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                RequestWaitAreaStatePacket packet = new RequestWaitAreaStatePacket();
//                packet.setCarId(cfgLoader.getCarId());
//                packet.setCorporationCode(cfgLoader.getCorporationCode());
//                packet.setServiceNumber(cfgLoader.getServiceNumber());
//                LogHelper.write("requestWaitAreaStateInner --- " + packet.toString());
//                request(packet);
//            }
//        }).start();
//    }

    //----------------------------------------------------------------------------------------
    // polling & timer
    //----------------------------------------------------------------------------------------
    private void cancelTimer(Timer t) {
        if (t != null) {
            t.cancel();
            t = null;
        }
    }

    /**
     * 주기 전송 패킷을 일정 간격마다 요청 한다.
     */
    private void pollingPeriod(int period) {
        LogHelper.d(">> Polling period : " + period + " sec");
        pollingHandler.removeMessages(MSG_PERIOD);

        Message msg = pollingHandler.obtainMessage();
        msg.what = MSG_PERIOD;
        msg.arg1 = period;

        pollingHandler.sendMessage(msg);
    }

    /**
     * 지정된 거리를 벗어 날 경우 대기취소 패킷을 요청 한다.
     */
//    private void pollingCheckWaitRange(boolean start) {
//        // 범위를 벗어 난 상태에서 speed 5 이상이면 대기 취소를 요청 할 것 (5초 주기로 체크)
//        pollingHandler.removeMessages(MSG_AREA_CHECK);
//        if (start) {
//            pollingHandler.sendEmptyMessage(MSG_AREA_CHECK);
//        }
//    }

    /**
     * Emergency 요청 패킷을 일정 간격마다 요청 한다.
     */
//    public void pollingEmergency() {
//        pollingHandler.removeMessages(MSG_EMERGENCY);
//        pollingHandler.sendEmptyMessage(MSG_EMERGENCY);
//
//        // 응급 상황에서는 주기를 올리지 않아야 한다.
//        pollingHandler.removeMessages(MSG_PERIOD);
//    }

    //----------------------------------------------------------------------------------------
    // getter / setter
    //----------------------------------------------------------------------------------------
    public GpsHelper getGpsHelper() {
        return mGpsHelper;
    }

    public Packets.BoardType getBoardType() {
        return boardType;
    }

    public void setBoardType(Packets.BoardType boardType) {
        this.boardType = boardType;
    }

    public Packets.RestType getRestType() {
        return restType;
    }

//    public boolean isPrevStatusBackground() {
//        return isPrevStatusBackground;
//    }

//    public void setPrevStatusBackground(boolean status) {
//        isPrevStatusBackground = status;
//    }

//    public Packets.EmergencyType getEmergencyType() {
//        return emergencyType;
//    }

    //----------------------------------------------------------------------------------------
    // method
    //----------------------------------------------------------------------------------------
    public void reset() {
        pollingHandler.removeMessages(MSG_LIVE);
        pollingHandler.removeMessages(MSG_PERIOD);
//        pollingHandler.removeMessages(MSG_EMERGENCY);

//        pollingCheckWaitRange(false);

        mNetworkManager.disconnect();
        mNetworkManager.livePacketDisconnect();
        hasCertification = false;
    }

    /**
     * 빈차등/미터기의 승차 신호를 처리 한다.
     *
     * @param fare    요금 (빈차등일 경우 0)
     * @param mileage 거리 (빈차등일 경우 0)
     */
    public void applyVacancy(int fare, int mileage) {
        if (boardType == Packets.BoardType.Empty) {
            LogHelper.d(">> Skip empty signal. already empty.");
            return;
        }

        boardType = Packets.BoardType.Empty;
        // 인증 전에는 빈차 신호를 무시 한다.
        if (hasCertification) {
            requestBoardState(Packets.ReportKind.GetOff, fare, mileage);
        }
    }

    /**
     * 빈차등/미터기의 승차 신호를 처리 한다.
     *
     * @param fare    요금 (빈차등일 경우 0)
     * @param mileage 거리 (빈차등일 경우 0)
     */
    public void applyDriving(int fare, int mileage) {
        if (boardType == Packets.BoardType.Boarding) {
            LogHelper.d(">> Skip driving signal. already driving.");
            return;
        }

        boardType = Packets.BoardType.Boarding;
        // 인증 전에는 승차 신호를 무시 한다.
        if (hasCertification) {
            requestBoardState(Packets.ReportKind.GetOn, fare, mileage);

            // 모바일 배차 수신 후 승차 신호가 올라갈 때 주기를 다시 변경 한다.
            // 배차가 하나도 없을 경우에 모바일 배차가 수신 되므로 orderkind 구분 하지 않아도 된다.
            if (periodTerm != mConfigLoader.getPst()) {
                periodTerm = mConfigLoader.getPst();
                pollingPeriod(periodTerm);
            }

//            // 대기 상태이면서 대기 고객 정보가 없을 때 주행이 올라오면 대기 취소를 한다.
//            ResponseWaitDecisionPacket p = PreferenceUtil.getWaitArea(context);
//            if (p != null && PreferenceUtil.getWaitOrderInfo(context) == null) {
//                requestWaitCancel(p.getWaitPlaceCode());
//            }
//
//            // 승차 신호가 들어올 경우 고객 정보 창을 닫도록 한다.
//            Activity act = mBaseApplication.getActivity(PassengerInfoPopupActivity.class);
//            if (act != null) {
//                act.finish();
//            }

            // 기기 테스트 화면에서 승/빈차 신호시 화면 종료되는 것에 대한 예외처리를 추가
//            if (mBaseApplication.getActivity(TestActivity.class) == null) {
//                INaviExecutor.cancelNavigation(context);
//            }
        }
    }

//    public void enabledEmergency(boolean enabled) {
//        if (enabled) {
//            emergencyType = Packets.EmergencyType.Begin;
//            pollingEmergency();
//        } else {
//            // 실제 Emergency Off는 서버에서 받아서 처리하도록 한다.
//            // 디버깅 페이지에서 Emergency를 Off하는 경우를 위해 구현해 둔다.
//            emergencyType = Packets.EmergencyType.End;
//            pollingHandler.removeMessages(MSG_EMERGENCY);

    // 응급 상황 해제 후에 주기 전송을 재시작 한다.
//            periodTerm = cfgLoader.getPst();
//            WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
//            OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
//            if (wait != null
//                    && wait.getOrderKind() == Packets.OrderKind.Mobile
//                    && !wait.isReported()) {
//                periodTerm = cfgLoader.getRc();
//            } else if (normal != null
//                    && normal.getOrderKind() == Packets.OrderKind.Mobile
//                    && !normal.isReported()) {
//                periodTerm = cfgLoader.getRc();
//            }
//            pollingPeriod(periodTerm);
//        }
//    }

    public String getCurrentTime() {
        return getCurrentTime("yyMMddHHmmss");
    }

    public String getCurrentTime(String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(new Date(System.currentTimeMillis()));
    }

    private void launchActivity(Class<?> cls) {
        // 인증 완료 후 지도 화면 -> 메시지(공지사항, 콜 등) 수신 -> 메시지(공지사항, 콜 등) 창을 닫을 때 지도 화면이 보여져야 한다.
        // Activity 실행시 FLAG_ACTIVITY_NEW_TASK를 사용하더라도 taskAffinity가 MainActivity와 같아서
        // 동일 Task 내에서 Activity가 실행된다. (FLAG_ACTIVITY_MULTIPLE_TASK를 같이 사용하면 무조건 다른 Task로
        // Activity를 실행 가능하나 Task가 여러개 생성됨에 따라 발생되는 이슈들에 대한 검증이 현재 어려우므로 사용이 어렵다.)

        // 위의 이슈로 메시지 창을 닫았을 때 MainActivity가 onResume 된다.
        // 콜 메인 화면 대신 지도 화면을 보여주기 위해서 현재 최상위 Activity가 MainActivity일 경우에만
        // background/foreground FLAG를 저장해 두었다가 BaseActivity.finishWithINavi()에서 이를 보고 지도를 실행하도록 한다.
//        Activity topAct = mBaseApplication.getTopActivity();
//        if (topAct != null && topAct.getClass().getSimpleName().contains("MainActivity")) {
//            isPrevStatusBackground = !mBaseApplication.isForegroundActivity(getPackageName());
//            LogHelper.write("#### isPrevStatusBackground = " + isPrevStatusBackground);
//        } else {
//            LogHelper.write("#### isPrevStatusBackground = " + (topAct == null ? "null" : topAct.getClass().getName()));
//        }
        Intent intent = new Intent(ScenarioService.this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showCallReceiveActivity() {
        LogHelper.write("#### showCallReceiveActivity()");
        launchActivity(CallReceiveActivity.class);
    }

    private void showCallInfoActivity() {
        LogHelper.write("#### showCallInfoActivity()");
        launchActivity(CallInfoActivity.class);
    }

    private void showCallBoardingActivity() {
        LogHelper.write("#### showCallBoardingActivity()");
        launchActivity(CallBoardingActivity.class);
    }

    /**
     * 공지사항 / 메시지 팝업 호출
     *
     * @param isNotice
     * @param title
     * @param content
     */
    private void showNoticePopup(boolean isNotice, String title, String content) {
        LogHelper.write("#### showNoticePopup() : " + isNotice);

        String strTitle;
        if (isNotice) {
            WavResourcePlayer.getInstance(mContext).play(R.raw.voice_115);
            strTitle = getString(R.string.main_menu_notice);
        } else {
            WavResourcePlayer.getInstance(mContext).play(R.raw.voice_142);
            strTitle = getString(R.string.main_menu_message);
        }

        Popup popup = new Popup
                .Builder(Popup.TYPE_ONE_BTN_LARGE, Constants.DIALOG_TAG_NOTICE)
                .setTitle(strTitle)
                .setContentTitle(title)
                .setContent(content)
                .build();

        Activity activity = mBaseApplication.getCurrentActivity();
        if (activity != null)
            ((BaseActivity) activity).showPopupDialog(popup);
    }


    /**
     * 교통 정보 Service를 실행 한다.
     */
    private void callTrafficReport() {
        LogHelper.d("callTrafficReport()");
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(AppDefine.PKG_NAME_DONGBUNTS, AppDefine.PKG_NAME_DONGBUNTS_SERVICE));
            intent.putExtra("SET_PACKAGE_NAME", getPackageName());
            intent.putExtra("MODEM_NUMBER", mConfigLoader.getModemNumber());
            startService(intent);

            LogHelper.d(">> callTrafficReport : " + mConfigLoader.getModemNumber());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 업데이터 호출 - 접속정보를 서버에서 받아서 처리
     */
    private void callOTAUpdater() {
        String curDateTime = getCurrentTime("yyyyMMddHHmmss");
        LogHelper.e("callOTAUpdater() - " + curDateTime);

        if (curDateTime.substring(0, 2).equals("19")) {
            sendNetworkRequest(NetworkUrlDefine.REQUEST_SERVER_TIME, null, ParamDefine.GET, mNetworkRunListener);
        } else {
            callOTAUpdater(curDateTime.substring(2));
        }
    }

    /**
     * 업데이터 호출 - 접속정보를 서버에서 받아서 처리
     */
    private void callOTAUpdater(String curDateTime) {
        LogHelper.write("#### callOTAUpdater. mConfiguration : " + mConfigLoader.toString());

        String carId = String.valueOf(mConfigLoader.getCarId());
        String serviceCode = String.valueOf(mConfigLoader.getServiceNumber());
        String tcomCode = String.valueOf(mConfigLoader.getCorporationCode());
        String curAppVersiion = String.valueOf(mConfigLoader.getAppVersion());

        String authCode = EncryptUtil.sha256("insol" + curDateTime + carId);
        LogHelper.e("callOTAUpdater authCode : " + authCode);

        JSONObject params = ParamDefine.getParamsApiAppUpdateServerInfo(authCode, curDateTime, carId, serviceCode, tcomCode, curAppVersiion);

        sendNetworkRequest(NetworkUrlDefine.APP_UPDATE_SERVER_INFO, params, mNetworkRunListener);
    }

    private void callOTAUpdater(String serverIp, String serverPort, String loginId, String loginPw, String workingDirectory) {
        LogHelper.write("#### callOTAUpdater.");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(AppDefine.PKG_NAME_OTA_UPDATER, AppDefine.PKG_NAME_OTA_UPDATER_SERVICE));
        intent.putExtra("extra_key_server_ip", serverIp);
        intent.putExtra("extra_key_server_port", serverPort);
        intent.putExtra("extra_key_login_id", loginId);
        intent.putExtra("extra_key_login_pw", loginPw);
        intent.putExtra("extra_key_working_directory", workingDirectory);
        intent.putExtra("extra_key_is_apk_meta_file", false);
        intent.putExtra("extra_need_force_update", true);
        startService(intent);
    }

    public void sendNetworkRequest(NetworkUrlDefine url, JSONObject params, NetworkRunListener listener) throws RuntimeException {
        sendNetworkRequest(url, params, ParamDefine.POST, listener);
    }

    /**
     * API 통신
     *
     * @param url      url
     * @param params   params
     * @param listener listener
     */
    public void sendNetworkRequest(NetworkUrlDefine url, JSONObject params, String requestMethod, NetworkRunListener listener) throws RuntimeException {

        if (url == null)
            throw new NullPointerException("URL 정보가 셋팅되지 않음");
        NetworkData data = new NetworkData();
        data.setContext(this);
        data.setRequestMethod(requestMethod);
        data.setNetworkUrl(url);
        data.setRequestParams(params);
        data.setErrorHandleType(NetworkData.ErrorHandleType.COMMON);


        if (data == null) {
            throw new NullPointerException("DATA가 생성 되지 않음 (생성자 x)");
        }
        if (data.getContext() == null) {
            data.setContext(this);
        }

        if (data.getNetworkUrl() == null && StringUtil.isEmpty(data.getStringUrl())) {
            throw new NullPointerException("URL 정보가 셋팅되지 않음");
        }
        data.setNetworkListener(listener);
        NetworkLoader.getInstance().sendRequest(data);
    }

    private NetworkRunListener mNetworkRunListener = new NetworkRunListener() {
        @Override
        public void preStart(NetworkData data) {

        }

        @Override
        public void doInBackground(NetworkData data) {

        }

        @Override
        public void onSuccess(NetworkData data) {
            JSONObject result = data.getResult();
            LogHelper.d("result : " + result.toString());

            boolean isSuccess = result.optBoolean(ParamDefine.IS_SUCCESSFUL);
            if (isSuccess) {

                switch (data.getNetworkUrl()) {
                    case REQUEST_SERVER_TIME:
                        String curDateTime = result.optString(ParamDefine.CUR_DATE_TIME);
                        callOTAUpdater(curDateTime);
                        break;

                    case APP_UPDATE_SERVER_INFO:    //앱 업데이트 서버 정보
                        String serverIp = result.optString("ftpServerIP");
                        String serverPort = result.optString("ftpServerPort");
                        String loginId = result.optString("ftpLoginID");
                        String loginPw = result.optString("ftpLoginPW");
                        String workingDirectory = result.optString("ftpDirectory");
                        String appVersion = result.optString("ftpAppVersion");

                        if (!serverIp.isEmpty() && !serverIp.equals("")
                                && !loginId.isEmpty() && !loginId.equals("")
                                && !loginPw.isEmpty() && !loginPw.equals("")
                                && !appVersion.isEmpty() && !appVersion.equals("")
                                && Integer.parseInt(appVersion) > mConfigLoader.getAppVersion()) {
                            LogHelper.i("ota updater 실행");
                            callOTAUpdater(serverIp, serverPort, loginId, loginPw, workingDirectory);
                        }

                        break;

                    default:
                        break;
                }
            } else {
                String errMsg = result.optString(ParamDefine.ERR_MSG);
                LogHelper.e("errMsg : " + errMsg);
            }
        }

        @Override
        public void onError(NetworkData data) {
            LogHelper.d("onError result : " + data.getErrorCode() + " : " + data.getErrorMessage());
        }
    };


}