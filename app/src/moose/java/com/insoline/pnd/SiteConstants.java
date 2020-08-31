package com.insoline.pnd;

public class SiteConstants {
	public static final String PROJECT_NAME = "Moose";
	public static final int APP_VERSION = 700;
	public static final int LIMIT_LENGTH_CAR_NUMBER = 4;

	// 빈차등, 미터기 연동 정보
	public static final String SERIAL_PORT_VACANCYLIGHT = "ttyXRM0";
	public static final String SERIAL_PORT_TACHOMETER = "ttyXRM1";

	public static final int USB_VENDOR_ID = 1250; 		//EP-100 벤더 ID
	public static final int USB_PRODUCT_ID = 5140; 		//EP-100 프로덕트 ID

	// 환경설정 기본설정 정보
	public static final boolean IS_CORPORATION = false; //개인-false, 법인-true
	public static final int SERVICE_NUMBER = 5;
	public static final int CORPORATION_CODE = 10;

	public static final int CONFIG_PST = 30;
	public static final int CONFIG_PSD = 3000;
	public static final int CONFIG_RC = 7;
	public static final int CONFIG_RT = 7;
	public static final int CONFIG_CVT = 6;

	public static final String CALL_SERVER_IP = "58.180.28.220"; 	// 콜 서버 IP
	public static final int CALL_SERVER_PORT = 3000; 				// 콜 서버 PORT
	public static final String API_SERVER_IP = "58.180.28.220"; 	// API 서버 IP
	public static final int API_SERVER_PORT = 8090; 				// API 서버 PORT

	public static final boolean IS_CONNECT_UART = true; 			// true:Uart(Moose), false:USB(Kiev)
	public static final String SERIAL_PORT_TYPE = SERIAL_PORT_VACANCYLIGHT;  	// 미터기 연결 시리얼 포트 타입 (케이블 연결에 따라 변경됨)


	//아이나비 네비 호출
	public static final String INAVI_REQUEST_RUNNAVI = "com.thinkware.externalservice.request.runnavi";
	public static final String INAVI_REQUEST_CURRENTON = "com.thinkware.externalservice.request.currenton";
	public static final String INAVI_REQUEST_RUNROUTENOW = "com.thinkware.externalservice.request.runroutepassui";
	public static final String INAVI_REQUEST_ROUTECANCEL = "com.thinkware.externalservice.request.routecancle";
	public static final String INAVI_REQUEST_STARTINAVI = "com.thinkware.sundo.inavi3d.action.ExtCommand";
	public static final String INAVI_PACKAGE_NAME = "com.thinkware.inavi3ds";
}