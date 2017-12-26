/**
 * **************************************************************************
 * PickTimeFragment.java
 * ****************************************************************************
 * Copyright © 2015 VLC authors and VideoLAN
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
package com.wenjoyai.tubeplayer.gui.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;

/**
 * 亮度对话框
 */
public class BrightnessDialog extends DialogFragment{

    public final static String TAG = "VLC/BrightnessDialog";

    private SeekBar mSeekBar;
    private ImageView mClose;

    public BrightnessDialog() {
    }

    public static BrightnessDialog newInstance(int theme) {
        BrightnessDialog myFragment = new BrightnessDialog();

        Bundle args = new Bundle();
        args.putInt("theme", theme);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, getArguments().getInt("theme"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_brightness, container);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mClose = (ImageView) view.findViewById(R.id.close);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getDialog().setCancelable(true);
        getDialog().setCanceledOnTouchOutside(true);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(UiTools.getResourceFromAttribute(getActivity(), R.attr.rounded_bg));
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        mSeekBar.setProgress((int)lp.screenBrightness);
        return view;
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                setWindowBrightness(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        getActivity().getWindow().setAttributes(lp);
    }
}
