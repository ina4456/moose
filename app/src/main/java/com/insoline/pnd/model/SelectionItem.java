package com.insoline.pnd.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by seok-beomkwon on 2017. 10. 23..
 */

public class SelectionItem implements Parcelable {
	private int itemId;
	private String itemTitle;
	private String itemContent;
	private boolean isChecked;


	public SelectionItem() {}

	private SelectionItem(Parcel in) {
		readFromParcel(in);
	}

	public static final Creator<SelectionItem> CREATOR = new Creator<SelectionItem>() {
		@Override
		public SelectionItem createFromParcel(Parcel in) {
			return new SelectionItem(in);
		}

		@Override
		public SelectionItem[] newArray(int size) {
			return new SelectionItem[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(itemId);
		parcel.writeString(itemTitle);
		parcel.writeString(itemContent);
		parcel.writeByte((byte)(isChecked ? 1 : 0));
	}

	private void readFromParcel(Parcel in) {
		itemId = in.readInt();
		itemTitle = in.readString();
		itemContent = in.readString();
		isChecked = in.readByte() != 0;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemTitle() {
		return itemTitle;
	}

	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}

	public String getItemContent() {
		return itemContent;
	}

	public void setItemContent(String itemContent) {
		this.itemContent = itemContent;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean checked) {
		isChecked = checked;
	}

	@Override
	public String toString() {
		return "SelectionItem{" +
				"itemId=" + itemId +
				", itemTitle='" + itemTitle + '\'' +
				", itemContent='" + itemContent + '\'' +
				", isChecked=" + isChecked +
				'}';
	}
}
