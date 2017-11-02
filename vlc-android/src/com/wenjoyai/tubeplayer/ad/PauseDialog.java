package com.wenjoyai.tubeplayer.ad;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
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

public class PauseDialog extends Dialog {
    NativeAdScrollView scrollView;
    ImageView mCloseIv;
    Context mContext;

    public PauseDialog(Context context) {
        super(context);
        mContext =context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pause);
        getWindow().setWindowAnimations(R.style.dialog_style);

        ADManager.getInstance().mIsPauseADShown = true;
        if (scrollView != null) {
            ((LinearLayout) findViewById(R.id.hscrollContainer)).removeView(scrollView);
        }
        scrollView = new NativeAdScrollView(getContext(), ADManager.getInstance().mPauseManager,
                NativeAdView.Type.HEIGHT_300);
        ((LinearLayout) findViewById(R.id.hscrollContainer)).addView(scrollView);
        StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_PAUSE_ADS + "shown");
        mCloseIv = (ImageView)findViewById(R.id.ad_close_iv);
        mCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
