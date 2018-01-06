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
import android.widget.TextView;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;

/**
 * 播放页面定时对话框
 */
public class TimerDialog extends DialogFragment{

    public final static String TAG = "VLC/PlaybackSpeedDialog";

    private ImageView mClose;
    private TextView mTimerOff;
    private TextView mTimer15;
    private TextView mTimer30;
    private TextView mTimer45;
    private TextView mTimer60;
    private TextView mTimerEnd;


    private TimerListener mTimerListener;

    //timeType: 0-5 分别对应 off 15 30 45 60 end
    public interface TimerListener {
        void timer(int timeType);
    }

    public TimerDialog() {
    }

    public static TimerDialog newInstance(int theme) {
        TimerDialog myFragment = new TimerDialog();

        Bundle args = new Bundle();
        args.putInt("theme", theme);
        myFragment.setArguments(args);

        return myFragment;
    }
    public void setListener(TimerListener listener){
        mTimerListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, getArguments().getInt("theme"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_timer, container);
        mTimerOff = (TextView)view.findViewById(R.id.timer_off);
        mTimer15 = (TextView)view.findViewById(R.id.timer_15);
        mTimer30 = (TextView)view.findViewById(R.id.timer_30);
        mTimer45 = (TextView)view.findViewById(R.id.timer_45);
        mTimer60 = (TextView)view.findViewById(R.id.timer_60);
        mTimerEnd = (TextView)view.findViewById(R.id.timer_end);
        mClose = (ImageView) view.findViewById(R.id.close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mTimerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null!= mTimerListener){
                    mTimerListener.timer(0);
                }
            }
        });
        mTimer15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null!= mTimerListener){
                    mTimerListener.timer(1);
                }
            }
        });
        mTimer30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null!= mTimerListener){
                    mTimerListener.timer(2);
                }
            }
        });
        mTimer45.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null!= mTimerListener){
                    mTimerListener.timer(3);
                }
            }
        });
        mTimer60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null!= mTimerListener){
                    mTimerListener.timer(4);
                }
            }
        });
        mTimerEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null!= mTimerListener){
                    mTimerListener.timer(5);
                }
            }
        });

        getDialog().setCancelable(true);
        getDialog().setCanceledOnTouchOutside(true);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(UiTools.getResourceFromAttribute(getActivity(), R.attr.rounded_bg));
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        return view;
    }
}
