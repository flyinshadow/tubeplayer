/*****************************************************************************
 * VideoListActivity.java
 *****************************************************************************
 * Copyright © 2011-2012 VLC authors and VideoLAN
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.interfaces.MediaAddedCb;
import org.videolan.medialibrary.interfaces.MediaUpdatedCb;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wenjoyai.tubeplayer.MediaParsingService;
import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.ad.ADConstants;
import com.wenjoyai.tubeplayer.ad.ADManager;
import com.wenjoyai.tubeplayer.ad.NativeAD;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.MainActivity;
import com.wenjoyai.tubeplayer.gui.RenameFileFragment;
import com.wenjoyai.tubeplayer.gui.SecondaryActivity;
import com.wenjoyai.tubeplayer.gui.browser.MediaBrowserFragment;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.gui.view.AutoFitRecyclerView;
import com.wenjoyai.tubeplayer.gui.view.ContextMenuRecyclerView;
import com.wenjoyai.tubeplayer.gui.view.SwipeRefreshLayout;
import com.wenjoyai.tubeplayer.interfaces.Filterable;
import com.wenjoyai.tubeplayer.interfaces.IEventsHandler;
import com.wenjoyai.tubeplayer.interfaces.ISortable;
import com.wenjoyai.tubeplayer.media.MediaGroup;
import com.wenjoyai.tubeplayer.media.MediaUtils;
import com.wenjoyai.tubeplayer.util.FileUtils;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.VLCInstance;

import java.util.ArrayList;
import java.util.List;

public class VideoGridFragment extends MediaBrowserFragment implements MediaUpdatedCb, ISortable, SwipeRefreshLayout.OnRefreshListener, MediaAddedCb, Filterable, IEventsHandler {

    public final static String TAG = "VLC/VideoListFragment";

    public final static String KEY_GROUP = "key_group";
    public final static String KEY_FOLDER_GROUP = "key_folder_group";
    public final static String KEY_FOLDER_TITLE = "key_folder_title";

    protected LinearLayout mLayoutFlipperLoading;
    protected AutoFitRecyclerView mGridView;
    protected TextView mTextViewNomedia;
    protected View mViewNomedia;
    protected String mGroup;
    protected String mFolderGroup;
    protected String mFolderTitle;
    private View mSearchButtonView;
    private VideoListAdapter mVideoAdapter;
    private DividerItemDecoration mDividerItemDecoration;


    //ad
    private AdView mAdView;

    /* All subclasses of Fragment must include a public empty constructor. */
    public VideoGridFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            setGroup(savedInstanceState.getString(KEY_GROUP));
            setFolderGroup(savedInstanceState.getString(KEY_FOLDER_GROUP), savedInstanceState.getString(KEY_FOLDER_TITLE));
        }

        int viewMode;
        if (mFolderGroup != null) {
            viewMode = VideoListAdapter.VIEW_MODE_FULL_TITLE;
        } else {
            viewMode = PreferenceManager.getDefaultSharedPreferences(
                    VLCApplication.getAppContext()).getInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE,
                    VideoListAdapter.VIEW_MODE_DEFAULT);
        }
        mVideoAdapter = new VideoListAdapter(this, viewMode);
        //加载feed流广告
        loadFeedNative();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.video_grid, container, false);

        // init the information for the scan (1/2)
        mLayoutFlipperLoading = (LinearLayout) v.findViewById(R.id.layout_flipper_loading);
        mTextViewNomedia = (TextView) v.findViewById(R.id.textview_nomedia);
        mViewNomedia = v.findViewById(android.R.id.empty);
        mGridView = (AutoFitRecyclerView) v.findViewById(android.R.id.list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);
        mSearchButtonView = v.findViewById(R.id.searchButton);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mDividerItemDecoration = new DividerItemDecoration(v.getContext(), DividerItemDecoration.VERTICAL);
        if (mVideoAdapter.getCurrentViewMode() == VideoListAdapter.VIEW_MODE_LIST)
            mGridView.addItemDecoration(mDividerItemDecoration);
        mGridView.setAdapter(mVideoAdapter);
        mAdView = (AdView) v.findViewById(R.id.adView);
        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mFolderGroup != null) {
            menu.findItem(R.id.ml_menu_view_mode).setVisible(false);
        }
    }


    public void onStart() {
        if (mMediaLibrary.isInitiated())
            onMedialibraryReady();
        else if (mGroup == null)
            setupMediaLibraryReceiver();
        super.onStart();
        mFabPlay.setImageResource(R.drawable.ic_fab_play);
        registerForContextMenu(mGridView);
    }

    @Override
    public void onResume() {
        super.onResume();
        setSearchVisibility(false);
        updateViewMode(mVideoAdapter.getCurrentViewMode());
        loadBanner();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaLibrary.removeMediaUpdatedCb();
        mMediaLibrary.removeMediaAddedCb();
        unregisterForContextMenu(mGridView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_GROUP, mGroup);
        outState.putString(KEY_FOLDER_GROUP, mFolderGroup);
        outState.putString(KEY_FOLDER_TITLE, mFolderTitle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoAdapter.clear();
    }

    protected void onMedialibraryReady() {
        super.onMedialibraryReady();
        if (mGroup == null) {
            mMediaLibrary.setMediaUpdatedCb(this, Medialibrary.FLAG_MEDIA_UPDATED_VIDEO);
            mMediaLibrary.setMediaAddedCb(this, Medialibrary.FLAG_MEDIA_ADDED_VIDEO);
        }
        mHandler.sendEmptyMessage(UPDATE_LIST);
    }

    protected String getTitle() {
        if (mGroup == null && mFolderGroup == null)
            return getString(R.string.video);
        else if (mGroup != null)
            return mGroup + "\u2026";
        else
            return mFolderTitle;
    }

    private void updateViewMode(int targetViewMode) {
        if (getView() == null || getActivity() == null) {
            Log.w(TAG, "Unable to setup the view");
            return;
        }
        Resources res = getResources();
        boolean listMode = targetViewMode != VideoListAdapter.VIEW_MODE_GRID;
//        listMode |= res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT &&
//                PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("force_list_portrait", false);
        // Compute the left/right padding dynamically
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        // Select between grid or list
        if (!listMode) {
            int thumbnailWidth = res.getDimensionPixelSize(R.dimen.grid_card_thumb_width);
            mGridView.setColumnWidth(mGridView.getPerfectColumnWidth(thumbnailWidth, res.getDimensionPixelSize(R.dimen.default_margin)));
            mVideoAdapter.setGridCardWidth(mGridView.getColumnWidth());
        }
        mGridView.setNumColumns(listMode ? 1 : -1);
        if (mVideoAdapter.isListMode() != listMode) {
            if (listMode && targetViewMode != VideoListAdapter.VIEW_MODE_BIGPIC && targetViewMode != VideoListAdapter.VIEW_MODE_FULL_TITLE)
                mGridView.addItemDecoration(mDividerItemDecoration);
            else
                mGridView.removeItemDecoration(mDividerItemDecoration);
            mVideoAdapter.setListMode(listMode);
        }
    }

    protected void playVideo(MediaWrapper media, boolean fromStart) {
        Activity activity = getActivity();
        if (activity instanceof PlaybackService.Callback)
            mService.removeCallback((PlaybackService.Callback) activity);
        media.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
        VideoPlayerActivity.start(getActivity(), media.getUri(), fromStart);
    }

    protected void playAudio(MediaWrapper media) {
        if (mService != null) {
            media.addFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
            mService.load(media);
        }
    }

    protected boolean handleContextItemSelected(MenuItem menu, final int position) {
        if (position >= mVideoAdapter.getItemCount())
            return false;
        final MediaWrapper media = mVideoAdapter.getItem(position);
        if (media == null)
            return false;
        switch (menu.getItemId()){
            case R.id.video_list_play_from_start:
                playVideo(media, true);
                return true;
            case R.id.video_list_play_audio:
                playAudio(media);
                return true;
            case R.id.video_list_play_all:
                ArrayList<MediaWrapper> playList = new ArrayList<>();
                MediaUtils.openList(getActivity(), playList, mVideoAdapter.getListWithPosition(playList, position));
                return true;
            case R.id.video_list_info:
                showInfoDialog(media);
                return true;
            case R.id.video_list_rename:
                renameVideo(media);
                return true;
            case R.id.video_list_delete:
                removeVideo(media);
                return true;
            case R.id.video_group_play:
                MediaUtils.openList(getActivity(), ((MediaGroup) media).getAll(), 0);
                return true;
            case R.id.video_list_append:
                if (media instanceof MediaGroup)
                    mService.append(((MediaGroup)media).getAll());
                else
                    mService.append(media);
                return true;
            case R.id.video_download_subtitles:
                MediaUtils.getSubs(getActivity(), media);
                return true;
        }
        return false;
    }

    private void renameVideo(final MediaWrapper media) {

        final RenameFileFragment fragment = new RenameFileFragment();
        fragment.setMedia(media);
        fragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                String savedName = fragment.getSavedName();
                if (!TextUtils.isEmpty(savedName) && !TextUtils.equals(savedName, media.getFileName())) {
                    String path = media.getUri().getPath();
                    String parentPath = FileUtils.getParent(path);

                    String dstFilePath = parentPath + "/" + savedName;

                    MediaWrapper dstMedia = new MediaWrapper(
                            Uri.parse(dstFilePath), media.getTime(), media.getLength(), media.getType(),
                            media.getPicture(), savedName, media.getArtist(), media.getGenre(), media.getAlbum(), media.getAlbumArtist(),
                            media.getWidth(), media.getHeight(), media.getArtworkURL(), media.getAudioTrack(), media.getSpuTrack(), media.getTrackNumber(),
                            media.getDiscNumber(), media.getLastModified());
                    mVideoAdapter.updateVideo(media, dstMedia);
                    renameMedia(media, dstMedia, false);
                }
            }
        });
        fragment.show(getFragmentManager(), "rename_file");
    }

    private void removeVideo(final MediaWrapper media) {
        mVideoAdapter.remove(media);
        if (getView() != null)
            UiTools.snackerWithCancel(getView(), getString(R.string.file_deleted), new Runnable() {
                @Override
                public void run() {
                    deleteMedia(media, false);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    mVideoAdapter.add(media);
                }
            });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (menuInfo == null)
            return;
        // Do not show the menu of media group.
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo)menuInfo;
        MediaWrapper media = mVideoAdapter.getItem(info.position);
        if (media == null)
            return;
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(media instanceof MediaGroup ? R.menu.video_group_contextual : R.menu.video_list, menu);
        if (media instanceof MediaGroup) {
            if (!AndroidUtil.isHoneycombOrLater) {
                menu.findItem(R.id.video_list_append).setVisible(false);
                menu.findItem(R.id.video_group_play).setVisible(false);
            }
        } else
            setContextMenuItems(menu, media);
    }

    private void setContextMenuItems(Menu menu, MediaWrapper mediaWrapper) {
        long lastTime = mediaWrapper.getTime();
        if (lastTime > 0)
            menu.findItem(R.id.video_list_play_from_start).setVisible(true);

        boolean hasInfo = false;
        final Media media = new Media(VLCInstance.get(), mediaWrapper.getUri());
        media.parse();
        boolean canWrite = FileUtils.canWrite(mediaWrapper.getLocation());
        if (media.getMeta(Media.Meta.Title) != null)
            hasInfo = true;
        media.release();
        menu.findItem(R.id.video_list_info).setVisible(hasInfo);
        menu.findItem(R.id.video_list_delete).setVisible(canWrite);
        if (!AndroidUtil.isHoneycombOrLater) {
            menu.findItem(R.id.video_list_play_all).setVisible(false);
            menu.findItem(R.id.video_list_append).setVisible(false);
        }
    }

    @Override
    public void onFabPlayClick(View view) {
        ArrayList<MediaWrapper> playList = new ArrayList<>();
        MediaUtils.openList(getActivity(), playList, mVideoAdapter.getListWithPosition(playList, 0));
    }

    @Override
    public void onMediaUpdated(final MediaWrapper[] mediaList) {
        int count = 0;
        for (MediaWrapper media : mediaList) {
            LogUtil.d(TAG, "xxxx onMediaUpdated [" + count++ + "] " + media.getUri().getPath() + " " + media.getArtworkMrl());
        }
        updateItems(mediaList);
    }

    @Override
    public void onMediaAdded(final MediaWrapper[] mediaList) {
        int count = 0;
        for (MediaWrapper media : mediaList) {
            LogUtil.d(TAG, "xxxx onMediaAdded [" + count++ + "] " + media.getUri().getPath() + " " + media.getArtworkMrl());
        }
        updateItems(mediaList);
    }

    public void updateItems(final MediaWrapper[] mediaList) {
        VLCApplication.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mVideoAdapter.update(mediaList);
                updateEmptyView();
            }
        });
    }

    @MainThread
    public void updateList() {
        mHandler.sendEmptyMessageDelayed(SET_REFRESHING, 300);

        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                final MediaWrapper[] itemList = mMediaLibrary.getVideos();
                final ArrayList<MediaWrapper> displayList = new ArrayList<>();
                int count = 0;
//                for (MediaWrapper item : itemList) {
//                    Log.d(TAG, "[" + count++ + "] uri_path: " + item.getUri().getPath() +
//                            " dir: " + FileUtils.getParent(item.getUri().getPath()) +
//                            " folder: " + item.getUri().getPathSegments().get(item.getUri().getPathSegments().size()-2) +
//                            " lastPathSegment: " + item.getUri().getLastPathSegment());
//                }
                if (mFolderGroup != null) {
                    for (MediaWrapper item : itemList) {
                        String path = FileUtils.getParent(item.getUri().getPath());
                        if (path.equals(mFolderGroup)) {
                            displayList.add(item);
                        }
                    }
                } else if (mGroup != null || itemList.length <= 10) {
                    for (MediaWrapper item : itemList) {
                        String title = item.getTitle().substring(item.getTitle().toLowerCase().startsWith("the") ? 4 : 0);
                        if (mGroup == null || title.toLowerCase().startsWith(mGroup.toLowerCase()))
                            displayList.add(item);
                    }
                } else {
                    for (MediaGroup item : MediaGroup.group(itemList))
                        displayList.add(item.getMedia());
                }
                VLCApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoAdapter.update(displayList, false);
                    }
                });
                mHandler.sendEmptyMessage(UNSET_REFRESHING);
            }
        });
    }

    void updateEmptyView() {
        mViewNomedia.setVisibility(mVideoAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void sortBy(int sortby) {
        mVideoAdapter.sortBy(sortby);
    }

    @Override
    public int sortDirection(int sortby) {
        return mVideoAdapter.sortDirection(sortby);
    }

    public void setGroup(String prefix) {
        mGroup = prefix;
    }

    public void setFolderGroup(String folder, String title) {
        mFolderGroup = folder;
        mFolderTitle = title;
    }

    public String getFolderGroup() {
        return mFolderGroup;
    }

    @Override
    public void onRefresh() {
        getActivity().startService(new Intent(MediaParsingService.ACTION_RELOAD, null, getActivity(), MediaParsingService.class));
    }

    @Override
    public void display() {}

    public void clear(){
        mVideoAdapter.clear();
    }

    @Override
    public void setFabPlayVisibility(boolean enable) {
        super.setFabPlayVisibility(!mVideoAdapter.isEmpty() && enable);
    }

    @Override
    protected void onParsingServiceStarted() {
        mHandler.sendEmptyMessageDelayed(SET_REFRESHING, 300);
    }

    @Override
    protected void onParsingServiceFinished() {
        LogUtil.d(TAG, "xxxx onParsingServiceFinished");
        mHandler.sendEmptyMessage(UPDATE_LIST);
    }

    @Override
    public boolean enableSearchOption() {
        return true;
    }

    @Override
    public Filter getFilter() {
        return mVideoAdapter.getFilter();
    }

    @Override
    public void restoreList() {
        mVideoAdapter.restoreList();
    }

    @Override
    public void setSearchVisibility(boolean visible) {
        mSearchButtonView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_mode_video, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int count = mVideoAdapter.getSelectionCount();
        if (count == 0) {
            stopActionMode();
            return false;
        }
        menu.findItem(R.id.action_video_info).setVisible(count == 1);
        menu.findItem(R.id.action_video_play).setVisible(AndroidUtil.isHoneycombOrLater || count == 1);
        menu.findItem(R.id.action_video_append).setVisible(mService.hasMedia() && AndroidUtil.isHoneycombOrLater);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<MediaWrapper> list = mVideoAdapter.getSelection();
        if (!list.isEmpty()) {
            switch (item.getItemId()) {
                case R.id.action_video_play:
                    MediaUtils.openList(getActivity(), list, 0);
                    break;
                case R.id.action_video_append:
                    MediaUtils.appendMedia(getActivity(), list);
                    break;
                case R.id.action_video_info:
                    showInfoDialog(list.get(0));
                    break;
    //            case R.id.action_video_delete:
    //                for (int position : mVideoAdapter.getSelectedPositions())
    //                    removeVideo(position, mVideoAdapter.getItem(position));
    //                break;
                case R.id.action_video_download_subtitles:
                    MediaUtils.getSubs(getActivity(), list);
                    break;
                case R.id.action_video_play_audio:
                    for (MediaWrapper media : list)
                        media.addFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
                    MediaUtils.openList(getActivity(), list, 0);
                    break;
                default:
                    stopActionMode();
                    return false;
            }
        }
        stopActionMode();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        ArrayList<MediaWrapper> items = mVideoAdapter.getAll();
        for (int i = 0; i < items.size(); ++i) {
            MediaWrapper mw = items.get(i);
            if (mw.hasStateFlags(MediaLibraryItem.FLAG_SELECTED)) {
                mw.removeStateFlags(MediaLibraryItem.FLAG_SELECTED);
                mVideoAdapter.resetSelectionCount();
                mVideoAdapter.notifyItemChanged(i, VideoListAdapter.UPDATE_SELECTION);
            }
        }
    }

    private static final int UPDATE_LIST = 14;
    private static final int SET_REFRESHING = 15;
    private static final int UNSET_REFRESHING = 16;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST:
                    removeMessages(UPDATE_LIST);
                    updateList();
                    break;
                case SET_REFRESHING:
                    mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case UNSET_REFRESHING:
                    removeMessages(SET_REFRESHING);
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    public void onClick(View v, int position, MediaLibraryItem item) {
        MediaWrapper media = (MediaWrapper) item;
            if (mActionMode != null) {
                item.toggleStateFlag(MediaLibraryItem.FLAG_SELECTED);
                mVideoAdapter.updateSelectionCount(item.hasStateFlags(MediaLibraryItem.FLAG_SELECTED));
                mVideoAdapter.notifyItemChanged(position, VideoListAdapter.UPDATE_SELECTION);
                invalidateActionMode();
                return;
            }
            Activity activity = getActivity();
            if (media instanceof MediaGroup) {
                String title = media.getTitle().substring(media.getTitle().toLowerCase().startsWith("the") ? 4 : 0);
                ((MainActivity)activity).showSecondaryFragment(SecondaryActivity.VIDEO_GROUP_LIST, title);
            } else {
                media.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
                if (settings.getBoolean("force_play_all", false)) {
                    ArrayList<MediaWrapper> playList = new ArrayList<>();
                    MediaUtils.openList(activity, playList, mVideoAdapter.getListWithPosition(playList, position));
                } else {
                    playVideo(media, false);
                }
            }
    }

    @Override
    public boolean onLongClick(View v, int position, MediaLibraryItem item) {
            if (mActionMode != null)
                return false;
            item.toggleStateFlag(MediaLibraryItem.FLAG_SELECTED);
            mVideoAdapter.updateSelectionCount(item.hasStateFlags(MediaLibraryItem.FLAG_SELECTED));
            mVideoAdapter.notifyItemChanged(position, VideoListAdapter.UPDATE_SELECTION);
            startActionMode();
            return true;
    }

    @Override
    public void onCtxClick(View v, int position, MediaLibraryItem item) {
            if (mActionMode != null)
                return;
            mGridView.openContextMenu(position);
    }

    @Override
    public void onUpdateFinished(RecyclerView.Adapter adapter) {
        LogUtil.d(TAG, "xxxx onUpdateFinished mMediaLibrary.isWorking() : " + mMediaLibrary.isWorking());
        if (!mMediaLibrary.isWorking())
            mHandler.sendEmptyMessage(UNSET_REFRESHING);
        updateEmptyView();
        setFabPlayVisibility(true);
    }

    public void toggleViewMode(MenuItem item) {
        if (mVideoAdapter != null) {
            int targetViewMode = (mVideoAdapter.getCurrentViewMode() + 1) % VideoListAdapter.VIEW_MODE_MAX;
            if (targetViewMode == VideoListAdapter.VIEW_MODE_GRID) {
                item.setIcon(R.drawable.ic_view_grid);
                mGridView.removeItemDecoration(mDividerItemDecoration);

                StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_GRID, null);

            } else if (targetViewMode == VideoListAdapter.VIEW_MODE_LIST) {
                item.setIcon(R.drawable.ic_view_list);
                mGridView.addItemDecoration(mDividerItemDecoration);

                StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_LIST, null);

            } else if (targetViewMode == VideoListAdapter.VIEW_MODE_BIGPIC) {
                item.setIcon(R.drawable.ic_view_bigpic);
                mGridView.removeItemDecoration(mDividerItemDecoration);

                StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_BIGPIC, null);

            }
            updateViewMode(targetViewMode);
            mVideoAdapter.toggleViewMode(targetViewMode);
        }
    }

    private void loadBanner(){
        if (ADManager.isShowGoogleVideoBanner) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.i("Ads", "onAdLoaded");
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.i("Ads", "onAdFailedToLoad");
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                    Log.i("Ads", "onAdOpened");
                    StatisticsManager.submitAd(getActivity(), StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_GOOGLE_VIDEO_BANNER);
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    Log.i("Ads", "onAdLeftApplication");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                    Log.i("Ads", "onAdClosed");
                }
            });

            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }
    public void loadFeedNative(){
        NativeAD mFeedNativeAD = new NativeAD();
        mFeedNativeAD.loadAD(getActivity(), ADManager.AD_Facebook, ADConstants.facebook_video_feed_native, new NativeAD.ADListener() {
            @Override
            public void onLoadedSuccess(com.facebook.ads.NativeAd ad) {
                // TODO: 2017/10/8
            }

            @Override
            public void onLoadedFailed(String msg) {

            }

            @Override
            public void onAdClick() {

            }
        });
    }

    public int getCurrentViewMode() {
        int mode = 0;
        if (mVideoAdapter != null) {
            mode = mVideoAdapter.getCurrentViewMode();
        }
        return mode;
    }
}
