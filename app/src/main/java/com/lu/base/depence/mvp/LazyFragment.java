package com.lu.base.depence.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;


/**
 * Created by 陆正威 on 2017/4/7.
 */

public abstract class LazyFragment extends Fragment {

    public interface onFragmentVisibleListener{
        void onVisible(boolean isFirstVisiable);
    }
    public interface onFragmentInVisibleListener{
        void onInVisible();
    }

    public void setInVisibleListener(onFragmentInVisibleListener inVisibleListener) {
        this.inVisibleListener = inVisibleListener;
    }

    public void setVisibleListener(onFragmentVisibleListener visibleListener) {
        this.visibleListener = visibleListener;
    }

    private onFragmentInVisibleListener inVisibleListener;
    private onFragmentVisibleListener visibleListener;
    /**
     * rootView是否初始化标志，防止回调函数在rootView为空的时候触发
     */
    private boolean hasCreateView;
    private boolean isInViewPager = true;

    /**
     * 当前Fragment是否处于可见状态标志，防止因ViewPager的缓存机制而导致回调函数的触发
     */
    private boolean isFragmentVisible;
    private boolean isFirstVisible;

    /**
     * onCreateView()里返回的view，修饰为protected,所以子类继承该类时，在onCreateView里必须对该变量进行初始化
     */
    protected View rootView;

    public void setInViewPager(boolean flag){
        isInViewPager = flag;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //Log.d(getTAG(), "setUserVisibleHint() -> isVisibleToUser: " + isVisibleToUser);
        if (rootView == null) {
            return;
        }
        hasCreateView = true;
        if (isVisibleToUser) {
            if(!isFirstVisible) {
                onFragmentVisibleChange(true, true);
                isFirstVisible = true;
            }
            else
                onFragmentVisibleChange(true,false);
            isFragmentVisible = true;
            return;
        }
        if (isFragmentVisible) {
            onFragmentVisibleChange(false,false);
            isFragmentVisible = false;
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        if (!hasCreateView && getUserVisibleHint()) {
            if(!isFirstVisible) {
                onFragmentVisibleChange(true, true);
                isFirstVisible = true;
            }
            else
                onFragmentVisibleChange(true,false);
            isFragmentVisible = true;
        }
        super.onViewCreated(view, savedInstanceState);
    }

    protected void initVariable() {
        hasCreateView = false;
        isFragmentVisible = false;
        isFirstVisible = false;
    }


    protected boolean isFragmentVisible(){
        return !isFragmentVisible;
    }
    /**************************************************************
     *  自定义的回调方法，子类可根据需求重写
     *************************************************************/

    /**
     * 当前fragment可见状态发生变化时会回调该方法
     * 如果当前fragment是第一次加载，等待onCreateView后才会回调该方法，其它情况回调时机跟 {@link #setUserVisibleHint(boolean)}一致
     * 在该回调方法中你可以做一些加载数据操作，甚至是控件的操作，因为配合fragment的view复用机制，你不用担心在对控件操作中会报 null 异常
     *
     * @param isVisible true  不可见 -> 可见
     *                  false 可见  -> 不可见
     */
    private void onFragmentVisibleChange(boolean isVisible,boolean isFirstVisible) {
        if(isVisible){
            if(visibleListener != null)
                visibleListener.onVisible(isFirstVisible);
            onFragmentVisible(isFirstVisible);
        }else{
            if(inVisibleListener != null)
                inVisibleListener.onInVisible();
            onFragmentInVisible();
        }
        //Log.e("", "onFragmentVisibleChange -> isVisible: " + isVisible);
    }

    protected void onFragmentVisible(boolean isFirstVisible) {}
    protected void onFragmentInVisible(){}

    @Override
    public void onDetach() {
        super.onDetach();
        /*防止ViewPager的直接调用,对于ViewPage在MVP架构时，Presenter会获得View的引用,当View
        被销毁后，该引用会为null.而使用ViewPager管理fragment时，会直接通过fragmentManger间接调用fragment的
        onDetach(),onAttach().setUserVisible()函数,当使用EventBus时，传递异步消息到多个View（View中会接收和视图相关的信息）时，
        中间存在延迟,如果快速切换ViewPager,相邻的fragment（他们被预加载过但任然是不可见状态）,会直接调用onDetach()函数
        跳过onFragmentInVisible,从而导致EventBus等异步订阅取消失败,异步消息一段延时后传入回调函数，
        但此时View为null,发生错误;
        */
        if(isInViewPager)
            onFragmentInVisible();
        inVisibleListener = null;
        visibleListener = null;
    }
}
