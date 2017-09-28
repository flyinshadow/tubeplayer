/*
 * *************************************************************************
 *  MediaBrowserFragment.java
 * **************************************************************************
 *  Copyright © 2015-2016 VLC authors and VideoLAN
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

package com.wenjoyai.tubeplayer.gui.browser;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;
import com.wenjoyai.tubeplayer.MediaParsingService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.InfoActivity;
import com.wenjoyai.tubeplayer.gui.PlaybackServiceFragment;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.gui.view.ContextMenuRecyclerView;
import com.wenjoyai.tubeplayer.gui.view.SwipeRefreshLayout;
import com.wenjoyai.tubeplayer.util.FileUtils;

import java.util.LinkedList;

public abstract class MediaBrowserFragment extends PlaybackServiceFragment implements android.support.v7.view.ActionMode.Callback {

    public final static String TAG = "VLC/MediaBrowserFragment";

    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected volatile boolean mReadyToDisplay = true;
    protected Medialibrary mMediaLibrary;
    protected ActionMode mActionMode;
    public FloatingActionButton mFabPlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaLibrary = VLCApplication.getMLInstance();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setColorSchemeColors(UiTools.getColorFromAttribute(getActivity(), R.attr.colorAccent));
    }

    public void onStart(){
        super.onStart();
        final AppCompatActivity activity = (AppCompatActivity)getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(getTitle());
            activity.getSupportActionBar().setSubtitle(getSubTitle());
            getActivity().supportInvalidateOptionsMenu();
        }
        mFabPlay = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (mFabPlay != null) {
            setFabPlayVisibility(false);
            mFabPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    StatisticsManager.submitHomeTab(getActivity(), StatisticsManager.ITEM_ID_PLAY_ALL_VIDEO);

                    onFabPlayClick(v);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopActionMode();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mParsingServiceReceiver);
    }

    public void setFabPlayVisibility(boolean enable) {
        if (mFabPlay != null)
            mFabPlay.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    public void onFabPlayClick(View view) {}

    public void setReadyToDisplay(boolean ready) {
        if (ready && !mReadyToDisplay)
            display();
        else
            mReadyToDisplay = ready;
    }


    protected abstract String getTitle();
    public abstract void onRefresh();

    protected String getSubTitle() { return null; }
    public void clear() {}
    protected void display() {}

    protected void inflate(Menu menu, int position) {}
    protected void setContextMenuItems(Menu menu, int position) {}
    protected boolean handleContextItemSelected(MenuItem menu, int position) { return false;}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (menuInfo == null)
            return;
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo)menuInfo;
        inflate(menu, info.position);

        setContextMenuItems(menu, info.position);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menu) {
        if(!getUserVisibleHint())
            return false;
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menu.getMenuInfo();

        return info != null && handleContextItemSelected(menu, info.position);
    }

    protected void renameMedia(final MediaWrapper srcMedia, final MediaWrapper dstMedia, final boolean refresh) {
        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                String folderToReload = null;

                String path = srcMedia.getUri().getPath();
                String parentPath = FileUtils.getParent(path);
                if (FileUtils.renameFile(path, dstMedia.getUri().getPath()) && srcMedia.getId() > 0L)
                    folderToReload = parentPath;

                if (folderToReload != null) {
                    mMediaLibrary.reload(folderToReload);
                }

                if (mService != null && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (refresh)
                                onRefresh();
                        }
                    });
                }
            }
        });
    }

    protected void deleteMedia(final MediaLibraryItem mw, final boolean refresh) {
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
                if (mService != null && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (String path : mediaPaths)
                                mService.removeLocation(path);
                            if (refresh)
                                onRefresh();
                        }
                    });
                }
            }
        });
    }

    protected void showInfoDialog(MediaLibraryItem item) {
        Intent i = new Intent(getActivity(), InfoActivity.class);
        i.putExtra(InfoActivity.TAG_ITEM, item);
        startActivity(i);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void startActionMode() {
        mActionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(this);
        setFabPlayVisibility(false);
    }

    protected void stopActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            onDestroyActionMode(mActionMode);
            setFabPlayVisibility(true);
        }
    }

    public void invalidateActionMode() {
        if (mActionMode != null)
            mActionMode.invalidate();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    protected void onMedialibraryReady() {
        IntentFilter parsingServiceFilter = new IntentFilter(MediaParsingService.ACTION_SERVICE_ENDED);
        parsingServiceFilter.addAction(MediaParsingService.ACTION_SERVICE_STARTED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mParsingServiceReceiver, parsingServiceFilter);
    }

    protected void setupMediaLibraryReceiver() {
        final BroadcastReceiver libraryReadyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                onMedialibraryReady();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(libraryReadyReceiver, new IntentFilter(VLCApplication.ACTION_MEDIALIBRARY_READY));
    }

    protected final BroadcastReceiver mParsingServiceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case MediaParsingService.ACTION_SERVICE_ENDED:
                    onParsingServiceFinished();
                    break;
                case MediaParsingService.ACTION_SERVICE_STARTED:
                    onParsingServiceStarted();
                    break;
            }
        }
    };

    protected void onParsingServiceStarted() {}

    protected void onParsingServiceFinished() {}
}
