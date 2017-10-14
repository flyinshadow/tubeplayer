package com.wenjoyai.tubeplayer.firebase;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.wenjoyai.tubeplayer.util.LogUtil;

import org.videolan.medialibrary.media.MediaWrapper;

/**
 * Created by LiJiaZhi on 2017/9/23.
 * <p>
 * 统计管理类
 */

public class StatisticsManager {

    public static final String TAG = "TubeStatisticsManager";

    //广告事件
    public static final String EVENT_AD="ad";
    public static final String TYPE_AD="rotate_ad";
    public static final String ITEM_AD_LIBRARY_NAME ="mobvista_library_roate_offer_wall";
    public static final String ITEM_AD_VIDEO_NAME ="mobvista_video_rotate_offer_wall";

    public static final String ITEM_AD_GOOGLE_BACK ="google_back_ad";
    public static final String ITEM_AD_GOOGLE_VIEWER ="google_viewer_ad";
    public static final String ITEM_AD_GOOGLE_FIRST_OPEN ="google_first_open_ad";
    public static final String ITEM_AD_GOOGLE_PAUSE_NATIVE ="google_pause_native_ad";
    public static final String ITEM_AD_GOOGLE_VIDEO_BANNER ="google_video_banner";

    public static final String ITEM_AD_FEED_NATIVE_1 ="feed_native_1";
    public static final String ITEM_AD_FEED_NATIVE_2 ="feed_native_2";
    public static final String ITEM_AD_FEED_NATIVE_3 ="feed_native_3";


    //视频播放
    public static final String EVENT_VIDEO_PLAY = "videoplay";
    public static final String TYPE_VIDEO_SUCCESS = "success";//格式
    public static final String TYPE_VIDEO_FAILED = "failed";//格式
    public static final String TYPE_VIDEO_PAUSE = "pause";
    public static final String TYPE_VIDEO_POPUP = "popup";
    public static final String TYPE_VIDEO_LOCK = "lock";
    public static final String TYPE_VIDEO_SELECT = "select";//字幕
    public static final String TYPE_VIDEO_DOWNLOAD = "download";
    public static final String TYPE_VIDEO_EXTEND = "extend";
    public static final String TYPE_VIDEO_EXTEND_SLEEP = "extend_sleep";
    public static final String TYPE_VIDEO_EXTEND_PLAYBACK_SPEED = "extend_playback_speed";
    public static final String TYPE_VIDEO_EXTEND_CHAPTER_TITLE = "extend_chapter_title";
    public static final String TYPE_VIDEO_EXTEND_AUDIO_DELAY = "extend_audio_delay";
    public static final String TYPE_VIDEO_EXTEND_SPU_DELAY = "extend_spu_delay";
    public static final String TYPE_VIDEO_EXTEND_JUMP_TO = "extend_jump_to";
    public static final String TYPE_VIDEO_EXTEND_PLAY_AS_AUDIO = "extend_play_as_audio";
    public static final String TYPE_VIDEO_EXTEND_POPUP_VIDEO = "extend_popup_video";
    public static final String TYPE_VIDEO_EXTEND_EQUALIZER = "extend_equalizer";
    public static final String TYPE_VIDEO_EXTEND_SAVE_PLAYLIST = "extend_save_playlist";
    public static final String TYPE_VIDEO_EXTEND_REPEAT = "extend_extend_repeat";
    public static final String TYPE_VIDEO_EXTEND_SHUFFLE = "extend_shuffle";
    public static final String TYPE_VIDEO_RATIO = "ratio";

    public static final String ITEM_VIDEO_LENGTH_0_1 = "0-1";
    public static final String ITEM_VIDEO_LENGTH_1_3 = "1-3";
    public static final String ITEM_VIDEO_LENGTH_3_5 = "3-5";
    public static final String ITEM_VIDEO_LENGTH_5_10 = "5-10";
    public static final String ITEM_VIDEO_LENGTH_10_30 = "10-30";
    public static final String ITEM_VIDEO_LENGTH_30_60 = "30-60";
    public static final String ITEM_VIDEO_LENGTH_60_120 = "60-120";
    public static final String ITEM_VIDEO_LENGTH_120_PLUS = "120+";

    public static final String ITEM_VIDEO_SIZE_240P_MINUS = "240P-";
    public static final String ITEM_VIDEO_SIZE_240P = "240P";   // 352x240
    public static final String ITEM_VIDEO_SIZE_360P = "360P";   // 480x360
    public static final String ITEM_VIDEO_SIZE_480P = "480P";   // 858x480
    public static final String ITEM_VIDEO_SIZE_720P = "720P";   // 1280x720
    public static final String ITEM_VIDEO_SIZE_1080P = "1080P"; // 1920x1080
    public static final String ITEM_VIDEO_SIZE_2K = "2K";       // 2560x1440
    public static final String ITEM_VIDEO_SIZE_4K = "4K";       // 3860x2160
    public static final String ITEM_VIDEO_SIZE_4K_PLUS = "4K+";

    public static final int VIDEO_SIZE_240P = 352 * 240;
    public static final int VIDEO_SIZE_360P = 480 * 360;
    public static final int VIDEO_SIZE_480P = 858 * 480;
    public static final int VIDEO_SIZE_720P = 1280 * 720;
    public static final int VIDEO_SIZE_1080P = 1920 * 1080;
    public static final int VIDEO_SIZE_2K = 2560 * 1440;
    public static final int VIDEO_SIZE_4K = 3860 * 2160;


    public static final String ITEM_VIDEO_VERTICAL = "V";
    public static final String ITEM_VIDEO_HORIZON = "H";

    public static final String ITEM_VIDEO_RATIO_BEST_FIT = "best_fit";
    public static final String ITEM_VIDEO_RATIO_FIT_SCREEN = "fit_screen";
    public static final String ITEM_VIDEO_RATIO_FILL_SCREEN = "fill_screen";
    public static final String ITEM_VIDEO_RATIO_16_9 = "16_9";
    public static final String ITEM_VIDEO_RATIO_4_3 = "4_3";
    public static final String ITEM_VIDEO_RATIO_CENTER = "center";

    //音频播放
    public static final String EVENT_AUDIO_PLAY = "audioplay";
    public static final String TYPE_AUDIO_PLAY = "play";////格式

    public static final String EVENT_VIDEO_PLAY_SUCCESS = "video_play_success";

    //主题
    public static final String EVENT_THEME = "theme";
    public static final String TYPE_THEME_SET = "set";//格式   样式序号（1，2， 3）

    //侧边栏
    public static final String EVENT_DRAWLAYOUT = "drawlayout";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_DIRECT = "direct";
    public static final String TYPE_LOCALNET = "localnet";
    public static final String TYPE_STREAM = "stream";
    public static final String TYPE_SETTING = "setting";
    public static final String TYPE_SHARE = "share";
    public static final String TYPE_NIGHTMODE = "nightmode";

    //主页面
    public static final String EVENT_HOME_TAB = "hometab";
    public static final String TYPE_VIEWER = "viewer";//list 列表    grid 网格      bigpic 大图
    public static final String TYPE_VIEWER_LIST = "viewer_list";
    public static final String TYPE_VIEWER_GRID = "viewer_grid";
    public static final String TYPE_VIEWER_BIGPIC = "viewer_bigpic";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_LAST_PLAYLIST = "play_last_playlist";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_EQUALIZER = "equalizer";
    public static final String TYPE_SORTBY = "sortby";//date name length
    public static final String TYPE_SORTBY_NAME = "sortby_name";
    public static final String TYPE_SORTBY_LENGTH = "sortby_length";
    public static final String TYPE_SORTBY_DATE = "sortby_date";
    public static final String TYPE_PLAY_ALL_VIDEO = "play_all_video";
    public static final String TYPE_RATE = "rate";//dislike cancel fivestar

    public static final String ITEM_VIEWER_LIST = "list";
    public static final String ITEM_VIEWER_GRID = "grid";
    public static final String ITEM_VIEWER_BIGPIC = "bigpic";

    public static final String ITEM_SORTBY_DATE = "date";
    public static final String ITEM_SORTBY_NAME = "name";
    public static final String ITEM_SORTBY_LENGTH = "length";

    public static final String EVENT_RATE = "rate";
    public static final String ITEM_RATE_DISLIKE = "dislike";
    public static final String ITEM_RATE_CANCEL = "cancel";
    public static final String ITEM_RATE_STAR = "fivestar";

    public static final String EVENT_PLAY_ERROR = "play_error";

    /**
     * 上报广告
     *
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
     *
     * @param context
     * @param type
     * @param itemId   文件类型
     * @param itemName 文件时长
     */
    public static void submitVideoPlay(Context context, String type, String itemId, String itemName) {
        LogUtil.d(TAG, "submitVideoPlay, " + type + " " + itemId + " " + itemName);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_VIDEO_PLAY, bundle);
    }

    public static void submitVideoPlaySuccess(Context context, String itemId, String itemName) {
        LogUtil.d(TAG, "submitVideoPlaySuccess, " + itemId + " " + itemName);
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(itemId)) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        }
        if (!TextUtils.isEmpty(itemName)) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        }
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_VIDEO_PLAY_SUCCESS, bundle);
    }

    public static void submitPlayError(Context context, String itemId) {
        LogUtil.d(TAG, "submitPlayError, " + itemId);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_PLAY_ERROR, bundle);
    }

    public static String getVideoLengthType(long lengthMs) {
        long length = lengthMs / 1000 / 60;
        if (0 <= length && length <= 1) {
            return ITEM_VIDEO_LENGTH_0_1;
        } else if (1 < length && length <= 3) {
            return ITEM_VIDEO_LENGTH_1_3;
        } else if (3 < length && length <= 5) {
            return ITEM_VIDEO_LENGTH_3_5;
        } else if (5 < length && length <= 10) {
            return ITEM_VIDEO_LENGTH_5_10;
        } else if (10 < length && length <= 30) {
            return ITEM_VIDEO_LENGTH_10_30;
        } else if (30 < length && length <= 60) {
            return ITEM_VIDEO_LENGTH_30_60;
        } else if (60 < length && length <= 120) {
            return ITEM_VIDEO_LENGTH_60_120;
        } else if (length > 120) {
            return ITEM_VIDEO_LENGTH_120_PLUS;
        }
        return "";
    }

    public static String getVideoSizeType(int width, int height) {
        int size = width * height;
        if (size > 0 && size < VIDEO_SIZE_240P) {
            return ITEM_VIDEO_SIZE_240P_MINUS;
        } else if (size >= VIDEO_SIZE_240P && size < VIDEO_SIZE_360P) {
            return ITEM_VIDEO_SIZE_240P;
        } else if (size >= VIDEO_SIZE_360P && size < VIDEO_SIZE_480P) {
            return ITEM_VIDEO_SIZE_360P;
        } else if (size >= VIDEO_SIZE_480P && size < VIDEO_SIZE_720P) {
            return ITEM_VIDEO_SIZE_480P;
        } else if (size >= VIDEO_SIZE_720P && size < VIDEO_SIZE_1080P) {
            return ITEM_VIDEO_SIZE_720P;
        } else if (size >= VIDEO_SIZE_1080P && size < VIDEO_SIZE_2K) {
            return ITEM_VIDEO_SIZE_1080P;
        } else if (size >= VIDEO_SIZE_2K && size < VIDEO_SIZE_4K) {
            return ITEM_VIDEO_SIZE_2K;
        } else if (size >= VIDEO_SIZE_4K) {
            return ITEM_VIDEO_SIZE_4K;
        }
        return "";
    }

    public static String getVideoVHType(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            if (videoWidth < videoHeight) {
                return ITEM_VIDEO_VERTICAL;
            } else {
                return ITEM_VIDEO_HORIZON;
            }
        }
        return "";
    }

    public static String getVideoInfoType(MediaWrapper media, int videoWidth, int videoHeight) {
        String info = "";
        if (media != null) {
            info = getVideoLengthType(media.getLength()) + "_" + getVideoVHType(videoWidth, videoHeight) + "_" +
                    getVideoSizeType(media.getWidth(), media.getHeight());
        }
        return info;
    }

    /**
     * 上报音频播放
     *
     * @param context
     * @param type
     * @param itemName
     */
    public static void submitAudioPlay(Context context, String type, String itemName) {
        LogUtil.d(TAG, "submitAudioPlay, " + type + " " + itemName);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemName);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_AUDIO_PLAY, bundle);
    }

    /**
     * 主题设置
     *
     * @param context
     * @param itemId
     */
    public static void submitTheme(Context context, String itemId) {
        LogUtil.d(TAG, "submitTheme, " + itemId);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_THEME, bundle);
    }

    /**
     * 侧边栏
     *
     * @param context
     */
    public static void submitDrawlayout(Context context, String type) {
        LogUtil.d(TAG, "submitDrawlayout, " + type);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_DRAWLAYOUT, bundle);
    }

    /**
     * 主页面
     *
     * @param context
     */
    public static void submitHomeTab(Context context, String type, String itemName) {
        LogUtil.d(TAG, "submitHomeTab, " + type + " " + itemName);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemName);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_HOME_TAB, bundle);
    }

    /**
     * @param context
     * @param itemId
     */
    public static void submitRate(Context context, String itemId) {
        LogUtil.d(TAG, "submitRate, " + itemId);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_RATE, bundle);
    }

    /**
     * @param context
     * @param contentType
     * @param itemId
     */
    public static void submitSelectContent(Context context, String contentType, String itemId) {
        LogUtil.d(TAG, "submitSelectContent, " + contentType + " " + itemId);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
