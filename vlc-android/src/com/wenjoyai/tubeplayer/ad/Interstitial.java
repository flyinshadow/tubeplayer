package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.out.InterstitialListener;
import com.mobvista.msdk.out.MVInterstitialHandler;

import java.util.HashMap;

/**
 * Created by LiJiaZhi on 2017/9/26.
 * 插屏广告聚合
 */

public class Interstitial {
    private static final String TAG = "Interstitial";
    //facebook
    com.facebook.ads.InterstitialAd mFacebookAd;
    com.google.android.gms.ads.InterstitialAd mGoogleAD;
    MVInterstitialHandler mInterstitialHandler;

    public void loadAD(Context context, long type, String adId, final ADListener listener) {
        if (type == ADManager.AD_MobVista) {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            //设置广告位ID 必填
            hashMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, adId);
            mInterstitialHandler = new MVInterstitialHandler(context, hashMap);
            mInterstitialHandler.setInterstitialListener(new InterstitialListener() {
                /**
                 * 当Interstitial显示成功后回调
                 */
                @Override
                public void onInterstitialShowSuccess() {
                    Log.e(TAG, "onInterstitialShowSuccess");
                    listener.onLoadedSuccess();
                }

                /**
                 * 当Interstitial显示错误后回调
                 * @prams errorMsg 错误消息
                 */
                @Override
                public void onInterstitialShowFail(String errorMsg) {
                    Log.e(TAG, "onInterstitialShowFail errorMsg:" + errorMsg);
                    listener.onLoadedFailed();
                }

                /**
                 * 当Interstitial广告加载成功后回调
                 */
                @Override
                public void onInterstitialLoadSuccess() {
                    Log.e(TAG, "onInterstitialLoadSuccess");
                }

                /**
                 * 当Interstitial 广告加载成功后回调
                 * @prams errorMsg 错误消息
                 */
                @Override
                public void onInterstitialLoadFail(String errorMsg) {
                    Log.e(TAG, "onInterstitialLoadFail errorMsg:" + errorMsg);
                }

                /**
                 * 当Interstitial关闭后回调
                 *
                 */
                @Override
                public void onInterstitialClosed() {
                    Log.e(TAG, "onInterstitialClosed");
                }

                /**
                 * 当Interstitial广告被点击后回调
                 */
                @Override
                public void onInterstitialAdClick() {
                    Log.e(TAG, "onInterstitialAdClick");
                    if (null != listener){
                        listener.onAdClick();
                    }
                }
            });
            mInterstitialHandler.preload();
        } else if (type == ADManager.AD_Facebook) {
            mFacebookAd = new InterstitialAd(context, adId);
            mFacebookAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    if (null != mFacebookAd){
                        mFacebookAd.destroy();
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
        } else if (mInterstitialHandler != null) {
            mInterstitialHandler.show();
        }
    }


    public interface ADListener {
        void onLoadedSuccess();

        void onLoadedFailed();
        void onAdClick();
    }
}
