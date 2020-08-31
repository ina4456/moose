package com.insoline.pnd.network;

import org.json.JSONObject;

public class NetworkResponseCheck {

    public static boolean isValid(JSONObject result) {
//        if (result != null && result.length() > 0 && HttpURLConnection.HTTP_OK == result.optInt(ParamDefine.STATUS, -1))) {
        if (result != null && result.length() > 0 ) {
                return true;
        }
        return false;
    }


}
