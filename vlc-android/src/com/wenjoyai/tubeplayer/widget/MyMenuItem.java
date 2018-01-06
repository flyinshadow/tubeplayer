package com.wenjoyai.tubeplayer.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wenjoyai.tubeplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mtime-PC on 2017/12/25.
 */

public class MyMenuItem {

    private int id;
    private int name;
    private int imageId;
    private boolean initValid = true;
    private boolean valid = true;

    public MyMenuItem(int id, int name, int imageId) {
        this.id = id;
        this.name = name;
        this.imageId = imageId;
    }

    public MyMenuItem(int id, int name, int imageId, boolean valid) {
        this(id, name, imageId);
        this.initValid = this.valid = valid;
    }

    public int getId() {
        return id;
    }

    public int getName() {
        return name;
    }

    int getImageId() {
        return imageId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private void reset() {
        this.valid = this.initValid;
    }

    public static MyMenuItem findItem(MyMenuItem[] myMenus, int id) {
        for (MyMenuItem myMenu : myMenus) {
            if (myMenu.getId() == id) {
                return myMenu;
            }
        }
        return null;
    }

    public static MyMenuItem[] makeValidMenu(MyMenuItem[] myMenu) {
        List<MyMenuItem> myMenuList = new ArrayList<>();
        for (MyMenuItem myMenuItem : myMenu) {
            if (myMenuItem.isValid()) {
                myMenuList.add(myMenuItem);
            }
        }
        return myMenuList.toArray(new MyMenuItem[myMenuList.size()]);
    }

    public static void resetMenu(MyMenuItem[] myMenu) {
        for (MyMenuItem myMenuItem : myMenu) {
            myMenuItem.reset();
        }
    }

    public static class MyMenuAdapter extends ArrayAdapter<MyMenuItem> {

        private int resId;

        public MyMenuAdapter(Context context, int resource, List<MyMenuItem> objects) {
            super(context, resource, objects);
            resId = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MyMenuItem item = getItem(position);
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resId, parent, false);
            } else {
                view = convertView;
            }
            ImageView itemImage = (ImageView) view.findViewById(R.id.image);
            TextView itemName = (TextView) view.findViewById(R.id.name);
            itemImage.setImageResource(item.getImageId());
            itemName.setText(item.getName());
            return view;
        }
    }

    public static class MenuAudioListBrowser {
        public static final int ID_AUDIO_PLAY = 1;
        public static final int ID_AUDIO_APPEND = 2;
        public static final int ID_AUDIO_INSERT_NEXT = 3;
        public static final int ID_AUDIO_PLAY_ALL = 4; //songs_view_only
        public static final int ID_AUDIO_INFO = 5; //songs_view_only
        public static final int ID_AUDIO_ADD_PLAYLIST = 6;
        public static final int ID_AUDIO_DELETE = 7;
        public static final int ID_AUDIO_SET_SONG = 8; //phone_only
        public static final int ID_AUDIO_SHARE = 9;
        public static final MyMenuItem[] menuAudio = {
                new MyMenuItem(ID_AUDIO_PLAY, R.string.play, R.drawable.ic_listmenu_play, false),
                new MyMenuItem(ID_AUDIO_PLAY_ALL, R.string.play_all, R.drawable.ic_listmenu_play),
                new MyMenuItem(ID_AUDIO_INFO, R.string.info, R.drawable.ic_listmenu_info),
                new MyMenuItem(ID_AUDIO_SHARE, R.string.share, R.drawable.ic_listmenu_share),
                new MyMenuItem(ID_AUDIO_SET_SONG, R.string.set_song, R.drawable.ic_set_song),
                new MyMenuItem(ID_AUDIO_INSERT_NEXT, R.string.insert_next, R.drawable.ic_insert_next),
                new MyMenuItem(ID_AUDIO_APPEND, R.string.append, R.drawable.ic_listmenu_append),
                new MyMenuItem(ID_AUDIO_ADD_PLAYLIST, R.string.add_to_playlist, R.drawable.ic_add_to_playlist),
                new MyMenuItem(ID_AUDIO_DELETE, R.string.delete, R.drawable.ic_listmenu_delete),
        };
    }
}
