package com.lu.lubottommenu.menuitem;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.lu.lubottommenu.R;
import com.lu.lubottommenu.logiclist.MenuItem;

/**
 * Created by 陆正威 on 2017/9/6.
 */

public class ImageViewItem extends BottomMenuItem<ImageButton> implements Parcelable {

    private int idRes;
    private boolean enableAutoSet = true;//点击后自动设置

    public ImageViewItem(Context context, MenuItem menuItem, int idRes) {
        this(context, menuItem, idRes, true);
    }

    public ImageViewItem(Context context, MenuItem menuItem, int idRes, boolean enableAutoSet) {
        super(context, menuItem);
        this.idRes = idRes;
        this.enableAutoSet = enableAutoSet;
    }


    @NonNull
    @Override
    public ImageButton createView() {
        ImageButton imageView = new ImageButton(getContext());
        if(!enableAutoSet) {
            TypedArray typedArray = getContext().obtainStyledAttributes(new int[]{R.attr.selectableItemBackgroundBorderless});
            Drawable drawable = typedArray.getDrawable(0);
            imageView.setBackgroundDrawable(drawable);
            typedArray.recycle();
        }else
            imageView.setBackgroundDrawable(null);
        imageView.setImageResource(idRes);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(12, 32, 12, 32);
        return imageView;
    }

    @Override
    public void settingAfterCreate(boolean isSelected, final ImageButton imageView) {
        if (enableAutoSet) {
            if (isSelected) {
                imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            } else {
                imageView.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            }
        }else {
            imageView.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
    }


    @Override
    public void onSelectChange(boolean isSelected) {
        ImageButton imageView = (ImageButton) getMainView();
        if (imageView == null) return;
        settingAfterCreate(isSelected, imageView);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.idRes);
        dest.writeInt(this.enableAutoSet ? 1 : 0);
    }

    protected ImageViewItem(Parcel in) {
        super(in);
        this.idRes = in.readInt();
        this.enableAutoSet = in.readInt() == 1;
    }

    public static final Creator<ImageViewItem> CREATOR = new Creator<ImageViewItem>() {
        @Override
        public ImageViewItem createFromParcel(Parcel source) {
            return new ImageViewItem(source);
        }

        @Override
        public ImageViewItem[] newArray(int size) {
            return new ImageViewItem[size];
        }
    };

    public boolean isEnableAutoSet() {
        return enableAutoSet;
    }

    public void setEnableAutoSet(boolean enableAutoSet) {
        this.enableAutoSet = enableAutoSet;
    }
}
