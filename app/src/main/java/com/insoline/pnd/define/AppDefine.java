package com.insoline.pnd.define;

import com.insoline.pnd.BuildConfig;
import com.insoline.pnd.config.ConfigLoader;

/**
 * <br>
 * APP 전체 DEFINE
 * </br>
 */

public class AppDefine {
    /**
     * Domain 정의 ENUM  TODO: TO-BE
     */
    public enum NetworkDomainDefine {
        DEV("https://"),
        REAL("https://");

        private final String domain;

        NetworkDomainDefine(String domain) {
            this.domain = domain;
        }

        public String getDomain() {
            return domain;
        }
    }

    public static NetworkDomainDefine SERVER_TYPE = (BuildConfig.DEBUG ? NetworkDomainDefine.DEV : NetworkDomainDefine.REAL);

    // 전문 서버
    public static String DOMAIN = SERVER_TYPE.getDomain() + ConfigLoader.getInstance().getApiServerIp() + ":" + ConfigLoader.getInstance().getApiServerPort();

    // live packet Server
    public static final String IP_LIVE_PACKET = "58.180.28.212";


    //device 패키지명
    public static final String PKG_NAME_EXTERNAL_DEVICE = "com.thinkware.houston.externaldevice";
    public static final String PKG_NAME_EXTERNAL_DEVICE_MAIN = "com.thinkware.houston.externaldevice.service.MainService";
    public static final String PKG_NAME_EXTERNAL_DEVICE_TACHOMETER = "com.thinkware.houston.externaldevice.service.TachoMeterService";
    public static final String PKG_NAME_EXTERNAL_DEVICE_VACANCYLIGHT = "com.thinkware.houston.externaldevice.service.VacancyLightService";

    public static final String PKG_NAME_SERVICEX_ADB_COMMAND = "com.thinkware.florida.servicex.intent.action.ADB_COMMAND";


    //업데이터 패키지명
    public static final String PKG_NAME_OTA_UPDATER = "com.insoline.moose.otaupdater";
    public static final String PKG_NAME_OTA_UPDATER_SERVICE = "com.insoline.moose.otaupdater.Updater";

    //동부교통(교통정보) 패키지명
    public static final String PKG_NAME_DONGBUNTS = "com.ntis.dongbunts.bstraffic";
    public static final String PKG_NAME_DONGBUNTS_SERVICE = "com.ntis.dongbunts.bstraffic.manager.BSService";

}
