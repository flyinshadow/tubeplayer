/*
 * *************************************************************************
 *  PlaylistAdapter.java
 * **************************************************************************
 *  Copyright © 2015-2017 VLC authors and VideoLAN
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

package com.wenjoyai.tubeplayer.gui.audio;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import com.facebook.ads.MediaView;
import com.wenjoyai.tubeplayer.BR;
import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.BaseQueuedAdapter;
import com.wenjoyai.tubeplayer.gui.helpers.AsyncImageLoader;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.interfaces.SwipeDragHelperAdapter;
import com.wenjoyai.tubeplayer.media.MediaUtils;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.MediaItemDiffCallback;
import com.wenjoyai.tubeplayer.util.WeakHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistAdapter extends BaseQueuedAdapter<ArrayList<MediaWrapper>, PlaylistAdapter.ViewHolder> implements SwipeDragHelperAdapter, Filterable {

    private static final String TAG = "VLC/PlaylistAdapter";

    private ItemFilter mFilter = new ItemFilter();
    private PlaybackService mService = null;
    private IPlayer mAudioPlayer;

    private volatile ArrayList<MediaWrapper> mDataSet = new ArrayList<>();
    private ArrayList<MediaWrapper> mOriginalDataSet;
    private int mCurrentIndex = 0;

    private boolean mIsVideoPlayer;

    public interface IPlayer {
        void onPopupMenu(View view, int position);
        void updateList();
        void onSelectionSet(int position);
    }

    public PlaylistAdapter(IPlayer audioPlayer, boolean video) {
        mAudioPlayer = audioPlayer;
        mIsVideoPlayer = video;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.playlist_item;
        if (mIsVideoPlayer) {
            layout = R.layout.playlist_video_item;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Context ctx = holder.itemView.getContext();
        final MediaWrapper media = getItem(position);
        if (media.getItemType() == MediaLibraryItem.TYPE_AD && holder.adItem != null) {
            if (holder.adItem != null) {
                holder.adItem.setVisibility(View.VISIBLE);
            }
            if (holder.videoItem != null) {
                holder.videoItem.setVisibility(View.GONE);
            }
//            bindAd(holder, (AdItem)media);
        } else {
            if (holder.adItem != null) {
                holder.adItem.setVisibility(View.GONE);
            }
            if (holder.videoItem != null) {
                holder.videoItem.setVisibility(View.VISIBLE);
            }
            holder.binding.setVariable(BR.media, media);
            holder.binding.setVariable(BR.subTitle, MediaUtils.getMediaSubtitle(media));
            LogUtil.d(TAG, "onBindViewHolder mCurrentIndex=" + mCurrentIndex + ", position=" + position);

            int titleColor = UiTools.getColorFromAttribute(ctx, R.attr.list_title);
            if (mIsVideoPlayer) {
                holder.itemView.setBackgroundResource(R.color.black);
            }
            if ((mOriginalDataSet == null && mCurrentIndex == position)) {
                titleColor = UiTools.getColorFromAttribute(ctx, R.attr.colorAccent);
                if (mIsVideoPlayer) {
                    holder.itemView.setBackgroundResource(R.color.video_playlist_selected_bg);
                }
            } else {
                if (mIsVideoPlayer) {
                    titleColor = VLCApplication.getAppResources().getColor(R.color.video_playlist_title_color);
                }
            }

            holder.binding.setVariable(BR.titleColor, titleColor);
            holder.binding.executePendingBindings();

            if (holder.thumbnail != null) {
                AsyncImageLoader.loadPicture(holder.thumbnail, media);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @MainThread
    public MediaWrapper getItem(int position) {
        if (position >= 0 && position < getItemCount())
            return mDataSet.get(position);
        else
            return null;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public String getLocation(int position) {
        MediaWrapper item = getItem(position);
        return item == null ? "" : item.getLocation();
    }

    @MainThread
    public void addAll(List<MediaWrapper> playList) {
        mDataSet.addAll(playList);
    }

    @MainThread
    protected void internalUpdate(final ArrayList<MediaWrapper> newList) {
        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {

//                prepareAdItems(newList);

                final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MediaItemDiffCallback(mDataSet, newList), false);
                VLCApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataSet.clear();
                        addAll(newList);
                        result.dispatchUpdatesTo(PlaylistAdapter.this);
                        if (mService != null) {
//                            setCurrentIndex(mService.getCurrentMediaPosition());
                            setCurrentMedia(mService.getCurrentMediaWrapper());
                        }
                        processQueue();
                    }
                });
            }
        });

    }

    @MainThread
    public void remove(int position) {
        if (mService == null)
            return;
        mService.remove(position);
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int position) {
        if (position == mCurrentIndex || position < 0 || position >= getItemCount())
            return;
        int former = mCurrentIndex;
        mCurrentIndex = position;
        notifyItemChanged(former);
        notifyItemChanged(position);
        mAudioPlayer.onSelectionSet(position);
    }

    public void setCurrentMedia(MediaWrapper media) {
        int pos = (mDataSet != null ? mDataSet.indexOf(media) : -1);
        setCurrentIndex(pos);
    }

    private boolean validateIndex(int index) {
        return index >= 0 && index < mDataSet.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (validateIndex(fromPosition) && validateIndex(toPosition)) {
            Collections.swap(mDataSet, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            mHandler.obtainMessage(PlaylistHandler.ACTION_MOVE, fromPosition, toPosition).sendToTarget();
        }
    }

    @Override
    public void onItemDismiss(final int position) {
        final MediaWrapper media = getItem(position);
        if (media == null)
            return;
        String message = String.format(VLCApplication.getAppResources().getString(R.string.remove_playlist_item), media.getTitle());
        if (mAudioPlayer instanceof Fragment){
            View v = ((Fragment) mAudioPlayer).getView();
            Runnable cancelAction = new Runnable() {
                @Override
                public void run() {
                    mService.insertItem(position, media);
                }
            };
            UiTools.snackerWithCancel(v, message, null, cancelAction);
        } else if (mAudioPlayer instanceof Context){
            Toast.makeText(VLCApplication.getAppContext(), message, Toast.LENGTH_SHORT).show();
        }
        remove(position);
    }

    public void setService(PlaybackService service) {
        mService = service;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ViewDataBinding binding;
        private ImageView thumbnail;
        private View adItem;
        private View videoItem;
        private LinearLayout adChoicesContainer;
        private TextView adCallToAction;
        private TextView adBody;
        private MediaView adMedia;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.setVariable(BR.holder, this);

            adItem = v.findViewById(R.id.pl_ad_item);
            adChoicesContainer = (LinearLayout) itemView.findViewById(R.id.ad_choices_container);
            adCallToAction = (TextView) itemView.findViewById(R.id.ad_call_to_action);
            adBody = (TextView) itemView.findViewById(R.id.ad_body);
            adMedia = (MediaView) itemView.findViewById(R.id.ad_media);

            videoItem = v.findViewById(R.id.pl_video_item);
            thumbnail = (ImageView) v.findViewById(R.id.pl_item_thumbnail);
            if (mIsVideoPlayer && thumbnail != null) {
                binding.setVariable(BR.cover, AsyncImageLoader.DEFAULT_COVER_VIDEO_DRAWABLE);
            }
        }
        public void onClick(View v, MediaWrapper media){
            int position = getMediaPosition(media);
            if (mService != null)
                mService.playIndex(position);
            if (mOriginalDataSet != null)
                restoreList();
        }
        public void onMoreClick(View v){
            mAudioPlayer.onPopupMenu(v, getLayoutPosition());
        }

        private int getMediaPosition(MediaWrapper media) {
            if (mOriginalDataSet == null) {
                MediaWrapper mw;
                ArrayList<MediaWrapper> medias = mService.getMedias();
                for (int i = 0 ; i < medias.size() ; ++i) {
                    mw = medias.get(i);
                    if (mw.equals(media))
                        return i;
                }
                return getLayoutPosition();
            } else {
                MediaWrapper mw;
                for (int i = 0 ; i < mOriginalDataSet.size() ; ++i) {
                    mw = mOriginalDataSet.get(i);
                    if (mw.equals(media))
                        return i;
                }
                return 0;
            }
        }
    }

    @MainThread
    public void restoreList() {
        if (mOriginalDataSet != null) {
            update(new ArrayList<>(mOriginalDataSet));
            mOriginalDataSet = null;
        }
    }

    private PlaylistHandler mHandler = new PlaylistHandler(this);

    private static class PlaylistHandler extends WeakHandler<PlaylistAdapter>{

        static final int ACTION_MOVE = 0;
        static final int ACTION_MOVED = 1;

        int from = -1, to = -1;

        PlaylistHandler(PlaylistAdapter owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ACTION_MOVE:
                    removeMessages(ACTION_MOVED);
                    if (from == -1)
                        from = msg.arg1;
                    to = msg.arg2;
                    sendEmptyMessageDelayed(ACTION_MOVED, 1000);
                    break;
                case ACTION_MOVED:
                    PlaybackService service = getOwner().mService;
                    if (from != -1 && to != -1 && service == null)
                        return;
                    if (to > from)
                        ++to;
                    service.moveItem(from, to);
                    from = to = -1;
                    break;
            }
        }
    }

    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (mOriginalDataSet == null)
                mOriginalDataSet = new ArrayList<>(mDataSet);
            final String[] queryStrings = charSequence.toString().trim().toLowerCase().split(" ");
            FilterResults results = new FilterResults();
            ArrayList<MediaWrapper> list = new ArrayList<>();
            String title, location, artist, album, albumArtist, genre;
            MediaWrapper media;
            mediaLoop:
            for (int i = 0 ; i < mOriginalDataSet.size() ; ++i) {
                media = mOriginalDataSet.get(i);
                title = MediaUtils.getMediaTitle(media);
                location = media.getLocation();
                artist = MediaUtils.getMediaArtist(VLCApplication.getAppContext(), media).toLowerCase();
                albumArtist = MediaUtils.getMediaAlbumArtist(VLCApplication.getAppContext(), media).toLowerCase();
                album = MediaUtils.getMediaAlbum(VLCApplication.getAppContext(), media).toLowerCase();
                genre = MediaUtils.getMediaGenre(VLCApplication.getAppContext(), media).toLowerCase();
                for (String queryString : queryStrings) {
                    if (queryString.length() < 2)
                        continue;
                    if (title != null && title.toLowerCase().contains(queryString) ||
                            location != null && location.toLowerCase().contains(queryString) ||
                            artist.contains(queryString) ||
                            albumArtist.contains(queryString) ||
                            album.contains(queryString) ||
                            genre.contains(queryString)) {
                        list.add(media);
                        continue mediaLoop; //avoid duplicates in search results, and skip useless processing
                    }
                }
            }
            results.values = list;
            results.count = list.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            update((ArrayList<MediaWrapper>) filterResults.values);
        }
    }
}
