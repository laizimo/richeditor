package com.lu.myview.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;

import com.lu.myview.R;

/**
 * Created by 陆正威 on 2017/4/26.
 */

@SuppressWarnings({"unused"})
public class ToggleButton extends AppCompatImageButton implements View.OnClickListener {

    private final static int DEFAULT_ORI_COLOR = R.color.colorPrimary;
    private final static int DEFAULT_CHG_COLOR = R.color.colorAccent;
    private final static int DEFAULT_DISABLE_COLOR = Color.argb(255, 231, 231, 231);

    private int disColor = DEFAULT_DISABLE_COLOR;
    private int oriColor = DEFAULT_ORI_COLOR;
    private int chgColor = DEFAULT_CHG_COLOR;

    private OnClickListener onClickListener;

    boolean isSelect = false;
    boolean isAutoToggle = true;

    public ToggleButton(Context context) {
        this(context,null);
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToggleButton,defStyleAttr, defStyleAttr);
        try {
            oriColor = typedArray.getColor(R.styleable.ToggleButton_lu_ini_color,ContextCompat.getColor(getContext(),DEFAULT_ORI_COLOR));
            chgColor = typedArray.getColor(R.styleable.ToggleButton_lu_chg_color, ContextCompat.getColor(getContext(),DEFAULT_CHG_COLOR));
            isAutoToggle = typedArray.getBoolean(R.styleable.ToggleButton_lu_toggle_auto,true);
        }catch (Exception ignored){

        }finally {
            typedArray.recycle();
        }

        super.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getBackground().setColorFilter(oriColor, PorterDuff.Mode.SRC_IN);
    }

    public void toggle(){
        if(isSelect)
            getBackground().setColorFilter(oriColor, PorterDuff.Mode.SRC_IN);
        else
            getBackground().setColorFilter(chgColor, PorterDuff.Mode.SRC_IN);
        isSelect = !isSelect;
    }

    public void setAutoToggleEnable(boolean isAutoToggle){
        this.isAutoToggle = isAutoToggle;
    }

    public void setSelect(boolean select) {
        if(select != isSelect) {
            toggle();
        }
    }

    public boolean isSelect(){return isSelect;}

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(!enabled) {
            getBackground().setColorFilter(disColor, PorterDuff.Mode.SRC_IN);
        }
        else {
            if(!isSelect)
                getBackground().setColorFilter(oriColor, PorterDuff.Mode.SRC_IN);
            else
                getBackground().setColorFilter(chgColor, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(this);
        this.onClickListener = l;
    }

    @Override
    public void onClick(View v) {
        if(isAutoToggle)
            toggle();
        if(onClickListener != null){
            onClickListener.onClick(v);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onClickListener = null;
    }

    public void setDisColor(int disColor) {
        this.disColor = disColor;
    }

    public void setOriColor(int oriColor) {
        this.oriColor = oriColor;
    }

    public void setChgColor(int chgColor) {
        this.chgColor = chgColor;
    }
}
