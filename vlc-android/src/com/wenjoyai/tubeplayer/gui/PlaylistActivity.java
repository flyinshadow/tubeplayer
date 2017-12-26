/*
 * *************************************************************************
 *  PlaylistActivity.java
 * **************************************************************************
 *  Copyright © 2017 VLC authors and VideoLAN
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

package com.wenjoyai.tubeplayer.gui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.databinding.PlaylistActivityBinding;
import com.wenjoyai.tubeplayer.gui.audio.AudioBrowserAdapter;
import com.wenjoyai.tubeplayer.gui.audio.AudioBrowserFragment;
import com.wenjoyai.tubeplayer.gui.dialogs.SavePlaylistDialog;
import com.wenjoyai.tubeplayer.gui.helpers.AudioUtil;
import com.wenjoyai.tubeplayer.gui.helpers.FloatingActionButtonBehavior;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.gui.view.ContextMenuRecyclerView;
import com.wenjoyai.tubeplayer.interfaces.IEventsHandler;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.FileUtils;
import com.wenjoyai.tubeplayer.util.ShareUtils;
import com.wenjoyai.tubeplayer.widget.MyBottomSheetDialog;
import com.wenjoyai.tubeplayer.widget.MyMenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.wenjoyai.tubeplayer.widget.MyMenuItem.*;
import static com.wenjoyai.tubeplayer.widget.MyMenuItem.MenuAudioListBrowser.*;

public class PlaylistActivity extends AudioPlayerContainerActivity implements IEventsHandler, ActionMode.Callback, View.OnClickListener {

    public final static String TAG = "VLC/PlaylistActivity";
    public final static String TAG_FAB_VISIBILITY = "FAB";

    private AudioBrowserAdapter mAdapter;
    private MediaLibraryItem mPlaylist;
    private Medialibrary mMediaLibrary = VLCApplication.getMLInstance();
    private PlaylistActivityBinding mBinding;
    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.playlist_activity);

        initAudioPlayerContainerActivity();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPlaylist = (MediaLibraryItem) (savedInstanceState != null ?
                savedInstanceState.getParcelable(AudioBrowserFragment.TAG_ITEM) :
                getIntent().getParcelableExtra(AudioBrowserFragment.TAG_ITEM));
        mBinding.setPlaylist(mPlaylist);
        mAdapter = new AudioBrowserAdapter(this, MediaLibraryItem.TYPE_PLAYLIST, this, false);

        mBinding.songs.setLayoutManager(new LinearLayoutManager(this));
        mBinding.songs.setAdapter(mAdapter);
        final int fabVisibility = savedInstanceState != null
                ? savedInstanceState.getInt(TAG_FAB_VISIBILITY) : -1;

        if (mPlaylist != null && !TextUtils.isEmpty(mPlaylist.getArtworkMrl())) {
            VLCApplication.runBackground(new Runnable() {
                @Override
                public void run() {
                    final Bitmap cover = AudioUtil.readCoverBitmap(Uri.decode(mPlaylist.getArtworkMrl()), 0);
                    if (cover != null) {
                        mBinding.setCover(new BitmapDrawable(PlaylistActivity.this.getResources(), cover));
                        VLCApplication.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                mBinding.appbar.setExpanded(true, true);
                                if (fabVisibility != -1)
                                    mBinding.fab.setVisibility(fabVisibility);
                            }
                        });
                    } else
                        fabFallback();
                }
            });
        } else
            fabFallback();
        mBinding.fab.setOnClickListener(this);
    }

    private void fabFallback() {
        mBinding.appbar.setExpanded(false);
        ViewCompat.setNestedScrollingEnabled(mBinding.songs, false);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mBinding.fab.getLayoutParams();
        lp.setAnchorId(mBinding.songs.getId());
        lp.anchorGravity = Gravity.BOTTOM | Gravity.RIGHT | Gravity.END;
        lp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.default_margin);
        lp.setBehavior(new FloatingActionButtonBehavior(PlaylistActivity.this, null));
        mBinding.fab.setLayoutParams(lp);
        mBinding.fab.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mFragmentContainer = mBinding.songs;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateList();
        registerForContextMenu(mBinding.songs);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopActionMode();
        unregisterForContextMenu(mBinding.songs);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(AudioBrowserFragment.TAG_ITEM, mPlaylist);
        outState.putInt(TAG_FAB_VISIBILITY, mBinding.fab.getVisibility());
        super.onSaveInstanceState(outState);
    }

    private void updateList() {
        if (mPlaylist != null)
            mAdapter.addAll(mPlaylist.getTracks(mMediaLibrary));
    }

    @Override
    public void onClick(View v, int position, MediaLibraryItem item) {
        if (mActionMode != null) {
            item.toggleStateFlag(MediaLibraryItem.FLAG_SELECTED);
            mAdapter.updateSelectionCount(item.hasStateFlags(MediaLibraryItem.FLAG_SELECTED));
            mAdapter.notifyItemChanged(position, item);
            invalidateActionMode();
        } else if (mService != null)
            mService.load(mPlaylist.getTracks(mMediaLibrary), position);
    }

    @Override
    public boolean onLongClick(View v, int position, MediaLibraryItem item) {
        if (mActionMode != null)
            return false;
        item.toggleStateFlag(MediaLibraryItem.FLAG_SELECTED);
        mAdapter.updateSelectionCount(item.hasStateFlags(MediaLibraryItem.FLAG_SELECTED));
        mAdapter.notifyItemChanged(position, item);
        startActionMode();
        return true;
    }

    @Override
    public void onCtxClick(View anchor, final int position, final MediaLibraryItem mediaItem) {
        if (mActionMode == null)
//            mBinding.songs.openContextMenu(position);
            popMenu(position);

    }

    @Override
    public void onUpdateFinished(RecyclerView.Adapter adapter) {
    }

    @Override
    protected void onPlayerStateChanged(View bottomSheet, int newState) {
        int visibility = mBinding.fab.getVisibility();
        if (visibility == View.VISIBLE && newState != BottomSheetBehavior.STATE_COLLAPSED && newState != BottomSheetBehavior.STATE_HIDDEN)
            mBinding.fab.setVisibility(View.INVISIBLE);
        else if (visibility == View.INVISIBLE && (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN))
            mBinding.fab.show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void startActionMode() {
        mActionMode = startSupportActionMode(this);
    }

    protected void stopActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            onDestroyActionMode(mActionMode);
        }
    }

    public void invalidateActionMode() {
        if (mActionMode != null)
            mActionMode.invalidate();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_mode_audio_browser, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int count = mAdapter.getSelectionCount();
        if (count == 0) {
            stopActionMode();
            return false;
        }
        boolean isSong = count == 1 && mAdapter.getSelection().get(0).getItemType() == MediaLibraryItem.TYPE_MEDIA;
        menu.findItem(R.id.action_mode_audio_set_song).setVisible(isSong && AndroidDevices.isPhone());
        menu.findItem(R.id.action_mode_audio_info).setVisible(isSong);
        menu.findItem(R.id.action_mode_audio_append).setVisible(mService.hasMedia());
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<MediaLibraryItem> list = mAdapter.getSelection();
        ArrayList<MediaWrapper> tracks = new ArrayList<>();
        for (MediaLibraryItem mediaItem : list)
            tracks.addAll(Arrays.asList(mediaItem.getTracks(mMediaLibrary)));
        stopActionMode();
        switch (item.getItemId()) {
            case R.id.action_mode_audio_play:
                mService.load(tracks, 0, null);
                break;
            case R.id.action_mode_audio_append:
                mService.append(tracks);
                break;
            case R.id.action_mode_audio_add_playlist:
                ;
                UiTools.addToPlaylist(this, tracks);
                break;
            case R.id.action_mode_audio_info:
                showInfoDialog((MediaWrapper) list.get(0));
                break;
            case R.id.action_mode_audio_set_song:
                AudioUtil.setRingtone((MediaWrapper) list.get(0), this);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        MediaLibraryItem[] items = mAdapter.getAll();
        if (items != null) {
            for (int i = 0; i < items.length; ++i) {
                if (items[i].hasStateFlags(MediaLibraryItem.FLAG_SELECTED)) {
                    items[i].removeStateFlags(MediaLibraryItem.FLAG_SELECTED);
                    mAdapter.notifyItemChanged(i, items[i]);
                }
            }
        }
        mAdapter.resetSelectionCount();
    }

    protected void showInfoDialog(MediaWrapper media) {
        Intent i = new Intent(this, InfoActivity.class);
        i.putExtra(InfoActivity.TAG_ITEM, media);
        startActivity(i);
    }

    public void popMenu(final int position) {
        final MediaWrapper media = (MediaWrapper) mAdapter.getItem(position);

        View popView = LayoutInflater.from(this).inflate(R.layout.context_menu, null);
        ((TextView) popView.findViewById(R.id.title)).setText(media.getTitle());
        final BottomSheetDialog dialog = new MyBottomSheetDialog(this);
        dialog.setOwnerActivity(this);
        dialog.setContentView(popView);

        resetMenu(menuAudio);
        final MyMenuItem[] menus = setPopupWindowItems(menuAudio, media);

        ListView menuList = (ListView) popView.findViewById(R.id.menu_list);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int menuPos, long id) {
                MyMenuItem myMenuItem = menus[menuPos];
                int menuId = myMenuItem.getId();
                if (menuId == ID_AUDIO_SET_SONG) {
                    AudioUtil.setRingtone(media, PlaylistActivity.this);
                } else if (menuId == ID_AUDIO_APPEND) {
                    mService.append(media);
                } else if (menuId == ID_AUDIO_INSERT_NEXT) {
                    mService.insertNext(media);
                } else if (menuId == ID_AUDIO_DELETE) {
                    mAdapter.remove(media);
                    UiTools.snackerWithCancel(getWindow().getDecorView(), getString(R.string.file_deleted), new Runnable() {
                        @Override
                        public void run() {
                            deleteMedia(media);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.addItem(position, media);
                        }
                    });
                } else if (menuId == ID_AUDIO_INFO) {
                    showInfoDialog(media);
                } else if (menuId == ID_AUDIO_ADD_PLAYLIST) {
                    FragmentManager fm = getSupportFragmentManager();
                    SavePlaylistDialog savePlaylistDialog = new SavePlaylistDialog();
                    Bundle args = new Bundle();
                    args.putParcelableArray(SavePlaylistDialog.KEY_NEW_TRACKS, media.getTracks(mMediaLibrary));
                    savePlaylistDialog.setArguments(args);
                    savePlaylistDialog.show(fm, "fragment_add_to_playlist");
                } else if (menuId == ID_AUDIO_SHARE) {
                    ShareUtils.shareMedia(PlaylistActivity.this, media);
                }
                dialog.dismiss();
            }
        });
        menuList.setAdapter(new MyMenuItem.MyMenuAdapter(this, R.layout.context_menu_item, Arrays.asList(menus)));

        // 点击弹出窗口
        dialog.show();
    }

    private MyMenuItem[] setPopupWindowItems(MyMenuItem[] myMenu, MediaLibraryItem mediaItem) {
        findItem(myMenu, ID_AUDIO_PLAY_ALL).setValid(true);
        findItem(myMenu, ID_AUDIO_INFO).setValid(true);
        findItem(myMenu, ID_AUDIO_PLAY_ALL).setValid(false);
        findItem(myMenu, ID_AUDIO_SET_SONG).setValid(AndroidDevices.isPhone());
        //Hide delete if we cannot
        String location = ((MediaWrapper) mediaItem).getLocation();
        findItem(myMenu, ID_AUDIO_DELETE).setValid(FileUtils.canWrite(location));
        return makeValidMenu(myMenu);
    }

    protected void setContextMenuItems(Menu menu, int position) {
        menu.setGroupVisible(R.id.songs_view_only, true);
        menu.findItem(R.id.audio_list_browser_play_all).setVisible(false);
        menu.setGroupVisible(R.id.phone_only, AndroidDevices.isPhone());
        //Hide delete if we cannot
        String location = ((MediaWrapper) mAdapter.getItem(position)).getLocation();
        menu.findItem(R.id.audio_list_browser_delete).setVisible(FileUtils.canWrite(location));
    }

    protected boolean handleContextItemSelected(MenuItem item, final int position) {
        int id = item.getItemId();

        final MediaWrapper media = (MediaWrapper) mAdapter.getItem(position);

        if (id == R.id.audio_list_browser_set_song) {
            AudioUtil.setRingtone(media, this);
            return true;
        } else if (id == R.id.audio_list_browser_append) {
            mService.append(media);
            return true;
        } else if (id == R.id.audio_list_browser_insert_next) {
            mService.insertNext(media);
            return true;
        } else if (id == R.id.audio_list_browser_delete) {
            mAdapter.remove(media);
            UiTools.snackerWithCancel(getWindow().getDecorView(), getString(R.string.file_deleted), new Runnable() {
                @Override
                public void run() {
                    deleteMedia(media);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    mAdapter.addItem(position, media);
                }
            });
            return true;
        } else if (id == R.id.audio_view_info) {
            showInfoDialog(media);
            return true;
        } else if (id == R.id.audio_view_add_playlist) {
            FragmentManager fm = getSupportFragmentManager();
            SavePlaylistDialog savePlaylistDialog = new SavePlaylistDialog();
            Bundle args = new Bundle();
            args.putParcelableArray(SavePlaylistDialog.KEY_NEW_TRACKS, media.getTracks(mMediaLibrary));
            savePlaylistDialog.setArguments(args);
            savePlaylistDialog.show(fm, "fragment_add_to_playlist");
            return true;
        } else if (id == R.id.audio_share) {
            ShareUtils.shareMedia(this, media);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (menuInfo == null)
            return;
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo;
        getMenuInflater().inflate(R.menu.audio_list_browser, menu);

        setContextMenuItems(menu, info.position);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menu) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menu.getMenuInfo();

        return info != null && handleContextItemSelected(menu, info.position);
    }

    protected void deleteMedia(final MediaLibraryItem mw) {
        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                final LinkedList<String> foldersToReload = new LinkedList<>();
                final LinkedList<String> mediaPaths = new LinkedList<>();
                for (MediaWrapper media : mw.getTracks(mMediaLibrary)) {
                    String path = media.getUri().getPath();
                    mediaPaths.add(media.getLocation());
                    String parentPath = FileUtils.getParent(path);
                    if (FileUtils.deleteFile(path) && media.getId() > 0L && !foldersToReload.contains(parentPath))
                        foldersToReload.add(parentPath);
                }
                for (String folder : foldersToReload)
                    mMediaLibrary.reload(folder);
                if (mService != null) {
                    VLCApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            for (String path : mediaPaths)
                                mService.removeLocation(path);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (mService != null)
            mService.load(mPlaylist.getTracks(mMediaLibrary), 0);
    }
}
