package com.wenjoyai.tubeplayer.ad;

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
    public static long sPlatForm = AD_Google;

    //广告级别
    public static final long Level_Little = 1;//只有feed流和pause的native
    public static final long Level_Normal = 2;//加上插屏
    public static final long Level_Big = 3;//加上banner
    public static long sLevel = Level_Normal;




    public static boolean isShowGoogleAD = false;//是否显示google广告
    public static boolean isShowGoogleVideoBanner = false;//是否显示视频列表页的banner
    public static boolean isShowMobvista = false;//是否显示旋转动画的mobvista广告

}
