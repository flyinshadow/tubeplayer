package com.wenjoyai.tubeplayer.gui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wenjoyai.tubeplayer.MediaParsingService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.browser.MediaBrowserFragment;
import com.wenjoyai.tubeplayer.gui.view.SwipeRefreshLayout;
import com.wenjoyai.tubeplayer.interfaces.IEventsHandler;
import com.wenjoyai.tubeplayer.media.FolderGroup;
import com.wenjoyai.tubeplayer.media.MediaGroup;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.FileUtils;
import com.wenjoyai.tubeplayer.util.Strings;

import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.interfaces.MediaAddedCb;
import org.videolan.medialibrary.interfaces.MediaUpdatedCb;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yuqilin on 2017/9/5.
 */

public class VideoFolderFragment extends MediaBrowserFragment implements MediaAddedCb, MediaUpdatedCb, IEventsHandler {

    private List<FolderGroup> mFolders = new ArrayList<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private VideoFolderAdapter mAdapter;
    private View mFolderDirectories;

    public VideoFolderFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new VideoFolderAdapter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.video_folder, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        mFolderDirectories = v.findViewById(R.id.folder_directories);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        mFolderDirectories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).showSecondaryFragment(SecondaryActivity.FILE_BROWSER);
            }
        });

        return v;
    }

    public void onStart() {
        if (mMediaLibrary.isInitiated()) {
            onMedialibraryReady();
        } else {
            setupMediaLibraryReceiver();
        }
        super.onStart();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.directories);
    }

    @Override
    public void onRefresh() {
        getActivity().startService(new Intent(MediaParsingService.ACTION_RELOAD, null, getActivity(), MediaParsingService.class));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

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

    protected void onMedialibraryReady() {
        super.onMedialibraryReady();
        mMediaLibrary.setMediaUpdatedCb(this, Medialibrary.FLAG_MEDIA_UPDATED_VIDEO);
        mMediaLibrary.setMediaAddedCb(this, Medialibrary.FLAG_MEDIA_ADDED_VIDEO);
        mHandler.sendEmptyMessage(UPDATE_LIST);
    }

    @Override
    public void onMediaAdded(final MediaWrapper[] mediaList) {
        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                for (MediaWrapper media : mediaList) {
                    FolderGroup.insertInto(mFolders, media);
                }
                FolderGroup.sort(mFolders);

                VLCApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void onMediaUpdated(MediaWrapper[] mediaList) {

    }

    @MainThread
    public void updateList() {
        mHandler.sendEmptyMessageDelayed(SET_REFRESHING, 300);
        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                final MediaWrapper[] itemList = mMediaLibrary.getVideos();
                mFolders = FolderGroup.group(itemList);
                FolderGroup.sort(mFolders);
                VLCApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
                mHandler.sendEmptyMessage(UNSET_REFRESHING);
            }
        });
    }

    @Override
    public void onClick(View v, int position, MediaLibraryItem item) {
        if (item instanceof FolderGroup) {
            TextView folderName = (TextView) v.findViewById(R.id.item_folder_name);
            String title = "";
            if (folderName != null && folderName.getText() != null) {
                title = folderName.getText().toString();
            }
            ((MainActivity)getActivity()).showSecondaryFragment2(SecondaryActivity.VIDEO_FOLDER_GROUP,
                    ((FolderGroup) item).getFolderPath(), title);
        }
    }

    @Override
    public boolean onLongClick(View v, int position, MediaLibraryItem item) {
        return false;
    }

    @Override
    public void onCtxClick(View v, int position, MediaLibraryItem item) {

    }

    @Override
    public void onUpdateFinished(RecyclerView.Adapter adapter) {

    }

    public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.VideoFolderViewHolder> {

        private IEventsHandler mEventsHandler;

        public VideoFolderAdapter(IEventsHandler eventsHandler) {
            mEventsHandler = eventsHandler;
        }

        @Override
        public VideoFolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.video_folder_item, parent, false);

            return new VideoFolderViewHolder(v);
        }

        @Override
        public void onBindViewHolder(VideoFolderViewHolder holder, int position) {
            if (position >= mFolders.size()) {
                return;
            }
            String folderPath = mFolders.get(position).getFolderPath();
            String folderName = FileUtils.getFileNameFromPath(folderPath);
            if (folderPath.equals(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY)) {
                folderName = getString(R.string.internal_memory);
            }
//            folderName = folderName + "(" + mFolders.get(position).size() + ")";
            holder.mFolderName.setText(folderName);
            String sVideoCount = mFolders.get(position).size() + " " + getResources().getString(R.string.videos);
            holder.mVideoCount.setText(sVideoCount);
        }

        @Override
        public void onBindViewHolder(VideoFolderViewHolder holder, int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            return mFolders.size();
        }

        @MainThread
        public void update(List<FolderGroup> folders) {

        }

        @MainThread
        public void add(MediaWrapper item) {
//            ArrayList<MediaWrapper> list = new ArrayList<>(peekLast());
//            list.add(item);
//            update(list, false);
        }

        @MainThread
        void update(final ArrayList<MediaWrapper> items, final boolean detectMoves) {
//            mPendingUpdates.add(items);
//            if (mPendingUpdates.size() == 1)
//                internalUpdate(items, detectMoves);
        }

        public class VideoFolderViewHolder extends  RecyclerView.ViewHolder {

            private View mList;
            private TextView mFolderName;
            private TextView mVideoCount;

            public VideoFolderViewHolder(View itemView) {
                super(itemView);
                mList = itemView.findViewById(R.id.folder_list_item);
                mFolderName = (TextView) itemView.findViewById(R.id.item_folder_name);
                mVideoCount = (TextView) itemView.findViewById(R.id.item_video_count);
                mList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getLayoutPosition();
                        if (mEventsHandler != null) {
                            mEventsHandler.onClick(view, position, mFolders.get(position));
                        }
                    }
                });
            }

        }
    }
}
