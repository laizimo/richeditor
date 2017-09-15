package com.lu.myview.customview;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.lu.myview.R;


/**
 * Created by 陆正威 on 2017/4/24.
 */
@SuppressWarnings({"unused"})
public class StateColorButton extends AppCompatButton {

    private final static int DEFAULT_START_BG_COLOR = Color.TRANSPARENT;
    private final static int DEFAULT_END_BG_COLOR = Color.BLACK;

    private final static int DEFAULT_START_TEXT_COLOR = Color.BLACK;
    private final static int DEFAULT_END_TEXT_COLOR = Color.WHITE;

    private final static int DEFAULT_ANIMATION_DURATION = 200;//ms
    private final int DEFAULT_WIDTH = 3;
    private final int DEFAULT_RADII = 10;

    private int mStroke = DEFAULT_WIDTH;
    private int mRadii = DEFAULT_RADII;

    private int startBgColor = DEFAULT_START_BG_COLOR;
    private int endBgColor = DEFAULT_END_BG_COLOR;
    private int startTextColor = DEFAULT_START_TEXT_COLOR;
    private int endTextColor = DEFAULT_END_TEXT_COLOR;
    private int anmDurtion = DEFAULT_ANIMATION_DURATION;

    private String startText;
    private String endText;

    private ValueAnimator mAnimator;
    private int state = 0;

    private int mBgColor;
    private int mTextColor;
    private ArgbEvaluator argbEvaluator;

    GradientDrawable background = new GradientDrawable();

    public StateColorButton(Context context) {
        this(context,null);
    }

    public StateColorButton(Context context, AttributeSet attrs) {
        this(context, attrs ,0);
    }

    public StateColorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StateColorButton,defStyleAttr, defStyleAttr);
        try {
            startBgColor = typedArray.getColor(R.styleable.StateColorButton_start_color,DEFAULT_START_BG_COLOR);
            endBgColor = typedArray.getColor(R.styleable.StateColorButton_end_color,DEFAULT_END_BG_COLOR);
            startTextColor =  typedArray.getColor(R.styleable.StateColorButton_start_text_color,DEFAULT_START_TEXT_COLOR);
            endTextColor =  typedArray.getColor(R.styleable.StateColorButton_end_text_color,DEFAULT_END_TEXT_COLOR);
            startText =  typedArray.getString(R.styleable.StateColorButton_start_text);
            endText = typedArray.getString(R.styleable.StateColorButton_end_text);
        }catch (Exception ignored){

        }finally {
            typedArray.recycle();
        }

        if(startText == null && endText != null) startText = endText;
        else if(startText != null && endText == null) endText = startText;
        else if(startText == null) endText = startText = getText().toString();

        mAnimator = new ValueAnimator();
        argbEvaluator = new ArgbEvaluator();
        setText(startText);
        setTextColor(startTextColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(background);
        }
        background.setCornerRadius(mRadii);
        background.setStroke(mStroke,endBgColor);
        background.setColor(startBgColor);
        initAnm();
    }

    public void toggle(){
        startColorAnm();
    }

    public int getState(){
        return state;
    }

    private void initAnm(){
        mAnimator.setDuration(anmDurtion);
        mAnimator = ValueAnimator.ofFloat(0,1);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue(),scale = Math.abs(0.1f-0.2f*value)+ 0.91f;
                mBgColor = (int) argbEvaluator.evaluate(value,startBgColor,endBgColor);
                mTextColor = (int) argbEvaluator.evaluate(value,startTextColor,endTextColor);
                setTextColor(mTextColor);
                setTextScaleX(Math.abs(0.5f - value)+0.5f);
                if(value > 0.5 && getText() != endText)
                    setText(endText);
                background.setColor(mBgColor);
                setScaleX(scale);
                setScaleY(scale);
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                state = 1;
                setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                state = 0;
                setEnabled(true);

                int temp;
                temp = startBgColor;
                startBgColor = endBgColor;
                endBgColor = temp;
                temp = startTextColor;
                startTextColor = endTextColor;
                endTextColor = temp;
                String temp2 = startText;
                startText = endText;
                endText = temp2;

                setTextColor(startTextColor);
                background.setColor(startBgColor);

                setScaleX(1);
                setScaleY(1);
                setTextScaleX(1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mBgColor = startBgColor;
                mTextColor = startTextColor;
                setScaleX(1);
                setScaleY(1);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void startColorAnm(){
        mAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mAnimator.isRunning())
            mAnimator.cancel();
    }
}
