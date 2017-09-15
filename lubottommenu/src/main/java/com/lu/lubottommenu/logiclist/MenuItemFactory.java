package com.lu.lubottommenu.logiclist;

import android.content.Context;
import android.view.View;

import com.lu.lubottommenu.menuitem.ImageViewItem;
import com.lu.lubottommenu.menuitem.TextViewItem;
import com.lu.lubottommenu.menuitem.ToggleButtonItem;
import com.lu.myview.customview.ToggleButton;

/**
 * Created by 陆正威 on 2017/9/6.
 */

public class MenuItemFactory {

    public static MenuItem generateMenuItem(long id, View contentView){
        return new MenuItem(id,contentView);
    }

    public static ImageViewItem generateImageItem(Context context,long id, int uri,boolean b){
        return new ImageViewItem(context,generateMenuItem(id,null),uri,b);
    }

    public static ImageViewItem generateImageItem(Context context,long id, int uri){
        return new ImageViewItem(context,generateMenuItem(id,null),uri);
    }

    public static TextViewItem generateTextItem(Context context, long id,String text){
        return new TextViewItem(context,generateMenuItem(id,null), text);
    }

    public static ToggleButtonItem generateToggleButtonItem(Context context, long id, int idres){
        return new ToggleButtonItem(context,generateMenuItem(id,null), idres);
    }

}
