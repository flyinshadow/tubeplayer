package com.wenjoyai.tubeplayer.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.ad.nativead.AbsNativeAd;
import com.facebook.ads.NativeAd;

import org.videolan.libvlc.Media;
import org.videolan.medialibrary.media.MediaWrapper;

/**
 * Created by yuqilin on 2017/10/8.
 */

public class AdItem extends MediaWrapper implements Parcelable {
    private AbsNativeAd mNativeAd;

    public AdItem(MediaWrapper media) {
        super(media.getUri(),
                media.getTime(),
                media.getLength(),
                MediaWrapper.TYPE_AD,
                null,
                media.getTitle(),
                media.getArtist(),
                media.getGenre(),
                media.getAlbum(),
                media.getAlbumArtist(),
                media.getWidth(),
                media.getHeight(),
                null, //media.getArtworkURL(),
                media.getAudioTrack(),
                media.getSpuTrack(),
                media.getTrackNumber(),
                media.getDiscNumber(),
                media.getLastModified());
    }

    public AdItem(Uri uri) {
        super(uri);
    }

    public AdItem(Media media) {
        super(media);
    }

    public AdItem(Parcel in) {
        super(in);
    }

    @Override
    public int getItemType() {
        return TYPE_AD;
    }

    public void setNativeAd(AbsNativeAd nativeAd) {
        mNativeAd = nativeAd;
    }

    public AbsNativeAd getNativeAd() {
        return mNativeAd;
    }
}
