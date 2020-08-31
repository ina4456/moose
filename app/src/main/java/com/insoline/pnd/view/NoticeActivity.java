package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.model.NoticeList;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.adapter.NoticeListAdapter;
import com.insoline.pnd.view.fragment.PopupDialogFragment;

public class NoticeActivity extends BaseActivity implements NoticeListAdapter.SelectionCallback {

    public static final String EXTRA_IS_NOTICE = "extra_is_notice";

    private TextView noticeTitle, noticeEmptyText;
    private RecyclerView noticeListRecyclerView;
    private boolean isNotice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        isNotice = getIntent().getBooleanExtra(EXTRA_IS_NOTICE, false);

        noticeTitle = findViewById(R.id.notice_title);
        noticeEmptyText = findViewById(R.id.notice_empty_text);
        noticeListRecyclerView = findViewById(R.id.notice_list_recycler_view);

        if (isNotice) {
            noticeTitle.setText(getString(R.string.main_menu_notice));
            noticeEmptyText.setText(getString(R.string.notice_empty));
        } else {
            noticeTitle.setText(getString(R.string.main_menu_message));
            noticeEmptyText.setText(getString(R.string.message_empty));
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
    }



    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
    }

    @Override
    public void onListItemSelected(NoticeList item) {
        String title = getString(R.string.main_menu_notice);
        String tag = Constants.DIALOG_TAG_NOTICE;
        if (!item.isNotice()) {
            title = getString(R.string.main_menu_message);
            tag = Constants.DIALOG_TAG_MESSAGE_FROM_LIST;
        }

        Popup popup = new Popup
                .Builder(Popup.TYPE_ONE_BTN_LARGE, tag)
                .setTitle(title)
                .setContentTitle(item.isNotice() ? item.getTitle() : null)
                .setContent(item.getContent())
                .build();
        showPopupDialog(popup);
    }
}
