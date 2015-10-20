package me.keiwu.kmusic.core.directory;

import java.io.File;
import java.util.ArrayList;

import me.keiwu.kmusic.util.FileManager;

/**
 * Created by kei on 2015/10/20.
 */
public class MusicManager {

    private static ArrayList<String> musicList;


    public ArrayList<String> getMusicList() {
        return musicList;
    }

    private MusicManager() {
//            change to SQL lite
        if (true) {
//            change to config
            String[] suffixs = {"mp3", "m4a", "flac", "ape", "wav"};
            FileManager fileMgr = new FileManager("/sdcard/Music/", suffixs);
            musicList = fileMgr.getFilePathList();
        }
    }

    private static class Singleton {
        private static final MusicManager INSTANCE = new MusicManager();
    }

    public static final MusicManager getInstance() {
        return Singleton.INSTANCE;
    }





}
