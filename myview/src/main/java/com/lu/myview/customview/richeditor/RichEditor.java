package com.lu.myview.customview.richeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Copyright (C) 2015 Wasabeef
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressWarnings({"unused"})
public class RichEditor extends WebView {

    public enum Type {
        BOLD(0x06),
        ITALIC(0x07),
        STRIKETHROUGH(0x08),
        BLOCKQUOTE(0x09),
        H1(0x0a),
        H2(0x0b),
        H3(0x0c),
        H4(0x0d);

        //SUPERSCRIPT(1),//SUBSCRIPT(2),//UNDERLINE(3),
        private long typeCode;
        Type(long i) {
            typeCode = i;
        }

        public long getTypeCode() {
            return typeCode;
        }

        public boolean isMapTo(long id){
            return typeCode == id;
        }
    }

    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

    public interface OnDecorationStateListener {
        void onStateChangeListener(String text, List<Type> types);
    }

    public interface OnLinkClickListener{
        void onLinkClick(String linkName,String url);
    }

    public interface OnFocusChangeListener{
        void onFocusChange(boolean isFocus);
    }

    public interface AfterInitialLoadListener {
        void onAfterInitialLoad(boolean isReady);
    }

    public interface OnImageClickListener{
        void onImageClick(long id);
    }

    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String CALLBACK_SCHEME = "callback://";
    private static final String STATE_SCHEME = "state://";
    private static final String LINK_CHANGE_SCHEME = "change://";
    private static final String FOCUS_CHANGE_SCHEME = "focus://";
    private static final String IMAGE_CLICK_SCHEME = "image://";
    private boolean isReady = false;
    private String mContents;
    private OnTextChangeListener mTextChangeListener;
    private OnDecorationStateListener mDecorationStateListener;
    private AfterInitialLoadListener mLoadListener;
    private OnScrollChangedCallback mOnScrollChangedCallback;
    private OnLinkClickListener mOnLinkClickListener;
    private OnFocusChangeListener mOnFocusChangeListener;
    private OnImageClickListener mOnImageClickListener;


    public RichEditor(Context context) {
        this(context, null);
    }

    public RichEditor(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public RichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        addJavascriptInterface(new Android4JsInterface(),"AndroidInterface");
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setWebViewClient(createWebViewClient());
        getSettings().setJavaScriptEnabled(true);
        loadUrl(SETUP_HTML);

        applyAttributes(context, attrs);
    }

    protected EditorWebViewClient createWebViewClient() {
        return new EditorWebViewClient();
    }

    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mTextChangeListener = listener;
    }

    public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
        mDecorationStateListener = listener;
    }

    public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
        mLoadListener = listener;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        this.mOnFocusChangeListener = onFocusChangeListener;
    }

    public void setOnLinkClickListener(OnLinkClickListener onLinkClickListener) {
        this.mOnLinkClickListener = onLinkClickListener;
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.mOnImageClickListener = onImageClickListener;
    }

    private void callback(String text) {
        mContents = text.replaceFirst(CALLBACK_SCHEME, "");
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange(mContents);
        }
    }

    private void linkChangeCallBack(String text) {
        text = text.replaceFirst(LINK_CHANGE_SCHEME, "");
        String[] result = text.split("@_@");
        if (mOnLinkClickListener != null && result.length >= 2) {
            mOnLinkClickListener.onLinkClick(result[0],result[1]);
        }
    }

    private void imageClickCallBack(String url){
        if(mOnImageClickListener != null)
            mOnImageClickListener.onImageClick(Long.parseLong(url.replaceFirst(IMAGE_CLICK_SCHEME,"")));
    }



    /**
     * WebView的滚动事件
     *
     * @param l
     * @param t
     * @param oldl
     * @param oldt
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollChangedCallback != null) {
            mOnScrollChangedCallback.onScroll(l - oldl, t - oldt);
        }

    }

    public OnScrollChangedCallback getOnScrollChangedCallback() {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(
            final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public interface OnScrollChangedCallback {
        void onScroll(int dx, int dy);
    }


    public void stateCheck(String text) {

        String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
        List<Type> types = new ArrayList<>();
        for (Type type : Type.values()) {
            if (TextUtils.indexOf(state, type.name()) != -1) {
                types.add(type);
            }
        }

        if (mDecorationStateListener != null) {
            mDecorationStateListener.onStateChangeListener(state, types);
        }
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        final int[] attrsArray = new int[]{
                android.R.attr.gravity
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

        int gravity = ta.getInt(0, NO_ID);
        switch (gravity) {
            case Gravity.LEFT:
                exec("javascript:RE.setTextAlign(\"left\")");
                break;
            case Gravity.RIGHT:
                exec("javascript:RE.setTextAlign(\"right\")");
                break;
            case Gravity.TOP:
                exec("javascript:RE.setVerticalAlign(\"top\")");
                break;
            case Gravity.BOTTOM:
                exec("javascript:RE.setVerticalAlign(\"bottom\")");
                break;
            case Gravity.CENTER_VERTICAL:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                break;
            case Gravity.CENTER_HORIZONTAL:
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
            case Gravity.CENTER:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
        }

        ta.recycle();
    }

    /**
     * setText
     *
     * @param contents
     */
//    public void setHtml(String contents) {
//        if (contents == null) {
//            contents = "";
//        }
//        try {
//            exec("javascript:RE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
//        } catch (UnsupportedEncodingException e) {
//            // No handling
//        }
//        mContents = contents;
//    }

    /**
     * getText
     *
     * @return
     */
    public void getHtmlAsyn() {
        exec("javascript:RE.getHtml4Android()");
    }

    public String getHtml(){
        return mContents;
    }

//    public void setEditorFontColor(int color) {
//        String hex = convertHexColorString(color);
//        exec("javascript:RE.setBaseTextColor('" + hex + "');");
//    }
//
//    public void setEditorFontSize(int px) {
//        exec("javascript:RE.setBaseFontSize('" + px + "px');");
//    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
                + "px');");
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        // still not support RTL.
        setPadding(start, top, end, bottom);
    }

    public void setEditorBackgroundColor(int color) {
        setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
    }
//
//    @Override
//    public void setBackgroundResource(int resid) {
//        Bitmap bitmap = Utils.decodeResource(getContext(), resid);
//        String base64 = Utils.toBase64(bitmap);
//        bitmap.recycle();
//
//        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
//    }

//    @Override
//    public void setBackground(Drawable background) {
//        Bitmap bitmap = Utils.toBitmap(background);
//        String base64 = Utils.toBase64(bitmap);
//        bitmap.recycle();
//
//        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
//    }
//
//    public void setBackground(String url) {
//        exec("javascript:RE.setBackgroundImage('url(" + url + ")');");
//    }

//    public void setEditorWidth(int px) {
//        exec("javascript:RE.setWidth('" + px + "px');");
//    }
//
//    public void setEditorHeight(int px) {
//        exec("javascript:RE.setHeight('" + px + "px');");
//    }

    public void setPlaceholder(String placeholder) {
        exec("javascript:RE.setPlaceholder('" + placeholder + "');");
    }

    public void loadCSS(String cssFile) {
        String jsCSSImport = "(function() {" +
                "    var head  = document.getElementsByTagName(\"head\")[0];" +
                "    var link  = document.createElement(\"link\");" +
                "    link.rel  = \"stylesheet\";" +
                "    link.type = \"text/css\";" +
                "    link.href = \"" + cssFile + "\";" +
                "    link.media = \"all\";" +
                "    head.appendChild(link);" +
                "}) ();";
        exec("javascript:" + jsCSSImport + "");
    }

    public void undo() {
        exec("javascript:RE.exec('undo');");
    }

    public void redo() {
        exec("javascript:RE.exec('redo');");
    }

    public void setBold() {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.exec('bold');");
    }

    public void setItalic() {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.exec('italic');");
    }

//    public void setSubscript() {
//        exec("javascript:RE.setSubscript();");
//    }
//
//    public void setSuperscript() {
//        exec("javascript:RE.setSuperscript();");
//    }

    public void setStrikeThrough() {
        exec("javascript:RE.saveRange()");
        exec("javascript:RE.exec('strikethrough');");
    }

//    public void setUnderline() {
//        exec("javascript:RE.setUnderline();");
//    }

//    public void setTextColor(int color) {
//        exec("javascript:RE.prepareInsert();");
//
//        String hex = convertHexColorString(color);
//        exec("javascript:RE.setTextColor('" + hex + "');");
//    }

//    public void setTextBackgroundColor(int color) {
//        exec("javascript:RE.prepareInsert();");
//
//        String hex = convertHexColorString(color);
//        exec("javascript:RE.setTextBackgroundColor('" + hex + "');");
//    }

//    public void setFontSize(int fontSize) {
//        if (fontSize > 7 || fontSize < 1) {
//            Log.e("RichEditor", "Font size should have a value between 1-7");
//        }
//        exec("javascript:RE.setFontSize('" + fontSize + "');");
//    }

//    public void removeFormat() {
//        exec("javascript:RE.removeFormat();");
//    }

    public void setHeading(int heading, boolean b) {
        exec("javascript:RE.saveRange();");
        if (b){
            exec("javascript:RE.exec('h"+heading+"')");
        }else {
            exec("javascript:RE.exec('p')");
        }
    }

//    public void setIndent() {
//        exec("javascript:RE.setIndent();");
//    }
//
//    public void setOutdent() {
//        exec("javascript:RE.setOutdent();");
//    }
//
//    public void setAlignLeft() {
//        exec("javascript:RE.setJustifyLeft();");
//    }
//
//    public void setAlignCenter() {
//        exec("javascript:RE.setJustifyCenter();");
//    }
//
//    public void setAlignRight() {
//        exec("javascript:RE.setJustifyRight();");
//    }

    public void setBlockquote(boolean b) {
        exec("javascript:RE.saveRange();");
        if(b){
            exec("javascript:RE.exec('blockquote')");
        }else {
            exec("javascript:RE.exec('p')");
        }
    }


//    public void setBullets() {
//        exec("javascript:RE.setBullets();");
//    }
//
//    public void setNumbers() {
//        exec("javascript:RE.setNumbers();");
//    }

    public void insertImage(String url, long width ,long height, long id) {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.insertImage('" + url + "', " + width + ","+ height + ","+id+");");
    }

    public void deleteImageByUri(Long id){
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.removeImage("+id+");");
    }

    public void insertHr() {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.insertLine();");
    }


    public void insertLink(String href, String title) {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.insertLink('" + title + "', '" + href + "');");
    }

    public void changeLink(String href, String title) {
        exec("javascript:RE.saveRange();");
        exec("javascript:RE.changeLink('" + title + "', '" + href + "');");
    }

    public void insertTodo() {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.setTodo('" + Utils.getCurrentTime() + "');");
    }

    public void setImageUploadProcess(String url,int process){
        exec("javascript:RE.changeProcess('"+ url +"', "+ process +");");
    }

    public void focusEditor() {
        requestFocus();
        exec("javascript:RE.focus();");
    }

    public void clearFocusEditor() {
        exec("javascript:RE.blurFocus();");
    }

    private String convertHexColorString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    protected void exec(final String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    exec(trigger);
                }
            }, 100);
        }
    }

    private void load(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    protected class EditorWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            isReady = url.equalsIgnoreCase(SETUP_HTML);
            if (mLoadListener != null) {
                mLoadListener.onAfterInitialLoad(isReady);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            String decode;
            try {
                decode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // No handling
                return false;
            }

            Log.e("decode",decode);

            if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
                callback(decode);
                return true;
            } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0) {
                stateCheck(decode);
                return true;
            }

            if(TextUtils.indexOf(url,LINK_CHANGE_SCHEME) == 0){
                linkChangeCallBack(decode);
                return true;
            }

            if(TextUtils.indexOf(url,IMAGE_CLICK_SCHEME) == 0){
                imageClickCallBack(decode);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

    }


    public class Android4JsInterface {
        @JavascriptInterface
        public void setViewEnabled(boolean enabled){
            Log.e("setViewEnabled","" + enabled);
            if(mOnFocusChangeListener != null)
                mOnFocusChangeListener.onFocusChange(enabled);
        }
        @JavascriptInterface
        public void setHtmlContent(String htmlContent){
            mContents = htmlContent;
            if(mTextChangeListener != null)
                mTextChangeListener.onTextChange(htmlContent);
        }
    }
}