package com.insoline.pnd.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.databinding.ItemWaitingZoneBinding;
import com.insoline.pnd.model.WaitingZone;
import com.insoline.pnd.utils.LogHelper;

import java.util.ArrayList;

public class WaitingZoneAdapter extends RecyclerView.Adapter<WaitingZoneAdapter.ViewHolder> {

	private ArrayList<WaitingZone> mItems;
	private Context mContext;
	private WaitingZoneListCallback mCallback;

	public WaitingZoneAdapter(Context context, ArrayList<WaitingZone> items, WaitingZoneListCallback callback) {
		mContext = context;
		mItems = items;
		mCallback = callback;
	}

	public interface WaitingZoneListCallback {
		void onListItemSelected(int index, WaitingZone item, boolean isRequest);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_waiting_zone, parent, false);
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

	public void refreshData(ArrayList<WaitingZone> runHistories) {
		if (runHistories != null) {
			mItems = runHistories;
		} else {
			mItems.clear();
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<WaitingZone> waitingZones) {
		int curSize = getItemCount();
		mItems.addAll(waitingZones);
		notifyItemRangeInserted(curSize, waitingZones.size());
	}

	public void setViewsAsWaitingOrNot(boolean isWaiting, ViewHolder vh, int index, int waitOrder) {
		vh.mBinding.btnRequestWait.setVisibility(isWaiting ? View.GONE : View.VISIBLE);
		vh.mBinding.btnCancelWait.setVisibility(isWaiting ? View.VISIBLE : View.GONE);
		vh.mBinding.tvTitle.setPressed(isWaiting);
		vh.mBinding.tvWaitingCount.setPressed(isWaiting);
		vh.mBinding.clWaitingZone.setSelected(isWaiting);

		for (int i = 0; i < mItems.size(); i++) {
			WaitingZone item = mItems.get(i);
			if (isWaiting) {
				if (i == index) {
					LogHelper.e("i : " + index);
					item.setMyWaitingOrder(waitOrder);
				} else {
					item.setAvailableWait(false);
					item.setMyWaitingOrder(0);
				}
			} else {
				item.setMyWaitingOrder(0);
			}
		}

		notifyDataSetChanged();
	}


	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		ItemWaitingZoneBinding mBinding;

		public ViewHolder(View view) {
			super(view);
			mBinding = DataBindingUtil.bind(view);
			if (mBinding != null) {
				mBinding.btnRequestWait.setOnClickListener(this);
				mBinding.btnCancelWait.setOnClickListener(this);
			}
		}

		private void bindBodyData(WaitingZone item) {
			mBinding.tvTitle.setText(item.getWaitingZoneName());


			if (item.getMyWaitingOrder() > 0) {
				mBinding.clWaitingZone.setEnabled(true);
				mBinding.clWaitingZone.setSelected(true);
				mBinding.tvTitle.setPressed(true);
				mBinding.tvWaitingCount.setPressed(true);
				mBinding.btnCancelWait.setVisibility(View.VISIBLE);
				mBinding.tvWaitingCount.setText(mContext.getString(
						R.string.wz_btn_waiting_and_order_count
						, item.getNumberOfCarsInAreas()
						, item.getMyWaitingOrder()
				));
				mBinding.btnRequestWait.setVisibility(View.GONE);
				mBinding.btnRequestWait.setEnabled(true);

			} else {
				mBinding.clWaitingZone.setEnabled(false);
				mBinding.clWaitingZone.setSelected(false);
				mBinding.tvTitle.setPressed(false);
				mBinding.tvWaitingCount.setPressed(false);
				mBinding.btnCancelWait.setVisibility(View.GONE);
				mBinding.tvWaitingCount.setText(mContext.getString(
						R.string.wz_btn_waiting_count, item.getNumberOfCarsInAreas()
				));

				boolean hasOrder = false;
				for (WaitingZone waitingZone : mItems) {
					if (waitingZone.getMyWaitingOrder() > 0) {
						hasOrder = true;
						break;
					}
				}

				mBinding.btnRequestWait.setVisibility(View.VISIBLE);
				mBinding.btnRequestWait.setEnabled(!hasOrder && item.isAvailableWait());
			}
		}

		@Override
		public void onClick(View v) {
			int index = getAdapterPosition();
			LogHelper.e("onClick : " + index);

			if (index != -1) {
				switch (v.getId()) {
					case R.id.btn_request_wait:
						LogHelper.e("btn_wz_request");
						mCallback.onListItemSelected(index, mItems.get(index), true);
						break;


					case R.id.btn_cancel_wait:
						LogHelper.e("btn_wz_cancel");
						mCallback.onListItemSelected(index, mItems.get(index), false);

						break;
				}
			}
		}
	}
}