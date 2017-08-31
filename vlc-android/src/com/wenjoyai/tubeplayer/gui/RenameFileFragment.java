package com.wenjoyai.tubeplayer.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.gui.helpers.UiTools;

import org.videolan.medialibrary.media.MediaWrapper;

/**
 * Created by yuqilin on 2017/8/22.
 */

public class RenameFileFragment extends DialogFragment implements View.OnClickListener {

    private TextView mCancel;
    private TextView mSave;
    private TextInputLayout mEditText;

    private MediaWrapper mMedia;

    private String mSavedFileName;

    private DialogInterface.OnDismissListener mOnDismissListener;

    public RenameFileFragment() {
    }

    public void setMedia(MediaWrapper media) {
        mMedia = media;
    }

    public String getSavedName() {
        String savedName = null;
        if (mEditText != null && mEditText.getEditText() != null && !TextUtils.isEmpty(mEditText.getEditText().getText())) {
            savedName = mEditText.getEditText().getText().toString().trim();
        }
        return mSavedFileName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setCancelable(false);
        int theme = getTheme();
        setStyle(DialogFragment.STYLE_NO_FRAME, theme);

        View v = inflater.inflate(R.layout.rename_file_dialog, container, false);

        mEditText = (TextInputLayout)v.findViewById(R.id.rename_file_edit);
        mCancel = (TextView)v.findViewById(R.id.rename_file_cancel);
        mSave = (TextView)v.findViewById(R.id.rename_file_save);

        mCancel.setOnClickListener(this);
        mSave.setOnClickListener(this);

        if (mMedia != null && mEditText.getEditText() != null && !TextUtils.isEmpty(mMedia.getFileName())) {
            mEditText.getEditText().setText(mMedia.getFileName());
            mEditText.getEditText().setSelection(0, mMedia.getFileName().lastIndexOf('.'));
        }

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.rename_file_cancel:
                dismiss();
                break;
            case R.id.rename_file_save:
                if (mEditText != null && mEditText.getEditText() != null && mEditText.getEditText().getText() != null &&
                        !TextUtils.isEmpty(mEditText.getEditText().getText().toString())) {
                    mSavedFileName = mEditText.getEditText().getText().toString().trim();
                }
                dismiss();
                break;
        }
    }


    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }
}
