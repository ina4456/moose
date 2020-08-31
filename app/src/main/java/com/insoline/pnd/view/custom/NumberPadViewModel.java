package com.insoline.pnd.view.custom;

import android.view.View;
import android.widget.Button;


/**
 * Created by seok-beomkwon on 2017. 9. 17..
 */

public class NumberPadViewModel {
	public interface NumberPadViewContract {
		void setNumberToTextView(String number);
		String getNumberFromTextView();
	}

	private NumberPadViewContract numberPadViewContract;
	private StringBuilder inputNumber;

	public NumberPadViewModel(NumberPadViewContract numberPadViewContract) {
		this.numberPadViewContract = numberPadViewContract;
		inputNumber = new StringBuilder();
	}

	public void onClick(View view) {
		String newNumber = ((Button)view).getText().toString();
		inputNumber = new StringBuilder(numberPadViewContract.getNumberFromTextView());

		int length = inputNumber.length();

		if(newNumber.equals("ac")) {
			if(length > 0){
				inputNumber.delete(0, length);
			}
		} else if(newNumber.equals("c")) {
			if(length > 0){
				inputNumber.delete(length-1, length);
			}
		} else {
			//Log.e("newNumber : ", "number : " + newNumber + " / length : " + length );
			if(length < 13) {
				inputNumber.append(newNumber);
			}

			if(length == 2 || length == 7){
				inputNumber.append("-");
			} else if (length == 3 || length == 8){
				String preCharater = inputNumber.substring(length-1, length);
				if(!preCharater.equals("-")){
					inputNumber.insert(length, "-");
				}
			}
		}
		numberPadViewContract.setNumberToTextView(inputNumber.toString());
	}
}
