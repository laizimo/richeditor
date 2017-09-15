package com.lu.lubottommenu;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;


import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.logiclist.MenuItem;
import com.lu.lubottommenu.logiclist.MenuItemFactory;
import com.lu.lubottommenu.logiclist.MenuItemTree;
import com.lu.lubottommenu.menuitem.BottomMenuItem;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by 陆正威 on 2017/9/6.
 */
@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
public class LuBottomMenu extends ViewGroup {

    private int MAX_NUM_ONE_ROW = 6;
    private int MAX_LEVELS = 3;
    private static int INNER_LAYOUT_PADDING_L = 0;
    private static int INNER_LAYOUT_PADDING_R = 0;
    private static int INNER_LAYOUT_PADDING_T = 0;
    private static int INNER_LAYOUT_PADDING_B = 0;

    private static int[] COLOR_SET = {Color.WHITE, Color.argb(255, 252, 252, 252)};

    private boolean colorSelector = true;

    private MenuItemTree mMenuTree;
    private HashMap<Long, BottomMenuItem> mBottomMenuItems;
    private ArrayDeque<MenuItem> mPathRecord;//菜单展开路径栈,通过存储这个信息配合逻辑树恢复其他信息，时间换空间
    private ArrayList<LinearLayout> mDisplayMenus;

    private MenuItem mCurMenuItem;//当前聚焦项目集合的最顶层父请节点
    private int mDisplayRowNum = 0;//显示的菜单行数
    private int mSingleRowHeight = 0;//每级菜单的高度

    private boolean isFirstMeasure = true;
    private boolean isFirstLayout = true;

    private int mWidth;
    private int mHeight;

    private IBottomMenuItem.OnIteClickListener mOnIteClickListener;

    private IBottomMenuItem.OnIteClickListener mInnerListener;

    public LuBottomMenu(Context context) {
        this(context, null);
    }

    public LuBottomMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuBottomMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPathRecord.isEmpty())
            addOneLevel();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (isFirstMeasure) {
            removeUnUselessViews();
            mSingleRowHeight = Math.max(heightSize - getPaddingBottom() - getPaddingTop(), 0);
            isFirstMeasure = false;
        }

        mWidth = Math.max(widthSize, 0);

        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                MAX_LEVELS = 5;
                break;
            case MeasureSpec.AT_MOST:
                Log.e("mode", "AT_MOST");
                break;
            case MeasureSpec.EXACTLY:
                Log.e("mode", "EXACTLY");
                break;
        }

        if (mDisplayRowNum < 1)
            throw new RuntimeException("nothing can been displayed ,please init at least one level of the menu-tree");

        mHeight = mDisplayRowNum * mSingleRowHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(mWidth, mHeight);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        final int topBase = getPaddingTop();
        final int leftBase = getPaddingLeft();
        final int width = r - l;
        final int height = b - t;

        int offset;
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                offset = mDisplayRowNum - 1 - i;
                child.layout(leftBase + child.getPaddingLeft(), topBase + offset * mSingleRowHeight + child.getPaddingTop(),
                        width - getPaddingRight() - child.getPaddingRight(), topBase + (offset + 1) * mSingleRowHeight - child.getPaddingBottom());
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state != null) {
            SaveState ss = (SaveState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            mMenuTree = ss.menuItemTree;//恢复逻辑树
            mBottomMenuItems = ss.bottomMenuItems;
            //mPathRecord = ss.pathRecord;
            restoreAllInfo(ss.pathRecord);//根据路径信息恢复其他信息
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SaveState ss = new SaveState(superState);
        ss.pathRecord = mPathRecord;
        ss.menuItemTree = mMenuTree;
        ss.bottomMenuItems = mBottomMenuItems;
        return ss;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Set<Map.Entry<Long, BottomMenuItem>> entrySet = mBottomMenuItems.entrySet();
        for (Map.Entry<Long, BottomMenuItem> e :
                entrySet) {
            e.getValue().onViewDestroy();
        }
    }

    //After init()
    private void removeUnUselessViews() {
        final int childCount = getChildCount();
        if (childCount > mDisplayMenus.size()) {
            for (int i = 0; i < childCount; i++) {
                View v = getChildAt(i);
                if (!mDisplayMenus.contains(v))
                    removeView(v);
            }
        }
    }

    private void init() {
        mMenuTree = new MenuItemTree();
        mDisplayMenus = new ArrayList<>();
        mPathRecord = new ArrayDeque<>();
        mBottomMenuItems = new HashMap<>();

        mInnerListener = new IBottomMenuItem.OnIteClickListener() {
            @Override
            public void onItemClick(MenuItem menuItem) {
                onItemClickPreHandle(menuItem);
                if (mOnIteClickListener != null)
                    mOnIteClickListener.onItemClick(menuItem);
            }
        };

        if (mCurMenuItem == null || mCurMenuItem.isLeafNode())
            mDisplayRowNum = 0;

        mCurMenuItem = mMenuTree.getRootItem();

        if (isInEditMode()) {
            addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x01, R.drawable.cloud));
            addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x02, R.drawable.cloud));
            addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x03, R.drawable.cloud));
            addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x04, R.drawable.cloud));

            addItem(0x01, MenuItemFactory.generateImageItem(getContext(), 0x05, R.drawable.cloud));
            addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x06, R.drawable.cloud));
            addItem(0x05, MenuItemFactory.generateImageItem(getContext(), 0x07, R.drawable.cloud));
            addOneLevel();
            mOnIteClickListener = new IBottomMenuItem.OnIteClickListener() {
                @Override
                public void onItemClick(MenuItem menuItem) {
                    //Log.e("click",menuItem.getId().toString()+ "," + mDisplayRowNum);
                }
            };
        }
    }

    private void onItemClickPreHandle(MenuItem menuItem) {
        if (!menuItem.equals(mCurMenuItem)) {
            if (!menuItem.isLeafNode()) {
                boolean isOldItemChild = menuItem.getDeep() > mCurMenuItem.getDeep();
                boolean isOldItemElder = (!isOldItemChild && menuItem.isElderOf(mCurMenuItem));//直系父亲节点
                mCurMenuItem = menuItem;

                if (isOldItemChild) {
                    addOneLevel();
                } else {
                    removeAllLevels(Math.max(0, mDisplayRowNum - mCurMenuItem.getDeep()));
                    if (!isOldItemElder)
                        addOneLevel();
                    else
                        mCurMenuItem = mCurMenuItem.getParent();
                }
            } else {
                BottomMenuItem newItem = getBottomMenuItem(menuItem);
                newItem.setSelected(!newItem.isSelected());
            }
        } else {
            //Log.e("onItemClick", "re-tap " + menuItem.getId().toString());
            if (!menuItem.isLeafNode()) {
                removeAllLevels(mDisplayRowNum - mCurMenuItem.getDeep());
                mCurMenuItem = mCurMenuItem.getParent();
            } else {
                BottomMenuItem newItem = getBottomMenuItem(menuItem);
                newItem.setSelected(!newItem.isSelected());
            }
        }
    }

    private void addOneLevel() {
        if (mCurMenuItem == null || mCurMenuItem.isLeafNode())
            return;
        LinearLayout linearLayout = new LinearLayout(getContext());

        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setBackgroundColor(getColorByDeep(mCurMenuItem.getDeep()));
        linearLayout.setPadding(INNER_LAYOUT_PADDING_L, INNER_LAYOUT_PADDING_T, INNER_LAYOUT_PADDING_R, INNER_LAYOUT_PADDING_B);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mDisplayMenus.add(linearLayout);
        fillMenu(linearLayout, mCurMenuItem);
        addView(linearLayout);
        mPathRecord.push(mCurMenuItem);
        if (mCurMenuItem != null && !mCurMenuItem.equals(mMenuTree.getRootItem()))
            getBottomMenuItem(mCurMenuItem).setSelected(true);
        mDisplayRowNum++;
    }

    private void removeTopLevel() {
        removeAllLevels(1);
    }

    private void removeAllLevels(int num) {
        if (num >= mDisplayRowNum || num < 1)
            return;

        int b = mDisplayRowNum - num;
        for (int i = mDisplayRowNum - 1; i >= b; i--) {
            if (mDisplayMenus.get(i).getChildAt(0) instanceof HorizontalScrollView) {
                View v = ((HorizontalScrollView) mDisplayMenus.get(i).getChildAt(0)).getChildAt(0);
                if (v != null && v instanceof LinearLayout)
                    ((LinearLayout) v).removeAllViews();
            }
            mDisplayMenus.get(i).removeAllViews();
            mDisplayMenus.remove(i);
            removeView(getChildAt(i));
            getBottomMenuItem(mPathRecord.peek()).setSelected(false);
            mPathRecord.pop();
            mDisplayRowNum--;
        }
    }

    private void fillTopMenu(MenuItem parent) {
        if (mDisplayRowNum >= 1)
            fillMenu(mDisplayMenus.get(mDisplayRowNum - 1), parent);
    }

    private void fillMenu(LinearLayout menu, MenuItem parent) {

        if (parent.getNextLevel() == null) return;
        final int count = parent.getNextLevel().size();
        if (count > MAX_NUM_ONE_ROW) {
            HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
            scrollView.setSmoothScrollingEnabled(true);
            scrollView.setHorizontalScrollBarEnabled(false);
            menu.addView(scrollView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            LinearLayout linearLayout = new LinearLayout(getContext());
            for (MenuItem item :
                    parent.getNextLevel()) {
                BottomMenuItem bottomMenuItem = getBottomMenuItem(item);
                bottomMenuItem.setOnIteClickListener(mInnerListener);
                //当恢复时Context对象无法序列化，这里临时传入
                if (bottomMenuItem.getContext() == null)
                    bottomMenuItem.setContext(getContext());
                bottomMenuItem.onDisplayPrepare();

                View v = bottomMenuItem.getMainView();

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                v.setLayoutParams(lp);
                linearLayout.addView(v);
            }
            scrollView.addView(linearLayout, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        } else {
            for (MenuItem item :
                    parent.getNextLevel()) {
                BottomMenuItem bottomMenuItem = getBottomMenuItem(item);
                bottomMenuItem.setOnIteClickListener(mInnerListener);
                //当恢复时Context对象无法序列化，这里临时传入
                if (bottomMenuItem.getContext() == null)
                    bottomMenuItem.setContext(getContext());
                bottomMenuItem.onDisplayPrepare();

                View v = bottomMenuItem.getMainView();

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
                lp.weight = 1;
                v.setLayoutParams(lp);
                menu.addView(v);
            }
        }
    }


    private int getAllChildrenWidthSum(boolean withPadding, boolean withMargin) {
        final int childCount = getChildCount();
        int width = 0;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            int margin = ((MarginLayoutParams) child.getLayoutParams()).leftMargin +
                    ((MarginLayoutParams) child.getLayoutParams()).rightMargin;

            width += child.getMeasuredWidth() +
                    (withPadding ? child.getPaddingLeft() + child.getPaddingRight() : 0) +
                    (withMargin ? margin : 0);
        }

        return Math.max(width, 0);
    }

    private int getAllChildrenHeightSum(boolean withPadding, boolean withMargin) {
        final int childCount = getChildCount();
        int height = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;

            int margin = ((MarginLayoutParams) child.getLayoutParams()).topMargin +
                    ((MarginLayoutParams) child.getLayoutParams()).bottomMargin;

            height += child.getMeasuredHeight() +
                    (withPadding ? child.getPaddingTop() + child.getPaddingBottom() : 0) +
                    (withMargin ? margin : 0);
        }
        return Math.max(height, 0);
    }

    private BottomMenuItem getBottomMenuItem(MenuItem item) {
        return mBottomMenuItems.get(item.getId());
    }

    //通过路径直接恢复视图序列
    private void restoreAllInfo(ArrayDeque<MenuItem> pathRecord) {
        while (!pathRecord.isEmpty()) {
            mCurMenuItem = pathRecord.getLast();
            addOneLevel();
            pathRecord.removeLast();
        }
    }

    private int getColorByDeep(int deep) {
        return COLOR_SET[deep % COLOR_SET.length];
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    public static class SaveState extends BaseSavedState implements Parcelable {
        ArrayDeque<MenuItem> pathRecord;
        MenuItemTree menuItemTree;
        HashMap<Long, BottomMenuItem> bottomMenuItems;

        public SaveState(Parcelable superState) {
            super(superState);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.pathRecord);
            dest.writeParcelable(this.menuItemTree, flags);
            dest.writeMap(this.bottomMenuItems);
        }

        protected SaveState(Parcel in) {
            super(in);
            this.pathRecord = (ArrayDeque<MenuItem>) in.readSerializable();
            this.menuItemTree = in.readParcelable(MenuItemTree.class.getClassLoader());
            this.bottomMenuItems = in.readHashMap(HashMap.class.getClassLoader());
        }

        public static final Creator<SaveState> CREATOR = new Creator<SaveState>() {
            @Override
            public SaveState createFromParcel(Parcel source) {
                return new SaveState(source);
            }

            @Override
            public SaveState[] newArray(int size) {
                return new SaveState[size];
            }
        };
    }

    //=========================================== 外部调用 =========================================================================================

    public void setOnItemClickListener(IBottomMenuItem.OnIteClickListener mOnIteClickListener) {
        this.mOnIteClickListener = mOnIteClickListener;
    }

    public void setItemSelected(long id, boolean isSelected) {
        BottomMenuItem bottomMenuItem = getBottomMenuItem(mMenuTree.getRootItem().getMenuItemById(id));
        if (bottomMenuItem != null)
            bottomMenuItem.setSelected(isSelected);
    }

    public void setEnabled(boolean enabled){
        Set<Map.Entry<Long, BottomMenuItem>> entrySet = mBottomMenuItems.entrySet();
        for (Map.Entry<Long, BottomMenuItem> e :
                entrySet) {
            Log.e("setEnabled",enabled+""+e.getValue().getItemId());
            if(e.getValue().getMainView()!=null)
                e.getValue().getMainView().setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void show(long d){
        AnimatorUtil.sbow(this,d);
    }

    public void hide(long d){
        AnimatorUtil.hide(this,d);
    }

    public int isItemSelected2(MenuItem item) {
        BottomMenuItem bottomMenuItem = getBottomMenuItem(item);
        if (bottomMenuItem != null)
            return bottomMenuItem.isSelected() ? 1 : 0;
        else
            return -1;
    }

    public int isItemSelected2(long id) {
        BottomMenuItem bottomMenuItem = getBottomMenuItem(mMenuTree.getRootItem().getMenuItemById(id));
        if (bottomMenuItem != null)
            return bottomMenuItem.isSelected() ? 1:0;
        else
            return -1;
    }

    public boolean isItemSelected(MenuItem item) {
        BottomMenuItem bottomMenuItem = getBottomMenuItem(item);
        if (bottomMenuItem != null)
            return bottomMenuItem.isSelected();
        else
            throw new RuntimeException("no item match");
    }

    public boolean isItemSelected(long id) {
        BottomMenuItem bottomMenuItem = getBottomMenuItem(mMenuTree.getRootItem().getMenuItemById(id));
        if (bottomMenuItem != null)
            return bottomMenuItem.isSelected();
        else
            throw new RuntimeException("no item match");
    }

    public void setScrollNum(int num) {
        MAX_NUM_ONE_ROW = num;
    }

    public void setMenuBackGroundColor(int... colors) {
        COLOR_SET = colors;

        if (!isFirstMeasure) {
            int i = 0;
            for (LinearLayout l :
                    mDisplayMenus) {
                l.setBackgroundColor(getColorByDeep(i));
                i++;
            }
            invalidate();
        }
    }

    public LuBottomMenu addRootItem(BottomMenuItem bottomMenuItem) {
        MenuItem menuItem = bottomMenuItem.getMenuItem();
        mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);

        mMenuTree.addRootItem(menuItem);
        return this;
    }

    public LuBottomMenu addItem(MenuItem parentItem, BottomMenuItem bottomMenuItem) {
        MenuItem menuItem = bottomMenuItem.getMenuItem();
        if (parentItem == mMenuTree.getRootItem())
            addRootItem(bottomMenuItem);
        else {
            mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);
            mMenuTree.addByParent(parentItem, menuItem);
        }
        return this;
    }

    public LuBottomMenu addItem(long parentId, BottomMenuItem bottomMenuItem) {
        MenuItem menuItem = bottomMenuItem.getMenuItem();

        mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);
        mMenuTree.addByParentId(parentId, menuItem);
        return this;
    }

    public LuBottomMenu addItems(long parentId, BottomMenuItem... menuItems) {
        for (BottomMenuItem bottomMenuItem :
                menuItems) {
            addItem(parentId, bottomMenuItem);
        }
        return this;
    }

    public LuBottomMenu addItems(MenuItem parentItem, BottomMenuItem... menuItems) {
        MenuItem menuItem;
        for (BottomMenuItem bottomMenuItem :
                menuItems) {
            menuItem = bottomMenuItem.getMenuItem();

            mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);
            mMenuTree.addByParent(parentItem, menuItem);
        }
        return this;
    }

    public static class Builder {
        private LuBottomMenu luBottomMenu;

        public Builder(Context context) {
            luBottomMenu = new LuBottomMenu(context);
            if (luBottomMenu.mMenuTree == null)
                luBottomMenu.mMenuTree = new MenuItemTree();
            luBottomMenu.mCurMenuItem = luBottomMenu.mMenuTree.getRootItem();
            if (luBottomMenu.mCurMenuItem == null || luBottomMenu.mCurMenuItem.getNextLevel().isEmpty())
                luBottomMenu.mDisplayRowNum = 0;
        }

        public Builder addRootItem(MenuItem menuItem) {
            luBottomMenu.mMenuTree.addRootItem(menuItem);
            if (luBottomMenu.mDisplayRowNum < 1)
                luBottomMenu.mDisplayRowNum = 1;
            return this;
        }

        public Builder addItem(MenuItem parentItem, BottomMenuItem bottomMenuItem) {
            MenuItem menuItem = bottomMenuItem.getMenuItem();
            luBottomMenu.mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);
            if (parentItem == luBottomMenu.mMenuTree.getRootItem())
                addRootItem(menuItem);
            else
                luBottomMenu.mMenuTree.addByParent(parentItem, menuItem);
            return this;
        }

        public Builder addItem(long parentId, BottomMenuItem bottomMenuItem) {
            MenuItem menuItem = bottomMenuItem.getMenuItem();
            bottomMenuItem.setOnIteClickListener(new IBottomMenuItem.OnIteClickListener() {
                @Override
                public void onItemClick(MenuItem menuItem) {
                    luBottomMenu.mCurMenuItem = menuItem;
                    if (luBottomMenu.mOnIteClickListener != null)
                        luBottomMenu.mOnIteClickListener.onItemClick(menuItem);
                }
            });

            luBottomMenu.mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);
            luBottomMenu.mMenuTree.addByParentId(parentId, menuItem);
            return this;
        }

        public Builder addItems(long parentId, BottomMenuItem... menuItems) {
            MenuItem menuItem;
            for (BottomMenuItem bottomMenuItem :
                    menuItems) {
                menuItem = bottomMenuItem.getMenuItem();
                bottomMenuItem.setOnIteClickListener(new IBottomMenuItem.OnIteClickListener() {
                    @Override
                    public void onItemClick(MenuItem menuItem) {
                        luBottomMenu.mCurMenuItem = menuItem;
                        if (luBottomMenu.mOnIteClickListener != null)
                            luBottomMenu.mOnIteClickListener.onItemClick(menuItem);
                    }
                });

                luBottomMenu.mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);
                luBottomMenu.mMenuTree.addByParentId(parentId, menuItem);
            }
            return this;
        }

        public Builder addItems(MenuItem parentItem, BottomMenuItem... menuItems) {
            MenuItem menuItem;
            for (BottomMenuItem bottomMenuItem :
                    menuItems) {
                menuItem = bottomMenuItem.getMenuItem();
                bottomMenuItem.setOnIteClickListener(new IBottomMenuItem.OnIteClickListener() {
                    @Override
                    public void onItemClick(MenuItem menuItem) {
                        luBottomMenu.mCurMenuItem = menuItem;
                        if (luBottomMenu.mOnIteClickListener != null)
                            luBottomMenu.mOnIteClickListener.onItemClick(menuItem);
                    }
                });

                luBottomMenu.mBottomMenuItems.put(menuItem.getId(), bottomMenuItem);
                luBottomMenu.mMenuTree.addByParent(parentItem, menuItem);
            }
            return this;
        }

        public LuBottomMenu build() {
            return luBottomMenu;
        }
    }
}
