package com.star.view.appbarlayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.NestedScrollView;
import android.view.View;
import android.widget.OverScroller;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ScrollItem {
    private int type; //1: NestedScrollView   2:RecyclerView
    private WeakReference<NestedScrollView> scrollViewRef;
    private WeakReference<LinearLayoutManager> layoutManagerRef;

    public ScrollItem(View v) {
        findScrollItem(v);
    }

    /**
     * 查找需要滑动的scroll对象
     *
     * @param v
     */
    protected boolean findScrollItem(View v) {
        if (findCommonScroll(v)) return true;
        if (v instanceof ViewPager) {
            View root = ViewPagerUtil.findCurrent((ViewPager) v);
            if (root != null) {
                View child = root.findViewWithTag("fling");
                return findCommonScroll(child);
            }
        }
        return false;
    }

    private boolean findCommonScroll(View v) {
        if (v instanceof NestedScrollView) {
            type = 1;
            scrollViewRef = new WeakReference<NestedScrollView>((NestedScrollView) v);
            stopScroll(scrollViewRef.get());
            return true;
        }
        if (v instanceof RecyclerView) {
            RecyclerView.LayoutManager lm = ((RecyclerView) v).getLayoutManager();
            if (lm instanceof LinearLayoutManager) {
                LinearLayoutManager llm = (LinearLayoutManager) lm;
                type = 2;
                layoutManagerRef = new WeakReference<LinearLayoutManager>(llm);
                stopScroll((RecyclerView) v);
                return true;
            }
        }
        return false;
    }

    /**
     * 停止NestedScrollView滚动
     *
     * @param v
     */
    private void stopScroll(NestedScrollView v) {
        try {
            Field field = ReflectUtil.getDeclaredField(v, "mScroller");
            if (field == null) return;
            field.setAccessible(true);
            OverScroller scroller = (OverScroller) field.get(v);
            if (scroller != null) scroller.abortAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止RecyclerView滚动
     *
     * @param
     */
    private void stopScroll(RecyclerView rv) {
        try {
            Field field = ReflectUtil.getDeclaredField(rv, "mViewFlinger");
            if (field == null) return;
            field.setAccessible(true);
            Object obj = field.get(rv);
            if (obj == null) return;
            Method method = obj.getClass().getDeclaredMethod("stop");
            method.setAccessible(true);
            method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scroll(int dy) {
        if (type == 1) {
            scrollViewRef.get().scrollTo(0, dy);
        } else if (type == 2) {
            layoutManagerRef.get().scrollToPositionWithOffset(0, -dy);
        }
    }

}
