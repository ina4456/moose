package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.utils.LogHelper;

public class MenuActivity extends BaseActivity {

    private ImageButton btnSetting, btnClose;
    private Button btnMyInfo, btnLogout;
    private TextView tvDriverName, tvVehicleNumber;
    private LinearLayout llBtnNotice, llBtnMsg, llBtnHistory, llBtnMyInfo, llBtnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnSetting = findViewById(R.id.btn_setting);
        btnClose = findViewById(R.id.btn_close);
        btnLogout = findViewById(R.id.btn_logout);
        btnMyInfo = findViewById(R.id.btn_my_info);

        tvDriverName = findViewById(R.id.tv_driver_name);
        tvVehicleNumber = findViewById(R.id.tv_vehicle_number);

        llBtnNotice = findViewById(R.id.ll_btn_notice);
        llBtnMsg = findViewById(R.id.ll_btn_msg);
        llBtnHistory = findViewById(R.id.ll_btn_history);
//        llBtnMyInfo = findViewById(R.id.ll_btn_my_info);
//        llBtnSetting = findViewById(R.id.ll_btn_setting);

        btnSetting.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnMyInfo.setOnClickListener(this);

        llBtnNotice.setOnClickListener(this);
        llBtnMsg.setOnClickListener(this);
        llBtnHistory.setOnClickListener(this);
        llBtnMyInfo.setOnClickListener(this);
        llBtnSetting.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        LogHelper.e("onResume()");
        super.onResume();
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

        Intent intent;
        switch (view.getId()) {
            case R.id.btn_close:
                finish();
                break;

            case R.id.btn_logout:   //로그아웃
                logout();
                finish();
                break;

            case R.id.btn_my_info:  //내정보
//            case R.id.ll_btn_my_info:
//                intent = new Intent(this, MyInfoActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);
//                break;

            case R.id.ll_btn_notice:    //공지사항
//                intent = new Intent(this, NoticeActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                intent.putExtra(NoticeActivity.EXTRA_IS_NOTICE, true);
//                startActivity(intent);
//                break;

            case R.id.ll_btn_msg:   //콜센터메시지
//                intent = new Intent(this, NoticeActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                intent.putExtra(NoticeActivity.EXTRA_IS_NOTICE, false);
//                startActivity(intent);
//                break;

            case R.id.ll_btn_history:   //운행이력
//                intent = new Intent(this, OperationHistoryActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);
                showPopupPreparing();
                break;

//            case R.id.ll_btn_setting:   //환경설정
            case R.id.btn_setting:
                goConfigActivity();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogHelper.d(">> requestCode : " + requestCode + ", resultCode : " + resultCode + ", data : " + data);

        switch (requestCode) {
            case ConfigActivity.REQUEST_CODE_CONFIG:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    //환경 설정 화면이동
    private void goConfigActivity() {
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
}
