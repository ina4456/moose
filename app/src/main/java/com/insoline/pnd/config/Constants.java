package com.insoline.pnd.config;

public class Constants {

	public static final int SERVICE_ID_MAIN = 1000;
	public static final int SERVICE_ID_SCENARIO = 1001;
	public static final int SERVICE_ID_NETWORK = 1002;
	public static final int SERVICE_ID_FLOATING = 1003;

	public static final String CALL_STATUS = "call_status";
	public static final int CALL_STATUS_VACANCY = 2100;
	public static final int CALL_STATUS_VACANCY_IN_WAITING_ZONE = 2110;
	public static final int CALL_STATUS_DRIVING = 2200;     //주행
	public static final int CALL_STATUS_RECEIVING = 2250;   //콜 수신
	public static final int CALL_STATUS_ALLOCATED = 2300;   //배차
	public static final int CALL_STATUS_ALLOCATION_FAILED = 2310;   //배차실패
	public static final int CALL_STATUS_ALLOCATED_WHILE_GETON = 2320;   //승차중배차
	public static final int CALL_STATUS_BOARDED = 2400;    //탑승
	public static final int CALL_STATUS_ALIGHTED = 2500;   //하차
	public static final int CALL_STATUS_BOARDED_WITHOUT_DESTINATION = 2410;
	public static final int CALL_STATUS_WORKING = 2800;
	public static final int CALL_STATUS_RESTING = 2900;


	//미터기 및 UI에서 상태 전환
	public static final String INTENT_KEY_STATUS = "intent_key_status";
	public static final String STATUS_VACANCY = "빈차";
	public static final String STATUS_DRIVING = "주행";
	public static final String STATUS_EXTRA_CHARGE = "할증";
	public static final String STATUS_PAYMENT = "지불";
	public static final String STATUS_CALL = "호출";
	public static final String STATUS_OUT_CITY_OR_COMPLEX = "복합/시외";

	public static final String STATUS_RESERVATION = "예약";
	public static final String STATUS_DAYOFF = "휴무";

	public static final String STATUS_BOARDED = "탑승";
	public static final String STATUS_RESTING = "휴식";
	public static final String STATUS_ALLOCATED = "배차";

	public static final int FONT_SIZE_INC_DEC_VALUE = 8;
	public static final int FONT_SIZE_SMALL = 0;
	public static final int FONT_SIZE_NORMAL = 1;
	public static final int FONT_SIZE_LARGE= 2;


	//Popup Dialog Tag
	public static final String DIALOG_TAG_PERMISSION_ERROR = "dialog_tag_permission_error";
	public static final String DIALOG_TAG_LOGIN_ERROR = "dialog_tag_login_error";

	public static final String DIALOG_TAG_CONFIG_SERVICE_NUM = "dialog_tag_config_service_num";
	public static final String DIALOG_TAG_CONFIG_CORPORATION_CODE = "dialog_tag_config_corporation_code";
	public static final String DIALOG_TAG_CONFIG_CAR_NUM = "dialog_tag_config_car_num";

	public static final String DIALOG_TAG_CONFIG_PST = "dialog_tag_config_pst";
	public static final String DIALOG_TAG_CONFIG_PSD = "dialog_tag_config_psd";
	public static final String DIALOG_TAG_CONFIG_RC = "dialog_tag_config_rc";
	public static final String DIALOG_TAG_CONFIG_RT = "dialog_tag_config_rt";
	public static final String DIALOG_TAG_CONFIG_CVT = "dialog_tag_config_cvt";

	public static final String DIALOG_TAG_CONFIG_SERVER_IP = "dialog_tag_config_server_ip";
	public static final String DIALOG_TAG_CONFIG_SERVER_PORT = "dialog_tag_config_server_port";
	public static final String DIALOG_TAG_CONFIG_API_SERVER_IP = "dialog_tag_config_api_server_ip";
	public static final String DIALOG_TAG_CONFIG_API_SERVER_PORT = "dialog_tag_config_api_server_port";

	public static final String DIALOG_TAG_CONFIG_INVALID = "dialog_tag_config_invalid";
	public static final String DIALOG_TAG_CONFIG_RESTART = "dialog_tag_config_restart";
	public static final String DIALOG_TAG_CONFIG_ADMIN = "dialog_tag_config_admin";

	public static final String DIALOG_TAG_CONFIG_CORPORATION = "dialog_tag_config_corporation";



	public static final String DIALOG_TAG_MESSAGE = "dialog_tag_message";
	public static final String DIALOG_TAG_MESSAGE_WILL_DISMISS = "dialog_tag_message_will_dismiss";
	public static final String DIALOG_TAG_MESSAGE_FROM_LIST = "dialog_tag_message_from_list";
	public static final String DIALOG_TAG_SEND_SMS = "dialog_tag_send_sms";

	public static final String DIALOG_TAG_FINISH_APP = "dialog_tag_finish_app";
	public static final String DIALOG_TAG_LOGOUT = "dialog_tag_logout";
	public static final String DIALOG_TAG_CANCEL_CALL = "dialog_tag_cancel_call";
	public static final String DIALOG_TAG_CANCEL_CALL_THEN_LOGOUT = "dialog_tag_cancel_call_then_logout";
	public static final String DIALOG_TAG_CANCEL_CALL_THEN_FINISH_APP = "dialog_tag_cancel_call_then_finish_app";
	public static final String DIALOG_TAG_COMPLETE_CALL = "dialog_tag_complete_call";
	public static final String DIALOG_TAG_COMPLETE_CALL_THEN_LOGOUT = "dialog_tag_complete_call_then_logout";
	public static final String DIALOG_TAG_NOTICE = "dialog_tag_notice";
	public static final String DIALOG_TAG_ALLOCATION_FAILURE = "dialog_tag_allocation_failure";
	public static final String DIALOG_TAG_MESSAGE_SELECTION = "dialog_tag_message_selection";
	public static final String DIALOG_TAG_CANCEL_REASON_SELECTION = "dialog_tag_cancel_reason_selection";
	public static final String DIALOG_TAG_ROUTING_TO_DEPARTURE = "dialog_tag_routing_to_departure";
	public static final String DIALOG_TAG_ROUTING_TO_DESTINATION = "dialog_tag_routing_to_destination";
	public static final String DIALOG_TAG_ALIGHTED = "dialog_tag_alighted";
	public static final String DIALOG_TAG_EXIT_WAITING_ZONE = "dialog_tag_exit_waiting_zone";

	public static final String DIALOG_TAG_FAILURE = "dialog_tag_failure";


	public static final String DIALOG_INTENT_KEY_PRESSED_POSITIVE_BTN = "dialog_intent_key_pressed_positive_btn";
	public static final String DIALOG_INTENT_KEY_SELECTED_ITEM = "dialog_intent_key_selected_item";
	public static final String DIALOG_INTENT_KEY_SELECTED_ITEM_INSTALLED = "dialog_intent_key_selected_item_installed";
	public static final String DIALOG_INTENT_KEY_SELECTED_ITEM_ID = "dialog_intent_key_selected_item_id";
	public static final String DIALOG_INTENT_KEY_ITEMS = "dialog_intent_key_checked_items";
	public static final String DIALOG_INTENT_KEY_EDIT_TEXT = "dialog_intent_key_edit_text";
	public static final String DIALOG_INTENT_KEY_RADIO_BUTTON = "dialog_intent_key_radio_button";
	public static final String DIALOG_INTENT_KEY_RADIO_BUTTON_ID = "dialog_intent_key_radio_button_position";
	public static final String DIALOG_INTENT_KEY_CHECKED_FLOATING_BTNS = "dialog_intent_key_checked_floating_btns";





	//System Property Key
	public static final String SP_KEY_LOGIN_STATUS = "service.houston.login_status";  // 0: 로그아웃, 1: 로그인-운행, 2: 로그인-휴식
	public static final String SP_KEY_DRIVER_PHONE_NUMBER = "service.houston.driver_phone";
	//System Property Value
	public static final String SP_VAL_LOGIN_STATUS_LOGOUT = "0";
	public static final String SP_VAL_LOGIN_STATUS_LOGIN = "1";
	public static final String SP_VAL_LOGIN_STATUS_LOGIN_REST = "2";


	//외부 앱 연동
	public static final String PACKAGE_NAME_DRIVER = "com.thinkware.houston.driver";
	public static final String PACKAGE_NAME_EXTERNAL_DEVICE = "com.thinkware.houston.externaldevice";
	public static final String CATEGORY_EXECUTE_AFTER_LOGIN = "houston.intent.category.EXECUTE_AFTER_LOGIN";

	public static final String INTENT_ACTION_EXT_MAIN = "com.thinkware.houston.externaldevice.service.MainService";
	public static final String INTENT_ACTION_EXT_TACHOMETER = "com.thinkware.houston.externaldevice.service.TachoMeterService";
	public static final String INTENT_ACTION_EXT_VACANCY_LIGHT = "com.thinkware.houston.externaldevice.service.VacancyLightService";
	public static final String INTENT_ACTION_SERVICEX_ADB_COMMAND = "com.thinkware.florida.servicex.intent.action.ADB_COMMAND";
	public static final String INTENT_ACTION_SERVICEX_CLEAR_DATA = "com.thinkware.florida.servicex.intent.action.CLEAR_DATA";
	public static final String INTENT_ACTION_CLEAR_DATA = "com.thinkware.houston.servicex.intent.action.CLEAR_DATA";

	public static final String INTENT_KEY_CALL_INFO = "intent_key_call_info";
	public static final String INTENT_KEY_POPUP_DIALOG = "intent_key_popup_dialog";
	public static final String INTENT_KEY_POPUP_LISTENER = "intent_key_popup_listener";
	public static final String INTENT_KEY_ADB_COMMAND = "extra_key_adb_command";
	public static final String INTENT_KEY_CLEAR_DATA = "extra_key_package_name";
	public static final String INTENT_VALUE_CLEAR_DATA = "com.kakao.taxi.driver";

	public static final String INTENT_KEY_PORT_PATH = "portPath";
	public static final String INTENT_KEY_PORT_PATH_VACANCY = "portPathVacancy";
	public static final String INTENT_KEY_PORT_PATH_METER = "portPathMeter";
	public static final String INTENT_KEY_METER_TYPE = "meterType";



	//Content Provider Key
	public static final String CONFIG_PROVIDER = "content://com.thinkware.houston.configuration.ConfigurationProvider";

	public static final String CP_KEY_OPERATION_AREA = "operationArea";
	public static final String CP_KEY_IS_CORPORATION = "isCorporation";
	public static final String CP_KEY_CAR_NUMBER = "carNumber";
	public static final String CP_KEY_DRIVER_PHONE_NUMBER = "driverPhoneNumber";

	public static final String CP_KEY_SERIAL_PORT_TYPE = "serialPortType";
	public static final String CP_KEY_TACHOMETER_TYPE = "tachometerType";

	public static final String CP_KEY_CONNECT_TACHOMETER_WITH_UART = "connectTachometerWithUart";
	public static final String CP_KEY_USE_TACHOMETER_SIGNAL = "useTachometerSignal";
	public static final String CP_KEY_IS_EMERGENCY_USE = "isEmergencyUse";
	public static final String CP_KEY_IS_FARE_ALERT_USE = "isFareAlertUse";
	public static final String CP_KEY_MODEM_TYPE = "modemType";

	public static final String CP_KEY_SERVICE_CODE = "serviceCode";
	public static final String CP_KEY_CALL_SERVER_IP = "callServerIp";
	public static final String CP_KEY_CALL_SERVER_PORT = "callServerPort";
	public static final String CP_KEY_APP_VERSION = "appVersion";

	public static final String CP_KEY_IS_LOCAL_LOGIN_USE = "isLocalLoginUse";


	//Shared Preference Key (배차 관련 없는 단순 개인 설정 정보 저장)
	public static final String SPFR_LOCAL_DATA = "INAVICALL.LOCAL_DATA";
	public static final String SPFR_KEY_NEED_AUTO_LOGIN = "bool_auto_login";
	public static final String SPFR_KEY_NEED_AUTO_LOGIN_NEXT_BOOT = "bool_auto_login_next_boot";

	//로그인 번호에 따른 개인화 설정
	public static final String SPFR_KEY_AUTO_ROUTING_TO_PASSENGER = "bool_auto_routing_to_passenger";
	public static final String SPFR_KEY_AUTO_ROUTING_TO_DESTINATION = "bool_auto_routing_to_destination";
	public static final String SPFR_KEY_FLOATING_BTN_BOARDING_ALIGHTING = "bool_floating_btn_boarding_alighting";
	public static final String SPFR_KEY_FLOATING_BTN_ENTERING_APP = "bool_floating_btn_entering_app";
	public static final String SPFR_KEY_FLOATING_BTN_CALL = "bool_floating_btn_call";

	public static final String SPFR_KEY_HAS_CALLED_LOGOUT_BROADCAST = "has_called_logout_broadcast";


	public static final String EXTRA_KEY_PACKAGE_NAME = "extra_key_package_name";


	//성남은 개인과 법인이 동일 버전을 쓰므로 대표번호인 0을 쓴다.
	public static final int AREA_SUNGNAM_GEN = 0;       //성남 일반
	public static final int AREA_SUNGNAM_GAEIN = 5;     //성남 개인
	public static final int AREA_SUNGNAM_CORP = 6;      //성남 법인
	public static final int AREA_SUNGNAM_MOBUM = 9;     //성남 모범
	public static final int AREA_SUNGNAM_BOKJI = 22;    //성남 복지

	public static final int AREA_KWANGJU = 3;           //광주

	//하남은 개인과 법인이 동일 버전을 쓰므로 대표번호인 100을 쓴다.
	public static final int AREA_HANAM_GEN = 100;       //하남 일반
	public static final int AREA_HANAM_GAEIN = 11;      //하남 개인
	public static final int AREA_HANAM_CORP = 12;       //하남 법인

	public static final int AREA_ICHON = 13;            //이천
}