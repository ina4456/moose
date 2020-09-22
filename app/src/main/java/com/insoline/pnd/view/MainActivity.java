package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.model.WaitingCall;
import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.WavResourcePlayer;

public class MainActivity extends BaseActivity {

    private Button btnMenu, btnLogout;
    private TextView tvCarNum;
    private Button btnResting, btnReceivingCall;
    private Button btnWaitingZone, btnCallList;

    private DrawerLayout drawerLayout;
    private TextView tvStatus, tvStatusSub;

    private boolean isRest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        btnMenu = findViewById(R.id.btn_menu);
        tvCarNum = findViewById(R.id.tv_car_num);
        btnLogout = findViewById(R.id.btn_logout);

        btnResting = findViewById(R.id.btn_resting);
        btnReceivingCall = findViewById(R.id.btn_receiving_call);

        drawerLayout = findViewById(R.id.drawer_layout);
        btnWaitingZone = findViewById(R.id.btn_waiting_zone);
        btnCallList = findViewById(R.id.btn_call_list);
        tvStatus = findViewById(R.id.tv_status);
        tvStatusSub = findViewById(R.id.tv_status_sub);

        btnMenu.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnResting.setOnClickListener(this);
        btnReceivingCall.setOnClickListener(this);
        btnWaitingZone.setOnClickListener(this);
        btnCallList.setOnClickListener(this);


        //if (getScenarioService().getRestType() == Packets.RestType.Rest)
        //    isRest = true;
        //else
        //    isRest = false;

        tvCarNum.setText(String.valueOf(getConfigLoader().getCarId()));
        checkRestView(isRest);
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
//        super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        Intent intent;
        switch (view.getId()) {
            case R.id.btn_menu: //메뉴
                intent = new Intent(this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btn_waiting_zone:   //대기장소
                intent = new Intent(this, WaitingZoneActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btn_call_list:   //콜목록
                intent = new Intent(this, WaitingCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btn_resting:  //휴식하기
                checkRequestRest(Packets.RestType.Rest);
                break;

            case R.id.btn_receiving_call:   //콜받기
                checkRequestRest(Packets.RestType.Working);
                break;

            case R.id.btn_logout:   //로그아웃
                logout();
                finish();
                break;

            default:
                break;
        }
    }

    private void checkRequestRest(Packets.RestType restType) {
        //배차정보 or 탑승 중 체크
        if (getScenarioService().getBoardType() == Packets.BoardType.Boarding || getPreferenceUtil().getCallInfoWait() != null
                || getPreferenceUtil().getCallInfoNormal() != null) {
            Popup popup = new Popup
                    .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_FAILURE)
                    .setTitle(getString(R.string.popup_btn_confirm))
                    .setContent(getString(R.string.boarding_or_reservation))
                    .build();
            showPopupDialog(popup);

        } else {
            getScenarioService().requestRest(restType);
        }
    }

    public void checkRestView(boolean isResting) {
        if (isResting) {
            WavResourcePlayer.getInstance(this).play(R.raw.voice_112);
            tvStatus.setText(R.string.main_status_text_resting);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.colorStatusRestingString));
            tvStatusSub.setVisibility(View.VISIBLE);
            btnReceivingCall.setVisibility(View.VISIBLE);
//            btnWaitingZone.setVisibility(View.GONE);
//            btnCallList.setVisibility(View.GONE);
            btnResting.setVisibility(View.GONE);

        } else {
            WavResourcePlayer.getInstance(this).play(R.raw.voice_114);
            tvStatus.setText(R.string.main_status_text_vacancy);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.colorStatusVacancyString));
            tvStatusSub.setVisibility(View.GONE);
            btnReceivingCall.setVisibility(View.GONE);
//            btnWaitingZone.setVisibility(View.VISIBLE);
//            btnCallList.setVisibility(View.VISIBLE);
            btnResting.setVisibility(View.VISIBLE);
        }
    }
}
