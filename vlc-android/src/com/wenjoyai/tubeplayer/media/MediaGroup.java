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
import android.preference.PreferenceManager;

import org.videolan.medialibrary.media.MediaWrapper;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.helpers.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

public class MediaGroup extends Group {

    public final static String TAG = "VLC/MediaGroup";

    public MediaGroup(MediaWrapper media) {
        super(media);
    }

    public MediaGroup(Uri uri) {
        super(uri);
    }

    public static MediaGroup getDummy() {
        return new MediaGroup(Uri.parse("file://dummy"));
    }

    public void insertInto(List<Group> groups, MediaWrapper media) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
        int minGroupLengthValue = Integer.valueOf(preferences.getString("video_min_group_length", "6"));
        for (Group mediaGroup : groups) {
            String group = mediaGroup.getTitle();
            String title = media.getTitle();

            //Handle titles starting with "The"
            int groupOffset = group.toLowerCase().startsWith("the") ? 4 : 0;
            if (title.toLowerCase().startsWith("the"))
                title = title.substring(4);

            // find common prefix
            int commonLength = 0;
            String groupTitle = group.substring(groupOffset);
            int minLength = Math.min(groupTitle.length(), title.length());
            while (commonLength < minLength
                    && groupTitle.toLowerCase().charAt(commonLength) == title.toLowerCase().charAt(commonLength))
                ++commonLength;

            if (commonLength >= minGroupLengthValue && minGroupLengthValue != 0) {
                if (commonLength == group.length()) {
                    // same prefix name, just add
                    mediaGroup.add(media);
                } else {
                    // not the same prefix, but close : merge
                    mediaGroup.merge(media, group.substring(0, commonLength+groupOffset));
                }
                return;
            }
        }

        // does not match any group, so add one
        groups.add(new MediaGroup(media));
    }
}
