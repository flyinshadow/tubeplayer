package com.wenjoyai.tubeplayer.gui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.ads.NativeAd;
import com.wenjoyai.tubeplayer.PlaybackService;
import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.gui.audio.PlaylistAdapter;
import com.wenjoyai.tubeplayer.gui.helpers.SwipeDragItemTouchHelperCallback;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by yuqilin on 2017/12/2.
 */

public class VideoPlaylistDialog extends DialogFragment
        implements PlaylistAdapter.IPlayer, View.OnClickListener {

    private static final String TAG = "VideoPlaylistDialog";

    public static final String KEY = "playlist_dialog";

    private RecyclerView mPlaylist;

    private PlaybackService mService;

    private PlaylistAdapter mPlaylistAdapter;

    private View mClose;
    private TextView mPlaylistTitle;
    private TextView mPlaylistOrderText;
    private ImageView mPlaylistOrderIcon;

//    private PlaylistAdapter.IPlayer mPlayer;

    public void setPlaybackService(PlaybackService service) {
        mService = service;
    }

//    public void setPlayer(PlaylistAdapter.IPlayer player) {
//        mPlayer = player;
//    }

    private List<NativeAd> mNativeAds = null;
    public void setNativeAds(List<NativeAd> nativeAds) {
        mNativeAds = nativeAds;
    }

    public void update() {
        if (mService != null && mPlaylistAdapter != null) {
            mPlaylistAdapter.update(mService.getMedias());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setStyle(DialogFragment.STYLE_NO_TITLE, );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        if (getDialog() != null && getDialog().getWindow() != null) {
//            getDialog().getWindow().setWindowAnimations(R.style.PlaylistPortraitAnimation);
//        }

        View v = inflater.inflate(R.layout.video_playlist, container, false);


        mPlaylist = (RecyclerView) v.findViewById(R.id.video_playlist_list);
        mClose = v.findViewById(R.id.video_playlist_close);
        mPlaylistTitle = (TextView) v.findViewById(R.id.video_playlist_title);
        mPlaylistOrderText = (TextView) v.findViewById(R.id.video_playlist_order_text);
        mPlaylistOrderIcon = (ImageView) v.findViewById(R.id.video_playlist_order_icon);

        mClose.setOnClickListener(this);
        mPlaylistOrderIcon.setOnClickListener(this);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mPlaylist.setLayoutManager(layoutManager);

        mPlaylistAdapter = new PlaylistAdapter(this, true);
        mPlaylistAdapter.setService(mService);
        mPlaylistAdapter.setNativeAds(mNativeAds);
        mPlaylist.setAdapter(mPlaylistAdapter);

        if (!TextUtils.isEmpty(mService.getPlaylistTitle()))
            mPlaylistTitle.setText(mService.getPlaylistTitle());

        // TODO: 2017/12/7 暂时禁用拖动手势，需考虑广告处理逻辑
//        ItemTouchHelper.Callback callback = new SwipeDragItemTouchHelperCallback(mPlaylistAdapter);
//        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
//        touchHelper.attachToRecyclerView(mPlaylist);
//
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        int orientation = getResources().getConfiguration().orientation;
        boolean portrait = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

        updateLayout(portrait);

        update();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean portrait = newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

        updateLayout(portrait);
    }

    private void updateLayout(boolean portrait) {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(portrait ? R.style.PlaylistPortraitAnimation : R.style.PlaylistLandscapeAnimation);
        window.setBackgroundDrawableResource(R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = portrait ? Gravity.BOTTOM : Gravity.RIGHT;
        lp.width = portrait ? WindowManager.LayoutParams.MATCH_PARENT : dm.widthPixels / 2;
        lp.height = portrait ? dm.heightPixels / 2 : WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    @Override
    public void onPopupMenu(View anchor, final int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.audio_player, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.audio_player_mini_remove) {
                    if (mService != null) {
                        mPlaylistAdapter.remove(position);
                        mService.remove(position);
                        return true;
                    }
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void updateList() {
        update();
    }

    @Override
    public void onSelectionSet(int position) {
        if (mPlaylist != null && mPlaylist.getLayoutManager() != null) {
            ((LinearLayoutManager) mPlaylist.getLayoutManager()).scrollToPositionWithOffset(position, 0);
        }
//        if (mPlaylistAdapter != null) {
//            mPlaylistAdapter.setCurrentIndex(position);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_playlist_close:
                dismiss();
                break;
            case R.id.video_playlist_order_icon:
                // 顺序播放 -> 随机播放 -> 单曲循环 -> 全部循环
                if (mService != null) {
                    if (mService.isShuffling()) {
                        mService.shuffle();
                        mService.setRepeatType(PlaybackService.REPEAT_ONE);
                        mPlaylistOrderText.setText(R.string.repeat_single);
                        mPlaylistOrderIcon.setImageResource(R.drawable.ic_playlist_repeatone);
                    } else {
                        int repeatType = mService.getRepeatType();
                        if (repeatType == PlaybackService.REPEAT_NONE) {
                            if (!mService.isShuffling()) {
                                mService.shuffle();
                                mService.setRepeatType(PlaybackService.REPEAT_ALL);
                            }
                            mPlaylistOrderText.setText(R.string.shuffle_on);
                            mPlaylistOrderIcon.setImageResource(R.drawable.ic_playlist_shuffle);
                        } else if (repeatType == PlaybackService.REPEAT_ONE) {
                            if (mService.isShuffling()) {
                                mService.shuffle();
                            }
                            mService.setRepeatType(PlaybackService.REPEAT_ALL);
                            mPlaylistOrderText.setText(R.string.repeat_all);
                            mPlaylistOrderIcon.setImageResource(R.drawable.ic_playlist_repeatall);
                        } else if (repeatType == PlaybackService.REPEAT_ALL) {
                            mService.setRepeatType(PlaybackService.REPEAT_NONE);
                            mPlaylistOrderText.setText("Order");
                            mPlaylistOrderIcon.setImageResource(R.drawable.ic_playlist_order);
                        }
                    }
                }
                break;
        }
    }
}
