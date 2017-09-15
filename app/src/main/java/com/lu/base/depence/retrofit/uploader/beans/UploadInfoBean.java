package com.lu.base.depence.retrofit.uploader.beans;


import com.lu.base.depence.retrofit.uploader.api.UploadState;

/**
 *
 * Created by 陆正威 on 2017/8/2.
 */

public class UploadInfoBean extends BaseUploadBean<UploadProgress> {
    public UploadInfoBean(String filePath, UploadProgress data) {
        super(UploadState.uploading, filePath, data);
    }
}
