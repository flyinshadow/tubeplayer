package com.wenjoyai.tubeplayer.media;

import android.os.Parcel;
import android.os.Parcelable;

import com.wenjoyai.tubeplayer.gui.helpers.BitmapUtil;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.FileUtils;

import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yuqilin on 2017/9/6.
 */

public class FolderGroup extends MediaWrapper implements Parcelable {

    private String mFolderPath;
    private ArrayList<MediaWrapper> mMedias = new ArrayList<>();

    public FolderGroup(MediaWrapper media) {
        super(media.getUri(),
                media.getTime(),
                media.getLength(),
                MediaWrapper.TYPE_GROUP,
                null,
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
                0l);
        mMedias.add(media);
        mFolderPath = FileUtils.getParent(media.getUri().getPath());
    }

    protected FolderGroup(Parcel in) {
        super(in);
        mFolderPath = in.readString();
        mMedias = in.createTypedArrayList(MediaWrapper.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mFolderPath);
        dest.writeTypedList(mMedias);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FolderGroup> CREATOR = new Creator<FolderGroup>() {
        @Override
        public FolderGroup createFromParcel(Parcel in) {
            return new FolderGroup(in);
        }

        @Override
        public FolderGroup[] newArray(int size) {
            return new FolderGroup[size];
        }
    };

    public String getFolderPath() {
        return mFolderPath;
    }

    public void add(MediaWrapper media) {
        mMedias.add(media);
    }

    public int size() {
        return mMedias.size();
    }

    @Override
    public MediaWrapper[] getTracks(Medialibrary ml) {
        return mMedias.toArray(new MediaWrapper[mMedias.size()]);
    }

    @Override
    public int getItemType() {
        return MediaLibraryItem.TYPE_FOLDER;
    }

    public static void sort(List<FolderGroup> folders) {
        Collections.sort(folders, new Comparator<FolderGroup>() {
            @Override
            public int compare(FolderGroup item1, FolderGroup item2) {
                if (item1 == null)
                    return item2 == null ? 0 : -1;
                else if (item2 == null)
                    return 1;
                if (item1.getFolderPath().equals(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY)) {
                    return -1;
                }
                if (item2.getFolderPath().equals(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY)) {
                    return 1;
                }
                String folderName1 = FileUtils.getFileNameFromPath(item1.getFolderPath());
                String folderName2 = FileUtils.getFileNameFromPath(item2.getFolderPath());
                int compare = folderName1.compareToIgnoreCase(folderName2);
                return compare;
            }
        });
    }

    public static List<FolderGroup> group(MediaWrapper[] mediaList) {
        ArrayList<FolderGroup> groups = new ArrayList<>();
        for (MediaWrapper media : mediaList)
            if (media != null)
                insertInto(groups, media);
        return groups;
    }

    public static void insertInto(List<FolderGroup> groups, MediaWrapper media) {
        for (FolderGroup group : groups) {
            String groupFolderPath = group.getFolderPath();
            String mediaFolderPath = FileUtils.getParent(media.getUri().getPath());

            if (mediaFolderPath.equals(groupFolderPath)) {
                group.add(media);
                return;
            }
        }
        // does not match any group, so add one
        groups.add(new FolderGroup(media));
    }

//    public static String getFolderName(String path) {
//        return FileUtils.getFileNameFromPath(path);
//    }
}
