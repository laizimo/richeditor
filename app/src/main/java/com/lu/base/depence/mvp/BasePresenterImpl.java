package com.lu.base.depence.mvp;


import android.support.annotation.NonNull;


import com.lu.base.depence.mvp.Baseinterfaces.BasePresenter;
import com.lu.base.depence.mvp.Baseinterfaces.BaseView;

import java.lang.ref.SoftReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


/**
 * Created by 陆正威 on 2017/3/31.
 */

/********************************************************************************************
 *
 * 解决presenter因为持有View的引用而导致的内存泄露
 * 这里持有View的软引用并在下层（Activity层）调用和
 * 管理View的生命周期
 *
 ********************************************************************************************/
public abstract class BasePresenterImpl<T extends BaseView> implements BasePresenter {
    private SoftReference<T> mBaseView;
    private CompositeDisposable compositeDisposable;
    private boolean hasConnected;

    public BasePresenterImpl() {
        hasConnected = false;
        compositeDisposable = new CompositeDisposable();
        mBaseView = null;
    }

    void attachView(@NonNull T t) {
        //Log.e("attach",t.getClass().getSimpleName());
        mBaseView = new SoftReference<>(t);
        hasConnected = true;
    }

    protected T getView() {
        return mBaseView == null ? null:mBaseView.get();
    }

    void detachView() {
        removeAllDisposables();
        if (mBaseView != null) {
            //Log.e("detachView","");
            mBaseView.clear();
            mBaseView = null;
            hasConnected = false;
        }
    }

    public boolean isHasConnected() {
        return hasConnected;
    }

    public void addCompositeDisposables(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    public void removeCompositeDisposables(Disposable disposable) {
        compositeDisposable.remove(disposable);
    }

    public void removeAllDisposables() {
        compositeDisposable.clear();
    }

    public void start() {

    }
}
