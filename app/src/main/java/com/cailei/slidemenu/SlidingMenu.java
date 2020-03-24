package com.cailei.slidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import androidx.core.view.ViewCompat;

/**
 * @author : cailei
 * @date : 2020-03-23 18:02
 * @description :
 */
public class SlidingMenu extends HorizontalScrollView {

    private int menuWidth;
    private int screenWidth;

    private ViewGroup mCotentView;
    private ViewGroup mMenuView;

    GestureDetector gestureDetector;

    private boolean isMenuOpen;
    private boolean mIsIntecept = false;

    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);
        int menuRight = array.getInteger(R.styleable.SlidingMenu_SlidingMenu_rightMargin, ScreenUtils.dip2px(context, 50));
        menuWidth = ScreenUtils.getScreenWidth(context) - menuRight;
        array.recycle();

        gestureDetector = new GestureDetector(context, mGestureDetector);
    }

    private GestureDetector.OnGestureListener mGestureDetector = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e("TAG", "velocityX" + "->" + velocityX);
            //小于0 快速往左滑动 大于0 快速往右滑动
            if (isMenuOpen) {
                //如果侧边的菜单栏打开，快速往左滑动就关闭
                if (velocityX < 0) {
                    closeMenu();
                    return true;
                }
            } else {
                if (velocityX > 0) {
                    openMenu();
                    return true;
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };

    //宽度不对，需要知道宽高

    @Override
    protected void onFinishInflate() {
        //布局加载完毕会调用这个方法



        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ViewGroup container = (ViewGroup) getChildAt(0);
        mMenuView = (ViewGroup) container.getChildAt(0);
        mMenuView.getLayoutParams().width = menuWidth;

        mCotentView = (ViewGroup) container.getChildAt(1);
        mCotentView.getLayoutParams().width = ScreenUtils.getScreenWidth(this.getContext());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        //初始化进来是关闭状态
        closeMenu();
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mIsIntecept){
            return true;
        }
        if (gestureDetector.onTouchEvent(ev)) {
            return true;
        }

        //快速滑动触发了就不要执行

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            //手指抬起 根据当前滚动的距离`来判断
            //特别注意getScrollX 是相对与最开始的0坐标点的，不是相对于自己的手指的
            int currentScrollX = getScrollX();
            if (currentScrollX > menuWidth / 2) {
                //超过菜单的一半 关闭
                closeMenu();
            } else {
                openMenu();
            }
            //确保super.onTouchEvent 不会执行
            return true;
        }
        return super.onTouchEvent(ev);
    }


    //处理各种缩放


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.e("TAG", l + "");
        //算一个梯度值
        float scale = 1f * l / menuWidth; //从1-0
        //右边缩放最小时0.7 最大是1
        float rightScale = 0.7f + 0.3f * scale;
        float leftScale = 1.0f - 0.3f * scale;
        //设置缩放中心点，否则就会缩到看不见
        ViewCompat.setPivotX(mCotentView, 0);
        ViewCompat.setPivotY(mCotentView, mCotentView.getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mCotentView, rightScale);
        ViewCompat.setScaleY(mCotentView, rightScale);

        //菜单的缩放和透明度 回拉的时候慢慢变成半透明 半透明-不透明 0.5f - 1f 缩放到不缩放  0.7f-1.0f
        float alpha = 1f - 0.5f * scale;
        ViewCompat.setAlpha(mMenuView, leftScale);
//        ViewCompat.setTranslationY(mMenuView,leftScale);
        ViewCompat.setScaleX(mMenuView, leftScale);
        ViewCompat.setScaleY(mMenuView, leftScale);


        //最后一个效果 退出这个按钮刚开始是在右边，按照我们目前的方式退出的出字永远都是在左边
        //设置平移
        ViewCompat.setTranslationX(mMenuView, 0.2f * l);


    }

    private void closeMenu() {
        smoothScrollTo(menuWidth, 0);
        isMenuOpen = false;
    }

    private void openMenu() {
        smoothScrollTo(0, 0);
        isMenuOpen = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsIntecept=false;
        if (isMenuOpen) {
            if (ev.getX() > menuWidth) {
                closeMenu();
                mIsIntecept=true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
