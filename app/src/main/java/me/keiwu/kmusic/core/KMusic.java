package me.keiwu.kmusic.core;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import java.io.IOException;

import me.keiwu.kmusic.constant.Constants;

import static android.media.MediaPlayer.*;


/**
 * Created by kei on 2015/10/18.
 */
public class KMusic {

    private MediaPlayer player;

    private OnCompletionListener completionListener;

    private OnErrorListener errorListener;

    private Boolean isReady;

    public KMusic(OnCompletionListener completionListener,
                  OnErrorListener errorListener) {
        this.isReady = false;
        this.player = new MediaPlayer();
        player.setOnErrorListener(errorListener);
        player.setOnCompletionListener(completionListener);
    }



    public Boolean isReady() {
        return this.isReady && player.getAudioSessionId() != 0;
    }

    public Boolean isPlaying() {
        if (!this.isReady)
            return false;
        return player.isPlaying();
    }



    public Integer getCurrentPosition() {
        if (!this.isReady)
            return null;
        return player.getCurrentPosition();
    }

    public Integer getDuration() {
        if(!this.isReady)
            return -1;
        return player.getDuration();
    }

    public Boolean seekTo(Integer msec) {
        if (!this.isReady)
            return false;
        player.seekTo(msec);
        return true;
    }

    public TrackInfo[] getTrackInfo() {
        if (!this.isReady)
            return null;
        return player.getTrackInfo();
    }

    public Boolean playback() {
        if (!this.isReady)
            return false;
        if (player.isPlaying()) {
            player.pause();
            return true;
        }
        player.start();
        return true;
    }

    public Boolean initPlay(String musicPath, Integer musicId) {
        return initPlay(musicPath, musicId, true);
    }

    public Boolean initPlay(String musicPath, Integer musicId, Boolean autoStart) {
        try {
            player.reset();
            player.setDataSource(musicPath);
            player.prepare();
            this.isReady = true;
            if (autoStart) {
                player.start();
            }
            return true;
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
            this.isReady = true;
            if(player != null)
                player.reset();
            return false;
        }
    }
}
