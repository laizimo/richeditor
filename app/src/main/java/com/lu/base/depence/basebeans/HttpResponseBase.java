package com.lu.base.depence.basebeans;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by 陆正威 on 2017/4/5.
 */

public class HttpResponseBase<T> implements Serializable{

    @SerializedName("message")
    private String message;
    @SerializedName("state")
    private int state;
    @SerializedName("data")
    private T data;

    public boolean isSucceful()
    {
        return state>=0;
    }

    public String getError()
    {
        return message == null ? "":message;
    }

    protected HttpResponseBase(String message, int state, T data) {
        this.message = message;
        this.state = state;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
