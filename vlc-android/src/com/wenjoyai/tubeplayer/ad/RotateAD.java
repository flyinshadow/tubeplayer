package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wenjoyai.tubeplayer.R;

import java.util.Random;

/**
 * Created by LiJiaZhi on 2017/9/16.
 */

public class RotateAD extends FrameLayout {
    private ImageView mFrontIv;
    private ImageView mBackIv;
    private FrameLayout mRootLayout;
    private Handler mHandler;
    private Runnable mRunnable;
    private Random mRandom = new Random();
    private int[] mDrawables = {R.drawable.p1, R.drawable.p2,R.drawable.p3,R.drawable.p4, R.drawable.p5, R.drawable.p6, R.drawable.p7, R.drawable.p8};

    public RotateAD(@NonNull Context context) {
        super(context);
        init();
    }

    public RotateAD(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotateAD(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rotate_ad, this, true);
        mRootLayout = (FrameLayout) rootView;
        setCameraDistance();
        mFrontIv = (ImageView) rootView.findViewById(R.id.frontView);
        mBackIv = (ImageView) rootView.findViewById(R.id.backView);
        updateBackResource();
        //旋转
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                rotable();
                mHandler.postDelayed(this, 3000);
            }
        };
        mHandler.postDelayed(mRunnable, 0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mRunnable);
    }

    /**
     * 翻转
     */
    private void rotable() {
        if (View.VISIBLE == mBackIv.getVisibility()) {
            Rotatable rotatable = new Rotatable.Builder(mRootLayout).sides(R.id.frontView, R.id.backView)
                    .direction(Rotatable.ROTATE_Y).rotationCount(1).build();
            rotatable.setTouchEnable(false);
            rotatable.rotate(Rotatable.ROTATE_Y, 0, 3000);
        } else if (View.VISIBLE == mFrontIv.getVisibility()) {
            mBackIv.setRotationY(180f);
            Rotatable rotatable = new Rotatable.Builder(mRootLayout).sides(R.id.frontView, R.id.backView)
                    .direction(Rotatable.ROTATE_Y).rotationCount(1).build();
            rotatable.setTouchEnable(false);
            rotatable.rotate(Rotatable.ROTATE_Y, -180, 3000);
        }
    }

    /**
     * 改变视角距离, 贴近屏幕
     */
    private void setCameraDistance() {
        int distance = 10000;
        float scale = getResources().getDisplayMetrics().density * distance;
        mRootLayout.setCameraDistance(scale);
    }

    private void updateBackResource() {
        //每次点击都切换一次图标
        int random = mRandom.nextInt(8);
        mFrontIv.setImageResource(mDrawables[random]);
        mBackIv.setImageResource(mDrawables[random]);
    }

    /**
     * 外界点击事件
     *
     * @param listener
     */
    public void setOnClick(final View.OnClickListener listener) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view);
                updateBackResource();
            }
        });

    }

}
