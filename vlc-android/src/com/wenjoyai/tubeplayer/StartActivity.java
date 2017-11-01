/*
 * *************************************************************************
 *  StartActivity.java
 * **************************************************************************
 *  Copyright © 2015 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */

package com.wenjoyai.tubeplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.ad.ADConstants;
import com.wenjoyai.tubeplayer.ad.ADManager;
import com.wenjoyai.tubeplayer.ad.NativeAD;
import com.wenjoyai.tubeplayer.gui.AudioPlayerContainerActivity;
import com.wenjoyai.tubeplayer.gui.BaseActivity;
import com.wenjoyai.tubeplayer.gui.MainActivity;
import com.wenjoyai.tubeplayer.gui.RateFragment;
import com.wenjoyai.tubeplayer.gui.SearchActivity;
import com.wenjoyai.tubeplayer.gui.tv.MainTvActivity;
import com.wenjoyai.tubeplayer.gui.tv.audioplayer.AudioPlayerActivity;
import com.wenjoyai.tubeplayer.gui.video.VideoGridFragment;
import com.wenjoyai.tubeplayer.gui.video.VideoPlayerActivity;
import com.wenjoyai.tubeplayer.media.MediaUtils;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.Permissions;

import org.videolan.libvlc.util.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends BaseActivity {

    public final static String TAG = "VLC/StartActivity";

    private static final String PREF_FIRST_RUN = "first_run";
    public static final String EXTRA_FIRST_RUN = "extra_first_run";
    public static final String EXTRA_UPGRADE = "extra_upgrade";

    private TextView mSkipTv;
    private FrameLayout mNativeContainer;

    boolean firstRun;
    boolean upgrade;
    boolean tv;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);
        mSkipTv = (TextView) findViewById(R.id.skip_tv);
        mNativeContainer = (FrameLayout) findViewById(R.id.adContainer);

        Intent intent = getIntent();
        tv = showTvUi();
        String action = intent != null ? intent.getAction() : null;

        if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
            intent.setDataAndType(intent.getData(), intent.getType());
            if (intent.getType() != null && intent.getType().startsWith("video"))
                startActivity(intent.setClass(this, VideoPlayerActivity.class));
            else
                MediaUtils.openMediaNoUi(intent.getData());
            finish();
            return;
        }

        // Start application
        /* Get the current version from package */
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int currentVersionNumber = BuildConfig.VERSION_CODE;
        int savedVersionNumber = settings.getInt(PREF_FIRST_RUN, -1);
        /* Check if it's the first run */
        firstRun = savedVersionNumber == -1;
        upgrade = firstRun || savedVersionNumber != currentVersionNumber;
        if (upgrade)
            settings.edit().putInt(PREF_FIRST_RUN, currentVersionNumber).apply();
        // Rate dialog should come out
        if (upgrade) {
            settings.edit().putLong(RateFragment.KEY_RATE_SHOW_LAST, 0).apply();
            settings.edit().putLong(RateFragment.KEY_RATE_SHOW_NEXT, 0).apply();
            settings.edit().putInt(RateFragment.KEY_RATE_SHOW_COUNT, 0).apply();
            settings.edit().putInt(RateFragment.KEY_RATE_LAST_VERSION, 0).apply();

            settings.edit().putBoolean(VideoGridFragment.KEY_STAT_VIDEO_COUNT, false).apply();
            settings.edit().putBoolean(VideoGridFragment.KEY_PARSING_ONCE, false).apply();
        }
        startMedialibrary(firstRun, upgrade);
        // Route search query
        if (Intent.ACTION_SEARCH.equals(action) || "com.google.android.gms.actions.SEARCH_ACTION".equals(action)) {
            startActivity(intent.setClass(this, tv ? com.wenjoyai.tubeplayer.gui.tv.SearchActivity.class : SearchActivity.class));
            finish();
            return;
        } else if (MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH.equals(action)) {
            Intent serviceInent = new Intent(PlaybackService.ACTION_PLAY_FROM_SEARCH, null, this, PlaybackService.class)
                    .putExtra(PlaybackService.EXTRA_SEARCH_BUNDLE, intent.getExtras());
            startService(serviceInent);
            //add
            finish();
            return;
        } else if (AudioPlayerContainerActivity.ACTION_SHOW_PLAYER.equals(action)) {
            startActivity(new Intent(this, tv ? AudioPlayerActivity.class : MainActivity.class));
            //add
            finish();
            return;
        } else {
            loadAD();
            timer = new CountDownTimer(5 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mSkipTv.setText(millisUntilFinished / 1000 + "s");
                }

                @Override
                public void onFinish() {
                    jump();
                }
            }.start();
        }
    }

    /**
     * 禁用返回键
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    private void jump() {
        startActivity(new Intent(this, tv ? MainTvActivity.class : MainActivity.class)
                .putExtra(EXTRA_FIRST_RUN, firstRun)
                .putExtra(EXTRA_UPGRADE, upgrade));
        finish();
    }

    private void startMedialibrary(boolean firstRun, boolean upgrade) {
        if (!VLCApplication.getMLInstance().isInitiated() && Permissions.canReadStorage())
            startService(new Intent(MediaParsingService.ACTION_INIT, null, this, MediaParsingService.class)
                    .putExtra(EXTRA_FIRST_RUN, firstRun)
                    .putExtra(EXTRA_UPGRADE, upgrade));
    }

    private boolean showTvUi() {
        return AndroidUtil.isJellyBeanMR1OrLater && (AndroidDevices.isAndroidTv() || !AndroidDevices.hasTsp() ||
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("tv_ui", false));
    }

    private void loadAD() {
        NativeAD mFeedNativeAD = new NativeAD();
        mFeedNativeAD.loadAD(this, ADManager.AD_Facebook, ADConstants.facebook_video_pause_native, new NativeAD.ADListener() {
            @Override
            public void onLoadedSuccess(com.facebook.ads.NativeAd nativeAd, String adId) {
                if (null == mNativeContainer || null == nativeAd) {
                    //异步过程，可能当前页面已经销毁了
                    return;
                }
                mNativeContainer.setVisibility(View.VISIBLE);
                LayoutInflater inflater = LayoutInflater.from(StartActivity.this);
                LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.layout_pause_native_ad, mNativeContainer, false);
                mNativeContainer.removeAllViews();
                mNativeContainer.addView(adView);

                // Create native UI using the ad_front metadata.
                ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.native_ad_icon);
                TextView nativeAdTitle = (TextView) adView.findViewById(R.id.native_ad_title);
                MediaView nativeAdMedia = (MediaView) adView.findViewById(R.id.native_ad_media);
                // TextView nativeAdSocialContext = (TextView) adView.findViewById(R.id.native_ad_social_context);
                TextView nativeAdBody = (TextView) adView.findViewById(R.id.native_ad_body);
                Button nativeAdCallToAction = (Button) adView.findViewById(R.id.native_ad_call_to_action);

                // Set the Text.
                nativeAdTitle.setText(nativeAd.getAdTitle());
                // nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                nativeAdBody.setText(nativeAd.getAdBody());
                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());

                // Download and display the ad_front icon.
                NativeAd.Image adIcon = nativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

                // Download and display the cover image.
                nativeAdMedia.setNativeAd(nativeAd);

                // Add the AdChoices icon
                LinearLayout adChoicesContainer = (LinearLayout) findViewById(R.id.ad_choices_container);
                AdChoicesView adChoicesView = new AdChoicesView(StartActivity.this, nativeAd, true);
                adChoicesContainer.addView(adChoicesView);

                // Register the Title and CTA button to listen for clicks.
                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(nativeAdTitle);
                clickableViews.add(nativeAdCallToAction);
                nativeAd.registerViewForInteraction(mNativeContainer, clickableViews);
            }

            @Override
            public void onLoadedFailed(String msg, String adId, int errorcode) {
                if (null != timer) {
                    timer.cancel();
                }
                jump();
            }

            @Override
            public void onAdClick() {

            }

            @Override
            public void onAdImpression(NativeAd ad, String adId) {

            }
        });
    }

}