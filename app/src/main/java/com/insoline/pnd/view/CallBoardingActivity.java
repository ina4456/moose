package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.utils.LogHelper;

public class CallBoardingActivity extends BaseActivity {

    private TextView tvAllocationTarget, tvAllocationTargetPoi, tvAllocationTargetAddr;
    private TextView tvAllocationPrevTarget, tvAllocationPrevTargetPoi;
    private View tvAllocationPrevTargetDivider;
    private ImageButton btnRoute;
    private Button btnAllocationAlighting;
    private TextView tvPassengerBoardedWithoutDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_boarding);

        tvAllocationTarget = findViewById(R.id.tv_allocation_target);
        tvAllocationTargetPoi = findViewById(R.id.tv_allocation_target_poi);
        tvAllocationTargetAddr = findViewById(R.id.tv_allocation_target_addr);

        tvAllocationPrevTarget = findViewById(R.id.tv_allocation_prev_target);
        tvAllocationPrevTargetDivider = findViewById(R.id.tv_allocation_prev_target_divider);
        tvAllocationPrevTargetPoi = findViewById(R.id.tv_allocation_prev_target_poi);

        btnRoute = findViewById(R.id.btn_route);
        btnAllocationAlighting = findViewById(R.id.btn_allocation_alighting);
        tvPassengerBoardedWithoutDestination = findViewById(R.id.tv_passenger_boarded_without_destination);

        btnRoute.setOnClickListener(this);
        btnAllocationAlighting.setOnClickListener(this);
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
            case R.id.btn_route:    //목적지로 길안내
                break;

            case R.id.btn_allocation_alighting:   //손님하차
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }
    }
}
