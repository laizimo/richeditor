package com.lu.lubottommenu.menuitem;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;

import com.lu.lubottommenu.logiclist.MenuItem;
import com.lu.myview.customview.ToggleButton;

/**
 *
 * Created by 陆正威 on 2017/9/10.
 */

public class ToggleButtonItem extends BottomMenuItem<ToggleButton> {

    @IdRes
    private int id;

    public ToggleButtonItem(Context context,MenuItem menuItem,int id){
        super(context,menuItem);
        this.id = id;
    }

    public ToggleButtonItem(Context context, MenuItem menuItem) {
        super(context, menuItem);
    }

    protected ToggleButtonItem(Parcel in) {
        super(in);
        this.id = in.readInt();
    }

    @NonNull
    @Override
    public ToggleButton createView() {
        ToggleButton toggleButton = new ToggleButton(getContext());
        toggleButton.setBackgroundResource(getIdRes());

        return toggleButton;
    }

    @Override
    public void settingAfterCreate(boolean isSelected, ToggleButton view) {
        if(view != null)
            view.setSelect(isSelected);
    }

    @Override
    public void onSelectChange(boolean isSelected) {
        if(getMainView() != null && getMainView() instanceof ToggleButton){
            ((ToggleButton) getMainView()).setSelect(isSelected);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.id);
    }

    public static final Creator<ToggleButtonItem> CREATOR = new Creator<ToggleButtonItem>() {
        @Override
        public ToggleButtonItem createFromParcel(Parcel source) {
            return new ToggleButtonItem(source);
        }

        @Override
        public ToggleButtonItem[] newArray(int size) {
            return new ToggleButtonItem[size];
        }
    };

    void setIdRes(int id){
        this.id = id;
    }

    public int getIdRes() {
        return id;
    }
}
