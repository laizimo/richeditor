package com.lu.base.depence.retrofit.uploader.beans;


import com.lu.base.depence.retrofit.uploader.api.UploadState;

/**
 * Created by 陆正威 on 2017/8/2.
 */

public class FailedBean extends BaseUploadBean<Throwable> {
    FailedBean(String filePath,Throwable data){
        super(UploadState.failed,filePath,data);
    }
}
