package com.insoline.pnd.network;

import android.content.Context;

import com.insoline.pnd.define.NetworkUrlDefine;
import com.insoline.pnd.network.listener.NetworkRunListener;
import com.insoline.pnd.utils.StringUtil;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class NetworkData {

    public enum ErrorHandleType {
        COMMON, SELF
    }

    public enum ErrorType {
        DATA, OTHERS
    }

    protected Context context;

    protected NetworkUrlDefine networkUrl;

    private String url;

    protected String requestMethod;
    protected Map<String, String> requestHeaderData;
    protected Map<String, List<String>> responseHeaderData;

    protected JSONObject requestParams;

    protected JSONObject result;
    protected String resultHTML;

    protected int readTimeOut = -1;
    protected int connectionTimeOut = -1;

    protected ErrorHandleType errorHandleType = ErrorHandleType.COMMON;

    protected String ErrorCode;
    protected String ErrorMessage;

    protected NetworkRunListener networkListener;

    public NetworkData() {
    }

    public void setContext(Context context){
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public NetworkUrlDefine getNetworkUrl() {
        return networkUrl;
    }

    public void setNetworkUrl(NetworkUrlDefine networkUrl) {
        this.networkUrl = networkUrl;
    }

    public void setStringUrl(String url) {
        this.url = url;
    }

    public String getStringUrl() {
        return url;
    }

    public String getUrl() {
        if (networkUrl != null) {
            return networkUrl.getUrl();
        } else if (!StringUtil.isEmpty(url)) {
            return url;
        }
        return "";
    }

    public String getRequestMethod() {
    	return requestMethod;
    }

    public void setRequestMethod(String method) {
    	this.requestMethod = method;
    }


    public Map<String, String> getRequestHeaderData() {
        return requestHeaderData;
    }

    public void setRequestHeaderData(Map<String, String> requestHeaderData) {
        this.requestHeaderData = requestHeaderData;
    }

    public Map<String, List<String>> getResponseHeaderData() {
        return responseHeaderData;
    }

    public void setResponseHeaderData(Map<String, List<String>> responseHeaderData) {
        this.responseHeaderData = responseHeaderData;
    }

    public JSONObject getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(JSONObject requestParams) {
        this.requestParams = requestParams;
    }

    public JSONObject getResult() {
        return result;
    }

    public void setResult(JSONObject result) {
        this.result = result;
    }

    public String getResultHTML() {
        return resultHTML;
    }

    public void setResultHTML(String resultHTML) {
        this.resultHTML = resultHTML;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public ErrorHandleType getErrorHandleType() {
        return errorHandleType;
    }

    public void setErrorHandleType(ErrorHandleType errorHandleType) {
        this.errorHandleType = errorHandleType;
    }

    public String getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(String errorCode) {
        ErrorCode = errorCode;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }


    public NetworkRunListener getNetworkListener() {
        return networkListener;
    }

    public void setNetworkListener(NetworkRunListener networkListener) {
        this.networkListener = networkListener;
    }

    public boolean isValid() {
        if (context != null && (networkUrl != null || !StringUtil.isEmpty(url)) )
            return true;
        return false;
    }
}
