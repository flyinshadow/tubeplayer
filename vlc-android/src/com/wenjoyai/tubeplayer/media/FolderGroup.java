package com.wenjoyai.tubeplayer.media;

import com.wenjoyai.tubeplayer.gui.helpers.BitmapUtil;
import com.wenjoyai.tubeplayer.util.AndroidDevices;
import com.wenjoyai.tubeplayer.util.FileUtils;

import org.videolan.medialibrary.media.MediaWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yuqilin on 2017/9/6.
 */

public class FolderGroup extends MediaWrapper {

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

    public String getFolderPath() {
        return mFolderPath;
    }

    public void add(MediaWrapper media) {
        mMedias.add(media);
    }

    public int size() {
        return mMedias.size();
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
