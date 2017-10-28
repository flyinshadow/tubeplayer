/*
 * ************************************************************************
 *  PopupManager.java
 * *************************************************************************
 *  Copyright © 2016 VLC authors and VideoLAN
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
 *
 *  *************************************************************************
 */

package com.wenjoyai.tubeplayer.gui.video;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.ThemeFragment;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.gui.view.PopupLayout;

public class PopupManager implements PlaybackService.Callback, GestureDetector.OnDoubleTapListener,
        View.OnClickListener, GestureDetector.OnGestureListener, IVLCVout.OnNewVideoLayoutListener {

    private static final String TAG ="VLC/PopupManager";

    private static final int FLING_STOP_VELOCITY = 3000;
    private static final int MSG_DELAY = 3000;

    private static final int SHOW_BUTTONS = 0;
    private static final int HIDE_BUTTONS = 1;

    private PlaybackService mService;

    private PopupLayout mRootView;
    private ImageView mExpandButton;
    private ImageView mCloseButton;
    private ImageView mPlayPauseButton;
    private final boolean mAlwaysOn;
    private SeekBar mSeekBar;

    public PopupManager(PlaybackService service) {
        mService = service;
        mAlwaysOn = PreferenceManager.getDefaultSharedPreferences(service).getBoolean("popup_keepscreen", false);
    }

    public void removePopup() {
        hideNotification();
        if (mRootView == null)
            return;
        mService.setVideoTrackEnabled(false);
        mService.removeCallback(this);
        final IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.detachViews();
        mRootView.close();
        mRootView = null;
    }

    public void showPopup() {
        mService.addCallback(this);
        int themeIndex = android.preference.PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).getInt(PreferencesActivity.KEY_CURRENT_THEME_INDEX, 0);
        mRootView = (PopupLayout) LayoutInflater.from(new ContextThemeWrapper(VLCApplication.getAppContext(), ThemeFragment.sThemeActionBarStyles[themeIndex])).inflate(R.layout.video_popup, null);
//        mRootView.setVideoSize(mService.getCurrentMediaWrapper().getWidth(), mService.getCurrentMediaWrapper().getHeight());
        if (mAlwaysOn)
            mRootView.setKeepScreenOn(true);
        mPlayPauseButton = (ImageView) mRootView.findViewById(R.id.video_play_pause);
        mCloseButton = (ImageView) mRootView.findViewById(R.id.popup_close);
        mExpandButton = (ImageView) mRootView.findViewById(R.id.popup_expand);
        mPlayPauseButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mExpandButton.setOnClickListener(this);

        mSeekBar = (SeekBar) mRootView.findViewById(R.id.popup_seekbar);
        mSeekBar.setVisibility(View.GONE);

        GestureDetectorCompat gestureDetector = new GestureDetectorCompat(mService, this);
        gestureDetector.setOnDoubleTapListener(this);
        mRootView.setGestureDetector(gestureDetector);

        final IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.setVideoView((SurfaceView) mRootView.findViewById(R.id.player_surface));
        vlcVout.attachViews(this);
        mRootView.setVLCVOut(vlcVout);
        mService.setVideoAspectRatio(null);
        mService.setVideoScale(0);
        mService.setVideoTrackEnabled(true);
//        if (!mService.isPlaying())
//            mService.playIndex(mService.getCurrentMediaPosition());
//        else
//            mService.flush();
//        mService.startService(new Intent(mService, PlaybackService.class));
        showNotification();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mPlayPauseButton.getVisibility() == View.VISIBLE)
            return false;
        mHandler.sendEmptyMessage(SHOW_BUTTONS);
        mHandler.sendEmptyMessageDelayed(HIDE_BUTTONS, MSG_DELAY);
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        mService.removePopup();
        mService.switchToVideo();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (Math.abs(velocityX) > FLING_STOP_VELOCITY || velocityY > FLING_STOP_VELOCITY) {
            stopPlayback();
            return true;
        }
        return false;
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height,
                                 int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

        Log.d("firstvideo", "PopupManager onNewVideoLayout width=" + width + ", height=" + height +
                ", visibleWidth=" + visibleWidth + ", visibleHeight=" + visibleHeight);

        int displayW = mRootView.getWidth(), displayH = mRootView.getHeight();

        // sanity check
        if (displayW * displayH == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        Media.VideoTrack vtrack = mService.getCurrentVideoTrack();
        if (vtrack != null) {
            width = vtrack.width;
            height = vtrack.height;
        }

        Log.d("firstvideo", "PopupManager onNewVideoLayout vtrack.width=" + vtrack.width + ", vtrack.height=" + vtrack.height);

        if (width == 0 || height == 0) {
            mRootView.setViewSize(displayW, displayH);
            return;
        }

        if (width < height) {
            displayW = VLCApplication.getAppResources().getDimensionPixelSize(R.dimen.video_pip_width_vertical);
        }
        displayH = displayW * height / width;
        mRootView.setViewSize(displayW, displayH);
    }

    @Override
    public void update() {}

    @Override
    public void updateProgress() {
        if (mSeekBar != null) {
            long time = mService.getTime();
            long length = mService.getLength();
            Log.e(TAG, "updateProgress length=" + length + ", time=" + time);
            mSeekBar.setVisibility(View.VISIBLE);
            mSeekBar.setMax((int)length);
            mSeekBar.setProgress((int) time);
        }
    }

    @Override
    public void onMediaEvent(Media.Event event) {}

    @Override
    public void onMediaPlayerEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Stopped:
                mService.removePopup();
                break;
            case MediaPlayer.Event.Playing:
                if (!mAlwaysOn)
                    mRootView.setKeepScreenOn(true);
                mPlayPauseButton.setImageResource(R.drawable.ic_popup_pause);
                showNotification();
                break;
            case MediaPlayer.Event.Paused:
                if (!mAlwaysOn)
                    mRootView.setKeepScreenOn(false);
                mPlayPauseButton.setImageResource(R.drawable.ic_popup_play);
                showNotification();
                break;
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_BUTTONS:
                    mPlayPauseButton.setVisibility(View.VISIBLE);
                    mCloseButton.setVisibility(View.VISIBLE);
                    mExpandButton.setVisibility(View.VISIBLE);
                    break;
                case HIDE_BUTTONS:
                    mPlayPauseButton.setVisibility(View.GONE);
                    mCloseButton.setVisibility(View.GONE);
                    mExpandButton.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_play_pause:
                if (mService.hasMedia()) {
                    boolean isPLaying = mService.isPlaying();
                    if (isPLaying)
                        mService.pause();
                    else
                        mService.play();
                }
                break;
            case R.id.popup_close:
                stopPlayback();
                break;
            case R.id.popup_expand:
                mService.removePopup();
                mService.switchToVideo();
                break;
        }
    }

    private void stopPlayback() {
        long time = mService.getTime();
        long length = mService.getLength();
        //remove saved position if in the last 5 seconds
        if (length - time < 5000)
            time = 0;
        else
            time -= 2000; // go back 2 seconds, to compensate loading time
        mService.stop();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mService).edit();
        // Save position
        if (mService.isSeekable() && time != -1)
            editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, time).apply();
    }

    private void showNotification() {
        PendingIntent piStop = PendingIntent.getBroadcast(mService, 0,
                new Intent(PlaybackService.ACTION_REMOTE_STOP), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.ic_notif_video)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(mService.getTitle())
                .setContentText(mService.getString(R.string.popup_playback))
                .setAutoCancel(false)
                .setOngoing(true)
                .setDeleteIntent(piStop);

        //Switch
        final Intent notificationIntent = new Intent(PlaybackService.ACTION_REMOTE_SWITCH_VIDEO);
        PendingIntent piExpand = PendingIntent.getBroadcast(mService, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PLay Pause
        PendingIntent piPlay = PendingIntent.getBroadcast(mService, 0, new Intent(PlaybackService.ACTION_REMOTE_PLAYPAUSE), PendingIntent.FLAG_UPDATE_CURRENT);

        if (mService.isPlaying())
            builder.addAction(R.drawable.ic_popup_pause, mService.getString(R.string.pause), piPlay);
        else
            builder.addAction(R.drawable.ic_popup_play, mService.getString(R.string.play), piPlay);
        builder.addAction(R.drawable.ic_popup_expand_w, mService.getString(R.string.popup_expand), piExpand);

        Notification notification = builder.build();
        try {
            NotificationManagerCompat.from(mService).notify(42, notification);
        } catch (IllegalArgumentException e) {}
    }

    private void hideNotification() {
        NotificationManagerCompat.from(mService).cancel(42);
    }
}
