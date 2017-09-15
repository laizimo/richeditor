package com.lu.base.depence.retrofit.uploader.api;

import com.lu.base.depence.retrofit.uploader.RxUploader;
import com.lu.base.depence.retrofit.uploader.beans.UploadProgress;

import okhttp3.ResponseBody;

/**
 * Created by 陆正威 on 2017/8/1.
 */
public interface Uploader{
    interface UploadTaskInfo {
        String getUUID();
    }

    interface OnTaskAutoFinishedListener {
        void onTaskAutoFinished(RxUploader.TaskController taskController);
    }

    interface OnUploadListener{
        void onStart();
        void onUploading(String filePath, UploadProgress progress);
        void onCompleted(String filePath, ResponseBody responseBody);
        void onFailed(String filePath, Throwable throwable);
    }

    interface AfterOperationTaskHandler extends TaskHandler{
        void preHandleSuccess(Long fromTaskId, RxUploader.TaskController taskController);
    }

    interface TaskHandler{
        void handle(Long fromTaskId, RxUploader.TaskController taskController);
        void touchTaskFailed();
    }
}
