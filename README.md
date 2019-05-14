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
