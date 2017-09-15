package com.lu.base.depence.retrofit.uploader.beans;

/**
 * Created by 陆正威 on 2017/7/31.
 */
@SuppressWarnings("unused")
public abstract class BaseUploadBean<T> {
    private String filePath;
    private int state;
    T data;

    BaseUploadBean(){
    }

    BaseUploadBean(int state){
        this(state,null);
    }

    BaseUploadBean(int state, String filePath){
        this(state,filePath,null);
    }

    BaseUploadBean(int state, String filePath, T data){
        this.state = state;
        this.filePath = filePath;
        this.data = data;
    }

    public int getState() {
        return state;
    }

    public String getFilePath() {
        return filePath;
    }

    public T getData(){
        return data;
    }
}
