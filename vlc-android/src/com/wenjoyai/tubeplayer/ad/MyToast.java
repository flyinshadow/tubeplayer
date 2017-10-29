package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.wenjoyai.tubeplayer.R;

/**
 * @author：LiJiaZhi on 2017/10/28
 * @des：ToDo
 * @org mtime.com
 */
public class MyToast {
    private Toast toast;
    private MyToast(Context context, CharSequence text, int duration) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_ad_toast, null);
        ImageView handIv = (ImageView)view.findViewById(R.id.hand_iv);
        handIv.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_bottom_in));
        if (toast != null) {
            toast.cancel();
        }
        toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);

    }

    public static MyToast makeText(Context context, CharSequence text, int duration) {
        return new MyToast(context, text, duration);
    }
    public void show() {
        if (toast != null) {
            toast.show();
        }
    }
}