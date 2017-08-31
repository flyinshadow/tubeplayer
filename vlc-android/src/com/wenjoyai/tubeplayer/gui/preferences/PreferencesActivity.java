/*****************************************************************************
 * PreferencesActivity.java
 *****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.wenjoyai.tubeplayer.gui.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.ThemeFragment;

import java.util.Calendar;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends AppCompatActivity implements PlaybackService.Client.Callback {

    public final static String TAG = "VLC/PreferencesActivity";

    public final static String NAME = "VlcSharedPreferences";
    public final static String VIDEO_RESUME_TIME = "VideoResumeTime";
    public final static String VIDEO_PAUSED = "VideoPaused";
    public final static String VIDEO_SUBTITLE_FILES = "VideoSubtitleFiles";
    public final static String VIDEO_SPEED = "VideoSpeed";
    public final static String VIDEO_BACKGROUND = "video_background";
    public final static String VIDEO_RESTORE = "video_restore";
    public final static String VIDEO_RATE = "video_rate";
    public final static String VIDEO_RATIO = "video_ratio";
    public final static String AUTO_RESCAN = "auto_rescan";
    public final static String LOGIN_STORE = "store_login";
    public static final String KEY_AUDIO_PLAYBACK_RATE = "playback_rate";
    public static final String KEY_AUDIO_PLAYBACK_SPEED_PERSIST = "playback_speed";

    public static final String KEY_ENABLE_NIGHT_THEME = "enable_night_theme";
    public static final String KEY_AUTO_DAY_NIGHT_MODE = "daynight";
    public static final String KEY_CURRENT_THEME_INDEX = "current_theme_index";
    public static final String KEY_CURRENT_VIEW_MODE = "current_view_mode";

    public final static int RESULT_RESCAN = RESULT_FIRST_USER + 1;
    public final static int RESULT_RESTART = RESULT_FIRST_USER + 2;
    public final static int RESULT_RESTART_APP = RESULT_FIRST_USER + 3;

    private PlaybackService.Client mClient = new PlaybackService.Client(this, this);
    private PlaybackService mService;
    private NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Theme must be applied before super.onCreate */
        applyTheme();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.preferences_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, new PreferencesFragment())
                    .commit();
        }
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedscrollview);
    }

    void scrollUp() {
        nestedScrollView.scrollTo(0,0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!getSupportFragmentManager().popBackStackImmediate())
                finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyTheme() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableBlackTheme = pref.getBoolean(PreferencesActivity.KEY_ENABLE_NIGHT_THEME, false);
        int themeIndex = pref.getInt(PreferencesActivity.KEY_CURRENT_THEME_INDEX, 0);
        boolean autoDayNight = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).getBoolean("daynight", false);
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean night = (hourOfDay <= 6) || (hourOfDay >= 18);
        if (enableBlackTheme || (autoDayNight && night)) {
            setTheme(ThemeFragment.sThemeNightStyles[themeIndex]);
        } else {
            setTheme(ThemeFragment.sThemeStyles[themeIndex]);
        }
    }

    @Override
    public void onConnected(PlaybackService service) {
        mService = service;
    }

    @Override
    public void onDisconnected() {
        mService = null;
    }

    public void restartMediaPlayer(){
        if (mService != null)
            mService.restartMediaPlayer();
    }

    public void exitAndRescan(){
        setRestart();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void setRestart(){
        setResult(RESULT_RESTART);
    }

    public void setRestartApp(){
        setResult(RESULT_RESTART_APP);
    }

    public void detectHeadset(boolean detect){
        if (mService != null)
            mService.detectHeadset(detect);
    }
}
