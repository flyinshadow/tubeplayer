package com.wenjoyai.tubeplayer.gui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.ad.MyToast;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.ShareUtils;
import com.wenjoyai.tubeplayer.util.Util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by yuqilin on 2017/9/13.
 */

public class RateFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "RateFragment";

    public static final String KEY_RATE_SHOW_LAST = "rate_show_last";       // long
    public static final String KEY_RATE_SHOW_NEXT = "rate_show_next";       // long
    public static final String KEY_RATE_SHOW_COUNT = "rate_show_count";     // int
    public static final String KEY_RATE_LAST_VERSION = "rate_last_version"; // int

    private static SharedPreferences sSettings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());

    private static final int CHECK_COUNT_PEROID = 24 * 60 * 1000; // 一天

    private TextView mRateAction;
    private View mCancel;
    private View mDislike;
    private long mNextTime = 0;
    private TextView mRateTips;

    private ImageView oneIv,twoIv,threeIv,fourIv,fiveIv;

    private Calendar mCalendar = Calendar.getInstance();

    private int mRateCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View v = inflater.inflate(R.layout.rate, container, false);
        mRateAction = (TextView) v.findViewById(R.id.rate_action);
        mCancel = v.findViewById(R.id.rate_cancel);
//        mDislike = v.findViewById(R.id.rate_dislike);
        mRateAction.setOnClickListener(this);
        mCancel.setOnClickListener(this);
//        mDislike.setOnClickListener(this);
//        setCancelable(false);
        mRateTips = (TextView) v.findViewById(R.id.rate_tips);

        oneIv = (ImageView) v.findViewById(R.id.one);
        twoIv = (ImageView) v.findViewById(R.id.two);
        threeIv = (ImageView) v.findViewById(R.id.three);
        fourIv = (ImageView) v.findViewById(R.id.four);
        fiveIv = (ImageView) v.findViewById(R.id.five);
        oneIv.setOnClickListener(this);
        twoIv.setOnClickListener(this);
        threeIv.setOnClickListener(this);
        fourIv.setOnClickListener(this);
        fiveIv.setOnClickListener(this);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        long time = new Date().getTime();
        switch (view.getId()) {
            case R.id.rate_action:
                if (mRateCount >= 4) {
                    MyToast.makeText(VLCApplication.getAppContext(), "", Toast.LENGTH_LONG).show();
                    StatisticsManager.submitRate(VLCApplication.getAppContext(), StatisticsManager.ITEM_RATE_STAR);
                    ShareUtils.launchAppDetail(VLCApplication.getAppContext(), VLCApplication.getAppContext().getPackageName());
                } else {
                    StatisticsManager.submitRate(VLCApplication.getAppContext(), StatisticsManager.ITEM_RATE_DISLIKE);
                    ShareUtils.adviceEmail(VLCApplication.getAppContext());
                }
                time = Util.getDateNext(7);
                mNextTime = time;
                dismiss();
                break;
            case R.id.rate_cancel:

                StatisticsManager.submitRate(getActivity(), StatisticsManager.ITEM_RATE_CANCEL);

                LogUtil.d(TAG, "rate_cancel last time:" + time + "(" + Util.millisToDate(time) + ")");
                time = Util.getDateNext(1);
                LogUtil.d(TAG, "rate_cancel next time:" + time + "(" + Util.millisToDate(time) + ")");
                mNextTime = time;
                dismiss();
                break;
//            case R.id.rate_dislike:
//
//                StatisticsManager.submitRate(getActivity(), StatisticsManager.ITEM_RATE_DISLIKE);
//
//                LogUtil.d(TAG, "rate_dislike last time:" + time + "(" + Util.millisToDate(time) + ")");
//                time = Util.getDateNext(7);
//                LogUtil.d(TAG, "rate_dislike next time:" + time + "(" + Util.millisToDate(time) + ")");
//                mNextTime = time;
//                dismiss();
//                break;
            case R.id.one:
                mRateCount=1;
                oneIv.setImageResource(R.drawable.rate1_normal);
                twoIv.setImageResource(R.drawable.rate2_grey);
                threeIv.setImageResource(R.drawable.rate3_grey);
                fourIv.setImageResource(R.drawable.rate4_grey);
                fiveIv.setImageResource(R.drawable.rate5_grey);
                mRateTips.setText(VLCApplication.getAppResources().getString(R.string.rate_content_1_4));
                mRateAction.setText(VLCApplication.getAppResources().getString(R.string.rate_feedback));
                break;
            case R.id.two:
                mRateCount=2;
                oneIv.setImageResource(R.drawable.rate2_normal);
                twoIv.setImageResource(R.drawable.rate2_normal);
                threeIv.setImageResource(R.drawable.rate3_grey);
                fourIv.setImageResource(R.drawable.rate4_grey);
                fiveIv.setImageResource(R.drawable.rate5_grey);
                mRateTips.setText(VLCApplication.getAppResources().getString(R.string.rate_content_1_4));
                mRateAction.setText(VLCApplication.getAppResources().getString(R.string.rate_feedback));
                break;
            case R.id.three:
                mRateCount=3;
                oneIv.setImageResource(R.drawable.rate3_normal);
                twoIv.setImageResource(R.drawable.rate3_normal);
                threeIv.setImageResource(R.drawable.rate3_normal);
                fourIv.setImageResource(R.drawable.rate4_grey);
                fiveIv.setImageResource(R.drawable.rate5_grey);
                mRateTips.setText(VLCApplication.getAppResources().getString(R.string.rate_content_1_4));
                mRateAction.setText(VLCApplication.getAppResources().getString(R.string.rate_feedback));
                break;
            case R.id.four:
                mRateCount=4;
                oneIv.setImageResource(R.drawable.rate4_normal);
                twoIv.setImageResource(R.drawable.rate4_normal);
                threeIv.setImageResource(R.drawable.rate4_normal);
                fourIv.setImageResource(R.drawable.rate4_normal);
                fiveIv.setImageResource(R.drawable.rate5_grey);
                mRateTips.setText(VLCApplication.getAppResources().getString(R.string.rate_content_1_4));
                mRateAction.setText(VLCApplication.getAppResources().getString(R.string.rate_us));
                break;
            case R.id.five:
                mRateCount=5;
                oneIv.setImageResource(R.drawable.rate5_normal);
                twoIv.setImageResource(R.drawable.rate5_normal);
                threeIv.setImageResource(R.drawable.rate5_normal);
                fourIv.setImageResource(R.drawable.rate5_normal);
                fiveIv.setImageResource(R.drawable.rate5_normal);
                mRateTips.setText(VLCApplication.getAppResources().getString(R.string.rate_content_5));
                mRateAction.setText(VLCApplication.getAppResources().getString(R.string.rate_us));
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
        if (mNextTime == -1) {
            sSettings.edit().putInt(KEY_RATE_LAST_VERSION, VLCApplication.getVersionCode()).apply();
        }

        if (getActivity() != null && (getActivity() instanceof DialogActivity)) {
            getActivity().finish();
        }
    }
}
