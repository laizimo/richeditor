package com.lu.richtexteditor.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.lu.myview.customview.Tag;

/**
 * Created by 陆正威 on 2017/9/12.
 */

public class DeleteDialog extends DialogFragment {
    public static final String Tag = "delete_dialog_fragment";
    private View dialog;
    private Long id;
    private OnDialogClickListener listener;

    public static DeleteDialog createDeleteDialog(Long id){
        final DeleteDialog newDialog = new DeleteDialog();
        newDialog.setId(id);
        return newDialog;
    }

    public DeleteDialog(){

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getActivity()).setMessage("是否删除这张图片?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(listener!=null)
                            listener.onConfirmButtonClick(id);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(listener!= null)
                            listener.onCancelButtonClick();
                    }
                }).setTitle("操作").create();
        return dialog;
    }

    public Long getImageId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setListener(OnDialogClickListener listener) {
        this.listener = listener;
    }

    public interface OnDialogClickListener {
        void onConfirmButtonClick(Long id);
        void onCancelButtonClick();
    }
}
