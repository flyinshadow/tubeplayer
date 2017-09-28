package com.wenjoyai.tubeplayer.firebase;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.wenjoyai.tubeplayer.util.LogUtil;

/**
 * Created by LiJiaZhi on 2017/9/23.
 *
 * 统计管理类
 */

public class StatisticsManager {

    public static final String TAG = "TubeStatisticsManager";

    //广告事件
    public static final String EVENT_AD="ad";
    public static final String TYPE_AD="rotate_ad";
    public static final String ITEM_AD_LIBRARY_NAME ="library_roate_offer_wall";
    public static final String ITEM_AD_VIDEO_NAME ="video_rotate_offer_wall";
    public static final String ITEM_AD_GOOGLE_BACK ="google_back_ad";

    //视频播放
    public static final String CONTENT_TYPE_VIDEO_PLAY = "video_play";
    public static final String ITEM_ID_VIDEO_SUCCESS = "success";
    public static final String ITEM_ID_VIDEO_PAUSE = "pause";
    public static final String ITEM_ID_VIDEO_POPUP = "popup";
    public static final String ITEM_ID_VIDEO_LOCK = "lock";
    public static final String ITEM_ID_VIDEO_SUBTITLE_SELECT = "select_subtitle";
    public static final String ITEM_ID_VIDEO_SUBTITLE_DOWNLOAD = "download_subtitle";
    public static final String ITEM_ID_VIDEO_RATIO = "ratio";
    public static final String ITEM_ID_VIDEO_RATIO_BEST_FIT = "ratio_best_fit";
    public static final String ITEM_ID_VIDEO_RATIO_FIT_SCREEN = "ratio_fit_screen";
    public static final String ITEM_ID_VIDEO_RATIO_FILL_SCREEN = "ratio_fill_screen";
    public static final String ITEM_ID_VIDEO_RATIO_16_9 = "ratio_16_9";
    public static final String ITEM_ID_VIDEO_RATIO_4_3 = "ratio_4_3";
    public static final String ITEM_ID_VIDEO_RATIO_CENTER = "ratio_center";
    public static final String ITEM_ID_VIDEO_EXTEND = "extend";
    public static final String ITEM_ID_VIDEO_EXTEND_SLEEP = "extend_sleep";
    public static final String ITEM_ID_VIDEO_EXTEND_JUMP_TO = "extend_jump_to";
    public static final String ITEM_ID_VIDEO_EXTEND_PLAY_AS_AUDIO = "extend_play_as_audio";
    public static final String ITEM_ID_VIDEO_EXTEND_AUDIO_DELAY = "extend_audio_delay";
    public static final String ITEM_ID_VIDEO_EXTEND_SPU_DELAY = "extend_spu_delay";
    public static final String ITEM_ID_VIDEO_EXTEND_CHAPTER_TITLE = "extend_chapter_title";
    public static final String ITEM_ID_VIDEO_EXTEND_PLAYBACK_SPEED = "extend_playback_speed";
    public static final String ITEM_ID_VIDEO_EXTEND_EQUALIZER = "extend_equalizer";
    public static final String ITEM_ID_VIDEO_EXTEND_SAVE_PLAYLIST = "extend_save_playlist";
    public static final String ITEM_ID_VIDEO_EXTEND_POPUP_VIDEO = "extend_popup_video";
    public static final String ITEM_ID_VIDEO_EXTEND_REPEAT = "extend_repeat";
    public static final String ITEM_ID_VIDEO_EXTEND_SHUFFLE = "extend_shuffle";

    public static final String CONTENT_TYPE_VIDEO_EXT = "video_ext";
    public static final String CONTENT_TYPE_VIDEO_LENGTH = "video_length";      // 视频时长

    public static final String VIDEO_LENGTH_0_5 = "0-5";
    public static final String VIDEO_LENGTH_5_10 = "5-10";
    public static final String VIDEO_LENGTH_10_30 = "10-30";
    public static final String VIDEO_LENGTH_30_60 = "30-60";
    public static final String VIDEO_LENGTH_60_PLUS = "60+";

    // 播放失败
    public static final String EVENT_PLAY_FAILED = "play_failed";
    public static final String KEY_FAILED_ERROR = "error";

    //音频播放
    public static final String CONTENT_TYPE_AUDIO_PLAY="audio_play";
    public static final String CONTENT_TYPE_AUDIO_EXT="audio_ext";

    //主题
    public static final String CONTENT_TYPE_THEME="theme";

    //侧边栏
    public static final String CONTENT_TYPE_DRAWLAYOUT="drawlayout";
    public static final String ITEM_ID_VIDEO="video";
    public static final String ITEM_ID_AUDIO="audio";
    public static final String ITEM_ID_DIRECT="directories";
    public static final String ITEM_ID_LOCALNET="localnet";
    public static final String ITEM_ID_STREAM="stream";
    public static final String ITEM_ID_SETTING="setting";
    public static final String ITEM_ID_SHARE="share";
    public static final String ITEM_ID_NIGHTMODE="nightmode";

    //主页面
    public static final String CONTENT_TYPE_HOMETAB="hometab";
    public static final String ITEM_ID_SEARCH="search";
    public static final String ITEM_ID_LAST_PLAYLIST="play_last_playlist";
    public static final String ITEM_ID_SORTBY="sortby";
    public static final String ITEM_ID_REFRESH="refresh";
    public static final String ITEM_ID_EQUALIZER="equalizer";
    public static final String ITEM_ID_PLAY_ALL_VIDEO="play_all_video";

    public static final String CONTENT_TYPE_VIEWER="viewer";//list 列表    grid 网格      bigpic 大图
    public static final String ITEM_ID_VIEWER_LIST="list";
    public static final String ITEM_ID_VIEWER_GRID="grid";
    public static final String ITEM_ID_VIEWER_BIGPIC="bigpic";

    public static final String CONTENT_TYPE_SORTBY="sortby";//date name length
    public static final String ITEM_ID_SORTBY_DATE="date";
    public static final String ITEM_ID_SORTBY_NAME="name";
    public static final String ITEM_ID_SORTBY_LENGTH="length";

    public static final String CONTENT_TYPE_RATE="rate";//dislike cancel fivestar
    public static final String ITEM_ID_RATE_DISLIKE="dislike";
    public static final String ITEM_ID_RATE_CANCEL="cancel";
    public static final String ITEM_ID_RATE_STAR="fivestar";


    /**
     * 上报广告
     * @param context
     * @param type
     * @param itemName
     */
    public static void submitAd(Context context, String type, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemName);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_AD, bundle);
    }

    /**
     * 上报视频播放
     * @param context
     * @param itemId   文件类型
     */
    public static void submitVideoPlay(Context context, String itemId) {
        LogUtil.d(TAG, "submitVideoPlay, " + itemId);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_TYPE_VIDEO_PLAY);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /**
     * @param context
     * @param ext
     */
    public static void submitVideoExt(Context context, String ext) {
        LogUtil.d(TAG, "submitVideoExt, " + ext);
        submitSelectContent(context, CONTENT_TYPE_VIDEO_EXT, ext);
    }

    public static void submitVideoLength(Context context, long lengthMs) {
        LogUtil.d(TAG, "submitVideoLength, " + lengthMs + " (" + getVideoLengthType(lengthMs) + ")");
        submitSelectContent(context, CONTENT_TYPE_VIDEO_LENGTH, getVideoLengthType(lengthMs));
    }

    public static String getVideoLengthType(long lengthMs) {
        long length = lengthMs / 1000 / 60;
        if (0 <= length && length <= 5) {
            return VIDEO_LENGTH_0_5;
        } else if (5 < length && length <= 10) {
            return VIDEO_LENGTH_5_10;
        } else if (10 < length && length <= 30) {
            return VIDEO_LENGTH_10_30;
        } else if (30 < length && length <= 60) {
            return VIDEO_LENGTH_30_60;
        } else if (60 < length) {
            return VIDEO_LENGTH_60_PLUS;
        }
        return "";
    }

    /**
     * 播放失败
     * @param context
     * @param error
     */
    public static void submitPlayFailed(Context context, String error) {
        LogUtil.d(TAG, "submitPlayFailed, " + error);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_FAILED_ERROR, error);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_PLAY_FAILED, bundle);
    }

    /**
     * @param context
     * @param itemId
     */
    public static void submitAudioPlay(Context context, String itemId) {
        LogUtil.d(TAG, "submitAudioPlay, " + itemId);
        submitSelectContent(context, CONTENT_TYPE_AUDIO_PLAY, itemId);
    }

    public static void submitAudioExt(Context context, String ext) {
        LogUtil.d(TAG, "submitAudioExt, " + ext);
        submitSelectContent(context, CONTENT_TYPE_AUDIO_EXT, ext);
    }

    /**
     * 主题设置
     * @param context
     */
    public static void submitTheme(Context context, int index) {
        LogUtil.d(TAG, "submitTheme, " + index);
        submitSelectContent(context, CONTENT_TYPE_THEME, "theme_" + index);
    }

    /**
     * 侧边栏
     * @param context
     */
    public static void submitDrawlayout(Context context, String itemId) {
        LogUtil.d(TAG, "submitDrawlayout, " + itemId);
        submitSelectContent(context, CONTENT_TYPE_DRAWLAYOUT, itemId);
    }

    /**
     * 主页面
     * @param context
     */
    public static void submitHomeTab(Context context, String itemId) {
        LogUtil.d(TAG, "submitHomeTab, " + itemId);
        submitSelectContent(context, CONTENT_TYPE_HOMETAB, itemId);
    }

    public static void submitViewer(Context context, String itemId) {
        LogUtil.d(TAG, "submitViewer, " + itemId);
        submitSelectContent(context, CONTENT_TYPE_VIEWER, itemId);
    }

    public static void submitSortby(Context context, String itemId) {
        LogUtil.d(TAG, "submitSortby, " + itemId);
        submitSelectContent(context, CONTENT_TYPE_SORTBY, itemId);
    }

    public static void submitRate(Context context, String itemId) {
        LogUtil.d(TAG, "submitRate, " + itemId);
        submitSelectContent(context, CONTENT_TYPE_RATE, itemId);
    }

    private static void submitSelectContent(Context context, String contentType, String itemId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
