package com.lu.base.depence.retrofit.uploader.utils;


import com.lu.base.depence.retrofit.uploader.api.UploadState;
import com.lu.base.depence.retrofit.uploader.beans.BaseUploadBean;

/**
 * Created by 陆正威 on 2017/8/2.
 */

public class UploadBeanUtil {
    public static boolean isFailed(BaseUploadBean bean){
        return bean.getState() == UploadState.failed;
    }

    public static boolean isCompleted(BaseUploadBean bean){
        return bean.getState() == UploadState.completed;
    }

    public static boolean isUploading(BaseUploadBean bean){
        return bean.getState() == UploadState.uploading;
    }

    public static boolean isFinished(BaseUploadBean bean){
        return bean.getState() == UploadState.finish;
    }
}
