package com.lu.base.depence.tools;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import static android.os.Build.VERSION.SDK_INT;


/**
 * Created by 陆正威 on 2017/4/6.
 */

public class Utils {

    public static void TRANSPARENT(Window window){

        if(SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }else {
            if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WindowManager.LayoutParams localLayoutParams = window.getAttributes();
                localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
            }
        }
    }

    public static boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) AppManager.app().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static void MakeToast(boolean isLong, String text){
        Toast.makeText(AppManager.app(),text,isLong ? Toast.LENGTH_LONG:Toast.LENGTH_SHORT).show();
    }

    public static void MakeShortToast(String text){
        Toast.makeText(AppManager.app(),text,Toast.LENGTH_SHORT).show();
    }

    public static void MakeLongToast(String text){
        Toast.makeText(AppManager.app(),text,Toast.LENGTH_LONG).show();
    }

    static int[] getScreenSize() {
        WindowManager wm = (WindowManager) AppManager.app().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
    }

    public static int dip2px(float dpValue) {
        final float scale = AppManager.app().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue) {
        final float scale = AppManager.app().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }




}
