package com.lu.base.depence.tools;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by 陆正威 on 2017/4/17.
 */

public class StatusBarUtils {
    public static int getHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        if (isFlymeOs4x()) {
            return 2 * statusBarHeight;
        }

        return statusBarHeight;
    }

    public static boolean isFlymeOs4x() {
        String sysVersion = android.os.Build.VERSION.RELEASE;
        if ("4.4.4".equals(sysVersion)) {
            String sysIncrement = android.os.Build.VERSION.INCREMENTAL;
            String displayId = android.os.Build.DISPLAY;
            if (!TextUtils.isEmpty(sysIncrement)) {
                return sysIncrement.contains("Flyme_OS_4");
            } else {
                return displayId.contains("Flyme OS 4");
            }
        }
        return false;
    }
}
