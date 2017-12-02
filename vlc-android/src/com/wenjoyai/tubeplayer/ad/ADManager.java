package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.video.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by LiJiaZhi on 2017/9/23.
 * 广告管理类
 */

public class ADManager {

    //广告平台   1：MobVista   2：google   3:facebook  4:百度
    public static final long AD_Google = 1;
    public static final long AD_Facebook = 2;
    public static final long AD_MobVista = 3;
    public static final long AD_Baidu = 4;
    public static long sPlatForm = AD_Facebook;

    //广告级别
    public static final long Level_None = 0; //无广告
    public static final long Level_Little = 1;//只有feed流和pause的native
    public static final long Level_Normal = 2;//加上插屏
    public static final long Level_Big = 3;//加上banner
    public static long sLevel = Level_Normal;


    //播放广告延迟加载时间
    public static long back_ad_delay_time = 30;


    public static boolean isShowGoogleVideoBanner = false;//是否显示视频列表页的banner
    public static boolean isShowMobvista = false;//是否显示旋转动画的mobvista广告

    public static boolean isShowExit = true;//是否显示退出广告位

    //暂停广告---个数  默认1个
    public static long pasue_ad_count = 1;


    private static volatile ADManager instance;

    public static ADManager getInstance() {
        if (instance == null) {
            synchronized (ADManager.class) {
                if (instance == null) {
                    instance = new ADManager();
                }
            }
        }
        return instance;
    }

    private ADManager() {
        mAdIdList.clear();
        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native1));
        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native2));
        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native3));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native4));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native5));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native6));
    }

    //缓存广告数组
    private int mNum = 3;
    //请求广告时间间隔默认10分钟
//    public static long REQUEST_FEED_NTIVE_INTERVAL = 10 * 60;
//    protected Timer UPDATE_PROGRESS_TIMER;
//    protected MyTimerTask mProgressTimerTask;
    private Context mContext;
    private ADNumListener mListener;

    //准备好的，给上层的数据 3个
    private LimitQueue<NativeWrapper> mReadyQueue = new LimitQueue<>(mNum);
    public List<AdID> mAdIdList = new ArrayList<>();
    private int mIndex = 0;
    private int mFinished = 0;

    class AdID {
        public String adId;
        public boolean isDepreted = false;

        public AdID(String adId) {
            this.adId = adId;
        }
    }

    class NativeWrapper {
        public String adId;
        public NativeAd nativeAd;
        public boolean isShown = false;
        public int errorcode = 0;//0是成功

        public NativeWrapper(String adId, NativeAd nativeAd, int errorcode) {
            this.adId = adId;
            this.nativeAd = nativeAd;
            this.errorcode = errorcode;
        }
    }

    /**
     * feed流广告个数
     *
     * @return
     */
    public List<NativeAd> getFeeds() {
        List<com.facebook.ads.NativeAd> tempList = new ArrayList<>();
        for (int i = 0; i < mReadyQueue.size(); i++) {
            if (null != mReadyQueue.get(i).nativeAd/**&& !mReadyQueue.get(i).isShown*/) {
                tempList.add(mReadyQueue.get(i).nativeAd);
            }
        }
        return tempList;
    }

    /**
     * 获取下一个广告位
     *
     * @return
     */
    private String getNextAdId() {
        AdID adID = mAdIdList.get(mIndex % mAdIdList.size());
        mIndex++;
        return adID.adId;
    }


    /**
     * 开始加载广告
     */
    public void startLoadAD(Context context) {
        mContext = context;
        mReadyQueue.clear();
        //初始化加载三个
        mFinished = 0;
        for (int i = 0; i < mNum; i++) {
            loadAD();
        }
    }


    private void loadAD() {
        if (sLevel == Level_None) {
            return;
        }
        String adId = getNextAdId();
        if (!TextUtils.isEmpty(adId)) {
            new NativeAD().loadAD(mContext, ADManager.AD_Facebook, adId, new NativeAD.ADListener() {
                @Override
                public void onLoadedSuccess(com.facebook.ads.NativeAd ad, String adId) {
                    mFinished++;
                    Log.e("ADManager", "onLoadedSuccess " + adId);
                    if (null != ad) {
                        mReadyQueue.offer(new NativeWrapper(adId, ad, 0));
                        if (mReadyQueue.size() == 1) {//只要有了一个广告成功就通知上层展示
                            callbackAD(false);
                        }
                        if (mFinished == mNum) {
                            callbackAD(true);
                        }
                    }
                }

                @Override
                public void onLoadedFailed(String msg, String adId, int errorcode) {
                    Log.e("ADManager", "onLoadedFailed ");
                    mReadyQueue.offer(new NativeWrapper(adId, null, errorcode));
                    mFinished++;
                    if (mFinished == mNum) {
                        int failedCount = 0;
                        for (int i = 0; i < mNum; i++) {
                            if (null == mReadyQueue.get(i).nativeAd) {
                                failedCount++;
                            }
                        }
                        if (failedCount == mNum) {
                            //三个都失败了
                            loadInterstitial();
                        } else {
                            callbackAD(false);
                        }
                    }
                }

                @Override
                public void onAdClick() {

                }

                @Override
                public void onAdImpression(NativeAd ad, String adId) {
                    Log.e("ADManager", "onAdImpression ");
                    for (int i = 0; i < mReadyQueue.size(); i++) {
                        if (mReadyQueue.get(i).adId.equals(adId)) {
                            mReadyQueue.get(i).isShown = true;
                            return;
                        }
                    }
                }
            });
        }
    }

    /**
     * 获取广告
     *
     * @return
     */
    public void getNativeAdlist(ADNumListener listener) {
        mListener = listener;
        callbackAD(false);
    }

    //回调给上层广告数组
    protected void callbackAD(boolean needGif) {
        Log.e("ADManager", "callbackAD " + mReadyQueue.size() + " " + needGif);
        if (null != mListener) {
            List<com.facebook.ads.NativeAd> tempList = new ArrayList<>();
            for (int i = 0; i < mReadyQueue.size(); i++) {
                if (null != mReadyQueue.get(i).nativeAd) {
                    tempList.add(mReadyQueue.get(i).nativeAd);
                }
            }
            mListener.onLoadedSuccess(tempList, needGif);
        }
    }

    public NativeAdsManager mExitManager = null;
    public boolean mExitAdsLoaded = false;

    public void loadExitAD(final Context context) {
        if (sLevel == Level_None) {
            return;
        }
        mExitManager = new NativeAdsManager(context, ADConstants.facebook_video_feed_native4, 3);
        mExitManager.setListener(new NativeAdsManager.Listener() {
            @Override
            public void onAdsLoaded() {
                mExitAdsLoaded = true;
                Log.e("ADManager", "onAdsLoaded exit");
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_EXIT_ADS + "loaded");
            }

            @Override
            public void onAdError(AdError adError) {
                Log.e("ADManager", "onAdError exit " + adError.getErrorCode() + " " + adError.getErrorMessage());
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_EXIT_ADS + "error " + adError.getErrorCode());
            }
        });
        mExitManager.loadAds(NativeAd.MediaCacheFlag.ALL);
    }

    public NativeAdsManager mPauseManager;
    public boolean mIsPauseADShown = false;

    public void loadPauseAD(final Context context) {
        if (sLevel == Level_None) {
            return;
        }
        mIsPauseADShown = false;
        mPauseManager = new NativeAdsManager(context, ADConstants.facebook_video_feed_native6, (int) pasue_ad_count);
        mPauseManager.setListener(new NativeAdsManager.Listener() {
            @Override
            public void onAdsLoaded() {
                Log.e("ADManager", "onAdsLoaded pause");
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_PAUSE_ADS + "loaded");
            }

            @Override
            public void onAdError(AdError adError) {
                Log.e("ADManager", "onAdError pause " + adError.getErrorCode() + " " + adError.getErrorMessage());
                StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_PAUSE_ADS + "error " + adError.getErrorCode());
            }
        });
        mPauseManager.loadAds(NativeAd.MediaCacheFlag.ALL);
    }


    public interface ADNumListener {
        void onLoadedSuccess(List<NativeAd> list, boolean needGif);//是否需要展示小动画
    }

    public Interstitial mInterstitial;

    public void loadInterstitial() {
        mInterstitial = new Interstitial();
        mInterstitial.loadAD(mContext, ADManager.AD_Google, ADConstants.google_gif_interstitial, new Interstitial.ADListener() {
            @Override
            public void onLoadedSuccess() {
                Log.e("ADManager", "loadInterstitial success");
                callbackAD(true);
            }

            @Override
            public void onLoadedFailed() {
                Log.e("ADManager", "loadInterstitial failed");
            }

            @Override
            public void onAdDisplayed() {
            }

            @Override
            public void onAdClose() {

            }
        });
    }

}
