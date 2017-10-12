package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by LiJiaZhi on 2017/9/26.
 * banner广告聚合
 */

public class BannerAD {
    private static final String TAG = "BannerAD";
    //facebook
    com.facebook.ads.AdView mFacebookAd;
    AdView mGoogleAD;

    public View loadAD(Context context, long type, String adId, final ADListener listener) {
        //nomral级别以上才展示插屏
        if (ADManager.sLevel<ADManager.Level_Big){
            return null;
        }
        if (type == ADManager.AD_MobVista) {
        } else if (type == ADManager.AD_Facebook) {
            mFacebookAd = new com.facebook.ads.AdView(context, adId, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            mFacebookAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

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
                    Log.e(TAG, "onAdLoaded "+adError.getErrorCode()+"  "+adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    listener.onLoadedSuccess();
                }

                @Override
                public void onAdClicked(Ad ad) {
                    if (null != listener){
                        listener.onAdClick();
                    }
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            mFacebookAd.loadAd();
            return mFacebookAd;
        } else if (type == ADManager.AD_Google) {
            mGoogleAD = new AdView(context);
            mGoogleAD.setAdSize(AdSize.SMART_BANNER);
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
            AdRequest adRequest = new AdRequest.Builder().build();
            mGoogleAD.loadAd(adRequest);
            return mGoogleAD;
        }
        return null;
    }

    public void destroy(){
        if (null!= mGoogleAD){
            mGoogleAD.destroy();
        } else if (null!= mFacebookAd){
            mFacebookAd.destroy();
        }
    }
    public interface ADListener {
        void onLoadedSuccess();

        void onLoadedFailed();
        void onAdClick();
        void onAdClose();
    }
}
