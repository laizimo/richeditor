package com.lu.base.depence.retrofit.uploader.beans;


import com.lu.base.depence.retrofit.uploader.api.UploadState;

import okhttp3.ResponseBody;

/**
 * Created by 陆正威 on 2017/8/2.
 */

public class SuccessBean extends BaseUploadBean<ResponseBody> {
    SuccessBean(String filePath,ResponseBody responseBody){
        super(UploadState.completed,filePath,responseBody);
    }
}
