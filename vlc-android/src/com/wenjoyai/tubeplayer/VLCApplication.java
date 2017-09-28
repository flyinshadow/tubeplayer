/*****************************************************************************
 * VLCApplication.java
 *****************************************************************************
 * Copyright © 2010-2013 VLC authors and VideoLAN
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
package com.wenjoyai.tubeplayer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.videolan.libvlc.Dialog;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.medialibrary.LogUtil;
import org.videolan.medialibrary.Medialibrary;

import com.google.android.gms.ads.MobileAds;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.wenjoyai.tubeplayer.ad.ADConstants;
import com.wenjoyai.tubeplayer.ad.ADManager;
import com.wenjoyai.tubeplayer.gui.DialogActivity;
import com.wenjoyai.tubeplayer.gui.RateFragment;
import com.wenjoyai.tubeplayer.gui.dialogs.VlcProgressDialog;
import com.wenjoyai.tubeplayer.gui.helpers.AudioUtil;
import com.wenjoyai.tubeplayer.gui.helpers.BitmapCache;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.Strings;
import com.wenjoyai.tubeplayer.util.Util;
import com.wenjoyai.tubeplayer.util.VLCInstance;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VLCApplication extends Application {
    public final static String TAG = "VLC/VLCApplication";

    public final static String ACTION_MEDIALIBRARY_READY = "VLC/VLCApplication";
    private static VLCApplication instance;
    private static Medialibrary sMedialibraryInstance;

    public final static String SLEEP_INTENT = Strings.buildPkgString("SleepIntent");

    public static Calendar sPlayerSleepTime = null;

    private static boolean sTV;
    private static SharedPreferences mSettings;

    private static SimpleArrayMap<String, Object> sDataMap = new SimpleArrayMap<>();

    /* Up to 2 threads maximum, inactive threads are killed after 2 seconds */
    private final int maxThreads = Math.max(AndroidUtil.isJellyBeanMR1OrLater ? Runtime.getRuntime().availableProcessors() : 2, 1);
    public static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setPriority(Process.THREAD_PRIORITY_DEFAULT+Process.THREAD_PRIORITY_LESS_FAVORABLE);
            return thread;
        }
    };
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(Math.min(2, maxThreads), maxThreads, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private static int sDialogCounter = 0;

    private boolean mAppForeground = false;

    public static void setLocale(Context context){
        // Are we using advanced debugging - locale?
        String p = mSettings.getString("set_locale", "");
        if (!p.equals("")) {
            Locale locale;
            // workaround due to region code
            if(p.equals("zh-TW")) {
                locale = Locale.TRADITIONAL_CHINESE;
            } else if(p.startsWith("zh")) {
                locale = Locale.CHINA;
            } else if(p.equals("pt-BR")) {
                locale = new Locale("pt", "BR");
            } else if(p.equals("bn-IN") || p.startsWith("bn")) {
                locale = new Locale("bn", "IN");
            } else {
                /**
                 * Avoid a crash of
                 * java.lang.AssertionError: couldn't initialize LocaleData for locale
                 * if the user enters nonsensical region codes.
                 */
                if(p.contains("-"))
                    p = p.substring(0, p.indexOf('-'));
                locale = new Locale(p);
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            context.getResources().updateConfiguration(config,
                    context.getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        setLocale(this);

        //监听程序进入前台、后台
        ForegroundCallbacks.init(this);
        ForegroundCallbacks.get().addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                mAppForeground = true;
            }

            @Override
            public void onBecameBackground() {
                mAppForeground = false;
            }
        });

        runBackground(new Runnable() {
            @Override
            public void run() {
                // Prepare cache folder constants
                AudioUtil.prepareCacheFolder(instance);

                sTV = AndroidDevices.isAndroidTv() || !AndroidDevices.hasTsp();

                if (!VLCInstance.testCompatibleCPU(instance))
                    return;
                Dialog.setCallbacks(VLCInstance.get(), mDialogCallbacks);

                // Disable remote control receiver on Fire TV.
                if (!AndroidDevices.hasTsp())
                    AndroidDevices.setRemoteControlReceiverEnabled(false);
                /**     初始化广告     */
                Looper.prepare();
                MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
                Map<String, String> map = sdk.getMVConfigurationMap(ADConstants.APP_ID,ADConstants.APP_KEY);
                sdk.init(map, VLCApplication.this);

                //初始化google广告
                MobileAds.initialize(instance, ADManager.GOOGLE_APP_ID);
                Looper.loop();
            }
        });
    }

    public static boolean sWillShowRate = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLocale(this);

        // 切换到横屏时提示
        if (mAppForeground && !DialogActivity.sRateStarted && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            long lastTime = mSettings.getLong(RateFragment.KEY_RATE_SHOW_LAST, 0);
            long nextTime = mSettings.getLong(RateFragment.KEY_RATE_SHOW_NEXT, 0);
            int count = mSettings.getInt(RateFragment.KEY_RATE_SHOW_COUNT, 0);
            int versionCode = mSettings.getInt(RateFragment.KEY_RATE_LAST_VERSION, 0);
            long currentTime = new Date().getTime();
            LogUtil.d(TAG, "rate tip, currentTime:" + currentTime + "(" + Util.millisToDate(currentTime) + ")" +
                    " lastTime:" + lastTime + "(" + Util.millisToDate(lastTime) + ")" +
                    " nextTime:" + nextTime + "(" + Util.millisToDate(nextTime) + ")" +
                    " count:" + count
                );

            sWillShowRate = false;
            if (nextTime == -1) {
                // 本版本不提示
                LogUtil.d(TAG, "rate tip will not show this version");
            } else if (nextTime == 0) {
                // 可以提示
                LogUtil.d(TAG, "rate tip can show NOW");
                sWillShowRate = true;
                showRateDialog();
            } else if (nextTime > 0) {
                 // 到提示时间
                if (currentTime - nextTime >= 0) {
                    LogUtil.d(TAG, "rate tip reach time, can show NOW");
                    sWillShowRate = true;
                    showRateDialog();
                } else {
                    LogUtil.d(TAG, "rate tip not reach time");
                }
            }
        }
    }

    private void showRateDialog() {
        startActivity(new Intent(instance, DialogActivity.class).setAction(DialogActivity.KEY_RATE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * Called when the overall system is running low on memory
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "System is running low on memory");

        BitmapCache.getInstance().clear();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.w(TAG, "onTrimMemory, level: "+level);

        BitmapCache.getInstance().clear();
    }

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext() {
        return instance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources()
    {
        return instance.getResources();
    }

    public static boolean showTvUi() {
        return sTV || mSettings.getBoolean("tv_ui", false);
    }

    public static void runBackground(Runnable runnable) {
        instance.mThreadPool.execute(runnable);
    }

    public static void runOnMainThread(Runnable runnable) {
        instance.mHandler.post(runnable);
    }

    public static void runOnMainThreadDelay(Runnable runnable, long delay) {
        instance.mHandler.postDelayed(runnable, delay);
    }

    public static boolean removeTask(Runnable runnable) {
        return instance.mThreadPool.remove(runnable);
    }

    public static void storeData(String key, Object data) {
        sDataMap.put(key, data);
    }

    public static Object getData(String key) {
        return sDataMap.remove(key);
    }

    public static void clearData() {
        sDataMap.clear();
    }

    Dialog.Callbacks mDialogCallbacks = new Dialog.Callbacks() {
        @Override
        public void onDisplay(Dialog.ErrorMessage dialog) {
            Log.w(TAG, "ErrorMessage "+dialog.getText());
        }

        @Override
        public void onDisplay(Dialog.LoginDialog dialog) {
            String key = DialogActivity.KEY_LOGIN + sDialogCounter++;
            fireDialog(dialog, key);
        }

        @Override
        public void onDisplay(Dialog.QuestionDialog dialog) {
            String key = DialogActivity.KEY_QUESTION + sDialogCounter++;
            fireDialog(dialog, key);
        }

        @Override
        public void onDisplay(Dialog.ProgressDialog dialog) {
            String key = DialogActivity.KEY_PROGRESS + sDialogCounter++;
            fireDialog(dialog, key);
        }

        @Override
        public void onCanceled(Dialog dialog) {
            if (dialog != null && dialog.getContext() != null)
                ((DialogFragment)dialog.getContext()).dismiss();
        }

        @Override
        public void onProgressUpdate(Dialog.ProgressDialog dialog) {
            VlcProgressDialog vlcProgressDialog = (VlcProgressDialog) dialog.getContext();
            if (vlcProgressDialog != null && vlcProgressDialog.isVisible())
                vlcProgressDialog.updateProgress();
        }
    };

    private void fireDialog(Dialog dialog, String key) {
        storeData(key, dialog);
        startActivity(new Intent(instance, DialogActivity.class).setAction(key)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static Medialibrary getMLInstance() {
        if (sMedialibraryInstance == null) {
            VLCInstance.get(); // ensure VLC is loaded before medialibrary
            sMedialibraryInstance = Medialibrary.getInstance(instance);
        }
        return sMedialibraryInstance;
    }

    public static int getVersionCode() {
        int versionCode = 0;
        PackageManager packageManager = VLCApplication.getAppContext().getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(VLCApplication.getAppContext().getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getVersionString() {
        String version = "";
        PackageManager manager = VLCApplication.getAppContext().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(VLCApplication.getAppContext().getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}
