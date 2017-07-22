package com.wenjoyai.tubeplayer.gui.tv;

import android.app.Activity;
import android.os.Bundle;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);
        UiTools.fillAboutView(getWindow().getDecorView().getRootView());
        TvUtil.applyOverscanMargin(this);
    }
}
