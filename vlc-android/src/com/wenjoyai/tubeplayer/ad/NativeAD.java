package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;

/**
 * Created by LiJiaZhi on 2017/9/26.
 * 原生广告聚合
 */

public class NativeAD {
    private static final String TAG = "NativeAD";
    //facebook
    NativeAd mFacebookAd;
//    com.google.android.gms.ads.InterstitialAd mGoogleAD;

    public void loadAD(final Context context, long type, String adId, final ADListener listener) {
//        if (type == ADManager.AD_MobVista) {
//        } else if (type == ADManager.AD_Facebook) {
            mFacebookAd = new NativeAd(context, adId);
            mFacebookAd.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError error) {
                    // Ad error callback
                    Toast.makeText(context,"onError "+error.getErrorCode()+error.getErrorMessage(),Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onError "+error.getErrorCode()+error.getErrorMessage());
                    if (null!= listener){
                        listener.onLoadedFailed(error.getErrorCode()+error.getErrorMessage());
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Toast.makeText(context,"onLoadedSuccess ",Toast.LENGTH_LONG).show();
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
