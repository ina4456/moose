package com.insoline.pnd.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.model.SelectionItem;
import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallOrderPacket;
import com.insoline.pnd.utils.INaviExecutor;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.StringUtil;
import com.insoline.pnd.utils.WavResourcePlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CallInfoActivity extends BaseActivity {

    private TextView tvAllocationStatus, tvAllocationTarget, tvAllocationTargetPoi, tvAllocationTargetAddr;
    private TextView tvAllocationNextOrPrevTarget, tvAllocationNextOrPrevTargetPoi;
    private ImageButton btnRoute, btnSendMessage, btnCallToPassenger;
    private Button btnAllocationCancelCall;

    private ResponseCallOrderPacket callInfoNormal;
    private String routingDestination = "";
    private double routingLatitude = -1;
    private double routingLongitude = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_info);

        tvAllocationStatus = findViewById(R.id.tv_allocation_status);
        tvAllocationTarget = findViewById(R.id.tv_allocation_target);
        tvAllocationTargetPoi = findViewById(R.id.tv_allocation_target_poi);
        tvAllocationTargetAddr = findViewById(R.id.tv_allocation_target_addr);

        tvAllocationNextOrPrevTarget = findViewById(R.id.tv_allocation_next_or_prev_target);
        tvAllocationNextOrPrevTargetPoi = findViewById(R.id.tv_allocation_next_or_prev_target_poi);

        btnRoute = findViewById(R.id.btn_route);
        btnSendMessage = findViewById(R.id.btn_send_message);
        btnCallToPassenger = findViewById(R.id.btn_call_to_passenger);
        btnAllocationCancelCall = findViewById(R.id.btn_allocation_cancel_call);

        btnRoute.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);
        btnCallToPassenger.setOnClickListener(this);
        btnAllocationCancelCall.setOnClickListener(this);

        initView();
    }

    private void initView() {
        callInfoNormal = getPreferenceUtil().getCallInfoNormal();
        ResponseCallOrderPacket callInfoGeton = getPreferenceUtil().getCallInfoGeton();

        if (callInfoNormal == null && callInfoGeton == null) {
            finishActivity();
            return;
        }

        if (callInfoNormal == null && callInfoGeton != null) {
            getPreferenceUtil().setCallInfoNormal(callInfoGeton);
            getPreferenceUtil().clearCallInfoGeton();

            callInfoNormal = getPreferenceUtil().getCallInfoNormal();
        }

        String targetPoi, targetAddr, emptyTargetPoi;
        String nextOrPrevTargetPoi, nextOrPrevTargetAddr, emptyNextOrPrev;
        double latitude, longitude;

        if (getScenarioService().getBoardType() != Packets.BoardType.Boarding) {
            //탑승 전
            WavResourcePlayer.getInstance(this).play(R.raw.voice_120);

            tvAllocationStatus.setText(getString(R.string.allocation_status_allocated));
            tvAllocationTarget.setText(getString(R.string.allocation_text_passenger_location));
            btnRoute.setBackgroundResource(R.drawable.selector_bg_route_passenger_btn);
            tvAllocationNextOrPrevTarget.setText(getString(R.string.allocation_text_destination));

            btnSendMessage.setVisibility(View.VISIBLE);
            btnCallToPassenger.setVisibility(View.VISIBLE);
            btnAllocationCancelCall.setVisibility(View.VISIBLE);

            targetPoi = callInfoNormal.getPlace();
            targetAddr = callInfoNormal.getPlaceExplanation();
            nextOrPrevTargetPoi = callInfoNormal.getDestName();
            nextOrPrevTargetAddr = "";
            emptyTargetPoi = getString(R.string.allocation_text_empty_departure);
            emptyNextOrPrev = getString(R.string.allocation_text_empty_destination);

            latitude = callInfoNormal.getLatitude();
            longitude = callInfoNormal.getLongitude();

            String phoneNumber = callInfoNormal.getCallerPhone();
            if (!StringUtil.isEmptyString(phoneNumber)) {
                btnCallToPassenger.setEnabled(true);
            } else {
                btnCallToPassenger.setEnabled(false);
            }

        } else {
            //탑승 후
            tvAllocationStatus.setText(getString(R.string.allocation_status_boarded));
            tvAllocationTarget.setText(getString(R.string.allocation_text_destination));
            btnRoute.setBackgroundResource(R.drawable.selector_bg_route_destination_btn);
            tvAllocationNextOrPrevTarget.setText(getString(R.string.allocation_text_departure));

            btnSendMessage.setVisibility(View.GONE);
            btnCallToPassenger.setVisibility(View.GONE);
            btnAllocationCancelCall.setVisibility(View.GONE);

            targetPoi = callInfoNormal.getDestName();
            targetAddr = "";
            nextOrPrevTargetPoi = callInfoNormal.getPlace();
            nextOrPrevTargetAddr = callInfoNormal.getPlaceExplanation();
            emptyTargetPoi = getString(R.string.allocation_text_empty_destination);
            emptyNextOrPrev = getString(R.string.allocation_text_empty_departure);

            latitude = callInfoNormal.getDestLatitude();
            longitude = callInfoNormal.getDestLongitude();
        }

        //출발지/목적지 정보
        if (!StringUtil.isEmptyString(targetPoi) && !StringUtil.isEmptyString(targetAddr)) {
            tvAllocationTargetPoi.setText(targetPoi);
            tvAllocationTargetAddr.setText(targetAddr);
            routingDestination = targetPoi;
        } else if (!StringUtil.isEmptyString(targetPoi) && StringUtil.isEmptyString(targetAddr)) {
            tvAllocationTargetPoi.setText(targetPoi);
            tvAllocationTargetAddr.setVisibility(View.GONE);
            routingDestination = targetPoi;
        } else if (StringUtil.isEmptyString(targetPoi) && !StringUtil.isEmptyString(targetAddr)) {
            tvAllocationTargetPoi.setText(targetAddr);
            tvAllocationTargetAddr.setVisibility(View.GONE);
            routingDestination = targetAddr;
        } else {
            tvAllocationTargetPoi.setText(emptyTargetPoi);
            tvAllocationTargetAddr.setVisibility(View.GONE);
        }

        //목적지/출발지 정보
        if (!StringUtil.isEmptyString(nextOrPrevTargetPoi)) {
            tvAllocationNextOrPrevTargetPoi.setText(nextOrPrevTargetPoi);
        } else if (!StringUtil.isEmptyString(nextOrPrevTargetAddr)) {
            tvAllocationNextOrPrevTargetPoi.setText(nextOrPrevTargetAddr);
        } else {
            tvAllocationNextOrPrevTargetPoi.setText(emptyNextOrPrev);
        }

        //출발지/목적지 좌표
        if (latitude != 0.0 && longitude != 0.0) {
            btnRoute.setEnabled(true);
            routingLatitude = latitude;
            routingLongitude = longitude;

            //자동으로 길안내
            showRoutingPopup();
        } else {
            btnRoute.setEnabled(false);
        }
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

        switch (view.getId()) {
            case R.id.btn_route:    //손님에게 길안내
                showRoutingPopup();
                break;

            case R.id.btn_send_message: //메시지
                showMessagePopup();
                break;

            case R.id.btn_call_to_passenger:    //손님에게 전화
                String phoneNumber = callInfoNormal.getCallerPhone();
                if (!StringUtil.isEmptyString(phoneNumber)) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.btn_allocation_cancel_call:   //탑승실패
                allocationCancel();
                break;
        }
    }

    private void allocationCancel() {
        getScenarioService().requestReport(callInfoNormal.getCallNumber(), callInfoNormal.getOrderCount(),
                callInfoNormal.getOrderKind(), callInfoNormal.getCallReceiptDate(), Packets.ReportKind.Failed, 0, 0);
        INaviExecutor.cancelNavigation(this);
    }

    //길안내
    private void showRoutingPopup() {
        Popup popup = new Popup.Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_ROUTING_TO_DEPARTURE)
                .setTitle(getString(R.string.popup_title_alert))
                .setContent(getString(R.string.popup_msg_routing_departure))
                .setDismissSecond(3)
                .build();
        showPopupDialog(popup);
    }

    //메시지 전송
    private void showMessagePopup() {
        ArrayList<SelectionItem> itemList = new ArrayList<>();
        List<String> list = Arrays.asList(getResources().getStringArray(R.array.items_messages_list));
        for (String content : list) {
            SelectionItem item = new SelectionItem();

            if (content.equals(getString(R.string.msg_soon))) {
                content = String.format(getString(R.string.msg_soon), String.valueOf(getConfigLoader().getCarId()));
            } else if (content.equals(getString(R.string.msg_arrival))) {
                content = String.format(getString(R.string.msg_arrival), String.valueOf(getConfigLoader().getCarId()));
            }

            item.setItemContent(content);
            itemList.add(item);
        }

        Popup popup = new Popup
                .Builder(Popup.TYPE_RADIO_TWO_BTN_LIST, Constants.DIALOG_TAG_MESSAGE_SELECTION)
                .setTitle(getString(R.string.allocation_send_message))
                .setSelectionItems(itemList)
                .setIsHiddenStatusBar(false)
                .build();
        showPopupDialog(popup);
    }

    //취소 사유
    private void showCancelReasonPopup() {
        ArrayList<SelectionItem> itemList = new ArrayList<>();
        List<String> list = Arrays.asList(getResources().getStringArray(R.array.items_alloc_cancel_reason));
        for (String content : list) {
            SelectionItem item = new SelectionItem();
            item.setItemContent(content);
            itemList.add(item);
        }

        Popup popup = new Popup
                .Builder(Popup.TYPE_RADIO_TWO_BTN_LIST, Constants.DIALOG_TAG_CANCEL_REASON_SELECTION)
                .setTitle(getString(R.string.allocation_btn_boarding_failure))
                .setSelectionItems(itemList)
                .setIsHiddenStatusBar(false)
                .build();
        showPopupDialog(popup);
    }

    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
        LogHelper.e("onDismissPopupDialog() / tag : " + tag);

        switch (tag) {
            case Constants.DIALOG_TAG_ROUTING_TO_DEPARTURE:
                WavResourcePlayer.getInstance(this).play(R.raw.voice_128);
                INaviExecutor.startNavigationNow(this, routingDestination, routingLatitude, routingLongitude);
                break;

            case Constants.DIALOG_TAG_MESSAGE_SELECTION:
                if (intent != null) {
                    String selectedItem = intent.getStringExtra(Constants.DIALOG_INTENT_KEY_RADIO_BUTTON);
                    selectedItem = selectedItem.replaceAll("\\<[^>]*>", "");
                    LogHelper.e("selectedItem : " + selectedItem);

                    //sms 전송
                }
                break;

            case Constants.DIALOG_TAG_CANCEL_REASON_SELECTION:
                if (intent != null) {
                    //탑승 실패 처리
                    String cancelReason = intent.getStringExtra(Constants.DIALOG_INTENT_KEY_RADIO_BUTTON);
                    Packets.ReportKind reason = Packets.ReportKind.FailedEtc;
                    LogHelper.e("cancelReason : " + cancelReason);
                    if (cancelReason != null) {
                        if (cancelReason.equals(getString(R.string.alloc_cancel_reason_passenger))) {
                            reason = Packets.ReportKind.FailedPassengerCancel;
                        } else if (cancelReason.equals(getString(R.string.alloc_cancel_reason_no_show))) {
                            reason = Packets.ReportKind.FailedNoShow;
                        } else if (cancelReason.equals(getString(R.string.alloc_cancel_reason_using_other_car))) {
                            reason = Packets.ReportKind.FailedUseAnotherTaxi;
                        } else if (cancelReason.equals(getString(R.string.alloc_cancel_reason_etc))) {
                            reason = Packets.ReportKind.FailedEtc;
                        }

                        getScenarioService().requestReport(callInfoNormal.getCallNumber(), callInfoNormal.getOrderCount(),
                                callInfoNormal.getOrderKind(), callInfoNormal.getCallReceiptDate(), reason, 0, 0);
                    }
                    INaviExecutor.cancelNavigation(this);
                }
                break;
        }
    }

}
