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
import com.insoline.pnd.databinding.ActivityWaitingZoneBinding;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.model.WaitingZone;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.adapter.WaitingZoneAdapter;
import com.insoline.pnd.view.fragment.PopupDialogFragment;

import java.util.ArrayList;


public class WaitingZoneActivity extends BaseActivity implements WaitingZoneAdapter.WaitingZoneListCallback {

	private static final int START_INDEX = 1;
	private boolean hasMoreData = false;
	private boolean isLoading = false;
//	private MainViewModel mViewModel;
	private ActivityWaitingZoneBinding mBinding;
	private WaitingZoneAdapter mWaitingZoneAdapter;

	public static void startActivity(Context context) {
		final Intent intent = new Intent(context, WaitingZoneActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.e("onCreate()");
//		mViewModel = new ViewModelProvider(this, new MainViewModel.Factory(getApplication()))
//				.get(MainViewModel.class);
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_waiting_zone);
		mBinding.setLifecycleOwner(this);
		mBinding.waitingZoneRefreshBtn.setOnClickListener(this);

//		subscribeViewModel(mViewModel);
//		requestWaitZoneList(START_INDEX);
//		initRecyclerView();
//		showListOrEmptyMsgView();
	}

//	private void subscribeViewModel(MainViewModel mainViewModel) {
//		if (mainViewModel != null) {
//			mainViewModel.getCallInfoLive().observe(this, new Observer<CallHistory>() {
//				@Override
//				public void onChanged(CallHistory call) {
//					LogHelper.e("onChanged-CallHistory");
//					if (call != null) {
//						int callStatus = call.getCallStatus();
//						LogHelper.e("onChanged-CallHistory : " + callStatus);
//						switch (callStatus) {
//							case Constants.CALL_STATUS_DRIVING:
//							case Constants.CALL_STATUS_BOARDED:
//							case Constants.CALL_STATUS_ALLOCATED:
//								finish();
//								break;
//
//							case Constants.CALL_STATUS_VACANCY:
//								requestWaitZoneList(START_INDEX);
//								break;
//						}
//					}
//				}
//			});
//		}
//	}

	private void initRecyclerView() {
		mWaitingZoneAdapter = new WaitingZoneAdapter(this, new ArrayList(), this);
		mBinding.rvWaitingZoneList.setNestedScrollingEnabled(false);
		mBinding.rvWaitingZoneList.setAdapter(mWaitingZoneAdapter);
		mBinding.rvWaitingZoneList.setFocusable(false);
		mBinding.rvWaitingZoneList.setVerticalScrollBarEnabled(true);
		mBinding.rvWaitingZoneList.scrollTo(0, 0);
		mBinding.rvWaitingZoneList.addOnScrollListener(mScrollListener);
	}

	private void showListOrEmptyMsgView() {
		if (mWaitingZoneAdapter != null) {
			if (mWaitingZoneAdapter.getItemCount() <= 0) {
				mBinding.waitingZoneEmptyText.setText(getString(R.string.waiting_zone_empty));
				mBinding.waitingZoneEmptyText.setVisibility(View.VISIBLE);
				mBinding.rvWaitingZoneList.setVisibility(View.GONE);
			} else {
				mBinding.waitingZoneEmptyText.setVisibility(View.GONE);
				mBinding.rvWaitingZoneList.setVisibility(View.VISIBLE);
			}
		}
	}

	private void requestWaitZoneList(int startIndex) {
		LogHelper.e("requestWaitZoneList : " + startIndex);
		isLoading = true;
		startLoadingProgress();

		if (startIndex == START_INDEX) {
			if (mWaitingZoneAdapter != null) {
				mWaitingZoneAdapter.refreshData(null);
			}
		}

//		MutableLiveData<ResponseWaitAreaListPacket> liveData = mViewModel.requestWaitArea(Packets.WaitAreaRequestType.Normal, startIndex);
//		liveData.observe(this, new Observer<ResponseWaitAreaListPacket>() {
//			@Override
//			public void onChanged(ResponseWaitAreaListPacket response) {
//				LogHelper.e("responseWaitCallListPacket : " + response);
//				liveData.removeObserver(this);
//				isLoading = false;
//				finishLoadingProgress();
//
//				if (response != null) {
//					try {
//						ArrayList<WaitingZone> waitingZoneList = new ArrayList<>();
//						String[] waitAreaIds = response.getWaitAreaIds().split("\\|\\|");
//						String[] waitAreaNames = response.getWaitAreaNames().split("\\|\\|");
//						String[] numberOfCarsInAreas = response.getNumberOfCarInAreas().split("\\|\\|");
//						String[] isAvailableWaits = response.getIsAvailableWaits().split("\\|\\|");
//						String[] myWaitNumbers = response.getMyWaitNumbers().split("\\|\\|");
//
//						LogHelper.e("waitAreaIds : " + waitAreaIds.length + " / waitAreaNames : " + waitAreaNames.length
//								+ " / numberOfCarsInAreas : " + numberOfCarsInAreas.length + " / isAvailableWaits : " + isAvailableWaits.length
//								+ " / myWaitNumbers: " + myWaitNumbers.length);
//
//						if (response.getTotalCount() > 0 && waitAreaIds.length > 0) {
//							hasMoreData = response.isHasMoreData();
//							LogHelper.e("hasMoreData : " + hasMoreData);
//
//							for (int i = 0; i < waitAreaIds.length; i++) {
//								WaitingZone waitZone = new WaitingZone();
//								waitZone.setWaitingZoneId(getStringFromArray(waitAreaIds, i));
//								waitZone.setWaitingZoneName(getStringFromArray(waitAreaNames, i));
//								waitZone.setNumberOfCarsInAreas(Integer.parseInt(getStringFromArray(numberOfCarsInAreas, i)));
//								waitZone.setAvailableWait(getStringFromArray(isAvailableWaits, i).equals("Y"));
//								String myWaitingOrder = getStringFromArray(myWaitNumbers, i);
//								waitZone.setMyWaitingOrder(Integer.parseInt(myWaitingOrder.equals("") ? "0" : myWaitingOrder));
//
//								waitingZoneList.add(waitZone);
//							}
//
//							if (startIndex == START_INDEX) {
//								mWaitingZoneAdapter.refreshData(waitingZoneList);
//								boolean hasOrder = false;
//								for (WaitingZone waitingZone : waitingZoneList) {
//									hasOrder = waitingZone.getMyWaitingOrder() != 0;
//									if (hasOrder) {
//										break;
//									}
//								}
//								LogHelper.e("hasorder : " + hasOrder);
//								if (!hasOrder) {
//									mViewModel.setWaitingZone(null, false);
//								}
//							} else {
//								mWaitingZoneAdapter.addData(waitingZoneList);
//							}
//
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
		if (view.getId() == R.id.waiting_zone_refresh_btn) {
			requestWaitZoneList(START_INDEX);
		}
	}

	@Override
	public void onListItemSelected(int index, WaitingZone item, boolean isRequest) {
		//LogHelper.e("item : " + item + " / " + isRequest);
		if (item != null) {
//			if (isRequest) {
//				MutableLiveData<ResponseWaitAreaDecisionPacket> liveData = mViewModel.requestWaitDecision(item.getWaitingZoneId());
//				liveData.observe(this, new Observer<ResponseWaitAreaDecisionPacket>() {
//					@Override
//					public void onChanged(ResponseWaitAreaDecisionPacket response) {
//						liveData.removeObserver(this);
//						LogHelper.e("대기 결정 응답 :  " + response);
//						if (response.getWaitProcType() == Packets.WaitProcType.Success) {
//							mViewModel.setWaitingZone(item, true);
//							requestWaitZoneList(START_INDEX);
//						} else {
//							showFailurePopup(isRequest);
//						}
//					}
//				});
//			} else {
//				MutableLiveData<ResponseWaitAreaCancelPacket> liveData = mViewModel.requestWaitCancel(item.getWaitingZoneId());
//				liveData.observe(this, new Observer<ResponseWaitAreaCancelPacket>() {
//					@Override
//					public void onChanged(ResponseWaitAreaCancelPacket response) {
//						liveData.removeObserver(this);
//						LogHelper.e("대기 취소 응답 :  " + response);
//						if (response.getWaitCancelType() == Packets.WaitCancelType.Success
//								|| response.getWaitCancelType() == Packets.WaitCancelType.AlreadyCancel) {
//							mViewModel.setWaitingZone(item, false);
//							requestWaitZoneList(START_INDEX);
//						} else {
//							showFailurePopup(isRequest);
//						}
//					}
//				});
//			}
		}
	}


	RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);

			if (hasMoreData) {
				LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
				if (layoutManager != null) {
					final int totalItemCount = layoutManager.getItemCount();
					int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();

					if (lastVisible >= totalItemCount - 1 && !isLoading) {
						LogHelper.e("lastVisibled : " + totalItemCount);

						mBinding.rvWaitingZoneList.post(new Runnable() {
							@Override
							public void run() {
								//layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
								requestWaitZoneList(totalItemCount + 1);
							}
						});
					}
				}
			}
		}
	};


	private void showFailurePopup(boolean isRequest) {
		Popup popup = new Popup
				.Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_ALLOCATION_FAILURE)
				.setContent(isRequest ? getString(R.string.wz_msg_request_failed)
						: getString(R.string.wz_msg_cancel_failed))
				.setBtnLabel(getString(R.string.popup_btn_confirm), null)
				.setDismissSecond(3)
				.build();
		showPopupDialog(popup);
	}

	@Override
	public void onDismissPopupDialog(String tag, Intent intent) {

	}
}
