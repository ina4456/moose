package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.adapter.OperationHistoryDetailListAdapter;

public class OperationHistoryDetailActivity extends BaseActivity {

    public static final String EXTRA_KEY_PERIOD_TYPE = "extra_key_period_type";

    private OperationHistoryDetailListAdapter mOperationHistoryDetailListAdapter;
    private int intPeriodType = 0;

    private TextView tvTitle, tvCompleteCnt, tvCancelCnt, operationEmptyText;
    private Button btnFilter;
    private RecyclerView rvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_history_detail);

        intPeriodType = getIntent().getIntExtra(EXTRA_KEY_PERIOD_TYPE, 0);

        tvTitle = findViewById(R.id.tv_title);
        tvCompleteCnt = findViewById(R.id.tv_complete_cnt);
        tvCancelCnt = findViewById(R.id.tv_cancel_cnt);
        btnFilter = findViewById(R.id.btn_filter);
        rvHistory = findViewById(R.id.rv_history);
        operationEmptyText = findViewById(R.id.operation_empty_text);

        btnFilter.setOnClickListener(this);

        //제목 설정
        int titleRid;
        switch (intPeriodType) {
            case 1:
                titleRid = R.string.history_week;
                break;
            case 2:
                titleRid = R.string.history_this_month;
                break;
            case 3:
                titleRid = R.string.history_last_month;
                break;
            default:
                titleRid = R.string.history_today;
                break;
        }
        tvTitle.setText(getString(R.string.main_menu_history) + " - " + getString(titleRid));
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
            case R.id.btn_filter:
                String filter = btnFilter.getText().toString();
                if (filter.equals(getString(R.string.history_call_type_all))) {
                    filter = getString(R.string.history_call_type_normal);
                } else if (filter.equals(getString(R.string.history_call_type_normal))) {
                    filter = getString(R.string.history_call_type_app);
                } else if (filter.equals(getString(R.string.history_call_type_app))) {
                    filter = getString(R.string.history_call_type_business);
                } else {
                    filter = getString(R.string.history_call_type_all);
                }

                btnFilter.setText(filter);
                break;

            default:
                break;
        }
    }
}
