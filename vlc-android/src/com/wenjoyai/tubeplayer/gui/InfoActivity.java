package com.wenjoyai.tubeplayer.gui;


import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.util.Extensions;
import org.videolan.medialibrary.media.MediaWrapper;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.databinding.InfoActivityBinding;
import com.wenjoyai.tubeplayer.gui.helpers.AudioUtil;
import com.wenjoyai.tubeplayer.gui.helpers.FloatingActionButtonBehavior;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.gui.video.MediaInfoAdapter;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.Strings;
import com.wenjoyai.tubeplayer.util.VLCInstance;
import com.wenjoyai.tubeplayer.util.WeakHandler;

import java.io.File;

public class InfoActivity extends AudioPlayerContainerActivity implements View.OnClickListener {

    public final static String TAG_ITEM = "ML_ITEM";
    public final static String TAG_FAB_VISIBILITY= "FAB";

    private MediaWrapper mItem;
    private MediaInfoAdapter mAdapter;
    private LoadImageTask mLoadImageTask = null;
    private CheckFileTask mCheckFileTask = null;
    private final static int EXIT = 2;
    private final static int SHOW_SUBTITLES = 3;

    InfoActivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.info_activity);

        initAudioPlayerContainerActivity();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItem = (MediaWrapper) (savedInstanceState != null ?
                savedInstanceState.getParcelable(TAG_ITEM) :
                getIntent().getParcelableExtra(TAG_ITEM));
        if (mItem == null) {
            LogUtil.e(TAG, "onCreate mItem == null");
            return;
        }
        if (mItem.getId() == 0L) {
            MediaWrapper mediaWrapper = VLCApplication.getMLInstance().getMedia(mItem.getUri());
            if (mediaWrapper != null)
                mItem = mediaWrapper;
        }
        mBinding.setItem(mItem);
        final int fabVisibility =  savedInstanceState != null
            ? savedInstanceState.getInt(TAG_FAB_VISIBILITY) : -1;

        mAdapter = new MediaInfoAdapter(this);
        mBinding.list.setAdapter(mAdapter);

        if (!TextUtils.isEmpty(mItem.getArtworkMrl())) {
            VLCApplication.runBackground(new Runnable() {
                @Override
                public void run() {
                    final Bitmap cover = AudioUtil.readCoverBitmap(Uri.decode(mItem.getArtworkMrl()), 0);
                    if (cover != null) {
                        mBinding.setCover(new BitmapDrawable(InfoActivity.this.getResources(), cover));
                        VLCApplication.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewCompat.setNestedScrollingEnabled(mBinding.container, true);
                                mBinding.appbar.setExpanded(true, true);
                                if (fabVisibility != -1)
                                    mBinding.fab.setVisibility(fabVisibility);
                            }
                        });
                    } else
                        noCoverFallback();
                }
            });
        } else
            noCoverFallback();
        mBinding.fab.setOnClickListener(this);
        mCheckFileTask = new CheckFileTask();
        mLoadImageTask = new LoadImageTask();

        mCheckFileTask.exec();
        mLoadImageTask.exec();

//        mBinding.length.setText(mItem.getLength() > 0L ? Tools.millisToString(mItem.getLength()) : "");
        mBinding.infoPath.setText(Uri.decode(mItem.getUri().getPath()));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mFragmentContainer = mBinding.container;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TAG_ITEM, mItem);
        outState.putInt(TAG_FAB_VISIBILITY, mBinding.fab.getVisibility());
    }

    private void noCoverFallback() {
        mBinding.appbar.setExpanded(false);
        ViewCompat.setNestedScrollingEnabled(mBinding.list, false);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mBinding.fab.getLayoutParams();
        lp.setAnchorId(mBinding.container.getId());
        lp.anchorGravity = Gravity.BOTTOM|Gravity.RIGHT|Gravity.END;
        lp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.default_margin);
        lp.setBehavior(new FloatingActionButtonBehavior(InfoActivity.this, null));
        mBinding.fab.setLayoutParams(lp);
        mBinding.fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (mItem != null) {
            mService.load(mItem.getTracks(VLCApplication.getMLInstance()), 0);
        }
        finish();
    }

    @Override
    protected void onPlayerStateChanged(View bottomSheet, int newState) {
        int visibility = mBinding.fab.getVisibility();
        if (visibility == View.VISIBLE && newState != BottomSheetBehavior.STATE_COLLAPSED && newState != BottomSheetBehavior.STATE_HIDDEN)
            mBinding.fab.setVisibility(View.INVISIBLE);
        else if (visibility == View.INVISIBLE && (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN))
            mBinding.fab.show();
    }

    private class CheckFileTask extends AsyncTask<Void, Void, File> {

        void exec() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                execute();
        }

        private void checkSubtitles(File itemFile) {
            String extension, filename, videoName = Uri.decode(itemFile.getName()), parentPath = Uri.decode(itemFile.getParent());
            videoName = videoName.substring(0, videoName.lastIndexOf('.'));
            String[] subFolders = {"/Subtitles", "/subtitles", "/Subs", "/subs"};
            String[] files = itemFile.getParentFile().list();
            int filesLength = files == null ? 0 : files.length;
            for (String subFolderName : subFolders){
                File subFolder = new File(parentPath+subFolderName);
                if (!subFolder.exists())
                    continue;
                String[] subFiles = subFolder.list();
                int subFilesLength = 0;
                String[] newFiles = new String[0];
                if (subFiles != null) {
                    subFilesLength = subFiles.length;
                    newFiles = new String[filesLength+subFilesLength];
                    System.arraycopy(subFiles, 0, newFiles, 0, subFilesLength);
                }
                if (files != null)
                    System.arraycopy(files, 0, newFiles, subFilesLength, filesLength);
                files = newFiles;
                filesLength = files.length;
            }
            for (int i = 0; i<filesLength ; ++i){
                filename = Uri.decode(files[i]);
                int index = filename.lastIndexOf('.');
                if (index <= 0)
                    continue;
                extension = filename.substring(index);
                if (!Extensions.SUBTITLES.contains(extension))
                    continue;

                if (mHandler == null || isCancelled())
                    return;
                if (filename.startsWith(videoName)) {
                    mHandler.obtainMessage(SHOW_SUBTITLES).sendToTarget();
                    return;
                }
            }
        }

        @Override
        protected File doInBackground(Void... params) {
            File itemFile = null;
            if (mItem != null) {
                itemFile = new File(Uri.decode(mItem.getLocation().substring(5)));

                if (mItem.getType() == MediaWrapper.TYPE_VIDEO)
                    checkSubtitles(itemFile);
            }
            return itemFile;
        }

        @Override
        protected void onPostExecute(File file) {
            mBinding.sizeValue.setText(Strings.readableFileSize(file.length()));
            mCheckFileTask = null;
        }

        @Override
        protected void onCancelled() {
            mCheckFileTask = null;
        }
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Media> {

        public void exec() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                execute();
        }

        @Override
        protected Media doInBackground(Void... params) {

            final LibVLC libVlc = VLCInstance.get();
            if (mItem == null || libVlc == null || isCancelled())
                return null;

            Media media = new Media(libVlc, mItem.getUri());
            media.parse();

            return media;
        }

        @Override
        protected void onPostExecute(Media media) {
            boolean hasSubs = false;
            if (media == null)
                return;
            final int trackCount = media.getTrackCount();
            for (int i = 0; i < trackCount; ++i) {
                final Media.Track track = media.getTrack(i);
                if (track.type == Media.Track.Type.Text)
                    hasSubs = true;
                mAdapter.add(track);
            }
            media.release();

            if (hasSubs && mHandler != null)
                mHandler.obtainMessage(SHOW_SUBTITLES).sendToTarget();
            mLoadImageTask = null;
        }

        @Override
        protected void onCancelled() {
            mLoadImageTask = null;
        }
    }

    private Handler mHandler = new InfoActivity.MediaInfoHandler(this);

    private static class MediaInfoHandler extends WeakHandler<InfoActivity> {
        MediaInfoHandler(InfoActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            final InfoActivity activity = getOwner();
            if(activity == null) return;

            switch (msg.what) {
                case EXIT:
                    activity.setResult(PreferencesActivity.RESULT_RESCAN);
                    activity.finish();
                    break;
                case SHOW_SUBTITLES:
                    activity.mBinding.infoSubtitles.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
