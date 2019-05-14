# AppBarLayout
支持AppBarLayout拖动缩放、解决滚动Fling传递问题

结合
1. https://github.com/iamyours/FlingAppBarLayout
2. https://github.com/ToDou/appbarlayout-spring-behavior

依赖：
```java
implementation 'com.github.lannaican:AppBarLayout:1.0.0'
```

布局：
```java
<androidx.coordinatorlayout.widget.CoordinatorLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fitsSystemWindows="true">

        <com.star.view.appbarlayout.AppBarLayout
            android:layout_width="match_parent"
            android:layout_height="338dp"
            android:fitsSystemWindows="true"
            app:layout_behavior="@string/appbar_spring_behavior">

        </com.star.view.appbarlayout.AppBarLayout>

        <androidx.viewpager.widget.ViewPager
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_behavior="@string/fling_behavior" />

    </androidx.coordinatorlayout.widget.CoordinatorLayout>
```
如果NestedScrollView或RecyclerView在ViewPager中的Fragment中，需要为其添加```android:tag="fling"```
配合SmartRefreshLayout使用需要使用：
```java
/**
 * 解决与AppBarLayout搭配时滑动冲突问题
 */
public class FlingRefreshLayout extends SmartRefreshLayout {

    public FlingRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int count = getChildCount();
        if (count > 3) {
            throw new RuntimeException("最多只支持3个子View，Most only support three sub view");
        }

        int indexContent = -1;
        int[] indexArray = {1,0,2};

        for (int index : indexArray) {
            if (index < count) {
                View view = getChildAt(index);
                if (!(view instanceof RefreshInternal)) {
                    indexContent = index;
                }
                if (view.canScrollVertically(-1) || view.canScrollVertically(1)) {
                    indexContent = index;
                }
            }
        }

        int indexHeader = -1;
        int indexFooter = -1;
        if (indexContent >= 0) {
            mRefreshContent = new CustomRefreshContentWrapper(getChildAt(indexContent));
            if (indexContent == 1) {
                indexHeader = 0;
                if (count == 3) {
                    indexFooter = 2;
                }
            } else if (count == 2) {
                indexFooter = 1;
            }
        }

        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (i == indexHeader || (i != indexFooter && indexHeader == -1 && mRefreshHeader == null && view instanceof RefreshHeader)) {
                mRefreshHeader = (view instanceof RefreshHeader)? (RefreshHeader) view : new RefreshHeaderWrapper(view);
            } else if (i == indexFooter || (indexFooter == -1 && view instanceof RefreshFooter)) {
                mEnableLoadMore = mEnableLoadMore || !mManualLoadMore;
                mRefreshFooter = (view instanceof RefreshFooter)? (RefreshFooter) view : new RefreshFooterWrapper(view);
            }
        }
    }

    static class CustomRefreshContentWrapper extends RefreshContentWrapper {

        public CustomRefreshContentWrapper(View view) {
            super(view);
        }

        @Override
        protected void findScrollableView(View content, RefreshKernel kernel) {
            mScrollableView = null;
            CoordinatorLayoutListener listener = null;
            boolean isInEditMode = mContentView.isInEditMode();
            while (mScrollableView == null || (mScrollableView instanceof NestedScrollingParent
                    && !(mScrollableView instanceof NestedScrollingChild))) {
                content = findScrollableViewInternal(content, mScrollableView == null);
                if (content == mScrollableView) {
                    break;
                }
                if (!isInEditMode) {
                    if (listener == null) {
                        listener = (enableRefresh, enableLoadMore) -> {
                            mEnableRefresh = enableRefresh;
                            mEnableLoadMore = enableLoadMore;
                        };
                    }
                    checkCoordinatorLayout(content, kernel, listener);
                }
                mScrollableView = content;
            }
        }

        public static void checkCoordinatorLayout(View content, RefreshKernel kernel, CoordinatorLayoutListener listener) {
            try {//try 不能删除，不然会出现兼容性问题
                if (content instanceof CoordinatorLayout) {
                    kernel.getRefreshLayout().setEnableNestedScroll(false);
                    wrapperCoordinatorLayout(((ViewGroup) content), kernel.getRefreshLayout(), listener);
                }
            } catch (Throwable ignored) {
            }
        }

        private static void wrapperCoordinatorLayout(ViewGroup layout, final RefreshLayout refreshLayout, final CoordinatorLayoutListener listener) {
            for (int i = layout.getChildCount() - 1; i >= 0; i--) {
                View view = layout.getChildAt(i);
                if (view instanceof AppBarLayout) {
                    ((AppBarLayout) view).addOnOffsetChangedListener((appBarLayout, verticalOffset) ->
                            listener.onCoordinatorUpdate(
                            verticalOffset >= 0,
                            refreshLayout.isEnableLoadMore() && (appBarLayout.getTotalScrollRange() + verticalOffset) <= 0));
                }
                if (view instanceof com.star.view.appbarlayout.AppBarLayout) {//自定义AppBarLayout
                    ((com.star.view.appbarlayout.AppBarLayout) view).addOnOffsetChangedListener((appBarLayout, verticalOffset) ->
                            listener.onCoordinatorUpdate(
                            verticalOffset >= 0,
                            refreshLayout.isEnableLoadMore() && (appBarLayout.getTotalScrollRange() + verticalOffset) <= 0));
                }
            }
        }
    }
}
```
