package com.insoline.pnd.common;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.insoline.pnd.IntroActivity;
import com.insoline.pnd.R;
import com.insoline.pnd.config.ConfigLoader;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.define.AppDefine;
import com.insoline.pnd.define.NetworkUrlDefine;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.network.NetworkData;
import com.insoline.pnd.network.NetworkLoader;
import com.insoline.pnd.network.listener.NetworkRunListener;
import com.insoline.pnd.service.FloatingViewService;
import com.insoline.pnd.service.ScenarioService;
import com.insoline.pnd.utils.INaviExecutor;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.PreferenceUtil;
import com.insoline.pnd.utils.StringUtil;
import com.insoline.pnd.view.ConfigActivity;
import com.insoline.pnd.view.LoginActivity;
import com.insoline.pnd.view.fragment.PopupDialogFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener, PopupDialogFragment.PopupDialogListener {

    private final int uiOptionFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    private PreferenceUtil mPreferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.e("BaseActivity : onCreate()");

        if (this instanceof IntroActivity || this instanceof LoginActivity || this instanceof ConfigActivity) {
            getWindow().getDecorView().setSystemUiVisibility(uiOptionFlags);

            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(uiOptionFlags);
                    }
                }
            });
        }

        mPreferenceUtil = new PreferenceUtil(this);
    }

    public BaseApplication getBaseApplication() {
        return (BaseApplication) this.getApplicationContext();
    }

    public ConfigLoader getConfigLoader() {
        return getBaseApplication().getConfigLoader();
    }

    public ScenarioService getScenarioService() {
        return getBaseApplication().getScenarioService();
    }

    public PreferenceUtil getPreferenceUtil() {
        return mPreferenceUtil;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LogHelper.d("onWindowFocusChanged : ");

        if (this instanceof IntroActivity || this instanceof LoginActivity || this instanceof ConfigActivity) {
            getWindow().getDecorView().setSystemUiVisibility(uiOptionFlags);
        }
    }

    @Override
    protected void onStart() {
        LogHelper.e("onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogHelper.e("onResume()");
        super.onResume();

        this.setCurrentActivity(this);
        unbindFloatingStatusService();
    }

    @Override
    protected void onDestroy() {
        LogHelper.e("onDestroy()");
        this.setCurrentActivity(null);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LogHelper.e("onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogHelper.e("onStop()");
        super.onStop();

        if (getBaseApplication().isBackground()) {
            Activity currentActivity = getBaseApplication().getCurrentActivity();
            if (!(currentActivity instanceof LoginActivity) && !(currentActivity instanceof ConfigActivity)) {
                bindFloatingStatusService();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogHelper.e("onNewIntent()");
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
    }

    //현재 activity 저장
    public void setCurrentActivity(Activity activity) {
        getBaseApplication().setCurrentActivity(activity);
    }

    public void finishActivity() {
        boolean wasBackground = getBaseApplication().wasBackground();
        if (wasBackground) {
            moveTaskToBack(true);
            finish();
        } else {
            if (!isFinishing()) {
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    //BackButton Listener 콜백
    private BackButtonPressedListener mBackButtonPressedListener;

    public interface BackButtonPressedListener {
        void onBackButtonPressed();
    }

    public void setBackButtonPressedListener(BackButtonPressedListener listener) {
        mBackButtonPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mBackButtonPressedListener != null) {
            mBackButtonPressedListener.onBackButtonPressed();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 로그아웃 처리 - 설정 변경시 앱 재실행
     */
    public void logout() {
        LogHelper.i("logout()");
        setPropertyViaServiceX(Constants.SP_VAL_LOGIN_STATUS_LOGOUT);
        getConfigLoader().setAutoLogin(false);
        getBaseApplication().resetServiceAndRestartApplication();
    }

    public void setPropertyViaServiceX(String value) {
        LogHelper.i("setPropertyViaServiceX()");
        Intent adbIntent = new Intent(AppDefine.PKG_NAME_SERVICEX_ADB_COMMAND);
        adbIntent.putExtra(Constants.INTENT_KEY_ADB_COMMAND, "setprop " + Constants.SP_KEY_LOGIN_STATUS + " " + value);
        getApplication().sendBroadcast(adbIntent);
    }

    /**
     * FloatingService Binding..
     */
    public void bindFloatingStatusService() {
        LogHelper.d(">> bindFloatingStatusService()");
        Intent intent = new Intent(getApplicationContext(), FloatingViewService.class);
        this.startService(intent);
    }

    public void unbindFloatingStatusService() {
        LogHelper.d(">> unbindFloatingStastusService()");
        this.stopService(new Intent(getApplicationContext(), FloatingViewService.class));
    }

    // FloatingStatus service
    private FloatingViewService mFloatingViewService;
    private ServiceConnection floatingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LogHelper.e(">> ServiceConnected : FloatingViewService");
            FloatingViewService.LocalBinder binder = (FloatingViewService.LocalBinder) service;
            mFloatingViewService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogHelper.e(">> onServiceDisconnected : FloatingViewService");
            mFloatingViewService = null;
        }
    };

    /**
     * 로딩바
     */
    public void startLoadingProgress() {
        getBaseApplication().progressOn(this);
    }

    public void startLoadingProgress(String msg) {
        getBaseApplication().progressOn(this, msg);
    }

    public void finishLoadingProgress() {
        getBaseApplication().progressOff();
    }

    //준비중 팝업
    public void showPopupPreparing() {
        Popup popup = new Popup
                .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_CONFIG_INVALID)
                .setTitle(getString(R.string.popup_title_alert))
                .setContent("준비중 ")
                .build();
        showPopupDialog(popup);
    }

    /**
     * 팝업 화면
     *
     * @param popup
     */
    public void showPopupDialog(Popup popup) {
        if (!isFinishing()) {
            PopupDialogFragment dialogFragment = PopupDialogFragment.newInstance(popup);
            FragmentManager fragmentManager = getSupportFragmentManager();

            PopupDialogFragment popupDialogFragment = (PopupDialogFragment) fragmentManager.findFragmentByTag(popup.getTag());
            if (popupDialogFragment != null) {
                fragmentManager.beginTransaction()
                        .remove(popupDialogFragment)
                        .commit();
            }

            fragmentManager.beginTransaction()
                    .add(dialogFragment, popup.getTag())
                    .commitAllowingStateLoss();
        }
    }

    // 전화 걸기 (call app 에 전달)
    public void callToPhone(String phoneNumber) {
        LogHelper.e("callToPhone : " + phoneNumber);
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            LogHelper.e("전화 번호 오류");
        }
    }

    public void sendBroadcast(String intentAction, String key, String value) {
        Intent intent = new Intent(intentAction);
        intent.putExtra(key, value);
        sendBroadcast(intent);
    }


    /**
     * 키패드 보이기
     */
    public void showInputKeyboard() {
        showInputKeyboard(this.getCurrentFocus());
    }

    public void showInputKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		if (imm != null && this.getCurrentFocus() != null) {
//			imm.showSoftInput(v, 0);
//		}
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 키패드 숨김
     */
    public void hideInputKeyboard() {
        hideInputKeyboard(this.getCurrentFocus());
    }

    public void hideInputKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && this.getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * @param url      url
     * @param params   params
     * @param listener listener
     */
    public void sendNetworkRequest(NetworkUrlDefine url, JSONObject params, NetworkRunListener listener) throws RuntimeException {
        this.sendNetworkRequest(url, params, listener, false);
    }

    /**
     * @param url      url
     * @param params   params
     * @param listener listener
     */
    public void sendNetworkRequest(NetworkUrlDefine url, JSONObject params, NetworkRunListener listener, boolean selfErrorControl) throws RuntimeException {
        if (url == null)
            throw new NullPointerException("URL 정보가 셋팅되지 않음");
        NetworkData data = new NetworkData();
        data.setContext(this);
        data.setNetworkUrl(url);
        data.setRequestParams(params);
        data.setErrorHandleType(selfErrorControl ? NetworkData.ErrorHandleType.SELF : NetworkData.ErrorHandleType.COMMON);
//        switch (url) {
//            case LOGIN:
//                data.setConnectionTimeOut(20 * 1000);
//                data.setReadTimeOut(20 * 1000);
//                break;
//        }

        this.sendNetworkRequest(data, listener);
    }

    /**
     * NetworkRequest General
     * <p>
     * 일반 전문 통신을 요청하기 위한 Request
     * </p>
     *
     * @param data     ( NetworkData )
     * @param listener ( NetworkRunListener )
     */
    public void sendNetworkRequest(NetworkData data, NetworkRunListener listener) throws RuntimeException {
        if (data == null) {
            throw new NullPointerException("DATA가 생성 되지 않음 (생성자 x)");
        }
        if (data.getContext() == null) {
            data.setContext(this);
        }

        if (data.getNetworkUrl() == null && StringUtil.isEmpty(data.getStringUrl())) {
            throw new NullPointerException("URL 정보가 셋팅되지 않음");
        }
        data.setNetworkListener(listener);
        NetworkLoader.getInstance().sendRequest(data);
    }


    /**
     * Permission check
     */
    // 필수 권한
    protected static final String[] permissionsEssential = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE    // 외부 저장소 읽기 허용
            , Manifest.permission.WRITE_EXTERNAL_STORAGE // 외부저장소 쓰기 허용
            , Manifest.permission.ACCESS_FINE_LOCATION   // 정확한 위치에 엑세스 할 수 있도록 허용
            , Manifest.permission.ACCESS_COARSE_LOCATION // 대략적 위치에 엑세스 할 수 있도록 하용
    };
    public static final int REQUEST_PERMISSION_ESSENTIAL = 9999;
    public static final int REQUEST_PERMISSION_OVERLAY = 9998;

    protected boolean isPermission(String[] permissions) {
        LogHelper.e("CHECK PERMISSIONS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> denied_permissions = new ArrayList<>();
            for (String perm : permissions) {
                if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    protected boolean checkPermission(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<String> denied_permissions = new ArrayList<>();
            for (String perm : permissions) {
                if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                    denied_permissions.add(perm);
            }

            if (denied_permissions.size() > 0) {
                String[] deniedPerms = denied_permissions.toArray(new String[0]);
                ActivityCompat.requestPermissions(this, deniedPerms, requestCode);
                return false;
            }
        }
        return true;
    }
}
