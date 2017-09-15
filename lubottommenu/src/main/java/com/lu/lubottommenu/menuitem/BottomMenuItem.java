package com.lu.lubottommenu.menuitem;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.logiclist.MenuItem;

import java.io.Serializable;

/**
 * Created by 陆正威 on 2017/9/6.
 */

public abstract class BottomMenuItem<T extends View> implements IBottomMenuItem,Parcelable,Serializable{

    private MenuItem mMenuItem;
    private boolean isSelected = false;
    private transient Context mContext;

    private OnIteClickListener onIteClickListener;

    public BottomMenuItem(Context context,MenuItem menuItem) {
        mMenuItem = menuItem;
        isSelected = false;
        mContext = context;
    }


    public void onDisplayPrepare(){
        View v = mMenuItem.getContentView();

        if(v == null)
            mMenuItem.setContentView(createView());

        settingAfterCreate(isSelected, (T) (mMenuItem.getContentView()));

        mMenuItem.getContentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onIteClickListener != null)
                    onIteClickListener.onItemClick(mMenuItem);
            }
        });
    }

    public void onViewDestroy(){
        if(getMainView() != null) {
            getMainView().setOnClickListener(null);
            mMenuItem.setContentView(null);
        }
    }

    @NonNull
    public abstract T createView();

    public abstract void settingAfterCreate(boolean isSelected, T view);

    public void onSelectChange(boolean isSelected){
        //do nothing
    }

    @Override
    public Long getItemId() {
        return mMenuItem.getId();
    }

    @Override
    public View getMainView() {
        return mMenuItem.getContentView();
    }

    public MenuItem getMenuItem(){
        return mMenuItem;
    }

    public void setOnIteClickListener(OnIteClickListener onIteClickListener) {
        this.onIteClickListener = onIteClickListener;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        if(selected != isSelected)
            onSelectChange(selected);
        isSelected = selected;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mMenuItem);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
    }

    protected BottomMenuItem(Parcel in) {
        this.mMenuItem = (MenuItem) in.readSerializable();
        this.isSelected = in.readByte() != 0;
    }
}
