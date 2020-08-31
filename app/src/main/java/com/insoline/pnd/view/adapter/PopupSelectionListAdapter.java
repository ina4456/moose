package com.insoline.pnd.view.adapter;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.insoline.pnd.R;
import com.insoline.pnd.databinding.ItemPopupListRadioBinding;
import com.insoline.pnd.model.SelectionItem;
import com.insoline.pnd.utils.LogHelper;

import java.util.ArrayList;

/**
 * Created by seok-beomkwon on 2017. 10. 17..
 */

public class PopupSelectionListAdapter extends RecyclerView.Adapter<PopupSelectionListAdapter.ViewHolder> {
	private Context mContext;
	private ArrayList<SelectionItem> mList;
	private boolean isRadioBtnList, isTwoBtn;
	public SelectionCallback mCallback;

	public PopupSelectionListAdapter(Context context, ArrayList<SelectionItem> list, SelectionCallback callback, boolean isRadioBtnList, boolean isTwoBtn) {
		this.mContext = context;
		this.mList = list;
		this.mCallback = callback;
		this.isRadioBtnList = isRadioBtnList;
		this.isTwoBtn = isTwoBtn;
	}

	public interface SelectionCallback {
		void onListItemSelected(boolean isRadioBtnList, String itemName, int itemId, boolean isTwoBtn);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		ViewDataBinding binding;
//		if(isRadioBtnList) {
			binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_list_radio, viewGroup, false);
//		} else {
//			binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_title_content_list, viewGroup, false);
//		}
		return new ViewHolder(binding.getRoot());
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.bindItemData(getItemAt(position));
	}

	@Override
	public int getItemCount() {
		return (null != mList ? mList.size() : 0);
	}

	public SelectionItem getItemAt(int position) {
		return mList.get(position);
	}

	public void setSingleSelection(int position){
		for (int i = 0; i < mList.size(); i++) {
			if (i == position) {
				mList.get(i).setChecked(true);
			} else {
				mList.get(i).setChecked(false);
			}
		}
		notifyDataSetChanged();
	}

	public String getSelectedItem() {
		for (SelectionItem item : mList) {
			if (item.isChecked()) {
				LogHelper.e("item " + item);
				return item.getItemContent();
			}
		}
		return "";
	}

	public void setMultiSelection(int position){
		for (int i = 0; i < mList.size(); i++) {
			if (i == position) {
				mList.get(i).setChecked(!mList.get(i).isChecked());
			}
		}
		notifyDataSetChanged();
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		ItemPopupListRadioBinding radioBtnListBinding;
//		ItemTitleContentListBinding mTitleContentListBinding;

		public ViewHolder(View itemView) {
			super(itemView);
//			if (isRadioBtnList) {
				radioBtnListBinding = DataBindingUtil.bind(itemView);
				radioBtnListBinding.clSelectionItem.setOnClickListener(this);
//			} else {
//				mTitleContentListBinding = DataBindingUtil.bind(itemView);
//			}
		}

		public void bindItemData(SelectionItem item) {
//			if (isRadioBtnList) {
				radioBtnListBinding.setSelectionItem(item);
				//색 포함 텍스트 설정
				String itemName = item.getItemContent();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					radioBtnListBinding.tvItem.setText(Html.fromHtml(itemName, Html.FROM_HTML_MODE_LEGACY));
				} else {
					//LogHelper.e("item : " + itemName);
					radioBtnListBinding.tvItem.setText(Html.fromHtml(itemName));
				}
//			} else {
//				mTitleContentListBinding.tvPopupTitle.setText(item.getItemTitle());
//				mTitleContentListBinding.tvPopupContent.setText(item.getItemContent());
//			}
		}


		@Override
		public void onClick(View view) {
			int position = getAdapterPosition();
			//LogHelper.e("position : " + position);
			if (position != -1) {
				SelectionItem item = getItemAt(position);
//				if (isRadioBtnList) {
					setSingleSelection(position);
//				} else {
//					setMultiSelection(position);
//				}
				mCallback.onListItemSelected(isRadioBtnList, item.getItemContent(), item.getItemId(), isTwoBtn);
			}
		}
	}
}