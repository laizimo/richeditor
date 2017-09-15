package com.lu.base.depence.rxjava;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by 陆正威 on 2017/4/7.
 */

public abstract class Notify implements Serializable{
    private final static Long TAG = 20170407L;

    private final int id;
    public final static int NORMAL = 0;
    public final static int CRITICAL = 1;
    public final static int FAKE = 3;
    private String title = "";
    private String content = "";
    private int State = NORMAL;

    public   Notify(){
        id = 0;
    }
    public Notify(int id) {
        this.id = id;
    }

    public Notify(int id,String title,String content){
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public Notify(String title){
        this.id = 1;
        this.title = title;
    }

    public Notify(String title,int state){
        this.id = 1;
        this.title = title;
        this.State = (Math.abs(state) > 3 ? 0 : Math.abs(state));
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getImageId() {
        return id;
    }
}
