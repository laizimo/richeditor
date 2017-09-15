package com.lu.myview.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.lu.myview.R;


/**
 * Created by 陆正威 on 2017/4/24.
 */
@SuppressWarnings({"unused"})
public class Tag extends android.support.v7.widget.AppCompatTextView{

    private final int DEFAULT_COLOR = Color.BLACK;
    private final int DEFAULT_WIDTH = 2;
    private final int DEFAULT_RADII = 10;

    private int mColor = DEFAULT_COLOR;
    private int mStroke = DEFAULT_WIDTH;
    private int mRadii = DEFAULT_RADII;

    GradientDrawable background;

    public Tag(Context context) {
        this(context,null);
    }

    public Tag(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Tag(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Tag,defStyleAttr, defStyleAttr);
        try {
            mColor = typedArray.getColor(R.styleable.Tag_tag_color,DEFAULT_COLOR);
            mStroke = typedArray.getDimensionPixelSize(R.styleable.Tag_tag_stroke_width, DEFAULT_WIDTH);
            mRadii = typedArray.getDimensionPixelSize(R.styleable.Tag_tag_corner,DEFAULT_RADII);
        }catch (Exception ignored){

        }finally {
            typedArray.recycle();
        }

        background = new GradientDrawable();
        background.setStroke(mStroke,mColor);
        background.setCornerRadii(new float[]{mRadii,mRadii,mRadii,mRadii,mRadii,mRadii,mRadii,mRadii});
        setBackground(background);
        //setPadding(mRadii>>1,mRadii>>2,mRadii>>1,mRadii>>2);
        setTextColor(mColor);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setTextColor(@ColorInt int color) {
        super.setTextColor(color);
        ((GradientDrawable)getBackground()).setStroke(mStroke,color);
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
//        try {
//                throw new Exception("can't set a new background");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        super.setBackgroundColor(color);
        setTextColor(color);
    }
}
