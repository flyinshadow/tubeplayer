/*****************************************************************************
 * VideoPlayerActivity.java
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

package com.wenjoyai.tubeplayer.gui.video;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Presentation;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ViewStubCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.NativeAdScrollView;
import com.facebook.ads.NativeAdView;
import com.wenjoyai.tubeplayer.BuildConfig;
import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.ad.ADConstants;
import com.wenjoyai.tubeplayer.ad.ADManager;
import com.wenjoyai.tubeplayer.ad.GifAD;
import com.wenjoyai.tubeplayer.ad.Interstitial;
import com.wenjoyai.tubeplayer.ad.RotateAD;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.MainActivity;
import com.wenjoyai.tubeplayer.gui.PlaybackServiceActivity;
import com.wenjoyai.tubeplayer.gui.RateDialog;
import com.wenjoyai.tubeplayer.gui.ThemeFragment;
import com.wenjoyai.tubeplayer.gui.audio.PlaylistAdapter;
import com.wenjoyai.tubeplayer.gui.browser.FilePickerActivity;
import com.wenjoyai.tubeplayer.gui.browser.FilePickerFragment;
import com.wenjoyai.tubeplayer.gui.dialogs.AdvOptionsDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.BrightnessDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.JumpToTimeDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.PlaybackSpeedDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.SelectChapterDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.SleepTimerDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.TimerDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.VolumeDialog;
import com.wenjoyai.tubeplayer.gui.helpers.AsyncImageLoader;
import com.wenjoyai.tubeplayer.gui.helpers.AudioUtil;
import com.wenjoyai.tubeplayer.gui.helpers.OnRepeatListener;
import com.wenjoyai.tubeplayer.gui.helpers.SwipeDragItemTouchHelperCallback;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.gui.tv.audioplayer.AudioPlayerActivity;
import com.wenjoyai.tubeplayer.interfaces.IPlaybackSettingsController;
import com.wenjoyai.tubeplayer.media.MediaDatabase;
import com.wenjoyai.tubeplayer.media.MediaUtils;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.FileUtils;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.Permissions;
import com.wenjoyai.tubeplayer.util.ScreenUtils;
import com.wenjoyai.tubeplayer.util.Strings;
import com.wenjoyai.tubeplayer.util.SubtitlesDownloader;
import com.wenjoyai.tubeplayer.util.VLCInstance;
import com.wenjoyai.tubeplayer.widget.PlayerItemLayout;
import com.wenjoyai.tubeplayer.widget.PlayerScrollview;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.Tools;
import org.videolan.medialibrary.media.MediaWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VideoPlayerActivity extends AppCompatActivity implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener,
        IPlaybackSettingsController, PlaybackService.Client.Callback, PlaybackService.Callback,
        PlaylistAdapter.IPlayer, OnClickListener, View.OnLongClickListener, ScaleGestureDetector.OnScaleGestureListener {

    public final static String TAG = "VLC/VideoPlayerActivity";

    // Internal intent identifier to distinguish between internal launch and
    // external intent.
    public final static String PLAY_FROM_VIDEOGRID = Strings.buildPkgString("gui.video.PLAY_FROM_VIDEOGRID");
    public final static String PLAY_FROM_SERVICE = Strings.buildPkgString("gui.video.PLAY_FROM_SERVICE");
    public final static String EXIT_PLAYER = Strings.buildPkgString("gui.video.EXIT_PLAYER");

    public final static String PLAY_EXTRA_ITEM_LOCATION = "item_location";
    public final static String PLAY_EXTRA_SUBTITLES_LOCATION = "subtitles_location";
    public final static String PLAY_EXTRA_ITEM_TITLE = "title";
    public final static String PLAY_EXTRA_FROM_START = "from_start";
    public final static String PLAY_EXTRA_START_TIME = "position";
    public final static String PLAY_EXTRA_OPENED_POSITION = "opened_position";
    public final static String PLAY_DISABLE_HARDWARE = "disable_hardware";

    public static final String PLAY_EXTRA_THUMB_TRANSITION_NAME = "player_thumb_transition_name";
    public static final String PLAY_EXTRA_ITEM = "player_item";

    public final static String ACTION_RESULT = Strings.buildPkgString("player.result");
    public final static String EXTRA_POSITION = "extra_position";
    public final static String EXTRA_DURATION = "extra_duration";
    public final static String EXTRA_URI = "extra_uri";
    public final static int RESULT_CONNECTION_FAILED = RESULT_FIRST_USER + 1;
    public final static int RESULT_PLAYBACK_ERROR = RESULT_FIRST_USER + 2;
    public final static int RESULT_HARDWARE_ACCELERATION_ERROR = RESULT_FIRST_USER + 3;
    public final static int RESULT_VIDEO_TRACK_LOST = RESULT_FIRST_USER + 4;
    private static final float DEFAULT_FOV = 80f;
    public static final float MIN_FOV = 20f;
    public static final float MAX_FOV = 150f;

    private final PlaybackServiceActivity.Helper mHelper = new PlaybackServiceActivity.Helper(this, this);
    protected PlaybackService mService;
    private Medialibrary mMedialibrary;
    private TextureView mTextureView = null;
    private SurfaceView mSurfaceView = null;
    private SurfaceView mSubtitlesSurfaceView = null;
    private View mRootView;
    private FrameLayout mSurfaceFrame;
    protected MediaRouter mMediaRouter;
    private MediaRouter.SimpleCallback mMediaRouterCallback;
    private SecondaryDisplay mPresentation;
    private int mPresentationDisplayId = -1;
    private Uri mUri;
    private boolean mAskResume = true;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mDetector = null;

    private ImageView mPlaylistToggle;
    private RecyclerView mPlaylist;
    private PlaylistAdapter mPlaylistAdapter;
    private ImageView mPlaylistNext;
    private ImageView mPlaylistPrevious;

    private ImageView mPopupPlayToggle;
    private ImageView mGoBack;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private int mCurrentSize;

    private SharedPreferences mSettings;

    private static final int TOUCH_FLAG_AUDIO_VOLUME = 1;
    private static final int TOUCH_FLAG_BRIGHTNESS = 1 << 1;
    private static final int TOUCH_FLAG_SEEK = 1 << 2;
    private int mTouchControls = 0;

    /**
     * Overlay
     */
    private ActionBar mActionBar;
    private ViewGroup mActionBarView;
    private View mOverlayProgress;
    private View mOverlayBackground;
    private View mOverlayButtons;
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int OVERLAY_INFINITE = -1;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int FADE_OUT_INFO = 3;
    private static final int START_PLAYBACK = 4;
    private static final int AUDIO_SERVICE_CONNECTION_FAILED = 5;
    private static final int RESET_BACK_LOCK = 6;
    private static final int CHECK_VIDEO_TRACKS = 7;
    private static final int LOADING_ANIMATION = 8;
    private static final int SHOW_INFO = 9;
    private static final int HIDE_INFO = 10;

    private static final int LOADING_ANIMATION_DELAY = 1000;

    private boolean mDragging;
    private boolean mShowing;
    private DelayState mPlaybackSetting = DelayState.OFF;
    private SeekBar mSeekbar;
    private TextView mTitle;
    private TextView mSysTime;
    private TextView mBattery;
    private TextView mTime;
    private TextView mLength;
    private TextView mInfo;
    private View mOverlayInfo;
    private View mVerticalBar;
    private View mVerticalBarProgress;
    private boolean mIsLoading;
    private boolean mIsPlaying = false;
    private ImageView mLoading;
    private ImageView mTipsBackground;
    private ImageView mPlayPause;
    private ImageView mTracks;
    private ImageView mNavMenu;
    private ImageView mRewind;
    private ImageView mForward;
    //    private ImageView mAdvOptions;
    private ImageView mPlaybackSettingPlus;
    private ImageView mPlaybackSettingMinus;
    private View mObjectFocused;
    private boolean mEnableBrightnessGesture;
    protected boolean mEnableCloneMode;
    private boolean mDisplayRemainingTime;
    private int mScreenOrientation;
    private int mScreenOrientationLock;
    private int mCurrentScreenOrientation;
    private ImageView mLock;
    //    private ImageView mSize;
    private String KEY_REMAINING_TIME_DISPLAY = "remaining_time_display";
    private String KEY_BLUETOOTH_DELAY = "key_bluetooth_delay";
    private long mSpuDelay = 0L;
    private long mAudioDelay = 0L;
    private boolean mRateHasChanged = false;
    private int mCurrentAudioTrack = -2, mCurrentSpuTrack = -2;

    private boolean mIsLocked = false;
    /* -1 is a valid track (Disable) */
    private int mLastAudioTrack = -2;
    private int mLastSpuTrack = -2;
    private int mOverlayTimeout = 0;
    private boolean mLockBackButton = false;
    boolean mWasPaused = false;
    private long mSavedTime = -1;
    private float mSavedRate = 1.f;

    /**
     * For uninterrupted switching between audio and video mode
     */
    private boolean mSwitchingView;
    private boolean mSwitchToPopup;
    private boolean mHasSubItems = false;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private boolean mMute = false;
    private int mVolSave;
    private float mVol;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_MOVE = 3;
    private static final int TOUCH_SEEK = 4;
    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange, mSurfaceXDisplayRange;
    private float mFov;
    private float mInitTouchY, mTouchY = -1f, mTouchX = -1f;

    //stick event
    private static final int JOYSTICK_INPUT_DELAY = 300;
    private long mLastMove;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;
    private float mRestoreAutoBrightness = -1f;

    // Tracks & Subtitles
    private MediaPlayer.TrackDescription[] mAudioTracksList;
    private MediaPlayer.TrackDescription[] mSubtitleTracksList;
    /**
     * Used to store a selected subtitle; see onActivityResult.
     * It is possible to have multiple custom subs in one session
     * (just like desktop VLC allows you as well.)
     */
    private volatile ArrayList<String> mSubtitleSelectedFiles = new ArrayList<>();

    /**
     * Flag to indicate whether the media should be paused once loaded
     * (e.g. lock screen, or to restore the pause state)
     */
    private boolean mPlaybackStarted = false;

    // Tips
    private View mOverlayTips;
    private static final String PREF_TIPS_SHOWN = "video_player_tips_shown";

    // Navigation handling (DVD, Blu-Ray...)
    private int mMenuIdx = -1;
    private boolean mIsNavMenu = false;

    /* for getTime and seek */
    private long mForcedTime = -1;
    private long mLastTime = -1;

    private OnLayoutChangeListener mOnLayoutChangeListener;
    private AlertDialog mAlertDialog;

    DisplayMetrics mScreen = new DisplayMetrics();

    protected boolean mIsBenchmark = false;

    //广告
    private RotateAD mRotateAD;
    private boolean mIsAdLoadSuc = false;
    private Interstitial mInterstitial;

    //包括关闭按钮
    private FrameLayout mNativeFrameLayout;
    private FrameLayout mNativeContainer;

    // 转场动画相关
    private View mPlayerVideoContainer;
    private View mPlayerMediaThumbContainer;
    private MediaWrapper mSharedItem;
    private String mSharedTransitionName;
    private boolean mWillTransition = false;
    private boolean mTransitionEnd = false;
    Transition mSharedElementEnterTransition;
    Transition.TransitionListener mTransitionListener;
    private boolean mTextureViewCreated;

    private long mPlayTime = 0;
    private long mPlayLength = 0;

    private GifAD mGifImageView;


    /**************************  new  *********************/
    //菜单栏
    private PlayerScrollview mMenuScrollview;
    private LinearLayout mMenuLinearLayout;

    /**************************  new  *********************/


    private static LibVLC LibVLC() {
        return VLCInstance.get();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.e(TAG, "--- onCreate");
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        applyTheme();
        super.onCreate(savedInstanceState);


        if (!VLCInstance.testCompatibleCPU(this)) {
            exit(RESULT_CANCELED);
            return;
        }

        supportPostponeEnterTransition();

        mSharedItem = (MediaWrapper) (savedInstanceState != null ?
                savedInstanceState.getParcelable(PLAY_EXTRA_ITEM) :
                (getIntent() != null ? getIntent().getParcelableExtra(PLAY_EXTRA_ITEM) : null));
        mSharedTransitionName = (savedInstanceState != null ? savedInstanceState.getString(PLAY_EXTRA_THUMB_TRANSITION_NAME) :
                (getIntent() != null ? getIntent().getStringExtra(PLAY_EXTRA_THUMB_TRANSITION_NAME) : null));

        if (AndroidUtil.isJellyBeanMR1OrLater) {
            // Get the media router service (Miracast)
            mMediaRouter = (MediaRouter) VLCApplication.getAppContext().getSystemService(Context.MEDIA_ROUTER_SERVICE);
//            LogUtil.d(TAG, "MediaRouter information : " + mMediaRouter  .toString());
        }

        if (!VLCApplication.showTvUi()) {
            mTouchControls = (mSettings.getBoolean("enable_volume_gesture", true) ? TOUCH_FLAG_AUDIO_VOLUME : 0)
                    + (mSettings.getBoolean("enable_brightness_gesture", true) ? TOUCH_FLAG_BRIGHTNESS : 0)
                    + (mSettings.getBoolean("enable_double_tap_seek", true) ? TOUCH_FLAG_SEEK : 0);
        }

        /* Services and miscellaneous */
        mAudioManager = (AudioManager) VLCApplication.getAppContext().getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mEnableCloneMode = mSettings.getBoolean("enable_clone_mode", false);
        createPresentation();

        mWillTransition = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) &&
                (mSharedItem != null) && (mSharedTransitionName != null) && (mPresentation == null);

        if (mWillTransition) {
            setContentView(R.layout.textureview_player);
        } else {
            setContentView(mPresentation == null ? R.layout.player : R.layout.player_remote_control);
        }

        /** initialize Views an their Events */
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setBackgroundDrawable(null);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.player_action_bar);

        mRootView = findViewById(R.id.player_root);
        mActionBarView = (ViewGroup) mActionBar.getCustomView();
        mGifImageView = (GifAD) findViewById(R.id.main_gif_ad);
        if (null != mGifImageView) {
            mGifImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatisticsManager.submitAd(VideoPlayerActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_PLAY_GIF + "click");
                    mGifImageView.setVisibility(View.GONE);
                    mHandler.removeCallbacks(mGifRunnable);
                    loadPauseNative(false);
                    pause();
                }
            });
        }
//        Remove ActionBar extra space
//        Toolbar toolbar = (Toolbar)mActionBarView.getParent();
//        toolbar.setContentInsetsAbsolute(0, 0);

        mTitle = (TextView) mActionBarView.findViewById(R.id.player_overlay_title);
        if (!AndroidUtil.isJellyBeanOrLater) {
            mSysTime = (TextView) findViewById(R.id.player_overlay_systime);
            mBattery = (TextView) findViewById(R.id.player_overlay_battery);
        }

        mPlaylistToggle = (ImageView) findViewById(R.id.playlist_toggle);
        mPlaylist = (RecyclerView) findViewById(R.id.video_playlist);

//        mPopupPlayToggle = (ImageView) findViewById(R.id.popup_toggle);
        mTracks = (ImageView) findViewById(R.id.player_overlay_tracks);
        mTracks.setOnClickListener(this);

        mGoBack = (ImageView) findViewById(R.id.player_goback);


        /**************************  new  *********************/
        mMenuScrollview = (PlayerScrollview) findViewById(R.id.player_menu_scrollview);
        mMenuLinearLayout = (LinearLayout) findViewById(R.id.player_menu_ll);
        initMenu();


        /**************************  new  *********************/
        mScreenOrientation = Integer.valueOf(
                mSettings.getString("screen_orientation", "99" /*SCREEN ORIENTATION SENSOR*/));

        if (mWillTransition) {
            mTextureView = (TextureView) findViewById(R.id.player_surface);
            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    LogUtil.d(TAG, "onSurfaceTextureAvailable mService=" + mService + ", mTransitionEnd=" + mTransitionEnd);
                    if (mService != null && mTransitionEnd) {
                        if (!mSwitchingView) {
                            mHandler.sendEmptyMessage(START_PLAYBACK);
                        }
                    }
                    mTextureViewCreated = true;
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    LogUtil.d(TAG, "onSurfaceTextureDestroyed");
                    mTextureViewCreated = false;
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        } else {
            mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        }

        mSubtitlesSurfaceView = (SurfaceView) findViewById(R.id.subtitles_surface);
//        mSubtitlesSurfaceView.setAlpha(0);
//        mSubtitlesSurfaceView.setOpaque(false);

        if (mWillTransition) {
            mSubtitlesSurfaceView.setZOrderOnTop(true);
        } else {
            mSubtitlesSurfaceView.setZOrderMediaOverlay(true);
        }
        mSubtitlesSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);

        /* Loading view */
        mLoading = (ImageView) findViewById(R.id.player_overlay_loading);
        if (mPresentation != null)
            mTipsBackground = (ImageView) findViewById(R.id.player_remote_tips_background);
        dimStatusBar(true);
//        mHandler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY);

        mSwitchingView = false;

        mAskResume = mSettings.getBoolean("dialog_confirm_resume", false);
        mDisplayRemainingTime = mSettings.getBoolean(KEY_REMAINING_TIME_DISPLAY, false);
        // Clear the resume time, since it is only used for resumes in external
        // videos.
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
        // Also clear the subs list, because it is supposed to be per session
        // only (like desktop VLC). We don't want the custom subtitle files
        // to persist forever with this video.
        editor.putString(PreferencesActivity.VIDEO_SUBTITLE_FILES, null);
        // Paused flag - per session too, like the subs list.
        editor.remove(PreferencesActivity.VIDEO_PAUSED);
        editor.apply();

        IntentFilter filter = new IntentFilter();
        if (mBattery != null)
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(VLCApplication.SLEEP_INTENT);
        registerReceiver(mReceiver, filter);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 100 is the value for screen_orientation_start_lock
        setRequestedOrientation(getScreenOrientation(mScreenOrientation));
        // Extra initialization when no secondary display is detected
        if (mPresentation == null) {
            // Orientation
            // Tips
            if (!BuildConfig.DEBUG && !VLCApplication.showTvUi() && !mSettings.getBoolean(PREF_TIPS_SHOWN, false)) {
                ((ViewStubCompat) findViewById(R.id.player_overlay_tips)).inflate();
                mOverlayTips = findViewById(R.id.overlay_tips_layout);
            }

            //Set margins for TV overscan
            if (VLCApplication.showTvUi()) {
                int hm = getResources().getDimensionPixelSize(R.dimen.tv_overscan_horizontal);
                int vm = getResources().getDimensionPixelSize(R.dimen.tv_overscan_vertical);

                RelativeLayout uiContainer = (RelativeLayout) findViewById(R.id.player_ui_container);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) uiContainer.getLayoutParams();
                lp.setMargins(hm, 0, hm, vm);
                uiContainer.setLayoutParams(lp);

                LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) mTitle.getLayoutParams();
                titleParams.setMargins(0, vm, 0, 0);
                mTitle.setLayoutParams(titleParams);
            }
        }

        getWindowManager().getDefaultDisplay().getMetrics(mScreen);
        mSurfaceYDisplayRange = Math.min(mScreen.widthPixels, mScreen.heightPixels);
        mSurfaceXDisplayRange = Math.max(mScreen.widthPixels, mScreen.heightPixels);
        mCurrentScreenOrientation = getResources().getConfiguration().orientation;
        if (mIsBenchmark) {
            mCurrentSize = SURFACE_FIT_SCREEN;
        } else {
            mCurrentSize = mSettings.getInt(PreferencesActivity.VIDEO_RATIO, SURFACE_BEST_FIT);
        }
        mMedialibrary = VLCApplication.getMLInstance();

        initAD();
        if (ADManager.sLevel >= ADManager.Level_Big) {
            preloadWall();
        }
        //返回广告
        if (ADManager.sLevel >= ADManager.Level_Big) {//如果是level 3才会加载返回广告
            mHandler.postDelayed(mBackInterstitialRunnable, ADManager.back_ad_delay_time * 1000);
        }

        initPauseNative();
        //gif
        if (null != mGifImageView) {
            mHandler.postDelayed(mGifRunnable, 10 * 1000);
        }

        mTranstionAnimIn = AnimationUtils.loadAnimation(this, R.anim.pause_ad_left_in);
        mTranstionAnimOut = AnimationUtils.loadAnimation(this, R.anim.pause_ad_leave_right);

        mPlayerVideoContainer = findViewById(R.id.player_video_container);
        mPlayerMediaThumbContainer = findViewById(R.id.player_media_thumb_container);

        if (mWillTransition) {
            initTransition();
        } else {
            if (mPlayerVideoContainer != null) {
                mPlayerVideoContainer.setVisibility(View.VISIBLE);
            }
            if (mPlayerMediaThumbContainer != null) {
                mPlayerMediaThumbContainer.setVisibility(View.GONE);
            }
        }

    }

    public class PlayerMenuModel {
        public int drawableId;
        public String name;
        public MenuType menuType;
        public boolean isSelected;//是否变颜色

        public PlayerMenuModel(int drawableId, String name, MenuType menuType) {
            this.drawableId = drawableId;
            this.name = name;
            this.menuType = menuType;
        }
    }

    public enum MenuType {
        mute, speed, audio_play, timer, volume, brightness, ratio, rotate, ic_arrow_back
    }

    private Map<MenuType, PlayerItemLayout> mItemMap = new HashMap<>();

    private void initMenu() {
        List<PlayerMenuModel> list = new ArrayList<>();
        //mute audio
        list.add(new PlayerMenuModel(R.drawable.ic_player_mute, getString(R.string.mute), MenuType.mute));
        //speed
        list.add(new PlayerMenuModel(R.drawable.ic_player_speed, getString(R.string.speed), MenuType.speed));
        //后台播放 audio play
        list.add(new PlayerMenuModel(R.drawable.ic_player_audio, getString(R.string.audio_play), MenuType.audio_play));
        //timer
        list.add(new PlayerMenuModel(R.drawable.ic_player_timer, getString(R.string.timer), MenuType.timer));
        //volume
        list.add(new PlayerMenuModel(R.drawable.ic_player_volume, getString(R.string.volume), MenuType.volume));
        //亮度
        list.add(new PlayerMenuModel(R.drawable.ic_player_brightness, getString(R.string.brightness), MenuType.brightness));
        //ratio
        list.add(new PlayerMenuModel(R.drawable.ic_player_ratio, getString(R.string.ratio), MenuType.ratio));
        //rotate
        list.add(new PlayerMenuModel(R.drawable.ic_player_rotate, getString(R.string.rotate), MenuType.rotate));
        fillMenu(list);
    }

    private boolean isMux = false;

    private void fillMenu(List<PlayerMenuModel> list) {
        mMenuLinearLayout.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            PlayerMenuModel model = list.get(i);
            PlayerItemLayout playerItemLayout = new PlayerItemLayout(this);
            playerItemLayout.init(model);
            playerItemLayout.setTag(model);
            playerItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlayerMenuModel model = (PlayerMenuModel) view.getTag();
                    switch (model.menuType) {
                        case mute://静音
                            mItemMap.get(model.menuType).setSelected(false);
                            // TODO: 2017/12/2 后去当前是否静音模式，设置
                            if (!isMux) {

                            } else {

                            }
                            break;
                        case speed:
                            showFragment(ID_PLAYBACK_SPEED);
                            break;
                        case audio_play:
                            mItemMap.get(model.menuType).setSelected(false);
                            StatisticsManager.submitVideoPlay(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_EXTEND_PLAY_AS_AUDIO, null, null);
                            switchToAudioMode(true);
                            break;
                        case timer:
                            showFragment(ID_SLEEP);
                            break;
                        case volume:
                            showFragment(ID_VOLUME);
                            break;
                        case brightness:
                            showFragment(ID_BRIGHTNESS);
                            break;
                        case ratio:
                            resizeVideo();
                            break;
                        case rotate:
                            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            } else {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            }
                            break;

                    }
                }
            });
            mItemMap.put(model.menuType, playerItemLayout);
            //将布局填充器加载到LinearLayout中进行显示
            mMenuLinearLayout.addView(playerItemLayout);
        }
        //back view
        View backView = LayoutInflater.from(VideoPlayerActivity.this).inflate(R.layout.player_menu_item_back, null);
        backView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenuScrollview.left();
            }
        });
        mMenuLinearLayout.addView(backView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //填充的view
        View mView = new View(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(this, 180), ViewGroup.LayoutParams.MATCH_PARENT);
        mMenuLinearLayout.addView(mView, layoutParams);
        mMenuScrollview.right();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initTransition() {
        mPlayerVideoContainer.setVisibility(View.GONE);
        mPlayerMediaThumbContainer.setVisibility(View.VISIBLE);
        final ImageView thumb = (ImageView) findViewById(R.id.player_media_thumb);
        thumb.setTransitionName(mSharedTransitionName);

        mSharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
        mTransitionEnd = false;
        mTransitionListener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                LogUtil.d(TAG, "onTransitionStart");
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                LogUtil.d(TAG, "onTransitionEnd, mTextureViewCreated=" + mTextureViewCreated);

                mPlayerVideoContainer.setVisibility(View.VISIBLE);
                mSubtitlesSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                mSubtitlesSurfaceView.setZOrderOnTop(true);

                if (mTextureViewCreated && !mSwitchingView) {
                    mHandler.sendEmptyMessage(START_PLAYBACK);
                }
                mTransitionEnd = true;
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                LogUtil.d(TAG, "onTransitionCancel");
            }

            @Override
            public void onTransitionPause(Transition transition) {
                LogUtil.d(TAG, "onTransitionPause");
            }

            @Override
            public void onTransitionResume(Transition transition) {
                LogUtil.d(TAG, "onTransitionResume");
            }
        };
        mSharedElementEnterTransition.addListener(mTransitionListener);
        if (mSharedItem != null) {
            VLCApplication.runBackground(new Runnable() {
                @Override
                public void run() {
                    final Bitmap cover = AudioUtil.readCoverBitmap(Uri.decode(mSharedItem.getArtworkMrl()), 0);
                    VLCApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (cover != null) {
                                thumb.setImageDrawable(new BitmapDrawable(getResources(), cover));
                            } else {
                                thumb.setImageDrawable(AsyncImageLoader.DEFAULT_COVER_VIDEO_DRAWABLE);
                            }
                            supportStartPostponedEnterTransition();
                        }
                    });
                }
            });
        }
    }

    private Animation mTranstionAnimIn;
    private Animation mTranstionAnimOut;
    Runnable mBackInterstitialRunnable = new Runnable() {
        @Override
        public void run() {
            loadInterstitial();
        }
    };

    Runnable mGifRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mGifImageView) {
                if (ADManager.getInstance().mPauseManager != null && ADManager.getInstance().mPauseManager.isLoaded() && !ADManager.getInstance().mIsPauseADShown) {
                    mGifImageView.setVisibility(View.VISIBLE);
                    StatisticsManager.submitAd(VideoPlayerActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_PLAY_GIF + "shown");
                    mHandler.postDelayed(mGifHideRunnable, 60 * 1000);
                }
            }
        }
    };

    Runnable mGifHideRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mGifImageView) {
                StatisticsManager.submitAd(VideoPlayerActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_PLAY_GIF + "gone");
                mGifImageView.setVisibility(View.GONE);
            }
        }
    };


    @Override
    protected void onResume() {
        LogUtil.d(TAG, "---- onResume");
        super.onResume();
        /*
         * Set listeners here to avoid NPE when activity is closing
         */
        setHudClickListeners();

        if (mIsLocked && mScreenOrientation == 99)
            setRequestedOrientation(mScreenOrientationLock);
    }

    private void setHudClickListeners() {
        if (mSeekbar != null)
            mSeekbar.setOnSeekBarChangeListener(mSeekListener);
        if (mLock != null)
            mLock.setOnClickListener(this);
        if (mPlayPause != null)
            mPlayPause.setOnClickListener(this);
        if (mPlayPause != null)
            mPlayPause.setOnLongClickListener(this);
        if (mLength != null)
            mLength.setOnClickListener(this);
        if (mTime != null)
            mTime.setOnClickListener(this);
//        if (mSize != null)
//            mSize.setOnClickListener(this);
        if (mNavMenu != null)
            mNavMenu.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Permissions.PERMISSION_STORAGE_TAG: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadMedia();
                } else {
                    Permissions.showStoragePermissionDialog(this, true);
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.d(TAG, "---- onNewIntent");
        setIntent(intent);
        if (mPlaybackStarted && mService.getCurrentMediaWrapper() != null) {
            Uri uri = intent.hasExtra(PLAY_EXTRA_ITEM_LOCATION) ?
                    (Uri) intent.getExtras().getParcelable(PLAY_EXTRA_ITEM_LOCATION) : intent.getData();
            if (uri == null || uri.equals(mUri))
                return;
            if (TextUtils.equals("file", uri.getScheme()) && uri.getPath().startsWith("/sdcard")) {
                Uri convertedUri = FileUtils.convertLocalUri(uri);
                if (convertedUri == null || convertedUri.equals(mUri))
                    return;
                else
                    uri = convertedUri;
            }
            mUri = uri;
            mTitle.setText(mService.getCurrentMediaWrapper().getTitle());
            if (mPlaylist.getVisibility() == View.VISIBLE) {
                mPlaylistAdapter.setCurrentIndex(mService.getCurrentMediaPosition());
                mPlaylist.setVisibility(View.GONE);
            }
            showTitle();
            initUI();
            setPlaybackParameters();
            mForcedTime = mLastTime = -1;
            setOverlayProgress();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
//        LogUtil.e(TAG,"onPause");
        super.onPause();
        hideOverlay(true);
        if (mSeekbar != null)
            mSeekbar.setOnSeekBarChangeListener(null);
        if (mLock != null)
            mLock.setOnClickListener(null);
        if (mPlayPause != null)
            mPlayPause.setOnClickListener(null);
        if (mPlayPause != null)
            mPlayPause.setOnLongClickListener(null);
        if (mLength != null)
            mLength.setOnClickListener(null);
        if (mTime != null)
            mTime.setOnClickListener(null);
//        if (mSize != null)
//            mSize.setOnClickListener(null);

        /* Stop the earliest possible to avoid vout error */
        if (!isInPictureInPictureMode() && (isFinishing() || (AndroidDevices.isAndroidTv() && !requestVisibleBehind(true))))
            stopPlayback();
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
        stopPlayback();
        exitOK();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (!AndroidUtil.isHoneycombOrLater) {
            LogUtil.d(TAG, "onConfigurationChanged changeSurfaceLayout");
            changeSurfaceLayout();
        }
        super.onConfigurationChanged(newConfig);
        getWindowManager().getDefaultDisplay().getMetrics(mScreen);
        mCurrentScreenOrientation = newConfig.orientation;
        mSurfaceYDisplayRange = Math.min(mScreen.widthPixels, mScreen.heightPixels);
        mSurfaceXDisplayRange = Math.max(mScreen.widthPixels, mScreen.heightPixels);
        resetHudLayout();

//        if (mService != null && mService.isPlaying() && RateDialog.isShowing()) {
//            doPlayPause();
//        }
    }

    public void resetHudLayout() {
        if (mOverlayButtons == null)
            return;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mOverlayButtons.getLayoutParams();
        int orientation = getScreenOrientation(100);
        boolean portrait = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        // TODO: 2017/12/2
//        if (portrait) {
//            layoutParams.addRule(RelativeLayout.BELOW, R.id.player_overlay_length);
//            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
//            layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
//        } else {
//            layoutParams.addRule(RelativeLayout.BELOW, R.id.player_overlay_seekbar);
//            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.player_overlay_time);
//            layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.player_overlay_length);
//        }
        mOverlayButtons.setLayoutParams(layoutParams);
    }


    @Override
    protected void onStart() {
        LogUtil.e(TAG, "---- onStart");
        super.onStart();
        mMedialibrary.pauseBackgroundOperations();
        mHelper.onStart();
        if (mSettings.getBoolean("save_brightness", false)) {
            float brightness = mSettings.getFloat("brightness_value", -1f);
            if (brightness != -1f)
                setWindowBrightness(brightness);
        }
        IntentFilter filter = new IntentFilter(PLAY_FROM_SERVICE);
        filter.addAction(EXIT_PLAYER);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mServiceReceiver, filter);
        if (mBtReceiver != null) {
            IntentFilter btFilter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
            btFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBtReceiver, btFilter);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onStop() {
        LogUtil.e(TAG, "---- onStop");
        super.onStop();
        mMedialibrary.resumeBackgroundOperations();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceReceiver);

        if (mBtReceiver != null)
            unregisterReceiver(mBtReceiver);
        if (mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();
        if (!isFinishing() && mService != null && mService.isPlaying() &&
                mSettings.getBoolean(PreferencesActivity.VIDEO_BACKGROUND, false)) {
            switchToAudioMode(false);
        }

        cleanUI();
        stopPlayback();

        SharedPreferences.Editor editor = mSettings.edit();
        if (mSavedTime != -1)
            editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, mSavedTime);

        editor.putFloat(PreferencesActivity.VIDEO_RATE, mSavedRate);

        // Save selected subtitles
        String subtitleList_serialized = null;
        if (mSubtitleSelectedFiles.size() > 0) {
//            LogUtil.d(TAG, "Saving selected subtitle files");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(mSubtitleSelectedFiles);
                subtitleList_serialized = bos.toString();
            } catch (IOException e) {
            }
        }
        editor.putString(PreferencesActivity.VIDEO_SUBTITLE_FILES, subtitleList_serialized);
        editor.apply();

        restoreBrightness();

        if (mSubtitlesGetTask != null)
            mSubtitlesGetTask.cancel(true);

        if (mService != null)
            mService.removeCallback(this);
        mHelper.onStop();
    }

    private void restoreBrightness() {
        if (mRestoreAutoBrightness != -1f) {
            int brightness = (int) (mRestoreAutoBrightness * 255f);
            try {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        brightness);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Save brightness if user wants to
        if (mSettings.getBoolean("save_brightness", false)) {
            float brightness = getWindow().getAttributes().screenBrightness;
            if (brightness != -1f) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putFloat("brightness_value", brightness);
                editor.apply();
            }
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "---- onDestroy");
        if (null != mInterstitial) {
            mInterstitial.show();
        }

        LogUtil.d(TAG, "onDestroy mPlayTime=" + mPlayTime + ", mPlayLength=" + mPlayLength);
        // 播放进度2分钟以上提示评分
        if (mPlayTime >= 120000) {
            RateDialog.tryToShow(VLCApplication.getAppContext(), 5);
        }

        mHandler.removeCallbacks(mBackInterstitialRunnable);
        mHandler.removeCallbacks(mGifRunnable);
        mHandler.removeCallbacks(mGifHideRunnable);

        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);

        // Dismiss the presentation when the activity is not visible.
        if (mPresentation != null) {
//            LogUtil.i(TAG, "Dismissing presentation because the activity is no longer visible.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        mAudioManager = null;
    }

    /**
     * Add or remove MediaRouter callbacks. This is provided for version targeting.
     *
     * @param add true to add, false to remove
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void mediaRouterAddCallback(boolean add) {
        if (!AndroidUtil.isJellyBeanMR1OrLater || mMediaRouter == null
                || add == (mMediaRouterCallback != null))
            return;

        if (add) {
            mMediaRouterCallback = new MediaRouter.SimpleCallback() {
                @Override
                public void onRoutePresentationDisplayChanged(
                        MediaRouter router, MediaRouter.RouteInfo info) {
//                    LogUtil.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
                    final Display presentationDisplay = info.getPresentationDisplay();
                    final int newDisplayId = presentationDisplay != null ? presentationDisplay.getDisplayId() : -1;
                    if (newDisplayId != mPresentationDisplayId)
                        removePresentation();
                }
            };
            mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        } else {
            mMediaRouter.removeCallback(mMediaRouterCallback);
            mMediaRouterCallback = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void surfaceFrameAddLayoutListener(boolean add) {
        if (!AndroidUtil.isHoneycombOrLater || mSurfaceFrame == null
                || add == (mOnLayoutChangeListener != null))
            return;

        if (add) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.d(TAG, "mOnLayoutChangeListener changeSurfaceLayout");

                        changeSurfaceLayout();
                    }
                };

                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        /* changeSurfaceLayout need to be called after the layout changed */
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
            mSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
            LogUtil.d(TAG, "surfaceFrameAddLayoutListener changeSurfaceLayout");

            changeSurfaceLayout();
        } else {
            mSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void startPlayback() {
        /* start playback only when audio service and both surfaces are ready */
        if (mPlaybackStarted || mService == null)
            return;

//        LogUtil.d(TAG, "startPlayback");

        mSavedRate = 1.0f;
        mSavedTime = -1;
        mPlaybackStarted = true;

        final IVLCVout vlcVout = mService.getVLCVout();
        if (vlcVout.areViewsAttached()) {
            if (mService.isPlayingPopup())
                mService.stop();
            vlcVout.detachViews();
        }
        if (mPresentation == null) {
            if (mWillTransition) {
                vlcVout.setVideoView(mTextureView);
            } else {
                vlcVout.setVideoView(mSurfaceView);
            }
            vlcVout.setSubtitlesView(mSubtitlesSurfaceView);
        } else {
            vlcVout.setVideoView(mPresentation.mSurfaceView);
            vlcVout.setSubtitlesView(mPresentation.mSubtitlesSurfaceView);
        }
        vlcVout.addCallback(this);
        vlcVout.attachViews(this);
        mService.setVideoTrackEnabled(true);

        initUI();

        loadMedia();
        boolean ratePref = mSettings.getBoolean(PreferencesActivity.KEY_AUDIO_PLAYBACK_SPEED_PERSIST, true);
        mService.setRate(ratePref || mRateHasChanged ? mSettings.getFloat(PreferencesActivity.VIDEO_RATE, 1.0f) : 1.0F, false);

        initPlaylistUi();
    }

    private void initPlaylistUi() {
        if (mService.hasPlaylist()) {
            mPlaylistPrevious = (ImageView) findViewById(R.id.playlist_previous);
            if (mPlaylistPrevious == null)
                return; //player HUD not yet inflated
            mPlaylistNext = (ImageView) findViewById(R.id.playlist_next);
            mPlaylistAdapter = new PlaylistAdapter(this);
            mPlaylistAdapter.setService(mService);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mPlaylist.setLayoutManager(layoutManager);
            mPlaylistToggle.setVisibility(View.VISIBLE);
            mPlaylistPrevious.setVisibility(View.VISIBLE);
            mPlaylistNext.setVisibility(View.VISIBLE);
            mPlaylistToggle.setOnClickListener(VideoPlayerActivity.this);
            mPlaylistPrevious.setOnClickListener(VideoPlayerActivity.this);
            mPlaylistNext.setOnClickListener(VideoPlayerActivity.this);
            mSeekbar.setNextFocusUpId(mPlaylistToggle.getId());

            ItemTouchHelper.Callback callback = new SwipeDragItemTouchHelperCallback(mPlaylistAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mPlaylist);
            if (AndroidUtil.isJellyBeanMR1OrLater && TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
                mPlaylistPrevious.setImageResource(R.drawable.ic_playlist_next_circle);
                mPlaylistNext.setImageResource(R.drawable.ic_playlist_previous_circle);
            }
        }
    }

    private void initUI() {

        cleanUI();

        /* Dispatch ActionBar touch events to the Activity */
        mActionBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return true;
            }
        });

        surfaceFrameAddLayoutListener(true);

        /* Listen for changes to media routes. */
        mediaRouterAddCallback(true);

        if (mRootView != null)
            mRootView.setKeepScreenOn(true);

//        mPopupPlayToggle.setOnClickListener(this);
        mGoBack.setOnClickListener(this);
    }

    private void setPlaybackParameters() {
        if (mAudioDelay != 0L && mAudioDelay != mService.getAudioDelay())
            mService.setAudioDelay(mAudioDelay);
        else if (mBtReceiver != null && (mAudioManager.isBluetoothA2dpOn() || mAudioManager.isBluetoothScoOn()))
            toggleBtDelay(true);
        if (mSpuDelay != 0L && mSpuDelay != mService.getSpuDelay())
            mService.setSpuDelay(mSpuDelay);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void stopPlayback() {
        if (!mPlaybackStarted)
            return;

//        LogUtil.d("firstvideo", "stopPlayback");

        mWasPaused = !mService.isPlaying();
        if (!isFinishing()) {
            mCurrentAudioTrack = mService.getAudioTrack();
            mCurrentSpuTrack = mService.getSpuTrack();
        }

        if (mMute)
            mute(false);

        mPlaybackStarted = false;

        mService.setVideoTrackEnabled(false);
        mService.removeCallback(this);

        mHandler.removeCallbacksAndMessages(null);
        final IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.removeCallback(this);
        vlcVout.detachViews();

        if (mSwitchingView && mService != null) {
//            LogUtil.d(TAG, "mLocation = \"" + mUri + "\"");
            if (mSwitchToPopup) {
                mService.switchToPopup(mService.getCurrentMediaPosition());
            } else {
                mService.getCurrentMediaWrapper().addFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
                mService.showWithoutParse(mService.getCurrentMediaPosition());
            }
            return;
        }

        if (mService.isSeekable()) {
            mSavedTime = getTime();
            long length = mService.getLength();
            //remove saved position if in the last 5 seconds
            if (length - mSavedTime < 5000)
                mSavedTime = 0;
            else
                mSavedTime -= 2000; // go back 2 seconds, to compensate loading time
        }

        mSavedRate = mService.getRate();
        mRateHasChanged = mSavedRate != 1.0f;

        mService.setRate(1.0f, false);

        if (!(mSwitchingView && mSwitchToPopup)) {
            mService.stop();
        }
    }

    private void cleanUI() {

        if (mRootView != null)
            mRootView.setKeepScreenOn(false);

        if (mDetector != null) {
            mDetector.setOnDoubleTapListener(null);
            mDetector = null;
        }

        /* Stop listening for changes to media routes. */
        mediaRouterAddCallback(false);

        surfaceFrameAddLayoutListener(false);

        if (AndroidUtil.isICSOrLater)
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);

        mActionBarView.setOnTouchListener(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (data == null) return;

        if (data.hasExtra(FilePickerFragment.EXTRA_MRL)) {
            mService.addSubtitleTrack(Uri.parse(data.getStringExtra(FilePickerFragment.EXTRA_MRL)), true);
            VLCApplication.runBackground(new Runnable() {
                @Override
                public void run() {
                    MediaDatabase.getInstance().saveSlave(mService.getCurrentMediaLocation(), Media.Slave.Type.Subtitle, 2, data.getStringExtra(FilePickerFragment.EXTRA_MRL));
                }
            });
        } else {
            LogUtil.d(TAG, "Subtitle selection dialog was cancelled");
        }
    }

    public static void start(Context context, Uri uri) {
        start(context, uri, null, false, -1);
    }

    public static void start(Context context, Uri uri, boolean fromStart, ImageView sharedImageView, MediaWrapper media) {
        start(context, uri, null, fromStart, -1, sharedImageView, media);
    }

    public static void start(Context context, Uri uri, boolean fromStart) {
        start(context, uri, null, fromStart, -1);
    }

    public static void start(Context context, Uri uri, String title) {
        start(context, uri, title, false, -1);
    }

    public static void startOpened(Context context, Uri uri, int openedPosition) {
        start(context, uri, null, false, openedPosition);
    }

    public static void startOpened(Context context, Uri uri, int openedPosition, ImageView sharedImageView, MediaWrapper media) {
        start(context, uri, null, false, openedPosition, sharedImageView, media);
    }

    private static void start(Context context, Uri uri, String title, boolean fromStart, int openedPosition) {
        Intent intent = getIntent(context, uri, title, fromStart, openedPosition);

        context.startActivity(intent);
    }

    private static void start(Context context, Uri uri, String title, boolean fromStart, int openedPosition, ImageView sharedImageView, MediaWrapper media) {
        Intent intent = getIntent(context, uri, title, fromStart, openedPosition);
        if (sharedImageView != null && !TextUtils.isEmpty(ViewCompat.getTransitionName(sharedImageView))) {
            intent.putExtra(PLAY_EXTRA_ITEM, media);
            intent.putExtra(PLAY_EXTRA_THUMB_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    (Activity) context,
                    sharedImageView,
                    ViewCompat.getTransitionName(sharedImageView));

            context.startActivity(intent, options.toBundle());
        } else {
            context.startActivity(intent);
        }
    }

    public static Intent getIntent(String action, MediaWrapper mw, boolean fromStart, int openedPosition) {
        return getIntent(action, VLCApplication.getAppContext(), mw.getUri(), mw.getTitle(), fromStart, openedPosition);
    }

    @NonNull
    public static Intent getIntent(Context context, Uri uri, String title, boolean fromStart, int openedPosition) {
        return getIntent(PLAY_FROM_VIDEOGRID, context, uri, title, fromStart, openedPosition);
    }

    @NonNull
    public static Intent getIntent(String action, Context context, Uri uri, String title, boolean fromStart, int openedPosition) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.setAction(action);
        intent.putExtra(PLAY_EXTRA_ITEM_LOCATION, uri);
        intent.putExtra(PLAY_EXTRA_ITEM_TITLE, title);
        intent.putExtra(PLAY_EXTRA_FROM_START, fromStart);

        if (openedPosition != -1 || !(context instanceof Activity)) {
            if (openedPosition != -1)
                intent.putExtra(PLAY_EXTRA_OPENED_POSITION, openedPosition);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                if (mBattery == null)
                    return;
                int batteryLevel = intent.getIntExtra("level", 0);
                if (batteryLevel >= 50)
                    mBattery.setTextColor(Color.GREEN);
                else if (batteryLevel >= 30)
                    mBattery.setTextColor(Color.YELLOW);
                else
                    mBattery.setTextColor(Color.RED);
                mBattery.setText(String.format("%d%%", batteryLevel));
            } else if (action.equalsIgnoreCase(VLCApplication.SLEEP_INTENT)) {
                exitOK();
            }
        }
    };

    protected void exit(int resultCode) {
        if (isFinishing())
            return;
        Intent resultIntent = new Intent(ACTION_RESULT);
        if (mUri != null && mService != null) {
            if (AndroidUtil.isNougatOrLater)
                resultIntent.putExtra(EXTRA_URI, mUri.toString());
            else
                resultIntent.setData(mUri);
            resultIntent.putExtra(EXTRA_POSITION, mService.getTime());
            resultIntent.putExtra(EXTRA_DURATION, mService.getLength());
        }
        setResult(resultCode, resultIntent);
//        finish();
        if (mWillTransition) {
            if (mSharedElementEnterTransition != null) {
                mSharedElementEnterTransition.removeListener(mTransitionListener);
            }
            mPlayerVideoContainer.setVisibility(View.GONE);
            mPlayerMediaThumbContainer.setVisibility(View.VISIBLE);
        }
        supportFinishAfterTransition();
    }

    private void exitOK() {
        exit(RESULT_OK);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (mIsLoading)
            return false;
        showOverlay();
        return true;
    }

    @TargetApi(12) //only active for Android 3.1+
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (mIsLoading)
            return false;
        //Check for a joystick event
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) !=
                InputDevice.SOURCE_JOYSTICK ||
                event.getAction() != MotionEvent.ACTION_MOVE)
            return false;

        InputDevice mInputDevice = event.getDevice();

        float dpadx = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float dpady = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if (mInputDevice == null || Math.abs(dpadx) == 1.0f || Math.abs(dpady) == 1.0f)
            return false;

        float x = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X);
        float y = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y);
        float rz = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_RZ);

        if (System.currentTimeMillis() - mLastMove > JOYSTICK_INPUT_DELAY) {
            if (Math.abs(x) > 0.3) {
                if (VLCApplication.showTvUi()) {
                    navigateDvdMenu(x > 0.0f ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT);
                } else
                    seekDelta(x > 0.0f ? 10000 : -10000);
            } else if (Math.abs(y) > 0.3) {
                if (VLCApplication.showTvUi())
                    navigateDvdMenu(x > 0.0f ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
                else {
                    if (mIsFirstBrightnessGesture)
                        initBrightnessTouch();
                    changeBrightness(-y / 10f);
                }
            } else if (Math.abs(rz) > 0.3) {
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int delta = -(int) ((rz / 7) * mAudioMax);
                int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
                setAudioVolume(vol);
            }
            mLastMove = System.currentTimeMillis();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mLockBackButton) {
            mLockBackButton = false;
            mHandler.sendEmptyMessageDelayed(RESET_BACK_LOCK, 2000);
            Toast.makeText(VLCApplication.getAppContext(), getString(R.string.back_quit_lock), Toast.LENGTH_SHORT).show();
        } else if (mPlaylist.getVisibility() == View.VISIBLE) {
            togglePlaylist();
        } else if (mPlaybackSetting != DelayState.OFF) {
            endPlaybackSetting();
        } else if (VLCApplication.showTvUi() && mShowing && !mIsLocked) {
            hideOverlay(true);
        } else {
            exitOK();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B)
            return super.onKeyDown(keyCode, event);
        if (mPlaybackSetting != DelayState.OFF)
            return false;
        if (mIsLoading) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_S:
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    exitOK();
                    return true;
            }
            return false;
        }
        //Handle playlist d-pad navigation
        if (mPlaylist.hasFocus()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mPlaylistAdapter.setCurrentIndex(mPlaylistAdapter.getCurrentIndex() - 1);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mPlaylistAdapter.setCurrentIndex(mPlaylistAdapter.getCurrentIndex() + 1);
                    break;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_BUTTON_A:
                    mService.playIndex(mPlaylistAdapter.getCurrentIndex());
                    break;
            }
            return true;
        }
        if (mShowing || (mFov == 0f && keyCode == KeyEvent.KEYCODE_DPAD_DOWN))
            showOverlayTimeout(OVERLAY_TIMEOUT);
        switch (keyCode) {
            case KeyEvent.KEYCODE_F:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                seekDelta(10000);
                return true;
            case KeyEvent.KEYCODE_R:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                seekDelta(-10000);
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                seekDelta(60000);
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                seekDelta(-60000);
                return true;
            case KeyEvent.KEYCODE_BUTTON_A:
                if (mOverlayProgress != null && mOverlayProgress.getVisibility() == View.VISIBLE)
                    return false;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
                if (mIsNavMenu)
                    return navigateDvdMenu(keyCode);
                else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) //prevent conflict with remote control
                    return super.onKeyDown(keyCode, event);
                else
                    doPlayPause();
                return true;
            case KeyEvent.KEYCODE_O:
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_MENU:
                showAdvancedOptions();
                return true;
            case KeyEvent.KEYCODE_V:
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
            case KeyEvent.KEYCODE_BUTTON_X:
                onAudioSubClick(mTracks);
                return true;
            case KeyEvent.KEYCODE_N:
                showNavMenu();
                return true;
            case KeyEvent.KEYCODE_A:
                resizeVideo();
                return true;
            case KeyEvent.KEYCODE_M:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                updateMute();
                return true;
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                exitOK();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!mShowing) {
                    if (mFov == 0f)
                        seekDelta(-10000);
                    else
                        mService.updateViewpoint(-5f, 0f, 0f, 0f, false);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!mShowing) {
                    if (mFov == 0f)
                        seekDelta(10000);
                    else
                        mService.updateViewpoint(5f, 0f, 0f, 0f, false);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!mShowing) {
                    if (mFov == 0f)
                        showAdvancedOptions();
                    else
                        mService.updateViewpoint(0f, -5f, 0f, 0f, false);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (!mShowing && mFov != 0f) {
                    mService.updateViewpoint(0f, 5f, 0f, 0f, false);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (!mShowing) {
                    doPlayPause();
                    return true;
                }
            case KeyEvent.KEYCODE_ENTER:
                if (mIsNavMenu)
                    return navigateDvdMenu(keyCode);
                else
                    return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_J:
                delayAudio(-50000l);
                return true;
            case KeyEvent.KEYCODE_K:
                delayAudio(50000l);
                return true;
            case KeyEvent.KEYCODE_G:
                delaySubs(-50000l);
                return true;
            case KeyEvent.KEYCODE_H:
                delaySubs(50000l);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mMute) {
                    updateMute();
                    return true;
                } else
                    return false;
            case KeyEvent.KEYCODE_CAPTIONS:
                selectSubtitles();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean navigateDvdMenu(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mService.navigate(MediaPlayer.Navigate.Up);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mService.navigate(MediaPlayer.Navigate.Down);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mService.navigate(MediaPlayer.Navigate.Left);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mService.navigate(MediaPlayer.Navigate.Right);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_A:
                mService.navigate(MediaPlayer.Navigate.Activate);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void showAudioDelaySetting() {
        mPlaybackSetting = DelayState.AUDIO;
        showDelayControls();
    }

    @Override
    public void showSubsDelaySetting() {
        mPlaybackSetting = DelayState.SUBS;
        showDelayControls();
    }

    @Override
    public void showPlaybackSpeedSetting() {
        mPlaybackSetting = DelayState.SPEED;
        showDelayControls();
    }

    public void showDelayControls() {
        mTouchAction = TOUCH_NONE;
        if (mPresentation != null)
            showOverlayTimeout(OVERLAY_INFINITE);
        ViewStubCompat vsc = (ViewStubCompat) findViewById(R.id.player_overlay_settings_stub);
        if (vsc != null) {
            vsc.inflate();
            mPlaybackSettingPlus = (ImageView) findViewById(R.id.player_delay_plus);
            mPlaybackSettingMinus = (ImageView) findViewById(R.id.player_delay_minus);

        }
        mPlaybackSettingMinus.setOnClickListener(this);
        mPlaybackSettingPlus.setOnClickListener(this);
        mPlaybackSettingMinus.setOnTouchListener(new OnRepeatListener(this));
        mPlaybackSettingPlus.setOnTouchListener(new OnRepeatListener(this));
        mPlaybackSettingMinus.setVisibility(View.VISIBLE);
        mPlaybackSettingPlus.setVisibility(View.VISIBLE);
        mPlaybackSettingPlus.requestFocus();
        initPlaybackSettingInfo();
    }

    private void initPlaybackSettingInfo() {
        initInfoOverlay();
        UiTools.setViewVisibility(mVerticalBar, View.GONE);
        UiTools.setViewVisibility(mOverlayInfo, View.VISIBLE);
        String text = "";
        if (mPlaybackSetting == DelayState.AUDIO) {
            text += getString(R.string.audio_delay) + "\n";
            text += mService.getAudioDelay() / 1000l;
            text += " ms";
        } else if (mPlaybackSetting == DelayState.SUBS) {
            text += getString(R.string.spu_delay) + "\n";
            text += mService.getSpuDelay() / 1000l;
            text += " ms";
        } else if (mPlaybackSetting == DelayState.SPEED) {
            text += getString(R.string.playback_speed) + "\n";
            text += mService.getRate();
            text += " x";
        } else
            text += "0";
        mInfo.setText(text);
    }

    @Override
    public void endPlaybackSetting() {
        mTouchAction = TOUCH_NONE;
        mService.saveMediaMeta();
        if (mBtReceiver != null && mPlaybackSetting == DelayState.AUDIO
                && (mAudioManager.isBluetoothA2dpOn() || mAudioManager.isBluetoothScoOn())) {
            String msg = getString(R.string.audio_delay) + "\n"
                    + mService.getAudioDelay() / 1000l
                    + " ms";
            Snackbar sb = Snackbar.make(mInfo, msg, Snackbar.LENGTH_LONG);
            sb.setAction(R.string.save_bluetooth_delay, mBtSaveListener);
            sb.show();
        }
        mPlaybackSetting = DelayState.OFF;
        if (mPlaybackSettingMinus != null) {
            mPlaybackSettingMinus.setOnClickListener(null);
            mPlaybackSettingMinus.setVisibility(View.INVISIBLE);
        }
        if (mPlaybackSettingPlus != null) {
            mPlaybackSettingPlus.setOnClickListener(null);
            mPlaybackSettingPlus.setVisibility(View.INVISIBLE);
        }
        UiTools.setViewVisibility(mOverlayInfo, View.INVISIBLE);
        mInfo.setText("");
        if (mPlayPause != null)
            mPlayPause.requestFocus();
    }

    public void delayAudio(long delta) {
        initInfoOverlay();
        long delay = mService.getAudioDelay() + delta;
        mService.setAudioDelay(delay);
        mInfo.setText(getString(R.string.audio_delay) + "\n" + (delay / 1000l) + " ms");
        mAudioDelay = delay;
        if (mPlaybackSetting == DelayState.OFF) {
            mPlaybackSetting = DelayState.AUDIO;
            initPlaybackSettingInfo();
        }
    }

    public void delaySubs(long delta) {
        initInfoOverlay();
        long delay = mService.getSpuDelay() + delta;
        mService.setSpuDelay(delay);
        mInfo.setText(getString(R.string.spu_delay) + "\n" + (delay / 1000l) + " ms");
        mSpuDelay = delay;
        if (mPlaybackSetting == DelayState.OFF) {
            mPlaybackSetting = DelayState.SUBS;
            initPlaybackSettingInfo();
        }
    }

    public void changeSpeed(float delta) {
        initInfoOverlay();
        float rate = Math.round((mService.getRate() + delta) * 100f) / 100f;
        if (rate < 0.25f || rate > 4f)
            return;
        mService.setRate(rate, false);
        mInfo.setText(getString(R.string.playback_speed) + "\n" + rate + " x");
        if (mPlaybackSetting == DelayState.OFF) {
            mPlaybackSetting = DelayState.SPEED;
            initPlaybackSettingInfo();
        }
    }

    /**
     * Lock screen rotation
     */
    private void lockScreen() {
        if (mScreenOrientation != 100) {
            mScreenOrientationLock = getRequestedOrientation();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            else
                setRequestedOrientation(getScreenOrientation(100));
        }
        showInfo(R.string.locked, 1000);
        mLock.setImageResource(R.drawable.ic_locked_circle);
        mTime.setEnabled(false);
        mSeekbar.setEnabled(false);
        mLength.setEnabled(false);
//        mSize.setEnabled(false);
        if (mPlaylistNext != null)
            mPlaylistNext.setEnabled(false);
        if (mPlaylistPrevious != null)
            mPlaylistPrevious.setEnabled(false);
        hideOverlay(true);
        mLockBackButton = true;
        mIsLocked = true;
    }

    /**
     * Remove screen lock
     */
    private void unlockScreen() {
        if (mScreenOrientation != 100)
            setRequestedOrientation(mScreenOrientationLock);
        showInfo(R.string.unlocked, 1000);
        mLock.setImageResource(R.drawable.ic_lock_circle);
        mTime.setEnabled(true);
        mSeekbar.setEnabled(mService == null || mService.isSeekable());
        mLength.setEnabled(true);
//        mSize.setEnabled(true);
        if (mPlaylistNext != null)
            mPlaylistNext.setEnabled(true);
        if (mPlaylistPrevious != null)
            mPlaylistPrevious.setEnabled(true);
        mShowing = false;
        mIsLocked = false;
        showOverlay();
        mLockBackButton = false;
    }

    /**
     * Show text in the info view and vertical progress bar for "duration" milliseconds
     *
     * @param text
     * @param duration
     * @param barNewValue new volume/brightness value (range: 0 - 15)
     */
    private void showInfoWithVerticalBar(String text, int duration, int barNewValue) {
        showInfo(text, duration);
        if (mVerticalBarProgress == null)
            return;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mVerticalBarProgress.getLayoutParams();
        layoutParams.weight = barNewValue;
        mVerticalBarProgress.setLayoutParams(layoutParams);
        mVerticalBar.setVisibility(View.VISIBLE);
    }

    /**
     * Show text in the info view for "duration" milliseconds
     *
     * @param text
     * @param duration
     */
    private void showInfo(String text, int duration) {
        initInfoOverlay();
        UiTools.setViewVisibility(mVerticalBar, View.GONE);
        UiTools.setViewVisibility(mOverlayInfo, View.VISIBLE);
        mInfo.setText(text);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    private void initInfoOverlay() {
        ViewStubCompat vsc = (ViewStubCompat) findViewById(R.id.player_info_stub);
        if (vsc != null) {
            vsc.inflate();
            // the info textView is not on the overlay
            mInfo = (TextView) findViewById(R.id.player_overlay_textinfo);
            mOverlayInfo = findViewById(R.id.player_overlay_info);
            mVerticalBar = findViewById(R.id.verticalbar);
            mVerticalBarProgress = findViewById(R.id.verticalbar_progress);
        }
    }

    private void showInfo(int textid, int duration) {
        initInfoOverlay();
        UiTools.setViewVisibility(mVerticalBar, View.GONE);
        UiTools.setViewVisibility(mOverlayInfo, View.VISIBLE);
        mInfo.setText(textid);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    /**
     * hide the info view with "delay" milliseconds delay
     *
     * @param delay
     */
    private void hideInfo(int delay) {
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, delay);
    }

    /**
     * hide the info view
     */
    private void hideInfo() {
        hideInfo(0);
    }

    private void fadeOutInfo() {
        if (mOverlayInfo != null && mOverlayInfo.getVisibility() == View.VISIBLE) {
            mOverlayInfo.startAnimation(AnimationUtils.loadAnimation(
                    VideoPlayerActivity.this, android.R.anim.fade_out));
            UiTools.setViewVisibility(mOverlayInfo, View.INVISIBLE);
        }
    }

    /* PlaybackService.Callback */

    @Override
    public void update() {
        updateList();
    }

    @Override
    public void updateProgress() {
        if (mService != null) {
            long time = mService.getTime();
            long length = mService.getLength();
            LogUtil.d(TAG, "updateProgress time=" + time + ", length=" + length);
            if (time > 0)
                mPlayTime = time;
            if (length > 0)
                mPlayLength = length;
        }
    }

    @Override
    public void onMediaEvent(Media.Event event) {
        switch (event.type) {
            case Media.Event.ParsedChanged:
                updateNavStatus();
                break;
            case Media.Event.MetaChanged:
                break;
            case Media.Event.SubItemTreeAdded:
                mHasSubItems = true;
                break;
        }
    }

    @Override
    public void onMediaPlayerEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Opening:
                mHasSubItems = false;
                break;
            case MediaPlayer.Event.Playing:
                onPlaying();
                if (mRotateAD != null) {
                    mRotateAD.setVisibility(View.INVISIBLE);
                }
                if (mNativeFrameLayout != null && mNativeFrameLayout.getVisibility() == View.VISIBLE) {
                    mNativeFrameLayout.startAnimation(mTranstionAnimOut);
                    mNativeFrameLayout.setVisibility(View.GONE);
                }
                changeSpeedMenu(mService.getRate());
                break;
            case MediaPlayer.Event.Paused:
                updateOverlayPausePlay();
                if (mIsAdLoadSuc) {
                    mRotateAD.setVisibility(View.VISIBLE);
                }
                //pause 广告
                loadPauseNative(true);
                break;
            case MediaPlayer.Event.Stopped:
                exitOK();
                break;
            case MediaPlayer.Event.EndReached:
                /* Don't end the activity if the media has subitems since the next child will be
                 * loaded by the PlaybackService */
                if (!mHasSubItems)
                    endReached();
                break;
            case MediaPlayer.Event.EncounteredError:
                encounteredError();
                break;
            case MediaPlayer.Event.TimeChanged:
                break;
            case MediaPlayer.Event.Vout:
                LogUtil.d(TAG, "MediaPlayer.Event.Vout");
//                Toast.makeText(this, "Vout", Toast.LENGTH_SHORT).show();
                updateNavStatus();
                if (mMenuIdx == -1)
                    handleVout(event.getVoutCount());
                break;
            case MediaPlayer.Event.ESAdded:
                if (mMenuIdx == -1) {
                    MediaWrapper media = mMedialibrary.findMedia(mService.getCurrentMediaWrapper());
                    if (media == null)
                        return;
                    if (event.getEsChangedType() == Media.Track.Type.Audio) {
                        setESTrackLists();
                        int audioTrack = (int) media.getMetaLong(mMedialibrary, MediaWrapper.META_AUDIOTRACK);
                        if (audioTrack != 0 || mCurrentAudioTrack != -2)
                            mService.setAudioTrack(media.getId() == 0L ? mCurrentAudioTrack : audioTrack);
                    } else if (event.getEsChangedType() == Media.Track.Type.Text) {
                        setESTrackLists();
                        int spuTrack = (int) media.getMetaLong(mMedialibrary, MediaWrapper.META_SUBTITLE_TRACK);
                        if (spuTrack != 0 || mCurrentSpuTrack != -2)
                            mService.setSpuTrack(media.getId() == 0L ? mCurrentAudioTrack : spuTrack);
                    }
                }
            case MediaPlayer.Event.ESDeleted:
                if (mMenuIdx == -1 && event.getEsChangedType() == Media.Track.Type.Video) {
                    mHandler.removeMessages(CHECK_VIDEO_TRACKS);
                    mHandler.sendEmptyMessageDelayed(CHECK_VIDEO_TRACKS, 1000);
                }
                invalidateESTracks(event.getEsChangedType());
                break;
            case MediaPlayer.Event.ESSelected:
                if (event.getEsChangedType() == Media.VideoTrack.Type.Video) {
                    Media.VideoTrack vt = mService.getCurrentVideoTrack();
//                    changeSurfaceLayout();
                    if (vt != null)
                        mFov = vt.projection == Media.VideoTrack.Projection.Rectangular ? 0f : DEFAULT_FOV;
                }
                break;
            case MediaPlayer.Event.SeekableChanged:
                updateSeekable(event.getSeekable());
                break;
            case MediaPlayer.Event.PausableChanged:
                updatePausable(event.getPausable());
                break;
            case MediaPlayer.Event.Buffering:
                if (!mIsPlaying)
                    break;
                if (event.getBuffering() == 100f)
                    stopLoading();
//                else if (!mHandler.hasMessages(LOADING_ANIMATION) && !mIsLoading
//                        && mTouchAction != TOUCH_SEEK && !mDragging)
//                    mHandler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY);
                break;
        }
    }

    /**
     * Handle resize of the surface and the overlay
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mService == null)
                return true;

            switch (msg.what) {
                case FADE_OUT:
//                    LogUtil.d(TAG, "FADE_OUT");
                    hideOverlay(false);
                    break;
                case SHOW_PROGRESS:
                    int pos = setOverlayProgress();
//                    LogUtil.d(TAG, "SHOW_PROGRESS pos=" + pos);
                    if (canShowProgress()) {
                        msg = mHandler.obtainMessage(SHOW_PROGRESS);
                        mHandler.sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case FADE_OUT_INFO:
//                    LogUtil.d(TAG, "FADE_OUT_INFO");
                    fadeOutInfo();
                    break;
                case START_PLAYBACK:
                    startPlayback();
                    break;
                case AUDIO_SERVICE_CONNECTION_FAILED:
                    exit(RESULT_CONNECTION_FAILED);
                    break;
                case RESET_BACK_LOCK:
                    mLockBackButton = true;
                    break;
                case CHECK_VIDEO_TRACKS:
                    if (mService.getVideoTracksCount() < 1 && mService.getAudioTracksCount() > 0) {
                        LogUtil.i(TAG, "No video track, open in audio mode");
                        switchToAudioMode(true);
                    }
                    break;
                case LOADING_ANIMATION:
                    startLoading();
                    break;
                case HIDE_INFO:
//                    LogUtil.d(TAG, "HIDE_INFO");
                    hideOverlay(true);
                    break;
                case SHOW_INFO:
                    showOverlay();
                    break;
            }
            return true;
        }
    });

    private boolean canShowProgress() {
        return !mDragging && mShowing && mService != null && mService.isPlaying();
    }

    private void onPlaying() {
        if (!mIsPlaying) {
            LogUtil.d(TAG, "first Playing");

            mPlayerMediaThumbContainer.setVisibility(View.GONE);

            MediaWrapper mw = mService.getCurrentMediaWrapper();
            if (mw != null) {
                String path = (mw.getUri() != null) ? mw.getUri().getPath() : "";

                int width = mw.getWidth();
                int height = mw.getHeight();
                Media.VideoTrack vt = mService.getCurrentVideoTrack();
                if (vt != null && vt.width > 0 && vt.height > 0) {
                    width = vt.width;
                    height = vt.height;
                }
                if (mVideoWidth > 0 && mVideoHeight > 0) {
                    width = mVideoWidth;
                    height = mVideoHeight;
                }
                LogUtil.d(TAG, "TubeStatisticsManager path=" + path + ", width=" + width + ", height=" + height);
                if (mw.getType() == MediaWrapper.TYPE_VIDEO) {
                    String videoInfoType = StatisticsManager.getVideoInfoType(mService.getCurrentMediaWrapper(), width, height);
                    StatisticsManager.submitVideoPlaySuccess(this, FileUtils.getFileExt(path), videoInfoType);
                } else if (mw.getType() == MediaWrapper.TYPE_AUDIO) {
                    StatisticsManager.submitAudioPlay(this, StatisticsManager.TYPE_AUDIO_PLAY, FileUtils.getFileExt(path));
                }
                if (mw.getUri() != null && !TextUtils.equals(mw.getUri().getScheme(), "file")) {
                    StatisticsManager.submitVideoPlaySuccess(this, mw.getUri().getPath(), null);
                }
            }
//            showOverlay();
        }
        mIsPlaying = true;
        setPlaybackParameters();
        stopLoading();
        updateOverlayPausePlay();
        updateNavStatus();
        if (!mService.getCurrentMediaWrapper().hasFlag(MediaWrapper.MEDIA_PAUSED))
            mHandler.sendEmptyMessageDelayed(FADE_OUT, OVERLAY_TIMEOUT);
        else {
            mService.getCurrentMediaWrapper().removeFlags(MediaWrapper.MEDIA_PAUSED);
            mWasPaused = false;
        }
        setESTracks();
    }

    private void endReached() {
        if (mService == null)
            return;
        if (mService.getRepeatType() == PlaybackService.REPEAT_ONE) {
            seek(0);
            return;
        }
        if (mService.expand(false) == 0) {
            mHandler.removeMessages(LOADING_ANIMATION);
            mHandler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY);
            LogUtil.d(TAG, "Found a video playlist, expanding it");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadMedia();
                }
            });
        }
        //Ignore repeat 
        if (mService.getRepeatType() == PlaybackService.REPEAT_ALL && mService.getMediaListSize() == 1)
            exitOK();
    }

    private void encounteredError() {
        if (isFinishing())
            return;
        //We may not have the permission to access files
        if (AndroidUtil.isMarshMallowOrLater && mUri != null &&
                TextUtils.equals(mUri.getScheme(), "file") &&
                !Permissions.canReadStorage()) {
            Permissions.checkReadStoragePermission(this, true);
            return;
        }
        /* Encountered Error, exit player with a message */
        mAlertDialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        exit(RESULT_PLAYBACK_ERROR);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        exit(RESULT_PLAYBACK_ERROR);
                    }
                })
                .setTitle(R.string.encountered_error_title)
                .setMessage(R.string.encountered_error_message)
                .create();
        mAlertDialog.show();
    }

    private final Runnable mSwitchAudioRunnable = new Runnable() {
        @Override
        public void run() {
            if (mService.hasMedia()) {
                LogUtil.i(TAG, "Video track lost, switching to audio");
                mSwitchingView = true;
            }
            exit(RESULT_VIDEO_TRACK_LOST);
        }
    };

    private void handleVout(int voutCount) {
        mHandler.removeCallbacks(mSwitchAudioRunnable);

        final IVLCVout vlcVout = mService.getVLCVout();
        if (vlcVout.areViewsAttached() && voutCount == 0) {
            mHandler.postDelayed(mSwitchAudioRunnable, 4000);
        }
    }

    public void switchToPopupMode() {
        if (mService == null)
            return;
        mSwitchingView = true;
        mSwitchToPopup = true;
        cleanUI();
        exitOK();
    }

    public void switchToAudioMode(boolean showUI) {
        if (mService == null)
            return;
        mSwitchingView = true;
        // Show the MainActivity if it is not in background.
        if (showUI) {
            Intent i = new Intent(this, VLCApplication.showTvUi() ? AudioPlayerActivity.class : MainActivity.class);
            startActivity(i);
        } else
            mSettings.edit().putBoolean(PreferencesActivity.VIDEO_RESTORE, true).apply();
        exitOK();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using MediaPlayer API */
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                mService.setVideoAspectRatio(null);
                mService.setVideoScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mService.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (mCurrentSize == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mService.setVideoScale(scale);
                    mService.setVideoAspectRatio(null);
                } else {
                    mService.setVideoScale(0);
                    mService.setVideoAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mService.setVideoAspectRatio("16:9");
                mService.setVideoScale(0);
                break;
            case SURFACE_4_3:
                mService.setVideoAspectRatio("4:3");
                mService.setVideoScale(0);
                break;
            case SURFACE_ORIGINAL:
                mService.setVideoAspectRatio(null);
                mService.setVideoScale(1);
                break;
        }
    }

    @Override
    public boolean isInPictureInPictureMode() {
        return AndroidUtil.isNougatOrLater && super.isInPictureInPictureMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        LogUtil.d(TAG, "onPictureInPictureModeChanged changeSurfaceLayout");
        changeSurfaceLayout();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeSurfaceLayout() {
        LogUtil.d(TAG, "changeSurfaceLayout");
        int sw;
        int sh;

        // get screen size
        if (mPresentation == null) {
            sw = getWindow().getDecorView().getWidth();
            sh = getWindow().getDecorView().getHeight();
        } else {
            sw = mPresentation.getWindow().getDecorView().getWidth();
            sh = mPresentation.getWindow().getDecorView().getHeight();
        }

        // sanity check
        if (sw * sh == 0) {
            LogUtil.e(TAG, "Invalid surface size");
            return;
        }

        if (mService != null) {
            final IVLCVout vlcVout = mService.getVLCVout();
            vlcVout.setWindowSize(sw, sh);
        }

        SurfaceView surface;
        TextureView texture = mTextureView;
        SurfaceView subtitlesSurface;
        FrameLayout surfaceFrame;
        if (mPresentation == null) {
            surface = mSurfaceView;
            subtitlesSurface = mSubtitlesSurfaceView;
            surfaceFrame = mSurfaceFrame;
        } else {
            surface = mPresentation.mSurfaceView;
            subtitlesSurface = mPresentation.mSubtitlesSurfaceView;
            surfaceFrame = mPresentation.mSurfaceFrame;
        }
        LayoutParams lp;
        if (texture != null) {
            lp = texture.getLayoutParams();
        } else {
            lp = surface.getLayoutParams();
        }

        if (mVideoWidth * mVideoHeight == 0 || isInPictureInPictureMode()) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.MATCH_PARENT;
            if (texture != null) {
                texture.setLayoutParams(lp);
            } else {
                surface.setLayoutParams(lp);
            }
            lp = surfaceFrame.getLayoutParams();
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.MATCH_PARENT;
            surfaceFrame.setLayoutParams(lp);
            if (mService != null && mVideoWidth * mVideoHeight == 0)
                changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (mService != null && lp.width == lp.height && lp.width == LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mService.setVideoAspectRatio(null);
            mService.setVideoScale(0);
        }

        double dw = sw, dh = sh;
        boolean isPortrait;

        if (mPresentation == null) {
            // getWindow().getDecorView() doesn't always take orientation into account, we have to correct the values
            isPortrait = mCurrentScreenOrientation == Configuration.ORIENTATION_PORTRAIT;
        } else {
            isPortrait = false;
        }

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        if (texture != null) {
            texture.setLayoutParams(lp);
        } else {
            surface.setLayoutParams(lp);
        }
        subtitlesSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);

        LogUtil.d(TAG, "surfaceFrame setLayoutParams width=" + lp.width + ",height=" + lp.height);
        surfaceFrame.setLayoutParams(lp);

        if (texture != null) {
            texture.invalidate();
        } else {
            surface.invalidate();
        }
        subtitlesSurface.invalidate();
    }

    private void sendMouseEvent(int action, int button, int x, int y) {
        if (mService == null)
            return;
        final IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.sendMouseEvent(action, button, x, y);
    }

    /**
     * show/hide the overlay
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mNativeFrameLayout != null && mNativeFrameLayout.getVisibility() == View.VISIBLE) {
            mNativeFrameLayout.startAnimation(mTranstionAnimOut);
            mNativeFrameLayout.setVisibility(View.GONE);
        }
        if (mService == null)
            return false;
        if (mDetector == null) {
            mDetector = new GestureDetectorCompat(this, mGestureListener);
            mDetector.setOnDoubleTapListener(mGestureListener);
        }
        if (mFov != 0f && mScaleGestureDetector == null)
            mScaleGestureDetector = new ScaleGestureDetector(this, this);
        if (mPlaybackSetting != DelayState.OFF) {
            if (event.getAction() == MotionEvent.ACTION_UP)
                endPlaybackSetting();
            return true;
        } else if (mPlaylist.getVisibility() == View.VISIBLE) {
            togglePlaylist();
            return true;
        }
        if (mTouchControls == 0 || mIsLocked) {
            // locked or swipe disabled, only handle show/hide & ignore all actions
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!mShowing) {
                    showOverlay();
                } else {
                    hideOverlay(true);
                }
            }
            return false;
        }
        if (mFov != 0f && mScaleGestureDetector != null)
            mScaleGestureDetector.onTouchEvent(event);
        if ((mScaleGestureDetector != null && mScaleGestureDetector.isInProgress()) ||
                (mDetector != null && mDetector.onTouchEvent(event)))
            return true;

        float x_changed, y_changed;
        if (mTouchX != -1f && mTouchY != -1f) {
            y_changed = event.getRawY() - mTouchY;
            x_changed = event.getRawX() - mTouchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }

        // coef is the gradient's move to determine a neutral zone
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = ((x_changed / mScreen.xdpi) * 2.54f);
        float delta_y = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / mScreen.xdpi + 0.5f) * 2f);

        int xTouch = Math.round(event.getRawX());
        int yTouch = Math.round(event.getRawY());

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                // Audio
                mTouchY = mInitTouchY = event.getRawY();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mTouchAction = TOUCH_NONE;
                // Seek
                mTouchX = event.getRawX();
                // Mouse events for the core
                sendMouseEvent(MotionEvent.ACTION_DOWN, 0, xTouch, yTouch);
                break;

            case MotionEvent.ACTION_MOVE:
                // Mouse events for the core
                sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);

                if (mFov == 0f) {
                    // No volume/brightness action if coef < 2 or a secondary display is connected
                    //TODO : Volume action when a secondary display is connected
                    if (mTouchAction != TOUCH_SEEK && coef > 2 && mPresentation == null) {
                        if (Math.abs(y_changed / mSurfaceYDisplayRange) < 0.05)
                            return false;
                        mTouchY = event.getRawY();
                        mTouchX = event.getRawX();
                        // Volume (Up or Down - Right side)
                        if ((mTouchControls & TOUCH_FLAG_AUDIO_VOLUME) != 0 && (int) mTouchX > (4 * mScreen.widthPixels / 7f)) {
                            doVolumeTouch(y_changed);
                            hideOverlay(true);
                        }
                        // Brightness (Up or Down - Left side)
                        if ((mTouchControls & TOUCH_FLAG_BRIGHTNESS) != 0 && (int) mTouchX < (3 * mScreen.widthPixels / 7f)) {
                            doBrightnessTouch(y_changed);
                            hideOverlay(true);
                        }
                    } else {
                        // Seek (Right or Left move)
                        doSeekTouch(Math.round(delta_y), xgesturesize, false);
                    }
                } else {
                    mTouchY = event.getRawY();
                    mTouchX = event.getRawX();
                    mTouchAction = TOUCH_MOVE;
                    float yaw = mFov * -x_changed / (float) mSurfaceXDisplayRange;
                    float pitch = mFov * -y_changed / (float) mSurfaceXDisplayRange;
                    mService.updateViewpoint(yaw, pitch, 0, 0, false);
                }
                break;

            case MotionEvent.ACTION_UP:
                // Mouse events for the core
                sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);
                // Seek
                if (mTouchAction == TOUCH_SEEK)
                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
                mTouchX = -1f;
                mTouchY = -1f;
                break;
        }
        return mTouchAction != TOUCH_NONE;
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        if (coef == 0)
            coef = 1;
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (Math.abs(gesturesize) < 1 || !mService.isSeekable())
            return;

        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
            return;
        mTouchAction = TOUCH_SEEK;

        long length = mService.getLength();
        long time = getTime();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length))
            jump = (int) (length - time);
        if ((jump < 0) && ((time + jump) < 0))
            jump = (int) -time;

        //Jump !
        if (seek && length > 0)
            seek(time + jump, length);

        if (length > 0)
            //Show the jump's size
            showInfo(String.format("%s%s (%s)%s",
                    jump >= 0 ? "+" : "",
                    Tools.millisToString(jump),
                    Tools.millisToString(time + jump),
                    coef > 1 ? String.format(" x%.1g", 1.0 / coef) : ""), 50);
        else
            showInfo(R.string.unseekable_stream, 1000);
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
        float delta = -((y_changed / (float) mScreen.heightPixels) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol);
        }
    }

    private void setAudioVolume(int vol) {
        try {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
            int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (vol != newVol)
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);

            mTouchAction = TOUCH_VOLUME;
            vol = vol * 100 / mAudioMax;
            showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
        } catch (Exception e) {
            LogUtil.e(TAG, "setAudioVolume got exception: ", e);
        }
    }

    private void mute(boolean mute) {
        mMute = mute;
        if (mMute)
            mVolSave = mService.getVolume();
        mService.setVolume(mMute ? 0 : mVolSave);
    }

    private void updateMute() {
        mute(!mMute);
        showInfo(mMute ? R.string.sound_off : R.string.sound_on, 1000);
    }

    private void initBrightnessTouch() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                if (!Permissions.canWriteSettings(this)) {
                    Permissions.checkWriteSettingsPermission(this, Permissions.PERMISSION_SYSTEM_BRIGHTNESS);
                    return;
                }
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                mRestoreAutoBrightness = android.provider.Settings.System.getInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else if (brightnesstemp == 0.6f) {
                brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        lp.screenBrightness = brightnesstemp;
        getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return;
        if (mIsFirstBrightnessGesture) initBrightnessTouch();
        mTouchAction = TOUCH_BRIGHTNESS;

        // Set delta : 2f is arbitrary for now, it possibly will fillMenu in the future
        float delta = -y_changed / mSurfaceYDisplayRange;

        changeBrightness(delta);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);
        showInfoWithVerticalBar(getString(R.string.brightness) + "\n" + (int) brightness + '%', 1000, (int) brightness);
    }

    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        getWindow().setAttributes(lp);
    }

    /**
     * handle changes of the seekbar (slicer)
     */
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
            showOverlayTimeout(OVERLAY_INFINITE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            showOverlay(true);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!isFinishing() && fromUser && mService.isSeekable()) {
                seek(progress);
                setOverlayProgress();
                mTime.setText(Tools.millisToString(progress));
                showInfo(Tools.millisToString(progress), 1000);
            }
        }
    };

    public void onAudioSubClick(View anchor) {
        final AppCompatActivity context = this;
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.audiosub_tracks, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.video_menu_audio_track).setEnabled(mService.getAudioTracksCount() > 0);
        popupMenu.getMenu().findItem(R.id.video_menu_subtitles).setEnabled(mService.getSpuTracksCount() > 0);
        //FIXME network subs cannot be enabled & screen cast display is broken with picker
        popupMenu.getMenu().findItem(R.id.video_menu_subtitles_picker).setEnabled(mPresentation == null);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.video_menu_audio_track) {
                    selectAudioTrack();
                    return true;
                } else if (item.getItemId() == R.id.video_menu_subtitles) {
                    selectSubtitles();
                    return true;
                } else if (item.getItemId() == R.id.video_menu_subtitles_picker) {

                    StatisticsManager.submitVideoPlay(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_SELECT, null, null);

                    if (mUri == null)
                        return false;
                    Intent filePickerIntent = new Intent(context, FilePickerActivity.class);
                    filePickerIntent.setData(Uri.parse(FileUtils.getParent(mUri.toString())));
                    context.startActivityForResult(filePickerIntent, 0);
                    return true;
                } else if (item.getItemId() == R.id.video_menu_subtitles_download) {

                    StatisticsManager.submitVideoPlay(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_DOWNLOAD, null, null);

                    if (mUri == null)
                        return false;
                    MediaUtils.getSubs(VideoPlayerActivity.this, mService.getCurrentMediaWrapper(), new SubtitlesDownloader.Callback() {
                        @Override
                        public void onRequestEnded(boolean success) {
                            if (success)
                                getSubtitles();
                        }
                    });
                }
                hideOverlay(true);
                return false;
            }
        });
        popupMenu.show();
        showOverlay();
    }

    @Override
    public void onPopupMenu(View anchor, final int position) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.audio_player, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.audio_player_mini_remove) {
                    if (mService != null) {
                        mPlaylistAdapter.remove(position);
                        mService.remove(position);
                        return true;
                    }
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void updateList() {
        if (mService == null || mPlaylistAdapter == null)
            return;

        mPlaylistAdapter.update(mService.getMedias());
    }

    @Override
    public void onSelectionSet(int position) {
        mPlaylist.scrollToPosition(position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_overlay_play:

                StatisticsManager.submitVideoPlay(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_PAUSE, null, null);

                doPlayPause();
                break;
            case R.id.playlist_toggle:
                togglePlaylist();
                break;
            case R.id.playlist_next:
                mService.next();
                break;
            case R.id.playlist_previous:
                mService.previous(false);
                break;
            case R.id.player_overlay_forward:
                seekDelta(10000);
                break;
            case R.id.player_overlay_rewind:
                seekDelta(-10000);
                break;
            case R.id.lock_overlay_button:

                StatisticsManager.submitVideoPlay(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_LOCK, null, null);

                if (mIsLocked)
                    unlockScreen();
                else
                    lockScreen();
                break;
//            case R.id.player_overlay_size:
//                resizeVideo();
//                break;
            case R.id.player_overlay_navmenu:
                showNavMenu();
                break;
            case R.id.player_overlay_length:
            case R.id.player_overlay_time:
                mDisplayRemainingTime = !mDisplayRemainingTime;
                showOverlay();
                mSettings.edit().putBoolean(KEY_REMAINING_TIME_DISPLAY, mDisplayRemainingTime).apply();
                break;
            case R.id.player_delay_minus:
                if (mPlaybackSetting == DelayState.AUDIO)
                    delayAudio(-50000);
                else if (mPlaybackSetting == DelayState.SUBS)
                    delaySubs(-50000);
                else if (mPlaybackSetting == DelayState.SPEED)
                    changeSpeed(-0.05f);
                break;
            case R.id.player_delay_plus:
                if (mPlaybackSetting == DelayState.AUDIO)
                    delayAudio(50000);
                else if (mPlaybackSetting == DelayState.SUBS)
                    delaySubs(50000);
                else if (mPlaybackSetting == DelayState.SPEED)
                    changeSpeed(0.05f);
                break;
//            case R.id.player_overlay_adv_function:
//
//                StatisticsManager.submitVideoPlay(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_EXTEND, null, null);
//
//                showAdvancedOptions();
//                break;
            case R.id.player_overlay_tracks:
                onAudioSubClick(v);
                break;
            case R.id.popup_toggle:
                StatisticsManager.submitVideoPlay(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_POPUP, null, null);

                if (Permissions.canDrawOverlays(this)) {
                    LogUtil.d(TAG, "switchToPopupMode try to show RateDialog");
                    // 小窗提示评分
                    RateDialog.tryToShow(this, 5);

                    switchToPopupMode();
                } else {
                    Permissions.checkDrawOverlaysPermission(this);
                }
                break;
            case R.id.player_goback:
                exitOK();
                break;
        }
    }

    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.player_overlay_play:
                if (mService == null)
                    return false;
                if (mService.getRepeatType() == PlaybackService.REPEAT_ONE) {
                    showInfo(getString(R.string.repeat), 1000);
                    mService.setRepeatType(PlaybackService.REPEAT_NONE);
                } else {
                    mService.setRepeatType(PlaybackService.REPEAT_ONE);
                    showInfo(getString(R.string.repeat_single), 1000);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float diff = DEFAULT_FOV * (1 - detector.getScaleFactor());
        if (mService.updateViewpoint(0, 0, 0, diff, false)) {
            mFov = Math.min(Math.max(MIN_FOV, mFov + diff), MAX_FOV);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return mSurfaceXDisplayRange != 0 && mFov != 0f;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    private interface TrackSelectedListener {
        boolean onTrackSelected(int trackID);
    }

    private void selectTrack(final MediaPlayer.TrackDescription[] tracks, int currentTrack, int titleId,
                             final TrackSelectedListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener must not be null");
        if (tracks == null)
            return;
        final String[] nameList = new String[tracks.length];
        final int[] idList = new int[tracks.length];
        int i = 0;
        int listPosition = 0;
        for (MediaPlayer.TrackDescription track : tracks) {
            idList[i] = track.id;
            nameList[i] = track.name;
            // map the track position to the list position
            if (track.id == currentTrack)
                listPosition = i;
            i++;
        }

        if (!isFinishing()) {
            mAlertDialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                    .setTitle(titleId)
                    .setSingleChoiceItems(nameList, listPosition, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int listPosition) {
                            int trackID = -1;
                            // Reverse map search...
                            for (MediaPlayer.TrackDescription track : tracks) {
                                if (idList[listPosition] == track.id) {
                                    trackID = track.id;
                                    break;
                                }
                            }
                            listener.onTrackSelected(trackID);
                            dialog.dismiss();
                        }
                    })
                    .create();
            mAlertDialog.setCanceledOnTouchOutside(true);
            mAlertDialog.setOwnerActivity(VideoPlayerActivity.this);
            mAlertDialog.show();
        }
    }

    private void selectAudioTrack() {
        setESTrackLists();
        selectTrack(mAudioTracksList, mService.getAudioTrack(), R.string.track_audio,
                new TrackSelectedListener() {
                    @Override
                    public boolean onTrackSelected(int trackID) {
                        if (trackID < -1 || mService == null)
                            return false;
                        mService.setAudioTrack(trackID);
                        MediaWrapper mw = mMedialibrary.findMedia(mService.getCurrentMediaWrapper());
                        if (mw != null && mw.getId() != 0L)
                            mw.setLongMeta(mMedialibrary, MediaWrapper.META_AUDIOTRACK, trackID);
                        return true;
                    }
                });
    }

    private void selectSubtitles() {
        setESTrackLists();
        selectTrack(mSubtitleTracksList, mService.getSpuTrack(), R.string.track_text,
                new TrackSelectedListener() {
                    @Override
                    public boolean onTrackSelected(int trackID) {
                        if (trackID < -1 || mService == null)
                            return false;
                        mService.setSpuTrack(trackID);
                        MediaWrapper mw = mMedialibrary.findMedia(mService.getCurrentMediaWrapper());
                        if (mw != null && mw.getId() != 0L)
                            mw.setLongMeta(mMedialibrary, MediaWrapper.META_SUBTITLE_TRACK, trackID);
                        return true;
                    }
                });
    }

    private void showNavMenu() {
        if (mMenuIdx >= 0)
            mService.setTitleIdx(mMenuIdx);
    }

    private void updateSeekable(boolean seekable) {
        if (mRewind != null) {
            mRewind.setEnabled(seekable);
            mRewind.setImageResource(seekable
                    ? R.drawable.ic_rewind_circle
                    : R.drawable.ic_rewind_circle_disable_o);
        }
        if (mForward != null) {
            mForward.setEnabled(seekable);
            mForward.setImageResource(seekable
                    ? R.drawable.ic_forward_circle
                    : R.drawable.ic_forward_circle_disable_o);
        }
        if (!mIsLocked && mSeekbar != null)
            mSeekbar.setEnabled(seekable);
    }

    private void updatePausable(boolean pausable) {
        if (mPlayPause == null)
            return;
        mPlayPause.setEnabled(pausable);
        if (!pausable)
            mPlayPause.setImageResource(R.drawable.ic_play_circle_disable_o);
    }

    private void doPlayPause() {
        if (!mService.isPausable())
            return;

        if (mService.isPlaying()) {
            pause();
        } else {
            play();
            showOverlay(true);
        }
    }

    private long getTime() {
        long time = mService.getTime();
        if (mForcedTime != -1 && mLastTime != -1) {
            /* XXX: After a seek, mService.getTime can return the position before or after
             * the seek position. Therefore we return mForcedTime in order to avoid the seekBar
             * to move between seek position and the actual position.
             * We have to wait for a valid position (that is after the seek position).
             * to re-init mLastTime and mForcedTime to -1 and return the actual position.
             */
            if (mLastTime > mForcedTime) {
                if (time <= mLastTime && time > mForcedTime || time > mLastTime)
                    mLastTime = mForcedTime = -1;
            } else {
                if (time > mForcedTime)
                    mLastTime = mForcedTime = -1;
            }
        } else if (time == 0)
            time = (int) mService.getCurrentMediaWrapper().getTime();
        return mForcedTime == -1 ? time : mForcedTime;
    }

    protected void seek(long position) {
        seek(position, mService.getLength());
    }

    private void seek(long position, long length) {
        mForcedTime = position;
        mLastTime = mService.getTime();
        mService.seek(position, length);
        setOverlayProgress((int) position, (int) length);
    }

    private void seekDelta(int delta) {
        // unseekable stream
        if (mService.getLength() <= 0 || !mService.isSeekable()) return;

        long position = getTime() + delta;
        if (position < 0) position = 0;
        seek(position);
        StringBuilder sb = new StringBuilder();
        if (delta > 0f)
            sb.append('+');
        sb.append((int) (delta / 1000f))
                .append("s (")
                .append(Tools.millisToString(mService.getTime()))
                .append(')');
        showInfo(sb.toString(), 1000);
    }

    private void initSeekButton() {
        mRewind = (ImageView) findViewById(R.id.player_overlay_rewind);
        mForward = (ImageView) findViewById(R.id.player_overlay_forward);
        mRewind.setVisibility(View.VISIBLE);
        mForward.setVisibility(View.VISIBLE);
        mRewind.setOnClickListener(this);
        mForward.setOnClickListener(this);
        mRewind.setOnTouchListener(new OnRepeatListener(this));
        mForward.setOnTouchListener(new OnRepeatListener(this));
    }

    private void resizeVideo() {
        if (mCurrentSize < SURFACE_ORIGINAL) {
            mCurrentSize++;
        } else {
            mCurrentSize = 0;
        }
        LogUtil.d(TAG, "resizeVideo changeSurfaceLayout");

        changeSurfaceLayout();
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:

                StatisticsManager.submitSelectContent(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_RATIO, StatisticsManager.ITEM_VIDEO_RATIO_BEST_FIT);

                showInfo(R.string.surface_best_fit, 1000);
                break;
            case SURFACE_FIT_SCREEN:

                StatisticsManager.submitSelectContent(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_RATIO, StatisticsManager.ITEM_VIDEO_RATIO_FIT_SCREEN);

                showInfo(R.string.surface_fit_screen, 1000);
                break;
            case SURFACE_FILL:

                StatisticsManager.submitSelectContent(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_RATIO, StatisticsManager.ITEM_VIDEO_RATIO_FILL_SCREEN);

                showInfo(R.string.surface_fill, 1000);
                break;
            case SURFACE_16_9:

                StatisticsManager.submitSelectContent(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_RATIO, StatisticsManager.ITEM_VIDEO_RATIO_16_9);

                showInfo("16:9", 1000);
                break;
            case SURFACE_4_3:

                StatisticsManager.submitSelectContent(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_RATIO, StatisticsManager.ITEM_VIDEO_RATIO_4_3);

                showInfo("4:3", 1000);
                break;
            case SURFACE_ORIGINAL:

                StatisticsManager.submitSelectContent(VideoPlayerActivity.this, StatisticsManager.TYPE_VIDEO_RATIO, StatisticsManager.ITEM_VIDEO_RATIO_CENTER);

                showInfo(R.string.surface_original, 1000);
                break;
        }
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(PreferencesActivity.VIDEO_RATIO, mCurrentSize);
        editor.apply();
        showOverlay();
    }

    /**
     * show overlay
     *
     * @param forceCheck: adjust the timeout in function of playing state
     */
    private void showOverlay(boolean forceCheck) {
        if (forceCheck)
            mOverlayTimeout = 0;
        showOverlayTimeout(0);
    }

    /**
     * show overlay with the previous timeout value
     */
    private void showOverlay() {
        showOverlay(false);
    }

    /**
     * show overlay
     */
    private void showOverlayTimeout(int timeout) {
        if (mService == null)
            return;
        initOverlay();
        if (timeout != 0)
            mOverlayTimeout = timeout;
        if (mOverlayTimeout == 0)
            mOverlayTimeout = mService.isPlaying() ? OVERLAY_TIMEOUT : OVERLAY_INFINITE;
        if (mIsNavMenu) {
            mShowing = true;
            return;
        }
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        if (!mShowing) {
            mShowing = true;
            if (!mIsLocked) {
                mPlayPause.setVisibility(View.VISIBLE);
                UiTools.setViewVisibility(mTracks, View.VISIBLE);
//                UiTools.setViewVisibility(mAdvOptions, View.VISIBLE);
//                UiTools.setViewVisibility(mSize, View.VISIBLE);
                UiTools.setViewVisibility(mRewind, View.VISIBLE);
                UiTools.setViewVisibility(mForward, View.VISIBLE);
                UiTools.setViewVisibility(mPlaylistNext, View.VISIBLE);
                UiTools.setViewVisibility(mPlaylistPrevious, View.VISIBLE);
            }
            dimStatusBar(false);
            mOverlayProgress.setVisibility(View.VISIBLE);
            mMenuScrollview.setVisibility(View.VISIBLE);
            if (mPresentation != null) mOverlayBackground.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(FADE_OUT);
        if (mOverlayTimeout != OVERLAY_INFINITE)
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), mOverlayTimeout);
        updateOverlayPausePlay();
        if (mObjectFocused != null)
            mObjectFocused.requestFocus();
        else if (getCurrentFocus() == null)
            mPlayPause.requestFocus();
    }

    private void initOverlay() {
        ViewStubCompat vsc = (ViewStubCompat) findViewById(R.id.player_hud_stub);
        if (vsc != null) {
            vsc.inflate();
            mOverlayProgress = findViewById(R.id.progress_overlay);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) mOverlayProgress.getLayoutParams();
            if (AndroidDevices.isPhone() || !AndroidDevices.hasNavBar()) {
                layoutParams.width = LayoutParams.MATCH_PARENT;
            } else {
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            }
            mOverlayProgress.setLayoutParams(layoutParams);
            mOverlayBackground = findViewById(R.id.player_overlay_background);
            mOverlayButtons = findViewById(R.id.player_overlay_buttons);
            // Position and remaining time
            final boolean rtl = AndroidUtil.isJellyBeanMR1OrLater && TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
            mTime = (TextView) findViewById(rtl ? R.id.player_overlay_length : R.id.player_overlay_time);
            mLength = (TextView) findViewById(rtl ? R.id.player_overlay_time : R.id.player_overlay_length);
            mPlayPause = (ImageView) findViewById(R.id.player_overlay_play);
//            mTracks = (ImageView) findViewById(R.id.player_overlay_tracks);
//            mTracks.setOnClickListener(this);
            mPopupPlayToggle = (ImageView) findViewById(R.id.popup_toggle);
            mPopupPlayToggle.setOnClickListener(this);
//            mAdvOptions = (ImageView) findViewById(R.id.player_overlay_adv_function);
//            mAdvOptions.setOnClickListener(this);
            mLock = (ImageView) findViewById(R.id.lock_overlay_button);
//            mSize = (ImageView) findViewById(R.id.player_overlay_size);
            mNavMenu = (ImageView) findViewById(R.id.player_overlay_navmenu);
            if (mSettings.getBoolean("enable_seek_buttons", false))
                initSeekButton();
            mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);
            resetHudLayout();
            updateOverlayPausePlay();
            updateSeekable(mService.isSeekable());
            updatePausable(mService.isPausable());
            updateNavStatus();
            setHudClickListeners();
            initPlaylistUi();
        }
    }


    /**
     * hider overlay
     */
    private void hideOverlay(boolean fromUser) {
//        LogUtil.d(TAG, "hideOverlay fromUser=" + fromUser + " mShowing=" + mShowing);
        if (mShowing) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.removeMessages(SHOW_PROGRESS);
            LogUtil.i(TAG, "remove View!");
            mObjectFocused = getCurrentFocus();
            UiTools.setViewVisibility(mOverlayTips, View.INVISIBLE);
            if (!fromUser && !mIsLocked) {
                mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mMenuScrollview.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//                mPlayPause.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//                if (mTracks != null)
//                    mTracks.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
////                if (mAdvOptions != null)
////                    mAdvOptions.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//                if (mRewind != null)
//                    mRewind.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//                if (mForward != null)
//                    mForward.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//                if (mPlaylistNext != null)
//                    mPlaylistNext.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//                if (mPlaylistPrevious != null)
//                    mPlaylistPrevious.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
////                mSize.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
            }
            if (mPresentation != null) {
                mOverlayBackground.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mOverlayBackground.setVisibility(View.INVISIBLE);
            }
            mOverlayProgress.setVisibility(View.INVISIBLE);
            mMenuScrollview.setVisibility(View.GONE);
            mPlayPause.setVisibility(View.INVISIBLE);
            UiTools.setViewVisibility(mTracks, View.INVISIBLE);
//            UiTools.setViewVisibility(mAdvOptions, View.INVISIBLE);
//            UiTools.setViewVisibility(mSize, View.INVISIBLE);
            UiTools.setViewVisibility(mRewind, View.INVISIBLE);
            UiTools.setViewVisibility(mForward, View.INVISIBLE);
            UiTools.setViewVisibility(mPlaylistNext, View.INVISIBLE);
            UiTools.setViewVisibility(mPlaylistPrevious, View.INVISIBLE);
            mShowing = false;
            dimStatusBar(true);
        } else if (!fromUser) {
            /*
             * Try to hide the Nav Bar again.
             * It seems that you can't hide the Nav Bar if you previously
             * showed it in the last 1-2 seconds.
             */
            dimStatusBar(true);
        }
    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void dimStatusBar(boolean dim) {
        if (dim || mIsLocked)
            mActionBar.hide();
        else
            mActionBar.show();
        if (!AndroidUtil.isHoneycombOrLater || mIsNavMenu)
            return;
        int visibility = 0;
        int navbar = 0;

        if (AndroidUtil.isJellyBeanOrLater) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (dim || mIsLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                navbar |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            else
                visibility |= View.STATUS_BAR_HIDDEN;
            if (!AndroidDevices.hasCombBar()) {
                navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (AndroidUtil.isKitKatOrLater)
                    visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                if (AndroidUtil.isJellyBeanOrLater)
                    visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
        } else {
            mActionBar.show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
            else
                visibility |= View.STATUS_BAR_VISIBLE;
        }

        if (AndroidDevices.hasNavBar())
            visibility |= navbar;
        getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    private void showTitle() {
        if (!AndroidUtil.isHoneycombOrLater || mIsNavMenu)
            return;
        int visibility = 0;
        int navbar = 0;
        mActionBar.show();

        if (AndroidUtil.isJellyBeanOrLater) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (AndroidUtil.isICSOrLater)
            navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (AndroidDevices.hasNavBar())
            visibility |= navbar;
        getWindow().getDecorView().setSystemUiVisibility(visibility);

    }

    private void updateOverlayPausePlay() {
        if (mService == null || mPlayPause == null)
            return;
        if (mService.isPausable())
            mPlayPause.setImageResource(mService.isPlaying() ? R.drawable.ic_pause_circle
                    : R.drawable.ic_play_circle);
    }

    /**
     * update the overlay
     */
    private int setOverlayProgress() {
        if (mService == null) {
            return 0;
        }
        int time = (int) getTime();
        int length = (int) mService.getLength();
        return setOverlayProgress(time, length);
    }

    private int setOverlayProgress(int time, int length) {
        // Update all view elements
        if (mSeekbar != null) {
            mSeekbar.setMax(length);
            mSeekbar.setProgress(time);
        }
        if (mSysTime != null)
            mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
        if (mTime != null && time >= 0) mTime.setText(Tools.millisToString(time));
        if (mLength != null && length >= 0) mLength.setText(mDisplayRemainingTime && length > 0
                ? "-" + '\u00A0' + Tools.millisToString(length - time)
                : Tools.millisToString(length));

//        LogUtil.d(TAG, "setOverlayProgress mTime=" + mTime + " mLength=" + mLength);
        return time;
    }

    private void invalidateESTracks(int type) {
        switch (type) {
            case Media.Track.Type.Audio:
                mAudioTracksList = null;
                break;
            case Media.Track.Type.Text:
                mSubtitleTracksList = null;
                break;
        }
    }

    private void setESTracks() {
        if (mLastAudioTrack >= -1) {
            mService.setAudioTrack(mLastAudioTrack);
            mLastAudioTrack = -2;
        }
        if (mLastSpuTrack >= -1) {
            mService.setSpuTrack(mLastSpuTrack);
            mLastSpuTrack = -2;
        }
    }

    private void setESTrackLists() {
        if (mAudioTracksList == null && mService.getAudioTracksCount() > 0)
            mAudioTracksList = mService.getAudioTracks();
        if (mSubtitleTracksList == null && mService.getSpuTracksCount() > 0)
            mSubtitleTracksList = mService.getSpuTracks();
    }


    /**
     *
     */
    private void play() {
        mService.play();
        if (mRootView != null)
            mRootView.setKeepScreenOn(true);
    }

    /**
     *
     */
    private void pause() {
        mService.pause();
        if (mRootView != null)
            mRootView.setKeepScreenOn(false);
    }

    /*
     * Additionnal method to prevent alert dialog to pop up
     */
    @SuppressWarnings({"unchecked"})
    private void loadMedia(boolean fromStart) {
        mAskResume = false;
        getIntent().putExtra(PLAY_EXTRA_FROM_START, fromStart);
        loadMedia();
    }

    /**
     * External extras:
     * - position (long) - position of the video to start with (in ms)
     * - subtitles_location (String) - location of a subtitles file to load
     * - from_start (boolean) - Whether playback should start from start or from resume point
     * - title (String) - video title, will be guessed from file if not set.
     */
    @TargetApi(12)
    @SuppressWarnings({"unchecked"})
    protected void loadMedia() {
//        LogUtil.e(TAG, "loadMedia");
        if (mService == null)
            return;
        mUri = null;
        mIsPlaying = false;
        String title = null;
        boolean fromStart = false;
        String itemTitle = null;
        int positionInPlaylist = -1;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        long savedTime = extras != null ? extras.getLong(PLAY_EXTRA_START_TIME) : 0L; // position passed in by intent (ms)
        if (extras != null && savedTime == 0L)
            savedTime = extras.getInt(PLAY_EXTRA_START_TIME);
        /*
         * If the activity has been paused by pressing the power button, then
         * pressing it again will show the lock screen.
         * But onResume will also be called, even if vlc-android is still in
         * the background.
         * To workaround this, pause playback if the lockscreen is displayed.
         */
        final KeyguardManager km = (KeyguardManager) VLCApplication.getAppContext().getSystemService(KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode())
            mWasPaused = true;
        if (mWasPaused)
            LogUtil.d(TAG, "Video was previously paused, resuming in paused mode");

        if (intent.getData() != null)
            mUri = intent.getData();
        if (extras != null) {
            if (intent.hasExtra(PLAY_EXTRA_ITEM_LOCATION))
                mUri = extras.getParcelable(PLAY_EXTRA_ITEM_LOCATION);
            fromStart = extras.getBoolean(PLAY_EXTRA_FROM_START, false);
            mAskResume &= !fromStart;
            positionInPlaylist = extras.getInt(PLAY_EXTRA_OPENED_POSITION, -1);
        }

        if (intent.hasExtra(PLAY_EXTRA_SUBTITLES_LOCATION))
            mSubtitleSelectedFiles.add(extras.getString(PLAY_EXTRA_SUBTITLES_LOCATION));
        if (intent.hasExtra(PLAY_EXTRA_ITEM_TITLE))
            itemTitle = extras.getString(PLAY_EXTRA_ITEM_TITLE);

        MediaWrapper openedMedia = null;
        if (positionInPlaylist != -1 && mService.hasMedia() && positionInPlaylist < mService.getMedias().size()) {
            // Provided externally from AudioService
            LogUtil.d(TAG, "Continuing playback from PlaybackService at index " + positionInPlaylist);
            openedMedia = mService.getMedias().get(positionInPlaylist);
            if (openedMedia == null) {
                encounteredError();
                return;
            }
            mUri = openedMedia.getUri();
            itemTitle = openedMedia.getTitle();
            updateSeekable(mService.isSeekable());
            updatePausable(mService.isPausable());

            mService.flush();
        }

        if (mUri != null) {
            if (mService.hasMedia() && !mUri.equals(mService.getCurrentMediaWrapper().getUri()))
                mService.stop();
            // restore last position
            MediaWrapper media;
            if (openedMedia == null || openedMedia.getId() <= 0L) {
                Medialibrary ml = mMedialibrary;
                media = ml.getMedia(mUri);
                if (media == null && TextUtils.equals(mUri.getScheme(), "file") &&
                        mUri.getPath() != null && mUri.getPath().startsWith("/sdcard")) {
                    mUri = FileUtils.convertLocalUri(mUri);
                    media = ml.getMedia(mUri);
                }
                if (media != null && media.getId() != 0L && media.getTime() == 0L)
                    media.setTime((long) (media.getMetaLong(mMedialibrary, MediaWrapper.META_PROGRESS) * (double) media.getLength()) / 100L);
            } else
                media = openedMedia;
            if (media != null) {
                // in media library
                if (media.getTime() > 0 && !fromStart && positionInPlaylist == -1) {
                    if (mAskResume) {
                        showConfirmResumeDialog();
                        return;
                    }
                }
                // Consume fromStart option after first use to prevent
                // restarting again when playback is paused.
                intent.putExtra(PLAY_EXTRA_FROM_START, false);
                if (fromStart || mService.isPlaying())
                    media.setTime(0L);
                else if (savedTime <= 0L)
                    savedTime = media.getTime();

                mLastAudioTrack = media.getAudioTrack();
                mLastSpuTrack = media.getSpuTrack();
            } else {
                // not in media library

                if (savedTime > 0L && mAskResume) {
                    showConfirmResumeDialog();
                    return;
                } else {
                    long rTime = mSettings.getLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
                    if (rTime > 0 && !fromStart) {
                        if (mAskResume) {
                            showConfirmResumeDialog();
                            return;
                        } else {
                            Editor editor = mSettings.edit();
                            editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
                            editor.apply();
                            savedTime = rTime;
                        }
                    }
                }
            }

            // Start playback & seek
            mService.addCallback(this);
            /* prepare playback */
            boolean hasMedia = mService.hasMedia();
            if (hasMedia)
                media = mService.getCurrentMediaWrapper();
            else if (media == null)
                media = new MediaWrapper(mUri);
            if (mWasPaused)
                media.addFlags(MediaWrapper.MEDIA_PAUSED);
            if (intent.hasExtra(PLAY_DISABLE_HARDWARE))
                media.addFlags(MediaWrapper.MEDIA_NO_HWACCEL);
            media.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
            media.addFlags(MediaWrapper.MEDIA_VIDEO);

            if (savedTime <= 0L && media.getTime() > 0L)
                savedTime = media.getTime();
            if (savedTime > 0L && !mService.isPlaying())
                mService.saveTimeToSeek(savedTime);

            // Handle playback
            if (!hasMedia) {
                if (positionInPlaylist != -1)
                    mService.loadLastPlaylist(PlaybackService.TYPE_VIDEO);
                else
                    mService.load(media);
            } else if (!mService.isPlaying())
                mService.playIndex(positionInPlaylist);
            else
                onPlaying();

            // Get possible subtitles
            getSubtitles();

            // Get the title
            if (itemTitle == null && !TextUtils.equals(mUri.getScheme(), "content"))
                title = mUri.getLastPathSegment();
        } else {
            mService.addCallback(this);
            mService.loadLastPlaylist(PlaybackService.TYPE_VIDEO);
            MediaWrapper mw = mService.getCurrentMediaWrapper();
            if (mw == null) {
//                finish();
                supportFinishAfterTransition();
                return;
            }
            mUri = mService.getCurrentMediaWrapper().getUri();
        }
        if (itemTitle != null)
            title = itemTitle;
        mTitle.setText(title);

//        LogUtil.d(TAG, "loadMedia mWasPaused=" + mWasPaused);
        if (mWasPaused) {
            // XXX: Workaround to update the seekbar position
            mForcedTime = savedTime;
            setOverlayProgress();
            mForcedTime = -1;
            showOverlay(true);
        }
    }

    private SubtitlesGetTask mSubtitlesGetTask = null;

    private class SubtitlesGetTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            final String subtitleList_serialized = strings[0];
            ArrayList<String> prefsList = new ArrayList<>();

            if (subtitleList_serialized != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(subtitleList_serialized.getBytes());
                try {
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    prefsList = (ArrayList<String>) ois.readObject();
                } catch (InterruptedIOException ignored) {
                    return prefsList; /* Task is cancelled */
                } catch (ClassNotFoundException | IOException ignored) {
                }
            }

            if (!TextUtils.equals(mUri.getScheme(), "content"))
                prefsList.addAll(MediaDatabase.getInstance().getSubtitles(mUri.getLastPathSegment()));

            return prefsList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> prefsList) {
            // Add any selected subtitle file from the file picker
            if (prefsList.size() > 0) {
                for (String file : prefsList) {
                    if (!mSubtitleSelectedFiles.contains(file))
                        mSubtitleSelectedFiles.add(file);
                    LogUtil.i(TAG, "Adding user-selected subtitle " + file);
                    mService.addSubtitleTrack(file, true);
                }
            }
            mSubtitlesGetTask = null;
        }

        @Override
        protected void onCancelled() {
            mSubtitlesGetTask = null;
        }
    }

    public void getSubtitles() {
        if (mSubtitlesGetTask != null || mService == null)
            return;
        final String subtitleList_serialized = mSettings.getString(PreferencesActivity.VIDEO_SUBTITLE_FILES, null);

        mSubtitlesGetTask = new SubtitlesGetTask();
        mSubtitlesGetTask.execute(subtitleList_serialized);
    }

    @SuppressWarnings("deprecation")
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private int getScreenOrientation(int mode) {
        switch (mode) {
            case 99: //screen orientation user
                return AndroidUtil.isJellyBeanMR2OrLater ?
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR :
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR;
            case 101: //screen orientation landscape
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            case 102: //screen orientation portrait
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        }
        /*
         mScreenOrientation = 100, we lock screen at its current orientation
         */
        WindowManager wm = (WindowManager) VLCApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rot = getScreenRotation();
        /*
         * Since getRotation() returns the screen's "natural" orientation,
         * which is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT,
         * we have to invert the SCREEN_ORIENTATION value if it is "naturally"
         * landscape.
         */
        @SuppressWarnings("deprecation")
        boolean defaultWide = display.getWidth() > display.getHeight();
        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
            defaultWide = !defaultWide;
        if (defaultWide) {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                default:
                    return 0;
            }
        } else {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                default:
                    return 0;
            }
        }
    }

    public void showConfirmResumeDialog() {
        if (isFinishing())
            return;
        mService.pause();
        /* Encountered Error, exit player with a message */
        mAlertDialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setMessage(R.string.confirm_resume)
                .setPositiveButton(R.string.resume_from_position, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadMedia(false);
                    }
                })
                .setNegativeButton(R.string.play_from_start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadMedia(true);
                    }
                })
                .create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }

    public void showAdvancedOptions() {
        FragmentManager fm = getSupportFragmentManager();
        AdvOptionsDialog advOptionsDialog = new AdvOptionsDialog();
        advOptionsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dimStatusBar(true);
            }
        });
        advOptionsDialog.show(fm, "fragment_adv_options");
        hideOverlay(false);
    }

    private void togglePlaylist() {
        if (mPlaylist.getVisibility() == View.VISIBLE) {
            mPlaylist.setVisibility(View.GONE);
            mPlaylist.setOnClickListener(null);
            return;
        }
        hideOverlay(true);
        mPlaylist.setVisibility(View.VISIBLE);
        mPlaylist.setAdapter(mPlaylistAdapter);
        updateList();
    }

    private BroadcastReceiver mBtReceiver = AndroidUtil.isICSOrLater ? new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    long savedDelay = mSettings.getLong(KEY_BLUETOOTH_DELAY, 0l);
                    long currentDelay = mService.getAudioDelay();
                    if (savedDelay != 0l) {
                        boolean connected = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1) == BluetoothA2dp.STATE_CONNECTED;
                        if (connected && currentDelay == 0l)
                            toggleBtDelay(true);
                        else if (!connected && savedDelay == currentDelay)
                            toggleBtDelay(false);
                    }
            }
        }
    } : null;

    private void toggleBtDelay(boolean connected) {
        mService.setAudioDelay(connected ? mSettings.getLong(KEY_BLUETOOTH_DELAY, 0) : 0l);
    }

    private OnClickListener mBtSaveListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mSettings.edit().putLong(KEY_BLUETOOTH_DELAY, mService.getAudioDelay()).apply();
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void createPresentation() {
        if (mMediaRouter == null || mEnableCloneMode)
            return;

        // Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        if (presentationDisplay != null) {
            // Show a new presentation if possible.
            LogUtil.i(TAG, "Showing presentation on display: " + presentationDisplay);
            mPresentation = new SecondaryDisplay(this, LibVLC(), presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
                mPresentationDisplayId = presentationDisplay.getDisplayId();
            } catch (WindowManager.InvalidDisplayException ex) {
                LogUtil.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        } else
            LogUtil.i(TAG, "No secondary display detected");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void removePresentation() {
        if (mMediaRouter == null)
            return;

        // Dismiss the current presentation if the display has changed.
        LogUtil.i(TAG, "Dismissing presentation because the current route no longer "
                + "has a presentation display.");
        if (mPresentation != null) mPresentation.dismiss();
        mPresentation = null;
        mPresentationDisplayId = -1;
        stopPlayback();

        recreate();
    }

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (dialog == mPresentation) {
                LogUtil.i(TAG, "Presentation was dismissed.");
                mPresentation = null;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static final class SecondaryDisplay extends Presentation {
        public final static String TAG = "VLC/SecondaryDisplay";

        private SurfaceView mSurfaceView;
        private SurfaceView mSubtitlesSurfaceView;
        private FrameLayout mSurfaceFrame;

        public SecondaryDisplay(Context context, LibVLC libVLC, Display display) {
            super(context, display);
            if (context instanceof AppCompatActivity) {
                setOwnerActivity((AppCompatActivity) context);
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.player_remote);

            mSurfaceView = (SurfaceView) findViewById(R.id.remote_player_surface);
            mSubtitlesSurfaceView = (SurfaceView) findViewById(R.id.remote_subtitles_surface);
            mSurfaceFrame = (FrameLayout) findViewById(R.id.remote_player_surface_frame);

            mSubtitlesSurfaceView.setZOrderMediaOverlay(true);
            mSubtitlesSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            VideoPlayerActivity activity = (VideoPlayerActivity) getOwnerActivity();
            if (activity == null) {
                LogUtil.e(TAG, "Failed to get the VideoPlayerActivity instance, secondary display won't work");
                return;
            }

            LogUtil.i(TAG, "Secondary display created");
        }
    }

    /**
     * Start the video loading animation.
     */
    private void startLoading() {
        if (mIsLoading)
            return;
        mIsLoading = true;
        AnimationSet anim = new AnimationSet(true);
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(800);
        rotate.setInterpolator(new DecelerateInterpolator());
        rotate.setRepeatCount(RotateAnimation.INFINITE);
        anim.addAnimation(rotate);
        mLoading.setVisibility(View.VISIBLE);
        mLoading.startAnimation(anim);
    }

    /**
     * Stop the video loading animation.
     */
    private void stopLoading() {
        mHandler.removeMessages(LOADING_ANIMATION);
        if (!mIsLoading)
            return;
        mIsLoading = false;
        mLoading.setVisibility(View.INVISIBLE);
        mLoading.clearAnimation();
        if (mPresentation != null) {
            mTipsBackground.setVisibility(View.VISIBLE);
        }
    }

    public void onClickOverlayTips(View v) {
        UiTools.setViewVisibility(mOverlayTips, View.GONE);
    }

    public void onClickDismissTips(View v) {
        UiTools.setViewVisibility(mOverlayTips, View.GONE);
        Editor editor = mSettings.edit();
        editor.putBoolean(PREF_TIPS_SHOWN, true);
        editor.apply();
    }

    private void updateNavStatus() {
        mIsNavMenu = false;
        mMenuIdx = -1;

        final MediaPlayer.Title[] titles = mService.getTitles();
        if (titles != null) {
            final int currentIdx = mService.getTitleIdx();
            for (int i = 0; i < titles.length; ++i) {
                final MediaPlayer.Title title = titles[i];
                if (title.isMenu()) {
                    mMenuIdx = i;
                    break;
                }
            }
            mIsNavMenu = mMenuIdx == currentIdx;
        }

        if (mIsNavMenu) {
            /*
             * Keep the overlay hidden in order to have touch events directly
             * transmitted to navigation handling.
             */
            hideOverlay(false);
        } else if (mMenuIdx != -1)
            setESTracks();

        UiTools.setViewVisibility(mNavMenu, mMenuIdx >= 0 && mNavMenu != null ? View.VISIBLE : View.GONE);
        supportInvalidateOptionsMenu();
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mHandler.sendEmptyMessageDelayed(mShowing ? HIDE_INFO : SHOW_INFO, 200);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mHandler.removeMessages(HIDE_INFO);
            mHandler.removeMessages(SHOW_INFO);
            float range = mCurrentScreenOrientation == Configuration.ORIENTATION_LANDSCAPE ? mSurfaceXDisplayRange : mSurfaceYDisplayRange;
            if (mService == null)
                return false;
            if (!mIsLocked) {
                if ((mTouchControls & TOUCH_FLAG_SEEK) == 0) {
                    doPlayPause();
                    return true;
                }
                float x = e.getX();
                if (x < range / 4f)
                    seekDelta(-10000);
                else if (x > range * 0.75)
                    seekDelta(10000);
                else
                    doPlayPause();
                return true;
            }
            return false;
        }
    };

    public PlaybackServiceActivity.Helper getHelper() {
        return mHelper;
    }

    @Override
    public void onConnected(PlaybackService service) {
        mService = service;
        if (!mSwitchingView && (!mWillTransition || mTransitionEnd))
            mHandler.sendEmptyMessage(START_PLAYBACK);
        mSwitchingView = false;
        mSettings.edit().putBoolean(PreferencesActivity.VIDEO_RESTORE, false).apply();

    }

    @Override
    public void onDisconnected() {
        mService = null;
        mHandler.sendEmptyMessage(AUDIO_SERVICE_CONNECTION_FAILED);
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        LogUtil.d(TAG, "VideoPlayerActivity onNewVideoLayout width=" + width + " height=" + height + " visibleWidth=" + visibleWidth + " visibleHeight=" + visibleHeight);
        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;
        LogUtil.d(TAG, "onNewVideoLayout changeSurfaceLayout");

        changeSurfaceLayout();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        LogUtil.d(TAG, "onSurfacesCreated");
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        LogUtil.d(TAG, "onSurfacesDestroyed");
    }

    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), PLAY_FROM_SERVICE))
                onNewIntent(intent);
            else if (TextUtils.equals(intent.getAction(), EXIT_PLAYER))
                exitOK();
        }
    };

    private void applyTheme() {
//        boolean enableBlackTheme = mSettings.getBoolean("enable_night_theme", false);
//        if (VLCApplication.showTvUi() || enableBlackTheme) {
//            setTheme(R.style.Theme_VLC_Black);
//        }
        int themeIndex = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).getInt(PreferencesActivity.KEY_CURRENT_THEME_INDEX, 0);
        setTheme(ThemeFragment.sThemeActionBarStyles[themeIndex]);
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
////                Log.e(TAG, "onPreloadSucceed");
//                mIsAdLoadSuc = true;
//            }
//
//            @Override
//            public void onPreloadFaild(String s) {
////                Log.e(TAG, "onPreloadFaild"+s);
//            }
//        });
//        sdk.preload(preloadMap);
    }

    public void loadInterstitial() {
        String adID = "";
        if (ADManager.sPlatForm == ADManager.AD_MobVista) {
        } else if (ADManager.sPlatForm == ADManager.AD_Google) {
            adID = ADConstants.google_video_back_interstitial;
        } else if (ADManager.sPlatForm == ADManager.AD_Facebook) {
            adID = ADConstants.facebook_video_back_interstitial;
        }
        if (!TextUtils.isEmpty(adID)) {
            mInterstitial = new Interstitial();
            mInterstitial.loadAD(this, ADManager.sPlatForm, adID, new Interstitial.ADListener() {
                @Override
                public void onLoadedSuccess() {
                }

                @Override
                public void onLoadedFailed() {

                }

                @Override
                public void onAdDisplayed() {
                }

                @Override
                public void onAdClose() {

                }
            });
        }
    }

    private void initPauseNative() {
        mNativeFrameLayout = (FrameLayout) findViewById(R.id.ad_frame_layout);
        mNativeContainer = (FrameLayout) findViewById(R.id.ad_container);
        ImageView adClose = (ImageView) findViewById(R.id.ad_close_iv);
        if (adClose != null) {
            adClose.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNativeFrameLayout.startAnimation(mTranstionAnimOut);
                    mNativeFrameLayout.setVisibility(View.GONE);
                    if (!mIsActive) {
                        play();
                    }
                }
            });
        }
    }

    NativeAdScrollView scrollView;
    private boolean mIsActive = true;

    private void loadPauseNative(boolean isActive) {//是否主动：主动的话，点x不要继续播放
        if (ADManager.getInstance().mPauseManager != null && ADManager.getInstance().mPauseManager.isLoaded()) {
            if (mNativeFrameLayout != null && mNativeContainer != null) {
                mIsActive = isActive;
                ADManager.getInstance().mIsPauseADShown = true;
                mNativeFrameLayout.setVisibility(View.VISIBLE);
                if (scrollView != null) {
                    mNativeContainer.removeView(scrollView);
                }
                scrollView = new NativeAdScrollView(VideoPlayerActivity.this, ADManager.getInstance().mPauseManager, NativeAdView.Type.HEIGHT_300);
                StatisticsManager.submitAd(VideoPlayerActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_PAUSE_ADS + "shown");
                mNativeContainer.addView(scrollView);
                mNativeFrameLayout.startAnimation(mTranstionAnimIn);
            }
        }
//        NativeAD mFeedNativeAD = new NativeAD();
//        mFeedNativeAD.loadAD(VideoPlayerActivity.this, ADManager.AD_Facebook, ADConstants.facebook_video_pause_native, new NativeAD.ADListener() {
//            @Override
//            public void onLoadedSuccess(com.facebook.ads.NativeAd nativeAd,String adId) {
//                if(null ==mNativeFrameLayout|| null==nativeAd){
//                    //异步过程，可能当前页面已经销毁了
//                    return;
//                }
//                mNativeFrameLayout.setVisibility(View.VISIBLE);
//                LayoutInflater inflater = LayoutInflater.from(VideoPlayerActivity.this);
//                RelativeLayout adView = (RelativeLayout) inflater.inflate(R.layout.layout_pause_native_ad, mNativeContainer, false);
//                mNativeContainer.removeAllViews();
//                mNativeContainer.addView(adView);
//
//                // Create native UI using the ad_front metadata.
//                ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.native_ad_icon);
//                TextView nativeAdTitle = (TextView) adView.findViewById(R.id.native_ad_title);
//                MediaView nativeAdMedia = (MediaView) adView.findViewById(R.id.native_ad_media);
//                // TextView nativeAdSocialContext = (TextView) adView.findViewById(R.id.native_ad_social_context);
//                TextView nativeAdBody = (TextView) adView.findViewById(R.id.native_ad_body);
//                Button nativeAdCallToAction = (Button) adView.findViewById(R.id.native_ad_call_to_action);
//
//                // Set the Text.
//                nativeAdTitle.setText(nativeAd.getAdTitle());
//                // nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
//                nativeAdBody.setText(nativeAd.getAdBody());
//                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
//
//                // Download and display the ad_front icon.
//                NativeAd.Image adIcon = nativeAd.getAdIcon();
//                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);
//
//                // Download and display the cover image.
//                nativeAdMedia.setNativeAd(nativeAd);
//
//                // Add the AdChoices icon
//                LinearLayout adChoicesContainer = (LinearLayout) findViewById(R.id.ad_choices_container);
//                AdChoicesView adChoicesView = new AdChoicesView(VideoPlayerActivity.this, nativeAd, true);
//                adChoicesContainer.addView(adChoicesView);
//
//                // Register the Title and CTA button to listen for clicks.
//                List<View> clickableViews = new ArrayList<>();
//                clickableViews.add(nativeAdTitle);
//                clickableViews.add(nativeAdCallToAction);
//                nativeAd.registerViewForInteraction(mNativeContainer, clickableViews);
//            }
//
//            @Override
//            public void onLoadedFailed(String msg,String adId, int errorcode) {
//
//            }
//
//            @Override
//            public void onAdDisplayed() {
//
//            }
//
//            @Override
//            public void onAdImpression(NativeAd ad, String adId) {
//
//            }
//        });
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
//        mRotateAD = (RotateAD) findViewById(R.id.player_roate_ad);
//        if (null != mRotateAD) {
//            mRotateAD.setOnClick(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    openWall();
//                    StatisticsManager.submitAd(VideoPlayerActivity.this, StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_VIDEO_NAME);
//                }
//            });
//        }
    }

    private void changeSpeedMenu(float speed) {
        String str = String.format("%.1f", speed);
        if (!"1.0".equals(str)) {
            if (null != mItemMap.get(MenuType.speed)) {
                mItemMap.get(MenuType.speed).setSelected(true);
                mItemMap.get(MenuType.speed).getContentTv().setText(String.format("%.1f", speed));
            }
        } else {
            if (null != mItemMap.get(MenuType.speed)) {
                mItemMap.get(MenuType.speed).reset();
            }
        }
    }

    private static final int ID_VOLUME = 11;
    private static final int ID_BRIGHTNESS = 12;
    private static final int ID_SLEEP = 1;
    private static final int ID_JUMP_TO = 2;
    private static final int ID_CHAPTER_TITLE = 5;
    private static final int ID_PLAYBACK_SPEED = 6;
    private static final int ID_SAVE_PLAYLIST = 8;

    //当前模式是否是播放完当前一个就停止播放
    private boolean mTimeEnd = false;

    private void showFragment(int id) {
        int mTheme = (UiTools.isBlackThemeEnabled()) ?
                R.style.Theme_VLC_Black :
                R.style.Theme_VLC;

        DialogFragment newFragment;
        String tag;
        switch (id) {
            case ID_VOLUME:
                newFragment = VolumeDialog.newInstance(mTheme);
                tag = "playback_speed";
                break;
            case ID_BRIGHTNESS:
                newFragment = BrightnessDialog.newInstance(mTheme);
                tag = "playback_speed";
                break;
            case ID_PLAYBACK_SPEED:
                newFragment = PlaybackSpeedDialog.newInstance(mTheme);
                ((PlaybackSpeedDialog) newFragment).setListener(new PlaybackSpeedDialog.SpeedListener() {
                    @Override
                    public void speedChanged(float speed) {
                        changeSpeedMenu(speed);
                    }
                });
                tag = "playback_speed";
                break;
            case ID_JUMP_TO:
                newFragment = JumpToTimeDialog.newInstance(mTheme);
                tag = "time";
                break;
            case ID_SLEEP:
                newFragment = TimerDialog.newInstance(mTheme);
                ((TimerDialog) newFragment).setListener(new TimerDialog.TimerListener() {
                    @Override
                    public void timer(int timeType) {
                        if (timeType == 0) {
                            if (null != mItemMap.get(MenuType.timer)) {
                                mItemMap.get(MenuType.timer).reset();
                            }
                            mTimeEnd = false;
                        } else {
                            if (null != mItemMap.get(MenuType.timer)) {
                                mItemMap.get(MenuType.timer).setSelected(true);
                                mTimeEnd = false;
                                if (timeType == 1) {
                                    restart(15);
                                } else if (timeType == 2) {
                                    restart(30);
                                } else if (timeType == 3) {
                                    restart(45);
                                } else if (timeType == 4) {
                                    restart(60);
                                } else if (timeType == 5) {
                                    // TODO: 2017/12/22
                                    //当前视频播放完成，怎么显示？seek的时候还要重新
                                    if (null != mItemMap.get(MenuType.timer)) {
                                        mItemMap.get(MenuType.timer).getContentTv().setText("End");
                                    }
                                    mTimeEnd = true;
                                }
                            }
                        }
                    }
                });
                tag = "time";
                break;
            case ID_CHAPTER_TITLE:
                newFragment = SelectChapterDialog.newInstance(mTheme);
                tag = "select_chapter";
                break;
            case ID_SAVE_PLAYLIST:
                UiTools.addToPlaylist(this, mService.getMedias());
                return;
            default:
                return;
        }
        if (newFragment != null)
            newFragment.show(getSupportFragmentManager(), tag);
    }

    CountDownTimer timer;

    /**
     * 取消倒计时
     */
    public void oncancel() {
        if (null != timer) {
            timer.cancel();
        }
    }

    /**
     * 开始倒计时
     */
    public void restart(int min) {
        oncancel();
        timer =  new CountDownTimer(1000 * min*60, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (null != mItemMap.get(MenuType.timer)) {
                    mItemMap.get(MenuType.timer).getContentTv().setText(change((int) (millisUntilFinished / 1000)));
                }
            }
            @Override
            public void onFinish() {
                if (null != mItemMap.get(MenuType.timer)) {
                    mItemMap.get(MenuType.timer).reset();
                }
                exitOK();
            }
        };
        timer.start();
    }

    public String change(int second) {
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        if (h <= 0) {
            return d + ":" +s;
        }
        return h + ":" + d + ":"+ s;
    }
}
