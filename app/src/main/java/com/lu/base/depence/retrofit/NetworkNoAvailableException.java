package com.lu.base.depence.retrofit;

/**
 * Created by 陆正威 on 2017/8/1.
 */

public class NetworkNoAvailableException extends Exception {
    public NetworkNoAvailableException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
