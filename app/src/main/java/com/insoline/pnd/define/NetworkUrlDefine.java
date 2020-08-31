package com.insoline.pnd.define;

/**
 * <br>
 * 네트워크 URL 정의 ENUM
 * </br>
 *
 * @see Enum
 */

public enum NetworkUrlDefine {

	//서버 시간 요청
	REQUEST_SERVER_TIME("/requestServerTime"),

    //app 업데이트 서버 접속 정보
    APP_UPDATE_SERVER_INFO("/post"),

    //Test
    TEST("/post");



    private final String url;

    NetworkUrlDefine(String url) {
        this.url = url;
    }

    /**
     * 도매인과 URL 을 붙여서 가져온다.
     *
     * @return DOMAIN + URL;
     */
    public String getUrl() {
        return AppDefine.DOMAIN + url;
    }

    /**
     * 도매인을 제외한 URL 만 가져온다.
     *
     * @return URL
     */
    public String getUrlWithoutDomain() {
        return url;
    }

}
