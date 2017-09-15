package com.lu.richtexteditor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.support.v7.app.AlertDialog;

import com.imnjh.imagepicker.FileChooseInterceptor;
import com.imnjh.imagepicker.PickerAction;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by 陆正威 on 2017/9/12.
 */
public class SingleFileLimitInterceptor implements FileChooseInterceptor {

    private static final long MAX_FILE_SIZE_ORIGINAL = 200 * 1024; // 200K

    public SingleFileLimitInterceptor() {}

    @Override
    public boolean onFileChosen(Context context, ArrayList<String> selectedPic,
                                boolean original,
                                int resultCode, PickerAction action) {
        if (resultCode != Activity.RESULT_OK) {
            return true;
        }
        if (original) {
            ArrayList<String> confirmedFiles = new ArrayList<>();
            for (String filePath : selectedPic) {
                File file = new File(filePath);
                if (file.exists()) {
                    if (file.length() <= MAX_FILE_SIZE_ORIGINAL) {
                        confirmedFiles.add(filePath);
                    }
                }
            }
            if (confirmedFiles.size() < selectedPic.size()) {
                showSingleFileLimitDialog(context, original, resultCode, action, confirmedFiles);
                return false;
            }
        }
        return true;
    }

    private void showSingleFileLimitDialog(Context context, final boolean original,
                                           final int resultCode,
                                           final PickerAction action, final ArrayList<String> confirmedFiles) {
        new AlertDialog.Builder(context)
                .setMessage("")
                .setPositiveButton(
                        R.string.general_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                action.proceedResultAndFinish(confirmedFiles, original, resultCode);
                            }
                        })
                .setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    protected SingleFileLimitInterceptor(Parcel in) {}

    public static final Creator<SingleFileLimitInterceptor> CREATOR =
            new Creator<SingleFileLimitInterceptor>() {
                @Override
                public SingleFileLimitInterceptor createFromParcel(Parcel source) {
                    return new SingleFileLimitInterceptor(source);
                }

                @Override
                public SingleFileLimitInterceptor[] newArray(int size) {
                    return new SingleFileLimitInterceptor[size];
                }
            };
}