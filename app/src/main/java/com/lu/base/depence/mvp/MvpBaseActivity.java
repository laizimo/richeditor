package com.lu.base.depence.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.SupportActivity;

import com.lu.base.depence.mvp.Baseinterfaces.BaseView;
import com.lu.base.depence.tools.Utils;


/**
 * Created by 陆正威 on 2017/3/31.
 */

/************************************************************************************************************************
 *
 * 泛型参数V ，P 分别代表View层和Presenter层，其中Presenter层持有View层的弱引用并已经对其做好管理函数，attach是将View
 * 存入Presenter层的函数，detach则反之，在Activity（View）创建时，将本身传入Presenter层，作为软引用，不会影响View的释放
 * 在View destroy时解除引用。
 *
 ***********************************************************************************************************************/



public abstract class MvpBaseActivity<V extends BaseView,P extends BasePresenterImpl> extends SupportActivity {

    public final String TAG = "MvpBaseActivity";
    protected P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(isTransparent())
            Utils.TRANSPARENT(getWindow());
        mPresenter = createPresent();
        mPresenter.attachView((V)this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    protected abstract P createPresent();
    protected abstract boolean isTransparent();

    public String getTAG(){
        return TAG;
    }
}
