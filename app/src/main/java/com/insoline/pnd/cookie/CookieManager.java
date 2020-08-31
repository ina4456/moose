package com.insoline.pnd.cookie;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import androidx.annotation.Nullable;

import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.StringUtil;

import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class CookieManager {
	private static final String TAG = CookieManager.class.getName();
	private static final String JSESSION_ID = "JSESSIONID";

	public static void setCookieManager(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (CookieHandler.getDefault() == null)
				CookieHandler.setDefault(new java.net.CookieManager());
			android.webkit.CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {

				@TargetApi(Build.VERSION_CODES.LOLLIPOP)
				@Override
				public void onReceiveValue(Boolean value) {
					android.webkit.CookieManager.getInstance().flush();
				}
			});
		} else {
			CookieSyncManager.createInstance(context.getApplicationContext());
			android.webkit.CookieManager.getInstance().removeAllCookie();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LogHelper.e(e.getMessage());
			}
			CookieSyncManager.getInstance().startSync();
			if (CookieHandler.getDefault() == null)
				CookieHandler.setDefault(new java.net.CookieManager());
		}
		android.webkit.CookieManager.getInstance().setAcceptCookie(true);
	}

	public static void setCookieSync(Context context, @Nullable final String domain) throws RuntimeException {
		if (StringUtil.isEmptyString(domain))
			return;
		if (CookieHandler.getDefault() == null) {
			if (context != null) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					CookieSyncManager.createInstance(context.getApplicationContext());
					CookieSyncManager.getInstance().startSync();
				}
				CookieHandler.setDefault(new java.net.CookieManager());
			} else {
				throw new RuntimeException("CookieManager is not set");
			}
		}

		CookieStore cookieStore = ((java.net.CookieManager) CookieHandler.getDefault()).getCookieStore();
		URI baseUri = null;
		try {
			baseUri = new URI(domain);
		} catch (URISyntaxException e) {
			return;
		}

		String curCookie = "";
		String preCookie = android.webkit.CookieManager.getInstance().getCookie(domain);
		List<HttpCookie> httpCookies = cookieStore.get(baseUri);
		if (httpCookies == null || httpCookies.size() == 0)
			return;
		LogHelper.d(TAG, "CookieManager httpCookies : " + httpCookies);
		for (HttpCookie item : httpCookies) {
			if (item.getName().equals(JSESSION_ID)) {
				curCookie = item.toString();
				break;
			}
		}
		LogHelper.d(TAG, "CookieManager curCookie : " + curCookie);
		if (StringUtil.isEmptyString(curCookie) || !curCookie.contains(JSESSION_ID)) {
			return;
		}
		if (StringUtil.isEmptyString(preCookie) || (!StringUtil.isEmptyString(preCookie) && !preCookie.equals(curCookie))) {
			new setCookie(domain, curCookie).start();
		}

	}

	public static class setCookie extends Thread {
		private final String domain;
		private final String cookie;

		public setCookie(final String domain, final String cookie) {
			this.domain = domain;
			this.cookie = cookie;
		}

		@Override
		public void run() {
			Looper.prepare();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				android.webkit.CookieManager.getInstance().setCookie(domain, cookie, new ValueCallback<Boolean>() {

					@TargetApi(Build.VERSION_CODES.LOLLIPOP)
					@Override
					public void onReceiveValue(Boolean value) {
						android.webkit.CookieManager.getInstance().flush();
					}
				});
			} else {
//				CookieManager.getInstance().removeAllCookie();
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					LogHelper.e(TAG, e.getMessage());
//				}

				android.webkit.CookieManager.getInstance().setCookie(domain, cookie);
				CookieSyncManager.getInstance().sync();
			}
			Looper.loop();
		}

	}

	public static void clearCookieSync() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			android.webkit.CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {

				@TargetApi(Build.VERSION_CODES.LOLLIPOP)
				@Override
				public void onReceiveValue(Boolean value) {
					android.webkit.CookieManager.getInstance().flush();
				}
			});
		} else {
			android.webkit.CookieManager.getInstance().removeAllCookie();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LogHelper.e(TAG, e.getMessage());
			}
			CookieSyncManager.getInstance().sync();
		}
	}

	public static void startCookieSync() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().startSync();
		}
	}

	public static void stopCookieSync() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().stopSync();
		}
	}

	public static void destroyCookieSync() {
		if (android.webkit.CookieManager.getInstance().hasCookies()) {
			clearCookieSync();
		}
		stopCookieSync();
	}

	public static String getCookie(String domain) {
		return android.webkit.CookieManager.getInstance().getCookie(domain);
	}

}
