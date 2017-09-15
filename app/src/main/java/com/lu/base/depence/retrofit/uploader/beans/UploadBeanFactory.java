package com.lu.base.depence.retrofit.uploader.beans;

import okhttp3.ResponseBody;

/**
 * Created by 陆正威 on 2017/8/2.
 */
@SuppressWarnings("unused")
public class UploadBeanFactory {
    public static BaseUploadBean<Throwable> failed(String filePath, Throwable throwable){
        return new FailedBean(filePath,throwable);
    }
    public static BaseUploadBean<UploadProgress> uploading(String filePath, UploadProgress progress){
        return new UploadInfoBean(filePath,progress);
    }
    public static BaseUploadBean<UploadProgress> uploading(String filePath, long progress1, long progress2){
        return new UploadInfoBean(filePath,new UploadProgress(progress1,progress2));
    }
    public static BaseUploadBean<ResponseBody> completed(String filePath, ResponseBody responseBody){
        return new SuccessBean(filePath,responseBody);
    }
    public static BaseUploadBean empty(){
        return new EmptyBean();
    }

    public static BaseUploadBean empty(int state){
        return new EmptyBean(state);
    }
}
