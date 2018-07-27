package com.wenjoyai.tubeplayer.ad;

/**
 * Created by LiJiaZhi on 2017/9/20.
 *
 * 广告常量
 */

public class ADConstants {
    /***************  facebook      *********************/
    //feed流
    private static final String facebook_video_feed_native1 ="2016444078685446_2016444465352074";
    private static final String facebook_video_feed_native2 ="2016444078685446_2016447112018476";
    private static final String facebook_video_feed_native3 ="2016444078685446_2016451138684740";
    public static  String[] facebook_feed_natives ={facebook_video_feed_native1,facebook_video_feed_native2,facebook_video_feed_native3};

    public static String facebook_video_pause ="2016444078685446_2016458755350645";
    public static final String facebook_first_open_interstitial = "2016444078685446_2018365338493320";
    public static String facebook_back_or_drawer_interstitial = "2016444078685446_2024980741165113";

    /***************  google      *********************/
    public static final String GOOGLE_APP_ID = "ca-app-pub-8002733251692869~7147743342";
    //首页第一次进入
    public static final String google_first_open_interstitial = "ca-app-pub-8002733251692869/7837158427";
    public static String google_back_or_drawer_interstitial = "ca-app-pub-8002733251692869/8172720833";

    /***************  baidu      *********************/
    //首页第一次进入
    public static final String baidu_first_open_interstitial = "156582";
    public static String baidu_back_or_drawer_interstitial = "156773";

    //feed流
    private static final String baidu_video_feed_native1 ="156583";
    private static final String baidu_video_feed_native2 ="156584";
    private static final String baidu_video_feed_native3 ="156585";
    public static String[] baidu_natives={baidu_video_feed_native1,baidu_video_feed_native2,baidu_video_feed_native3};

    /***************  yeahmobi      *********************/
    public static final String yeahmobi_open_interstitial = "24190982";
    public static final String yeahmobi_back_or_drawer_interstitial = "68224334";

    private static final String yeahmobi_feed_native1 = "76734943";
    private static final String yeahmobi_feed_native2 = "28507932";
    private static final String yeahmobi_feed_native3 = "53222291";
    public static String[] yeahmobi_natives={yeahmobi_feed_native1,yeahmobi_feed_native2,yeahmobi_feed_native3};
}
