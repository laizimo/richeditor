package com.lu.base.depence.retrofit.uploader.api;

/**
 * Created by 陆正威 on 2017/7/31.
 */
@SuppressWarnings("unused")
public class UploadState {
    public static int start = 0x000;
    public static int failed = 0x001;
    public static int completed = 0x002;
    //public static int pause = 0x004;
    public static int stop = 0x008;
    public static int uploading = 0x010;
    public static int finish = 0x020;
}
