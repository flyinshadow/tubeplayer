package com.wenjoyai.tubeplayer.widget;

/**
 * @author：LiJiaZhi on 2017/12/4
 * @des：ToDo
 * @org mtime.com
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 自定义控件：圆角RelativeLayout
 */
public class RoundFrameLayout extends FrameLayout {
    private RoundViewDelegate mRoundViewDelegate;

    public RoundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mRoundViewDelegate == null) {
            mRoundViewDelegate = new RoundViewDelegate(this, getContext());
        }
    }

    public RoundFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (mRoundViewDelegate == null) {
            mRoundViewDelegate = new RoundViewDelegate(this, getContext());
        }
    }

    public RoundFrameLayout(Context context) {
        super(context);
        if (mRoundViewDelegate == null) {
            mRoundViewDelegate = new RoundViewDelegate(this, getContext());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = getWidth();
        int h = getHeight();
        mRoundViewDelegate.roundRectSet(w, h);
    }

    @Override
    public void draw(Canvas canvas) {
        mRoundViewDelegate.canvasSetLayer(canvas);
        super.draw(canvas);
        canvas.restore();
    }

}