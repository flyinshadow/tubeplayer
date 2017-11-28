//package com.wenjoyai.tubeplayer.ad;
//
//import android.content.Context;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.facebook.ads.NativeAd;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by LiJiaZhi on 2017/9/23.
// * 广告管理类
// */
//
//public class ADManager_backup {
//
//    //广告平台   1：MobVista   2：google   3:facebook  4:百度
//    public static final long AD_Google = 1;
//    public static final long AD_Facebook = 2;
//    public static final long AD_MobVista = 3;
//    public static final long AD_Baidu = 4;
//    public static long sPlatForm = AD_Facebook;
//
//    //广告级别
//    public static final long Level_Little = 1;//只有feed流和pause的native
//    public static final long Level_Normal = 2;//加上插屏
//    public static final long Level_Big = 3;//加上banner
//    public static long sLevel = Level_Normal;
//
//
//    //播放广告延迟加载时间
//    public static long back_ad_delay_time = 30;
//
//
//    public static boolean isShowGoogleVideoBanner = false;//是否显示视频列表页的banner
//    public static boolean isShowMobvista = false;//是否显示旋转动画的mobvista广告
//
//
//    private static volatile ADManager_backup instance;
//
//    public static ADManager_backup getInstance() {
//        if (instance == null) {
//            synchronized (ADManager_backup.class) {
//                if (instance == null) {
//                    instance = new ADManager_backup();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private ADManager_backup() {
//        mAdIdList.clear();
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native1));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native2));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native3));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native4));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native5));
//        mAdIdList.add(new AdID(ADConstants.facebook_video_feed_native6));
//    }
//
//    //缓存广告数组
//    private int mNum = 3;
//    //请求广告时间间隔默认10分钟
////    public static long REQUEST_FEED_NTIVE_INTERVAL = 10 * 60;
////    protected Timer UPDATE_PROGRESS_TIMER;
////    protected MyTimerTask mProgressTimerTask;
//    private Context mContext;
//    private ADNumListener mListener;
//
//    //准备好的，给上层的数据 3个
//    private LimitQueue<NativeAd> mReadyQueue = new LimitQueue<>(mNum);
//    public List<AdID> mAdIdList = new ArrayList<>();
//    private int mIndex = 0;
//
//    private long mStopTime = 0;
//
//    class AdID {
//        public String adId;
//        public boolean isDepreted = false;
//
//        public AdID(String adId) {
//            this.adId = adId;
//        }
//    }
//
//    /**
//     * 获取下一个广告位
//     *
//     * @return
//     */
//    private String getNextAdId() {
//        Log.e("ADManager", "getDepretedCount " +getDepretedCount());
//        AdID adID = mAdIdList.get(mIndex % mAdIdList.size());
//        if (adID.isDepreted) {
//            if (getDepretedCount() < mAdIdList.size()) {
//                mIndex++;
//                return getNextAdId();
//            } else {
//                if (0 == mStopTime) {
//                    mStopTime = System.currentTimeMillis() / 1000;
//                }
//                return null;
//            }
//        }
//        mIndex++;
//        return adID.adId;
//    }
//
//    private int getDepretedCount() {
//        int count = 0;
//        for (AdID ad : mAdIdList) {
//            if (ad.isDepreted) {
//                count++;
//            }
//        }
//        return count;
//    }
//
//    private void setDepreted(String adId) {
//        for (int i = 0; i < mAdIdList.size(); i++) {
//            if (mAdIdList.get(i).adId.equals(adId)) {
//                mAdIdList.get(i).isDepreted = true;
//                return;
//            }
//        }
//    }
//
//    private void reset() {
//        for (int i = 0; i < mAdIdList.size(); i++) {
//            mAdIdList.get(i).isDepreted = false;
//        }
//    }
//
//
//    /**
//     * 开始加载广告
//     */
//    public void startLoadAD(Context context) {
//        Log.e("ADManager", "startLoadAD " + mAdIdList.size());
//        mContext = context;
//        mReadyQueue.clear();
//        mIndex = 0;
//        //初始化加载三个
//        for (int i = 0; i < mNum; i++) {
//            loadAD();
//        }
//    }
//
//
//    private void loadAD() {
//        Log.e("ADManager", "loadAD " + mIndex + " ");
//        String adId = getNextAdId();
//        if (!TextUtils.isEmpty(adId)) {
//            new NativeAD().loadAD(mContext, ADManager_backup.AD_Facebook, adId, new NativeAD.ADListener() {
//                @Override
//                public void onLoadedSuccess(NativeAd ad, String adId) {
//                    Log.e("ADManager", "onLoadedSuccess " + adId);
//                    if (null != ad) {
//                        mReadyQueue.offer(ad);
//                        if (mReadyQueue.size() == 1) {//只要有了一个广告成功就通知上层展示
//                            callbackAD();
//                        }
//                    }
//                }
//
//                @Override
//                public void onLoadedFailed(String msg, String adId, int errorcode) {
//                    Log.e("ADManager", "onLoadedFailed ");
////                    if (errorcode == 1001){
//                    setDepreted(adId);
////                    }
//                    checkNeedLoad();
//                }
//
//                @Override
//                public void onAdDisplayed() {
//
//                }
//
//                @Override
//                public void onAdImpression(NativeAd ad, String adId) {
//                    Log.e("ADManager", "onAdImpression ");
//                    boolean success = mReadyQueue.remove(ad);
//                    if (success) {
//                        Log.e("ADManager", "onAdImpression success " + adId);
//                        checkNeedLoad();
//                    }
//                }
//            });
//        }
//    }
//
//    private void checkNeedLoad() {
//        if (mReadyQueue.size() < mReadyQueue.getLimit()) {
//            loadAD();
//        }
//    }
//
//    /**
//     * 获取广告
//     *
//     * @return
//     */
//    public void getNativeAdlist(ADNumListener listener) {
//        mListener = listener;
//        callbackAD();
//
//        if (getDepretedCount() == mAdIdList.size()) {
//            //已经体停止了
//            if (System.currentTimeMillis() / 1000 - mStopTime > 5 * 60) {
//                mStopTime = 0;
//                reset();
//                checkNeedLoad();
//            }
//        }
//    }
//
//    //回调给上层广告数组
//    protected void callbackAD() {
//        Log.e("ADManager", "callbackAD " + mReadyQueue.size());
//        if (null != mListener && mReadyQueue.size() > 0) {
//            List<NativeAd> tempList = new ArrayList<>();
//            for (int i = 0; i < mReadyQueue.size(); i++) {
//                tempList.add(mReadyQueue.get(i));
//            }
//            if (tempList.size() > 0) {
//                mListener.onLoadedSuccess(tempList);
//            }
//        }
//    }
//
////    protected void startProgressTimer() {
////        try {
////            UPDATE_PROGRESS_TIMER = new Timer();
////            mProgressTimerTask = new MyTimerTask();
////            UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, REQUEST_FEED_NTIVE_INTERVAL * 1000);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
////
////    public void cancelProgressTimer() {
////        if (UPDATE_PROGRESS_TIMER != null) {
////            UPDATE_PROGRESS_TIMER.cancel();
////        }
////        if (mProgressTimerTask != null) {
////            mProgressTimerTask.cancel();
////        }
////    }
////
////    public final int CODE_REFRESH = 1;
////
////    Handler mHandler = new Handler() {
////        @Override
////        public void handleMessage(Message msg) {
////            switch (msg.what) {
////                case CODE_REFRESH:
////                    if (VLCApplication.getAppContext().mAppForeground) {
////                        mFailed = 0;
////                        mNativeAdlist.clear();
////                        for (int i = 0; i < mNum; i++) {
////                            final NativeAD mFeedNativeAD = new NativeAD();
////                            String adUnit = "";
////                            if (i % 3 == 0) {
////                                adUnit = ADConstants.facebook_video_feed_native1;
////                            } else if (i % 3 == 1) {
////                                adUnit = ADConstants.facebook_video_feed_native2;
////                            } else {
////                                adUnit = ADConstants.facebook_video_feed_native3;
////                            }
////                            mFeedNativeAD.loadAD(mContext, ADManager.AD_Facebook, adUnit, new NativeAD.ADListener() {
////                                @Override
////                                public void onLoadedSuccess(com.facebook.ads.NativeAd ad, String adId) {
////                                    if (null != ad) {
////                                        mNativeAdlist.add(ad);
////                                        if (mNativeAdlist.size()==1){//只要有了一个广告成功就通知上层展示
////                                            callbackAD();
////                                        }
////                                    }
////                                }
////
////                                @Override
////                                public void onLoadedFailed(String msg, String adId) {
////                                    mFailed++;
////                                    if (mFailed == mNum) {
////                                        StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FAILED + " all ");
////                                    }
////                                }
////
////                                @Override
////                                public void onAdDisplayed() {
////
////                                }
////                            });
////                        }
////                    }
////                    break;
////            }
////        }
////    };
////
////
////    protected class MyTimerTask extends TimerTask {
////        @Override
////        public void run() {
////            Message msg = mHandler.obtainMessage();
////            msg.what = CODE_REFRESH;
////            mHandler.sendMessage(msg);
////        }
////    }
//
//public interface ADNumListener {
//    void onLoadedSuccess(List<NativeAd> list);
//}
//
//}
