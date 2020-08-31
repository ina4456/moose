package com.insoline.pnd.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.databinding.ItemNoticeListBinding;
import com.insoline.pnd.model.NoticeList;
import com.insoline.pnd.utils.LogHelper;

import java.util.ArrayList;

/**
 * Created by seok-beomkwon on 2017. 10. 17..
 * Notice, Message 동시 사용
 */

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {
	private ArrayList<NoticeList> mList;
	private SelectionCallback mCallback;

	public NoticeListAdapter(ArrayList<NoticeList> list, SelectionCallback callback) {
		this.mList = list;
		this.mCallback = callback;
	}

	public interface SelectionCallback {
		void onListItemSelected(NoticeList item);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
		View view = layoutInflater.inflate(R.layout.item_notice_list, viewGroup, false);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.bindData(getItemAt(position));
		if (position == (mList.size() - 1)) {
			holder.notificationListBinding.divider.setVisibility(View.GONE);
		} else {
			holder.notificationListBinding.divider.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public int getItemCount() {
		return (null != mList ? mList.size() : 0);
	}

	public NoticeList getItemAt(int position) {
		return mList.get(position);
	}

	public void setDataList(ArrayList<NoticeList> list) {
		if (this.mList != null) {
			this.mList.clear();
			this.mList.addAll(list);
		} else {
			this.mList = list;
		}
		notifyDataSetChanged();
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		public ItemNoticeListBinding notificationListBinding;

		public ViewHolder(View itemView) {
			super(itemView);
			notificationListBinding = DataBindingUtil.bind(itemView);
			notificationListBinding.clSelectionItem.setOnClickListener(this);
		}

		public void bindData(NoticeList item) {
			LogHelper.e("notice : " + item);
//			notificationListBinding.setNoticeItem(item);
			notificationListBinding.executePendingBindings();
		}

		@Override
		public void onClick(View view) {
			int position = getAdapterPosition();
			//LogHelper.e("position : " + position);
			if (position != -1) {
				NoticeList item = getItemAt(position);
				mCallback.onListItemSelected(item);
			}
		}
	}
}