package com.wenjoyai.tubeplayer.ad;

import android.content.Context;

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


    //进入前台 超过2分钟才会展示 open广告
    public static boolean isShowOpenAD = true;

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



    private List<com.facebook.ads.NativeAd> mNativeAdlist=new ArrayList<>();
    private long mStartTime = 0;
    private int done = 0;

    /**
     * 加载num个feed流广告
     * @param context
     */
    public  void loadNumNativeAD(Context context, final int num, final ADNumListener listener){
        //大于15秒才会再次更新广告
        if ((System.currentTimeMillis()-mStartTime)/1000<15){
            return;
        }
        mStartTime=System.currentTimeMillis();
        mNativeAdlist.clear();
        done = 0;
        for (int i =0;i<num;i++) {
            NativeAD mFeedNativeAD = new NativeAD();
            String adUnit="";
            if (i%3==0){
                adUnit = ADConstants.facebook_video_feed_native;
            } else if (i%3==1){
                adUnit = ADConstants.facebook_video_feed_native1;
            } else {
                adUnit = ADConstants.facebook_video_feed_native2;
            }
            mFeedNativeAD.loadAD(context, ADManager.AD_Facebook, adUnit, new NativeAD.ADListener() {
                @Override
                public void onLoadedSuccess(com.facebook.ads.NativeAd ad) {
                    done++;
                    if (null!= ad) {
                        mNativeAdlist.add(ad);
                    }
                    if (done==num&&null != listener){
                        listener.onLoadedSuccess(mNativeAdlist);
                    }
                }

                @Override
                public void onLoadedFailed(String msg) {
                    done++;
                    if (done==num&&null != listener){
                        listener.onLoadedSuccess(mNativeAdlist);
                    }
                }

                @Override
                public void onAdClick() {

                }
            });
        }
    }


    public interface ADNumListener {
        void onLoadedSuccess(List<NativeAd> list);
    }
}
