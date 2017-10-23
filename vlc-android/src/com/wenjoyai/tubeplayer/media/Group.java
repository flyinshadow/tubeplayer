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

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcel;
import android.preference.PreferenceManager;

import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.helpers.BitmapUtil;

import org.videolan.medialibrary.media.MediaWrapper;

import java.util.ArrayList;
import java.util.List;

public abstract class Group extends MediaWrapper {

    public final static String TAG = "VLC/Group";

    private ArrayList<MediaWrapper> mMedias;

    public Group(MediaWrapper media) {
        super(media.getUri(),
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
        mMedias = new ArrayList<MediaWrapper>();
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

    public List<Group> group(MediaWrapper[] mediaList) {
        ArrayList<Group> groups = new ArrayList<>();
        for (MediaWrapper media : mediaList)
            if (media != null)
                insertInto(groups, media);
        return groups;
    }

    public List<Group> group(List<MediaWrapper> mediaList) {
        ArrayList<Group> groups = new ArrayList<>();
        for (MediaWrapper media : mediaList)
            if (media != null)
                insertInto(groups, media);
        return groups;
    }

    public abstract void insertInto(List<Group> groups, MediaWrapper media);

}