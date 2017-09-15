package com.lu.base.depence.retrofit.uploader.beans;

import android.support.annotation.NonNull;


import com.lu.base.depence.retrofit.uploader.api.UploadState;

import java.io.IOException;

import io.reactivex.Emitter;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * RequestBody的包装类，增加对其写入的监听，近似的监听上传速度
 * Emitter的包装类。
 * Created by 陆正威 on 2017/7/30.
 */
@SuppressWarnings({"WeakerAccess"})
public class RequestBodyWrapper extends RequestBody implements Emitter<BaseUploadBean> {

    private RequestBody mRequestBody;
    private String mFilePath;
    private FlowableProcessor<BaseUploadBean> mUploadProcessor;

    public RequestBodyWrapper(@NonNull RequestBody requestBody, String filePath) {
        this.mRequestBody = requestBody;
        this.mFilePath = filePath;
        this.mUploadProcessor = BehaviorProcessor.create();
    }

    public FlowableProcessor<BaseUploadBean> getUploadProcessor() {
        return mUploadProcessor;
    }

    @Override
    public void onNext(BaseUploadBean baseUploadBean){
        if(mUploadProcessor!=null) {
            mUploadProcessor.onNext(baseUploadBean);
        }
    }

    @Override
    public void onError(Throwable throwable){
        if(mUploadProcessor != null && throwable != null) {
            mUploadProcessor.onNext(UploadBeanFactory.failed(mFilePath,throwable));
        }
    }

    @Override
    public void onComplete() {
        if(mUploadProcessor != null ) {
            mUploadProcessor.onComplete();
        }
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink)throws IOException{
        CountingSink countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        mRequestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
        onNext(UploadBeanFactory.empty(UploadState.finish));
    }

    private class CountingSink extends ForwardingSink {
        private long bytesWritten = 0;
        CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void close() throws IOException {
            super.close();
        }

        @Override
        public void write(okio.Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            if(bytesWritten < contentLength())
                onNext(UploadBeanFactory.uploading(mFilePath, bytesWritten,contentLength()));
        }
    }

}
