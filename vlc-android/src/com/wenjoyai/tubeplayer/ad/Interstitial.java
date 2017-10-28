package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

/**
 * Created by LiJiaZhi on 2017/9/26.
 * 插屏广告聚合
 */

public class Interstitial {
    private static final String TAG = "Interstitial";
    //facebook
    com.facebook.ads.InterstitialAd mFacebookAd;
    com.google.android.gms.ads.InterstitialAd mGoogleAD;
//    MVInterstitialHandler mInterstitialHandler;

    public void loadAD(final Context context, long type, final String adId, final ADListener listener) {
        //nomral级别以上才展示插屏
        if (ADManager.sLevel<ADManager.Level_Normal){
            return;
        }
        if (type == ADManager.AD_MobVista) {
        } else if (type == ADManager.AD_Facebook) {
            Log.e(TAG, "startloading ");
            mFacebookAd = new InterstitialAd(context, adId);
            mFacebookAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                    Log.e(TAG, "onInterstitialDisplayed ");
                    if (null != listener){
                        listener.onAdClick();
                    }

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    if (null != mFacebookAd){
                        mFacebookAd.destroy();
                    }
                    if (null!= listener){
                        listener.onAdClose();
                    }
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.e(TAG, "onError "+adError.getErrorCode()+"  "+adError.getErrorMessage());
                    //如果facebook加载失败，尝试加载google ad
                    mFacebookAd = null;
                    if (adId==ADConstants.facebook_first_open_interstitial){
                        loadAD(context, ADManager.AD_Google,ADConstants.google_first_open_interstitial,listener);
                    } else if (adId ==ADConstants.facebook_video_back_interstitial){
                        loadAD(context, ADManager.AD_Google,ADConstants.google_video_back_interstitial,listener);
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e(TAG, "onAdLoaded ");
                    listener.onLoadedSuccess();
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.e(TAG, "onAdClicked ");
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            mFacebookAd.loadAd();
        } else if (type == ADManager.AD_Google) {
            mGoogleAD = new com.google.android.gms.ads.InterstitialAd(context);
            mGoogleAD.setAdUnitId(adId);
            mGoogleAD.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.e(TAG, "onAdLoaded");
                    listener.onLoadedSuccess();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.e(TAG, "onAdFailedToLoad "+errorCode);
                    listener.onLoadedFailed();
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when the ad is displayed.
                    Log.e(TAG, "onAdOpened");
                    if (null != listener){
                        listener.onAdClick();
                    }
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    Log.i("Ads", "onAdLeftApplication");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when when the interstitial ad is closed.
                    Log.e(TAG, "onAdClosed");
                    if (null!= listener){
                        listener.onAdClose();
                    }
                }

                @Override
                public void onAdClicked() {
                    Log.e(TAG, "onAdClicked");
                    super.onAdClicked();
                }

                @Override
                public void onAdImpression() {
                    Log.e(TAG, "onAdImpression");
                    super.onAdImpression();
                }
            });
            AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("88EC935CF17E8EACA538F5A876BB5355")
                    .build();
            mGoogleAD.loadAd(adRequest);
        }

    }

    /**
     * 在activty的ondestroy里处理
     */
//    public void destroy() {
//        if (mFacebookAd != null) {
//            mFacebookAd.destroy();
//        } else if (mGoogleAD != null) {
//        }
//    }

    public void show() {
        if (mFacebookAd != null) {
            mFacebookAd.show();
        } else if (mGoogleAD != null && mGoogleAD.isLoaded()) {
            mGoogleAD.show();
        }
//        else if (mInterstitialHandler != null) {
//            mInterstitialHandler.show();
//        }
    }


    public interface ADListener {
        void onLoadedSuccess();

        void onLoadedFailed();
        void onAdClick();
        void onAdClose();
    }
}
