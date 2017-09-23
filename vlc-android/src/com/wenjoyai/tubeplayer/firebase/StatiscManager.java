package com.wenjoyai.tubeplayer.firebase;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by LiJiaZhi on 2017/9/23.
 *
 * 统计管理类
 */

public class StatiscManager {
    //广告事件
    public static final String EVENT_AD="ad";
    public static final String TYPE_AD="rotate_ad";
    public static final String ITEM_AD_LIBRARY_NAME ="library_roate_offer_wall";
    public static final String ITEM_AD_VIDEO_NAME ="video_rotate_offer_wall";


    //视频播放
    public static final String EVENT_VIDEO_PLAY="videoplay";
    public static final String TYPE_VIDEO_SUCCESS="success";//格式
    public static final String TYPE_VIDEO_FAILED="failed";//格式
    public static final String TYPE_VIDEO_PAUSE="pause";
    public static final String TYPE_VIDEO_POPUP="popup";
    public static final String TYPE_VIDEO_LOCK="lock";
    public static final String TYPE_VIDEO_SELECT="select";//字幕
    public static final String TYPE_VIDEO_DOWNLOAD="download";
    public static final String TYPE_VIDEO_EXTEND="extend";


    //音频播放
    public static final String EVENT_AUDIO_PLAY="audiooplay";
    public static final String TYPE_AUDIO_PLAY="play";////格式


    //主题
    public static final String EVENT_THEME="theme";
    public static final String TYPE_THEME_SET="set";//格式   样式序号（1，2， 3）

    //侧边栏
    public static final String EVENT_DRAWLAYOUT="drawlayout";
    public static final String TYPE_VIDEO="video";
    public static final String TYPE_AUDIO="audio";
    public static final String TYPE_DIRECT="direct";
    public static final String TYPE_LOCALNET="localnet";
    public static final String TYPE_STREAM="stream";
    public static final String TYPE_SETTING="setting";
    public static final String TYPE_SHARE="share";
    public static final String TYPE_NIGHTMODE="nightmode";

    //主页面
    public static final String EVENT_HOME_TAB="hometab";
    public static final String TYPE_VIEWER="viewer";//list 列表    album 网格      big  picture 大图
    public static final String TYPE_SEARCH="search";
    public static final String TYPE_PLAY_LASET="play_last_playlist";
    public static final String TYPE_REFRESH="refresh";
    public static final String TYPE_EQUALIZER="equalizer";
    public static final String TYPE_SORTBY="sortby";//date name length
    public static final String TYPE_PLAY_ALL_VIDEO="play_all_video";
    public static final String TYPE_RATE="rate";//dislike cancel fivestar


    /**
     * 上报广告
     * @param context
     * @param type
     * @param itemName
     */
    public static void submitAd(Context context,String type, String itemName){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemName);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_AD, bundle);

    }

    /**
     * 上报视频播放
     * @param context
     * @param type
     * @param itemId   文件类型
     * @param itemName  文件时长  0~5   5~10  10~30  30~60  60+
     */
    public static void submitVideoPlay(Context context,String type, String itemId, String itemName){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_VIDEO_PLAY, bundle);
    }

    /**
     * 上报音频播放
     * @param context
     * @param type
     * @param itemName
     */
    public static void submitAudioPlay(Context context,String type, String itemName){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemName);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_AUDIO_PLAY, bundle);
    }

    /**
     * 主题设置
     * @param context
     * @param itemName
     */
    public static void submitTheme(Context context,String itemName){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemName);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, TYPE_THEME_SET);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_THEME, bundle);
    }

    /**
     * 侧边栏
     * @param context
     */
    public static void submitDrawlayout(Context context,String type){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_DRAWLAYOUT, bundle);
    }

    /**
     * 主页面
     * @param context
     */
    public static void submitHomeTab(Context context,String type, String itemName){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemName);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_HOME_TAB, bundle);
    }
}
