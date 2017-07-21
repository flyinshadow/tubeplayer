package com.wenjoyai.videoplayer.gui.tv;

import android.app.Activity;
import android.os.Bundle;

import com.wenjoyai.videoplayer.R;
import com.wenjoyai.videoplayer.gui.helpers.UiTools;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);
        UiTools.fillAboutView(getWindow().getDecorView().getRootView());
        TvUtil.applyOverscanMargin(this);
    }
}
