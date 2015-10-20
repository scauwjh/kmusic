package me.keiwu.kmusic.util;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import me.keiwu.kmusic.constant.Constants;

/**
 * Created by kei on 2015/10/20.
 */
public class FileManager {

    private ArrayList<File> fileList;
    private ArrayList<String> filePathList;

    public FileManager(String path, String[] suffixs) {
        fileList = new ArrayList();
        filePathList = new ArrayList();
        getFiles(path, suffixs);
    }

    public FileManager(String path) {
        fileList = new ArrayList();
        filePathList = new ArrayList();
        getFiles(path, null);
    }

    private void getFiles(String path, String[] suffixs) {
        File[] files = new File(path).listFiles();
        if (files == null)
            return;
        for (File file : files) {
            if (file.isFile() && checkSuffix(file.getName().toLowerCase(), suffixs)) {
                fileList.add(file);
                filePathList.add(file.getPath());
                continue;
            }
            getFiles(file.getPath(), suffixs);
        }
        return;
    }

    private Boolean checkSuffix(String fileName, String[] suffixs) {
        if (suffixs == null) return true;
        for (int i = 0; i < suffixs.length; i++) {
            if (fileName.endsWith(suffixs[i].toLowerCase()))
                return true;
        }
        return false;
    }

    /**
     * getter and setter
     */
    public ArrayList<File> getFileList() {
        return fileList;
    }

    public ArrayList<String> getFilePathList() {
        return filePathList;
    }
}
