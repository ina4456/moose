package com.insoline.pnd.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.databinding.ActivityWaitingCallBinding;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.model.WaitingCall;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.adapter.WaitingCallAdapter;
import com.insoline.pnd.view.fragment.PopupDialogFragment;

import java.util.ArrayList;


public class WaitingCallActivity extends BaseActivity implements WaitingCallAdapter.CallListCallback {

	private static final int START_INDEX = 1;
	private boolean hasMoreData = false;

	private ActivityWaitingCallBinding mBinding;
	private WaitingCallAdapter mWaitingCallListAdapter;
//	private MainViewModel mMainViewModel;

	public static void startActivity(Context context) {
		final Intent intent = new Intent(context, WaitingCallActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		mMainViewModel = new ViewModelProvider(this, new MainViewModel.Factory(getApplication()))
//				.get(MainViewModel.class);
//		subscribeMainViewModel(mMainViewModel);
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_waiting_call);
		mBinding.btnWaitingCallRefresh.setOnClickListener(this);

//		initToolbar();
//		requestWaitCallList(START_INDEX);
//		initRecyclerView();
//		showListOrEmptyMsgView();
	}

//	private void subscribeMainViewModel(MainViewModel mainViewModel) {
//		mainViewModel.getCallInfoLive().observe(this, new Observer<CallHistory>() {
//			@Override
//			public void onChanged(CallHistory call) {
//				LogHelper.e("onChanged()-call");
//				//콜 리스트에서 배차 요청을 하고 난 후, 배차 성공을 했다면, 해당 리스트를 닫는다.
//				switch (call.getCallStatus()) {
//					case Constants.CALL_STATUS_ALLOCATED:
//					case Constants.CALL_STATUS_BOARDED:
//					case Constants.CALL_STATUS_DRIVING:
//						finish();
//						break;
//				}
//			}
//		});
//	}

//	private void initToolbar() {
//		setSupportActionBar(mBinding.wcToolbar.toolbar);
//		mBinding.wcToolbar.ibtnActionButton.setImageResource(R.drawable.selector_bg_common_refresh_btn);
//		mBinding.wcToolbar.ibtnActionButton.setOnClickListener(this);
//		ActionBar ab = getSupportActionBar();
//		if (ab != null) {
//			ab.setTitle(getString(R.string.main_btn_waiting_call_list));
//			ab.setDisplayHomeAsUpEnabled(true);
//			ab.setDisplayShowHomeEnabled(true);
//			ab.setDisplayShowTitleEnabled(true);
//		}
//	}

	private void initRecyclerView() {
		LogHelper.e("initRecyclerView()");
		mWaitingCallListAdapter = new WaitingCallAdapter(this, new ArrayList(), this);
		mBinding.rvWaitingCall.setNestedScrollingEnabled(false);
		mBinding.rvWaitingCall.setAdapter(mWaitingCallListAdapter);
		mBinding.rvWaitingCall.setFocusable(false);
		mBinding.rvWaitingCall.setVerticalScrollBarEnabled(true);
		mBinding.rvWaitingCall.scrollTo(0, 0);
		mBinding.rvWaitingCall.addOnScrollListener(mScrollListener);
	}


	private void showListOrEmptyMsgView() {
		if (mWaitingCallListAdapter != null) {
			if (mWaitingCallListAdapter.getItemCount() <= 0) {
				mBinding.waitingCallEmptyText.setText(getString(R.string.waiting_call_empty));
				mBinding.waitingCallEmptyText.setVisibility(View.VISIBLE);
				mBinding.rvWaitingCall.setVisibility(View.GONE);
			} else {
				mBinding.waitingCallEmptyText.setVisibility(View.GONE);
				mBinding.rvWaitingCall.setVisibility(View.VISIBLE);
			}
		}
	}

	private void requestWaitCallList(int startIndex) {
//		startLoadingProgress();
//
//		Packets.WaitCallListType requestType = Packets.WaitCallListType.RequestAgain;
//		if (startIndex == START_INDEX) {
//			requestType = RequestFirstTime;
//			if (mWaitingCallListAdapter != null) {
//				mWaitingCallListAdapter.refreshData(null);
//			}
//		}
//
//		MutableLiveData<ResponseWaitCallListPacket> liveData = mMainViewModel.requestWaitingCallList(requestType, startIndex);
//		liveData.observe(this, new Observer<ResponseWaitCallListPacket>() {
//			@Override
//			public void onChanged(ResponseWaitCallListPacket response) {
//				LogHelper.e("responseWaitCallListPacket : " + response);
//				liveData.removeObserver(this);
//				finishLoadingProgress();
//
//				if (response != null) {
//					try {
//						ArrayList<CallHistory> waitingCallList = new ArrayList<>();
//						String[] callNumbers = response.getCallNumbers().split("\\|\\|");
//						String[] callReceiptDates = response.getCallReceiptDates().split("\\|\\|");
//						String[] callOrderCounts = response.getOrderCounts().split("\\|\\|");
//						String[] departures = response.getDepartures().split("\\|\\|");
//						String[] destinations = response.getDestinations().split("\\|\\|");
//						String[] distances = response.getDistances().split("\\|\\|");
//
//						LogHelper.e("callNumbers : " + callNumbers.length + " / callReceiptDates : " + callReceiptDates.length
//								+ " / callOrderCounts : " + callOrderCounts.length + " / departures : " + departures.length
//								+ " / destinations: " + destinations.length + " / distances : " + distances.length);
//
//						if (response.getWaitCallCount() > 0 && callNumbers.length > 0) {
//							hasMoreData = response.isHasMoreList();
//							LogHelper.e("hasMoreData : " + hasMoreData);
//
//							for (int i = 0; i < callNumbers.length; i++) {
//								CallHistory call = new CallHistory();
//								call.setCallNumber(Integer.parseInt(getStringFromArray(callNumbers, i)));
//								call.setCallReceivedDate(getStringFromArray(callReceiptDates, i));
//								call.setCallOrderCount(Integer.parseInt(getStringFromArray(callOrderCounts, i)));
//								call.setDeparturePoi(getStringFromArray(departures, i));
//								call.setDestinationPoi(getStringFromArray(destinations, i));
//								call.setDistance(Integer.valueOf(getStringFromArray(distances, i)));
//								waitingCallList.add(call);
//							}
//
//							if (startIndex == START_INDEX) {
//								mWaitingCallListAdapter.refreshData(waitingCallList);
//							} else {
//								mWaitingCallListAdapter.addData(waitingCallList);
//							}
//
//							showListOrEmptyMsgView();
//						} else {
//							showListOrEmptyMsgView();
//						}
//					} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});
	}

	private String getStringFromArray(String[] array, int index) {
		String result = "";
		try {
			result = array[index];
			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			return result;
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_waiting_call_refresh) {
			requestWaitCallList(START_INDEX);
		}
	}

	@Override
	public void onListItemSelected(WaitingCall call) {
		LogHelper.e("selected Item : " + call.toString());
//		call.setCallStatus(Constants.CALL_STATUS_RECEIVING);
//		mMainViewModel.updateCallInfo(call);
//		CallReceivingActivity.startActivity(this, true);
	}

	RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);

			if (hasMoreData) {
				LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
				if (layoutManager != null) {
					int totalItemCount = layoutManager.getItemCount();
					int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();

					if (lastVisible >= totalItemCount - 1) {
						LogHelper.e("lastVisibled : " + totalItemCount);
						requestWaitCallList(totalItemCount + 1);
					}
				}
			}
		}
	};

	private void showFailurePopup() {
		Popup popup = new Popup
				.Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_ALLOCATION_FAILURE)
				.setContent(getString(R.string.alloc_msg_failed_call))
				.setBtnLabel(getString(R.string.popup_btn_confirm), null)
				.setDismissSecond(3)
				.build();
		showPopupDialog(popup);
	}

	@Override
	public void onDismissPopupDialog(String tag, Intent intent) {

	}
}

