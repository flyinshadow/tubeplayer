package com.wenjoyai.tubeplayer.extensions;

import android.net.Uri;

import org.videolan.medialibrary.media.MediaWrapper;
import com.wenjoyai.tubeplayer.extensions.api.VLCExtensionItem;

public class Utils {

    public static MediaWrapper mediawrapperFromExtension(VLCExtensionItem vlcItem) {
                MediaWrapper media = new MediaWrapper(Uri.parse(vlcItem.link));
                media.setDisplayTitle(vlcItem.title);
                if (vlcItem.type != VLCExtensionItem.TYPE_OTHER_FILE)
                    media.setType(vlcItem.type);
        return media;

    }
}