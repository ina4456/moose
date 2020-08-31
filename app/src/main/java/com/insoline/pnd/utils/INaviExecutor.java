package com.insoline.pnd.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.insoline.pnd.SiteConstants;

import java.util.List;

/**
 * Created by Mihoe on 2016-09-20.
 */
public class INaviExecutor {
	private static final String INAVI_REQUEST_RUNNAVI = SiteConstants.INAVI_REQUEST_RUNNAVI;
	private static final String INAVI_REQUEST_CURRENTON = SiteConstants.INAVI_REQUEST_CURRENTON;
	private static final String INAVI_REQUEST_RUNROUTENOW = SiteConstants.INAVI_REQUEST_RUNROUTENOW;
	private static final String INAVI_REQUEST_ROUTECANCEL = SiteConstants.INAVI_REQUEST_ROUTECANCEL;
	private static final String INAVI_REQUEST_STARTINAVI = SiteConstants.INAVI_REQUEST_STARTINAVI;
	private static final String InaviPackageName = SiteConstants.INAVI_PACKAGE_NAME;

	public static boolean isNavigating = false; // 승차 신호가 올라올 경우 경로 취소를 하기 위함.

	public static void startNavigationNow(Context context, String destination, double latitude, double longitute) {
		if (!isINaviRunning(context)) {
			run(context);
		}

		Intent intent = new Intent();
		intent.setAction(INAVI_REQUEST_RUNROUTENOW);
		intent.putExtra("GOAL1_NAME", destination);
		intent.putExtra("GOAL1_X", longitute);
		intent.putExtra("GOAL1_Y", latitude);
		context.sendBroadcast(intent);

		//context.startService(new Intent(context, AlwaysOnService.class));
		isNavigating = true;
	}

	public static void cancelNavigation(Context context) {
		Intent intent = new Intent();
		intent.setAction(INAVI_REQUEST_ROUTECANCEL);
		context.sendBroadcast(intent);

		//execute(context);
		isNavigating = false;
	}

	public static void run(Context context) {
		LogHelper.e("INaviExecutor run ");
		Intent intent = new Intent();
		intent.setAction(INAVI_REQUEST_RUNNAVI);
		context.sendBroadcast(intent);

		execute(context);
	}

	public static void currentOn(Context context) {
		Intent intent = new Intent();
		intent.setAction(INAVI_REQUEST_CURRENTON);
		context.sendBroadcast(intent);

		execute(context);
	}

	private static void execute(Context context) {
		startInavi(context);

		//context.startService(new Intent(context, AlwaysOnService.class));
	}

	// 아이나비 실행 함수 예제
	private static void startInavi(Context context) {
		if (isINaviRunning(context)) {
			// 아이나비 실행된 상태
			Intent intent = new Intent();
			intent.setAction(INAVI_REQUEST_STARTINAVI);
			intent.putExtra("EXTTYPE", "ExtCurrentOn");
			context.sendBroadcast(intent);
		} else {
			// 아이나비 미실행 상태
			Intent intent = context.getPackageManager().getLaunchIntentForPackage(InaviPackageName);
			if (intent != null) {
				context.startActivity(intent);
			}
		}
	}

	// 아이나비 실행 여부 체크 함수 예제
	private static boolean isINaviRunning(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) { // 11
			final ActivityManager activityManager =(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			final List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

			ActivityManager.RecentTaskInfo recentTaskInfo = null;
			int taskInfoCount = recentTasks.size();

			for (int i = taskInfoCount - 1; i >= 0; i--) {
				recentTaskInfo = recentTasks.get(i);

				if(recentTaskInfo != null && recentTaskInfo.id > -1) {
					if (recentTaskInfo.baseIntent != null) {
						String packageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
						if (packageName.equals(InaviPackageName) == true) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
