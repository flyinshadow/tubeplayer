package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;

/**
 * Created by LiJiaZhi on 2017/9/26.
 * 插屏广告聚合
 */

public class Interstitial {
    private static final String TAG = "Interstitial";
    private com.facebook.ads.InterstitialAd mFacebookAd;
    private com.google.android.gms.ads.InterstitialAd mGoogleAD;
    private Context mContext;
    private String mAdId;

    public void loadAD(final Context context, long type, final String adId, final ADListener listener) {
        if (ADManager.sLevel == ADManager.Level_None) {
            return;
        }
        mAdId = adId;
        mContext = context;
        if (type == ADManager.AD_MobVista) {
        } else if (type == ADManager.AD_Facebook) {
            mFacebookAd = new InterstitialAd(context, adId);
            mFacebookAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                    if (null != listener) {
                        listener.onAdDisplayed();
                    }
                    submitImpression(adId, true);
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    if (null != mFacebookAd) {
                        mFacebookAd.destroy();
                    }
                    if (null != listener) {
                        listener.onAdClose();
                    }
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    submitError(adId, true, adError.getErrorCode());
                    //如果facebook加载失败，尝试加载google ad
                    mFacebookAd = null;
                    if (adId == ADConstants.facebook_first_open_interstitial) {
                        loadAD(context, ADManager.AD_Google, ADConstants.google_first_open_interstitial, listener);
                    } else if (adId == ADConstants.facebook_video_back_interstitial) {
                        loadAD(context, ADManager.AD_Google, ADConstants.google_video_back_interstitial, listener);
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    listener.onLoadedSuccess();
                    submitLoaded(adId, true);
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.e(TAG, "onAdClicked ");
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            submitRequest(adId, true);
            mFacebookAd.loadAd();
        } else if (type == ADManager.AD_Google) {
            mGoogleAD = new com.google.android.gms.ads.InterstitialAd(context);
            mGoogleAD.setAdUnitId(adId);
            mGoogleAD.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    listener.onLoadedSuccess();
                    submitLoaded(adId, false);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    listener.onLoadedFailed();
                    submitError(adId, false, errorCode);
                }

                @Override
                public void onAdOpened() {
                    if (null != listener) {
                        listener.onAdDisplayed();
                    }
                    submitImpression(adId, false);
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
                    if (null != listener) {
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
                    .build();
            submitRequest(adId, false);
            mGoogleAD.loadAd(adRequest);
        }

    }

    /**
     * 在activty的ondestroy里处理
     */
    public void destroy() {
        if (mFacebookAd != null && mFacebookAd.isAdLoaded()) {
            mFacebookAd.destroy();
        }
    }

    public void show() {
        try {
            if (mFacebookAd != null) {
                submitShow(mAdId,true);
                mFacebookAd.show();
            } else if (mGoogleAD != null && mGoogleAD.isLoaded()) {
                submitShow(mAdId,false);
                mGoogleAD.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface ADListener {
        void onLoadedSuccess();

        void onLoadedFailed();

        void onAdDisplayed();

        void onAdClose();
    }

    private void submitRequest(String adId, boolean isFacebook) {
        Log.e(TAG, isFacebook?"facebook":"google"+" adRequest ");
        if (null == mContext) {
            return;
        }
        if (isFacebook) {
            if (adId == ADConstants.facebook_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_REQUEST + "open");
            } else if (adId == ADConstants.facebook_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_REQUEST + "back");
            }
        } else {
            if (adId == ADConstants.google_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_REQUEST + "open");
            } else if (adId == ADConstants.google_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_REQUEST + "back");
            } else if (adId == ADConstants.google_gif_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_REQUEST + "gif");
            }
        }
    }

    private void submitLoaded(String adId, boolean isFacebook) {
        Log.e(TAG, isFacebook?"facebook":"google"+" onAdLoaded ");
        if (null == mContext) {
            return;
        }
        if (isFacebook) {
            if (adId == ADConstants.facebook_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_LOADED + "open");
            } else if (adId == ADConstants.facebook_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_LOADED + "back");
            }
        } else {
            if (adId == ADConstants.google_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_LOADED + "open");
            } else if (adId == ADConstants.google_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_LOADED + "back");
            } else if (adId == ADConstants.google_gif_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_LOADED + "gif");
            }
        }
    }

    private void submitError(String adId, boolean isFacebook, int error) {
        Log.e(TAG, isFacebook?"facebook":"google"+" onError " + error);
        if (null == mContext) {
            return;
        }
        if (isFacebook) {
            if (adId == ADConstants.facebook_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_FAILED + "open");
            } else if (adId == ADConstants.facebook_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_FAILED + "back");
            }
        } else {
            if (adId == ADConstants.google_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_FAILED + "open");
            } else if (adId == ADConstants.google_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_FAILED + "back");
            } else if (adId == ADConstants.google_gif_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_FAILED + "gif");
            }
        }
    }

    private void submitImpression(String adId, boolean isFacebook) {
        Log.e(TAG, isFacebook?"facebook":"google"+" Impression ");
        if (null == mContext) {
            return;
        }
        if (isFacebook) {
            if (adId == ADConstants.facebook_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_IMPRESSION + "open");
            } else if (adId == ADConstants.facebook_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_IMPRESSION + "back");
            }
        } else {
            if (adId == ADConstants.google_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_IMPRESSION + "open");
            } else if (adId == ADConstants.google_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_IMPRESSION + "back");
            } else if (adId == ADConstants.google_gif_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_IMPRESSION + "gif");
            }
        }
    }
    private void submitShow(String adId, boolean isFacebook) {
        Log.e(TAG, isFacebook?"facebook":"google"+" Impression ");
        if (null == mContext) {
            return;
        }
        if (isFacebook) {
            if (adId == ADConstants.facebook_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_SHOW + "open");
            } else if (adId == ADConstants.facebook_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "facebook_" + StatisticsManager.ITEM_AD_INTERSTITIAL_SHOW + "back");
            }
        } else {
            if (adId == ADConstants.google_first_open_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_SHOW + "open");
            } else if (adId == ADConstants.google_video_back_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_SHOW + "back");
            } else if (adId == ADConstants.google_gif_interstitial) {
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, "google_" + StatisticsManager.ITEM_AD_INTERSTITIAL_SHOW + "gif");
            }
        }
    }
}
