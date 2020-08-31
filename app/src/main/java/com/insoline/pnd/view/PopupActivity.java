package com.insoline.pnd.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.fragment.PopupDialogFragment;

public class PopupActivity extends BaseActivity {

    private PopupDialogListener listener;

    public interface PopupDialogListener {
        void onDismissPopupDialog(String tag, Intent intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Popup popup = (Popup) getIntent().getSerializableExtra(Constants.INTENT_KEY_POPUP_DIALOG);
        if (popup != null) {
            showPopupDialog(popup);
        }

        LogHelper.e("getCallingActivity() : " + getCallingActivity());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Popup popup = (Popup) intent.getSerializableExtra(Constants.INTENT_KEY_POPUP_DIALOG);
        showPopupDialog(popup);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogHelper.e("onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);    //다이얼로그 종료시 전환 애니메이션 제거
    }

    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
        LogHelper.e("onDismissPopupDialog()");
        finish();
    }

    @Override
    public void onBackPressed() {
        LogHelper.e("onBackPressed()");
        finish();
    }
}
