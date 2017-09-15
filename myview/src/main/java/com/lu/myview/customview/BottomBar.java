package com.lu.myview.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

/**
 * Created by 陆正威 on 2017/4/12.
 */
@SuppressWarnings({"unused"})
public class BottomBar extends LinearLayout {
    private static final int TRANSLATE_DURATION_MILLIS = 200;

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private boolean mVisible = true;

    private LinearLayout mTabLayout;

    private LayoutParams mTabParams;
    private int mCurrentPosition = 0;
    private int mFakeItemNum = 0;

    private OnTabSelectedListener mSelectedListener;
    private OnTabClickListener mClickListener;

    public BottomBar(Context context) {
        this(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);

        mTabLayout = new LinearLayout(context);
        mTabLayout.setBackgroundColor(Color.WHITE);
        mTabLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(mTabLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mTabParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        mTabParams.weight = 1;
    }

    public BottomBar addItem(final BottomBarTab tab) {
        addItem(tab,true);
        return this;
    }

    public BottomBar addItem(final BottomBarTab tab, final boolean canSelect) {
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedListener == null) return;

                int pos = tab.getTabPosition();
                if (mCurrentPosition == pos) {
                    mSelectedListener.onTabReselected(pos);
                } else {
                    if(canSelect) {
                        mSelectedListener.onTabSelected(pos, mCurrentPosition);
                        tab.setSelected(true);
                        mSelectedListener.onTabUnselected(mCurrentPosition);
                        mTabLayout.getChildAt(mCurrentPosition).setSelected(false);
                        mCurrentPosition = pos;
                    }else {
                        if(mClickListener != null)
                            mClickListener.onTabClick(tab,pos);
                    }
                }
            }
        });
        tab.setTabPosition(mTabLayout.getChildCount());
        tab.setTabPositionWithFakeItem(mTabLayout.getChildCount() - mFakeItemNum);
        tab.setLayoutParams(mTabParams);
        mTabLayout.addView(tab);
        if(!canSelect) mFakeItemNum++;
        return this;
    }

    public int getFixPosition(int pos){
        if(mTabLayout.getChildAt(pos) instanceof BottomBarTab)
            return ((BottomBarTab)mTabLayout.getChildAt(pos)).getTabPositionWithFakeItem();
        else
            return -1;
    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        mSelectedListener = onTabSelectedListener;
    }

    public void setCurrentItem(final int position) {
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                mTabLayout.getChildAt(position).performClick();
            }
        });
    }

    public void setOnTabClickListener(OnTabClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position, int prePosition);

        void onTabUnselected(int position);

        void onTabReselected(int position);
    }

    public interface OnTabClickListener{
        void onTabClick(BottomBarTab v, int position);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (mCurrentPosition != ss.position) {
            mTabLayout.getChildAt(mCurrentPosition).setSelected(false);
            mTabLayout.getChildAt(ss.position).setSelected(true);
        }
        mCurrentPosition = ss.position;
    }

    public int getCurrentItemPosition() {
        return mCurrentPosition;
    }

    private static class SavedState extends BaseSavedState {
        private int position;

        SavedState(Parcel source) {
            super(source);
            position = source.readInt();
        }

        SavedState(Parcelable superState, int position) {
            super(superState);
            this.position = position;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(position);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void hide() {
        hide(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void show() {
        show(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void hide(boolean anim) {
        toggle(false, anim, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void show(boolean anim) {
        toggle(true, anim, false);
    }

    public boolean isVisible() {
        return mVisible;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    // view树完成测量并且分配空间而绘制过程还没有开始的时候播放动画。
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            final int translationY = visible ? 0 : height;
            if (animate) {
                final LayoutParams layoutParams= (LayoutParams) getLayoutParams();
                animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY)
                        .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                layoutParams.setMargins(layoutParams.leftMargin, (int) (-getTranslationY()),layoutParams.rightMargin,layoutParams.bottomMargin);
                                //Log.e("vale",layoutParams.topMargin+","+layoutParams.bottomMargin);
                                requestLayout();
                            }
                        });
            } else {
                ViewCompat.setTranslationY(this, translationY);

            }
        }
    }
}
