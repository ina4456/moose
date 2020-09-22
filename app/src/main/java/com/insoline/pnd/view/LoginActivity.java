package com.insoline.pnd.view;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.define.AppDefine;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.remote.manager.NetworkManager;
import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.StringUtil;
import com.insoline.pnd.utils.WavResourcePlayer;
import com.insoline.pnd.view.custom.LetterSpacingTextView;
import com.insoline.pnd.view.custom.NumberPadView;

public class LoginActivity extends BaseActivity implements TextWatcher {

    private Button loginBtnCallCenter, loginBtnSetting, loginBtnLogin;
    private LetterSpacingTextView loginTvPhoneNumber;
    private NumberPadView loginNumberPadView;
    private TextView tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.e("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtnCallCenter = findViewById(R.id.login_btn_call_center);
		loginBtnSetting = findViewById(R.id.login_btn_setting);
        loginBtnLogin = findViewById(R.id.login_btn_login);
        loginTvPhoneNumber = findViewById(R.id.login_tv_phone_number);
        loginNumberPadView = findViewById(R.id.login_number_pad_view);
        tvAppVersion = findViewById(R.id.tv_app_version);

        loginBtnCallCenter.setOnClickListener(this);
        loginBtnSetting.setOnClickListener(this);
        loginBtnLogin.setOnClickListener(this);

        loginTvPhoneNumber.setLetterSpacing(10);
        loginTvPhoneNumber.addTextChangedListener(this);
        loginNumberPadView.setTextView(loginTvPhoneNumber);

        tvAppVersion.setText(getString(R.string.app_version, getConfigLoader().getAppVersion()));

        //휴대폰번호 체크
        if (!getConfigLoader().isCorporation() && !StringUtil.isEmptyString(getConfigLoader().getDriverPhoneNumber())) {
            loginTvPhoneNumber.setText(StringUtil.phoneNumberForm(getConfigLoader().getDriverPhoneNumber()));
        }
    }

    @Override
    protected void onResume() {
        LogHelper.e("onResume()");
        super.onResume();

        //환경설정 체크
        if (hasConfiguration()) {
            //자동로그인 체크 (개인 and 자동로그인) isCorporation:개인/법인 여부 개인(false),법인(true) // isAutoLogin : 자동로그인 유무(true:개인, false:법인, 로그아웃한 개인)
            Log.d("로그인", getConfigLoader().isCorporation()+", "+getConfigLoader().isAutoLogin());

            if (!getConfigLoader().isCorporation() && getConfigLoader().isAutoLogin()) {
                Log.d("로그인", "2");
                String strPhoneNum = loginTvPhoneNumber.getText().toString();

                if (!StringUtil.isEmptyString(strPhoneNum)) {
                    goLogin();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        LogHelper.e("onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogHelper.e("onNewIntent()");
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        LogHelper.e("onBackPressed()");
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.login_btn_call_center: //콜센터 전화
//                callToPhone("");
                break;

            case R.id.login_btn_setting:    //설정화면
                checkPermission();
                break;

            case R.id.login_btn_login:  //인증하기
                goLogin();
                break;

            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString().replaceAll("-", "");
        text = text.replaceAll("\\s", "");
        boolean isEnabled = false;
        if (text.length() >= 10) {
            isEnabled = true;
        }
        loginBtnLogin.setEnabled(isEnabled);
    }

    /**
     * 환경 설정 파일이 존재하는지 체크 한다.
     */
    public boolean hasConfiguration() {
        Log.d("로그인 Hasconfiguration : ", getConfigLoader().hasConfiguration()+"");
        if (!getConfigLoader().hasConfiguration()) {
            Log.d("로그인", " : "+Popup.TYPE_TWO_BTN_EDIT_TEXT +" , "+ Constants.DIALOG_TAG_CONFIG_ADMIN);
            goConfigActivity();
            return false;
        } else {
            return true;
        }
    }

    //로그인
    private void goLogin() {
        super.startLoadingProgress();

        Log.d("고 로그인", "hasConfiguration() : "+hasConfiguration());//true
        //환경설정 체크
        if (hasConfiguration()) {

            //네트워크 체크 -> 모뎀전화번호 체크 -> 인증요청
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean isNetwork = false;
                    for (int i = 0; i < 5; i++) {
                        if (NetworkManager.getInstance().isAvailableNetwork(LoginActivity.this)) {
                            isNetwork = true;

                            //모뎀전화번호 체크
                            if (StringUtil.isEmptyString(getConfigLoader().getModemNumber())) {
                                getBaseApplication().requestModemNumber();
                            }

                            //인증요청 (1111->1112)
                            getScenarioService().requestService(loginTvPhoneNumber.getText().toString(), true);
                            break;
                        }

                        try {
                            Thread.sleep(5 * 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //네트워크 오류
                    if (!isNetwork) {
                        Log.d("고 로그인", "네트워크 오류");
                        LoginActivity.super.finishLoadingProgress();
                        WavResourcePlayer.getInstance(LoginActivity.this).play(R.raw.voice_103);
                        Popup popup = new Popup
                                .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_LOGIN_ERROR)
                                .setTitle(getString(R.string.fail_cert))
                                .setContent(getString(R.string.popup_msg_login_failed_network_disconnected))
                                .build();
                        showPopupDialog(popup);
                    }

                }
            }, 100);
        }
    }

    //환경 설정 화면이동
    private void goConfigActivity() {
        Log.d("로그인", " : "+Popup.TYPE_TWO_BTN_EDIT_TEXT +" , "+ Constants.DIALOG_TAG_CONFIG_ADMIN);
        Popup popup = new Popup
                .Builder(Popup.TYPE_TWO_BTN_EDIT_TEXT, Constants.DIALOG_TAG_CONFIG_ADMIN)
                .setTitle(getString(R.string.configuration_admin_password))
                .setIsHiddenStatusBar(true)
                .setIsSecureTextType(true)
                .build();
        showPopupDialog(popup);
    }

    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
        if (tag.equals(Constants.DIALOG_TAG_CONFIG_ADMIN)) {
            hideInputKeyboard();

            if (intent != null) {
                String inputValue = intent.getStringExtra(Constants.DIALOG_INTENT_KEY_EDIT_TEXT);
                if (inputValue != null) {
                    inputValue = inputValue.trim();

                    String password = getConfigLoader().getPassword();
                    if (password.equals(inputValue)) {
                        Intent intentConfig = new Intent(this, ConfigActivity.class);
                        intentConfig.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivityForResult(intentConfig, ConfigActivity.REQUEST_CODE_CONFIG);
                    }
                }
            }
        }
    }

    /**
     * ACTION_MANAGE_OVERLAY_PERMISSION
     */
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_PERMISSION_OVERLAY);
            } else {
                goConfigActivity();
            }
        } else {
            goConfigActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogHelper.d(">> requestCode : " + requestCode + ", resultCode : " + resultCode + ", data : " + data);

        switch (requestCode) {
            case ConfigActivity.REQUEST_PERMISSION_OVERLAY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        // TODO 동의를 얻지 못했을 경우의 처리
                        Popup popup = new Popup
                                .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_CONFIG_INVALID)
                                .setTitle(getString(R.string.popup_title_alert))
                                .setContent("권한 동의 필수")
                                .build();
                        showPopupDialog(popup);
                    } else {
                        goConfigActivity();
                    }
                }
                break;
            case ConfigActivity.REQUEST_CODE_CONFIG:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }



    /**
     * 인증결과 처리
     * @param result
     * @param certCode
     */
    public void applyCertificationResult(Packets.CertificationResult result, int certCode) {
        Log.d("차량인증", ">>Cert Result = " + result + ", " + certCode);
        super.finishLoadingProgress();

        if (result != Packets.CertificationResult.Success) {
            String message = getString(R.string.fail_cert);

            switch (result) {
                case InvalidCar:
                    message = getString(R.string.fail_car) + " (0x" + Integer.toHexString(result.value) + ")";
                    WavResourcePlayer.getInstance(this).play(R.raw.voice_105);
                    break;

                case InvalidContact:
                    message = getString(R.string.fail_phone_number) + " (0x" + Integer.toHexString(result.value) + ")";
                    WavResourcePlayer.getInstance(this).play(R.raw.voice_104);
                    break;

                case DriverPenalty:
                    message = getString(R.string.fail_panelty) + " (0x" + Integer.toHexString(result.value) + ")";
                    WavResourcePlayer.getInstance(this).play(R.raw.voice_103);
                    break;

                case InvalidHoliday:
                    message = getString(R.string.fail_vacation) + " (0x" + Integer.toHexString(result.value) + ")";
                    WavResourcePlayer.getInstance(this).play(R.raw.voice_103);
                    break;

                default:
                    message += "(0x" + Integer.toHexString(certCode) + ")";
                    WavResourcePlayer.getInstance(this).play(R.raw.voice_103);
                    break;
            }

            Popup popup = new Popup
                    .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_LOGIN_ERROR)
                    .setTitle(getString(R.string.fail_cert))
                    .setContent(message)
                    .build();
            showPopupDialog(popup);

        } else {
            // 인증 완료 되면 전화번호를 저장 한다.
            String phoneNum = loginTvPhoneNumber.getText().toString();
            phoneNum = phoneNum.replaceAll("-", "");
            getConfigLoader().setDriverPhoneNumber(phoneNum);

            //자동로그인 체크 (개인인 경우)
            if (!getConfigLoader().isCorporation()) {
                getConfigLoader().setAutoLogin(true);
            } else {
                getConfigLoader().setAutoLogin(false);
            }
            getConfigLoader().save();

            setOtherThingsAfterLogin(phoneNum);
            WavResourcePlayer.getInstance(this).play(R.raw.voice_102);

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            bindFloatingStatusService();
            finish();
        }
    }

    /**
     * 전화연결 앱 등에 로그인 정보를 알리기 위해 ServiceX를 통한 property 설정
     * @param phoneNumber
     */
    public void setOtherThingsAfterLogin(String phoneNumber) {
        LogHelper.e("setOtherThingsAfterLogin phoneNumber : " + phoneNumber);
        sendBroadcast(AppDefine.PKG_NAME_SERVICEX_ADB_COMMAND
                , Constants.INTENT_KEY_ADB_COMMAND
                , "setprop " + Constants.SP_KEY_DRIVER_PHONE_NUMBER + " " + phoneNumber);
        sendBroadcast(AppDefine.PKG_NAME_SERVICEX_ADB_COMMAND
                , Constants.INTENT_KEY_ADB_COMMAND
                , "setprop " + Constants.SP_KEY_LOGIN_STATUS + " " + Constants.SP_VAL_LOGIN_STATUS_LOGIN);
    }
}
