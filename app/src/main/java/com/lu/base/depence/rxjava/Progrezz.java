package com.lu.base.depence.rxjava;

/**
 * Created by 陆正威 on 2017/4/7.
 */

//this is a clazz that can count the completeabe of one thing, if reach the end_var it well notify any one who has observed it;

@SuppressWarnings({"unused"})
public class Progrezz {
    private String TAG;
    private int begin;
    private int end;
    private volatile int here;
    private final boolean autoreset;
    private OnProgrezzListener onProgrezzListener;
    private OnProgrezzEndListener onProgrezzEndListener;

    @SuppressWarnings({"WeakerAccess"})
    public interface OnProgrezzListener {
        void onStart();

        void onEnd();

        void onWhere(int here);
    }

    @SuppressWarnings({"WeakerAccess"})
    public interface OnProgrezzEndListener {
        void onEnd();
    }

    public void setOnProgrezzEndListener(OnProgrezzEndListener onProgrezzEndListener){
        this.onProgrezzEndListener = onProgrezzEndListener;
    }

    public void setOnProgrezzListener(OnProgrezzListener onProgrezzListener) {
        this.onProgrezzListener = onProgrezzListener;
    }

    public static Progrezz create(String tag , int end){
        return (new Progrezz(end)).setTAG(tag);
    }

    public static Progrezz create(String tag ,int begin,int end){
        return (new Progrezz(begin,end)).setTAG(tag);
    }

    public static Progrezz create(String tag ,int begin,int end,boolean g){
        return (new Progrezz(begin,end,g)).setTAG(tag);
    }

    public static Progrezz create(String tag,int end,boolean g){
        return (new Progrezz(end,g)).setTAG(tag);
    }

    private Progrezz(int begin, int end) {
        if (begin > end) {
            int temp = begin;
            begin = end;
            end = temp;
        }
        this.begin = begin;
        this.end = end;
        autoreset = true;
        TAG = "NOMAL_TAG";
        here = begin;
    }

    @SuppressWarnings({"WeakerAccess"})
    public Progrezz(int begin, int end, boolean isAutoreset) {
        if (begin > end) {
            int temp = begin;
            begin = end;
            end = temp;
        }
        this.begin = begin;
        this.end = end;
        autoreset = isAutoreset;
        TAG = "NOMAL_TAG";
        here = begin;
    }

    private Progrezz(int end) {
        if (end < 0) {
            end = -end;
        }
        this.begin = 0;
        this.end = end;
        autoreset = true;
        TAG = "NOMAL_TAG";
        here = begin;
    }

    private Progrezz(int end, boolean isautoreset) {
        if (end < 0) {
            end = -end;
        }
        this.begin = 0;
        this.end = end;
        autoreset = isautoreset;
        TAG = "NOMAL_TAG";
        here = begin;
    }

    private Progrezz setTAG(String TAG) {
        this.TAG = TAG;
        return this;
    }

    public void go(int step) {
        if (step <= 0) {
            step = -step;
        }
        synchronized (this) {
            here += step;
            if(here >= end){
                if(onProgrezzListener != null)
                    onProgrezzListener.onEnd();
                if(onProgrezzEndListener != null)
                    onProgrezzEndListener.onEnd();
                if(autoreset){
                    here = begin;
                    if (onProgrezzListener != null) {
                        onProgrezzListener.onWhere(here);
                        onProgrezzListener.onStart();
                    }
                }else{
                    here = end;
                    if (onProgrezzListener != null) {
                        onProgrezzListener.onWhere(here);
                    }
                }
            }else{
                if (onProgrezzListener != null)
                    onProgrezzListener.onWhere(here);
            }
        }
    }

    public void go(){
        here++;
        if(here >= end){
            if(onProgrezzListener != null)
                onProgrezzListener.onEnd();
            if(onProgrezzEndListener != null)
                onProgrezzEndListener.onEnd();

            if(autoreset){
                here = begin;
                if(onProgrezzListener != null) {
                    onProgrezzListener.onWhere(here);
                    onProgrezzListener.onStart();
                }
            }else{
                here = end;
                if (onProgrezzListener != null)
                    onProgrezzListener.onWhere(here);
            }
        }else{
            if (onProgrezzListener != null)
                onProgrezzListener.onWhere(here);
        }
    }

    public String getTAG() {
        return TAG;
    }

    public synchronized void reset(){
        here = begin;
        if (onProgrezzListener != null) {
            onProgrezzListener.onStart();
            onProgrezzListener.onWhere(here);
        }
    }
}