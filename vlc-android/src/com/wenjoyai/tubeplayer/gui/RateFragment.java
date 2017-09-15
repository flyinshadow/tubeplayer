package com.wenjoyai.tubeplayer.gui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.Util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by yuqilin on 2017/9/13.
 */

public class RateFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "RateFragment";

    public static final String KEY_RATE_SHOW_LAST = "rate_show_last";
    public static final String KEY_RATE_SHOW_NEXT = "rate_show_next";
    public static final String KEY_RATE_SHOW_COUNT = "rate_show_count";

    private static SharedPreferences sSettings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());

    private static final int CHECK_COUNT_PEROID = 24 * 60 * 1000; // 一天

    private View mRateStar;
    private View mCancel;
    private View mDislike;
    private long mNextTime = 0;

    private Calendar mCalendar = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.rate, container, false);
        mRateStar = v.findViewById(R.id.rate_star);
        mCancel = v.findViewById(R.id.rate_cancel);
        mDislike = v.findViewById(R.id.rate_dislike);
        mRateStar.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mDislike.setOnClickListener(this);
//        setCancelable(false);
        return v;
    }

    @Override
    public void onClick(View view) {
        long time = new Date().getTime();
        switch (view.getId()) {
            case R.id.rate_star:
                LogUtil.d(TAG, "rate_star time:" + time + "(" + Util.millisToDate(time) + ")");
                mNextTime = -1;
                dismiss();
                break;
            case R.id.rate_cancel:
                LogUtil.d(TAG, "rate_cancel last time:" + time + "(" + Util.millisToDate(time) + ")");
                time = Util.getDateNext(1);
                LogUtil.d(TAG, "rate_cancel next time:" + time + "(" + Util.millisToDate(time) + ")");
                mNextTime = time;
                dismiss();
                break;
            case R.id.rate_dislike:
                LogUtil.d(TAG, "rate_dislike last time:" + time + "(" + Util.millisToDate(time) + ")");
                time = Util.getDateNext(7);
                LogUtil.d(TAG, "rate_dislike next time:" + time + "(" + Util.millisToDate(time) + ")");
                mNextTime = time;
                dismiss();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        long currentTime = new Date().getTime();
        long lastTime = sSettings.getLong(KEY_RATE_SHOW_LAST, 0);
        int count = sSettings.getInt(KEY_RATE_SHOW_COUNT, 0);
        LogUtil.d(TAG, "onDismiss, currentTime:" + currentTime + "(" + Util.millisToDate(currentTime) + ")" +
                " lastTime: " + lastTime + "(" + Util.millisToDate(lastTime) + ")" +
                " nextTime:" + mNextTime + "(" + Util.millisToDate(mNextTime) + ")" +
                " count:" + count);
        // 未点击任意按钮
        if (mNextTime == 0) {
            if (lastTime == 0 || currentTime - lastTime >= CHECK_COUNT_PEROID) {
                lastTime = currentTime;
                count = 0;
                sSettings.edit().putLong(KEY_RATE_SHOW_LAST, lastTime).apply();
            }
            // 上次提示在一天内
            if (currentTime - lastTime < CHECK_COUNT_PEROID) {
                // 最多5次
                if (count < 5) {
                    count++;
                    sSettings.edit().putInt(KEY_RATE_SHOW_COUNT, count).apply();
                } else {
                    // 超过5次，下次提示在一天后
                    mNextTime = lastTime + CHECK_COUNT_PEROID;
                    LogUtil.d(TAG, "onDismiss, reach 5 times nextTime:" + mNextTime + "(" + Util.millisToDate(mNextTime) + ")");
                }
            }
        } else {
            // 重置计数
            sSettings.edit().putInt(KEY_RATE_SHOW_COUNT, 0).apply();
            sSettings.edit().putLong(KEY_RATE_SHOW_LAST, 0).apply();
        }
        // 记录下次提示时间
        sSettings.edit().putLong(KEY_RATE_SHOW_NEXT, mNextTime).apply();

        getActivity().finish();
    }
}
