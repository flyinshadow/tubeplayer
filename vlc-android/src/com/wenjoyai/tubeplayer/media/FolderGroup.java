package com.wenjoyai.tubeplayer.media;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.FileUtils;

import org.videolan.medialibrary.Medialibrary;
import org.videolan.medialibrary.media.MediaLibraryItem;
import org.videolan.medialibrary.media.MediaWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yuqilin on 2017/9/6.
 */

public class FolderGroup extends Group implements Parcelable {

    private static FolderGroup dummy = new FolderGroup(Uri.parse("file://dummy"));

    private String mFolderPath;
//    private ArrayList<MediaWrapper> mMedias = new ArrayList<>();

    public FolderGroup(MediaWrapper media) {
//        super(media.getUri(),
//                media.getTime(),
//                media.getLength(),
//                MediaWrapper.TYPE_GROUP,
//                null,
//                media.getTitle(),
//                media.getArtist(),
//                media.getGenre(),
//                media.getAlbum(),
//                media.getAlbumArtist(),
//                media.getWidth(),
//                media.getHeight(),
//                media.getArtworkURL(),
//                media.getAudioTrack(),
//                media.getSpuTrack(),
//                media.getTrackNumber(),
//                media.getDiscNumber(),
//                0l);
        super(media, Uri.parse(FileUtils.getParent(media.getUri().getPath())), null);
        mFolderPath = FileUtils.getParent(media.getUri().getPath());
        setTitle(getFolderTitle(mFolderPath));
        setLastModified(0L);
    }

    public static String getFolderTitle(String folderPath) {
        String folderName = FileUtils.getFileNameFromPath(folderPath);
        if (folderPath.equals(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY)) {
            folderName = VLCApplication.getAppResources().getString(R.string.internal_memory);
        }
        return folderName;
    }

    protected FolderGroup(Parcel in) {
        super(in);
        mFolderPath = in.readString();
        mMedias = in.createTypedArrayList(MediaWrapper.CREATOR);
    }

    public FolderGroup(Uri uri) {
        super(uri);
    }

    public static FolderGroup getDummy() {
        return dummy;
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

    @Override
    public MediaWrapper[] getTracks(Medialibrary ml) {
        return mMedias.toArray(new MediaWrapper[mMedias.size()]);
    }

    @Override
    public int getItemType() {
        return MediaLibraryItem.TYPE_FOLDER;
    }

    public static <T extends MediaWrapper> void sort(List<T> folders) {
        Collections.sort(folders, new Comparator<T>() {
            @Override
            public int compare(T item1, T item2) {
                FolderGroup itemX = (FolderGroup) item1;
                FolderGroup itemY = (FolderGroup) item2;
                return compareInternal(itemX, itemY);
            }

            private int compareInternal(FolderGroup item1, FolderGroup item2) {
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

    public void insertInto(List<MediaWrapper> groups, MediaWrapper media) {
        for (MediaWrapper group : groups) {
            FolderGroup folderGroup = (FolderGroup) group;
            String groupFolderPath = folderGroup.getFolderPath();
            String mediaFolderPath = FileUtils.getParent(media.getUri().getPath());

            if (mediaFolderPath.equals(groupFolderPath)) {
                folderGroup.add(media);
                return;
            }
        }
        // does not match any group, so add one
        groups.add(new FolderGroup(media));
    }

    @Override
    public boolean equals(MediaLibraryItem other) {
        if (other instanceof FolderGroup) {
            return TextUtils.equals((this).getFolderPath(), ((FolderGroup) other).getFolderPath())
                    && this.mMedias.size() == ((FolderGroup) other).mMedias.size();
        }
        return false;
    }
}
