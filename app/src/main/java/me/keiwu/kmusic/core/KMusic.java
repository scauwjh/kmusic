package me.keiwu.kmusic.core;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.TrackInfo;
import android.util.Log;

import java.io.IOException;

import me.keiwu.kmusic.constant.Constants;



/**
 * Created by kei on 2015/10/18.
 */
public class KMusic {

    private MediaPlayer player;

    private Boolean isReady;

    public KMusic(OnCompletionListener completionListener,
                  OnErrorListener errorListener) {
        isReady = false;
        player = new MediaPlayer();
        player.setOnErrorListener(errorListener);
        player.setOnCompletionListener(completionListener);
    }

    public Boolean isReady() {
        return isReady && player !=null && player.getAudioSessionId() != 0;
    }

    public Boolean isPlaying() {
        if (!isReady())
            return false;
        return player.isPlaying();
    }

    public Integer getCurrentPosition() {
        if (!isReady())
            return null;
        return player.getCurrentPosition();
    }

    public Integer getDuration() {
        if(!isReady())
            return -1;
        return player.getDuration();
    }

    public TrackInfo[] getTrackInfo() {
        if (!isReady())
            return null;
        return player.getTrackInfo();
    }

    public Boolean seekTo(Integer msec) {
        if (!isReady())
            return false;
        player.seekTo(msec);
        return true;
    }

    public Boolean playback() {
        if (!isReady())
            return false;
        if (player.isPlaying()) {
            player.pause();
            return true;
        }
        player.start();
        return true;
    }

    public Boolean initPlay(String musicPath) {
        if (player == null)
            return false;
        return initPlay(musicPath, true);
    }

    public Boolean initPlay(String musicPath, Boolean autoStart) {
        if (player == null)
            return false;
        try {
            player.reset();
            player.setDataSource(musicPath);
            player.prepare();
            isReady = true;
            if (autoStart) {
                player.start();
            }
            return true;
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
            isReady = false;
            if(player != null)
                player.reset();
            return false;
        }
    }

    public void release() {
        if (player == null)
            return;
        player.release();
        player = null;
        isReady = false;
    }
}
