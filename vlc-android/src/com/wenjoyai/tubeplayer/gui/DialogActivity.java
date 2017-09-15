/*
 * ***************************************************************************
 * DialogActivity.java
 * ***************************************************************************
 * Copyright © 2016 VLC authors and VideoLAN
 * Author: Geoffrey Métais
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
 * ***************************************************************************
 */

package com.wenjoyai.tubeplayer.gui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.wenjoyai.tubeplayer.MediaParsingService;
import com.wenjoyai.tubeplayer.gui.dialogs.ExternalStorageDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.VlcDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.VlcLoginDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.VlcProgressDialog;
import com.wenjoyai.tubeplayer.gui.dialogs.VlcQuestionDialog;
import com.wenjoyai.tubeplayer.gui.network.MRLPanelFragment;

public class DialogActivity extends AppCompatActivity {

    public static final String KEY_LOGIN = "LoginDialog";
    public static final String KEY_QUESTION = "QuestionDialog";
    public static final String KEY_PROGRESS = "ProgressDialog";
    public static final String KEY_STREAM = "streamDialog";
    public static final String KEY_STORAGE = "storageDialog";
    public static final String KEY_RATE = "rateDialog";

    public static boolean sRateStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key = getIntent().getAction();
        if (TextUtils.isEmpty(key)) {
            finish();
            return;
        }
        if (key.startsWith(KEY_LOGIN))
            setupLoginDialog(key);
        else if (key.startsWith(KEY_QUESTION))
            setupQuestionDialog(key);
        else if (key.startsWith(KEY_PROGRESS))
            setupProgressDialog(key);
        else if (KEY_STREAM.equals(key))
            setupStreamDialog();
        else if (KEY_STORAGE.equals(key))
            setupStorageDialog();
        else if (KEY_RATE.equals(key))
            startRateDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sRateStarted = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setupStorageDialog() {
        ExternalStorageDialog dialog = new ExternalStorageDialog();
        Bundle b = new Bundle(2);
        b.putString(MediaParsingService.EXTRA_PATH, getIntent().getStringExtra(MediaParsingService.EXTRA_PATH));
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), "fragment_storage");
    }

    private void setupStreamDialog() {
        new MRLPanelFragment().show(getSupportFragmentManager(), "fragment_mrl");
    }

    private void setupLoginDialog(String key) {
        VlcLoginDialog dialog = new VlcLoginDialog();
        startVlcDialog(key, dialog);
    }

    private void setupQuestionDialog(String key) {
        VlcQuestionDialog dialog = new VlcQuestionDialog();
        startVlcDialog(key, dialog);
    }

    private void setupProgressDialog(String key) {
        VlcProgressDialog dialog = new VlcProgressDialog();
        startVlcDialog(key, dialog);
    }

    private void startVlcDialog(String key, VlcDialog dialog) {
        dialog.init(key);
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, key);
    }

    private void startRateDialog() {
        sRateStarted = true;
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        if (mRate != null && mRate.isVisible())
//            return;
//        if (mRate == null) {
//            mRate = new RateFragment();
//        }
        new RateFragment().show(getSupportFragmentManager(), "rate_fragment");
    }
}
