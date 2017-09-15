package com.lu.base.depence.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.lu.base.depence.mvp.Baseinterfaces.BaseView;


/**
 * Created by 陆正威 on 2017/4/3.
 */

public abstract class MvpBaseFragment<V extends BaseView,P extends BasePresenterImpl> extends LazyFragment{
    protected P mPresenter;
    public final String TAG = "MVPBASE";

    public String getTAG(){
        return TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPresenter = createPresent();
        mPresenter.attachView((V)this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mPresenter.detachView();
    }

    protected abstract P createPresent();
}
