package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by LiJiaZhi on 2017/9/26.
 * 原生广告聚合
 */

public class NativeAD {
    private static final String TAG = "NativeAD";
    //facebook
    private NativeAd mFacebookAd;
    private String mAdId;
    private long mType;
    private ADListener mListener;
    private Context mContext;
    protected Timer UPDATE_PROGRESS_TIMER;
    protected MyTimerTask mProgressTimerTask;
    private int mRetryCount = 0;

    public NativeAD() {
        mRetryCount = 0;
    }

    public void loadAD(final Context context, long type, final String adId, final ADListener listener) {
        mAdId = adId;
        mType = type;
        mListener = listener;
        mContext = context;
        mFacebookAd = new NativeAd(context, mAdId);
        mFacebookAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                submitError(mAdId, error);
                startProgressTimer();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                submitLoaded(mAdId);
                // Ad loaded callback
                if (null != listener) {
                    listener.onLoadedSuccess(mFacebookAd, mAdId);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                submitClick(mAdId);
                // Ad clicked callback
                if (null != listener) {
                    listener.onAdClick();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                if (null != listener) {
                    listener.onAdImpression(mFacebookAd,mAdId);
                }
                submitImpression(mAdId);
                Log.e(TAG, "onLoggingImpression");
            }
        });
        // Request an ad
        submitRequest(mAdId);
        mFacebookAd.loadAd(NativeAd.MediaCacheFlag.ALL);
    }

    public interface ADListener {
        void onLoadedSuccess(NativeAd ad, String adId);

        void onLoadedFailed(String msg, String adId);

        void onAdClick();

        void onAdImpression(NativeAd ad, String adId);
    }

    protected void startProgressTimer() {
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new MyTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 5 * 1000);//5秒
    }

    protected void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    public final int CODE_REFRESH = 1;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_REFRESH:
                    Log.e(TAG, "MyTimerTask " + mAdId + " " + mRetryCount);
                    mRetryCount++;
                    cancelProgressTimer();
                    if (mRetryCount > 2) {
                        Log.e(TAG, "retry all still failed" + mRetryCount);
                        if (null != mListener) {
                            mListener.onLoadedFailed("retry all still failed", mAdId);
                        }
                    } else {
                        loadAD(mContext, mType, mAdId, mListener);
                    }
                    break;
            }
        }
    };

    protected class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Message msg = mHandler.obtainMessage();
            msg.what = CODE_REFRESH;
            mHandler.sendMessage(msg);
        }
    }

    private void submitRequest(String adId) {
        Log.e(TAG, "adRequest ");
        if (null == mContext) {
            return;
        }
        // Ad impression logged callback
        if (adId == ADConstants.facebook_video_feed_native1) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_REQUEST + "1");
        } else if (adId == ADConstants.facebook_video_feed_native2) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_REQUEST + "2");
        } else if (adId == ADConstants.facebook_video_feed_native3) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_REQUEST + "3");
        } else if (adId == ADConstants.facebook_video_feed_native4) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_REQUEST + "4");
        } else if (adId == ADConstants.facebook_video_feed_native5) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_REQUEST + "5");
        } else if (adId == ADConstants.facebook_video_feed_native6) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_REQUEST + "6");
        }
    }

    private void submitLoaded(String adId) {
        Log.e(TAG, "onAdLoaded");
        if (null == mContext) {
            return;
        }
        // Ad impression logged callback
        if (adId == ADConstants.facebook_video_feed_native1) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_LOADED + "1");
        } else if (adId == ADConstants.facebook_video_feed_native2) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_LOADED + "2");
        } else if (adId == ADConstants.facebook_video_feed_native3) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_LOADED + "3");
        } else if (adId == ADConstants.facebook_video_feed_native4) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_LOADED + "4");
        } else if (adId == ADConstants.facebook_video_feed_native5) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_LOADED + "5");
        } else if (adId == ADConstants.facebook_video_feed_native6) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_LOADED + "6");
        }
    }

    private void submitError(String adId, AdError error) {
        Log.e(TAG, "onError " + error.getErrorCode() + error.getErrorMessage());
        if (null == mContext) {
            return;
        }
        if (adId == ADConstants.facebook_video_feed_native1) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + "1 " + error.getErrorCode());
        } else if (adId == ADConstants.facebook_video_feed_native2) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + "2 " + error.getErrorCode());
        } else if (adId == ADConstants.facebook_video_feed_native3) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + "3 " + error.getErrorCode());
        } else if (adId == ADConstants.facebook_video_feed_native4) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + "4 " + error.getErrorCode());
        } else if (adId == ADConstants.facebook_video_feed_native5) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + "5 " + error.getErrorCode());
        } else if (adId == ADConstants.facebook_video_feed_native6) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + "6 " + error.getErrorCode());
        }

    }

    private void submitImpression(String adId) {
        if (null == mContext) {
            return;
        }
        // Ad impression logged callback
        if (adId == ADConstants.facebook_video_feed_native1) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_IMPRESSION + "1");
        } else if (adId == ADConstants.facebook_video_feed_native2) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_IMPRESSION + "2");
        } else if (adId == ADConstants.facebook_video_feed_native3) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_IMPRESSION + "3");
        } else if (adId == ADConstants.facebook_video_feed_native4) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_IMPRESSION + "4");
        } else if (adId == ADConstants.facebook_video_feed_native5) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_IMPRESSION + "5");
        } else if (adId == ADConstants.facebook_video_feed_native6) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_IMPRESSION + "6");
        }
    }

    private void submitClick(String adId) {
        Log.e(TAG, "onAdClicked");
        if (null == mContext) {
            return;
        }
        // Ad impression logged callback
        if (adId == ADConstants.facebook_video_feed_native1) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_CLICK + "1");
        } else if (adId == ADConstants.facebook_video_feed_native2) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_CLICK + "2");
        } else if (adId == ADConstants.facebook_video_feed_native3) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_CLICK + "3");
        } else if (adId == ADConstants.facebook_video_feed_native4) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_CLICK + "4");
        } else if (adId == ADConstants.facebook_video_feed_native5) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_CLICK + "5");
        } else if (adId == ADConstants.facebook_video_feed_native6) {
            StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_CLICK + "6");
        }
    }


}
