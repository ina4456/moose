package com.insoline.pnd.view.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.SiteConstants;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.databinding.DialogListSingleSelectionBinding;
import com.insoline.pnd.databinding.DialogOneBtnBinding;
import com.insoline.pnd.databinding.DialogTwoBtnBinding;
import com.insoline.pnd.databinding.DialogTwoBtnEditTextBinding;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.model.SelectionItem;
import com.insoline.pnd.utils.DeviceUtil;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.adapter.PopupSelectionListAdapter;

import java.util.ArrayList;

public class PopupDialogFragment extends DialogFragment implements View.OnClickListener, View.OnTouchListener, PopupSelectionListAdapter.SelectionCallback {

    private static final String ARG_DIALOG_POPUP = "arg_dialog_popup";
    private static final String ARG_LISTENER = "arg_listener";
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private boolean toggleIp = false;

    private DialogOneBtnBinding dialogOneBtnBinding;
    private DialogTwoBtnEditTextBinding dialogTwoBtnEditTextBinding;
    private DialogListSingleSelectionBinding dialogSingleSelection;
    private Popup popup;
    private PopupDialogListener listener;
    private String twoBtnRadioListSelectedItem;

    public interface PopupDialogListener {
        void onDismissPopupDialog(String tag, Intent intent);
    }

    /**
     * 팝업 다이얼로그 프레그먼트 생성
     *
     * @param popup 팝업 객체
     */
    public static PopupDialogFragment newInstance(Popup popup) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_DIALOG_POPUP, popup);

        PopupDialogFragment fragment = new PopupDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        //LogHelper.e("onAttach()");
        super.onAttach(context);
        try {
            if (getParentFragment() != null) {
                listener = (PopupDialogListener) getParentFragment();
            } else {
                listener = (PopupDialogListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PopupDialogListener for callback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            popup = (Popup) getArguments().getSerializable(ARG_DIALOG_POPUP);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        LogHelper.e("onCreateDialog()");

        Dialog dialog = new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                //실시간 메시지의 경우 처리
                if (popup != null && popup.getTag().equals(Constants.DIALOG_TAG_MESSAGE)) {
                    LogHelper.e("onBackPressed() && popup dialog from PopupActivity");
                    dismissDialog(null);
                    getActivity().finish();
                } else {
                    dismissDialog(null);
                }
            }
        };
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //LogHelper.e("onCreateView()");
        Dialog dialog = getDialog();
        Window window = dialog.getWindow();
        window.requestFeature(STYLE_NO_TITLE); //타이틀 영역 제거
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //기본 배경색 투명 적용
        dialog.setCanceledOnTouchOutside(true); //외부 클릭시 다이얼로그 종료되지 않게 설정
        setCancelable(false);

        ViewDataBinding viewDataBinding = null;
        if (popup == null) {
            dismiss();
        } else {
            switch (popup.getType()) {
                case Popup.TYPE_ONE_BTN_NORMAL:
                case Popup.TYPE_ONE_BTN_LARGE:
                    viewDataBinding = getOneBtnDialog();
                    break;
                case Popup.TYPE_TWO_BTN_NORMAL:
                case Popup.TYPE_TWO_BTN_LARGE:
                    viewDataBinding = getTwoBtnDialog();
                    break;
                case Popup.TYPE_TWO_BTN_EDIT_TEXT:
                    viewDataBinding = getTwoBtnEditTextDialog();
                    break;
                case Popup.TYPE_RADIO_BTN_LIST:
                case Popup.TYPE_RADIO_TWO_BTN_LIST:
                    viewDataBinding = getSingleSelectionDialog();
                    break;
            }
        }

        if (viewDataBinding != null) {
            return viewDataBinding.getRoot();
        } else {
            //throw new NullPointerException("something wrong with dialog view");
            return null;
        }
    }

    @Override
    public void onResume() {
        LogHelper.e("onResume()");
        super.onResume();
        Window window = getDialog().getWindow();
        if (window != null && popup != null) {
            int dialogWidth = popup.getWidth();
            if (dialogWidth != 0) {
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
            } else {
                throw new NullPointerException("must set width of PopupDialogFragment");
            }

            if (popup.isHiddenStatusBar()) {
                setFullScreen(window);
            }
            if (popup.getDismissSecond() != 0) { //자동 소거
                startCountDown(popup.getDismissSecond());
            }
            if (popup.getType() == Popup.TYPE_TWO_BTN_EDIT_TEXT) {
                setDialogPositionAboveStatusBar(window);
            }
        }
    }

    @Override
    public void onDetach() {
        LogHelper.e("onDetach()");
        super.onDetach();
    }

    @Override
    public void onStop() {
        LogHelper.e("onStop()");
        super.onStop();
    }


    @Override
    public void onAttachFragment(Fragment childFragment) {
        //LogHelper.e("onAttachFragment()");
        super.onAttachFragment(childFragment);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private ViewDataBinding getOneBtnDialog() {
        dialogOneBtnBinding = DataBindingUtil
                .inflate(LayoutInflater.from(getContext()), R.layout.dialog_one_btn, null, true);
        dialogOneBtnBinding.dialogOneBtnTitle.setText(popup.getTitle());
        dialogOneBtnBinding.dialogOneBtnContent.setText(popup.getContent());
        dialogOneBtnBinding.dialogOneBtnContent.setMovementMethod(new ScrollingMovementMethod());

        String labelPositiveBtn = popup.getLabelPositiveBtn();
        if (popup.getDismissSecond() != 0) {
            labelPositiveBtn = getBtnLabelWithDismissSecond(labelPositiveBtn, popup.getDismissSecond());
        }
        dialogOneBtnBinding.dialogOneBtnBtn.setText(labelPositiveBtn);
        dialogOneBtnBinding.dialogOneBtnBtn.setOnClickListener(this);

        //공지 사항 UI 변경
        if (popup.getType() == Popup.TYPE_ONE_BTN_LARGE && popup.getContentTitle() != null) {
            getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            dialogOneBtnBinding.dialogOneBtnContentTitle.setText(popup.getContentTitle());
            dialogOneBtnBinding.dialogOneBtnContentTitle.setVisibility(View.VISIBLE);
            dialogOneBtnBinding.dialogOneBtnContent.setGravity(Gravity.START);
            dialogOneBtnBinding.dialogOneBtnContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

            ConstraintLayout.LayoutParams parameter = (ConstraintLayout.LayoutParams) dialogOneBtnBinding.dialogOneBtnContent.getLayoutParams();
            parameter.setMargins(0, DeviceUtil.convertDpToPx(getContext(), 14),
                    0, DeviceUtil.convertDpToPx(getContext(), 19));
            parameter.height = DeviceUtil.convertDpToPx(getContext(), 160);
            dialogOneBtnBinding.dialogOneBtnContent.setLayoutParams(parameter);

        } else if (popup.getType() == Popup.TYPE_ONE_BTN_LARGE && popup.getContentTitle() == null) {
            //메시지
            getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            dialogOneBtnBinding.dialogOneBtnContentTitle.setVisibility(View.GONE);
            dialogOneBtnBinding.dialogOneBtnContent.setGravity(Gravity.START);
            dialogOneBtnBinding.dialogOneBtnContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

            ConstraintLayout.LayoutParams parameter = (ConstraintLayout.LayoutParams) dialogOneBtnBinding.dialogOneBtnContent.getLayoutParams();
            parameter.setMargins(0, DeviceUtil.convertDpToPx(getContext(), 14),
                    0, DeviceUtil.convertDpToPx(getContext(), 19));
            parameter.height = DeviceUtil.convertDpToPx(getContext(), 231);
            dialogOneBtnBinding.dialogOneBtnContent.setLayoutParams(parameter);

        }

        return dialogOneBtnBinding;
    }

    private ViewDataBinding getTwoBtnDialog() {
        DialogTwoBtnBinding dialogTwoBtnBinding = DataBindingUtil
                .inflate(LayoutInflater.from(getContext()), R.layout.dialog_two_btn, null, true);
        dialogTwoBtnBinding.dialogTwoBtnTitle.setText(popup.getTitle());
        dialogTwoBtnBinding.dialogTwoBtnContent.setText(popup.getContent());
        dialogTwoBtnBinding.dialogTwoBtnNegativeBtn.setText(popup.getLabelNegativeBtn());
        dialogTwoBtnBinding.dialogTwoBtnPositiveBtn.setText(popup.getLabelPositiveBtn());
        dialogTwoBtnBinding.dialogTwoBtnNegativeBtn.setOnClickListener(this);
        dialogTwoBtnBinding.dialogTwoBtnPositiveBtn.setOnClickListener(this);
        return dialogTwoBtnBinding;
    }

    private ViewDataBinding getTwoBtnEditTextDialog() {
        setDialogAboveStatusBar();
        dialogTwoBtnEditTextBinding = DataBindingUtil
                .inflate(LayoutInflater.from(getContext()), R.layout.dialog_two_btn_edit_text, null, true);

        // TODO: 2020-01-21 입력 글자수 처리 param 받아서 처리하게 변경 필요
        if (popup.isIntegerForEditText()) {
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(7);
            dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setFilters(filterArray);
            dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setInputType(InputType.TYPE_CLASS_PHONE);
        }

        if (popup.isSecureTextType()) {
            dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setInputType(
                    InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            if (popup.getTag().equals(Constants.DIALOG_TAG_CONFIG_CAR_NUM)) {
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(SiteConstants.LIMIT_LENGTH_CAR_NUMBER);
                dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setFilters(filterArray);
                dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }

        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextTitle.setText(popup.getTitle());
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setText(popup.getContent());
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextNegativeBtn.setText(popup.getLabelNegativeBtn());
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextPositiveBtn.setText(popup.getLabelPositiveBtn());
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextTitle.setOnClickListener(this);
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextNegativeBtn.setOnClickListener(this);
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextPositiveBtn.setOnClickListener(this);
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent
                .setSelection(dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.getText().length());

        //가상 키보드에서 완료(done) 터치시 이벤트 감지 후 Positive 버튼 클릭 이벤트 처리
        dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextPositiveBtn);
                }
                return false;
            }
        });

        //키보드 표시
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        return dialogTwoBtnEditTextBinding;
    }

    private ViewDataBinding getSingleSelectionDialog() {
        setDialogAboveStatusBar();
        dialogSingleSelection = DataBindingUtil
                .inflate(LayoutInflater.from(getContext()), R.layout.dialog_list_single_selection, null, true);
        dialogSingleSelection.dialogListRadioBtnTitle.setText(popup.getTitle());
        dialogSingleSelection.dialogListRadioBtnNegativeBtn.setText(popup.getLabelNegativeBtn());
        dialogSingleSelection.dialogListRadioBtnNegativeBtn.setOnClickListener(this);

        //라디오버튼 리스트 다이얼로그에 확인 버튼 추가
        boolean isTwoBtn = false;
        if (popup.getType() == Popup.TYPE_RADIO_TWO_BTN_LIST) {
            dialogSingleSelection.dialogListRadioBtnPositiveBtn.setText(popup.getLabelPositiveBtn());
            dialogSingleSelection.dialogListRadioBtnPositiveBtn.setVisibility(View.VISIBLE);
            dialogSingleSelection.dialogListRadioBtnPositiveBtn.setOnClickListener(this);
            isTwoBtn = true;
        }

//        boolean isRadioBtnList = popup.getType() != Popup.TYPE_ONE_BTN_TITLE_CONTENT;
        boolean isRadioBtnList = true;

        final ArrayList<SelectionItem> list = popup.getSelectionItems();
        RecyclerView recyclerView = dialogSingleSelection.dialogOneBtnRecyclerView;
        PopupSelectionListAdapter adapter = new PopupSelectionListAdapter(getContext(), list, this, isRadioBtnList, isTwoBtn);
        recyclerView.setAdapter(adapter);

        return dialogSingleSelection;
    }

    private void startCountDown(int dismissSecond) {
        CountDownTimer countDownTimer = new CountDownTimer((dismissSecond + 2) * COUNT_DOWN_INTERVAL, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long l) {
                String label = getBtnLabelWithDismissSecond(popup.getLabelPositiveBtn(), (int) (l / 1000) - 1); //0초 카운트를 보여주기 위함
                dialogOneBtnBinding.dialogOneBtnBtn.setText(label);
            }

            @Override
            public void onFinish() {
                if (getDialog() != null && getDialog().isShowing()) {
                    dismissDialog(null);
                }
            }
        };
        countDownTimer.start();
    }

    private String getBtnLabelWithDismissSecond(String labelPositiveBtn, final int dismissSecond) {
        if (isAdded() && getActivity() != null) {
            labelPositiveBtn = getString(R.string.popup_btn_dismiss_second, labelPositiveBtn, String.valueOf(dismissSecond));
        }
        return labelPositiveBtn;
    }

    //statusBar 위에 그리기 (포지션이 아닌 허용 여부 설정)
    private void setDialogAboveStatusBar() {
        //LogHelper.e("setDialogAboveStatusBar() : " + getDialog().getWindow().getAttributes().toString());
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        getDialog().getWindow().setAttributes(windowLayoutParams);
        //LogHelper.e("setDialogAboveStatusBar() : " + getDialog().getWindow().getAttributes().toString());
    }

    private void setDialogPositionAboveStatusBar(Window window) {
        window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = DeviceUtil.convertDpToPx(getContext(), 0);
        params.y = DeviceUtil.convertDpToPx(getContext(), 20);
        window.setAttributes(params);
    }

    private void setFullScreen(Window window) {
        //LogHelper.e("setFullScreen()");
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void dismissDialog(Intent intent) {
        if (dialogTwoBtnEditTextBinding != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.getWindowToken(), 0);
            dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.clearFocus();
        }
        listener.onDismissPopupDialog(popup.getTag(), intent);
        LogHelper.e("isVisible : " + isVisible());
        //this.dismiss();
        this.dismissAllowingStateLoss();
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.dialog_two_btn_positive_btn:
            case R.id.dialog_two_btn_edit_text_positive_btn:
            case R.id.dialog_list_radio_btn_positive_btn:
                intent.putExtra(Constants.DIALOG_INTENT_KEY_PRESSED_POSITIVE_BTN, true);
                switch (popup.getType()) {
                    case Popup.TYPE_TWO_BTN_EDIT_TEXT:
                        intent.putExtra(Constants.DIALOG_INTENT_KEY_EDIT_TEXT,
                                String.valueOf(dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.getText()));
                        break;
                    case Popup.TYPE_RADIO_TWO_BTN_LIST:
                        PopupSelectionListAdapter adapter = (PopupSelectionListAdapter) dialogSingleSelection
                                .dialogOneBtnRecyclerView.getAdapter();
                        twoBtnRadioListSelectedItem = adapter.getSelectedItem();
                        intent.putExtra(Constants.DIALOG_INTENT_KEY_RADIO_BUTTON, twoBtnRadioListSelectedItem);
                        break;
                }
                dismissDialog(intent);
                break;

            case R.id.dialog_two_btn_negative_btn:
            case R.id.dialog_two_btn_edit_text_negative_btn:
            case R.id.dialog_list_radio_btn_negative_btn:
            case R.id.dialog_one_btn_btn:
            case R.id.dialog_container:
                dismissDialog(null);
                break;

            case R.id.dialog_two_btn_edit_text_title:
                if (((TextView) v).getText().equals(getString(R.string.configuration_service_server_ip))) {
                    if (dialogTwoBtnEditTextBinding != null) {
                        toggleIp = !toggleIp;
                        if (toggleIp) {
                            dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setText(getString(R.string.configuration_server_ip_idc));
                        } else {
                            dialogTwoBtnEditTextBinding.dialogTwoBtnEditTextContent.setText(getString(R.string.configuration_server_ip_ts));
                        }
                    }
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.dialog_container:
            case R.id.dialog_one_btn_content:
                dismissDialog(null);
                break;
        }
        return false;
    }

    @Override
    public void onListItemSelected(boolean isRadioBtnList, String itemName, int itemId, boolean isTwoBtn) {
        LogHelper.e("onListItemSelected() : " + isTwoBtn);
        Intent intent = new Intent();
        if (isRadioBtnList && !isTwoBtn) {
            LogHelper.e("onListItemSelected() : " + itemName + " / " + itemId);

            intent.putExtra(Constants.DIALOG_INTENT_KEY_RADIO_BUTTON, itemName);
            intent.putExtra(Constants.DIALOG_INTENT_KEY_RADIO_BUTTON_ID, itemId);
            dismissDialog(intent);
        } else if (isRadioBtnList && isTwoBtn) {
            twoBtnRadioListSelectedItem = itemName;
        }
    }
}
