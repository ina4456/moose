package com.insoline.pnd.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.insoline.pnd.config.ConfigLoader;
import com.insoline.pnd.define.PreferenceDefine;
import com.insoline.pnd.model.WaitingCall;
import com.insoline.pnd.model.WaitingZone;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallOrderPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseMessagePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseNoticePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseWaitCallInfoPacket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Preference를 사용하기 위하여 선언 해둔 곳.
 */
@SuppressLint("CommitPrefEdits")
public class PreferenceUtil {
    private static final String TAG = PreferenceUtil.class.getName();

    private SharedPreferences preference = null;
    private SharedPreferences.Editor editor = null;

    public PreferenceUtil(Context context) {
        this.preference = context.getSharedPreferences(context.getPackageName() + "." + PreferenceDefine.PREFERENCE_KEY, Context.MODE_PRIVATE);
        this.editor = preference.edit();
    }

    public boolean getBoolean(String name) {
        return preference.getBoolean(name, false);
    }

    public boolean getBoolean(String name, boolean defValue) {
        return preference.getBoolean(name, defValue);
    }

    public String getString(String name) {
        return preference.getString(name, "");
    }

    public String getString(String name, String defValue) {
        return preference.getString(name, defValue);
    }

    public ArrayList<String> getStringList(String name) {
        String json = getString(name, null);
        ArrayList<String> list = null;
        if (json != null) {
            try {
                list = new ArrayList<String>();
                JSONArray jArr = new JSONArray(json);
                for (int i = 0; i < jArr.length(); i++) {
                    String item = jArr.optString(i);
                    list.add(item);
                }
            } catch (JSONException e) {
                LogHelper.e(e.getMessage());
            }
        }
        return list;
    }

    public ArrayList<HashMap<String, String>> getHashMapList(String name) {
        String json = getString(name, null);
        ArrayList<HashMap<String, String>> list = null;
        if (json != null) {
            try {
                list = new ArrayList<HashMap<String, String>>();
                JSONArray jArr = new JSONArray(json);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject item = jArr.optJSONObject(i);
                    Iterator<String> iter = item.keys();
                    HashMap<String, String> data = new HashMap<String, String>();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        data.put(key, item.optString(key));
                    }

                    list.add(data);
                }
            } catch (JSONException e) {
                LogHelper.e(e.getMessage());
            }
        }
        return list;
    }

    public HashMap<String, String> getHashMap(String name) {
        String json = getString(name, null);
        HashMap<String, String> data = null;
        if (json != null) {
            try {
                data = new HashMap<String, String>();
                JSONObject obj = new JSONObject(json);
                Iterator<String> iter = obj.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    data.put(key, obj.optString(key));
                }
            } catch (JSONException e) {
                LogHelper.e(e.getMessage());
            }
        }
        return data;
    }

    public int getInt(String name) {
        return preference.getInt(name, 0);
    }

    public int getInt(String name, int defValue) {
        return preference.getInt(name, defValue);
    }

    public long getLong(String name) {
        return preference.getLong(name, 0l);
    }

    public long getLong(String name, long defValue) {
        return preference.getLong(name, defValue);
    }

    public float getFloat(String name) {
        return preference.getFloat(name, 0.0f);
    }

    public float getFloat(String name, float defValue) {
        return preference.getFloat(name, defValue);
    }

    public void setString(String name, String value) {
        editor.putString(name, value);
        editor.commit();
    }

    public void setStringList(String name, ArrayList<String> value) {
        JSONArray jArr = new JSONArray();
        for (int i = 0; i < value.size(); i++) {
            jArr.put(value.get(i));
        }
        if (!value.isEmpty()) {
            editor.putString(name, jArr.toString());
        } else {
            editor.putString(name, null);
        }
        editor.commit();
    }

    public void setHashMapList(String name, ArrayList<HashMap<String, String>> value) {
        JSONArray jArr = new JSONArray();
        if (value != null) {
            for (int i = 0; i < value.size(); i++) {
                JSONObject obj = new JSONObject();
                for (String key : value.get(i).keySet()) {
                    try {
                        obj.put(key, value.get(i).get(key));
                    } catch (JSONException e) {
                        LogHelper.e(e.getMessage());
                    }
                }
                jArr.put(obj);
            }
        }
        if (value != null && !value.isEmpty()) {
            editor.putString(name, jArr.toString());
        } else {
            editor.putString(name, null);
        }
        editor.commit();
    }

    public void setHashMap(String name, HashMap<String, String> value) {
        JSONObject obj = new JSONObject();
        if (value != null) {
            for (String key : value.keySet()) {
                try {
                    obj.put(key, value.get(key));
                } catch (JSONException e) {
                    LogHelper.e(e.getMessage());
                }
            }
        }

        if (value != null && !value.isEmpty()) {
            editor.putString(name, obj.toString());
        } else {
            editor.putString(name, null);
        }
        editor.commit();
    }

    public void setBoolean(String name, boolean value) {
        editor.putBoolean(name, value);
        editor.commit();
    }

    public void setInt(String name, int value) {
        editor.putInt(name, value);
        editor.commit();
    }

    public void setLong(String name, long value) {
        editor.putLong(name, value);
        editor.commit();
    }

    public void setFloat(String name, float value) {
        editor.putFloat(name, value);
        editor.commit();
    }

    public void remove(String name) {
        editor.remove(name);
        editor.commit();
    }

    public void clear() {
        editor.clear();
    }

    private <T extends  Object> T getDataAsJson(String key, Class<T> type) {
        Gson gson = new Gson();
        String json = getString(key, null);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, (Type)type);
    }


    // ===================================================================

    /**
     * 환경 설정 저장
     */
    public void setConfiguration(String config) {
        LogHelper.write("setConfiguration : " + config);
        setString(PreferenceDefine.PREF_CONFIG, config);
    }

    /**
     * 환경 설정 가져오기
     */
    public ConfigLoader.Config getConfiguration() {
        return getDataAsJson(PreferenceDefine.PREF_CONFIG, ConfigLoader.Config.class);
    }

    /**
     * 임시배차 저장
     */
    public void setCallInfoTemp(ResponseCallOrderPacket callInfo) {
        LogHelper.write("setCallInfoTemp : " + callInfo);
        setString(PreferenceDefine.PREF_CALL_INFO_TEMP, new Gson().toJson(callInfo));
    }

    /**
     * 임시배차 가져오기
     */
    public ResponseCallOrderPacket getCallInfoTemp() {
        return getDataAsJson(PreferenceDefine.PREF_CALL_INFO_TEMP, ResponseCallOrderPacket.class);
    }

    /**
     * 임시배차 삭제
     */
    public void clearCallInfoTemp() {
        LogHelper.write("clearCallInfoTemp ");
        remove(PreferenceDefine.PREF_CALL_INFO_TEMP);
    }

    /**
     * 일반배차 저장
     */
    public void setCallInfoNormal(ResponseCallOrderPacket callInfo) {
        LogHelper.write("setCallInfoNormal : " + callInfo);
        setString(PreferenceDefine.PREF_CALL_INFO_NORMAL, new Gson().toJson(callInfo));
    }

    /**
     * 일반배차 가져오기
     */
    public ResponseCallOrderPacket getCallInfoNormal() {
        return getDataAsJson(PreferenceDefine.PREF_CALL_INFO_NORMAL, ResponseCallOrderPacket.class);
    }

    /**
     * 일반배차 삭제
     */
    public void clearCallInfoNormal() {
        LogHelper.write("clearCallInfoNormal ");
        remove(PreferenceDefine.PREF_CALL_INFO_NORMAL);
    }

    /**
     * 승차중배차 저장
     */
    public void setCallInfoGeton(ResponseCallOrderPacket callInfo) {
        LogHelper.write("setCallInfoGeton : " + callInfo);
        setString(PreferenceDefine.PREF_CALL_INFO_GETON, new Gson().toJson(callInfo));
    }

    /**
     * 승차중배차 가져오기
     */
    public ResponseCallOrderPacket getCallInfoGeton() {
        return getDataAsJson(PreferenceDefine.PREF_CALL_INFO_GETON, ResponseCallOrderPacket.class);
    }

    /**
     * 승차중배차 삭제
     */
    public void clearCallInfoGeton() {
        LogHelper.write("clearCallInfoGeton ");
        remove(PreferenceDefine.PREF_CALL_INFO_GETON);
    }

    /**
     * 대기배차 저장
     */
    public void setCallInfoWait(ResponseWaitCallInfoPacket callInfo) {
        LogHelper.write("setCallInfoWait : " + callInfo);
        setString(PreferenceDefine.PREF_CALL_INFO_WAIT, new Gson().toJson(callInfo));
    }

    /**
     * 대기배차 가져오기
     */
    public ResponseWaitCallInfoPacket getCallInfoWait() {
        return getDataAsJson(PreferenceDefine.PREF_CALL_INFO_WAIT, ResponseWaitCallInfoPacket.class);
    }

    /**
     * 대기배차 삭제
     */
    public void clearCallInfoWait() {
        LogHelper.write("clearCallInfoWait ");
        remove(PreferenceDefine.PREF_CALL_INFO_WAIT);
    }

    /**
     * 대기상태 저장
     */
    public void setWaitArea(String callInfo) {
        LogHelper.write("setWaitArea : " + callInfo);
        setString(PreferenceDefine.PREF_WAIT_AREA, callInfo);
    }

    /**
     * 대기상태 가져오기
     */
    public WaitingZone getWaitArea() {
        return getDataAsJson(PreferenceDefine.PREF_WAIT_AREA, WaitingZone.class);
    }

    /**
     * 대기상태 삭제
     */
    public void clearWaitArea() {
        LogHelper.write("clearWaitArea ");
        remove(PreferenceDefine.PREF_WAIT_AREA);
    }

}
