package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    }

    //缓存广告数组
    private List<NativeAd> mNativeAdlist = new ArrayList<>();
    private int mNum = 3;
    //请求广告时间间隔默认10分钟
    public static long REQUEST_FEED_NTIVE_INTERVAL = 10 * 60;
    protected Timer UPDATE_PROGRESS_TIMER;
    protected MyTimerTask mProgressTimerTask;
    private Context mContext;
    //失败个数
    private int mFailed = 0;

    /**
     * 开始加载广告
     */
    public void startLoadAD(Context context){
        mContext = context;
        startProgressTimer();
    }

    /**
     * 获取广告
     * @return
     */
    public List<com.facebook.ads.NativeAd> getNativeAdlist(){
        List<com.facebook.ads.NativeAd> tempList = new ArrayList<>();
        tempList.addAll(mNativeAdlist);
        return tempList;
    }

    protected void startProgressTimer() {
        try {
            UPDATE_PROGRESS_TIMER = new Timer();
            mProgressTimerTask = new MyTimerTask();
            UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, REQUEST_FEED_NTIVE_INTERVAL*1000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cancelProgressTimer() {
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
                    Log.e("NativeAD", "bigggggggggggggggggggg ");
                    mFailed = 0;
                    mNativeAdlist.clear();
                    for (int i = 0; i < mNum; i++) {
                        final NativeAD mFeedNativeAD = new NativeAD();
                        String adUnit = "";
                        if (i % 3 == 0) {
                            adUnit = ADConstants.facebook_video_feed_native;
                        } else if (i % 3 == 1) {
                            adUnit = ADConstants.facebook_video_feed_native1;
                        } else {
                            adUnit = ADConstants.facebook_video_feed_native2;
                        }
                        mFeedNativeAD.loadAD(mContext, ADManager.AD_Facebook, adUnit, new NativeAD.ADListener() {
                            @Override
                            public void onLoadedSuccess(com.facebook.ads.NativeAd ad,String adId) {
                                if (null != ad) {
                                    mNativeAdlist.add(ad);
                                }
                            }

                            @Override
                            public void onLoadedFailed(String msg,String adId) {
                                mFailed++;
                                if (mFailed ==mNum) {
                                    StatisticsManager.submitAd(mContext, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_FEED_NATIVE_FACEBOOK_FAILED + " all ");
                                }
                            }

                            @Override
                            public void onAdClick() {

                            }
                        });
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
