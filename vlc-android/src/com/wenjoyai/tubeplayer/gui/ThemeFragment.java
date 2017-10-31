package com.wenjoyai.tubeplayer.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.wenjoyai.tubeplayer.R;
import com.wenjoyai.tubeplayer.VLCApplication;
import com.wenjoyai.tubeplayer.firebase.StatisticsManager;
import com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity;

import static com.wenjoyai.tubeplayer.gui.preferences.PreferencesActivity.RESULT_RESTART;

/**
 * Created by yuqilin on 2017/8/15.
 */

public class ThemeFragment extends DialogFragment {

    private static SharedPreferences sSettings = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());

    private GridView mGridView;
    private ThemeAdapter mThemeAdapter;
    public static final int DEFAULT_THEME_INDEX = 6;

    public static final int[] sThemeStyles = {
            R.style.Theme_VideoPlayer_Apearance_Black,
            R.style.Theme_VideoPlayer_Apearance_Red,
            R.style.Theme_VideoPlayer_Apearance_Pink,
            R.style.Theme_VideoPlayer_Apearance_Purple,
            R.style.Theme_VideoPlayer_Apearance_DeepPurple,
            R.style.Theme_VideoPlayer_Apearance_Indigo,
            R.style.Theme_VideoPlayer_Apearance_Blue,
            R.style.Theme_VideoPlayer_Apearance_LightBlue,
            R.style.Theme_VideoPlayer_Apearance_Cyan,
            R.style.Theme_VideoPlayer_Apearance_Teal,
            R.style.Theme_VideoPlayer_Apearance_Green,
            R.style.Theme_VideoPlayer_Apearance_LightGreen,
            R.style.Theme_VideoPlayer_Apearance_Lime,
            R.style.Theme_VideoPlayer_Apearance_Amber,
            R.style.Theme_VideoPlayer_Apearance_Orange,
            R.style.Theme_VideoPlayer_Apearance_DeepOrange,
    };

    public static final int[] sThemeNightStyles = {
            R.style.Theme_VideoPlayer_Apearance_Black_Night,
            R.style.Theme_VideoPlayer_Apearance_Red_Night,
            R.style.Theme_VideoPlayer_Apearance_Pink_Night,
            R.style.Theme_VideoPlayer_Apearance_Purple_Night,
            R.style.Theme_VideoPlayer_Apearance_DeepPurple_Night,
            R.style.Theme_VideoPlayer_Apearance_Indigo_Night,
            R.style.Theme_VideoPlayer_Apearance_Blue_Night,
            R.style.Theme_VideoPlayer_Apearance_LightBlue_Night,
            R.style.Theme_VideoPlayer_Apearance_Cyan_Night,
            R.style.Theme_VideoPlayer_Apearance_Teal_Night,
            R.style.Theme_VideoPlayer_Apearance_Green_Night,
            R.style.Theme_VideoPlayer_Apearance_LightGreen_Night,
            R.style.Theme_VideoPlayer_Apearance_Lime_Night,
            R.style.Theme_VideoPlayer_Apearance_Amber_Night,
            R.style.Theme_VideoPlayer_Apearance_Orange_Night,
            R.style.Theme_VideoPlayer_Apearance_DeepOrange_Night,
    };

    public static final int[] sThemeActionBarStyles = {
            R.style.Theme_VideoPlayer_Apearance_Black_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Red_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Pink_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Purple_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_DeepPurple_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Indigo_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Blue_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_LightBlue_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Cyan_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Teal_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Green_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_LightGreen_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Lime_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Amber_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_Orange_ActionBar,
            R.style.Theme_VideoPlayer_Apearance_DeepOrange_ActionBar,
    };

    private final int[] mThemeColors = {
            R.color.theme_color_black, R.color.theme_color_red, R.color.theme_color_pink, R.color.theme_color_purple,
            R.color.theme_color_deep_purple, R.color.theme_color_indigo, R.color.theme_color_blue, R.color.theme_color_light_blue,
            R.color.theme_color_cyan, R.color.theme_color_teal, R.color.theme_color_green, R.color.theme_color_light_green,
            R.color.theme_color_lime, R.color.theme_color_amber, R.color.theme_color_orange, R.color.theme_color_deep_orange
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.theme_selector, container, false);
        mGridView = (GridView)v.findViewById(R.id.theme_selector_grid);
        mThemeAdapter = new ThemeAdapter(getActivity());
        mGridView.setAdapter(mThemeAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("theme_selector", "onItemClick i = " + i + ", l = " + l);

//                getActivity().setTheme(mThemeStyles[i]);
//                getActivity().getTheme().applyStyle(mThemeStyles[i], true);

                StatisticsManager.submitTheme(getActivity(), "theme_" + i);

                if (sSettings.getInt(PreferencesActivity.KEY_CURRENT_THEME_INDEX, DEFAULT_THEME_INDEX) == i) {
                    return;
                }

                sSettings.edit().putInt(PreferencesActivity.KEY_CURRENT_THEME_INDEX, i).apply();

                mThemeAdapter.notifyDataSetChanged();

//                sHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        getActivity().setResult(RESULT_RESTART);
//                        Intent intent = getActivity().getIntent();
//
//                        if (intent != null) {
//                            getActivity().finish();
////                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                        }
////                        finish();
//                    }
//                }, 200);

                getActivity().setResult(RESULT_RESTART);

//                if (Build.VERSION.SDK_INT >= 11) {
//                    getActivity().recreate();
//                } else {
                    getActivity().finish();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
//                }
            }
        });
        return v;
    }

//    private void finish() {
//        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
//    }

    private class ThemeAdapter extends BaseAdapter {
        private Context mContext;
        public ThemeAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mThemeColors.length;
        }

        @Override
        public Object getItem(int i) {
            return mThemeColors[i];
        }

        @Override
        public long getItemId(int i) {
            return mThemeColors[i];
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.theme_selector_item, null);
            }
            ImageView theme = (ImageView) view.findViewById(R.id.theme_selector_image);
            ImageView indicator = (ImageView) view.findViewById(R.id.theme_selector_indicator);
            theme.setBackgroundColor(getResources().getColor(mThemeColors[i]));
            indicator.setVisibility(sSettings.getInt(PreferencesActivity.KEY_CURRENT_THEME_INDEX, DEFAULT_THEME_INDEX) == i ? View.VISIBLE : View.GONE);
            return view;
        }
    }
}
