package com.wenjoyai.tubeplayer.ad;

/**
 * Created by LiJiaZhi on 2017/9/20.
 *
 * 广告常量
 */

public class ADConstants {
    /***************  facebook      *********************/
    //feed流
    private static final String facebook_video_feed_native1 ="129279111221574_162874264528725";
    private static final String facebook_video_feed_native2 ="129279111221574_162880634528088";
    private static final String facebook_video_feed_native3 ="129279111221574_162880707861414";
    public static  String[] facebook_feed_natives ={facebook_video_feed_native1,facebook_video_feed_native2,facebook_video_feed_native3};

    public static String facebook_video_pause ="129279111221574_162880861194732";
    public static String facebook_back_or_drawer_interstitial = "129279111221574_175903883225763";

    /***************  google      *********************/
    public static final String GOOGLE_APP_ID = "ca-app-pub-1877164599441785~1620583916";
    //首页第一次进入
    public static final String google_first_open_interstitial = "ca-app-pub-1877164599441785/4873433211";
    public static String google_back_or_drawer_interstitial = "ca-app-pub-1877164599441785/1398348677";

    /***************  baidu      *********************/
    //首页第一次进入
    public static final String baidu_first_open_interstitial = "152406";
    public static String baidu_back_or_drawer_interstitial = "152416";

    //feed流
    private static final String baidu_video_feed_native1 ="152410";
    private static final String baidu_video_feed_native2 ="152411";
    private static final String baidu_video_feed_native3 ="152412";
    public static String[] baidu_natives={baidu_video_feed_native1,baidu_video_feed_native2,baidu_video_feed_native3};

    /***************  yeahmobi      *********************/
    public static final String yeahmobi_open_interstitial = "29265026";
    public static final String yeahmobi_back_or_drawer_interstitial = "11635383";

    private static final String yeahmobi_feed_native1 = "59308167";
    private static final String yeahmobi_feed_native2 = "81033478";
    private static final String yeahmobi_feed_native3 = "29724315";
    public static String[] yeahmobi_natives={yeahmobi_feed_native1,yeahmobi_feed_native2,yeahmobi_feed_native3};
}
