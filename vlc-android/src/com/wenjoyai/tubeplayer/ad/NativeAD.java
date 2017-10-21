package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.video.VideoPlayerActivity;

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

    public void loadAD(final Context context, long type, String adId, final ADListener listener) {
        mAdId = adId;
        mType = type;
        mListener = listener;
        mContext = context;
        mFacebookAd = new NativeAd(context, mAdId);
        mFacebookAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                if (mAdId == ADConstants.facebook_video_feed_native) {
                    StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FACEBOOK_FAILED + " 1 " + error.getErrorCode());
                } else if (mAdId == ADConstants.facebook_video_feed_native1) {
                    StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FACEBOOK_FAILED + " 2 " + error.getErrorCode());
                } else if (mAdId == ADConstants.facebook_video_feed_native2) {
                    StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FACEBOOK_FAILED + " 3 " + error.getErrorCode());
                }
                // Ad error callback
                Log.e(TAG, "onError " + error.getErrorCode() + error.getErrorMessage());
                startProgressTimer();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.e(TAG, "onAdLoaded");
                // Ad loaded callback
                if (null != listener) {
                    listener.onLoadedSuccess(mFacebookAd, mAdId);
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
                if (mAdId == ADConstants.facebook_video_feed_native) {
                    StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_1);
                } else if (mAdId == ADConstants.facebook_video_feed_native1) {
                    StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_2);
                } else if (mAdId == ADConstants.facebook_video_feed_native2) {
                    StatisticsManager.submitAd(context, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_3);
                }
            }
        });
        // Request an ad
        Log.e(TAG, "loadAd ");
        mFacebookAd.loadAd(NativeAd.MediaCacheFlag.ALL);
    }

    public interface ADListener {
        void onLoadedSuccess(NativeAd ad, String adId);

        void onLoadedFailed(String msg, String adId);

        void onAdClick();
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
    public final  int CODE_REFRESH = 1;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CODE_REFRESH:
                    Log.e(TAG, "MyTimerTask "+mAdId+" "+mRetryCount);
                    mRetryCount++;
                    cancelProgressTimer();
                    if (mRetryCount > 2) {
                        Log.e(TAG, "retry all still failed"+mRetryCount);
                        if (null != mListener) {
                            mListener.onLoadedFailed("retry all still failed", mAdId);
                        }
                    } else {
                        loadAD(mContext,mType,mAdId,mListener);
                    }
                    break;
            }
        }
    };

    protected class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Message msg= mHandler.obtainMessage();
            msg.what = CODE_REFRESH;
            mHandler.sendMessage(msg);
        }
    }
}
