package com.insoline.pnd.network;

import android.os.Handler;
import android.os.Message;

import com.insoline.pnd.cookie.CookieManager;
import com.insoline.pnd.define.AppDefine;
import com.insoline.pnd.network.listener.NetworkRunListener;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;


/**
 * 실제로 백그라운드에서 실제 전문 처리
 */
public class NetworkRunnable implements Runnable {

    private static final String TAG = NetworkRunnable.class.getName();

    private final NetworkData mNetworkData;
    private final NetworkRunListener mNetworkListener;
    private final ResponseHandler mResponseHandler = new ResponseHandler(this);

    private enum MsgStatus {
        MSG_ERROR, MSG_PROGRESS, MSG_BACKGROUND, MSG_SUCCESS
    }

    @SuppressWarnings("WeakerAccess")
    public NetworkRunnable(NetworkData networkData) {
        mNetworkData = networkData;
        mNetworkListener = networkData.getNetworkListener();
    }

    @Override
    public void run() {
        Message responseMsg = new Message();
        LogHelper.d("\n    ********** NetworkRunnable Request Start *********** " + "\n"
                + "    REQUEST_HEADER : " + mNetworkData.getRequestHeaderData() + "\n"
                + "    URL : " + mNetworkData.getUrl() + "\n"
                + "    REQUEST PARAM : " + mNetworkData.getRequestParams() + "\n"
                + "    ********** NetworkRunnable Request End  ***********");
        if (mNetworkData.isValid()) {
            OutputStream outputStream = null;
            InputStream inputStream = null;
            HttpURLConnection httpURLConnection = null;
            boolean isError = false;

            try {
                URL url = new URL(mNetworkData.getUrl());
                httpURLConnection = getConnection(url, mNetworkData);

//	            httpURLConnection.setRequestMethod("POST");
	            httpURLConnection.setRequestMethod(mNetworkData.getRequestMethod());

                String getCookie = CookieManager.getCookie(AppDefine.DOMAIN);
                if (StringUtil.isNotEmpty(getCookie) && mNetworkData.getUrl().contains(AppDefine.DOMAIN)) {
                    httpURLConnection.setRequestProperty("Cookie", getCookie);
                }

//                httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");

	            httpURLConnection.setRequestProperty("Accept", "application/json");
	            httpURLConnection.setRequestProperty("Content-type", "application/json;charset=UTF-8");

                if (!mNetworkData.getRequestMethod().equals("GET")) {
	                httpURLConnection.setDoOutput(true);

	                outputStream = httpURLConnection.getOutputStream();
	                outputStream.write(getParamQuery(mNetworkData.getRequestParams()).getBytes());

	                outputStream.flush();
	                outputStream.close();
	                outputStream = null;
                }

	            LogHelper.d("HttpURLConnection getRequestMethod : " + httpURLConnection.getRequestMethod());
	            LogHelper.d("HttpURLConnection Request : " + httpURLConnection.getHeaderFields());

                int response = httpURLConnection.getResponseCode();
                LogHelper.d("HttpURLConnection response : " + response);

                if (response == HttpURLConnection.HTTP_OK) {
                    mNetworkData.setResponseHeaderData(httpURLConnection.getHeaderFields());
                    inputStream = httpURLConnection.getInputStream();
                    mNetworkData.setResult(new JSONObject(getResult(inputStream)));
                } else {
                    isError = true;
                    mNetworkData.setErrorCode(NetworkStatics.NETWORK_ERROR_RESPONSE[0]);
                    mNetworkData.setErrorMessage(""+response);
                }
                if (!isError) {
                    //COOKIE Sync
//                    CookieManager.setCookieSync(mNetworkData.getContext(), AppDefine.DOMAIN);
//                    LogHelper.d(TAG, "setCookieSync ");

                    if (NetworkResponseCheck.isValid(mNetworkData.getResult())) {
                        if (mNetworkListener != null)
                            mNetworkListener.doInBackground(mNetworkData);
                    } else {
                        isError = true;
                        mNetworkData.setErrorCode(NetworkStatics.RESPONSE_CODE_ERROR[0]);
                        mNetworkData.setErrorMessage(NetworkStatics.RESPONSE_CODE_ERROR[1]);
                    }

                }

            } catch (UnsupportedEncodingException e) {
                LogHelper.e("UnsupportedEncodingException ", e);
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_ERROR_UNSUPPORTED_ENCODING_EXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_ERROR_UNSUPPORTED_ENCODING_EXCEPTION[1]);
            } catch (MalformedURLException e) {
                LogHelper.e("MalformedURLException ", e);
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_ERROR_MALFORMED_URL_EXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_ERROR_MALFORMED_URL_EXCEPTION[1]);
            } catch (KeyManagementException e) {
                LogHelper.e("KeyManagementException ", e);
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_ERROR_KEY_MANAGEMENT_EXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_ERROR_KEY_MANAGEMENT_EXCEPTION[1]);
            } catch (NoSuchAlgorithmException e) {
                LogHelper.e("NoSuchAlgorithmException ", e);
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_ERROR_NOSUCH_ALGORITHM_EXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_ERROR_NOSUCH_ALGORITHM_EXCEPTION[1]);
            } catch (ProtocolException e) {
                LogHelper.e("ProtocolException ", e);
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_ERROR_PROTOCOL_EXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_ERROR_PROTOCOL_EXCEPTION[1]);
            } catch (IOException e) {
                LogHelper.e("IOException ", e);
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_IOEXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_IOEXCEPTION[1]);
            } catch (JSONException e) {
                LogHelper.e("JSONException ", e);
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_JSON_EXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_JSON_EXCEPTION[1]);
            } catch (Exception e) {
                LogHelper.e("Exception ", e.toString());
                isError = true;
                mNetworkData.setErrorCode(NetworkStatics.NETWORK_ETC_EXCEPTION[0]);
                mNetworkData.setErrorMessage(NetworkStatics.NETWORK_ETC_EXCEPTION[1]);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        LogHelper.e("IOException ", e);
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogHelper.e("IOException ", e);
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

                if (isError) {
                    responseMsg.what = MsgStatus.MSG_ERROR.ordinal();
                    responseMsg.obj = mNetworkData;
                } else {
                    responseMsg.what = MsgStatus.MSG_SUCCESS.ordinal();
                    responseMsg.obj = mNetworkData;
                }

                mResponseHandler.sendMessage(responseMsg);
            }

        } else {
            mNetworkData.setErrorCode(NetworkStatics.NETWORK_ERROR_NO_URL[0]);
            mNetworkData.setErrorMessage(NetworkStatics.NETWORK_ERROR_NO_URL[1]);
            responseMsg.what = MsgStatus.MSG_ERROR.ordinal();
            responseMsg.obj = mNetworkData;
            mResponseHandler.sendMessage(responseMsg);
        }
    }

    private static final String COOKIES_HEADER = "Set-Cookie";

    @SuppressWarnings("unused")
    private List<String> parseCookie(HttpURLConnection connection, NetworkData networkData) {
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                LogHelper.d("COOKIE", "cookiesHeader:" + cookie);
            }
        }
        return cookiesHeader;
    }

    private HttpURLConnection getConnection(URL url, NetworkData networkdata) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        HttpURLConnection connection;

        if ("https".equals(url.getProtocol())) {
//            trustAllHosts();
            connection = (HttpsURLConnection) url.openConnection();
            NetworkUtil.trustAllHosts((HttpsURLConnection) connection);
            NetworkUtil.setDoNotVerify((HttpsURLConnection) connection);
//            ((HttpsURLConnection) connection).setHostnameVerifier(DO_NOT_VERIFY);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        if (networkdata.getRequestHeaderData() != null && networkdata.getRequestHeaderData().size() > 0) {
            Set<String> keySet = networkdata.getRequestHeaderData().keySet();
            for (String key : keySet) {
                connection.setRequestProperty(key, networkdata.getRequestHeaderData().get(key));
            }
        }
        connection.setReadTimeout(networkdata.getReadTimeOut());
        connection.setConnectTimeout(networkdata.getConnectionTimeOut());


        if (LogHelper.DEBUG) {
            Map<String, List<String>> props = connection.getRequestProperties();
            Set<String> keys = props.keySet();
            for (String key : keys) {
                List<String> values = props.get(key);
                StringBuilder builder = new StringBuilder();
                if (values != null) {
                    for (String value : values) {
                        builder.append("||");
                        builder.append(value);
                    }
                }

                LogHelper.d("RequestProperty[" + key + "]" + builder.toString());
            }
        }

        return connection;
    }

    private String getResult(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();

        LogHelper.d("getResult : " + builder.toString());

        return builder.toString();
    }

	private String getParamQuery(JSONObject params) {
		if (params == null)
			return "";
		return params.toString();
	}

    private String getParamQuery(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null)
            return "";
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String key : params.keySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(
                    URLEncoder.encode(key, "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(params.get(key), "UTF-8")
                    );
        }
        return result.toString();
    }

    public class ResponseHandler extends Handler {

        private final WeakReference<NetworkRunnable> mRunnable;

        @SuppressWarnings("WeakerAccess")
        public ResponseHandler(NetworkRunnable runnable) {
            mRunnable = new WeakReference<>(runnable);
        }

        @Override
        public void handleMessage(Message msg) {
            NetworkRunnable runnable = mRunnable.get();
            if (runnable == null || mNetworkListener == null) {
                return;
            }

            MsgStatus status = MsgStatus.values()[msg.what];
            switch (status) {
                case MSG_PROGRESS:
                    break;
                case MSG_ERROR:
                    LogHelper.d("\n    ********** NetworkRunnable Error Start *********** " + "\n"
                            + "    URL : " + mNetworkData.getUrl() + "\n"
                            + "    REQUEST PARAM : " + mNetworkData.getRequestParams() + "\n"
                            + "    RESULT JSON : " + mNetworkData.getResult() + "\n"
                            + "    ********** NetworkRunnable Error Start ***********");
                    mNetworkListener.onError((NetworkData) msg.obj);
                    break;
                case MSG_BACKGROUND:
                    mNetworkListener.doInBackground((NetworkData) msg.obj);
                    break;
                case MSG_SUCCESS:
                    LogHelper.d("\n    ********** NetworkRunnable Success Start *********** " + "\n"
                            + "    REQUEST_HEADER : " + mNetworkData.getRequestHeaderData() + "\n"
                            + "    RESPONSE_HEADER : " + mNetworkData.getResponseHeaderData() + "\n"
                            + "    URL : " + mNetworkData.getUrl() + "\n"
                            + "    REQUEST PARAM : " + mNetworkData.getRequestParams() + "\n"
                            + "    RESULT JSON : " + mNetworkData.getResult() + "\n"
                            + "    ********** NetworkRunnable Success End *********** ");
                    mNetworkListener.onSuccess((NetworkData) msg.obj);

                    break;
            }

        }

    }
}
