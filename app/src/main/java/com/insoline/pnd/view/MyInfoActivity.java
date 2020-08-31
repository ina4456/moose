package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.utils.LogHelper;

public class MyInfoActivity extends BaseActivity {

    private TextView tvName, tvDriverNum, tvCompany;
    private TextView tvVehicleNumber, tvVehicleModel, tvVehicleColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        tvName = findViewById(R.id.tv_name);
        tvDriverNum = findViewById(R.id.tv_driver_num);
        tvCompany = findViewById(R.id.tv_company);
        tvVehicleNumber = findViewById(R.id.tv_vehicle_number);
        tvVehicleModel = findViewById(R.id.tv_vehicle_model);
        tvVehicleColor = findViewById(R.id.tv_vehicle_color);
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
    }
}
