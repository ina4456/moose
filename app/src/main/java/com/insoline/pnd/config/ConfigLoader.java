package com.insoline.pnd.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.insoline.pnd.SiteConstants;
import com.insoline.pnd.common.BaseApplication;
import com.insoline.pnd.remote.packets.server2mdt.ResponseConfigPacket;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.PreferenceUtil;

import java.io.File;

/**
 * Created by zic325 on 2016. 9. 13..
 */
public class ConfigLoader {
    private static final ConfigLoader instance = new ConfigLoader();

    private PreferenceUtil mPreferenceUtil;
    private Config mConfig;
    private Context mContext;

    public class Config {
        protected boolean initiated;
        protected boolean isLoaded;
        protected int appVersion; // 프로그램 버전
        protected int configVersion; // 환경설정 버전

        protected int serviceNumber; // 서비스 번호
        protected boolean isCorporation; // 개인/법인 여부 - 개인(false) , 법인(true)
        protected int corporationCode; // 회사 코드
        protected int carId; // Car ID
        protected String driverPhoneNumber; // 운전자 번호(직접입력하는 번호)
        protected String modemNumber; // 모뎀 번호(단말 전화번호)

        protected String callServerIp; // 콜 서버 IP
        protected int callServerPort; // 콜 서버 PORT
        protected String apiServerIp; // API 서버 IP
        protected int apiServerPort; // API 서버 PORT

        protected int pst; // 주기 전송 시간
        protected int psd; // 주기 전송 거리
        protected int rc; // 모바일콜 배차 후 승차신호 올라올 때까지 8초 주기로 주기 전송.
        protected int rt; // 재시도 시간 (Live 패킷 전송 주기)
        protected int cvt; // 콜 방송 표기 시간
        protected boolean ls; // 로그 저장 여부

        protected boolean isConnectUart;  // true:Uart(Moose), false:USB(Kiev) - 설정 UI 노출되지 않음.
        protected String serialPortType;  // 미터기 연결 시리얼 포트 타입 (케이블 연결에 따라 변경됨) - 설정 UI 노출되지 않음.
        protected boolean isVacancyLight; // 승빈차 신호를 빈차등으로 할지 미터기로 할지 true:빈차등, false:미터기
        protected String meterDeviceType; // 승빈차 신호를 미터기로 했을 경우 미터기 타입(금호, 한국MTS, 광신, EB통합미터)

        protected String password;
        protected int emergencyPeriodTime; // Emergency 주기 시간

        protected String callCenterNumber;  //콜센터 번호
        protected boolean isAutoLogin;  //자동로그인 유무 (true:개인, false:법인, 로그아웃한 개인)


        @Override
        public String toString() {
            return "Config{" +
                    "initiated=" + initiated +
                    ", isLoaded=" + isLoaded +
                    ", appVersion=" + appVersion +
                    ", configVersion=" + configVersion +
                    ", serviceNumber=" + serviceNumber +
                    ", isCorporation=" + isCorporation +
                    ", corporationCode=" + corporationCode +
                    ", carId=" + carId +
                    ", driverPhoneNumber='" + driverPhoneNumber + '\'' +
                    ", modemNumber='" + modemNumber + '\'' +
                    ", callServerIp='" + callServerIp + '\'' +
                    ", callServerPort=" + callServerPort +
                    ", apiServerIp='" + apiServerIp + '\'' +
                    ", apiServerPort=" + apiServerPort +
                    ", pst=" + pst +
                    ", psd=" + psd +
                    ", rc=" + rc +
                    ", rt=" + rt +
                    ", cvt=" + cvt +
                    ", ls=" + ls +
                    ", isConnectUart=" + isConnectUart +
                    ", serialPortType='" + serialPortType + '\'' +
                    ", isVacancyLight=" + isVacancyLight +
                    ", meterDeviceType='" + meterDeviceType + '\'' +
                    ", password='" + password + '\'' +
                    ", emergencyPeriodTime=" + emergencyPeriodTime +
                    ", callCenterNumber=" + callCenterNumber +
                    ", isAutoLogin=" + isAutoLogin +
                    '}';
        }
    }

    private ConfigLoader() {
        // Activity와 서비스 각각 객체 생성시 환경 설정 정보 싱크가 맞지 않는 이슈가 있어
        // singletone으로 정의 한다.
    }

    public static ConfigLoader getInstance() {
        return instance;
    }

    public void initialize(Context context) {
        this.mPreferenceUtil = new PreferenceUtil(context);
        this.mContext = context;
        load();
    }

    public String toString() {
        return mConfig.toString();
    }

    private void load() {

        mConfig = mPreferenceUtil.getConfiguration();
        if (mConfig != null) {
            mConfig.isLoaded = true;

        } else {
            // load default
            mConfig = new Config();
            mConfig.initiated = false;
            mConfig.isLoaded = false;
            mConfig.appVersion = SiteConstants.APP_VERSION;
            mConfig.configVersion = 0;

            mConfig.isCorporation = SiteConstants.IS_CORPORATION;
            mConfig.serviceNumber = SiteConstants.SERVICE_NUMBER;
            mConfig.corporationCode = SiteConstants.CORPORATION_CODE;

            mConfig.callServerIp = SiteConstants.CALL_SERVER_IP;
            mConfig.callServerPort = SiteConstants.CALL_SERVER_PORT;
            mConfig.apiServerIp = SiteConstants.API_SERVER_IP;
            mConfig.apiServerPort = SiteConstants.API_SERVER_PORT;

            mConfig.pst = SiteConstants.CONFIG_PST;
            mConfig.psd = SiteConstants.CONFIG_PSD;
            mConfig.rc = SiteConstants.CONFIG_RC;
            mConfig.rt = SiteConstants.CONFIG_RT;
            mConfig.cvt = SiteConstants.CONFIG_CVT;
            mConfig.ls = false;

            mConfig.isConnectUart = SiteConstants.IS_CONNECT_UART;
            mConfig.serialPortType = SiteConstants.SERIAL_PORT_TYPE;
            mConfig.isVacancyLight = true;
            mConfig.meterDeviceType = "금호";

            mConfig.password = "0";
            mConfig.emergencyPeriodTime = 10;

            mConfig.callCenterNumber = "";
            mConfig.isAutoLogin = false;
        }

        mkLoggingDir();

        LogHelper.write("==> 환경설정 로드 : " + mConfig.toString());
    }

    public boolean hasConfiguration() {
        return mConfig.initiated;
    }

    public void write(ResponseConfigPacket response) {
        mConfig.carId = response.getCarId();
        mConfig.configVersion = response.getVersion();
        mConfig.pst = response.getPeriodSendingTime();
        mConfig.psd = response.getPeriodSendingRange();
        mConfig.rc = response.getRetryNumber();
        mConfig.rt = response.getRetryTime();
        mConfig.cvt = response.getCallAcceptanceTime();
        mConfig.emergencyPeriodTime = response.getPeriodEmergency();
        boolean isLogging = mConfig.ls;
        mConfig.ls = response.isLogging();
        mConfig.callServerIp = response.getCallServerIp();
        mConfig.callServerPort = response.getCallServerPort();
        mConfig.apiServerIp = response.getUpdateServerIp();
        mConfig.apiServerPort = response.getUpdateServerPort();
        mConfig.password = response.getPassword();
        save();

        if (isLogging != mConfig.ls) {
            mkLoggingDir();
        }
    }

    public void mkLoggingDir() {
        if (mConfig.ls) {
            String rootPath = ((BaseApplication) mContext.getApplicationContext()).getInternalDir();
            File directory = new File(rootPath);
            if (directory == null || !directory.exists()) {
                directory.mkdirs();
            }
            LogHelper.enableWriteLogFile(directory.getAbsolutePath());
        } else {
            LogHelper.disableWriteLogFile();
        }
    }

    public void save() {
        Gson gson = new GsonBuilder().create();

        mConfig.initiated = true;

        // 법인의 경우 전화번호를 저장하면 안된다.
//        String driverNumber = "";
//        if (mConfig.isCorporation) {
//            driverNumber = mConfig.driverPhoneNumber;
//            mConfig.driverPhoneNumber = "";
//        }
        String config = gson.toJson(mConfig);
        mPreferenceUtil.setConfiguration(config);
        LogHelper.write("==> 환경설정 저장 : " + mConfig.toString());

//        if (mConfig.isCorporation) {
//            mConfig.driverPhoneNumber = driverNumber;
//        }
    }

    public boolean isLoaded() {
        return mConfig.isLoaded;
    }

    public void setLoaded(boolean loaded) {
        mConfig.isLoaded = loaded;
    }

    public int getAppVersion() {
        return mConfig.appVersion;
    }

    public void setAppVersion(int appVersion) {
        mConfig.appVersion = appVersion;
    }

    public int getConfigVersion() {
        return mConfig.configVersion;
    }

    public void setConfigVersion(int configVersion) {
        mConfig.configVersion = configVersion;
    }

    public int getServiceNumber() {
        return mConfig.serviceNumber;
    }

    public void setServiceNumber(int serviceNumber) {
        mConfig.serviceNumber = serviceNumber;
    }

    public boolean isCorporation() {
        return mConfig.isCorporation;
    }

    public void setCorporation(boolean corporation) {
        mConfig.isCorporation = corporation;
    }

    public int getCorporationCode() {
        return mConfig.corporationCode;
    }

    public void setCorporationCode(int corporationCode) {
        mConfig.corporationCode = corporationCode;
    }

    public int getCarId() {
        return mConfig.carId;
    }

    public void setCarId(int carId) {
        mConfig.carId = carId;
    }

    public String getDriverPhoneNumber() {
        return mConfig.driverPhoneNumber;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        mConfig.driverPhoneNumber = driverPhoneNumber;
    }

    public String getModemNumber() {
        return mConfig.modemNumber;
    }

    public void setModemNumber(String modemNumber) {
        mConfig.modemNumber = modemNumber;

        // 동부교통앱에 공유할 모뎀번호는 별도의 preference에 저장
//        SharedPreferences preferences = mContext.getSharedPreferences("ShareToOtherApp", Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
//        SharedPreferences.Editor editor = preferences.edit();
//        if (TextUtils.isEmpty(modemNumber)) {
//            editor.putString("ModemNumber", modemNumber);
//        } else {
//            editor.putString("ModemNumber", modemNumber.replace("+82", "0"));
//        }
//        editor.commit();
    }

    public String getCallServerIp() {
        return mConfig.callServerIp;
    }

    public void setCallServerIp(String callServerIp) {
        mConfig.callServerIp = callServerIp;
    }

    public int getCallServerPort() {
        return mConfig.callServerPort;
    }

    public void setCallServerPort(int callServerPort) {
        mConfig.callServerPort = callServerPort;
    }

    public String getApiServerIp() {
        return mConfig.apiServerIp;
    }

    public void setApiServerIp(String apiServerIp) {
        mConfig.apiServerIp = apiServerIp;
    }

    public int getApiServerPort() {
        return mConfig.apiServerPort;
    }

    public void setApiServerPort(int apiServerPort) {
        mConfig.apiServerPort = apiServerPort;
    }

    public int getPst() {
        return mConfig.pst;
    }

    public void setPst(int pst) {
        mConfig.pst = pst;
    }

    public int getPsd() {
        return mConfig.psd;
    }

    public void setPsd(int psd) {
        mConfig.psd = psd;
    }

    public int getRc() {
        return mConfig.rc;
    }

    public void setRc(int rc) {
        mConfig.rc = rc;
    }

    public int getRt() {
        return mConfig.rt;
    }

    public void setRt(int rt) {
        mConfig.rt = rt;
    }

    public int getCvt() {
        return mConfig.cvt;
    }

    public void setCvt(int cvt) {
        mConfig.cvt = cvt;
    }

    public boolean isLs() {
        return mConfig.ls;
    }

    public void setLs(boolean ls) {
        mConfig.ls = ls;
    }

    public boolean isConnectUart() {
        return mConfig.isConnectUart;
    }

    public void setConnectUart(boolean isConnectUart) {
        mConfig.isConnectUart = isConnectUart;
    }

    public String getSerialPortType() {
        return mConfig.serialPortType;
    }

    public void setSerialPortType(String serialPortType) {
        mConfig.serialPortType = serialPortType;
    }

    public boolean isVacancyLight() {
        return mConfig.isVacancyLight;
    }

    public void setVacancyLight(boolean boardingSignal) {
        mConfig.isVacancyLight = boardingSignal;

        if (isConnectUart()) {
            if (isVacancyLight())
                setSerialPortType(SiteConstants.SERIAL_PORT_VACANCYLIGHT);
            else
                setSerialPortType(SiteConstants.SERIAL_PORT_TACHOMETER);
        }
    }

    public String getMeterDeviceType() {
        return mConfig.meterDeviceType;
    }

    public void setMeterDeviceType(String meterDeviceType) {
        mConfig.meterDeviceType = meterDeviceType;
    }

    public String getPassword() {
        return mConfig.password;
    }

    public void setPassword(String password) {
        mConfig.password = password;
    }

    public int getEmergencyPeriodTime() {
        return mConfig.emergencyPeriodTime;
    }

    public void setEmergencyPeriodTime(int time) {
        mConfig.emergencyPeriodTime = time;
    }

    public String getCallCenterNumber() {
        return mConfig.callCenterNumber;
    }

    public void setCallCenterNumber(String callCenterNumber) {
        mConfig.callCenterNumber = callCenterNumber;
    }

    public boolean isAutoLogin() {
        return mConfig.isAutoLogin;
    }

    public void setAutoLogin(boolean isAutoLogin) {
        mConfig.isAutoLogin = isAutoLogin;
    }

}
