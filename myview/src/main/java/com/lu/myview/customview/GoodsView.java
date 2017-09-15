package com.lu.myview.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lu.myview.R;


/**
 * Created by 陆正威 on 2017/4/14.
 */
@SuppressWarnings({"unused"})
public class GoodsView extends LinearLayout implements View.OnClickListener {

    private  boolean isMeasure = false;
    private final static int DEFAULT_MAX = 1000;
    private final static int DEFAULT_MIN = 0;
    private final static int DEFAULT_RES_ID = R.color.colorAccent;
    private final static int DEFAULT_COLOR = Color.BLACK;
    private final static int DEFAULT_TEXT_SIZE = 32;
    private final static int DEFAULT_TEXT_TYPE = Typeface.BOLD;
    private final static int DEFAULT_NUM = 1;

    private int Max = DEFAULT_MAX;
    private int Min = DEFAULT_MIN;
    private int TextColor = DEFAULT_COLOR;
    private int TextSize = DEFAULT_TEXT_SIZE;
    private int TextType = DEFAULT_TEXT_TYPE;
    private int DEFAULTNum = DEFAULT_NUM;

    Context mContext;
    private LinearLayout mGoodsLayout;
    private ImageView mDecreaseBtn;
    private ImageView mIncreaseBtn;
    private LayoutParams mEditLayoutParams;
    private LayoutParams mButtonLayoutParams;
    private TextView mEditText;
    private int ResId1 = 0;
    private int ResId2 = 0;
    private OnChangeListener changeListener;
    private OnOutOfRangeListener rangeListener;
    private int width = 0;
    private int height = 0;

    public GoodsView(Context context, @IdRes int id,@IdRes int id2){
        this(context);
        ResId1 = id;
        ResId2 = id2;
    }

    public GoodsView(Context context) {
        this(context,null);
    }

    public GoodsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GoodsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,R.styleable.GoodsView,defStyleAttr,defStyleAttr);
        try {
            ResId1 = typedArray.getResourceId(R.styleable.GoodsView_increaseRes, DEFAULT_RES_ID);
            ResId2 = typedArray.getResourceId(R.styleable.GoodsView_decreaseRes, DEFAULT_RES_ID);
            TextColor = typedArray.getColor(R.styleable.GoodsView_lu_textColor,DEFAULT_COLOR);
            TextSize = typedArray.getDimensionPixelSize(R.styleable.GoodsView_lu_textSize, DEFAULT_TEXT_SIZE);
            Max = typedArray.getInteger(R.styleable.GoodsView_lu_maxNum,DEFAULT_MAX);
            Min = typedArray.getInteger(R.styleable.GoodsView_lu_minNum,DEFAULT_MIN);
        }catch (Exception ignored){

        }finally {
            typedArray.recycle();
        }
        mContext = context;
        Init();
    }


    private void Init(){
        setOrientation(VERTICAL);

        mGoodsLayout = new LinearLayout(mContext);
        mGoodsLayout.setOrientation(HORIZONTAL);

        addView(mGoodsLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mEditText = new TextView(mContext);
        mEditText.setTypeface(Typeface.defaultFromStyle(TextType));
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,TextSize);
        mEditText.setGravity(Gravity.CENTER);
        //mEditText.setBackground(null);
        mEditLayoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1);
        mButtonLayoutParams = new LayoutParams(0,0);
        mButtonLayoutParams.gravity =  Gravity.CENTER_VERTICAL;

        mDecreaseBtn = new ImageView(mContext);
        mIncreaseBtn = new ImageView(mContext);
        mDecreaseBtn.setImageResource(ResId2);
        mIncreaseBtn.setImageResource(ResId1);

        mIncreaseBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDecreaseBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        mDecreaseBtn.setTag("sub");
        mIncreaseBtn.setTag("add");

        mDecreaseBtn.setOnClickListener(this);
        mIncreaseBtn.setOnClickListener(this);

        mEditText.setText(String.valueOf(DEFAULTNum));
        mEditText.setTextColor(TextColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        mButtonLayoutParams.width = height;
        mButtonLayoutParams.height = height;

        int textWidth = width - height - height;
        if(textWidth > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mEditLayoutParams.setMarginStart(textWidth>>4);
                mEditLayoutParams.setMarginEnd(textWidth>>4);
            }else {
                mEditLayoutParams.setMargins(textWidth>>4,mEditLayoutParams.topMargin,textWidth>>4,mEditLayoutParams.bottomMargin);
            }
        }

        if(!isMeasure) {
            mGoodsLayout.addView(mDecreaseBtn, mButtonLayoutParams);
            mGoodsLayout.addView(mEditText, mEditLayoutParams);
            mGoodsLayout.addView(mIncreaseBtn, mButtonLayoutParams);
            isMeasure = true;
        }else{
            mEditText.setLayoutParams(mEditLayoutParams);
            mIncreaseBtn.setLayoutParams(mButtonLayoutParams);
            mDecreaseBtn.setLayoutParams(mButtonLayoutParams);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View v) {

        if(changeListener!=null) changeListener.onClick(v);

        if(v.getTag().equals("sub")) {
            String string = mEditText.getText().toString();
            if(string.isEmpty()) string = "0";
            int i = Integer.parseInt(string);
            if(i <= Min) {
                if(rangeListener != null)
                    rangeListener.onOutOfRange(this,true);
                return;
            }

            String afterstring = String.valueOf(--i);
            mEditText.setText(afterstring);

            if(string.length() != afterstring.length())
                mEditText.requestLayout();

            if(changeListener != null)
                changeListener.onChangeListener(this,false);
        }
        else{

            String string = mEditText.getText().toString();
            if(string.isEmpty()) string = "0";
            int i = Integer.parseInt(string);
            if(i >= Max) {
                if(rangeListener != null)
                    rangeListener.onOutOfRange(this,false);
                return;
            }

            mEditText.setText(String.valueOf(++i));

            if(changeListener != null)
                changeListener.onChangeListener(this,true);

        }
    }


    public void setOnChangeListener(OnChangeListener listener) {
        this.changeListener = listener;
    }

    public void setOnOutOfRangeListener(OnOutOfRangeListener listener){
        this.rangeListener = listener;
    }

    public interface OnOutOfRangeListener{
        void onOutOfRange(View view, boolean isTooSmall);
    }

    public interface OnChangeListener extends OnClickListener{
        void onChangeListener(View view, boolean isIncrease);
    }

    public int getNum(){
        return Integer.parseInt(mEditText.getText().toString());
    }

    public void setNum(int num){
        mEditText.setText(String.valueOf(num));
        mEditText.requestLayout();
    }

    public void setMin(int min){
        new Builder(mContext).setMin(min);
    }

    public void setMax(int max){
        new Builder(mContext).setMax(max);
    }

    @SuppressWarnings("WeakerAccess")
    public class Builder {

        public Builder(Context context){
            mContext = context;
        }

        public Builder setTextColor(int color){
            TextColor = color;
            return this;
        }

        public Builder setMax(int max){
            if(max < DEFAULT_MAX && max >Min)
                Max = max;
            return this;
        }

        public Builder setMin(int min){
            if(min > DEFAULT_MIN && min <Max)
                Min = min;
            return this;
        }

        public Builder setImage(@IdRes int id, @IdRes int id2){
            ResId1 = id;ResId2 = id2;
            return this;
        }

        public Builder setTextSize(int size){
            if(size > 0)
                TextSize = size;
            return this;
        }

        public Builder setTypeFace(int type){
            if(type<4 && type >-1)
                TextType = type;
            return this;
        }

        public Builder setDEFAULTNum(int num){
            if(num > Min && num <Max)
                DEFAULTNum = num;
            return this;
        }

        public GoodsView build(){
            return new GoodsView(mContext);
        }
    }
}
