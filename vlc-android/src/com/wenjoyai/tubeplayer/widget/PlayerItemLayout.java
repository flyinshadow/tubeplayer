package com.wenjoyai.tubeplayer.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.gui.video.VideoPlayerActivity;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author：LiJiaZhi on 2017/12/4
 * @des：ToDo
 * @org mtime.com
 */
public class PlayerItemLayout extends FrameLayout {

    private FloatingActionButton mButton;
    private TextView mTitileTv;
    private TextView mContentTv;
    private LinearLayout mRootll;
    private FrameLayout mContentFrameLayout;

    private VideoPlayerActivity.PlayerMenuModel mModel;

    public PlayerItemLayout(Context context) {
        super(context);
        init();
    }

    public PlayerItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.player_menu_item, this);
        mButton = (FloatingActionButton) itemView.findViewById(R.id.iv);
        mTitileTv = (TextView) itemView.findViewById(R.id.title_tv);
        mContentTv = (TextView) itemView.findViewById(R.id.content_tv);
        mContentFrameLayout = (FrameLayout) itemView.findViewById(R.id.content_fl);
        mRootll = (LinearLayout) findViewById(R.id.root);
    }

    public void init(VideoPlayerActivity.PlayerMenuModel model) {
        mModel = model;
        reset();
    }
    public void reset(){
        if (null == mModel){
            return;
        }
        if (!TextUtils.isEmpty(mModel.name)) {
            mTitileTv.setText(mModel.name);
        }
        mButton.setImageResource(mModel.drawableId);
        mButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_player_item)));
        mContentTv.setVisibility(GONE);
        mButton.setVisibility(VISIBLE);
    }

    public void setSelected(boolean isText) {//是否显示文字
        mButton.setBackgroundTintList(ColorStateList.valueOf(getColorPrimary()));
        if (isText) {
            mContentTv.setVisibility(VISIBLE);
            mButton.setVisibility(GONE);
        } else {
            mContentTv.setVisibility(GONE);
            mButton.setVisibility(VISIBLE);
        }
    }

    public TextView getContentTv(){
        return mContentTv;
    }

    /**
     * 获取主题颜色
     * @return
     */
    private int getColorPrimary(){
        TypedValue typedValue = new  TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
}
