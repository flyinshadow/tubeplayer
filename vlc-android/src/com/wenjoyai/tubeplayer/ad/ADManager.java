package com.wenjoyai.tubeplayer.ad;

/**
 * Created by LiJiaZhi on 2017/9/23.
 * 广告管理类
 */

public class ADManager {
    //广告类型   1：MobVista   2：google   3:facebook  4:百度
    public static final long AD_MobVista = 1;
    public static final long AD_Google = 2;
    public static final long AD_Facebook = 3;
    public static final long AD_Baidu = 4;
    public static long sType = AD_Google;
    public static boolean isShowGoogleAD = false;//是否显示google广告

}
