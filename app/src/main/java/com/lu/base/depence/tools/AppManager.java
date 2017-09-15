package com.lu.base.depence.tools;

import android.app.ActivityManager;
import android.app.Application;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.imnjh.imagepicker.PickerConfig;
import com.imnjh.imagepicker.SImagePicker;
import com.lu.base.depence.fresco.FrescoCacheParams;
import com.lu.base.depence.fresco.FrescoImageLoader;
import com.lu.base.depence.retrofit.RetrofitClient;

import java.io.File;


/**
 * Created by 陆正威 on 2017/4/6.
 */

public class AppManager extends Application {
    private static AppManager context;
    private ActivityManager activityManager;
//    private HttpProxyCacheServer proxy;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        IjkMediaPlayer.loadLibrariesOnce(null);
//        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        ScreenParamsInit();
        FrescoInit();
        SImagePicker.init(new PickerConfig.Builder().setAppContext(this)
                .setImageLoader(new FrescoImageLoader())
                .build());
    }

    public static AppManager app() {
        return context;
    }

    private void ScreenParamsInit() {
        int a[] = Utils.getScreenSize();
        Constant.screenwithpx = a[0];
        Constant.screenheightpx = a[1];
        Constant.screenwithdp = Utils.px2dip(Constant.screenwithpx);
        Constant.screenheightdp = Utils.px2dip(Constant.screenheightpx);
    }

//    public static HttpProxyCacheServer getProxy() {
//        if(context == null)
//            return null;
//        return context.proxy == null ? (context.proxy = context.newProxy()) : context.proxy;
//    }
//
//    private HttpProxyCacheServer newProxy() {
//        return new HttpProxyCacheServer(this);
//    }

//    private void GreenDaoInit(){
//        DBManager.initialize(this);
//    }

    private void FrescoInit() {
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setMaxCacheSize(40 * ByteConstants.MB)
                .setBaseDirectoryPathSupplier(new Supplier<File>() {
                    @Override
                    public File get() {
                        return getCacheDir();
                    }
                })
                .build();

        final FrescoCacheParams bitmapCacheParams = new FrescoCacheParams(activityManager);
        //Set<RequestListener> listeners = new HashSet<>();
        ImagePipelineConfig imagePipelineConfig = OkHttpImagePipelineConfigFactory.newBuilder(this, RetrofitClient.getInstance().getOkHttpClient())
                .setMainDiskCacheConfig(diskCacheConfig)
                .setBitmapMemoryCacheParamsSupplier(bitmapCacheParams)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, imagePipelineConfig);
    }

//    public static String getBK1() {
//        return BuildConfig.localKey1;
//    }
//    public static String getBK2(){
//        return app().getResources().getString(R.string.local_key2);
//    }
//    public static String getBK0(){ return getBK1().substring(0,16).replace("q","f").substring(4,8);}

//    public static String getDefaultBK(){
//        return getBK0().concat(getBK1().substring(4).concat(getBK2()).concat("l"));
//    }
}
