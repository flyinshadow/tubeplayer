package com.wenjoyai.tubeplayer.ad;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.wenjoyai.tubeplayer.R;


/**
 * Created by LiJiaZhi on 16/12/31.
 * open ad loading对话框
 */

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    public LoadingDialog(Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_open_ad_loading);
    }
}
