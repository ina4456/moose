package com.insoline.pnd.service;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseApplication;
import com.insoline.pnd.config.ConfigLoader;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.define.PreferenceDefine;
import com.insoline.pnd.model.WaitingZone;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.PreferenceUtil;

import java.util.HashMap;


public class FloatingViewService extends Service implements View.OnTouchListener, View.OnClickListener {

	private WindowManager windowManager;
	private View statusView;
	private float offsetX;
	private float offsetY;
	private int originalXPos;
	private int originalYPos;
	private boolean moving;
	private View topLeftView;
	private final int touchSlop = 10;

	private ConfigLoader mConfigLoader;
	private PreferenceUtil mPreferenceUtil;

//	private Repository mRepository;
//	private CallHistory mCall;

	private RelativeLayout rlMainBtn;
	private TextView tvCarNumber;
	private Button btnBoardingOrAlighting;


	private final IBinder binder = new FloatingViewService.LocalBinder();
	public class LocalBinder extends Binder {
		public FloatingViewService getService() {
			return FloatingViewService.this;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
//		super.onBind(intent);
		LogHelper.d(">> onBind()");
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogHelper.e("onCreate()");

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (inflater != null) {
			statusView = inflater.inflate(R.layout.view_floating_status, null, false);
			statusView.setOnTouchListener(this);

			rlMainBtn = statusView.findViewById(R.id.rl_main_btn);
			tvCarNumber = statusView.findViewById(R.id.tv_car_number);
			btnBoardingOrAlighting = statusView.findViewById(R.id.btn_boarding_or_alighting);

			rlMainBtn.setOnClickListener(this);
			rlMainBtn.setOnTouchListener(this);
			btnBoardingOrAlighting.setOnClickListener(this);
			btnBoardingOrAlighting.setOnTouchListener(this);
//			btnCallPassenger.setOnClickListener(this);
//			btnCallPassenger.setOnTouchListener(this);

			mPreferenceUtil = new PreferenceUtil(this);
			mConfigLoader = ((BaseApplication)getApplication()).getConfigLoader();
			tvCarNumber.setText(String.valueOf(mConfigLoader.getCarId()));

//			mRepository = ((BaseApplication)getApplication()).getRepository();
//			subscribeCallInfo(mRepository);
			//addStatusView();

		}
	}

//	private void subscribeCallInfo(Repository repository) {
//
//		MainApplication mainApplication = (MainApplication) getApplication();
//
//		repository.getConfigLive().observe(this, new Observer<Configuration>() {
//			@Override
//			public void onChanged(Configuration configuration) {
//				LogHelper.e("onChanged-configuration : " + configuration.toString());
//				mConfig = configuration;
//				WindowManager.LayoutParams params = (WindowManager.LayoutParams) statusView.getLayoutParams();
//				if (params != null) {
//					params.x = configuration.getFloatingBtnLastX();
//					params.y = configuration.getFloatingBtnLastY();
//					windowManager.updateViewLayout(statusView, params);
//				}
//
//			}
//		});
//
//		PersonalConfiguration personalConfig = repository.getPersonalConfig(mConfig.getDriverPhoneNumber());
//		repository.getCallInfoLive().observe(FloatingViewService.this, new Observer<CallHistory>() {
//			@Override
//			public void onChanged(CallHistory call) {
//				LogHelper.e("onChanged-call : ");
//				if (call != null) {
//					LogHelper.e("onChanged-call : " + call.toString());
//					mCall = call;
//					setStatusImage(getResourceIdFromStatusString(call.getCallStatus()));
//
//					mBinding.rlMainBtn.setVisibility(personalConfig.isUseFBtnEnteringApp() ? View.VISIBLE : View.GONE);
//					if (personalConfig.isUseFBtnBoardingAlighting()) {
//						setVisibilityBoardingAlightingBtn(call.getCallStatus());
//					}
//					if (personalConfig.isUseFBtnCall()) {
//						setVisibilityCallButton(call);
//					}
//
//					if (call.getCallStatus() == Constants.CALL_STATUS_ALIGHTED) {
//						mainApplication.enterApplication();
//					} else if (call.getCallStatus() == Constants.CALL_STATUS_ALLOCATED) {
//						LogHelper.e("wasBackground : " + mainApplication.wasBackground());
//						if (mainApplication.wasBackground()) {
//							mainApplication.enterApplication();
//							mainApplication.setWasBackground(false);
//						}
//					}
//				}
//			}
//		});
//
//		/*repository.getTachometerDataLive().observe(FloatingViewService.this, new Observer<TachometerData>() {
//			@Override
//			public void onChanged(TachometerData tachometerData) {
//				LogHelper.e("initialized : " + initialized);
//				if (tachometerData != null && initialized) {
//					int status = tachometerData.getStatus();
//					LogHelper.e("onChanged-Tachometer : " + tachometerData.getStatusStr());
//					if (status == TachometerData.STATUS_VACANCY || status == TachometerData.STATUS_DRIVING
//							|| status == TachometerData.STATUS_PAYMENT || status == TachometerData.STATUS_PAYMENT_EXCLUDE_KUMHO) {
//						boolean isBoarding = status != TachometerData.STATUS_VACANCY;
//						changeCallStatus(isBoarding);
//					}
//				} else {
//					initialized = true;
//				}
//			}
//		});*/
//
//	}

	//탑승 or 하차 버튼 표시 여부
	private void setVisibilityBoardingAlightingBtn(int status) {
		LogHelper.e("setVisibilityBoardingAlightingBtn() : " + status);
//		switch (status) {
//			case Constants.CALL_STATUS_ALLOCATED:
//				mBinding.btnBoardingOrAlighting.setVisibility(SiteConstants.USE_BOARDING_ALIGHTING_BTN ? View.VISIBLE : View.GONE);
//				mBinding.btnBoardingOrAlighting.setText(getString(R.string.main_status_btn_boarding_s));
//				break;
//
//			case Constants.CALL_STATUS_BOARDED:
//			case Constants.CALL_STATUS_BOARDED_WITHOUT_DESTINATION:
//				mBinding.btnBoardingOrAlighting.setText(getString(R.string.main_status_btn_alighting_s));
//				Drawable backgroundDrawable;
//				if(Build.VERSION.SDK_INT >= 21){
//					backgroundDrawable = getResources().getDrawable(R.drawable.selector_bg_floating_btn_alighting, getTheme());
//				} else {
//					backgroundDrawable = getResources().getDrawable(R.drawable.selector_bg_floating_btn_alighting);
//				}
//				mBinding.btnBoardingOrAlighting.setBackground(backgroundDrawable);
//				mBinding.btnBoardingOrAlighting.setTextColor(getResources().getColorStateList(R.color.selector_tc_floating_alight_btn));
//				mBinding.btnBoardingOrAlighting.setVisibility(SiteConstants.USE_BOARDING_ALIGHTING_BTN ? View.VISIBLE : View.GONE);
//				mBinding.btnCallPassenger.setVisibility(View.GONE);
//				break;
//
//			default:
//				mBinding.btnBoardingOrAlighting.setVisibility(View.GONE);
//				break;
//		}
	}

	//전화 버튼 표시 여부
//	private void setVisibilityCallButton(CallHistory call) {
//		LogHelper.e("setVisibilityCallButton()");
//		if (isNeedToDisplayCallButton(call)) {
//			mBinding.btnCallPassenger.setVisibility(View.VISIBLE);
//		} else {
//			mBinding.btnCallPassenger.setVisibility(View.GONE);
//		}
//	}
//
//	//전화 버튼 표시 여부
//	private boolean isNeedToDisplayCallButton(CallHistory call) {
//		if (call != null) {
//			String passengerPhoneNumber = call.getPassengerPhoneNumber();
//			int callStatus = call.getCallStatus();
//			LogHelper.e("passengerPhoneNumber : " + passengerPhoneNumber);
//			LogHelper.e("callStatus : " + callStatus);
//
//			if (passengerPhoneNumber != null && !passengerPhoneNumber.isEmpty() && !passengerPhoneNumber.equals("-1")) {
//				if (callStatus == Constants.CALL_STATUS_ALLOCATED) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		saveLastPosition();
		//windowManager.removeView(statusView);
		//windowManager.removeView(topLeftView);
		this.stopSelf();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		LogHelper.e("onTaskRemoved");
	}

	private void addStatusView() {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.START | Gravity.TOP;

		saveLastPosition();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		windowManager.addView(statusView, params);

		topLeftView = new View(this);
		WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		topLeftParams.gravity = Gravity.START | Gravity.TOP;
		topLeftParams.x = 0;
		topLeftParams.y = 0;
		topLeftParams.width = 0;
		topLeftParams.height = 0;
		windowManager.addView(topLeftView, topLeftParams);
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_boarding_or_alighting:
				boolean isBoarding = btnBoardingOrAlighting.getText().equals(getString(R.string.main_status_btn_boarding_s));
				changeCallStatus(isBoarding);
				break;

			case R.id.rl_main_btn:
				((BaseApplication)getApplication()).enterApplication();
				break;

//			case R.id.btn_call_passenger:
//				LogHelper.e("call btn clicked");
//				try {
//					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mCall.getPassengerPhoneNumber()));
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(intent);
//				} catch (ActivityNotFoundException e) {
//					e.printStackTrace();
//				}
//				break;
		}
	}

	private void changeCallStatus(boolean isBoarding) {
		LogHelper.e("탑승 여부 : " + isBoarding);

		//탑승
//		if (isBoarding) {
//			mRepository.changeCallStatus(Constants.CALL_STATUS_BOARDED);
//			PersonalConfiguration personalConfig = mRepository.getPersonalConfig(mConfig.getDriverPhoneNumber());
//			if(personalConfig.isUseAutoRoutingToDestination()) {
//				((MainApplication)getApplication()).enterApplication();
//			}
//		//하차
//		} else {
//			mRepository.changeCallStatus(Constants.CALL_STATUS_VACANCY);
//		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			float x = event.getRawX();
			float y = event.getRawY();

			moving = false;

			int[] location = new int[2];
			statusView.getLocationOnScreen(location);

			originalXPos = location[0];
			originalYPos = location[1];

			offsetX = originalXPos - x;
			offsetY = originalYPos - y;

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			int[] topLeftLocationOnScreen = new int[2];
			topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

			float x = event.getRawX();
			float y = event.getRawY();

			WindowManager.LayoutParams params = (WindowManager.LayoutParams) statusView.getLayoutParams();

			int newX = (int) (offsetX + x);
			int newY = (int) (offsetY + y);

			if (Math.abs(newX - originalXPos) < 50 && Math.abs(newY - originalYPos) < 50 && !moving) {
				return false;
			}

			params.x = newX - (topLeftLocationOnScreen[0]);
			params.y = newY - (topLeftLocationOnScreen[1]);

			windowManager.updateViewLayout(statusView, params);
			moving = true;

		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (moving && !isInTouchSlop(offsetX + event.getRawX(), offsetY + event.getRawY())) {
				saveLastPosition();
				return true;
			}
		}

		return false;
	}

	private boolean isInTouchSlop(float newX, float newY) {
		if (Math.abs(newX - originalXPos) > touchSlop || Math.abs(newY - originalYPos) > touchSlop) {
			return false;
		}
		return true;
	}

	//마지막 위치 저장
	private void saveLastPosition() {
		LogHelper.e("saveLastPosition");

		WindowManager.LayoutParams params = (WindowManager.LayoutParams) statusView.getLayoutParams();
		if (params != null) {
			HashMap<String, String> lastPosition = new HashMap<>();
			lastPosition.put("lastX", String.valueOf(params.x));
			lastPosition.put("lastY", String.valueOf(params.y));

			mPreferenceUtil.setHashMap(PreferenceDefine.PREF_FLOATING_LAST_POSITION, lastPosition);
		}
	}

	private int getResourceIdFromStatusString(int status) {
		int resId = -1;
		switch (status) {
			case Constants.CALL_STATUS_DRIVING:
//			case Constants.CALL_STATUS_PAYMENT:
//			case Constants.CALL_STATUS_EXTRA_CHARGE:
			case Constants.CALL_STATUS_BOARDED:
				resId = R.drawable.fab_riding;
				break;
			case Constants.CALL_STATUS_WORKING:
			case Constants.CALL_STATUS_VACANCY:
//				WaitingZone waitingZone = mRepository.getWaitingZone();
//				LogHelper.e("waitingZone : " + waitingZone);
//				if (waitingZone != null) {
//					resId = R.drawable.fab_standby;
//				} else {
//					resId = R.drawable.fab_empty;
//				}

				break;
			case Constants.CALL_STATUS_RESTING:
				resId = R.drawable.fab_break;
				break;
			case Constants.CALL_STATUS_ALLOCATED:
				resId = R.drawable.fab_dispatch;
				break;
		}
		return resId;
	}

	private void setStatusImage(final int resId) {
		if (resId != -1) {
//			imgStatus.post(new Runnable() {
//				@Override
//				public void run() {
//					imgStatus.setImageResource(resId);
//				}
//			});
		}
	}
}
