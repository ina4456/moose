package com.insoline.pnd.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.insoline.pnd.R;
import com.insoline.pnd.databinding.ViewNumberPadBinding;

/**
 * Created by seok-beomkwon on 2017. 9. 17..
 */

public class NumberPadView extends LinearLayout implements NumberPadViewModel.NumberPadViewContract {

	private ViewNumberPadBinding mBinding;
	private TextView textView;

	public NumberPadView(Context context) {
		super(context);
		initView();
	}

	public NumberPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public NumberPadView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		initView();
	}

	private void initView(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		mBinding = DataBindingUtil.inflate(inflater, R.layout.view_number_pad, this, false);

		NumberPadViewModel numberPadViewModel = new NumberPadViewModel(this);
		mBinding.setViewModel(numberPadViewModel);

		addView(mBinding.getRoot());
	}

	public void setTextView(TextView textView) {
		this.textView = textView;
	}

	public void setEnabledButtons(boolean isEnabled) {
		mBinding.number0.setEnabled(isEnabled);
		mBinding.number1.setEnabled(isEnabled);
		mBinding.number2.setEnabled(isEnabled);
		mBinding.number3.setEnabled(isEnabled);
		mBinding.number4.setEnabled(isEnabled);
		mBinding.number5.setEnabled(isEnabled);
		mBinding.number6.setEnabled(isEnabled);
		mBinding.number7.setEnabled(isEnabled);
		mBinding.number8.setEnabled(isEnabled);
		mBinding.number9.setEnabled(isEnabled);
		mBinding.clearAll.setEnabled(isEnabled);
		mBinding.clearOne.setEnabled(isEnabled);
	}

	@Override
	public void setNumberToTextView(String number) {
		textView.setText(number);
	}

	@Override
	public String getNumberFromTextView() {
		return textView.getText().toString();
	}
}
