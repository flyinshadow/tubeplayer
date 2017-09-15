/*
 * *************************************************************************
 *  SecondaryActivity.java
 * **************************************************************************
 *  Copyright © 2015 VLC authors and VideoLAN
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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import org.videolan.medialibrary.Medialibrary;
import com.wenjoyai.tubeplayer.MediaParsingService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.audio.AudioAlbumsSongsFragment;
import com.wenjoyai.tubeplayer.gui.audio.AudioBrowserFragment;
import com.wenjoyai.tubeplayer.gui.audio.EqualizerFragment;
import com.wenjoyai.tubeplayer.gui.browser.FileBrowserFragment;
import com.wenjoyai.tubeplayer.gui.browser.StorageBrowserFragment;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;
import com.wenjoyai.tubeplayer.gui.tv.TvUtil;
import com.wenjoyai.tubeplayer.gui.video.VideoGridFragment;
import com.wenjoyai.tubeplayer.gui.video.VideoListAdapter;
import com.wenjoyai.tubeplayer.interfaces.ISortable;

public class SecondaryActivity extends AudioPlayerContainerActivity {
    public final static String TAG = "VLC/SecondaryActivity";

    public static final int ACTIVITY_RESULT_SECONDARY = 3;

    public static final String KEY_FRAGMENT = "fragment";

    public static final String ALBUMS_SONGS = "albumsSongs";
    public static final String EQUALIZER = "equalizer";
    public static final String ABOUT = "about";
    public static final String VIDEO_GROUP_LIST = "videoGroupList";
    public static final String STORAGE_BROWSER = "storage_browser";
    public static final String VIDEO_FOLDER_GROUP = "videoFolderGroup";
    public static final String FILE_BROWSER = "file_browser";


    Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondary);

        initAudioPlayerContainerActivity();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getSupportFragmentManager().getFragments() == null) {
            String fragmentId = getIntent().getStringExtra(KEY_FRAGMENT);
            fetchSecondaryFragment(fragmentId);
            if (mFragment == null){
                finish();
                return;
            }
            getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_placeholder, mFragment)
            .commit();
            if (VLCApplication.showTvUi() && STORAGE_BROWSER.equals(fragmentId))
                Snackbar.make(getWindow().getDecorView(), R.string.tv_settings_hint, Snackbar.LENGTH_LONG).show();
        }

        if (VLCApplication.showTvUi())
            TvUtil.applyOverscanMargin(this);
    }

    @Override
    protected void onResume() {
        overridePendingTransition(0,0);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isFinishing())
            overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT_SECONDARY) {
            if (resultCode == PreferencesActivity.RESULT_RESCAN) {
                startService(new Intent(MediaParsingService.ACTION_RELOAD, null,this, MediaParsingService.class));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mFragment instanceof VideoGridFragment) {
            getMenuInflater().inflate(R.menu.video_group, menu);
        }

        MenuItem viewModeItem = menu.findItem(R.id.ml_menu_view_mode);
        if (viewModeItem != null) {
            int currentViewMode = mSettings.getInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE, VideoListAdapter.VIEW_MODE_DEFAULT);
            if (currentViewMode == VideoListAdapter.VIEW_MODE_LIST) {
                viewModeItem.setIcon(R.drawable.ic_view_list);
            } else if (currentViewMode == VideoListAdapter.VIEW_MODE_GRID) {
                viewModeItem.setIcon(R.drawable.ic_view_grid);
            } else if (currentViewMode == VideoListAdapter.VIEW_MODE_BIGPIC) {
                viewModeItem.setIcon(R.drawable.ic_view_bigpic);
            } else {
                viewModeItem.setVisible(false);
            }
            if (mFragment instanceof VideoGridFragment && (((VideoGridFragment) mFragment).getFolderGroup() != null)) {
                viewModeItem.setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.ml_menu_view_mode:
                ((VideoGridFragment)mFragment).toggleViewMode(item);
                mSettings.edit().putInt(PreferencesActivity.KEY_CURRENT_VIEW_MODE,
                        ((VideoGridFragment)mFragment).getCurrentViewMode()).apply();
                break;
            case R.id.ml_menu_sortby_date:
                ((ISortable) mFragment).sortBy(VideoListAdapter.SORT_BY_DATE);
                break;
            case R.id.ml_menu_sortby_name:
                ((ISortable) mFragment).sortBy(VideoListAdapter.SORT_BY_TITLE);
                break;
            case R.id.ml_menu_sortby_length:
                ((ISortable) mFragment).sortBy(VideoListAdapter.SORT_BY_LENGTH);
                break;
            case R.id.ml_menu_refresh:
                Medialibrary ml = VLCApplication.getMLInstance();
                if (!ml.isWorking())
                    startService(new Intent(MediaParsingService.ACTION_RELOAD, null,this, MediaParsingService.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchSecondaryFragment(String id) {
        if (id.equals(ALBUMS_SONGS)) {
            mFragment = new AudioAlbumsSongsFragment();
            Bundle args = new Bundle();
            args.putParcelable(AudioBrowserFragment.TAG_ITEM, getIntent().getParcelableExtra(AudioBrowserFragment.TAG_ITEM));
            mFragment.setArguments(args);
        } else if(id.equals(EQUALIZER)) {
            mFragment = new EqualizerFragment();
        } else if(id.equals(ABOUT)) {
            mFragment = new AboutFragment();
        } else if(id.equals(VIDEO_GROUP_LIST)) {
            mFragment = new VideoGridFragment();
            ((VideoGridFragment) mFragment).setGroup(getIntent().getStringExtra("param"));
        } else if (id.equals(VIDEO_FOLDER_GROUP)) {
            mFragment = new VideoGridFragment();
            ((VideoGridFragment) mFragment).setFolderGroup(getIntent().getStringExtra("param"), getIntent().getStringExtra("param2"));
        } else if (id.equals(STORAGE_BROWSER)){
            mFragment = new StorageBrowserFragment();
        } else if (id.equals(FILE_BROWSER)) {
            mFragment = new FileBrowserFragment();
        } else {
            throw new IllegalArgumentException("Wrong fragment id.");
        }
    }
}
