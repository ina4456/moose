package com.insoline.pnd.model;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseApplication;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.utils.ContextProvider;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by seok-beomkwon on 2017. 10. 18..
 */


public class Popup implements Serializable {
    public static final int TYPE_ONE_BTN_NORMAL = 10;
    public static final int TYPE_TWO_BTN_NORMAL = 20;
    public static final int TYPE_ONE_BTN_LARGE = 11;
    public static final int TYPE_TWO_BTN_LARGE = 21;
    public static final int TYPE_TWO_BTN_EDIT_TEXT = 22;
    public static final int TYPE_RADIO_BTN_LIST = 100;
    public static final int TYPE_RADIO_TWO_BTN_LIST = 101;

    private String tag;

    private int type;
    private int width;
    private int height;
    private int dismissSecond;

    private String title;
    private String contentTitle;
    private String content;
    private String labelPositiveBtn;
    private String labelNegativeBtn;
    private ArrayList<SelectionItem> selectionItems;
    private boolean isHiddenStatusBar;
    private boolean isIntegerForEditText;
    private boolean isSecureTextType;

    public String getTag() {
        return tag;
    }

    public int getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContent() {
        return content;
    }

    public String getLabelPositiveBtn() {
        if (labelPositiveBtn == null) {
            return BaseApplication.getContextProvider().getString(R.string.popup_btn_confirm);
        } else {
            return labelPositiveBtn;
        }
    }

    public String getLabelNegativeBtn() {
        if (labelNegativeBtn == null) {
            return BaseApplication.getContextProvider().getString(R.string.popup_btn_cancel);
        } else {
            return labelNegativeBtn;
        }
    }

    public int getDismissSecond() {
        return dismissSecond;
    }

    public ArrayList<SelectionItem> getSelectionItems() {
        return selectionItems;
    }

    public boolean isHiddenStatusBar() {
        return isHiddenStatusBar;
    }

    public boolean isIntegerForEditText() {
        return isIntegerForEditText;
    }

    public boolean isSecureTextType() {
        return isSecureTextType;
    }

    @Override
    public String toString() {
        return "Popup{" +
                "tag='" + tag + '\'' +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
                ", dismissSecond=" + dismissSecond +
                ", title='" + title + '\'' +
                ", contentTitle='" + contentTitle + '\'' +
                ", content='" + content + '\'' +
                ", labelPositiveBtn='" + labelPositiveBtn + '\'' +
                ", labelNegativeBtn='" + labelNegativeBtn + '\'' +
                ", selectionItems=" + selectionItems +
                ", isHiddenStatusBar=" + isHiddenStatusBar +
                ", isIntegerForEditText=" + isIntegerForEditText +
                ", isSecureTextType=" + isSecureTextType +
                '}';
    }

    private Popup(Builder builder) {
        this.tag = builder.tag;
        this.type = builder.type;
        this.width = builder.width;
        this.height = builder.height;
        this.title = builder.title;
        this.contentTitle = builder.contentTitle;
        this.content = builder.content;
        this.labelPositiveBtn = builder.labelPositiveBtn;
        this.labelNegativeBtn = builder.labelNegativeBtn;
        this.dismissSecond = builder.dismissSecond;
        this.selectionItems = builder.selectionItems;
        this.isHiddenStatusBar = builder.isHiddenStatusBar;
        this.isIntegerForEditText = builder.isIntegerForEditText;
        this.isSecureTextType = builder.isSecureTextType;
    }

    public static class Builder {
        private String tag;
        private int type;
        private int width;
        private int height;
        private int dismissSecond;
        private String title;
        private String contentTitle;
        private String content;
        private String labelPositiveBtn;
        private String labelNegativeBtn;
        private ArrayList<SelectionItem> selectionItems;
        private boolean isHiddenStatusBar;
        private boolean isIntegerForEditText;
        private boolean isSecureTextType;

        public Builder(int type, String tag) {
            this.tag = tag;
            this.type = type;
            final ContextProvider rp = BaseApplication.getContextProvider();
            switch (type) {
                case TYPE_ONE_BTN_NORMAL:
                case TYPE_TWO_BTN_NORMAL:
                case TYPE_TWO_BTN_EDIT_TEXT:
                    this.width = rp.getResources().getDimensionPixelSize(R.dimen.dialog_fragment_width_normal);
                    break;
				case TYPE_ONE_BTN_LARGE:
				case TYPE_TWO_BTN_LARGE:
					this.width = rp.getResources().getDimensionPixelSize(R.dimen.dialog_fragment_width_large);
					break;
                case TYPE_RADIO_BTN_LIST:
                case TYPE_RADIO_TWO_BTN_LIST:
                    switch (tag) {
                        case Constants.DIALOG_TAG_CONFIG_CORPORATION:
                            this.width = rp.getResources().getDimensionPixelSize(R.dimen.dialog_fragment_width_selection_list_corporation);
                            break;
                        default:
                            this.width = rp.getResources().getDimensionPixelSize(R.dimen.dialog_fragment_width_selection_list);
                            break;
                    }
                    break;
            }
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setContentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder setBtnLabel(String labelPositiveBtn, String labelNegativeBtn) {
            this.labelPositiveBtn = labelPositiveBtn;
            this.labelNegativeBtn = labelNegativeBtn;
            return this;
        }

        public Builder setDismissSecond(int dismissSecond) {
            this.dismissSecond = dismissSecond;
            return this;
        }

        public Builder setSelectionItems(ArrayList<SelectionItem> selectionItems) {
            this.selectionItems = selectionItems;
            return this;
        }

        public Builder setIsHiddenStatusBar(boolean isHiddenStatusBar) {
            this.isHiddenStatusBar = isHiddenStatusBar;
            return this;
        }

        public Builder setIsIntegerForEditText(boolean isIntegerForEditText) {
            this.isIntegerForEditText = isIntegerForEditText;
            return this;
        }

        public Builder setIsSecureTextType(boolean isSecureTextType) {
            this.isSecureTextType = isSecureTextType;
            return this;
        }


        public Popup build() {
            return new Popup(this);
        }
    }
}
