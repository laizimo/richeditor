package com.lu.base.depence.rxjava;



import com.lu.base.depence.basebeans.HttpResponseBase;

import io.reactivex.functions.Function;

/**
 * Created by 陆正威 on 2017/4/6.
 */
@SuppressWarnings({"unused"})
public class HttpResBase2Object<T> implements Function<HttpResponseBase<T>,T> {

    @Override
    public T apply(HttpResponseBase<T> tHttpResponseBase) throws Exception {
        if(tHttpResponseBase.isSucceful() && tHttpResponseBase.getData() != null) {
            return tHttpResponseBase.getData();
        }else{
            throw new Exception(tHttpResponseBase.getError());
        }
    }
}
