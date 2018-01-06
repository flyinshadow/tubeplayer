package com.wenjoyai.tubeplayer.util;

import android.support.v7.util.DiffUtil;

import com.wenjoyai.tubeplayer.media.AdItem;

import org.videolan.medialibrary.media.MediaLibraryItem;

import java.util.List;


public class MediaItemDiffCallback extends DiffUtil.Callback {
    private static final String TAG = "MediaItemDiffCallback";
    private MediaLibraryItem[] oldList, newList;

    public MediaItemDiffCallback(List<? extends MediaLibraryItem> oldList, List<? extends MediaLibraryItem> newList) {
        this.oldList = oldList.toArray(new MediaLibraryItem[oldList.size()]);
        this.newList = newList.toArray(new MediaLibraryItem[newList.size()]);
    }

    public MediaItemDiffCallback(MediaLibraryItem[] oldList, MediaLibraryItem[] newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList == null ? 0 :oldList.length;
    }

    @Override
    public int getNewListSize() {
        return newList == null ? 0 : newList.length;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return (oldList[oldItemPosition] == null ) == ( newList[newItemPosition] == null) && (oldList[oldItemPosition].equals(newList[newItemPosition]) ||
                (oldList[oldItemPosition].getItemType() == MediaLibraryItem.TYPE_AD && newList[newItemPosition].getItemType() == MediaLibraryItem.TYPE_AD));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        MediaLibraryItem oldItem = oldList[oldItemPosition];
        MediaLibraryItem newItem = newList[newItemPosition];
        if (oldItem != null && newItem != null && oldItem.getItemType() == MediaLibraryItem.TYPE_AD && newItem.getItemType() == MediaLibraryItem.TYPE_AD) {
            String ad1 = ((AdItem)oldItem).getNativeAd().getAdBody();
            String ad2 = ((AdItem)newItem).getNativeAd().getAdBody();
            return ad1.equals(ad2);
        }
        return true;
    }
}
