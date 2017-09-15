package com.lu.base.depence.fresco;

import android.app.ActivityManager;
import android.os.Build;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.cache.MemoryCacheParams;

/**
 * Created by 陆正威 on 2017/4/8.
 */

public class FrescoCacheParams implements Supplier<MemoryCacheParams> {
    private ActivityManager activityManager;

    public FrescoCacheParams(ActivityManager activityManager){
        this.activityManager = activityManager;
    }
    @Override
    public MemoryCacheParams get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new MemoryCacheParams(
                    getMaxCacheSize(),
                    Integer.MAX_VALUE,
                    getMaxCacheSize(),
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);
        }else {
            return new MemoryCacheParams(
                    getMaxCacheSize(),
                    Integer.MAX_VALUE,
                    getMaxCacheSize(),
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);
        }
    }
    private int getMaxCacheSize(){
        final int maxMemory = Math.min(activityManager.getMemoryClass() * ByteConstants.MB, Integer.MAX_VALUE);
        if(maxMemory < 32 *ByteConstants.MB){
            return 4 * ByteConstants.MB;
        }else if(maxMemory < 64* ByteConstants.MB){
            return 6 * ByteConstants.MB;
        }else {
            return maxMemory/4;
        }
    }
}
