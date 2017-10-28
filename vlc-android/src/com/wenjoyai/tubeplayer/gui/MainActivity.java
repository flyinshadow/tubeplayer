/*****************************************************************************
 * MainActivity.java
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

package com.wenjoyai.tubeplayer.gui;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FilterQueryProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wenjoyai.tubeplayer.BuildConfig;
import com.wenjoyai.tubeplayer.MediaParsingService;
import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.StartActivity;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.ad.ADConstants;
import com.wenjoyai.tubeplayer.ad.ADManager;
import com.wenjoyai.tubeplayer.ad.ExitDialog;
import com.wenjoyai.tubeplayer.ad.Interstitial;
import com.wenjoyai.tubeplayer.ad.LoadingDialog;
import com.wenjoyai.tubeplayer.ad.NetWorkUtil;
import com.wenjoyai.tubeplayer.extensions.ExtensionListing;
import com.wenjoyai.tubeplayer.extensions.ExtensionManagerService;
import com.wenjoyai.tubeplayer.extensions.api.VLCExtensionItem;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.audio.AudioBrowserFragment;
import com.wenjoyai.tubeplayer.gui.browser.BaseBrowserFragment;
import com.wenjoyai.tubeplayer.gui.browser.ExtensionBrowser;
import com.wenjoyai.tubeplayer.gui.browser.MediaBrowserFragment;
import com.wenjoyai.tubeplayer.gui.browser.NetworkBrowserFragment;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.gui.network.MRLPanelFragment;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesFragment;
import com.wenjoyai.tubeplayer.gui.video.VideoGridFragment;
import com.wenjoyai.tubeplayer.gui.video.VideoListAdapter;
import com.wenjoyai.tubeplayer.gui.view.HackyDrawerLayout;
import com.wenjoyai.tubeplayer.interfaces.Filterable;
import com.wenjoyai.tubeplayer.interfaces.IHistory;
import com.wenjoyai.tubeplayer.interfaces.IRefreshable;
import com.wenjoyai.tubeplayer.interfaces.ISortable;
import com.wenjoyai.tubeplayer.media.MediaDatabase;
import com.wenjoyai.tubeplayer.media.MediaUtils;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.Permissions;
import com.wenjoyai.tubeplayer.util.VLCInstance;

import org.videolan.medialibrary.Medialibrary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity.RESULT_RESTART;

public class MainActivity extends AudioPlayerContainerActivity implements FilterQueryProvider, NavigationView.OnNavigationItemSelectedListener, ExtensionManagerService.ExtensionManagerActivity, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {
    public final static String TAG = "VLC/MainActivity";

    private static final int ACTIVITY_RESULT_PREFERENCES = 1;
    private static final int ACTIVITY_RESULT_OPEN = 2;
    public static final int ACTIVITY_RESULT_SECONDARY = 3;
    private static final int ACTIVITY_SHOW_INFOLAYOUT = 2;
    private static final int ACTIVITY_HIDE_INFOLAYOUT = 3;
    private static final int ACTIVITY_SHOW_PROGRESSBAR = 4;
    private static final int ACTIVITY_HIDE_PROGRESSBAR = 5;
    private static final int ACTIVITY_SHOW_TEXTINFO = 6;
    private static final int ACTIVITY_UPDATE_PROGRESS = 7;


    private Medialibrary mMediaLibrary;
    private HackyDrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    private int mCurrentFragmentId;

    private boolean mScanNeeded = false;

    private Menu mMenu;
    private SearchView mSearchView;
    private boolean mFirstRun, mUpgrade;

    // Extensions management
    private ServiceConnection mExtensionServiceConnection;
    private ExtensionManagerService mExtensionManagerService;
    private static final int PLUGIN_NAVIGATION_GROUP = 2;
    //广告view
//    private RotateAD mRotateAD;
    private Interstitial mFirstOpenInterstitialAd;
    private boolean mIsResumed = true;//当前页面是否在前台
    private static SharedPreferences sSettings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
    private long mOpenCount;//启动次数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!VLCInstance.testCompatibleCPU(this)) {
            finish();
            return;
        }
        /* Enable the indeterminate progress feature */
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        Permissions.checkReadStoragePermission(this, false);

        /*** Start initializing the UI ***/

        setContentView(R.layout.main);
        initConfig();
//        initAD();
        //开始广告缓存
        ADManager.getInstance().startLoadAD(this);
        mDrawerLayout = (HackyDrawerLayout) findViewById(R.id.root_container);
        setupNavigationView();

        initAudioPlayerContainerActivity();

        if (savedInstanceState != null) {
            mCurrentFragmentId = savedInstanceState.getInt("current");
            if (mCurrentFragmentId > 0)
                mNavigationView.setCheckedItem(mCurrentFragmentId);
        }

        /* Set up the action bar */
        prepareActionBar();

        /* Set up the sidebar click listener
         * no need to invalidate menu for now */
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder) instanceof MediaBrowserFragment)
                    ((MediaBrowserFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder)).setReadyToDisplay(true);
            }

            // Hack to make navigation drawer browsable with DPAD.
            // see https://code.google.com/p/android/issues/detail?id=190975
            // and http://stackoverflow.com/a/34658002/3485324
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (mNavigationView.requestFocus())
                    ((NavigationMenuView) mNavigationView.getFocusedChild()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        if (getIntent().getBooleanExtra(StartActivity.EXTRA_UPGRADE, false)) {
            mUpgrade = true;
            mFirstRun = getIntent().getBooleanExtra(StartActivity.EXTRA_FIRST_RUN, false);
            /*
             * The sliding menu is automatically opened when the user closes
             * the info dialog. If (for any reason) the dialog is not shown,
             * open the menu after a short delay.
             */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(mNavigationView);
                }
            }, 500);
            getIntent().removeExtra(StartActivity.EXTRA_UPGRADE);
        }

        /* Reload the latest preferences */
        reloadPreferences();
        mScanNeeded = savedInstanceState == null && mSettings.getBoolean("auto_rescan", true);

        mMediaLibrary = VLCApplication.getMLInstance();
        submitNetwork();

        mOpenCount = mSettings.getLong(OPEN_COUNT, 0);
        mOpenCount++;
        sSettings.edit().putLong(OPEN_COUNT, mOpenCount).apply();
    }

    private void submitNetwork() {
        String str = NetWorkUtil.getCurrentNetworkType();
        Log.e(TAG, str);
        StatisticsManager.submitSelectContent(MainActivity.this, StatisticsManager.TYPE_NETWORK, str);
    }

    private boolean isloadAD = false;

    private void loadAD() {
        //旋转广告墙
        if (ADManager.sLevel >= ADManager.Level_Big) {
            preloadWall();
        }
        loadOpenAD();
        loadExitAD();
    }

    private Handler mHandler = new Handler();
    public static final String KEY_LAST_OPEN_TIME = "key_last_open_time";
    LoadingDialog dialog;
    ExitDialog mExitDialog;

    //第一次打开
    private void loadOpenAD() {

        long second = mSettings.getLong(KEY_LAST_OPEN_TIME, 0);
        if (second == 0 || (System.currentTimeMillis() / 1000 - second) / 60 >= 2) {
            mSettings.edit().putLong(KEY_LAST_OPEN_TIME, System.currentTimeMillis() / 1000).apply();

            String adID = "";
            if (ADManager.sPlatForm == ADManager.AD_MobVista) {
            } else if (ADManager.sPlatForm == ADManager.AD_Google) {
                adID = ADConstants.google_first_open_interstitial;
            } else if (ADManager.sPlatForm == ADManager.AD_Facebook) {
                adID = ADConstants.facebook_first_open_interstitial;
            }
            if (!TextUtils.isEmpty(adID)) {
                mFirstOpenInterstitialAd = new Interstitial();
                StatisticsManager.submitAd(this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_GOOGLE_FIRST_OPEN + "request");
                mFirstOpenInterstitialAd.loadAD(this, ADManager.sPlatForm, adID, new Interstitial.ADListener() {
                    @Override
                    public void onLoadedSuccess() {
                        StatisticsManager.submitAd(MainActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_GOOGLE_FIRST_OPEN + "loaded");
                        if (mIsResumed) {
                            // create alert dialog
                            if (dialog == null) {
                                dialog = new LoadingDialog(MainActivity.this, R.style.dialog);
                                dialog.setCancelable(true);
                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        mFirstOpenInterstitialAd.show();
                                        StatisticsManager.submitAd(MainActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_GOOGLE_FIRST_OPEN + "show");
                                    }
                                });
                            }
                            if (null != dialog && !isFinishing() && !dialog.isShowing())
                                dialog.show();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (null != dialog && dialog.isShowing() && !isFinishing()) {
                                        dialog.dismiss();
                                    }
                                }
                            }, 1000);
                        }
                    }

                    @Override
                    public void onLoadedFailed() {

                    }

                    @Override
                    public void onAdClick() {
                        StatisticsManager.submitAd(MainActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_GOOGLE_FIRST_OPEN + "click");
                    }

                    @Override
                    public void onAdClose() {

                    }
                });
            }
        }
    }

    private void loadExitAD() {
        ADManager.getInstance().loadExitAD(this);
        ADManager.getInstance().loadPauseAD(this);
    }

    private void setupNavigationView() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        if (TextUtils.equals(BuildConfig.FLAVOR_target, "chrome")) {
            MenuItem item = mNavigationView.getMenu().findItem(R.id.nav_directories);
            item.setTitle(R.string.open);
        }

        mNavigationView.getMenu().findItem(R.id.nav_history).setVisible(mSettings.getBoolean(PreferencesFragment.PLAYBACK_HISTORY, true));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Permissions.PERMISSION_STORAGE_TAG:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent serviceIntent = new Intent(MediaParsingService.ACTION_INIT, null, this, MediaParsingService.class);
                    serviceIntent.putExtra(StartActivity.EXTRA_FIRST_RUN, mFirstRun);
                    serviceIntent.putExtra(StartActivity.EXTRA_UPGRADE, mUpgrade);
                    startService(serviceIntent);
                } else
                    Permissions.showStoragePermissionDialog(this, false);
                break;
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void prepareActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Deactivated for now
//        createExtensionServiceConnection();

        clearBackstackFromClass(ExtensionBrowser.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //准备新广告
//        ADManager.getInstance().startLoadAD(this);
        if (mExtensionServiceConnection != null) {
            unbindService(mExtensionServiceConnection);
            mExtensionServiceConnection = null;
        }
    }

    private void loadPlugins() {
        Menu navMenu = mNavigationView.getMenu();
        navMenu.removeGroup(PLUGIN_NAVIGATION_GROUP);
        List<ExtensionListing> plugins = mExtensionManagerService.updateAvailableExtensions();
        if (plugins.isEmpty()) {
            unbindService(mExtensionServiceConnection);
            mExtensionServiceConnection = null;
            mExtensionManagerService.stopSelf();
            return;
        }
        PackageManager pm = getPackageManager();
        SubMenu subMenu = navMenu.addSubMenu(PLUGIN_NAVIGATION_GROUP, PLUGIN_NAVIGATION_GROUP,
                PLUGIN_NAVIGATION_GROUP, R.string.plugins);
        for (int i = 0; i < plugins.size(); ++i) {
            ExtensionListing extension = plugins.get(i);
            MenuItem item = subMenu.add(PLUGIN_NAVIGATION_GROUP, i, 0, extension.title());
            int iconRes = extension.menuIcon();
            Drawable extensionIcon = null;
            if (iconRes != 0) {
                try {
                    Resources res = VLCApplication.getAppContext().getPackageManager().getResourcesForApplication(extension.componentName().getPackageName());
                    extensionIcon = res.getDrawable(extension.menuIcon());
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            if (extensionIcon != null)
                item.setIcon(extensionIcon);
            else
                try {
                    item.setIcon(pm.getApplicationIcon(plugins.get(i).componentName().getPackageName()));
                } catch (PackageManager.NameNotFoundException e) {
                    item.setIcon(R.drawable.icon);
                }
        }
        mNavigationView.invalidate();
    }

    private void createExtensionServiceConnection() {
        mExtensionServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mExtensionManagerService = ((ExtensionManagerService.LocalBinder) service).getService();
                mExtensionManagerService.setExtensionManagerActivity(MainActivity.this);
                loadPlugins();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        // Bind service which discoverves au connects toplugins
        if (!bindService(new Intent(MainActivity.this,
                ExtensionManagerService.class), mExtensionServiceConnection, Context.BIND_AUTO_CREATE))
            mExtensionServiceConnection = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;
        if (mMediaLibrary.isInitiated()) {
            /* Load media items from database and storage */
            if (mScanNeeded && Permissions.canReadStorage())
                startService(new Intent(MediaParsingService.ACTION_RELOAD, null, this, MediaParsingService.class));
            else
                restoreCurrentList();
        }
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(mCurrentFragmentId);
        mCurrentFragmentId = mSettings.getInt("fragment_id", R.id.nav_video);
        if (!isloadAD) {
            isloadAD = true;
            int interval = 0;
            if (mOpenCount == 1) {
                interval = 15000;
            } else {
                interval = 500;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAD();
                }
            }, interval);
        }
    }

    //google lijiazhi
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static final String PLATFOM = "ad_platform";
    private static final String OPEN_COUNT = "first_open";


    private void initConfig() {
        //init
        ADManager.sPlatForm = mSettings.getLong(PLATFOM, ADManager.AD_Facebook);

        // Get Remote Config instance.
        // [START get_remote_config_instance]
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. See Best Practices in the
        // README for more information.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
//        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        // [END set_default_values]

        fetchWelcome();
    }

    /**
     * Fetch a welcome message from the Remote Config service, and then activate it.
     */
    private void fetchWelcome() {
//        mWelcomeTextView.setText(mFirebaseRemoteConfig.getString(LOADING_PHRASE_CONFIG_KEY));
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        }
                        ADManager.isShowGoogleVideoBanner = mFirebaseRemoteConfig.getBoolean("is_video_banner_show");
                        ADManager.isShowMobvista = mFirebaseRemoteConfig.getBoolean("is_mobvista_ad_open");

                        ADManager.sPlatForm = mFirebaseRemoteConfig.getLong("ad_platform_type");
                        ADManager.sLevel = mFirebaseRemoteConfig.getLong("ad_level_type");
                        ADManager.back_ad_delay_time = mFirebaseRemoteConfig.getLong("back_ad_delay_time");
                        sSettings.edit().putLong(PLATFOM, ADManager.sPlatForm).apply();

//                        ADManager.REQUEST_FEED_NTIVE_INTERVAL = mFirebaseRemoteConfig.getLong("request_feed_native_interval");

                    }
                });
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        // Figure out if currently-loaded fragment is a top-level fragment.
        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_placeholder);

        /**
         * Restore the last view.
         *
         * Replace:
         * - null fragments (freshly opened Activity)
         * - Wrong fragment open AND currently displayed fragment is a top-level fragment
         *
         * Do not replace:
         * - Non-sidebar fragments.
         * It will try to remove() the currently displayed fragment
         * (i.e. tracks) and replace it with a blank screen. (stuck menu bug)
         */
        if (current == null) {
            String tag = getTag(mCurrentFragmentId);
            mNavigationView.setCheckedItem(mCurrentFragmentId);
            Fragment ff = getFragment(mCurrentFragmentId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_placeholder, ff, tag)
                    .addToBackStack(tag)
                    .commit();
        }
    }

    /**
     * Stop audio player and save opened tab
     */
    @Override
    protected void onPause() {
        super.onPause();
        mIsResumed = false;
        mNavigationView.setNavigationItemSelectedListener(null);
        if (getChangingConfigurations() == 0) {
            /* Check for an ongoing scan that needs to be resumed during onResume */
            mScanNeeded = mMediaLibrary.isWorking();
        }
        /* Save the tab status in pref */
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt("fragment_id", mCurrentFragmentId);
        editor.apply();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current", mCurrentFragmentId);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /* Reload the latest preferences */
        reloadPreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        /* Close the menu first */
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }

        /* Close playlist search if open or Slide down the audio player if it is shown entirely. */
        if (isAudioPlayerReady() && (mAudioPlayer.clearSearch() || slideDownAudioPlayer()))
            return;

        // If it's the directory view, a "backpressed" action shows a parent.
        Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_placeholder);
        if (fragment instanceof BaseBrowserFragment) {
            ((BaseBrowserFragment) fragment).goBack();
            return;
        } else if (fragment instanceof ExtensionBrowser) {
            ((ExtensionBrowser) fragment).goBack();
            return;
        }
        if (ADManager.getInstance().mExitManager.isLoaded()) {
            showExitDialog();
        } else {
            finish();
        }
    }

    private void showExitDialog() {
        if (mExitDialog == null) {
            mExitDialog = new ExitDialog(MainActivity.this);
            mExitDialog.setCancelable(true);
        }
        if (null != mExitDialog && !isFinishing() && !mExitDialog.isShowing())
            mExitDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);

        LogUtil.d(TAG, "viewmode onConfigurationChanged: " + newConfig.orientation);
        boolean visible = true;
        int viewMode = mSettings.getInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE, VideoListAdapter.VIEW_MODE_DEFAULT);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            visible = false;
            viewMode = VideoListAdapter.VIEW_MODE_GRID;
        }
        if (fragment instanceof VideoGridFragment) {
            if (mMenu != null)
                mMenu.findItem(R.id.ml_menu_view_mode).setVisible(visible);
            if (viewMode != ((VideoGridFragment) fragment).getCurrentViewMode()) {
                ((VideoGridFragment) fragment).toggleVideoMode(viewMode);
            }
        }
    }

    private Fragment getFragment(int id) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(getTag(id));
        if (frag != null)
            return frag;
        return getNewFragment(id);
    }

    @NonNull
    private Fragment getNewFragment(int id) {
        switch (id) {
            case R.id.nav_audio:
                return new AudioBrowserFragment();
            case R.id.nav_directories:
                return new VideoFolderFragment();
            case R.id.nav_history:
                return new HistoryFragment();
            case R.id.nav_network:
                return new NetworkBrowserFragment();
            default:
                return new VideoGridFragment();
        }
    }

    @Override
    public void displayExtensionItems(String title, List<VLCExtensionItem> items, boolean showParams, boolean refresh) {
        FragmentManager fm = getSupportFragmentManager();

        if (refresh && fm.findFragmentById(R.id.fragment_placeholder) instanceof ExtensionBrowser) {
            ExtensionBrowser browser = (ExtensionBrowser) fm.findFragmentById(R.id.fragment_placeholder);
            browser.doRefresh(title, items);
        } else {
            ExtensionBrowser fragment = new ExtensionBrowser();
            ArrayList<VLCExtensionItem> list = new ArrayList<>(items);
            Bundle args = new Bundle();
            args.putParcelableArrayList(ExtensionBrowser.KEY_ITEMS_LIST, list);
            args.putBoolean(ExtensionBrowser.KEY_SHOW_FAB, showParams);
            args.putString(ExtensionBrowser.KEY_TITLE, title);
            fragment.setArguments(args);
            fragment.setExtensionService(mExtensionManagerService);

            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.anim_enter_right, 0, R.anim.anim_enter_left, 0);
            ft.replace(R.id.fragment_placeholder, fragment, title);
            if (!(fm.findFragmentById(R.id.fragment_placeholder) instanceof ExtensionBrowser))
                ft.addToBackStack(getTag(mCurrentFragmentId));
            else
                ft.addToBackStack(title);
            ft.commit();
        }
    }

    /**
     * Show a secondary fragment.
     */
    public void showSecondaryFragment(String fragmentTag) {
        showSecondaryFragment(fragmentTag, null);
    }

    public void showSecondaryFragment(String fragmentTag, String param) {
        Intent i = new Intent(this, SecondaryActivity.class);
        i.putExtra("fragment", fragmentTag);
        if (param != null)
            i.putExtra("param", param);
        startActivityForResult(i, ACTIVITY_RESULT_SECONDARY);
        // Slide down the audio player if needed.
        slideDownAudioPlayer();
    }

    public void showSecondaryFragment2(String fragmentTag, String param, String param2) {
        Intent i = new Intent(this, SecondaryActivity.class);
        i.putExtra("fragment", fragmentTag);
        if (param != null)
            i.putExtra("param", param);
        if (param2 != null)
            i.putExtra("param2", param2);
        startActivityForResult(i, ACTIVITY_RESULT_SECONDARY);
        // Slide down the audio player if needed.
        slideDownAudioPlayer();
    }

    @Nullable
    @Override
    public ActionMode startSupportActionMode(@NonNull ActionMode.Callback callback) {
        mAppBarLayout.setExpanded(true);
        return super.startSupportActionMode(callback);
    }

    /**
     * Create menu from XML
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        /* Note: on Android 3.0+ with an action bar this method
         * is called while the view is created. This can happen
         * any time after onCreate.
         */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.media_library, menu);

        MenuItem searchItem = menu.findItem(R.id.ml_menu_filter);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint(getString(R.string.search_list_hint));
        mSearchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchItem, this);

        MenuItem viewModeItem = menu.findItem(R.id.ml_menu_view_mode);
        if (viewModeItem != null) {
            int currentViewMode = mSettings.getInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE, VideoListAdapter.VIEW_MODE_DEFAULT);
            if (currentViewMode == VideoListAdapter.VIEW_MODE_LIST) {
                viewModeItem.setIcon(R.drawable.ic_view_list);
            } else if (currentViewMode == VideoListAdapter.VIEW_MODE_GRID) {
                viewModeItem.setIcon(R.drawable.ic_view_grid);
            } else if (currentViewMode == VideoListAdapter.VIEW_MODE_BIGPIC) {
                viewModeItem.setIcon(R.drawable.ic_view_bigpic);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu == null)
            return false;
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
        MenuItem item;
        // Disable the sort option if we can't use it on the current fragment.
        if (current == null || !(current instanceof ISortable)) {
            item = menu.findItem(R.id.ml_menu_sortby);
            if (item == null)
                return false;
            item.setEnabled(false);
            item.setVisible(false);
        } else {
            ISortable sortable = (ISortable) current;
            item = menu.findItem(R.id.ml_menu_sortby);
            if (item == null)
                return false;
            item.setEnabled(true);
            item.setVisible(true);
            item = menu.findItem(R.id.ml_menu_sortby_name);
            if (sortable.sortDirection(VideoListAdapter.SORT_BY_TITLE) == 1)
                item.setTitle(R.string.sortby_name_desc);
            else
                item.setTitle(R.string.sortby_name);
            item = menu.findItem(R.id.ml_menu_sortby_length);
            if (sortable.sortDirection(VideoListAdapter.SORT_BY_LENGTH) == 1)
                item.setTitle(R.string.sortby_length_desc);
            else
                item.setTitle(R.string.sortby_length);
            item = menu.findItem(R.id.ml_menu_sortby_date);
            if (sortable.sortDirection(VideoListAdapter.SORT_BY_DATE) == 1)
                item.setTitle(R.string.sortby_date_desc);
            else
                item.setTitle(R.string.sortby_date);
        }

        if (current instanceof NetworkBrowserFragment &&
                !((NetworkBrowserFragment) current).isRootDirectory()) {
            item = menu.findItem(R.id.ml_menu_save);
            item.setVisible(true);
            String mrl = ((BaseBrowserFragment) current).mMrl;
            boolean isFavorite = MediaDatabase.getInstance().networkFavExists(Uri.parse(mrl));
            item.setIcon(isFavorite ?
                    R.drawable.ic_menu_bookmark_w :
                    R.drawable.ic_menu_bookmark_outline_w);
            item.setTitle(isFavorite ? R.string.favorites_remove : R.string.favorites_add);
        } else
            menu.findItem(R.id.ml_menu_save).setVisible(false);
        if (current instanceof IHistory)
            menu.findItem(R.id.ml_menu_clean).setVisible(!((IHistory) current).isEmpty());
        boolean showLast = current instanceof AudioBrowserFragment || current instanceof VideoGridFragment;
        menu.findItem(R.id.ml_menu_last_playlist).setVisible(showLast);
        menu.findItem(R.id.ml_menu_filter).setVisible(current instanceof Filterable && ((Filterable) current).enableSearchOption());
        LogUtil.d(TAG, "viewmode getScreenRotation:" + getScreenRotation());
        menu.findItem(R.id.ml_menu_view_mode).setVisible(current instanceof VideoGridFragment &&
                ((getScreenRotation() == Surface.ROTATION_0) || (getScreenRotation() == Surface.ROTATION_180)));
//        if (viewModeMenu != null) {
//            boolean screenVertical = (getScreenRotation() == Surface.ROTATION_0) || (getScreenRotation() == Surface.ROTATION_180);
//            int currentViewMode = mSettings.getInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE, VideoListAdapter.VIEW_MODE_DEFAULT);
//            if (screenVertical) {
//                if (currentViewMode == VideoListAdapter.VIEW_MODE_LIST) {
//                    viewModeMenu.setIcon(R.drawable.ic_view_list);
//                } else if (currentViewMode == VideoListAdapter.VIEW_MODE_GRID) {
//                    viewModeMenu.setIcon(R.drawable.ic_view_grid);
//                } else if (currentViewMode == VideoListAdapter.VIEW_MODE_BIGPIC) {
//                    viewModeMenu.setIcon(R.drawable.ic_view_bigpic);
//                }
//                if (current instanceof VideoGridFragment && currentViewMode != VideoListAdapter.VIEW_MODE_GRID) {
//                    ((VideoGridFragment) current).toggleVideoMode(currentViewMode);
//                }
//            } else {
//                if (current instanceof VideoGridFragment) {
//                    ((VideoGridFragment) current).toggleVideoMode(VideoListAdapter.VIEW_MODE_GRID);
//                }
//            }
//            viewModeMenu.setVisible(current instanceof VideoGridFragment && screenVertical);
//        }
        return true;
    }

    private int getScreenRotation() {
        WindowManager wm = (WindowManager) VLCApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        try {
            Method m = display.getClass().getDeclaredMethod("getRotation");
            return (Integer) m.invoke(display);
        } catch (Exception e) {
            return Surface.ROTATION_0;
        }
    }

    /**
     * Handle onClick form menu buttons
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UiTools.setKeyboardVisibility(mDrawerLayout, false);

        // Current fragment loaded
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);

        switch (item.getItemId()) {
            case R.id.ml_menu_sortby_name:
                StatisticsManager.submitHomeTab(this, StatisticsManager.TYPE_SORTBY_NAME, null);
                break;
            case R.id.ml_menu_sortby_length:
                StatisticsManager.submitHomeTab(this, StatisticsManager.TYPE_SORTBY_LENGTH, null);
                break;
            case R.id.ml_menu_sortby_date:
                StatisticsManager.submitHomeTab(this, StatisticsManager.TYPE_SORTBY_DATE, null);
                break;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.ml_menu_sortby_name:
            case R.id.ml_menu_sortby_length:
            case R.id.ml_menu_sortby_date:
                if (current == null)
                    break;
                if (current instanceof ISortable) {
                    int sortBy = VideoListAdapter.SORT_BY_TITLE;
                    if (item.getItemId() == R.id.ml_menu_sortby_length)
                        sortBy = VideoListAdapter.SORT_BY_LENGTH;
                    else if (item.getItemId() == R.id.ml_menu_sortby_date)
                        sortBy = VideoListAdapter.SORT_BY_DATE;
                    ((ISortable) current).sortBy(sortBy);
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.ml_menu_equalizer:

                StatisticsManager.submitHomeTab(this, StatisticsManager.TYPE_EQUALIZER, null);

                showSecondaryFragment(SecondaryActivity.EQUALIZER);
                break;
            // Refresh
            case R.id.ml_menu_refresh:

                StatisticsManager.submitHomeTab(this, StatisticsManager.TYPE_REFRESH, null);

                forceRefresh(current);
                break;
            case R.id.ml_menu_search:

                StatisticsManager.submitHomeTab(this, StatisticsManager.TYPE_SEARCH, null);

                startActivity(new Intent(Intent.ACTION_SEARCH, null, this, SearchActivity.class));
                break;
            // Restore last playlist
            case R.id.ml_menu_last_playlist:

                StatisticsManager.submitHomeTab(this, StatisticsManager.TYPE_LAST_PLAYLIST, null);

                boolean audio = current instanceof AudioBrowserFragment;
                Intent i = new Intent(audio ? PlaybackService.ACTION_REMOTE_LAST_PLAYLIST :
                        PlaybackService.ACTION_REMOTE_LAST_VIDEO_PLAYLIST);
                sendBroadcast(i);
                break;
            case android.R.id.home:
                // Slide down the audio player.
                if (slideDownAudioPlayer())
                    break;
                /* Toggle the sidebar */
                if (mDrawerToggle.onOptionsItemSelected(item)) {
                    return true;
                }
                break;
            case R.id.ml_menu_clean:
                if (current instanceof IHistory)
                    ((IHistory) current).clearHistory();
                break;
            case R.id.ml_menu_save:
                if (current == null)
                    break;
                ((NetworkBrowserFragment) current).toggleFavorite();
                item.setIcon(R.drawable.ic_menu_bookmark_w);
                break;
            case R.id.ml_menu_view_mode:
                if (current == null)
                    break;
                ((VideoGridFragment) current).toggleViewMode(item);
                break;
        }
        mDrawerLayout.closeDrawer(mNavigationView);
        return super.onOptionsItemSelected(item);
    }

    public void forceRefresh() {
        forceRefresh(getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder));
    }

    private void forceRefresh(Fragment current) {
        if (!mMediaLibrary.isWorking()) {
            if (current != null && current instanceof IRefreshable)
                ((IRefreshable) current).refresh();
            else
                startService(new Intent(MediaParsingService.ACTION_RELOAD, null, this, MediaParsingService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT_PREFERENCES) {
            switch (resultCode) {
                case PreferencesActivity.RESULT_RESCAN:
                    for (Fragment fragment : getSupportFragmentManager().getFragments())
                        if (fragment instanceof MediaBrowserFragment)
                            ((MediaBrowserFragment) fragment).clear();
                    startService(new Intent(MediaParsingService.ACTION_RELOAD, null, this, MediaParsingService.class));
                    break;
                case PreferencesActivity.RESULT_RESTART:
                case PreferencesActivity.RESULT_RESTART_APP:
                    Intent intent = new Intent(MainActivity.this, resultCode == PreferencesActivity.RESULT_RESTART_APP ? StartActivity.class : MainActivity.class);
                    finish();
                    startActivity(intent);
                    break;
            }
        } else if (requestCode == ACTIVITY_RESULT_OPEN && resultCode == RESULT_OK) {
            MediaUtils.openUri(this, data.getData());
        } else if (requestCode == ACTIVITY_RESULT_SECONDARY) {
            if (resultCode == PreferencesActivity.RESULT_RESCAN) {
                forceRefresh(getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder));
            }
        }
    }

    // Note. onKeyDown will not occur while moving within a list
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Filter for LG devices, see https://code.google.com/p/android/issues/detail?id=78154
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
                (Build.VERSION.SDK_INT <= 16) &&
                (Build.MANUFACTURER.compareTo("LGE") == 0)) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            MenuItemCompat.expandActionView(mMenu.findItem(R.id.ml_menu_filter));
        }
        return super.onKeyDown(keyCode, event);
    }

    // Note. onKeyDown will not occur while moving within a list
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Filter for LG devices, see https://code.google.com/p/android/issues/detail?id=78154
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
                (Build.VERSION.SDK_INT <= 16) &&
                (Build.MANUFACTURER.compareTo("LGE") == 0)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void reloadPreferences() {
        mCurrentFragmentId = mSettings.getInt("fragment_id", R.id.nav_video);
    }

    @Override
    public Cursor runQuery(final CharSequence constraint) {
        return null;
    }

    //Filtering
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String filterQueryString) {
        if (filterQueryString.length() < 3)
            return false;
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
        if (current instanceof Filterable) {
            ((Filterable) current).getFilter().filter(filterQueryString);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        setSearchVisibility(true);
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        setSearchVisibility(false);
        restoreCurrentList();
        return true;
    }

    public void closeSearchView() {
        if (mMenu != null)
            MenuItemCompat.collapseActionView(mMenu.findItem(R.id.ml_menu_filter));
    }

    public void openSearchActivity() {
        startActivity(new Intent(Intent.ACTION_SEARCH, null, this, SearchActivity.class)
                .putExtra(SearchManager.QUERY, mSearchView.getQuery().toString()));
    }

    public void restoreCurrentList() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
        if (current instanceof Filterable) {
            ((Filterable) current).restoreList();
        }
    }

    private void setSearchVisibility(boolean visible) {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
        if (current instanceof Filterable)
            ((Filterable) current).setSearchVisibility(visible);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // This should not happen
        if (item == null)
            return false;

        getSupportActionBar().setTitle(null); //clear title
        getSupportActionBar().setSubtitle(null); //clear subtitle

        int id = item.getItemId();

        {
            // 统计上报
            switch (id) {
                case R.id.nav_video:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_VIDEO);
                    break;
                case R.id.nav_audio:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_AUDIO);
                    break;
                case R.id.nav_directories:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_DIRECT);
                    break;
                case R.id.nav_network:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_LOCALNET);
                    break;
                case R.id.nav_mrl:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_STREAM);
                    break;
                case R.id.nav_settings:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_SETTING);
                case R.id.nav_share_app:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_SHARE);
                    break;
                case R.id.nav_rate_app:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_RATE);
                    break;
                case R.id.nav_night_mode:
                    StatisticsManager.submitDrawlayout(this, StatisticsManager.TYPE_NIGHTMODE);
                    break;
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment current = fm.findFragmentById(R.id.fragment_placeholder);

        if (item.getGroupId() == PLUGIN_NAVIGATION_GROUP) {
            mExtensionManagerService.openExtension(id);
            mCurrentFragmentId = id;
        } else {
            if (mExtensionServiceConnection != null)
                mExtensionManagerService.disconnect();

            if (current == null) {
                mDrawerLayout.closeDrawer(mNavigationView);
                return false;
            }

            if (mCurrentFragmentId == id) { /* Already selected */
                // Go back at root level of current browser
                if (current instanceof BaseBrowserFragment && !((BaseBrowserFragment) current).isRootDirectory()) {
                    clearBackstackFromClass(current.getClass());
                } else {
                    mDrawerLayout.closeDrawer(mNavigationView);
                    return false;
                }
            }

            String tag = getTag(id);
            switch (id) {
                case R.id.nav_about:
                    showSecondaryFragment(SecondaryActivity.ABOUT);
                    break;
                case R.id.nav_theme:
                    new ThemeFragment().show(getSupportFragmentManager(), "theme");
                    break;
                case R.id.nav_night_mode:
                    toggleNightMode();
                    break;
                case R.id.nav_settings:
                    startActivityForResult(new Intent(this, PreferencesActivity.class), ACTIVITY_RESULT_PREFERENCES);
                    break;
                case R.id.nav_mrl:
                    new MRLPanelFragment().show(getSupportFragmentManager(), "fragment_mrl");
                    break;
                case R.id.nav_share_app:
                    shareApp();
                    break;
                case R.id.nav_rate_app:
                    startActivity(new Intent(MainActivity.this, DialogActivity.class).setAction(DialogActivity.KEY_RATE)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case R.id.nav_directories:
                    if (TextUtils.equals(BuildConfig.FLAVOR_target, "chrome")) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("audio/* video/*");
                        startActivityForResult(intent, ACTIVITY_RESULT_OPEN);
                        mDrawerLayout.closeDrawer(mNavigationView);
                        return true;
                    }
                default:
                /* Slide down the audio player */
                    slideDownAudioPlayer();

                /* Switch the fragment */
                    Fragment fragment = getFragment(id);
                    fm.beginTransaction()
                            .replace(R.id.fragment_placeholder, fragment, tag)
                            .addToBackStack(tag)
                            .commit();
                    mCurrentFragmentId = id;
            }
        }
        mNavigationView.setCheckedItem(mCurrentFragmentId);
        mDrawerLayout.closeDrawer(mNavigationView);
        return true;
    }

    private void clearBackstackFromClass(Class clazz) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_placeholder);
        while (clazz.isInstance(current)) {
            if (!fm.popBackStackImmediate())
                break;
            current = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_placeholder);
        }
    }

    private String getTag(int id) {
        switch (id) {
            case R.id.nav_about:
                return ID_ABOUT;
            case R.id.nav_settings:
                return ID_PREFERENCES;
            case R.id.nav_audio:
                return ID_AUDIO;
            case R.id.nav_directories:
                return ID_DIRECTORIES;
            case R.id.nav_history:
                return ID_HISTORY;
            case R.id.nav_mrl:
                return ID_MRL;
            case R.id.nav_network:
                return ID_NETWORK;
            case R.id.nav_theme:
                return ID_THEME;
            case R.id.nav_night_mode:
                return ID_NIGHT_MODE;
            case R.id.nav_share_app:
                return ID_SHARE;
            case R.id.nav_rate_app:
                return ID_RATE;
            default:
                return ID_VIDEO;
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.searchButton)
            openSearchActivity();
    }

    private void toggleNightMode() {
        boolean enabled = mSettings.getBoolean(PreferencesActivity.KEY_ENABLE_NIGHT_THEME, false);
        mSettings.edit().putBoolean(PreferencesActivity.KEY_ENABLE_NIGHT_THEME, !enabled).apply();
        setResult(RESULT_RESTART);
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void shareApp() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                VLCApplication.getAppResources().getString(R.string.share_app_text));
        shareIntent.setType("text/plain");

        // 设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "Share To"));
    }

    /**
     * 对appwall做预加载，建议开发者使用，会提高收入
     */
    public void preloadWall() {
//        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
//        Map<String, Object> preloadMap = new HashMap<String, Object>();
//        preloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_APPWALL);
//        preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, ADConstants.mobvista_library_roate_offer_wall);
//        preloadMap.put(MobVistaConstans.PRELOAD_RESULT_LISTENER, new PreloadListener() {
//            @Override
//            public void onPreloadSucceed() {
//                LogUtil.d(TAG, "onPreloadSucceed");
//                VLCApplication.runOnMainThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRotateAD.setVisibility(View.VISIBLE);
//                    }
//                });
//            }
//
//            @Override
//            public void onPreloadFaild(String s) {
//                LogUtil.d(TAG, "onPreloadFaild");
//            }
//        });
//        sdk.preload(preloadMap);
    }

    /**
     * 通过intent打开appwall
     */
    public void openWall() {
//        try {
//            Class<?> aClass = Class.forName("com.mobvista.msdk.shell.MVActivity");
//            Intent intent = new Intent(this, aClass);
//            intent.putExtra(MobVistaConstans.PROPERTIES_UNIT_ID, ADConstants.mobvista_library_roate_offer_wall);
//            this.startActivity(intent);
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
    }

    /**
     * 初始化广告view
     */
    private void initAD() {
//        mRotateAD = (RotateAD) findViewById(R.id.act_main_roate_ad);
//        mRotateAD.setOnClick(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openWall();
//                StatisticsManager.submitAd(MainActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_LIBRARY_NAME);
//            }
//        });
    }

}
