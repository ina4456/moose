package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.utils.LogHelper;

public class OperationHistoryActivity extends BaseActivity {

    private ConstraintLayout clHistoryToday, clHistoryWeek, clHistoryThisMonth, clHistoryLastMmonth;
    private TextView tvTodayTotalCount, tvTodayNormalCount, tvTodayAppCount;
    private TextView tvRecent7daysTotalCount, tvRecent7daysNormalCount, tvRecent7daysAppCount;
    private TextView tvThisMonthTotalCount, tvThisMonthNormalCount, tvThisMonthAppCount;
    private TextView tvLastMonthTotalCount, tvLastMonthNormalCount, tvLastMonthAppCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_history);

        clHistoryToday = findViewById(R.id.cl_history_today);
        tvTodayTotalCount = findViewById(R.id.tv_today_total_count);
        tvTodayNormalCount = findViewById(R.id.tv_today_normal_count);
        tvTodayAppCount = findViewById(R.id.tv_today_app_count);

        clHistoryWeek = findViewById(R.id.cl_history_week);
        tvRecent7daysTotalCount = findViewById(R.id.tv_recent_7days_total_count);
        tvRecent7daysNormalCount = findViewById(R.id.tv_recent_7days_normal_count);
        tvRecent7daysAppCount = findViewById(R.id.tv_recent_7days_app_count);

        clHistoryThisMonth = findViewById(R.id.cl_history_this_month);
        tvThisMonthTotalCount = findViewById(R.id.tv_this_month_total_count);
        tvThisMonthNormalCount = findViewById(R.id.tv_this_month_normal_count);
        tvThisMonthAppCount = findViewById(R.id.tv_this_month_app_count);

        clHistoryLastMmonth = findViewById(R.id.cl_history_last_month);
        tvLastMonthTotalCount = findViewById(R.id.tv_last_month_total_count);
        tvLastMonthNormalCount = findViewById(R.id.tv_last_month_normal_count);
        tvLastMonthAppCount = findViewById(R.id.tv_last_month_app_count);

        clHistoryToday.setOnClickListener(this);
        clHistoryWeek.setOnClickListener(this);
        clHistoryThisMonth.setOnClickListener(this);
        clHistoryLastMmonth.setOnClickListener(this);
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

        int periodType = 0;
        switch (view.getId()) {
            case R.id.cl_history_today: //오늘
                periodType = 0;
                break;

            case R.id.cl_history_week:  //최근7일
                periodType = 1;
                break;

            case R.id.cl_history_this_month:    //이번달
                periodType = 2;
                break;

            case R.id.cl_history_last_month:    //지난달
                periodType = 3;
                break;

            default:
                break;
        }

        Intent intent = new Intent(this, OperationHistoryDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(OperationHistoryDetailActivity.EXTRA_KEY_PERIOD_TYPE, periodType);
        startActivity(intent);
    }
}
