/*****************************************************************************
 * VideoListAdapter.java
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

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.videolan.medialibrary.Tools;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;
import com.wenjoyai.tubeplayer.BR;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.helpers.AsyncImageLoader;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;
import com.wenjoyai.tubeplayer.interfaces.IEventsHandler;
import com.wenjoyai.tubeplayer.media.MediaGroup;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.MediaItemFilter;
import com.wenjoyai.tubeplayer.util.Strings;
import com.wenjoyai.tubeplayer.util.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> implements Filterable {

    public final static String TAG = "VLC/VideoListAdapter";

    public final static int SORT_BY_TITLE = 0;
    public final static int SORT_BY_LENGTH = 1;
    public final static int SORT_BY_DATE = 2;

    // 默认按日期倒序排列
    public final static int SORT_BY_DEFAULT = SORT_BY_DATE;
    public final static int SORT_DIRECTION_DEFAULT = -1;


    final static int UPDATE_SELECTION = 0;
    final static int UPDATE_THUMB = 1;
    final static int UPDATE_TIME = 2;

    public final static int VIEW_MODE_BIGPIC = 2;
    public final static int VIEW_MODE_LIST = 1;
    public final static int VIEW_MODE_GRID = 0;
    public final static int VIEW_MODE_MAX = 3;

    public static final int VIEW_MODE_FULL_TITLE = 4;

    public final static int VIEW_MODE_DEFAULT = VIEW_MODE_GRID;

    private boolean mListMode = false;
    private IEventsHandler mEventsHandler;
    private VideoComparator mVideoComparator = new VideoComparator();
    private ArrayList<MediaWrapper> mVideos = new ArrayList<>();
    private ArrayDeque<ArrayList<MediaWrapper>> mPendingUpdates = new ArrayDeque<>();
    private ArrayList<MediaWrapper> mOriginalData = null;
    private ItemFilter mFilter = new ItemFilter();
    private int mSelectionCount = 0;
    private int mGridCardWidth = 0;

    private int mCurrentViewMode = VIEW_MODE_DEFAULT;

    protected final ExecutorService mUpdateExecutor = Executors.newSingleThreadExecutor();

    VideoListAdapter(IEventsHandler eventsHandler, int viewMode) {
        super();
        mEventsHandler = eventsHandler;
        mCurrentViewMode = viewMode;
    }

    VideoComparator getComparator() {
        return mVideoComparator;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int video_layout = R.layout.video_grid_card;
        if (mCurrentViewMode == VIEW_MODE_GRID) {
            video_layout = R.layout.video_grid_card;
        } else if (mCurrentViewMode == VIEW_MODE_LIST) {
            video_layout = R.layout.video_list_card;
        } else if (mCurrentViewMode == VIEW_MODE_BIGPIC) {
            video_layout = R.layout.video_bigpic_card;
        } else if (mCurrentViewMode == VIEW_MODE_FULL_TITLE) {
            video_layout = R.layout.video_full_title;
        }
        View v = inflater.inflate(video_layout, parent, false);
//        if (!mListMode) {
//            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) v.getLayoutParams();
//            params.width = mGridCardWidth;
////            params.height = params.width;//*10/16;
//            params.height = GridLayoutManager.LayoutParams.WRAP_CONTENT;
//            v.setLayoutParams(params);
//        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MediaWrapper media = mVideos.get(position);
        if (media == null)
            return;

        LogUtil.d(TAG, "xxxx onBindViewHolder position: " + position + " " + media.getUri().getPath() +
        " " + media.getArtworkMrl());

        holder.binding.setVariable(BR.scaleType, ImageView.ScaleType.CENTER_CROP);
        fillView(holder, media);
        holder.binding.setVariable(BR.media, media);
        boolean isSelected = media.hasStateFlags(MediaLibraryItem.FLAG_SELECTED);
        holder.binding.setVariable(BR.selected, isSelected);

        if (holder.fileSize != null) {
            holder.fileSize.setText(Strings.readableSize(media.getFileSize()));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position);
        else {
            MediaWrapper media = mVideos.get(position);
            for (Object data : payloads) {
                switch ((int) data) {
                    case UPDATE_THUMB:
                        LogUtil.d(TAG, "xxxx onBindViewHolder UPDATE_THUMB position: " + position + " " + media.getUri().getPath() +
                                " " + media.getArtworkMrl());
                        AsyncImageLoader.loadPicture(holder.thumbView, media);
                        break;
                    case UPDATE_TIME:
                        fillView(holder, media);
                        break;
                    case UPDATE_SELECTION:
                        boolean isSelected = media.hasStateFlags(MediaLibraryItem.FLAG_SELECTED);
                        holder.binding.setVariable(BR.selected, isSelected);
                        holder.binding.setVariable(BR.bgColor, ContextCompat.getColor(holder.itemView.getContext(), mListMode && isSelected ? R.color.orange200transparent : R.color.transparent));
                        break;
                }
            }
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.binding.setVariable(BR.cover, AsyncImageLoader.DEFAULT_COVER_VIDEO_DRAWABLE);
    }

    public boolean isEmpty() {
        return peekLast().size() == 0;
    }

    @Nullable
    public MediaWrapper getItem(int position) {
        return isPositionValid(position) ? mVideos.get(position) : null;
    }

    private boolean isPositionValid(int position) {
        return position >= 0 && position < mVideos.size();
    }

    @MainThread
    public void add(MediaWrapper item) {
        ArrayList<MediaWrapper> list = new ArrayList<>(peekLast());
        list.add(item);
        int cnt = 0;
        for (MediaWrapper media : list) {
            LogUtil.d(TAG, "xxxx add items[" + cnt++ + "] " + media.getUri().getPath() + " " + media.getArtworkMrl());
        }
        update(list, false);
    }

    @MainThread
    public void updateVideo(MediaWrapper srcItem, MediaWrapper dstItem) {
        ArrayList<MediaWrapper> refList = new ArrayList<>(peekLast());
//        if (refList.remove(srcItem) && refList.add(dstItem)) {
//            update(refList, false);
//        }
        int position = refList.indexOf(srcItem);
        if (position != -1) {
            refList.set(position, dstItem);
        }
        update(refList, false);
    }

    @MainThread
    public void remove(MediaWrapper item) {
        ArrayList<MediaWrapper> refList = new ArrayList<>(peekLast());
        if (refList.remove(item))
            update(refList, false);
    }

    @MainThread
    public void addAll(Collection<MediaWrapper> items) {
        mVideos.addAll(items);
        mOriginalData = null;
    }

    @MainThread
    private ArrayList<MediaWrapper> peekLast() {
        if (mPendingUpdates.isEmpty()) {
            LogUtil.d(TAG, "xxxx peekLast return mVideos");
            return mVideos;
        } else {
            LogUtil.d(TAG, "xxxx peekLast return mPendingUpdates peekLast");
            return mPendingUpdates.peekLast();
        }
    }

    public boolean contains(MediaWrapper mw) {
        return mVideos.indexOf(mw) != -1;
    }

    public ArrayList<MediaWrapper> getAll() {
        return mVideos;
    }

    List<MediaWrapper> getSelection() {
        List<MediaWrapper> selection = new LinkedList<>();
        for (int i = 0; i < mVideos.size(); ++i) {
            MediaWrapper mw = mVideos.get(i);
            if (mw.hasStateFlags(MediaLibraryItem.FLAG_SELECTED)) {
                if (mw instanceof MediaGroup)
                    selection.addAll(((MediaGroup) mw).getAll());
                else
                    selection.add(mw);
            }

        }
        return selection;
    }

    @MainThread
    int getSelectionCount() {
        return mSelectionCount;
    }

    @MainThread
    void resetSelectionCount() {
        mSelectionCount = 0;
    }

    @MainThread
    void updateSelectionCount(boolean selected) {
        mSelectionCount += selected ? 1 : -1;
    }

    @MainThread
    public void update(final MediaWrapper[] items) {
        final ArrayList<MediaWrapper> list = new ArrayList<>(peekLast());
        VLCApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                for (MediaWrapper media : list) {
                    LogUtil.d(TAG, "xxxx insertOrUdpate 0 peekLast[" + i++ + "] " + media.getUri().getPath() + " " + media.getArtworkMrl());
                }
                Util.insertOrUdpate(list, items);
                for (MediaWrapper media : list) {
                    LogUtil.d(TAG, "xxxx insertOrUdpate 1 peekLast[" + i++ + "] " + media.getUri().getPath() + " " + media.getArtworkMrl());
                }
                VLCApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        update(list, false);
                    }
                });
            }
        });
    }

    @MainThread
    public void clear() {
        LogUtil.d(TAG, "xxxx clear");
        mVideos.clear();
        mOriginalData = null;
    }

    private void fillView(ViewHolder holder, MediaWrapper media) {
        String text = "";
        String resolution = "";
        int max = 0;
        int progress = 0;

        if (media.getType() == MediaWrapper.TYPE_GROUP) {
            MediaGroup mediaGroup = (MediaGroup) media;
            int size = mediaGroup.size();
            text = VLCApplication.getAppResources().getQuantityString(R.plurals.videos_quantity, size, size);
        } else {
            /* Time / Duration */
            if (media.getLength() > 0) {
                long lastTime = media.getTime();
                if (lastTime > 0) {
//                    text = Tools.getProgressText(media);
                    text = Tools.getProgressString(media);
                    max = (int) (media.getLength() / 1000);
                    progress = (int) (lastTime / 1000);
                } else {
//                    text = Tools.millisToText(media.getLength());
                    text = Tools.millisToString(media.getLength());
                }
            }
            resolution = Tools.getResolution(media);
        }

        holder.binding.setVariable(BR.resolution, resolution);
        holder.binding.setVariable(BR.time, text);
        holder.binding.setVariable(BR.max, max);
        holder.binding.setVariable(BR.progress, progress);
    }

    public void setListMode(boolean value) {
        mListMode = value;
    }

    void setGridCardWidth(int gridCardWidth) {
        mGridCardWidth = gridCardWidth;
    }

    public boolean isListMode() {
        return mListMode;
    }

    public void toggleViewMode(int targetViewMode) {
        mCurrentViewMode = targetViewMode;
        notifyDataSetChanged();
    }

    public int getCurrentViewMode() {
        return mCurrentViewMode;
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        return mCurrentViewMode;
    }

    int getListWithPosition(ArrayList<MediaWrapper>  list, int position) {
        MediaWrapper mw;
        int offset = 0;
        for (int i = 0; i < getItemCount(); ++i) {
            mw = mVideos.get(i);
            if (mw instanceof MediaGroup) {
                for (MediaWrapper item : ((MediaGroup) mw).getAll())
                    list.add(item);
                if (i < position)
                    offset += ((MediaGroup)mw).size()-1;
            } else
                list.add(mw);
        }
        return position+offset;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener {
        public ViewDataBinding binding;
        private ImageView thumbView;
        private TextView fileSize;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            thumbView = (ImageView) itemView.findViewById(R.id.ml_item_thumbnail);
            fileSize = (TextView) itemView.findViewById(R.id.ml_item_size);

            binding.setVariable(BR.holder, this);
            binding.setVariable(BR.cover, AsyncImageLoader.DEFAULT_COVER_VIDEO_DRAWABLE);
            itemView.setOnFocusChangeListener(this);
        }

        public void onClick(View v) {
            int position = getLayoutPosition();
            if (position >= 0 && position < mVideos.size()) {
                mEventsHandler.onClick(v, position, mVideos.get(position));
            }
        }

        public void onMoreClick(View v){
            mEventsHandler.onCtxClick(v, getLayoutPosition(), null);
        }

        public boolean onLongClick(View v) {
            int position = getLayoutPosition();
            if (position >= 0 && position < mVideos.size()) {
                return mEventsHandler.onLongClick(v, position, mVideos.get(position));
            }
            return false;
        }

//        private void setOverlay(boolean selected) {
//            thumbView.setImageResource(selected ? R.drawable.ic_action_mode_select_1610 : mListMode ? 0 : R.drawable.black_gradient);
//        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            setViewBackground(hasFocus || mVideos.get(getLayoutPosition()).hasStateFlags(MediaLibraryItem.FLAG_SELECTED));
        }

        private void setViewBackground(boolean highlight) {
            itemView.setBackgroundColor(highlight ? UiTools.ITEM_FOCUS_ON : UiTools.ITEM_FOCUS_OFF);
        }
    }
    int sortDirection(int sortDirection) {
        return mVideoComparator.sortDirection(sortDirection);
    }

    void sortBy(int sortby) {
        mVideoComparator.sortBy(sortby);
    }

    private class VideoComparator implements Comparator<MediaWrapper> {

        private static final String KEY_SORT_BY =  "sort_by";
        private static final String KEY_SORT_DIRECTION =  "sort_direction";

        private int mSortDirection;
        private int mSortBy;
        protected SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());

        VideoComparator() {
            mSortBy = mSettings.getInt(KEY_SORT_BY, SORT_BY_DEFAULT);
            mSortDirection = mSettings.getInt(KEY_SORT_DIRECTION, SORT_DIRECTION_DEFAULT);
        }

        int sortDirection(int sortby) {
            if (sortby == mSortBy)
                return  mSortDirection;
            else
                return -1;
        }

        void sortBy(int sortby) {
            switch (sortby) {
                case SORT_BY_TITLE:
                    if (mSortBy == SORT_BY_TITLE)
                        mSortDirection *= -1;
                    else {
                        mSortBy = SORT_BY_TITLE;
                        mSortDirection = 1;
                    }
                    break;
                case SORT_BY_LENGTH:
                    if (mSortBy == SORT_BY_LENGTH)
                        mSortDirection *= -1;
                    else {
                        mSortBy = SORT_BY_LENGTH;
                        mSortDirection *= 1;
                    }
                    break;
                case SORT_BY_DATE:
                    if (mSortBy == SORT_BY_DATE)
                        mSortDirection *= -1;
                    else {
                        mSortBy = SORT_BY_DATE;
                        mSortDirection *= 1;
                    }
                    break;
                default:
                    mSortBy = SORT_BY_TITLE;
                    mSortDirection = 1;
                    break;
            }
            ArrayList<MediaWrapper> list = new ArrayList<>(mVideos);
            update(list, true);

            mSettings.edit()
                    .putInt(KEY_SORT_BY, mSortBy)
                    .putInt(KEY_SORT_DIRECTION, mSortDirection)
                    .apply();
        }

        @Override
        public int compare(MediaWrapper item1, MediaWrapper item2) {
            if (item1 == null)
                return item2 == null ? 0 : -1;
            else if (item2 == null)
                return 1;

            int compare = 0;
            switch (mSortBy) {
                case SORT_BY_TITLE:
                    compare = item1.getTitle().toUpperCase(Locale.ENGLISH).compareTo(
                            item2.getTitle().toUpperCase(Locale.ENGLISH));
                    break;
                case SORT_BY_LENGTH:
                    compare = ((Long) item1.getLength()).compareTo(item2.getLength());
                    break;
                case SORT_BY_DATE:
                    compare = ((Long) item1.getLastModified()).compareTo(item2.getLastModified());
                    break;
            }
            return mSortDirection * compare;
        }
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @MainThread
    void restoreList() {
        if (mOriginalData != null) {
            update(new ArrayList<>(mOriginalData), false);
            mOriginalData = null;
        }
    }

    private class ItemFilter extends MediaItemFilter {

        @Override
        protected List<MediaWrapper> initData() {
            if (mOriginalData == null) {
                mOriginalData = new ArrayList<>(mVideos.size());
                for (int i = 0; i < mVideos.size(); ++i)
                    mOriginalData.add(mVideos.get(i));
            }
            return mOriginalData;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            //noinspection unchecked
            update((ArrayList<MediaWrapper>) filterResults.values, false);
        }
    }

    @BindingAdapter({"time", "resolution"})
    public static void setLayoutHeight(View view, String time, String resolution) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = TextUtils.isEmpty(time) && TextUtils.isEmpty(resolution) ?
                ViewGroup.LayoutParams.MATCH_PARENT :
                ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    @MainThread
    void update(final ArrayList<MediaWrapper> items, final boolean detectMoves) {
        mPendingUpdates.add(items);
        if (mPendingUpdates.size() == 1) {
            internalUpdate(items, detectMoves);
        } else {
            LogUtil.d(TAG, "xxxx mPendingUpdates.size()=" + mPendingUpdates.size());
        }
    }

    private void internalUpdate(final ArrayList<MediaWrapper> items, final boolean detectMoves) {
        int cnt = 0;
        for (MediaWrapper media : items) {
            LogUtil.d(TAG, "xxxx internalUpdate items[" + cnt++ + "] " + media.getUri().getPath() + " " +
                    media.getArtworkMrl());
        }

        try {
            mUpdateExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Collections.sort(items, mVideoComparator);
                    final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new VideoItemDiffCallback(mVideos, items), detectMoves);
                    int i = 0;
                    for (MediaWrapper media : mVideos) {
                        LogUtil.d(TAG, "xxxx internalUpdate 0 mVideos[" + i++ + "] " + media.getUri().getPath() + " " +
                                media.getArtworkMrl());
                    }
                    VLCApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {

                            int cnt = 0;
                            for (MediaWrapper media : mVideos) {
                                LogUtil.d(TAG, "xxxx internalUpdate 1 mVideos[" + cnt++ + "] " + media.getUri().getPath() + " " +
                                        media.getArtworkMrl());
                            }
                            mVideos = items;
                            cnt = 0;
                            for (MediaWrapper media : mVideos) {
                                LogUtil.d(TAG, "xxxx internalUpdate 2 mVideos[" + cnt++ + "] " + media.getUri().getPath() + " " +
                                        media.getArtworkMrl());
                            }
                            result.dispatchUpdatesTo(VideoListAdapter.this);

                            mPendingUpdates.remove();
                            if (mPendingUpdates.isEmpty())
                                mEventsHandler.onUpdateFinished(null);
                            else {
                                ArrayList<MediaWrapper> lastList = mPendingUpdates.peekLast();
                                if (!mPendingUpdates.isEmpty()) {
                                    mPendingUpdates.clear();
                                    mPendingUpdates.add(lastList);
                                }
                                internalUpdate(lastList, false);
                            }
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ignored) {} // Will be retried
    }

    private class VideoItemDiffCallback extends DiffUtil.Callback {
        ArrayList<MediaWrapper> oldList, newList;
        VideoItemDiffCallback(ArrayList<MediaWrapper> oldList, ArrayList<MediaWrapper> newList) {
            this.oldList = new ArrayList<>(oldList);
            this.newList = new ArrayList<>(newList);
        }

        @Override
        public int getOldListSize() {
            return oldList == null ? 0 : oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList == null ? 0 : newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            MediaWrapper oldItem = oldList.get(oldItemPosition);
            MediaWrapper newItem = newList.get(newItemPosition);
            return oldItem == newItem ||
                    (oldList != null && newList != null && oldItem.getType() == newItem.getType() && oldItem.equals(newItem));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            MediaWrapper oldItem = oldList.get(oldItemPosition);
            MediaWrapper newItem = newList.get(newItemPosition);
            return oldItem == newItem ||
                    (oldItem.getTime() == newItem.getTime() && TextUtils.equals(oldItem.getArtworkMrl(), newItem.getArtworkMrl()));
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            MediaWrapper oldItem = oldList.get(oldItemPosition);
            MediaWrapper newItem = newList.get(newItemPosition);
            if (oldItem.getTime() != newItem.getTime())
                return UPDATE_TIME;
            else {
                LogUtil.d(TAG, "xxxx getChangePayload UPDATE_THUMB oldItem:" + oldItemPosition +
                        " newItem:" + newItemPosition);
                return UPDATE_THUMB;
            }
        }
    }
}
