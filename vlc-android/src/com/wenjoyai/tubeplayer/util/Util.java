/*****************************************************************************
 * UiTools.java
 *****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
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

package com.wenjoyai.tubeplayer.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.Nullable;

import org.videolan.medialibrary.Tools;
import org.videolan.medialibrary.media.MediaWrapper;

import com.wenjoyai.tubeplayer.VLCApplication;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class Util {
    public final static String TAG = "VLC/Util";

    public static String readAsset(String assetName, String defaultS) {
        InputStream is = null;
        BufferedReader r = null;
        try {
            is = VLCApplication.getAppResources().getAssets().open(assetName);
            r = new BufferedReader(new InputStreamReader(is, "UTF8"));
            StringBuilder sb = new StringBuilder();
            String line = r.readLine();
            if(line != null) {
                sb.append(line);
                line = r.readLine();
                while(line != null) {
                    sb.append('\n');
                    sb.append(line);
                    line = r.readLine();
                }
            }
            return sb.toString();
        } catch (IOException e) {
            return defaultS;
        } finally {
            close(is);
            close(r);
        }
    }

    public static boolean close(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
                return true;
            } catch (IOException e) {}
        return false;
    }

    public static boolean isListEmpty(@Nullable Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isCallable(Intent intent) {
        List<ResolveInfo> list = VLCApplication.getAppContext().getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void removeItemInArray(Object[] array, Object item, Object[] destArray) {
        int offset = 0, count = destArray.length;
        for (int i = 0; i<count; ++i) {
            if (array[i].equals(item))
                offset = 1;
            destArray[i] = array[i+offset];
        }
    }

    public static void removePositionInArray(Object[] array, int position, Object[] destArray) {
        int offset = 0, count = destArray.length;
        for (int i = 0; i<count; ++i) {
            if (i == position)
                ++offset;
            destArray[i] = array[i+offset];
        }
    }

    public static void addItemInArray(Object[] array, int position, Object item, Object[] destArray) {
        int offset = 0, count = destArray.length;
        for (int i = 0; i < count; ++i) {
            if (i == position) {
                ++offset;
                destArray[i] = item;
            } else
                destArray[i] = array[i-offset];
        }
    }

    public static boolean arrayContains(Object[] array, Object item) {
        if (Tools.isArrayEmpty(array))
            return false;
        for (Object obj : array)
            if (obj.equals(item))
                return true;
        return false;
    }

    public static String millisToDate(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(new Date(millis));
    }

    public static long getDateNext(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, days);
        return calendar.getTimeInMillis();
    }

    public static void insertOrUdpate(ArrayList<MediaWrapper> dataset, MediaWrapper[] items) {
        ArrayList<MediaWrapper> newItems = new ArrayList<>();
        outer:
        for (MediaWrapper newItem : items) {
            for (ListIterator it = dataset.listIterator(); it.hasNext();) {
                MediaWrapper oldItem = (MediaWrapper) it.next();
                if (newItem.equals(oldItem)) {
                    it.set(newItem);
                    continue outer;
                }
            }
            newItems.add(newItem);
        }
        dataset.addAll(newItems);
    }
}
