package com.wenjoyai.tubeplayer.ad;

import com.facebook.ads.NativeAd;

/**
 * @author：LiJiaZhi on 2017/10/21
 * @des：ToDo
 * @org mtime.com
 */
public class NativeAdBean  {
    public NativeAd ad;
    public String adId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NativeAdBean that = (NativeAdBean) o;

        return adId.equals(that.adId);

    }

    @Override
    public int hashCode() {
        return adId.hashCode();
    }
}
