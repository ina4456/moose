package com.insoline.pnd;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.remote.manager.NetworkManager;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.LoginActivity;
import com.insoline.pnd.view.MainActivity;

import java.util.Arrays;

public class IntroActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogHelper.i("onResume()");

        //퍼미션 체크
        checkPermission();
    }

    //네트워크 연결 상태 체크 (모뎀 부팅 체크)
    private void checkNetwork() {
        super.startLoadingProgress();

        getConfigLoader().setModemNumber("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    if (NetworkManager.getInstance().isAvailableNetwork(IntroActivity.this)) {
                        getBaseApplication().requestModemNumber();
                        break;
                    }

                    try {
                        Thread.sleep(5 * 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                goActivity();

            }
        }, 100);
    }

    private void goActivity() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (getScenarioService() != null && getScenarioService().hasCertification()) {
                    Activity lastActivity = getBaseApplication().getCurrentActivity();
                    LogHelper.i("lastActivity : " + lastActivity);

                    if (lastActivity != null && !(lastActivity instanceof IntroActivity)) {
                        intent = new Intent(IntroActivity.this, lastActivity.getClass());
                    } else {
                        intent = new Intent(IntroActivity.this, MainActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                } else {
                    intent = new Intent(IntroActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }

                actFinish(intent);
            }
        }, 100);
    }

    private void actFinish(Intent intent) {
        super.finishLoadingProgress();
        startActivity(intent);
        finish();
    }


    /**
     * Permission check
     */
    public void checkPermission() {
        if (!isPermission(permissionsEssential)) {
            checkPermission(permissionsEssential, REQUEST_PERMISSION_ESSENTIAL);
        } else {
            checkNetwork();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LogHelper.e("onRequestPermissionsResult : " + Arrays.toString(permissions) + " // " + Arrays.toString(grantResults));
        switch (requestCode) {
            case REQUEST_PERMISSION_ESSENTIAL:
                boolean isGranted = true;
                for (int granted : grantResults) {
                    if (granted != PackageManager.PERMISSION_GRANTED) {
                        isGranted = false;
                    }
                }

                if (!isGranted) {
                    Popup popup = new Popup
                            .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_PERMISSION_ERROR)
                            .setTitle(getString(R.string.fail_permissions))
                            .setContent(getString(R.string.popup_msg_permissions_error))
                            .build();
                    showPopupDialog(popup);
                } else {
                    checkNetwork();
                }
                break;
        }
    }

    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
        if (tag.equals(Constants.DIALOG_TAG_PERMISSION_ERROR)) {
            if (intent != null) {
                boolean isConfirm = intent.getBooleanExtra(Constants.DIALOG_INTENT_KEY_PRESSED_POSITIVE_BTN, false);
                if (isConfirm) {
                    Intent intent2 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                    finish();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        }
    }
}
