/*
 * ************************************************************************
 *  PopupView.java
 * *************************************************************************
 *  Copyright © 2016 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *
 *  *************************************************************************
 */

package com.wenjoyai.tubeplayer.gui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.util.LogUtil;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.util.AndroidUtil;

public class PopupLayout extends RelativeLayout implements ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    private static final String TAG = "VLC/PopupLayout";

    private static final int X_MARGIN = 8;
    private static final int Y_MARGIN = 8;

    private IVLCVout mVLCVout;
    private WindowManager mWindowManager;
    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private double mScaleFactor = 1.d;
    private int mPopupWidth, mPopupHeight;
    private int mScreenWidth, mScreenHeight;

    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    WindowManager.LayoutParams mLayoutParams;

    public PopupLayout(Context context) {
        super(context);
        init(context);
    }

    public PopupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PopupLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setVLCVOut(IVLCVout vout) {
        mVLCVout = vout;
        if (mVLCVout != null && mPopupWidth > 0 && mPopupHeight > 0) {
            mVLCVout.setWindowSize(mPopupWidth, mPopupHeight);
        }
    }

    /*
     * Remove layout from window manager
     */
    public void close() {
        setKeepScreenOn(false);
        mWindowManager.removeViewImmediate(this);
        mWindowManager = null;
        mVLCVout = null;
    }

    public void setGestureDetector(GestureDetectorCompat gdc) {
        mGestureDetector = gdc;
    }

    /*
     * Update layout dimensions and apply layout params to window manager
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void setViewSize(int width, int height) {
        if (width > mScreenWidth) {
            height = height * mScreenWidth / width;
            width = mScreenWidth;
        }
        if (height > mScreenHeight){
            width = width * mScreenHeight / height;
            height = mScreenHeight;
        }
        containInScreen(width, height);
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        LogUtil.d(TAG, "PopupManager setViewSize updateViewLayout [" + mLayoutParams.x + "," + mLayoutParams.y + "," +mLayoutParams.width + ","
                + mLayoutParams.height + "]");
        mWindowManager.updateViewLayout(this, mLayoutParams);
        mPopupWidth = width;
        mPopupHeight = height;
        if (mVLCVout != null)
            mVLCVout.setWindowSize(mPopupWidth, mPopupHeight);
    }

    public int getPopupWidth() {
        return mPopupWidth;
    }

    public int getPopupHeight() {
        return mPopupHeight;
    }

    @SuppressWarnings("deprecation")
    private void init(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        mPopupWidth = VLCApplication.getAppResources().getDimensionPixelSize(R.dimen.video_pip_width);
        mPopupHeight = VLCApplication.getAppResources().getDimensionPixelSize(R.dimen.video_pip_height);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                mPopupWidth,
                mPopupHeight,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.OPAQUE);

        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.x = X_MARGIN;
        params.y = Y_MARGIN;
        params.windowAnimations = android.R.style.Animation_Translucent;
        if (AndroidUtil.isHoneycombOrLater)
            mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        mWindowManager.addView(this, params);
        mLayoutParams = (WindowManager.LayoutParams)getLayoutParams();

        updateWindowSize();
    }

    private void updateWindowSize() {
        if (AndroidUtil.isHoneycombMr2OrLater) {
            Point size = new Point();
            mWindowManager.getDefaultDisplay().getSize(size);
            mScreenWidth = size.x;
            mScreenHeight = size.y;
        } else {
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mWindowManager == null)
            return false;
        if (mScaleGestureDetector != null)
            mScaleGestureDetector.onTouchEvent(event);
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(event))
            return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = mLayoutParams.x;
                initialY = mLayoutParams.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                updateWindowSize();
                return true;
            case MotionEvent.ACTION_UP:
                return true;
//            case MotionEvent.ACTION_MOVE:
//                if (mScaleGestureDetector == null || !mScaleGestureDetector.isInProgress()) {
//                    mLayoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
//                    mLayoutParams.y = initialY - (int) (event.getRawY() - initialTouchY);
//                    containInScreen(mLayoutParams.width, mLayoutParams.height);
//                    LogUtil.d(TAG, "PopupManager ACTION_MOVE updateViewLayout [" + mLayoutParams.x + "," + mLayoutParams.y + "," +mLayoutParams.width + ","
//                            + mLayoutParams.height + "]");
//                    mWindowManager.updateViewLayout(PopupLayout.this, mLayoutParams);
//                    return true;
//                }
        }
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();

        mScaleFactor = Math.max(0.1d, Math.min(mScaleFactor, 5.0d));
        mPopupWidth = (int) (getWidth()*mScaleFactor);
        mPopupHeight = (int) (getHeight()*mScaleFactor);
        return true;
    }
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        setViewSize(mPopupWidth, mPopupHeight);
        mScaleFactor = 1.0d;
    }

    private void containInScreen(int width, int height) {
        mLayoutParams.x = Math.max(mLayoutParams.x, 0);
        mLayoutParams.y = Math.max(mLayoutParams.y, 0);
        if (mLayoutParams.x + width > mScreenWidth)
            mLayoutParams.x = mScreenWidth - width;
        if (mLayoutParams.y + height > mScreenHeight)
            mLayoutParams.y = mScreenHeight - height;
    }

    private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub
            LogUtil.d(TAG, "onAnimationStart");
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // TODO Auto-generated method stub
            LogUtil.d(TAG, "onAnimationEnd");
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub
            LogUtil.d(TAG, "onAnimationRepeat");
        }
    };

    private void animationMoveVideo(int toX, int toY, boolean visibleAfterMove) {
        LogUtil.d(TAG, "animationMoveVideo toX=" + toX + ", toY=" + toY);
        MyTranslateAnimation animation = new MyTranslateAnimation(this, toX, toY, visibleAfterMove);
        animation.setAnimationListener(mAnimationListener);
        findViewById(R.id.popup_player_layout).startAnimation(animation);
    }

    public void slideToLeft() {
        animationMoveVideo(X_MARGIN, mLayoutParams.y, true);
    }

    public void slideToRight() {
        animationMoveVideo(mScreenWidth - getWidth() - X_MARGIN, mLayoutParams.y, true);
    }

    public void slideToTop() {
        int statusBarHeight = UiTools.getStatusBarHeight(getContext());
        LogUtil.d(TAG, "slideToTop statusBarHeight=" + statusBarHeight);
        animationMoveVideo(mLayoutParams.x, mScreenHeight - statusBarHeight - getHeight() - Y_MARGIN, true);
    }

    public void slideToBottom() {
        animationMoveVideo(mLayoutParams.x, Y_MARGIN, true);
    }

    private class MyTranslateAnimation extends Animation {
        private final int interval = 200;
        private View view;
        private int fromX;
        private int fromY;
        private int toX;
        private int toY;
        private boolean visibleAfterAnimation = true;

        public MyTranslateAnimation(View view, int toX, int toY, boolean visibleAfterAnimation) {
            this.view = view;
            WindowManager.LayoutParams lp = mLayoutParams;//(WindowManager.LayoutParams)view.getLayoutParams();
            this.fromX = lp.x;
            this.fromY = lp.y;
            this.toX = toX;
            this.toY = toY;
            this.visibleAfterAnimation = visibleAfterAnimation;
            this.setInterpolator(new DecelerateInterpolator());
            this.setDuration(interval);
//            this.setFillEnabled(true);
//            this.setFillAfter(true);

            LogUtil.d(TAG, "MyTranslateAnimation from "
                    + "[ " + fromX + "," + fromY + "," + lp.width  + "," + lp.height + "]" + ",to"
                    + "[ " + toX + "," + toY + "," + lp.width  + "," + lp.height + "]");
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            LogUtil.d(TAG, "applyTransformation interpolatedTime = " + interpolatedTime);
            WindowManager.LayoutParams lp = mLayoutParams;//(WindowManager.LayoutParams)view.getLayoutParams();
            int newX = 0;
            int newY = 0;
            if (interpolatedTime == 1.0f) {
                newX = toX;
                newY = toY;
            } else {
                newX = (int)(fromX + (toX - fromX) * interpolatedTime);
                newY = (int)(fromY + (toY - fromY) * interpolatedTime);
            }
            if (newX != lp.x || newY != lp.y) {
                LogUtil.d(TAG, "applyTransformation new [" + newX + "," + newY + "]");
                lp.x = newX;
                lp.y = newY;
//                view.setLayoutParams(lp);
                mLayoutParams = lp;
                mWindowManager.updateViewLayout(PopupLayout.this, mLayoutParams);
            }
//            if (interpolatedTime == 1.0f) {
//                if (!visibleAfterAnimation) {
//                    view.setVisibility(View.GONE);
//                }
//            }
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }
}
