package com.insoline.pnd.view.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.databinding.ItemOperationHistoryDetailListBinding;
import com.insoline.pnd.model.CallHistory;
import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.utils.LogHelper;

import java.util.ArrayList;


public class OperationHistoryDetailListAdapter extends RecyclerView.Adapter<OperationHistoryDetailListAdapter.ViewHolder> {

	private ArrayList<CallHistory> mItems;
	private Context mContext;

	public OperationHistoryDetailListAdapter(Context context, ArrayList<CallHistory> items) {
		mContext = context;
		mItems = items;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_operation_history_detail_list, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.bindBodyData(mItems.get(position));
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	public void refreshData(ArrayList<CallHistory> runHistories) {
		if (runHistories != null) {
			mItems = runHistories;
		} else {
			mItems.clear();
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<CallHistory> callHistories) {
		int curSize = getItemCount();
		mItems.addAll(callHistories);
		notifyItemRangeInserted(curSize, callHistories.size());
	}


	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		ItemOperationHistoryDetailListBinding mBinding;

		public ViewHolder(View view) {
			super(view);
			mBinding = DataBindingUtil.bind(view);
			if (mBinding != null) {
				mBinding.clCallPassenger.setOnClickListener(this);
			}
		}

		private void bindBodyData(CallHistory item) {
			mBinding.tvDate.setText(item.getDate());
			mBinding.tvOrderStatus.setText(item.getCallTypeStr());
			mBinding.tvDeparture.setText(item.getDeparture());

			String destination = item.getDestination();
			if (destination == null || destination.equals("null") || destination.isEmpty()) {
				destination = mContext.getString(R.string.allocation_text_empty_destination);
			}
			mBinding.tvDestination.setText(destination);

			String startTime = item.getStartTime();
			if (startTime == null || startTime.equals("null") || startTime.isEmpty()) {
				startTime = "";
			}
			mBinding.tvStartTime.setText(startTime);

			String endTime = item.getEndTime();
			if (endTime == null || endTime.equals("null") || endTime.isEmpty()) {
				endTime = "";
			}
			mBinding.tvEndTime.setText(endTime);

			int colorId = R.color.colorYellow;
			int resourceId = R.drawable.selector_rounded_border_rect_status_normal;
			String callType = mContext.getString(R.string.history_call_type_normal);
//			if (item.getCallType() == Packets.StatisticListType.AppCall) {
//				colorId = R.color.colorGreen01;
//				resourceId = R.drawable.selector_rounded_border_rect_status_app;
//				callType = mContext.getString(R.string.history_call_type_app);
//			} else if (item.getCallType() == Packets.StatisticListType.BusinessCall){
//				colorId = R.color.colorBlue01;
//				resourceId = R.drawable.selector_rounded_border_rect_status_business;
//				callType = mContext.getString(R.string.history_call_type_business);
//			} else {
//				colorId = R.color.colorYellow;
//				resourceId = R.drawable.selector_rounded_border_rect_status_normal;
//				callType = mContext.getString(R.string.history_call_type_normal);
//			}
			mBinding.tvCallType.setTextColor(mContext.getResources().getColor(colorId));
			mBinding.tvCallType.setBackgroundResource(resourceId);
			mBinding.tvCallType.setText(callType);

			mBinding.clCallPassenger.setEnabled(false);
			mBinding.ivCallPassenger.setEnabled(false);
			mBinding.tvCallPassenger.setEnabled(false);
			String phoneNumber = item.getPassengerPhoneNumber();
			if (phoneNumber != null && !phoneNumber.isEmpty() && !phoneNumber.equals("0")) {
				//mBinding.clCallPassenger.setVisibility(View.VISIBLE);
				mBinding.clCallPassenger.setEnabled(true);
				mBinding.ivCallPassenger.setEnabled(true);
				mBinding.tvCallPassenger.setEnabled(true);
			}
		}

		@Override
		public void onClick(View v) {
			int index = getAdapterPosition();
			String phoneNumber = mItems.get(index).getPassengerPhoneNumber();
			if (phoneNumber != null && !phoneNumber.isEmpty()) {
				LogHelper.e("phoneNumber : " + phoneNumber);

				try {
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				LogHelper.e("전화번호가 유효하지 않음");
			}
		}
	}
}