/*****************************************************************************
 * CoverMediaSwitcher.java
 *****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
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
 *****************************************************************************/

package com.wenjoyai.tubeplayer.gui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.wenjoyai.tubeplayer.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CoverMediaSwitcher extends AudioMediaSwitcher {

    private final int mPadding = getContext().getResources().getDimensionPixelSize(R.dimen.audio_player_cover_margin);
    private final int mBorderWidth = getContext().getResources().getDimensionPixelSize(R.dimen.audio_player_cover_border);
    private final int mBorderColor = getContext().getResources().getColor(R.color.black);
    private final Animation mRotate = AnimationUtils.loadAnimation(getContext(), R.anim.anim_rotate);

    public CoverMediaSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void addMediaView(LayoutInflater inflater, String title, String artist, Bitmap cover) {
        CircleImageView imageView = new CircleImageView(getContext());
        if (cover == null) {
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_no_cover);
        } else {
            imageView.setPadding(mPadding, mPadding, mPadding, mPadding);
            imageView.setBorderWidth(mBorderWidth);
            imageView.setBorderColor(mBorderColor);
        }
        imageView.setImageBitmap(cover);
        imageView.setAnimation(mRotate);
        addView(imageView);
    }
}
