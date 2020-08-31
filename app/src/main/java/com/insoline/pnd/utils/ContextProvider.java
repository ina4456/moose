package com.insoline.pnd.utils;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by seok-beomkwon on 2017. 10. 18..
 */

public class ContextProvider extends ContextWrapper {
	private static ContextProvider instance;

	public ContextProvider(Context base) {
		super(base);
	}
	public static ContextProvider getInstance(Context context){
		if (instance == null) {
			instance = new ContextProvider(context);
		}
		return instance;
	}
}
