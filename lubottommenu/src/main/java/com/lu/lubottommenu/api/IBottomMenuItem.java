package com.lu.lubottommenu.api;

import android.view.View;

import com.lu.lubottommenu.logiclist.MenuItem;

/**
 * Created by 陆正威 on 2017/9/6.
 */

public interface IBottomMenuItem {
    Long getItemId();
    View getMainView();

    interface OnIteClickListener {
        void onItemClick(MenuItem item);
    }
}
