/*****************************************************************************
 * MediaGroup.java
 *****************************************************************************
 * Copyright © 2013 VLC authors and VideoLAN
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

package com.wenjoyai.tubeplayer.media;

import android.net.Uri;
import android.os.Parcel;

import com.wenjoyai.tubeplayer.gui.helpers.BitmapUtil;

import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import java.util.ArrayList;
import java.util.List;

public abstract class Group extends MediaWrapper {

    public final static String TAG = "VLC/Group";

    protected ArrayList<MediaWrapper> mMedias;

    public Group(MediaWrapper media, Uri uri) {
        super(uri == null ? media.getUri() : uri,
                media.getTime(),
                media.getLength(),
                MediaWrapper.TYPE_GROUP,
                BitmapUtil.getPicture(media),
                media.getTitle(),
                media.getArtist(),
                media.getGenre(),
                media.getAlbum(),
                media.getAlbumArtist(),
                media.getWidth(),
                media.getHeight(),
                media.getArtworkURL(),
                media.getAudioTrack(),
                media.getSpuTrack(),
                media.getTrackNumber(),
                media.getDiscNumber(),
                media.getLastModified());
        mMedias = new ArrayList<>();
        mMedias.add(media);
    }

    public Group(Parcel in) {
        super(in);
    }

    public Group(Uri uri) {
        super(uri);
    }

    public String getDisplayTitle() {
        return getTitle() + "\u2026";
    }

    public void add(MediaWrapper media) {
        mMedias.add(media);
    }

    public MediaWrapper getMedia() {
        return mMedias.size() == 1 ? mMedias.get(0) : this;
    }

    public MediaWrapper getFirstMedia() {
        return mMedias.get(0);
    }

    public ArrayList<MediaWrapper> getAll() {
        return mMedias;
    }

    public int size() {
        return mMedias.size();
    }

    public void merge(MediaWrapper media, String title) {
        mMedias.add(media);
        this.mTitle = title;
    }

    public List<MediaWrapper> group(MediaWrapper[] mediaList) {
        ArrayList<MediaWrapper> groups = new ArrayList<>();
        for (MediaWrapper media : mediaList)
            if (media != null)
                insertInto(groups, media);
        return groups;
    }

    public List<MediaWrapper> group(List<MediaWrapper> mediaList) {
        ArrayList<MediaWrapper> groups = new ArrayList<>();
        for (MediaWrapper media : mediaList)
            if (media != null)
                insertInto(groups, media);
        return groups;
    }

    public abstract void insertInto(List<MediaWrapper> groups, MediaWrapper media);

    @Override
    public boolean equals(MediaLibraryItem other) {
        if (other instanceof Group && super.equals(other)) {
            return this.mMedias.size() == ((Group) other).mMedias.size();
        }
        return false;
    }
}