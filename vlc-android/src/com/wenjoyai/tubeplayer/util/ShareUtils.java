package com.wenjoyai.tubeplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;

import org.videolan.medialibrary.media.MediaWrapper;

import java.io.File;
import java.util.ArrayList;

import static org.videolan.medialibrary.media.MediaWrapper.TYPE_AUDIO;

/**
 * Created by LiJiaZhi on 16/12/18. share
 */

public class ShareUtils {
    // 分享文字--全局
    public static void shareAppText(Activity act) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                VLCApplication.getAppResources().getString(R.string.share_app_text));
        shareIntent.setType("text/plain");

        // 设置分享列表的标题，并且每次都显示分享列表
        act.startActivity(Intent.createChooser(shareIntent, "Share To"));
    }
    //home
    public static void shareHomeText(Activity act) {
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.putExtra(Intent.EXTRA_TEXT,
//                String.format("I have maked %1$d ringtones with  mp3 cutter and ringtone maker APP,come and enjoy it ! https://play.google.com/store/apps/details?id=com.av.ringtone", UserDatas.getInstance().getCutCount()));
//        shareIntent.setType("text/plain");
//
//        // 设置分享列表的标题，并且每次都显示分享列表
//        act.startActivity(Intent.createChooser(shareIntent, "Share To"));
    }


    public static void shareMultipleImage(Activity act) {
        ArrayList<Uri> uriList = new ArrayList<>();

        String path = Environment.getExternalStorageDirectory() + File.separator;
        uriList.add(Uri.fromFile(new File(path + "australia_1.jpg")));
        uriList.add(Uri.fromFile(new File(path + "australia_2.jpg")));
        uriList.add(Uri.fromFile(new File(path + "australia_3.jpg")));

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        shareIntent.setType("image/*");
        act.startActivity(Intent.createChooser(shareIntent, "Share To"));
    }

    public static void shareMedia(Context context, MediaWrapper media) {
        // intent.setType("text/plain"); //纯文本
        /*
         * 图片分享 it.setType("image/png"); //添加图片 File f = new
         * File(Environment.getExternalStorageDirectory()+"/name.png");
         *
         * Uri uri = Uri.fromFile(f); intent.putExtra(Intent.EXTRA_STREAM, uri);
         */
        if (media == null || media.getUri() == null) {
            return;
        }
        String mimeType = "*/*";
//        if (media.getType() == TYPE_AUDIO) {
//            mimeType = "audio/*";
//        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        // intent.setType("image/*");
        // intent.setType("audio/*");//此处可发送多种文件
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent.putExtra(Intent.EXTRA_STREAM, media.getUri());
        intent.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my Video Player app");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, "Share To"));
    }

    public static void adviceEmail(Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto: wenjoyai@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, VLCApplication.getAppContext().getString(R.string.app_name));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void launchAppDetail(Context context, String appPkg) {
        if (TextUtils.isEmpty(appPkg))
            return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + appPkg));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
