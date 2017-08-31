/*****************************************************************************
 * AboutActivity.java
 *****************************************************************************
 * Copyright © 2011-2012 VLC authors and VideoLAN
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

package com.wenjoyai.tubeplayer.gui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;

public class AboutFragment extends Fragment {
    public final static String TAG = "VLC/AboutActivity";

//    public final static int MODE_ABOUT = 0;
//    public final static int MODE_LICENCE = 1;
//    public final static int MODE_TOTAL = 2; // Number of audio browser modes

//    private ViewPager mViewPager;
//    private TabLayout mTabLayout;

    private TextView mVersion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (getActivity() instanceof AppCompatActivity)
//            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("VLC " + BuildConfig.VERSION_NAME);
        View v = inflater.inflate(R.layout.about, container, false);
//        //Fix android 7 Locale problem with webView
//        //https://stackoverflow.com/questions/40398528/android-webview-locale-changes-abruptly-on-android-n
//        if (AndroidUtil.isNougatOrLater)
//            VLCApplication.setLocale(getContext());
//
//        View aboutMain = v.findViewById(R.id.about_main);
//        WebView webView = (WebView)v.findViewById(R.id.webview);
//        String revision = getString(R.string.build_revision);
//        webView.loadData(Util.readAsset("licence.htm", "").replace("!COMMITID!",revision), "text/html", "UTF8");
//
//
//        UiTools.fillAboutView(v);
//
//        View[] lists = new View[]{aboutMain, webView};
//        String[] titles = new String[] {getString(R.string.about), getString(R.string.licence)};
//        mViewPager = (ViewPager) v.findViewById(R.id.pager);
//        mViewPager.setOffscreenPageLimit(MODE_TOTAL-1);
//        mViewPager.setAdapter(new AudioPagerAdapter(lists, titles));
//
//        mTabLayout = (TabLayout) v.findViewById(R.id.sliding_tabs);
//        mTabLayout.setupWithViewPager(mViewPager);

        mVersion = (TextView) v.findViewById(R.id.version);

        mVersion.setText(String.format(getString(R.string.about_version), getVersion()));

        return v;
    }

    private String getVersion() {
        try {
            PackageManager manager = VLCApplication.getAppContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(VLCApplication.getAppContext().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
