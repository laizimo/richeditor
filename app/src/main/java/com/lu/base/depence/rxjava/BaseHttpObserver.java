package com.lu.base.depence.rxjava;

import android.util.Log;

import com.lu.base.depence.basebeans.HttpResponseBase;

import io.reactivex.Observer;


/**
 * Created by 陆正威 on 2017/4/6.
 */
@SuppressWarnings({"unused"})
public abstract class BaseHttpObserver<T> implements Observer<HttpResponseBase<T>> {
    private static final String TAG = "BaseHttpObserver";

    @Override
    public void onNext(HttpResponseBase<T> value) {
        if (value.isSucceful()) {
            T t = value.getData();
            onHandleSuccess(t);
        } else {
            onHandleError(value.getMessage());
        }
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "error:" + e.toString());
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "onComplete");
    }

    @SuppressWarnings({"WeakerAccess"})
    protected abstract void onHandleSuccess(T t);

    @SuppressWarnings({"WeakerAccess"})
    protected void onHandleError(String msg) {

    }
}
