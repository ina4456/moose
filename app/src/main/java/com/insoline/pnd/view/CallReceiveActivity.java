package com.insoline.pnd.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallOrderPacket;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.StringUtil;
import com.insoline.pnd.utils.WavResourcePlayer;

public class CallReceiveActivity extends BaseActivity {

    private RatingBar rbCallGrade;
    private TextView tvHere, tvAllocationDistance, tvAllocationDeparturePoi, tvAllocationDepartureAddr;
    private TextView tvAllocationDestinationPoi;
    private Button btnAllocationRefuse, btnAllocationRequest;

    private ResponseCallOrderPacket callInfoTemp;
    private CountDownTimer countDownTimer;
    private static final int COUNT_DOWN_INTERVAL = 1000;

    private boolean hasGotResponse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_receive);

        rbCallGrade = findViewById(R.id.rb_call_grade);

        tvHere = findViewById(R.id.tv_here);
        tvAllocationDistance = findViewById(R.id.tv_allocation_distance);
        tvAllocationDeparturePoi = findViewById(R.id.tv_allocation_departure_poi);
        tvAllocationDepartureAddr = findViewById(R.id.tv_allocation_departure_addr);

        tvAllocationDestinationPoi = findViewById(R.id.tv_allocation_destination_poi);

        btnAllocationRefuse = findViewById(R.id.btn_allocation_refuse);
        btnAllocationRequest = findViewById(R.id.btn_allocation_request);

        btnAllocationRefuse.setOnClickListener(this);
        btnAllocationRequest.setOnClickListener(this);

        initView();
    }

    private void initView() {
        callInfoTemp = getPreferenceUtil().getCallInfoTemp();

        if (callInfoTemp == null) {
            finishActivity();
            return;
        }

        //RatingBar
//        LayerDrawable stars = (LayerDrawable) rbCallGrade.getProgressDrawable();
//        setRatingStarColor(stars.getDrawable(2), ContextCompat.getColor(this, R.color.colorYellow));
//        setRatingStarColor(stars.getDrawable(1), ContextCompat.getColor(this, R.color.colorYellow));
//        setRatingStarColor(stars.getDrawable(0), ContextCompat.getColor(this, R.color.colorGray07));

        if (callInfoTemp.getCallClass() == 0) {
            rbCallGrade.setVisibility(View.GONE);
        } else {
            rbCallGrade.setVisibility(View.VISIBLE);
            rbCallGrade.setRating(callInfoTemp.getCallClass());
        }

        //출발지까지의 거리
        if (callInfoTemp.getLatitude() != 0.0 && callInfoTemp.getLongitude() != 0.0) {
            int distance = (int) getScenarioService().getGpsHelper().getDistance(callInfoTemp.getLatitude(), callInfoTemp.getLongitude());
            tvAllocationDistance.setText(distance + "m");
        } else {
            tvHere.setVisibility(View.INVISIBLE);
            tvAllocationDistance.setVisibility(View.INVISIBLE);
        }

        //출발지 정보
        String place = callInfoTemp.getPlace();
        String placeExplanation = callInfoTemp.getPlaceExplanation();
        if (!StringUtil.isEmptyString(place) && !StringUtil.isEmptyString(placeExplanation)) {
            tvAllocationDeparturePoi.setText(place);
            tvAllocationDepartureAddr.setText(placeExplanation);
        } else if (!StringUtil.isEmptyString(place) && StringUtil.isEmptyString(placeExplanation)) {
            tvAllocationDeparturePoi.setText(place);
            tvAllocationDepartureAddr.setVisibility(View.GONE);
        } else if (!StringUtil.isEmptyString(place) && StringUtil.isEmptyString(placeExplanation)) {
            tvAllocationDeparturePoi.setText(placeExplanation);
            tvAllocationDepartureAddr.setVisibility(View.GONE);
        } else {
            tvAllocationDeparturePoi.setText(getString(R.string.allocation_text_empty_departure));
            tvAllocationDepartureAddr.setVisibility(View.GONE);
        }

        //목적지 정보
        String destName = callInfoTemp.getDestName();
        if (StringUtil.isEmptyString(destName)) {
            destName = getString(R.string.allocation_text_empty_destination);
        }
        tvAllocationDestinationPoi.setText(destName);

        int displayTime = getConfigLoader().getCvt();
        if (displayTime <= 0) {
            displayTime = 5;
        }
        startCountDown(displayTime);
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

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
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
        callReject();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.btn_allocation_refuse:    //배차거절
                WavResourcePlayer.getInstance(this).play(R.raw.voice_126);
                callReject();
                break;

            case R.id.btn_allocation_request:   //배차요청
                hasGotResponse = false;
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }

                WavResourcePlayer.getInstance(this).play(R.raw.voice_124);
                getScenarioService().requestCallOrderRealtime(Packets.OrderDecisionType.Request, callInfoTemp);

                //10초간 응답 못받으면 배차거절 처리
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LogHelper.e("hasGotResponse : " + hasGotResponse);
                        if (!hasGotResponse) {
                            WavResourcePlayer.getInstance(CallReceiveActivity.this).play(R.raw.voice_122);
                            showAllocationFailedPopup();
                        }
                    }
                }, 10000);
                break;

            default:
                break;
        }
    }

    /**
     * 배차 거절
     */
    private void callReject() {
        getPreferenceUtil().clearCallInfoTemp();
        getScenarioService().requestCallOrderRealtime(Packets.OrderDecisionType.Reject, callInfoTemp);
        finishActivity();
    }

    private void startCountDown(int displayTime) {
        countDownTimer = new CountDownTimer((displayTime) * COUNT_DOWN_INTERVAL, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long l) {
                setTextWithCount((int) (l / 1000));
            }

            @Override
            public void onFinish() {
                LogHelper.e("onFinish()");
                callReject();
            }
        };
        countDownTimer.start();
    }

    private void setTextWithCount(int count) {
        String label = String.format(getString(R.string.allocation_btn_refuse_with_count), count);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            btnAllocationRefuse.setText(Html.fromHtml(label, Html.FROM_HTML_MODE_LEGACY));
        } else {
            btnAllocationRefuse.setText(Html.fromHtml(label));
        }
    }

    private void setRatingStarColor(Drawable drawable, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(drawable, color);
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    //배차 요청 실패 팝업
    private void showAllocationFailedPopup() {
        Popup popup = new Popup
                .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_FAILURE)
                .setTitle(getString(R.string.popup_title_alert))
                .setContent(getString(R.string.popup_msg_allocation_failed))
                .setIsHiddenStatusBar(false)
                .setDismissSecond(3)
                .build();
        showPopupDialog(popup);
    }

    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
        LogHelper.e("onDismissPopupDialog() / tag : " + tag);
        switch (tag) {
            case Constants.DIALOG_TAG_FAILURE:  //배차 요청 실패
                getPreferenceUtil().clearCallInfoTemp();
                finishActivity();
                break;
        }
    }

    public void setHasGotResponse() {
        hasGotResponse = true;
        finishActivity();
    }
}
