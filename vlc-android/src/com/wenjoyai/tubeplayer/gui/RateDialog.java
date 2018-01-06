package com.wenjoyai.tubeplayer.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.ad.MyToast;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.video.VideoGridFragment;
import com.wenjoyai.tubeplayer.util.LogUtil;
import com.wenjoyai.tubeplayer.util.ShareUtils;
import com.wenjoyai.tubeplayer.util.Util;

import java.util.Date;

/**
 * Created by yuqilin on 2017/9/13.
 */

public class RateDialog extends DialogFragment implements View.OnClickListener, GestureDetector.OnGestureListener, View.OnTouchListener {
    private static final String TAG = "RateDialog";

    public static final String EXTRA_RATE_SCORE_DEFAULT = "rate_score_default";

    public static final String KEY_RATE_SHOW_LAST = "rate_show_last";       // long
    public static final String KEY_RATE_SHOW_NEXT = "rate_show_next";       // long
    public static final String KEY_RATE_SHOW_COUNT = "rate_show_count";     // int
    public static final String KEY_RATE_LAST_VERSION = "rate_last_version"; // int

    private static final int MIN_FLING_DISTANCE_X = 50;
    private static final int MIN_FLING_VELOCITY_X = 0;

    private static SharedPreferences sSettings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());

    private static final int CHECK_COUNT_PERIOD = 24 * 60 * 1000; // 一天

    private TextView mRateAction;
    private View mCancel;
    private View mDislike;
    private long mNextTime = sSettings.getLong(KEY_RATE_SHOW_NEXT, 0);
    private TextView mRateTips;

    private ImageView oneIv,twoIv,threeIv,fourIv,fiveIv;

    private int mRateScore = 5;
    private int mDefaultScore = 5;

    private View mRoot;
    private GestureDetectorCompat mDetector;

    private static boolean sStarted = false;

    public static boolean isShowing() {
        return sStarted;
    }

    public static boolean willShow() {
        if (!sSettings.getBoolean(VideoGridFragment.KEY_PARSING_ONCE, false)) {
            LogUtil.d(TAG, "rate tip will not show when media parsing");
            return false;
        }
        long lastTime = sSettings.getLong(RateDialog.KEY_RATE_SHOW_LAST, 0);
        long nextTime = sSettings.getLong(RateDialog.KEY_RATE_SHOW_NEXT, 0);
        int count = sSettings.getInt(RateDialog.KEY_RATE_SHOW_COUNT, 0);
        int versionCode = sSettings.getInt(RateDialog.KEY_RATE_LAST_VERSION, 0);
        long currentTime = new Date().getTime();
        LogUtil.d(TAG, "rate tip, currentTime:" + currentTime + "(" + Util.millisToDate(currentTime) + ")" +
                " lastTime:" + lastTime + "(" + Util.millisToDate(lastTime) + ")" +
                " nextTime:" + nextTime + "(" + Util.millisToDate(nextTime) + ")" +
                " count:" + count
        );

        boolean willShow = false;
        if (nextTime == -1) {
            // 本版本不提示
            LogUtil.d(TAG, "rate tip will not show this version");
        } else if (nextTime == 0) {
            // 可以提示
            LogUtil.d(TAG, "rate tip can start NOW");
            willShow = true;
        } else if (nextTime > 0) {
            // 到提示时间
            if (currentTime - nextTime >= 0) {
                LogUtil.d(TAG, "rate tip reach time, can start NOW");
                willShow = true;
            } else {
                LogUtil.d(TAG, "rate tip not reach time");
            }
        }
        return willShow;
    }

    public static void start(Context context, int defaultScore) {
        if (!sStarted) {
            Intent intent = new Intent(context, DialogActivity.class);
            intent.setAction(DialogActivity.KEY_RATE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_RATE_SCORE_DEFAULT, defaultScore);
            context.startActivity(intent);
            sStarted = true;
        }
    }

    public static void tryToShow(Context context, int defaultScore) {
        if (willShow()) {
            start(context, defaultScore);
        }
    }

    public void setDefaultScore(int defaultScore) {
        mDefaultScore = defaultScore;
    }

    public interface OnRateDismiss {
        void onDismiss();
    }
    OnRateDismiss mOnRateDismiss;
    public void setOnDismissRate(OnRateDismiss onRateDismiss) {
        mOnRateDismiss = onRateDismiss;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setWindowAnimations(R.style.RateDialogAnimation);
        }

        View v = inflater.inflate(R.layout.rate, container, false);

        mRoot = v.findViewById(R.id.rate_root_container);
        mRateAction = (TextView) v.findViewById(R.id.rate_action);
        mCancel = v.findViewById(R.id.rate_cancel);
//        mDislike = v.findViewById(R.id.rate_dislike);
        mRateAction.setOnClickListener(this);
        mCancel.setOnClickListener(this);
//        mDislike.setOnClickListener(this);
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

        v.setOnTouchListener(this);
        mDetector = new GestureDetectorCompat(VLCApplication.getAppContext(), this);

        score(mDefaultScore == 0 ? 5 : mDefaultScore);

//        setCancelable(false);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.d(TAG, "onStart");
//        mRoot.startAnimation(AnimationUtils.loadAnimation(VLCApplication.getAppContext(), R.anim.anim_enter_left));
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        mRoot.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        mRoot.setVisibility(View.GONE);
        super.onStop();
        LogUtil.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        sStarted = false;
    }

    private void score(int score) {
        mRateScore = score;
        oneIv.setImageResource(score >= 1 ? R.drawable.rate1_normal : R.drawable.rate1_grey);
        twoIv.setImageResource(score >= 2 ? R.drawable.rate2_normal : R.drawable.rate2_grey);
        threeIv.setImageResource(score >= 3 ? R.drawable.rate3_normal : R.drawable.rate3_grey);
        fourIv.setImageResource(score >= 4 ? R.drawable.rate4_normal : R.drawable.rate4_grey);
        fiveIv.setImageResource(score >= 5 ? R.drawable.rate5_normal : R.drawable.rate5_grey);
        mRateTips.setText(score == 5 ? VLCApplication.getAppResources().getString(R.string.rate_content_5) : VLCApplication.getAppResources().getString(R.string.rate_content_1_4));
        mRateAction.setText(score > 3 ? VLCApplication.getAppResources().getString(R.string.rate_us) : VLCApplication.getAppResources().getString(R.string.rate_feedback));
    }

    @Override
    public void onClick(View view) {
        long time = new Date().getTime();
        switch (view.getId()) {
            case R.id.rate_action:
                if (mRateScore >= 4) {
                    MyToast.makeText(VLCApplication.getAppContext(), "", Toast.LENGTH_LONG).show();
                    StatisticsManager.submitRate(VLCApplication.getAppContext(), StatisticsManager.ITEM_RATE_STAR);
                    ShareUtils.launchAppDetail(VLCApplication.getAppContext(), VLCApplication.getAppContext().getPackageName());
                } else {
                    StatisticsManager.submitRate(VLCApplication.getAppContext(), StatisticsManager.ITEM_RATE_DISLIKE);
                    ShareUtils.adviceEmail(VLCApplication.getAppContext());
                }
                time = Util.getDateNext(7);
                mNextTime = time;
                dismissRate(R.anim.anim_leave_right);
                break;
            case R.id.rate_cancel:
                StatisticsManager.submitRate(getActivity(), StatisticsManager.ITEM_RATE_CANCEL);

                LogUtil.d(TAG, "rate_cancel last time:" + time + "(" + Util.millisToDate(time) + ")");
                time = Util.getDateNext(3);
                LogUtil.d(TAG, "rate_cancel next time:" + time + "(" + Util.millisToDate(time) + ")");
                mNextTime = time;
                dismissRate(R.anim.anim_leave_right);
                break;
            case R.id.one:
                score(1);
                break;
            case R.id.two:
                score(2);
                break;
            case R.id.three:
                score(3);
                break;
            case R.id.four:
                score(4);
                break;
            case R.id.five:
                score(5);
                break;
        }
    }

    private boolean animStarted = false;
    private void dismissRate(int animId) {
        if (animStarted) {
            return;
        }
        animStarted = false;
        Animation animLeave = AnimationUtils.loadAnimation(VLCApplication.getAppContext(), animId);
        animLeave.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animStarted = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogUtil.d(TAG, "dismiss onAnimationEnd");
                animStarted = false;
                mRoot.setVisibility(View.GONE);
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRoot.startAnimation(animLeave);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        LogUtil.d(TAG, "onCancel");
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
        if (mNextTime < currentTime) {
//            if (lastTime == 0 || currentTime - lastTime >= CHECK_COUNT_PERIOD) {
//                lastTime = currentTime;
//                count = 0;
//                sSettings.edit().putLong(KEY_RATE_SHOW_LAST, lastTime).apply();
//            }
//            // 上次提示在一天内
//            if (currentTime - lastTime < CHECK_COUNT_PERIOD) {
//                // 最多5次
//                if (count < 5) {
//                    count++;
//                    sSettings.edit().putInt(KEY_RATE_SHOW_COUNT, count).apply();
//                } else {
//                    // 超过5次，下次提示在一天后
//                    mNextTime = lastTime + CHECK_COUNT_PERIOD;
//                    LogUtil.d(TAG, "onDismiss, reach 5 times nextTime:" + mNextTime + "(" + Util.millisToDate(mNextTime) + ")");
//                }
//            }

            // 等同于Cancel
            long time = new Date().getTime();
            LogUtil.d(TAG, "rate dismiss last time:" + time + "(" + Util.millisToDate(time) + ")");
            time = Util.getDateNext(3);
            LogUtil.d(TAG, "rate dismiss next time:" + time + "(" + Util.millisToDate(time) + ")");
            mNextTime = time;

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

        if (mOnRateDismiss != null) {
            mOnRateDismiss.onDismiss();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mDetector != null && mDetector.onTouchEvent(event)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        LogUtil.d(TAG, "onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        LogUtil.d(TAG, "onShowPress");

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        LogUtil.d(TAG, "onSingleTapUp");

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        LogUtil.d(TAG, "onScroll");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        LogUtil.d(TAG, "onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        LogUtil.d(TAG, "onFling e1[" + e1.getX() + "," + e1.getY() + "], e2[" + e2.getX() + "," + e2.getY() + "], velocityX=" + velocityX + ", velocityY=" + velocityY);
        if (e1.getX() - e2.getX() > MIN_FLING_DISTANCE_X && Math.abs(velocityX) > MIN_FLING_VELOCITY_X) {
            // fling left
            dismissRate(R.anim.anim_leave_left);
        } else if (e2.getX() - e1.getX() > MIN_FLING_DISTANCE_X && Math.abs(velocityX) > MIN_FLING_VELOCITY_X) {
            // fling right
            dismissRate(R.anim.anim_leave_right);
        }
        return false;
    }


}
