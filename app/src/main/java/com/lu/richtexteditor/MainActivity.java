package com.lu.richtexteditor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.lu.base.depence.retrofit.uploader.RxUploader;
import com.lu.base.depence.tools.SizeUtil;
import com.lu.base.depence.tools.Utils;
import com.lu.lubottommenu.LuBottomMenu;
import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.logiclist.MenuItem;
import com.lu.lubottommenu.logiclist.MenuItemFactory;
import com.lu.myview.customview.richeditor.RichEditor;
import com.lu.richtexteditor.dialogs.DeleteDialog;
import com.lu.richtexteditor.dialogs.LinkDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import static com.lu.myview.customview.richeditor.RichEditor.Type.BLOCKQUOTE;
import static com.lu.myview.customview.richeditor.RichEditor.Type.BOLD;
import static com.lu.myview.customview.richeditor.RichEditor.Type.ITALIC;
import static com.lu.myview.customview.richeditor.RichEditor.Type.STRIKETHROUGH;


public class MainActivity extends AppCompatActivity implements IBottomMenuItem.OnIteClickListener {

    private static final int REQUEST_CODE_IMAGE = 101;
    private LuBottomMenu mLuBottomMenu;
    private RichEditor mRichTextView;

    private SelectController controller;//单选控制器
    private ArrayList<String> selImageList; //当前选择的所有图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selImageList = new ArrayList<>();
        initView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int id = 0;
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE) {
            final ArrayList<String> pathList =
                    data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);
            selImageList = pathList;
            for (String path :
                    pathList) {
                long size[] = SizeUtil.getBitmapSize(path);
                mRichTextView.insertImage(path, size[0], size[1], id++);
                mRichTextView.setImageUploadProcess(path,20);
            }
            final boolean original =
                    data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
        }
    }

    private void initView() {
        mLuBottomMenu = (LuBottomMenu) findViewById(R.id.lu_bottom_menu);
        mLuBottomMenu.setOnItemClickListener(this);
        initBottomMenu(mLuBottomMenu);
        mRichTextView = (RichEditor) findViewById(R.id.rich_text_view);
        initRichTextViewListeners(mRichTextView);
        initImagePicker();
    }

    private void initImagePicker() {

    }

    private void initRichTextViewListeners(final RichEditor editor) {
        editor.setOnDecorationChangeListener(new RichEditor.OnDecorationStateListener() {
            @Override
            public void onStateChangeListener(String text, List<RichEditor.Type> types) {
                for (long i = BOLD.getTypeCode(); i <= STRIKETHROUGH.getTypeCode(); i++) {
                    mLuBottomMenu.setItemSelected(i, false);
                }
                controller.reset();
                for (RichEditor.Type t :
                        types) {
                    if(!controller.contain(t.getTypeCode()))
                        mLuBottomMenu.setItemSelected(t.getTypeCode(), true);
                    else
                        controller.changeState(t.getTypeCode());

                }
            }
        });
        editor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                //Log.e("onTextChange", text);
            }
        });
        editor.setOnFocusChangeListener(new RichEditor.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean isFocus) {
                if (!isFocus) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mLuBottomMenu.show(200);
                    }
                } else {
                    mLuBottomMenu.hide(200);
                }

            }
        });
        editor.setOnLinkClickListener(new RichEditor.OnLinkClickListener() {
            @Override
            public void onLinkClick(String linkName, String url) {
                showLinkDialog(LinkDialog.createLinkDialog(linkName, url), true);
            }
        });
        editor.setOnImageClickListener(new RichEditor.OnImageClickListener() {
            @Override
            public void onImageClick(long id) {
                showDeleteDialog(DeleteDialog.createDeleteDialog(id));
            }
        });

//        editor.setOnInitialLoadListener(new RichEditor.AfterInitialLoadListener() {
//            @Override
//            public void onAfterInitialLoad(boolean isReady) {
//                if(isReady)
//                    mRichTextView.focusEditor();
//            }
//        });
    }

    private void initBottomMenu(LuBottomMenu luBottomMenu) {
        luBottomMenu.
                addRootItem(MenuItemFactory.generateImageItem(this, 0x01, R.drawable.insert_image, false)).
                addRootItem(MenuItemFactory.generateImageItem(this, 0x02, R.drawable.a)).
                addRootItem(MenuItemFactory.generateImageItem(this, 0x03, R.drawable.more)).
                addRootItem(MenuItemFactory.generateImageItem(this, 0x04, R.drawable.back, false)).
                addRootItem(MenuItemFactory.generateImageItem(this, 0x05, R.drawable.redo, false)).

                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x06, R.drawable.bold_d)).
                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x07, R.drawable.italic_d)).
                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x08, R.drawable.strikethrough_d)).
                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x09, R.drawable.blockquote_d)).
                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x0a, R.drawable.h1)).
                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x0b, R.drawable.h2)).
                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x0c, R.drawable.h3)).
                addItem(0x02, MenuItemFactory.generateImageItem(this, 0x0d, R.drawable.h4)).
                addItem(0x03, MenuItemFactory.generateImageItem(this, 0x0e, R.drawable.halving_line, false)).
                addItem(0x03, MenuItemFactory.generateImageItem(this, 0x0f, R.drawable.link, false));

        controller = SelectController.createController();
        controller.addAll(0x09L, 0x0aL, 0x0bL, 0x0cL, 0x0dL);
        controller.setHandler(new SelectController.StatesTransHandler() {
            @Override
            public void handleA2B(long id) {
                if (id > 0)
                    mLuBottomMenu.setItemSelected(id, true);
            }

            @Override
            public void handleB2A(long id) {
                if (id > 0)
                    mLuBottomMenu.setItemSelected(id, false);
            }
        });
    }

    @Override
    public void onItemClick(MenuItem item) {
        final long id = item.getId();

        if (controller.contain(id)) {
            if (id > 0x09) {
                mRichTextView.setHeading((int) (id - 0x09),
                        mLuBottomMenu.isItemSelected2(item) == 1);
            }
            controller.changeState(id);
        }
        if (id == 0x01) {
            showImagePicker();
        } else if (id == 0x04) {
            mRichTextView.undo();

        } else if (id == 0x05) {
            mRichTextView.redo();

        } else if (BOLD.isMapTo(id)) {
            mRichTextView.setBold();

        } else if (ITALIC.isMapTo(id)) {
            mRichTextView.setItalic();

        } else if (STRIKETHROUGH.isMapTo(id)) {
            mRichTextView.setStrikeThrough();

        } else if (BLOCKQUOTE.isMapTo(id)) {
            mRichTextView.setBlockquote(mLuBottomMenu.isItemSelected2(item) == 1);

        } else if (id == 0x0e) {
            mRichTextView.insertHr();

        } else if (id == 0x0f) {
            showLinkDialog(LinkDialog.createLinkDialog(), false);

        }
    }

    private void showImagePicker(){
        SImagePicker
                .from(MainActivity.this)
                .setSelected(selImageList)
                .maxCount(9)
                .rowCount(3)
                .pickMode(SImagePicker.MODE_IMAGE)
                .fileInterceptor(new SingleFileLimitInterceptor())
                .forResult(REQUEST_CODE_IMAGE);
    }

    private void showLinkDialog(final LinkDialog dialog, final boolean isChange) {
        dialog.setListener(new LinkDialog.OnDialogClickListener() {
            @Override
            public void onConfirmButtonClick(String name, String url) {
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                    Utils.MakeLongToast("不能为空！");
                } else {
                    //do something
                    if (!isChange)
                        mRichTextView.insertLink(url, name);
                    else
                        mRichTextView.changeLink(url, name);
                    onCancelButtonClick();
                }
            }

            @Override
            public void onCancelButtonClick() {
                dialog.dismiss();
            }
        });
        dialog.show(getSupportFragmentManager(), LinkDialog.Tag);
    }

    private void showDeleteDialog(final DeleteDialog dialog) {
        dialog.setListener(new DeleteDialog.OnDialogClickListener() {
            @Override
            public void onConfirmButtonClick(Long id) {
                mRichTextView.deleteImageByUri(id);
            }

            @Override
            public void onCancelButtonClick() {
                //dialog.dismiss();
            }
        });
        dialog.show(getSupportFragmentManager(), DeleteDialog.Tag);
    }
}
