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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

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

public class StartActivity extends BaseActivity {

    public final static String TAG = "VLC/StartActivity";

    private static final String PREF_FIRST_RUN = "first_run";
    public static final String EXTRA_FIRST_RUN = "extra_first_run";
    public static final String EXTRA_UPGRADE = "extra_upgrade";

//    public static final int AD_LOAD_START = 1;
//    public static final int AD_SKIP = 2;
//    public static final int AD_LOAD_SUCCESS = 3;
//    public static final int AD_LOAD_FAILED = 4;
//    public static final int AD_CLOSED = 5;
//
//    public static final int AD_DELAY = 3000;
//
//    private Interstitial mInterstitial;
//    private boolean mNormalStart = false;
//
//    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
//
//        @Override
//        public boolean handleMessage(Message message) {
//
//            switch (message.what) {
//                case AD_LOAD_START:
//                    break;
//                case AD_LOAD_SUCCESS:
//                    showAd();
//                    break;
//                case AD_SKIP:
//                case AD_LOAD_FAILED:
//                case AD_CLOSED:
//                    normalStart();
//                    break;
//            }
//            return true;
//        }
//    });
//
//    private void showAd() {
//        if (mNormalStart) {
//            return;
//        }
//        mHandler.removeCallbacksAndMessages(null);
//        if (mInterstitial != null)
//            mInterstitial.show();
//        else {
//            normalStart();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean tv =  showTvUi();
        String action = intent != null ? intent.getAction(): null;

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
        boolean firstRun = savedVersionNumber == -1;
        boolean upgrade = firstRun || savedVersionNumber != currentVersionNumber;
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
        } else if (AudioPlayerContainerActivity.ACTION_SHOW_PLAYER.equals(action)) {
            startActivity(new Intent(this, tv ? AudioPlayerActivity.class : MainActivity.class));
        } else {
            startActivity(new Intent(this, tv ? MainTvActivity.class : MainActivity.class)
                    .putExtra(EXTRA_FIRST_RUN, firstRun)
                    .putExtra(EXTRA_UPGRADE, upgrade));
        }
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

}
