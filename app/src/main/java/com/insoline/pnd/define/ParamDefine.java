package com.insoline.pnd.define;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused, WeakerAccess")
public class ParamDefine {
	public static final String GET = "GET";
	public static final String POST = "POST";

    //200 이면 정상 이외는 오류
    public static final String STATUS = "status";

    //기본 전문
    public static final String TIME_STAMP = "timestamp";
    public static final String RESULT = "result";
    public static final String PARAM = "param";
	public static final String IS_SUCCESSFUL = "isSuccessful";
	public static final String ERR_MSG = "errMsg";

	//앱 업데이트 서버 접속 정보
	public static final String AUTH_CODE = "auth_code";
	public static final String CUR_DATE_TIME = "curDateTime";
	public static final String CAR_ID = "carID";
	public static final String SERVICE_CODE = "serviceCode";
	public static final String TCOM_CODE = "tcomCode";
	public static final String CUR_APP_VERSION = "curAppVersion";



	@SafeVarargs
    private static JSONObject getParams(JSONObject... params) {
        JSONObject result;
        if (params != null && params.length > 0) {
            result = params[0];
        } else {
            result = new JSONObject();
        }
        return result;
    }

    /**
     * 앱 업데이트 서버 접속 정보
     */
    public static JSONObject getParamsApiAppUpdateServerInfo(String authCode, String curDateTime, String carID, String serviceCode, String tcomCode, String curAppVersion) {
	    JSONObject result = new JSONObject();
	    try {
		    result.put(AUTH_CODE, authCode);
		    result.put(CUR_DATE_TIME, curDateTime);
		    result.put(CAR_ID, carID);
		    result.put(SERVICE_CODE, serviceCode);
		    result.put(TCOM_CODE, tcomCode);
		    result.put(CUR_APP_VERSION, curAppVersion);
	    } catch (JSONException e) {
	    	e.printStackTrace();
	    }

        return result;
    }

}