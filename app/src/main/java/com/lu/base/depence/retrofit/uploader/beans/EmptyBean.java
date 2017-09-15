package com.lu.base.depence.retrofit.uploader.beans;

/**
 * Created by 陆正威 on 2017/8/2.
 */

public class EmptyBean extends BaseUploadBean {
    EmptyBean(int state){
        super(state);
    }

    public EmptyBean(int state, String filePath) {
        super(state, filePath);
    }

    public EmptyBean(int state, String filePath, Object data) {
        super(state, filePath, data);
    }

    public EmptyBean() {
    }
}
