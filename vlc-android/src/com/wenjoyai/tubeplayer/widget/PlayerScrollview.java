package com.wenjoyai.tubeplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * @author：LiJiaZhi on 2017/12/1
 * @des：ToDo
 * @org mtime.com
 */
public class PlayerScrollview extends HorizontalScrollView {
    private Scroller mScroller;
    private int mTouchSlop;
    private boolean isScrolledToTop = true;// 初始化的时候设置一下值
    private boolean isScrolledToBottom = false;
    public PlayerScrollview(Context context) {
        super(context);
        init();
    }

    public PlayerScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init(){
        mScroller = new Scroller(this.getContext());
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//
//                break;
//            case MotionEvent.ACTION_MOVE:
//                int scrollX=getScrollX();
//                int width=getWidth();
//                int scrollViewMeasuredWidth=getChildAt(0).getMeasuredWidth();
//                if(scrollX==0){
//                    System.out.println("滑动到了顶端 view.getScrollY()="+scrollX);
//                }
//                if((scrollX+width)==scrollViewMeasuredWidth){
//                    System.out.println("滑动到了底部 scrollY="+scrollX);
//                    System.out.println("滑动到了底部 height="+width);
//                    System.out.println("滑动到了底部 scrollViewMeasuredHeight="+scrollViewMeasuredWidth);
//                }
//                break;
//
//            default:
//                break;
//        }
//        return super.onTouchEvent(ev);
//    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (l-oldl>mTouchSlop){//右滑
            right();
        }
        if (l-oldl<-mTouchSlop){//左滑
            left();
        }
        if (getScrollX() == 0) {    // 小心踩坑1: 这里不能是getScrollY() <= 0
            isScrolledToTop = true;
            isScrolledToBottom = false;
        } else if (getScrollX() + getWidth() - getPaddingLeft()-getPaddingRight() == getChildAt(0).getWidth()) {
            isScrolledToBottom = true;
            isScrolledToTop = false;

        } else {
            isScrolledToTop = false;
            isScrolledToBottom = false;
        }
        notifyScrollChangedListeners();
    }
    private void notifyScrollChangedListeners() {
        LinearLayout layout = (LinearLayout)getChildAt(0);
        if (isScrolledToBottom){
            layout.getChildAt(layout.getChildCount()-2).setVisibility(VISIBLE);
        }else {
            layout.getChildAt(layout.getChildCount()-2).setVisibility(INVISIBLE);
        }


        if (isScrolledToTop) {
            Log.e("tag", "isScrolledToTop");
            if (mSmartScrollChangedListener != null) {
                mSmartScrollChangedListener.onScrolledToTop();

            }
        } else if (isScrolledToBottom) {
            Log.e("tag", "isScrolledToBottom");
            if (mSmartScrollChangedListener != null) {
                mSmartScrollChangedListener.onScrolledToBottom();
            }
        }
    }

    /**
     * 向右快速滑动
     */
    public void right(){
        smooth(getScrollX(), 1200);
    }
    /**
     * 向左快速滑动
     */
    public void left(){
        smooth(getScrollX(), 0);
    }

    /**
     * 匀速滑动
     * @param srcX
     * @param desX
     */
    public void smooth(int srcX, int desX) {
        mScroller.startScroll(srcX, 0, desX - srcX, 0, Math.abs(desX - srcX) * 1);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    private ISmartScrollChangedListener mSmartScrollChangedListener;

    /** 定义监听接口 */
    public interface ISmartScrollChangedListener {
        void onScrolledToBottom();
        void onScrolledToTop();
    }

    public void setScanScrollChangedListener(ISmartScrollChangedListener smartScrollChangedListener) {
        mSmartScrollChangedListener = smartScrollChangedListener;
    }
}
