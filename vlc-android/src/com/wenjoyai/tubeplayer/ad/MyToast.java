package com.wenjoyai.tubeplayer.ad;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
//        View v = LayoutInflater.from(context).inflate(R.layout.layout_ad_toast, null);
//        TextView textView = (TextView) v.findViewById(R.id.toast_text);
//        textView.setText(text);
//        mToast = new Toast(context);
//        mToast.setDuration(duration);
//        mToast.setView(v);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_ad_toast, null);
        ImageView imageView=(ImageView)view.findViewById(R.id.toast_image);
        imageView.setBackgroundResource(R.drawable.icon);
        TextView t = (TextView) view.findViewById(R.id.toast_text);
        t.setText(text);
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
    public void setGravity(int gravity, int xOffset, int yOffset) {
        if (toast != null) {
            toast.setGravity(gravity, xOffset, yOffset);
        }
    }
}