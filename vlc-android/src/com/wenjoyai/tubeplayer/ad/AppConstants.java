package com.wenjoyai.tubeplayer.ad;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wenjoyai.tubeplayer.VLCApplication;

/**
 * @author：LiJiaZhi on 2018/3/7
 * @des：ToDo
 * @org mtime.com
 */
public class AppConstants {

    //app打开次数
    private static final String OPEN_COUNT = "open_count";

    public static long getOpenCount(){
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
        return s.getLong(OPEN_COUNT, 0);
    }
    public static void setOpenCount(long openCount){
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
        s.edit().putLong(OPEN_COUNT, openCount).apply();
    }

}
