package com.wenjoyai.tubeplayer.rate;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.util.LogUtil;

public class RateManager {
    private static final String TAG = "RateManager";

    private static SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());

    private final static int MIN_WEIGHT = 3;

    public enum RateWeight {
        PLAY_TUBE_MODE("key_weight_play_tube_mode"),
        PLAY_POPUP("key_weight_play_popup"),
        PLAY_BACKGROUND("key_weight_play_background"),
        PLAY_LOCK_SCREEN("key_weight_play_lock_screen"),
        PLAY_ADVANCE_MENU("key_weight_play_advance_menu"),
        PLAY_TIME("key_weight_play_time"),
        NAV_AUDIO("key_weight_nav_audio"),
        NAV_DIRECTORIES("key_weight_nav_directories"),
        NAV_THEME("key_weight_nav_theme");

        private String key;
        private int weight;

        public String getKey() {
            return key;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            pref.edit().putInt(key, weight).apply();
        }

        RateWeight(String key) {
            this.key = key;
            this.weight = pref.getInt(key, 0);
        }
    }

    // 竖屏模式播放
    private static final String KEY_WEIGHT_TUBE_MODE_PLAY = "key_weight_tube_mode_play";
    // 浮窗播放
    private static final String KEY_WEIGHT_POPUP_PLAY = "key_weight_popup_play";
    // 后台播放
    private static final String KEY_WEIGHT_BACKGROUND_PLAY = "key_weight_background_play";
    // 播放锁定
    private static final String KEY_WEIGHT_LOCK_PLAY = "key_weight_lock_play";
    // 高级菜单
    private static final String KEY_WEIGHT_ADVANCE_MENU = "key_weight_advance_menu";
    // 播放时长达标
    private static final String KEY_WEIGHT_PLAY_TIME = "key_weight_play_time";
    // 导航侧边栏Audio
    private static final String KEY_WEIGHT_NAV_AUDIO = "key_weight_nav_audio";
    // 导航侧边栏Directories
    private static final String KEY_WEIGHT_NAV_DIRECTORIES = "key_weight_nav_directories";
    // 导航侧边栏Theme
    private static final String KEY_WEIGHT_NAV_THEME = "key_weight_nav_theme";

    public static int getWeightSum() {
        int sum = 0;
        for (RateWeight rateWeight : RateWeight.values()) {
            LogUtil.d(TAG, "getWeightSum: " + rateWeight.getKey() + " : " + rateWeight.getWeight());
            sum += rateWeight.getWeight();
        }
        return sum;
    }

    public static boolean checkRateWeight() {
        return getWeightSum() >= MIN_WEIGHT;
    }

    public static void setRateWeight(RateWeight rateWeight, int weight) {
        rateWeight.setWeight(weight);
    }

}
