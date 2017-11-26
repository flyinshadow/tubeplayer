package com.wenjoyai.tubeplayer.gui;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.util.LogUtil;

import java.util.Calendar;


public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    //firebase统计  https://firebase.google.com/docs/analytics/android/start/
    protected FirebaseAnalytics mFirebaseAnalytics;

    static {
        AppCompatDelegate.setDefaultNightMode(PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).getBoolean("daynight", false) ? AppCompatDelegate.MODE_NIGHT_AUTO : AppCompatDelegate.MODE_NIGHT_NO);
    }

    protected SharedPreferences mSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /* Get settings */
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        /* Theme must be applied before super.onCreate */
        applyTheme();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
    }

    private void applyTheme() {
        boolean enableBlackTheme = mSettings.getBoolean(PreferencesActivity.KEY_ENABLE_NIGHT_THEME, false);
        int themeIndex = PreferenceManager.getDefaultSharedPreferences(
                VLCApplication.getAppContext()).getInt(PreferencesActivity.KEY_CURRENT_THEME_INDEX, 0);
        boolean autoDayNight = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).getBoolean("daynight", false);
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean night = (hourOfDay <= 6) || (hourOfDay >= 18);
        if (VLCApplication.showTvUi() || enableBlackTheme || (autoDayNight && night)) {
            setTheme(ThemeFragment.sThemeNightStyles[themeIndex]);
        } else {
            setTheme(ThemeFragment.sThemeStyles[themeIndex]);
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LogUtil.d(TAG, "orientation change to landscape, try to show RateDialog");
            RateDialog.tryToShow(this, 3);
        }
    }
}
