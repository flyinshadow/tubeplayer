package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.example.ad.ADConfig;
import com.example.ad.ADFactory;
import com.example.ad.ADInit;
import com.example.ad.bean.ADWrapper;
import com.example.ad.interstitial.AbsInterstitial;
import com.example.ad.interstitial.InterstitialListener;
import com.example.ad.nativead.AbsNativeAd;
import com.example.ad.utils.MLogWriter;
import com.example.config.RemoteConfigManager;
import com.example.config.UpdateManager;
import com.facebook.ads.NativeAdsManager;
import com.wenjoyai.buy.RemoveAdManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiJiaZhi on 2017/9/23.
 * 广告管理类:广告+去广告+远程配置
 */

public class ADManager {
    protected MLogWriter mLogWriter = new MLogWriter(this.getClass().getSimpleName());

    /**
     * TODO: 1.0
     * 可以设置默认的广告平台 ADConfig.sNativeDefaultPlatForm
     * 必须在RemoteConfigManager.init之前
     */
    private void setdefaultPlatForm(long sNativeDefaultPlatForm, long sOtherDefaultPlatForm) {
        ADConfig.sNativeDefaultPlatForm = sNativeDefaultPlatForm;
        ADConfig.sOtherDefaultPlatForm = sOtherDefaultPlatForm;
    }


    /**
     * TODO: 2.0
     * 购买去广告 要么关掉此功能，要么设置key
     */
    public static String base64EncodedPublicKey = "";

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

    /**
     * 配置
     */
    //Open广告
    private ArrayList<ADWrapper> mOpenList = new ArrayList<>();
    private int mOpenIndex = 0;

    //播放返回广告
    private ArrayList<ADWrapper> mBackList = new ArrayList<>();
    private int mBackIndex = 0;

    //feeds广告
    private ArrayList<ADWrapper> mFeedList = new ArrayList<>();
    private int mFeedIndex = 0;

    /**
     *
     * @param context
     * @param listener  null:没有去广告功能
     */
    public void config(Context context, RemoveAdManager.RemoveAdListener listener) {
        /**
         * 整个功能开关  true则此有广告，默认有广告
         */
        ADInit.sOpen = true;
        /**
         * 设置默认的广告平台 ADConfig.sNativeDefaultPlatForm
         */
        setdefaultPlatForm(ADInit.AD_Facebook,ADInit.AD_Google);

        /**
         * 远程配置
         */
        RemoteConfigManager.getInstance().init(context,null);

        /**
         * 去广告的初始化
         */
        if (null!= listener&&!TextUtils.isEmpty(base64EncodedPublicKey)) {
            RemoveAdManager.getInstance().init(context, base64EncodedPublicKey, listener);
        } else {
            //没有购买
            RemoveAdManager.setIsOpen(false);
        }

        mOpenList.clear();
        mOpenIndex = 0;
        mOpenList.add(new ADWrapper(ADInit.AD_Google, ADConstants.google_first_open_interstitial));
        mOpenList.add(new ADWrapper(ADInit.AD_Yeahmobi, ADConstants.yeahmobi_open_interstitial));
        mOpenList.add(new ADWrapper(ADInit.AD_DuAd, ADConstants.baidu_first_open_interstitial));
        for (int i = 0; i < mOpenList.size(); i++) {
            if (mOpenList.get(i).platform == ADConfig.sOtherDefaultPlatForm) {
                mOpenIndex = i;
                break;
            }
        }

//        mBackList.clear();
//        mBackIndex = 0;
//        mBackList.add(new ADWrapper(ADInit.AD_Facebook, ADConstants.facebook_back_or_drawer_interstitial));
//        mBackList.add(new ADWrapper(ADInit.AD_Google, ADConstants.google_back_or_drawer_interstitial));
//        mBackList.add(new ADWrapper(ADInit.AD_Yeahmobi, ADConstants.yeahmobi_back_or_drawer_interstitial));
//        mBackList.add(new ADWrapper(ADInit.AD_DuAd, ADConstants.baidu_back_or_drawer_interstitial));
//        for (int i = 0; i < mBackList.size(); i++) {
//            if (mBackList.get(i).platform == ADConfig.sOtherDefaultPlatForm) {
//                mBackIndex = i;
//                break;
//            }
//        }

        mFeedList.clear();
        mFeedIndex = 0;
        if (TextUtils.isEmpty(ADConfig.sFbVersion) || UpdateManager.compareVersion(UpdateManager.getVersion(context), ADConfig.sFbVersion)>=0) {
            mFeedList.add(new ADWrapper(ADInit.AD_Facebook, ADConstants.facebook_feed_natives));
        }
        mFeedList.add(new ADWrapper(ADInit.AD_Yeahmobi, ADConstants.yeahmobi_natives));
        mFeedList.add(new ADWrapper(ADInit.AD_DuAd, ADConstants.baidu_natives));
        for (int i = 0; i < mFeedList.size(); i++) {
            if (mFeedList.get(i).platform == ADConfig.sNativeDefaultPlatForm) {
                mFeedIndex = i;
                break;
            }
        }
    }

    /**
     * release
     */
    public void release() {
        //reset变量
        ADFactory.getInstance().release();
        RemoveAdManager.getInstance().release();
    }

    /**
     * 加载open广告
     * @param context
     * @param listener
     */
    /**
     * open广告---回调
     */
    public interface OpenADListener {
        void onLoadedSuccess(AbsInterstitial interstitial);
    }

    public void loadOpenAD(final Context context, final OpenADListener listener) {
        if (RemoveAdManager.rmAd()) {
            return;
        }
        ADFactory.getInstance().loadInterstitial(context, mOpenList.get(mOpenIndex).platform, mOpenList.get(mOpenIndex).otherId, new InterstitialListener() {
            @Override
            public void onAdLoaded(final AbsInterstitial interstitial) {
                if (null != listener) {
                    listener.onLoadedSuccess(interstitial);
                }
            }

            @Override
            public void onOpenAd(AbsInterstitial interstitial) {
            }

            @Override
            public void onError(AbsInterstitial interstitial, Object error) {
                mOpenIndex++;
                if (mOpenIndex < mOpenList.size()) {
                    loadOpenAD(context, listener);
                }
            }
        });
    }

    /**
     * 开始加载广告
     * Facebook native广告
     */
    public void startLoadAD(final Context context) {
        if (RemoveAdManager.rmAd()) {
            return;
        }
        ADFactory.getInstance().loadFeedNatives(context, mFeedList.get(mFeedIndex).platform, mFeedList.get(mFeedIndex).nativeIds, new ADFactory.ADNumListener() {
            @Override
            public void onLoadedFirstSuccess(List<AbsNativeAd> list) {
                mLogWriter.e(" onLoadedFirstSuccess");
            }

            @Override
            public void onLoadedResult(List<AbsNativeAd> list) {
                mLogWriter.e(" onLoadedResult");
            }

            @Override
            public void onAllError() {
                mLogWriter.e(" onAllError");
                mFeedIndex++;
                if (mFeedIndex < mFeedList.size()) {
                    startLoadAD(context);
                }
            }
        });
    }

    /**
     * 获取广告
     *
     * @return
     */
    public void observerFeedNatives(ADFactory.ADNumListener listener) {
        ADFactory.getInstance().observerFeedNatives(listener);
    }

    /**
     * feed流广告个数
     *
     * @return
     */
    public List<AbsNativeAd> getFeedNatives() {
        return ADFactory.getInstance().getFeedNatives();
    }

    /**
     * 暂停广告
     */
    private NativeAdsManager mPauseManager;

    public void loadPauseAD(final Context context) {
        if (RemoveAdManager.rmAd()) {
            return;
        }
        mPauseManager = ADFactory.getInstance().loadFacebookNativeAdsManager(context, ADConstants.facebook_video_pause, 1);
    }

    public void showPauseAd(ViewGroup container) {
        ADFactory.getInstance().showFacebookPauseAd(container, mPauseManager);
    }

    public boolean canShowPause() {
        return ADFactory.getInstance().canShowFacebookPause(mPauseManager);
    }

    /**
     * 侧边栏或者播放返回广告
     */
    public AbsInterstitial mBackOrDrawerInterstitial;

    public void loadBackOrDrawerInterstitial(final Context context) {
        if (RemoveAdManager.rmAd()) {
            return;
        }
        ADFactory.getInstance().loadInterstitial(context, mBackList.get(mBackIndex).platform, mBackList.get(mBackIndex).otherId, new InterstitialListener() {
            @Override
            public void onAdLoaded(final AbsInterstitial interstitial) {
                mBackOrDrawerInterstitial = interstitial;
            }

            @Override
            public void onOpenAd(AbsInterstitial interstitial) {
            }

            @Override
            public void onError(AbsInterstitial interstitial, Object error) {
                mBackOrDrawerInterstitial = null;
                mBackIndex++;
                if (mBackIndex < mBackList.size()) {
                    loadBackOrDrawerInterstitial(context);
                }
            }
        });
    }

    public void tryshowBackOrDrawerInterstitial() {
        if (null != mBackOrDrawerInterstitial) {
            mBackOrDrawerInterstitial.show();
            mBackOrDrawerInterstitial = null;
        }
    }

    public boolean canShowBackOrDrawerInterstitial() {
        return null != mBackOrDrawerInterstitial;
    }
}
