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
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.PopupMenu;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.MediaParsingService;
import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.ad.ADConstants;
import com.wenjoyai.tubeplayer.ad.ADManager;
import com.wenjoyai.tubeplayer.ad.BannerAD;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.MainActivity;
import com.wenjoyai.tubeplayer.gui.RenameFileFragment;
import com.wenjoyai.tubeplayer.gui.SecondaryActivity;
import com.wenjoyai.tubeplayer.gui.browser.MediaBrowserFragment;
import com.wenjoyai.tubeplayer.gui.helpers.RecyclerViewHeader;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.gui.view.AutoFitRecyclerView;
import com.wenjoyai.tubeplayer.gui.view.ContextMenuRecyclerView;
import com.wenjoyai.tubeplayer.gui.view.SwipeRefreshLayout;
import com.wenjoyai.tubeplayer.interfaces.Filterable;
import com.wenjoyai.tubeplayer.interfaces.IEventsHandler;
import com.wenjoyai.tubeplayer.interfaces.ISortable;
import com.wenjoyai.tubeplayer.media.FolderGroup;
import com.wenjoyai.tubeplayer.media.Group;
import com.wenjoyai.tubeplayer.media.MediaGroup;
import com.wenjoyai.tubeplayer.media.MediaUtils;
import com.wenjoyai.tubeplayer.util.FileUtils;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.ShareUtils;
import com.wenjoyai.tubeplayer.util.VLCInstance;
import com.wenjoyai.tubeplayer.widget.MyBottomSheetDialog;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.interfaces.MediaAddedCb;
import org.videolan.medialibrary.interfaces.MediaUpdatedCb;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoGridFragment extends MediaBrowserFragment implements MediaUpdatedCb, ISortable, SwipeRefreshLayout.OnRefreshListener, MediaAddedCb, Filterable, IEventsHandler {

    public final static String TAG = "VLC/VideoGridFragment";

    public final static String KEY_GROUP = "key_group";
    public final static String KEY_FOLDER_GROUP = "key_folder_group";
    public final static String KEY_FOLDER_MAIN = "key_folder_main";

    public static final String KEY_STAT_VIDEO_COUNT = "stat_video_count";
    public static final String KEY_PARSING_ONCE = "parsing_once";

    protected LinearLayout mLayoutFlipperLoading;
    protected AutoFitRecyclerView mGridView;
    protected TextView mTextViewNomedia;
    protected View mViewNomedia;
    protected String mGroup;
    protected String mFolderGroup;
    protected boolean mFolderMain;
    private View mSearchButtonView;
    private VideoListAdapter mVideoAdapter;
    private DividerItemDecoration mDividerItemDecoration;


    //ad
    private FrameLayout mAdContainer;
    private BannerAD mBannerAD;
    //    private boolean mAdLoaded = false;
    private boolean mShowAd = false;

    private List<NativeAd> mNativeAdList = null;

    private RecyclerViewHeader mHeader;
    private LinearLayout mDirectories;

    final static int ID_PLAY_FROM_START = 1;
    final static int ID_PLAY_ALL = 2;
    //    final static int ID_APPEND = 3;
    final static int ID_PLAY_AS_AUDIO = 4;
    final static int ID_RENAME = 5;
    final static int ID_DELETE = 6;
    final static int ID_SHARE = 7;
    final static int ID_INFO = 8;
    final static int ID_GROUP_PLAY = 9;
    final static MyMenuItem[] menusVideo = {
            new MyMenuItem(ID_PLAY_ALL, R.string.play, R.drawable.ic_listmenu_play),
            new MyMenuItem(ID_INFO, R.string.info, R.drawable.ic_listmenu_info),
            new MyMenuItem(ID_SHARE, R.string.share, R.drawable.ic_listmenu_share),
            new MyMenuItem(ID_RENAME, R.string.rename, R.drawable.ic_listmenu_rename),
            new MyMenuItem(ID_DELETE, R.string.delete, R.drawable.ic_listmenu_delete),
            new MyMenuItem(ID_PLAY_AS_AUDIO, R.string.play_as_audio, R.drawable.ic_listmenu_audio),
    };
    final static MyMenuItem[] menusGroup = {
            new MyMenuItem(ID_GROUP_PLAY, R.string.play, R.drawable.ic_listmenu_play),
    };

    /* All subclasses of Fragment must include a public empty constructor. */
    public VideoGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            setGroup(savedInstanceState.getString(KEY_GROUP));
            setFolder(savedInstanceState.getString(KEY_FOLDER_GROUP), savedInstanceState.getBoolean(KEY_FOLDER_MAIN));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

        if (mGroup != null || mFolderGroup != null || mFolderMain) {
            mHeader = (RecyclerViewHeader) v.findViewById(R.id.header);
            mHeader.attachTo(mGridView);
            mHeader.setVisibility(View.VISIBLE);

            mDirectories = (LinearLayout) mHeader.findViewById(R.id.folder_directories);
            if (mFolderMain) {
                mDirectories.setVisibility(View.VISIBLE);
                mDirectories.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).showSecondaryFragment(SecondaryActivity.FILE_BROWSER);
                    }
                });
            } else {
                mDirectories.setVisibility(View.GONE);
            }
        }

        if (mFolderMain) {
            mGridView.setPadding(0, 0, mGridView.getPaddingRight(), mGridView.getPaddingBottom());
        }

        if (mVideoAdapter == null) {
            mVideoAdapter = new VideoListAdapter(this);
        }
        if (mVideoAdapter.getCurrentViewMode() != -1) {
            updateViewMode(mVideoAdapter.getCurrentViewMode());
        }
        mGridView.setAdapter(mVideoAdapter);

        mAdContainer = (FrameLayout) v.findViewById(R.id.adContainer);
        return v;
    }

    public void popMenu(final int position) {
        if (position < 0)
            return;

        final MediaWrapper media = mVideoAdapter.getItem(position);
        if (media == null)
            return;

        View popView = LayoutInflater.from(getContext()).inflate(R.layout.context_menu, null);
        ((TextView) popView.findViewById(R.id.title)).setText(media.getTitle());
        final BottomSheetDialog dialog = new MyBottomSheetDialog(getContext());
        dialog.setOwnerActivity(getActivity());
        dialog.setContentView(popView);

        MyMenuItem[] tmpMenus = media instanceof MediaGroup ? menusGroup : menusVideo;
        MyMenuItem[] myMenus = Arrays.copyOf(tmpMenus, tmpMenus.length);
        if (media instanceof Group) {
            if (!AndroidUtil.isHoneycombOrLater) {
//                menu.findItem(R.id.video_list_append).setVisible(false);
//                menu.findItem(R.id.video_group_play).setVisible(false);
                myMenus = new MyMenuItem[0];
            }
        } else {
            myMenus = setPopupWindowItems(myMenus, media);
        }

        final MyMenuItem[] menus = myMenus;
        ListView menuList = (ListView) popView.findViewById(R.id.menu_list);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int menuPos, long id) {
                MyMenuItem myMenuItem = menus[menuPos];
                switch (myMenuItem.getId()) {
                    case ID_PLAY_FROM_START:
                        playVideo(media, true);
                        break;
                    case ID_PLAY_AS_AUDIO:
                        playAudio(media);
                        break;
                    case ID_PLAY_ALL:
                        ArrayList<MediaWrapper> playList = new ArrayList<>();
                        MediaUtils.openList(getActivity(), playList, mVideoAdapter.getListWithPosition(playList, position));
                        break;
                    case ID_INFO: {
                        View itemView = mGridView.getLayoutManager().findViewByPosition(position);
                        ImageView thumb = (ImageView) itemView.findViewById(R.id.ml_item_thumbnail);
                        showInfoDialog(media, thumb);
                    }
                    break;
                    case ID_RENAME:
                        renameVideo(media);
                        break;
                    case ID_DELETE:
                        removeVideo(media);
                        break;
                    case ID_GROUP_PLAY:
                        MediaUtils.openList(getActivity(), ((Group) media).getAll(), 0);
                        break;
//                    case ID_APPEND:
//                        if (media instanceof MediaGroup)
//                            mService.append(((MediaGroup) media).getAll());
//                        else
//                            mService.append(media);
//                        break;
//                    case R.id.video_download_subtitles:
//                        MediaUtils.getSubs(getActivity(), media);
//                        break;
                    case ID_SHARE:
                        ShareUtils.shareMedia(getActivity() != null ? getActivity() : VLCApplication.getAppContext(), media);
                        break;
                }
//                popupWindow.dismiss();
                dialog.dismiss();
            }
        });
        menuList.setAdapter(new MenuAdapter(getContext(), R.layout.context_menu_item, Arrays.asList(menus)));

        // 点击弹出窗口
        dialog.show();
    }

    private MyMenuItem[] setPopupWindowItems(MyMenuItem[] myMenus, MediaWrapper mediaWrapper) {
//        long lastTime = mediaWrapper.getTime();
//        if (lastTime > 0)
//            menu.findItem(R.id.video_list_play_from_start).setVisible(true);

        boolean hasInfo = false;
        final Media media = new Media(VLCInstance.get(), mediaWrapper.getUri());
        media.parse();
        boolean canWrite = FileUtils.canWrite(mediaWrapper.getLocation());
        if (media.getMeta(Media.Meta.Title) != null)
            hasInfo = true;
        media.release();
        findItem(myMenus, ID_INFO).setValid(hasInfo);
        findItem(myMenus, ID_DELETE).setValid(canWrite);
        if (!AndroidUtil.isHoneycombOrLater) {
            findItem(myMenus, ID_PLAY_ALL).setValid(false);
//            findItem(myMenus, ID_APPEND).setValid(false);
        }
        List<MyMenuItem> myMenuList = new ArrayList<>();
        for (MyMenuItem myMenuItem : myMenus) {
            if (myMenuItem.isValid()) {
                myMenuList.add(myMenuItem);
            }
        }
        return myMenuList.toArray(new MyMenuItem[myMenuList.size()]);
    }

    MyMenuItem findItem(MyMenuItem[] myMenus, int id) {
        for (MyMenuItem myMenu : myMenus) {
            if (myMenu.getId() == id) {
                return myMenu;
            }
        }
        return null;
    }


    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha 屏幕透明度0.0-1.0 1表示完全不透明
     */
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
//        if (mFolderGroup != null) {
//            menu.findItem(R.id.ml_menu_view_mode).setVisible(false);
//        }
    }

    public void onStart() {
        LogUtil.d(TAG, "aaaa onStart video size:" + mVideoAdapter.getItemCount());

        if (mMediaLibrary.isInitiated()) {
            LogUtil.d(TAG, "aaaa onStart mMediaLibrary isInitiated");
            Log.e("yNativeAD", "aaaa onStart mMediaLibrary isInitiated onMedialibraryReady");
            onMedialibraryReady();
        } else if (mGroup == null && mFolderGroup == null) {
            LogUtil.d(TAG, "aaaa onStart setupMediaLibraryReceiver");
            Log.e("yNativeAD", "aaaa onStart setupMediaLibraryReceiver");
            setupMediaLibraryReceiver();
        }
        super.onStart();
        mFabPlay.setImageResource(R.drawable.ic_fab_play);
        registerForContextMenu(mGridView);
    }

    @Override
    public void onResume() {
        LogUtil.d(TAG, "aaaa onResume video size:" + mVideoAdapter.getItemCount());

        super.onResume();
        setSearchVisibility(false);
        int viewMode;
        if (mFolderMain) {
            viewMode = VideoListAdapter.VIEW_MODE_FOLDER;
        } else {
            viewMode = PreferenceManager.getDefaultSharedPreferences(
                    VLCApplication.getAppContext()).getInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE,
                    VideoListAdapter.VIEW_MODE_DEFAULT);
        }
        toggleVideoMode(viewMode);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
        loadBanner();
        //加载feed流广告
        loadFeedNative();
//            }
//        },500);
    }

    @Override
    public void onStop() {
        LogUtil.d(TAG, "aaaa onStop");
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
        outState.putBoolean(KEY_FOLDER_MAIN, mFolderMain);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "aaaa onDestroy");

        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(mSwipeRefreshRunnable);
        }
//        mVideoAdapter.clear();
        if (null != mBannerAD) {
            mBannerAD.destroy();
        }
    }

    protected void onMedialibraryReady() {
        super.onMedialibraryReady();

        LogUtil.d(TAG, "aaaa onMedialibraryReady UPDATE_LIST");
        if (mGroup == null && mFolderGroup == null) {
            mMediaLibrary.setMediaUpdatedCb(VideoGridFragment.this, Medialibrary.FLAG_MEDIA_UPDATED_VIDEO);
            mMediaLibrary.setMediaAddedCb(VideoGridFragment.this, Medialibrary.FLAG_MEDIA_ADDED_VIDEO);
        }
        if (mFolderMain || mGroup != null || mFolderGroup != null || mParsingFinished || mParsed) {
            mHandler.sendEmptyMessage(UPDATE_LIST);
        }
    }

    protected String getTitle() {
        if (mFolderMain) {
            return getString(R.string.folders);
        }
        if (mGroup == null && mFolderGroup == null) {
            return getString(R.string.video);
        }
        if (mGroup != null)
            return mGroup + "\u2026";
        else
            return FolderGroup.getFolderTitle(mFolderGroup);
    }

    private void updateViewMode(int targetViewMode) {
//        if (getView() == null || getActivity() == null) {
//            Log.w(TAG, "Unable to setup the view");
//            return;
//        }
        Resources res = getResources();
        boolean listMode = targetViewMode != VideoListAdapter.VIEW_MODE_GRID;

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
            mVideoAdapter.setListMode(listMode);
        }
    }

    protected void playVideo(MediaWrapper media, boolean fromStart) {
        Activity activity = getActivity();
        if (activity instanceof PlaybackService.Callback && mService != null)
            mService.removeCallback((PlaybackService.Callback) activity);
        media.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
        if (mService != null && mService.isPlayingPopup()) {
            mService.load(media);
        } else {
            VideoPlayerActivity.start(getActivity(), media.getUri(), fromStart);
        }
    }

    protected void playVideo(MediaWrapper media, boolean fromStart, ImageView sharedImageView) {
        Activity activity = getActivity();
        if (activity instanceof PlaybackService.Callback)
            mService.removeCallback((PlaybackService.Callback) activity);
        media.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
        if (mService.isPlayingPopup()) {
            mService.load(media);
        } else {
            VideoPlayerActivity.start(getActivity(), media.getUri(), fromStart, sharedImageView, media);
        }
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
        switch (menu.getItemId()) {
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
            case R.id.video_list_info: {
                View itemView = mGridView.getLayoutManager().findViewByPosition(position);
                ImageView thumb = (ImageView) itemView.findViewById(R.id.ml_item_thumbnail);
                showInfoDialog(media, thumb);
            }
            return true;
            case R.id.video_list_rename:
                renameVideo(media);
                return true;
            case R.id.video_list_delete:
                removeVideo(media);
                return true;
            case R.id.video_group_play:
                MediaUtils.openList(getActivity(), ((Group) media).getAll(), 0);
                return true;
            case R.id.video_list_append:
                if (media instanceof MediaGroup)
                    mService.append(((MediaGroup) media).getAll());
                else
                    mService.append(media);
                return true;
            case R.id.video_download_subtitles:
                MediaUtils.getSubs(getActivity(), media);
                return true;
            case R.id.video_share:
                ShareUtils.shareMedia(getActivity() != null ? getActivity() : VLCApplication.getAppContext(), media);
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
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo;
        MediaWrapper media = mVideoAdapter.getItem(info.position);
        if (media == null)
            return;
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(media instanceof MediaGroup ? R.menu.video_group_contextual : R.menu.video_list_contextual, menu);
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
//        for (MediaWrapper media : mediaList) {
//            LogUtil.d(TAG, "xxxx onMediaUpdated [" + count++ + "] " + media.getUri().getPath() + " " + media.getArtworkMrl());
//        }
        Log.e("yNativeAD", "onMediaUpdated mediaList:" + mediaList.length);

        if (!mFolderMain && !mParsingFinished && mediaList.length > 0) {
            updateItems(mediaList);
        }
    }

    @Override
    public void onMediaAdded(final MediaWrapper[] mediaList) {
        int count = 0;
//        for (MediaWrapper media : mediaList) {
//            LogUtil.d(TAG, "xxxx onMediaAdded [" + count++ + "] " + media.getUri().getPath() + " " + media.getArtworkMrl());
//        }
        LogUtil.d(TAG, "onMediaAdded mediaList:" + mediaList.length);

        if (mediaList.length > 0) {
            updateItems(mediaList);
        }
    }

    public void updateItems(final MediaWrapper[] mediaList) {
        VLCApplication.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mFolderMain) {
                    mVideoAdapter.updateFolder(mediaList);
                } else {
                    mVideoAdapter.update(mediaList);
                }
                updateEmptyView();
            }
        });
    }

    @MainThread
    public void updateList() {
        LogUtil.d(TAG, "aaaa updateList SET_REFRESHING");
        mHandler.sendEmptyMessageDelayed(SET_REFRESHING, 300);

        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                final MediaWrapper[] itemList = mMediaLibrary.getVideos();
                final ArrayList<MediaWrapper> displayList = new ArrayList<>();

                LogUtil.d(TAG, "aaaa updateList getVideos size:" + itemList.length);
//                int count = 0;
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
                } else if (mGroup != null) {
                    for (MediaWrapper item : itemList) {
                        String title = item.getTitle().substring(item.getTitle().toLowerCase().startsWith("the") ? 4 : 0);
                        if (mGroup == null || title.toLowerCase().startsWith(mGroup.toLowerCase()))
                            displayList.add(item);
                    }
                } else {
                    if (mFolderMain) {
                        displayList.addAll(FolderGroup.getDummy().group(itemList));
                    } else {
                        if (mParsed) {
                            for (MediaWrapper item : MediaGroup.getDummy().group(itemList))
                                displayList.add(((MediaGroup) item).getMedia());
                        } else {
                            displayList.addAll(Arrays.asList(itemList));
                        }
                    }
                }
                if (mGroup == null && mFolderGroup == null && mParsingFinished && !mSubmitVideoCount) {
                    LogUtil.d(TAG, "xxxx updateList StatisticsManager displayList:" + displayList.size() + ", videoSize:" + itemList.length);
                    submitVideoCount(displayList.size(), itemList.length);
                }
                VLCApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoAdapter.setShowAds(mFolderGroup == null && mShowAd);
                        if (checkAds()) {
                            mVideoAdapter.setNativeAd(mNativeAdList);
                        }
                        LogUtil.d(TAG, "aaaa updateList displayList size:" + displayList.size());
                        mVideoAdapter.update(displayList, false);
                    }
                });

                mHandler.sendEmptyMessage(UNSET_REFRESHING);
            }
        });
    }

    private boolean mSubmitVideoCount = false;

    private void submitVideoCount(int group, int video) {
        if (group <= 0 || video <= 0) {
            LogUtil.e(TAG, "submitVideoCount group:" + group + ",video:" + video);
            return;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
        mSubmitVideoCount = settings.getBoolean(KEY_STAT_VIDEO_COUNT, false);
        if (!mSubmitVideoCount) {
            StatisticsManager.submitVideoCount(VLCApplication.getAppContext(), "group_" + StatisticsManager.getVideoCountRange(group));
            StatisticsManager.submitVideoCount(VLCApplication.getAppContext(), "video_" + StatisticsManager.getVideoCountRange(video));
            settings.edit().putBoolean(KEY_STAT_VIDEO_COUNT, true).apply();
            mSubmitVideoCount = true;
        }
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

    public void setFolder(String folderGroup, boolean folderMain) {
        mFolderGroup = folderGroup;
        mFolderMain = folderMain;
    }

    public String getFolderGroup() {
        return mFolderGroup;
    }

    public boolean getFolderMain() {
        return mFolderMain;
    }

    private Runnable mSwipeRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "aaaa onRefresh mParsingStarted:" + mParsingStarted + ", mParsingFinished:" +
                    mParsingFinished + ", isRefreshing:" + mSwipeRefreshLayout.isRefreshing());
            if (mParsingFinished && mSwipeRefreshLayout.isRefreshing()) {
                LogUtil.d(TAG, "aaaa onRefresh setRefreshing false");
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    };

    @Override
    public void onRefresh() {
        LogUtil.d(TAG, "aaaa onRefresh");
        mVideoAdapter.resetAdIndex();
        if (mHandler != null) {
            mHandler.postDelayed(mSwipeRefreshRunnable, 5000);
        }
        getActivity().startService(new Intent(MediaParsingService.ACTION_RELOAD, null, getActivity(), MediaParsingService.class));
    }

    @Override
    public void display() {
    }

    public void clear() {
        mVideoAdapter.clear();
    }

    @Override
    public void setFabPlayVisibility(boolean enable) {
        super.setFabPlayVisibility(!mVideoAdapter.isEmpty() && enable);
    }

    private boolean mParsingStarted = false;
    private boolean mParsingFinished = false;

    @Override
    protected void onParsingServiceStarted() {
        LogUtil.d(TAG, "aaaa onParsingServiceStarted");
        Log.e("yNativeAD", "aaaa onParsingServiceStarted");
        mParsingStarted = true;
        mParsingFinished = false;
//        Log.e("yNativeAD", "aaaa onParsingServiceStarted");
        mHandler.sendEmptyMessageDelayed(SET_REFRESHING, 300);
    }

    @Override
    protected void onParsingServiceFinished() {
        LogUtil.d(TAG, "aaaa onParsingServiceFinished");
        Log.e("yNativeAD", "aaaa onParsingServiceFinished");
        mParsingStarted = false;
        mParsingFinished = true;
        if (checkAds() && !mShowAd) {
            mShowAd = true;
        }

        if (!mParsed) {
            PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).edit().putBoolean(KEY_PARSING_ONCE, true).apply();
            mParsed = true;
        }

        mHandler.sendEmptyMessageDelayed(UPDATE_LIST, 1000);
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
            if (mw != null && mw.hasStateFlags(MediaLibraryItem.FLAG_SELECTED)) {

                mw.removeStateFlags(MediaLibraryItem.FLAG_SELECTED);
                mVideoAdapter.resetSelectionCount();
                mVideoAdapter.notifyItemChanged(i, VideoListAdapter.UPDATE_SELECTION);
            }
        }
    }

    private static final int UPDATE_LIST = 14;
    private static final int SET_REFRESHING = 15;
    private static final int UNSET_REFRESHING = 16;
    private static final int UPDATE_AD = 17;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST:
                    LogUtil.d(TAG, "aaaa handleMessage UPDATE_LIST");
                    removeMessages(UPDATE_LIST);
                    updateList();
                    break;
                case SET_REFRESHING:
                    LogUtil.d(TAG, "aaaa handleMessage SET_REFRESHING");
                    mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case UNSET_REFRESHING:
                    LogUtil.d(TAG, "aaaa handleMessage UNSET_REFRESHING");
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
            ((MainActivity) activity).showSecondaryFragment(SecondaryActivity.VIDEO_GROUP_LIST, title);
        } else if (media instanceof FolderGroup) {
            ((MainActivity) activity).showSecondaryFragment(SecondaryActivity.VIDEO_FOLDER_GROUP,
                    ((FolderGroup) item).getFolderPath());
        } else if (media != null) {
            media.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
            if (settings.getBoolean("force_play_all", false)) {
                ArrayList<MediaWrapper> playList = new ArrayList<>();
                MediaUtils.openList(activity, playList, mVideoAdapter.getListWithPosition(playList, position));
            } else {
                ImageView thumb = (ImageView) v.findViewById(R.id.ml_item_thumbnail);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && thumb != null) {
                    playVideo(media, false, thumb);
                } else {
                    playVideo(media, false);
                }
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
    public void onCtxClick(View v, final int position, MediaLibraryItem item) {
//        if (mActionMode != null)
//            return;
//        mGridView.openContextMenu(position);

        popMenu(position);
    }

    private void showPopupMenu(View anchor, int menuRes, PopupMenu.OnMenuItemClickListener onMenuItemClickListener) {
        PopupMenu popup = new PopupMenu(getActivity(), anchor);
        popup.getMenuInflater().inflate(menuRes, popup.getMenu());
        popup.setOnMenuItemClickListener(onMenuItemClickListener);
        popup.show();
    }

    @Override
    public void onUpdateFinished(RecyclerView.Adapter adapter) {
        LogUtil.d(TAG, "aaaa onUpdateFinished mMediaLibrary.isWorking() : " + mMediaLibrary.isWorking());
        if (!mMediaLibrary.isWorking())
            mHandler.sendEmptyMessage(UNSET_REFRESHING);
        updateEmptyView();
        setFabPlayVisibility(true);
    }

    public void toggleViewMode(MenuItem item) {
        if (mVideoAdapter != null) {
            int targetViewMode;
            FragmentActivity activity = getActivity();
            if (activity instanceof MainActivity) {
                targetViewMode = (mVideoAdapter.getCurrentViewMode() + 1) % VideoListAdapter.VIEW_MODE_MAX;
                if (targetViewMode == VideoListAdapter.VIEW_MODE_GRID) {
                    item.setIcon(R.drawable.ic_view_grid);

                    StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_GRID, null);
                    PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).edit().putInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE, VideoListAdapter.VIEW_MODE_GRID).apply();
                    ((MainActivity) activity).toggleFrgament(R.id.nav_video);
                    return;

                } else if (targetViewMode == VideoListAdapter.VIEW_MODE_LIST) {
                    item.setIcon(R.drawable.ic_view_list);

                    StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_LIST, null);

                } else if (targetViewMode == VideoListAdapter.VIEW_MODE_FOLDER) {
                    item.setIcon(R.drawable.ic_view_bigpic);

                    StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_BIGPIC, null);

                    ((MainActivity) activity).toggleFrgament(R.id.nav_directories);
                    return;
                }
            } else {
                targetViewMode = (mVideoAdapter.getCurrentViewMode() + 1) % (VideoListAdapter.VIEW_MODE_MAX - 1);
                if (targetViewMode == VideoListAdapter.VIEW_MODE_GRID) {
                    item.setIcon(R.drawable.ic_view_grid);

                    StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_GRID, null);

                } else if (targetViewMode == VideoListAdapter.VIEW_MODE_LIST) {
                    item.setIcon(R.drawable.ic_view_list);

                    StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.TYPE_VIEWER_LIST, null);

                }
            }

            toggleVideoMode(targetViewMode);
            PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).edit().putInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE, targetViewMode).apply();
        }
    }

    public void toggleVideoMode(int targetViewMode) {
        if (mVideoAdapter.getCurrentViewMode() != targetViewMode) {
            updateViewMode(targetViewMode);
            mVideoAdapter.toggleViewMode(targetViewMode);
        }
    }

    private void loadBanner() {
        String adID = "";
        if (ADManager.sPlatForm == ADManager.AD_MobVista) {
        } else if (ADManager.sPlatForm == ADManager.AD_Google) {
            adID = ADConstants.google_video_grid_bannar;
        } else if (ADManager.sPlatForm == ADManager.AD_Facebook) {
            adID = ADConstants.facebook_video_grid_bannar;
        }
        if (!TextUtils.isEmpty(adID)) {
            mBannerAD = new BannerAD();
            mBannerAD.loadAD(getActivity(), ADManager.sPlatForm, adID, new BannerAD.ADListener() {
                @Override
                public void onLoadedSuccess(View view) {
                    if (null != view) {
                        mAdContainer.removeAllViews();
                        mAdContainer.addView(view);
                    }
                    StatisticsManager.submitAd(getActivity(), StatisticsManager.TYPE_AD, StatisticsManager.ITEM_AD_GOOGLE_VIDEO_BANNER);
                }

                @Override
                public void onLoadedFailed() {

                }

                @Override
                public void onAdClick() {

                }

                @Override
                public void onAdClose() {

                }
            });
        }
    }

    private boolean checkAds() {
        return mNativeAdList != null && mNativeAdList.size() > 0;
    }

    private boolean mParsed = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext()).getBoolean(KEY_PARSING_ONCE, false);

    private ADManager.ADNumListener mAdNumListener = new ADManager.ADNumListener() {
        @Override
        public void onLoadedSuccess(List<NativeAd> list, boolean needGif) {
            mNativeAdList = list;
            Log.e("yNativeAD", "aaaa onLoadedSuccess list:" + (list == null ? 0 : list.size()));
            if (checkAds()) {
                mVideoAdapter.setNativeAd(mNativeAdList);
                if (mParsingFinished || mParsed) {
                    mHandler.sendEmptyMessage(UPDATE_LIST);
                }
            }

            if (needGif && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showGif(new NeedFreshListener() {
                    @Override
                    public void fresh() {
                        mVideoAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    public void loadFeedNative() {
        ADManager.getInstance().getNativeAdlist(mAdNumListener);
    }

    public int getCurrentViewMode() {
        int mode = 0;
        if (mVideoAdapter != null) {
            mode = mVideoAdapter.getCurrentViewMode();
        }
        return mode;
    }

    public interface NeedFreshListener {
        void fresh();
    }

    public static class MyMenuItem {

        private int id;
        private int name;
        private int imageId;
        private boolean valid = true;

        MyMenuItem(int id, int name, int imageId) {
            this.id = id;
            this.name = name;
            this.imageId = imageId;
        }

        int getId() {
            return id;
        }

        public int getName() {
            return name;
        }

        int getImageId() {
            return imageId;
        }

        boolean isValid() {
            return valid;
        }

        void setValid(boolean valid) {
            this.valid = valid;
        }
    }

    public static class MenuAdapter extends ArrayAdapter<MyMenuItem> {

        private int resId;

        public MenuAdapter(Context context, int resource, List<MyMenuItem> objects) {
            super(context, resource, objects);
            resId = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MyMenuItem item = getItem(position);
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resId, parent, false);
            } else {
                view = convertView;
            }
            ImageView itemImage = (ImageView) view.findViewById(R.id.image);
            TextView itemName = (TextView) view.findViewById(R.id.name);
            itemImage.setImageResource(item.getImageId());
            itemName.setText(item.getName());
            return view;
        }
    }
}
