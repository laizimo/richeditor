package com.lu.myview.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.lu.myview.R;

/**
 * Created by 陆正威 on 2017/4/12.
 */
@SuppressWarnings({"unused"})
public class BottomBarTab extends FrameLayout {
    private ImageView mIcon;
    private Context mContext;
    private int icon;
    private int icon2;
    private int mTabPosition = -1;
    private int mTabPositionWithFakeItem = -1;

    public BottomBarTab(Context context){
        this(context,-1,-1);
    }

    public BottomBarTab(Context context, @DrawableRes int icon,@DrawableRes int icon2) {
        this(context, null, icon,icon2);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int icon,int icon2) {
        this(context, attrs, 0, icon,icon2);
        init(context, icon ,icon2);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int icon, int icon2) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, int icon,int icon2) {
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackgroundBorderless});
        Drawable drawable = typedArray.getDrawable(0);
        setBackgroundDrawable(drawable);
        typedArray.recycle();

        this.icon = icon;
        this.icon2 = icon2;

        mIcon = new ImageView(context);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        mIcon.setImageResource(this.icon);
        mIcon.setLayoutParams(params);
        mIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        addView(mIcon);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mIcon.setImageResource(this.icon2);
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.colorActive));
        } else {
            mIcon.setImageResource(this.icon);
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
    }

    public void setTabPosition(int position) {
        mTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }

    public int getTabPosition() {
        return mTabPosition;
    }

    public int getTabPositionWithFakeItem() {
        return mTabPositionWithFakeItem;
    }

    public void setTabPositionWithFakeItem(int mTabPositionWithFakeItem) {
        this.mTabPositionWithFakeItem = mTabPositionWithFakeItem;
    }
}
