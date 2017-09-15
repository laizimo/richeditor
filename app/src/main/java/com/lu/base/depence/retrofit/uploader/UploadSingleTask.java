package com.lu.base.depence.retrofit.uploader;

import android.support.annotation.NonNull;
import android.util.Log;


import com.lu.base.depence.retrofit.RetrofitClient;
import com.lu.base.depence.retrofit.uploader.api.UploadState;
import com.lu.base.depence.retrofit.uploader.api.Uploader;
import com.lu.base.depence.retrofit.uploader.beans.BaseUploadBean;
import com.lu.base.depence.retrofit.uploader.beans.RequestBodyWrapper;
import com.lu.base.depence.retrofit.uploader.beans.UploadBeanFactory;
import com.lu.base.depence.retrofit.uploader.beans.UploadProgress;
import com.lu.base.depence.retrofit.uploader.utils.UploadBeanUtil;
import com.lu.base.depence.retrofit.uploader.utils.UploadHelper;
import com.lu.base.depence.rxjava.RxSchedulers;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by 陆正威 on 2017/7/31.
 */
@SuppressWarnings("unused")
public class UploadSingleTask {
    private final String TAG = getClass().getSimpleName();

    private RxUploader.TaskController mTaskController;
    private String mUrl;
    private RequestBodyWrapper mUploadFileRequestBody;

    private Observable<BaseUploadBean> mObserver;
    private Observable<ResponseBody> mUploadObservable;

    private Disposable mUploadDisposable;
    private Disposable mReceiveEventDisposable;

    private Uploader.OnTaskAutoFinishedListener onTaskAutoFinishedListener;

    UploadSingleTask(RxUploader.TaskController taskController, String url, String mediaType, File file, String parName){
        this.mUrl = checkNull(url);
        this.mUploadFileRequestBody = new RequestBodyWrapper(RequestBody.create(MediaType.parse(checkNull(mediaType)),file),mTaskController.getFilePath());

        initUploadObservable(url,checkNull(parName),file,mUploadFileRequestBody);
        initFlowable(url ,mUploadFileRequestBody);
    }

    UploadSingleTask(RxUploader.TaskController taskController, String url, String mediaType, String filePath , String parName){
        this(taskController,url,mediaType,new File(filePath),parName);
    }

    private UploadSingleTask(RxUploader.TaskController controller) {
        this(controller,null,null,"",null);
    }

    public static UploadSingleTask getFakeTask(RxUploader.TaskController controller){
        return new UploadSingleTask(controller);
    }

    public void start(){
        if(mUploadObservable != null && !isRunning())
            mUploadObservable
                .subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mUploadDisposable = d;
            }

            @Override
            public void onNext(@NonNull ResponseBody responseBody) {
                mUploadFileRequestBody.onNext(UploadBeanFactory.completed(mTaskController.getFilePath(),responseBody));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                mUploadFileRequestBody.onError(e);
            }

            @Override
            public void onComplete() {
                Log.d(TAG,"upload completed:"+ mTaskController +mTaskController.getFilePath());
                mUploadFileRequestBody.onComplete();
            }
        });
    }

    public void receiveEvent(Uploader.OnUploadListener listener){
        receiveEvent(listener,0);
    }

    public void receiveEvent(final Uploader.OnUploadListener listener, final int filterNum){
        if(mObserver != null) {
            mObserver
                    .compose(RxSchedulers.<BaseUploadBean>compose())
                    .filter(new Predicate<BaseUploadBean>() {
                        int ignoreNum = filterNum;

                        @Override
                        public boolean test(@NonNull BaseUploadBean baseUploadBean) throws Exception {
                            if (baseUploadBean.getState() == UploadState.finish)
                                ignoreNum--;
                            return ignoreNum <= 0;
                        }
                    })
                    .subscribe(new Consumer<BaseUploadBean>() {
                        @Override
                        public void accept(@NonNull BaseUploadBean bean) throws Exception {
                            if (UploadBeanUtil.isUploading(bean)) {
                                listener.onUploading(bean.getFilePath(), (UploadProgress) bean.getData());
                            }else if(UploadBeanUtil.isFailed(bean)){
                                listener.onFailed(bean.getFilePath(), (Throwable) bean.getData());
                            }else if(UploadBeanUtil.isCompleted(bean)){
                                listener.onCompleted(bean.getFilePath(), (ResponseBody) bean.getData());
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            listener.onFailed(mTaskController.getFilePath(),throwable);
                            stopReceiveEvent();
                        }
                    }, new Action() {
                        @Override
                        public void run() throws Exception {
                            Log.d(TAG,"upload receive completed:"+ mTaskController.toString());
                        }
                    }, new Consumer<Disposable>() {
                        @Override
                        public void accept(@NonNull Disposable disposable) throws Exception {
                            mReceiveEventDisposable = disposable;
                            listener.onStart();
                        }
                    });
        }
    }

    public boolean stopReceiveEvent(){
        if(mReceiveEventDisposable != null && !mReceiveEventDisposable.isDisposed()) {
            mReceiveEventDisposable.dispose();
            return true;
        }
        return false;
    }

    public boolean cancel(){
        if(isRunning()) {
            mUploadDisposable.dispose();
            return true;
        }
        return false;
    }

    public boolean isRunning(){
        return mUploadDisposable != null && !mUploadDisposable.isDisposed();
    }

    public RxUploader.TaskController getTaskController() {
        return mTaskController;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getFilePath() {
        return mTaskController.getFilePath();
    }

    void setOnTaskAutoFinishedListener(Uploader.OnTaskAutoFinishedListener onTaskAutoFinishedListener) {
        this.onTaskAutoFinishedListener = onTaskAutoFinishedListener;
    }

    private void initUploadObservable(String url, String parName, final File file, RequestBodyWrapper requestBody){
        MultipartBody.Part part = UploadHelper.generateMultiPart(parName,file,checkNull(requestBody));

        mUploadObservable = RetrofitClient.getInstance()
                .getRetrofit()
                .create(UploadHelper.Api.class)
                .uploadFile(url, part)
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload disposed");
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.d(TAG,"upload error:"+throwable.getMessage());
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload completed:"+ mTaskController.toString());
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload finally"+ mTaskController.toString());
                        //stopReceiveEvent();
                        if(onTaskAutoFinishedListener != null){
                            onTaskAutoFinishedListener.onTaskAutoFinished(mTaskController);
                        }
                    }
                }
                )
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload terminate:"+ mTaskController.toString());

                    }
                })
                .compose(RxSchedulers.<ResponseBody>compose());
    }

    private void initFlowable(String filePath,RequestBodyWrapper requestBody){
        mObserver = UploadHelper.generateFlowable(requestBody,filePath)
                .toObservable()
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        Log.d(TAG,"upload receive start:"+ mTaskController.toString() );
                    }
                })
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload receive dispose:"+ mTaskController.toString()  );
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload receive completed:"+ mTaskController.toString() );
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload receive finally:"+ mTaskController.toString() );
                    }
                })
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload receive terminate:"+ mTaskController.toString() );
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"upload receive after_terminate:"+ mTaskController.toString() );
                    }
                })

        ;
    }

    private void tryToStopReceive(){
        mUploadFileRequestBody.onComplete();
    }

    private <T> T checkNull(T t){
        if(t != null)
            return t;
        else
            throw new NullPointerException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UploadSingleTask that = (UploadSingleTask) o;

        return mTaskController.equals(that.mTaskController);

    }

    @Override
    public int hashCode() {
        return mTaskController.hashCode();
    }
}
