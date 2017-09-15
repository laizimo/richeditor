package com.lu.base.depence.retrofit.uploader.beans;

/**
 *
 * Created by 陆正威 on 2017/8/2.
 */
@SuppressWarnings("unused")
public class UploadProgress {
    private long writtenBytes;
    private long totalBytes;

    UploadProgress(long writtenBytes,long totalBytes){
        this.totalBytes = totalBytes;
        this.writtenBytes = writtenBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getWrittenBytes() {
        return writtenBytes;
    }

    public float getProgress(){
        if(totalBytes > 0){
            return (float) ((double)writtenBytes / (double)totalBytes)*100;
        }
        return 0f;
    }
}
