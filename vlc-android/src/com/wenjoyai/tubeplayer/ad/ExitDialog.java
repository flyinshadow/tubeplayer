package com.wenjoyai.tubeplayer.ad;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.NativeAdScrollView;
import com.facebook.ads.NativeAdView;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;


/**
 * Created by LiJiaZhi on 16/12/31.
 * exit对话框
 */

public class ExitDialog extends Dialog {
    NativeAdScrollView scrollView;
    TextView mCancelTv;
    TextView mOkTv;
    Context mContext;
    public ExitDialog(Context context, int theme) {
        super(context, theme);
    }

    public ExitDialog(Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_exit);
        if (scrollView != null) {
            ((LinearLayout) findViewById(R.id.hscrollContainer)).removeView(scrollView);
        }
        scrollView = new NativeAdScrollView(getContext(), ADManager.getInstance().mExitManager,
                NativeAdView.Type.HEIGHT_400);
        ((LinearLayout) findViewById(R.id.hscrollContainer)).addView(scrollView);
        StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_EXIT_ADS + "shown");
        mCancelTv = (TextView)findViewById(R.id.exit_cancel);
        mOkTv = (TextView)findViewById(R.id.exit_ok);
        mCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mOkTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)mContext).finish();
            }
        });
    }
}
