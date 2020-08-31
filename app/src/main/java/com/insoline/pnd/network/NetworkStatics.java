package com.insoline.pnd.network;


/**
 * 네트워크 Statics변수 집합소.
 * 서버 URL, 각 API URL 및 코드 등을 final로 가지고 있는다
 */
public class NetworkStatics {

    //Network Error
    public static final String[] NETWORK_ERROR_NO_URL = {"E000001", "NO URL"};
    public static final String[] NETWORK_ERROR_NO_NETWORK_DATA = {"E000002", "NO NETWORK DATA"};
    public static final String[] NETWORK_ERROR_NO_REQEUST_PARAM_DATA = {"E000003", "NO REQUEST PARAM DATA"};
    public static final String[] NETWORK_ERROR_NO_REQEUST_JSON_DATA = {"E000004", "NO REQUEST JSON DATA"};
    public static final String[] NETWORK_ERROR_NO_REQEUST_FILE_DATA = {"E000005", "NO REQUEST FILE SAVE FOLDER"};
    public static final String[] NETWORK_ERROR_NO_REQEUST_FILE_PARAM_DATA = {"E000006", "NO REQUEST FILE SAVE FOLDER OR NO REQUEST FILE PARAM DATA"};
    public static final String[] NETWORK_ERROR_NO_REQEUST_FILE_JSON_DATA = {"E000007", "NO REQUEST FILE SAVE FOLDER OR NO REQUEST FILE JSON DATA"};
    public static final String[] NETWORK_ERROR_NO_REQEUST_UPLOAD_DATA = {"E000008", "NO REQUEST FILE UPLOAD DATA OR NOT ENOUGHT SET"};
    public static final String[] NETWORK_ERROR_RESPONSE = {"E000009", "네트워크 상태가 좋지 않습니다.\n재시도해주세요."};
    public static final String[] NETWORK_ERROR_UNSUPPORTED_ENCODING_EXCEPTION = {"E000010", "UNSUPPORTED_ENCODING_EXCEPTION"};
    public static final String[] NETWORK_ERROR_MALFORMED_URL_EXCEPTION = {"E000011", "MALFORMED_URL_EXCEPTION"};
    public static final String[] NETWORK_ERROR_KEY_MANAGEMENT_EXCEPTION = {"E000012", "KEY_MANAGEMENT_EXCEPTION"};
    public static final String[] NETWORK_ERROR_NOSUCH_ALGORITHM_EXCEPTION = {"E000013", "NOSUCH_ALGORITHM_EXCEPTION"};
    public static final String[] NETWORK_ERROR_PROTOCOL_EXCEPTION = {"E000014", "PROTOCOL_EXCEPTION"};
    public static final String[] NETWORK_IOEXCEPTION = {"E000015", "네트워크 상태가 좋지 않습니다.\n재시도해주세요."};
    public static final String[] NETWORK_JSON_EXCEPTION = {"E000016", "JSON_EXCEPTION"};
    public static final String[] NETWORK_ETC_EXCEPTION = {"E000017", "ETC_EXCEPTION"};

    public static final String[] RESPONSE_CODE_ERROR = {"E999999", "SERVER RESPONSE DATA ERROR"};
}
