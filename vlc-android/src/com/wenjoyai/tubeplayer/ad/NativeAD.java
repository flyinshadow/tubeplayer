package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.video.VideoPlayerActivity;

/**
 * Created by LiJiaZhi on 2017/9/26.
 * 原生广告聚合
 */

public class NativeAD {
    private static final String TAG = "NativeAD";
    //facebook
    NativeAd mFacebookAd;

    public void loadAD(final Context context, long type, final String adId, final ADListener listener) {
//        if (type == ADManager.AD_MobVista) {
//        } else if (type == ADManager.AD_Facebook) {
            mFacebookAd = new NativeAd(context, adId);
            mFacebookAd.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError error) {
                    // Ad error callback
                    Log.e(TAG, "onError "+error.getErrorCode()+error.getErrorMessage());
                    if (null!= listener){
                        listener.onLoadedFailed(error.getErrorCode()+error.getErrorMessage());
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e(TAG, "onAdLoaded");
                    // Ad loaded callback
                    if (null != listener) {
                        listener.onLoadedSuccess(mFacebookAd);
                    }
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.e(TAG, "onAdClicked");
                    // Ad clicked callback
                    if (null != listener) {
                        listener.onAdClick();
                    }
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.e(TAG, "onLoggingImpression");
                    // Ad impression logged callback
                    if (adId == ADConstants.facebook_video_feed_native){
                        StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_1);
                    } else if (adId == ADConstants.facebook_video_feed_native1){
                        StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_2);
                    } else if (adId == ADConstants.facebook_video_feed_native2){
                        StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_3);
                    }
                }
            });

            // Request an ad
            mFacebookAd.loadAd(NativeAd.MediaCacheFlag.ALL);
//        }
//        else if (type == ADManager.AD_Google) {
//        }
    }

    public interface ADListener {
        void onLoadedSuccess(NativeAd ad);

        void onLoadedFailed(String msg);

        void onAdClick();
    }
}
