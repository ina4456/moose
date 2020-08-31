package com.insoline.pnd.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.databinding.ItemWaitingCallBinding;
import com.insoline.pnd.model.WaitingCall;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.view.WaitingCallActivity;

import java.util.ArrayList;


public class WaitingCallAdapter extends RecyclerView.Adapter<WaitingCallAdapter.ViewHolder> {

	private ArrayList<WaitingCall> mItems;
	private Context mContext;
	private CallListCallback mCallback;

	public WaitingCallAdapter(Context context, ArrayList<WaitingCall> items, CallListCallback callback) {
		this.mContext = context;
		this.mItems = items;
		this.mCallback = callback;
	}

	public interface CallListCallback {
		void onListItemSelected(WaitingCall item);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_waiting_call, parent, false);
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

	public void refreshData(ArrayList<WaitingCall> calls) {
		LogHelper.e("@@@@ refreshData");
		if (calls != null) {
			LogHelper.e("refreshData() : " + calls.size());
			mItems = calls;
		} else {
			mItems.clear();
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<WaitingCall> calls) {
		LogHelper.e("@@@@ addData");
		int curSize = getItemCount();
		mItems.addAll(calls);
		notifyItemRangeInserted(curSize, calls.size());
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		ItemWaitingCallBinding mBinding;

		public ViewHolder(View view) {
			super(view);
			mBinding = DataBindingUtil.bind(view);
			if (mBinding != null) {
				mBinding.btnWaitingCallRequestAllocation.setOnClickListener(this);
			}
		}

		private void bindBodyData(WaitingCall item) {
			//LogHelper.e("item : " + item.toString());
			mBinding.tvDistance.setText(item.getCallDistanceToDeparture());
			mBinding.tvWaitingCallDeparture.setText(item.getDeparturePoi());

			String destinationPoi = item.getDestinationPoi();
			String destinationAddr = item.getDestinationAddress();
			if (destinationPoi == null || destinationPoi.isEmpty()) {
				if (destinationAddr != null && !destinationAddr.isEmpty()) {
					destinationPoi = destinationAddr;
				} else {
					destinationPoi = mContext.getString(R.string.allocation_text_empty_destination);
				}
			}
			mBinding.tvWaitingCallDestination.setText(destinationPoi);
		}

		@Override
		public void onClick(View v) {
			int index = getAdapterPosition();
			LogHelper.e("onClick : " + index);
			WaitingCall item = mItems.get(index);
			mCallback.onListItemSelected(item);
		}
	}
}