package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.NativeAd;

import java.util.ArrayList;
import java.util.List;

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
    public static final long Level_Little = 1;//只有feed流和pause的native
    public static final long Level_Normal = 2;//加上插屏
    public static final long Level_Big = 3;//加上banner
    public static long sLevel = Level_Normal;


    //播放广告延迟加载时间
    public static long back_ad_delay_time = 30;


    public static boolean isShowGoogleVideoBanner = false;//是否显示视频列表页的banner
    public static boolean isShowMobvista = false;//是否显示旋转动画的mobvista广告


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
        mAdIdList.add(ADConstants.facebook_video_feed_native1);
        mAdIdList.add(ADConstants.facebook_video_feed_native2);
        mAdIdList.add(ADConstants.facebook_video_feed_native3);
        mAdIdList.add(ADConstants.facebook_video_feed_native4);
        mAdIdList.add(ADConstants.facebook_video_feed_native5);
        mAdIdList.add(ADConstants.facebook_video_feed_native6);
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
    private LimitQueue<NativeAd> mReadyQueue = new LimitQueue<>(mNum);
    public List<String> mAdIdList = new ArrayList<>();
    private int mIndex = 0;

    /**
     * 获取下一个广告位
     * @return
     */
    private String getNextAdId() {
        String adId = mAdIdList.get(mIndex % mAdIdList.size());
        mIndex++;
        return adId;
    }

    /**
     * 开始加载广告
     */
    public void startLoadAD(Context context) {
        Log.e("ADManager", "startLoadAD " + mAdIdList.size());
        mContext = context;
        mReadyQueue.clear();
        mIndex = 0;
        //初始化加载三个
        for (int i = 0; i < mNum; i++) {
            loadAD();
        }
    }



    private void loadAD() {
        Log.e("ADManager", "loadAD " + mIndex + " ");
        final NativeAD mFeedNativeAD = new NativeAD();
        mFeedNativeAD.loadAD(mContext, ADManager.AD_Facebook, getNextAdId(), new NativeAD.ADListener() {
            @Override
            public void onLoadedSuccess(com.facebook.ads.NativeAd ad, String adId) {
                Log.e("ADManager", "onLoadedSuccess "+adId);
                if (null != ad) {
                    mReadyQueue.offer(ad);
                    if (mReadyQueue.size() == 1) {//只要有了一个广告成功就通知上层展示
                        callbackAD();
                    }
                }
            }

            @Override
            public void onLoadedFailed(String msg, String adId) {
                Log.e("ADManager", "onLoadedFailed ");
                checkNeedLoad();
            }

            @Override
            public void onAdClick() {

            }

            @Override
            public void onAdImpression(NativeAd ad, String adId) {
                Log.e("ADManager", "onAdImpression ");
                boolean success = mReadyQueue.remove(ad);
                if (success) {
                    Log.e("ADManager", "onAdImpression success "+ adId);
                    checkNeedLoad();
                }
            }
        });
    }

    private void checkNeedLoad() {
        if (mReadyQueue.size()<mReadyQueue.getLimit()){
            loadAD();
        }
    }

    /**
     * 获取广告
     *
     * @return
     */
    public void getNativeAdlist(ADNumListener listener) {
        mListener = listener;
        callbackAD();
    }

    //回调给上层广告数组
    protected void callbackAD() {
        Log.e("ADManager", "callbackAD "+mReadyQueue.size());
        if (null != mListener && mReadyQueue.size() > 0) {
            List<com.facebook.ads.NativeAd> tempList = new ArrayList<>();
            for (int i = 0; i < mReadyQueue.size(); i++) {
                tempList.add(mReadyQueue.get(i));
            }
            if (tempList.size() > 0) {
                mListener.onLoadedSuccess(tempList);
            }
        }
    }

//    protected void startProgressTimer() {
//        try {
//            UPDATE_PROGRESS_TIMER = new Timer();
//            mProgressTimerTask = new MyTimerTask();
//            UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, REQUEST_FEED_NTIVE_INTERVAL * 1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void cancelProgressTimer() {
//        if (UPDATE_PROGRESS_TIMER != null) {
//            UPDATE_PROGRESS_TIMER.cancel();
//        }
//        if (mProgressTimerTask != null) {
//            mProgressTimerTask.cancel();
//        }
//    }
//
//    public final int CODE_REFRESH = 1;
//
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case CODE_REFRESH:
//                    if (VLCApplication.getAppContext().mAppForeground) {
//                        mFailed = 0;
//                        mNativeAdlist.clear();
//                        for (int i = 0; i < mNum; i++) {
//                            final NativeAD mFeedNativeAD = new NativeAD();
//                            String adUnit = "";
//                            if (i % 3 == 0) {
//                                adUnit = ADConstants.facebook_video_feed_native1;
//                            } else if (i % 3 == 1) {
//                                adUnit = ADConstants.facebook_video_feed_native2;
//                            } else {
//                                adUnit = ADConstants.facebook_video_feed_native3;
//                            }
//                            mFeedNativeAD.loadAD(mContext, ADManager.AD_Facebook, adUnit, new NativeAD.ADListener() {
//                                @Override
//                                public void onLoadedSuccess(com.facebook.ads.NativeAd ad, String adId) {
//                                    if (null != ad) {
//                                        mNativeAdlist.add(ad);
//                                        if (mNativeAdlist.size()==1){//只要有了一个广告成功就通知上层展示
//                                            callbackAD();
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onLoadedFailed(String msg, String adId) {
//                                    mFailed++;
//                                    if (mFailed == mNum) {
//                                        StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + " all ");
//                                    }
//                                }
//
//                                @Override
//                                public void onAdClick() {
//
//                                }
//                            });
//                        }
//                    }
//                    break;
//            }
//        }
//    };
//
//
//    protected class MyTimerTask extends TimerTask {
//        @Override
//        public void run() {
//            Message msg = mHandler.obtainMessage();
//            msg.what = CODE_REFRESH;
//            mHandler.sendMessage(msg);
//        }
//    }

    public interface ADNumListener {
        void onLoadedSuccess(List<NativeAd> list);
    }

}
