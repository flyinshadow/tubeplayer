package com.wenjoyai.tubeplayer.gui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.view.ActionMode;
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
import com.wenjoyai.tubeplayer.media.MediaGroup;
import com.wenjoyai.tubeplayer.util.Strings;

import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.interfaces.MediaAddedCb;
import org.videolan.medialibrary.interfaces.MediaUpdatedCb;
import org.videolan.medialibrary.media.MediaWrapper;

import java.util.ArrayList;

/**
 * Created by yuqilin on 2017/9/5.
 */

public class VideoFolderFragment extends MediaBrowserFragment implements MediaAddedCb, MediaUpdatedCb {

    private ArrayList<String> mFolders = new ArrayList<>();
    private ArrayList<MediaWrapper> mVideos = new ArrayList<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private VideoFolderAdapter mAdapter;

    public VideoFolderFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new VideoFolderAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.video_folder, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);

        mRecyclerView.setAdapter(mAdapter);

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
        return "VideoFolderFragment";
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
    public void onMediaAdded(MediaWrapper[] mediaList) {

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
                final ArrayList<MediaWrapper> displayList = new ArrayList<>();
                for (MediaWrapper item : itemList) {
//                    Strings.removeFileProtocole(item.getUri());
                }
//                if (mGroup != null) {
//                    for (MediaWrapper item : itemList) {
//                        String title = item.getTitle().substring(item.getTitle().toLowerCase().startsWith("the") ? 4 : 0);
//                        if (mGroup == null || title.toLowerCase().startsWith(mGroup.toLowerCase()))
//                            displayList.add(item);
//                    }
//                } else {
//                    for (MediaGroup item : MediaGroup.group(itemList))
//                        displayList.add(item.getMedia());
//                }
//                VLCApplication.runOnMainThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mVideoAdapter.update(displayList, false);
//                    }
//                });
                mHandler.sendEmptyMessage(UNSET_REFRESHING);
            }
        });
    }

    public void updateItems(final MediaWrapper[] mediaList) {
        for (final MediaWrapper mw : mediaList)
            if (mw != null && mw.getType() == MediaWrapper.TYPE_VIDEO)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.update(mw);
//                        updateEmptyView();
                    }
                });
    }

    public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderViewHolder> {

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
            holder.mFolderName.setText(mFolders.get(position));
        }

        @Override
        public int getItemCount() {
            return mFolders.size();
        }

        @MainThread
        public void update(MediaWrapper item) {
            int position = mVideos.indexOf(item);
            if (position != -1) {
                if (!(mVideos.get(position) instanceof MediaGroup))
                    mVideos.set(position, item);
            } else {
                add(item);
            }
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
    }

    public class VideoFolderViewHolder extends  RecyclerView.ViewHolder {

        private TextView mFolderName;

        public VideoFolderViewHolder(View itemView) {
            super(itemView);
            mFolderName = (TextView) itemView.findViewById(R.id.item_folder_name);
        }
    }
}
