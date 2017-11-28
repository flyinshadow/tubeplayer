package com.wenjoyai.tubeplayer.ad;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.R;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;


/**
 * Created by LiJiaZhi on 16/12/31.
 * exit对话框
 */

public class ExitDialog extends Dialog {
    public AutoScrollViewPager viewPager;
    ImageView mCloseTv;
    Context mContext;
    MyViewPagerAdapter myViewPagerAdapter;

    public ExitDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_exit);
        getWindow().setWindowAnimations(R.style.dialog_gif_style);
        getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);


        viewPager = (AutoScrollViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        mCloseTv = (ImageView) findViewById(R.id.exit_cancel);
        mCloseTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        myViewPagerAdapter = new MyViewPagerAdapter(ADManager.getInstance().getFeeds());
        viewPager.setAdapter(myViewPagerAdapter);
        myViewPagerAdapter.notifyDataSetChanged();
        viewPager.setCycle(true);
        viewPager.setInterval(2000);
        viewPager.startAutoScroll();
    }

    class MyViewPagerAdapter extends PagerAdapter {

        //直接继承PagerAdapter，至少必须重写下面的四个方法，否则会报错
        private List<View> views = new ArrayList<>();
        private List<NativeAd> mDatas = new ArrayList<>();

        public MyViewPagerAdapter(List<NativeAd> mDatas) {
            this.mDatas = mDatas;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mDatas.get(position).unregisterView();
            container.removeView(views.get(position));//删除页卡
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.layout_pause_native_ad, null);

            NativeAd nativeAd = mDatas.get(position);
            // Create native UI using the ad_front metadata.
            ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.native_ad_icon);
            TextView nativeAdTitle = (TextView) adView.findViewById(R.id.native_ad_title);
            MediaView nativeAdMedia = (MediaView) adView.findViewById(R.id.native_ad_media);
            // TextView nativeAdSocialContext = (TextView) adView.findViewById(R.id.native_ad_social_context);
            TextView nativeAdBody = (TextView) adView.findViewById(R.id.native_ad_body);
            Button nativeAdCallToAction = (Button) adView.findViewById(R.id.native_ad_call_to_action);

            // Set the Text.
            nativeAdTitle.setText(nativeAd.getAdTitle());
            // nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
            nativeAdBody.setText(nativeAd.getAdBody());
            nativeAdCallToAction.setText(nativeAd.getAdCallToAction());

            // Download and display the ad_front icon.
            NativeAd.Image adIcon = nativeAd.getAdIcon();
            NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

            // Download and display the cover image.
            nativeAdMedia.setNativeAd(nativeAd);

            // Add the AdChoices icon
            LinearLayout adChoicesContainer = (LinearLayout) adView.findViewById(R.id.ad_choices_container);
            AdChoicesView adChoicesView = new AdChoicesView(adView.getContext(), nativeAd, true);
            adChoicesContainer.addView(adChoicesView);

            // Register the Title and CTA button to listen for clicks.
            List<View> clickableViews = new ArrayList<>();
            clickableViews.add(nativeAdTitle);
            clickableViews.add(nativeAdIcon);
            clickableViews.add(nativeAdMedia);
            clickableViews.add(nativeAdBody);
            clickableViews.add(nativeAdCallToAction);
            nativeAd.unregisterView();
            nativeAd.registerViewForInteraction(adView, clickableViews);

            views.add(adView);
            //这个方法用来实例化页卡
            container.addView(views.get(position));

            return views.get(position);
        }

        @Override
        public int getCount() {
            return mDatas.size();//返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;//官方提示这样写
        }
    }
}


