package me.keiwu.kmusic.core.kMusic;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import java.io.IOException;

import me.keiwu.kmusic.constant.Constants;


/**
 * Created by kei on 2015/10/18.
 */
public class KMusic {

    private MediaPlayer player;

    private OnCompletionListener onCompleteListener;

    private Boolean isReady;

    private Integer trackNum;

    private KMusic() {
        isReady = false;
        player = new MediaPlayer();
        setListener();
    }

    private static class Singleton {
        private static final KMusic INSTANCE = new KMusic();
    }

    public static final KMusic getInstance() {
        return Singleton.INSTANCE;
    }

    public Boolean isReady() {
        return isReady && player.getAudioSessionId() != 0;
    }

    public Boolean isPlaying() {
        return player.isPlaying();
    }

    public void seekTo(Integer msec) {
        player.seekTo(msec);
    }

    public MediaPlayer.TrackInfo[] getTrackInfo() {
        return player.getTrackInfo();
    }

    public void release() {
        player.release();
        player = null;
        isReady = false;
        player = new MediaPlayer();
    }

    public void playback() {
        if (player.isPlaying()) {
            player.pause();
            return;
        }
        player.start();
    }

    public void initPlay(String musicPath, Integer trackNum) {
        initPlay(musicPath, trackNum, true);
    }

    public void initPlay(String musicPath, Integer trackNum, Boolean autoStart) {
        try {
            player.reset();
            player.setDataSource(musicPath);
            player.prepare();
            isReady = true;
            player.setOnCompletionListener(onCompleteListener);
            this.trackNum = trackNum;
            if (autoStart) {
                player.start();
            }
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "IOException: " + e.getMessage());
            player.reset();
            e.printStackTrace();
        }
    }


    private void setListener() {
        onCompleteListener = new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(Constants.LOG_TAG, "onCompletion");
                mp.reset();
            }
        };
    }

    /**
     * getter and setter
     */
    public void setOnCompleteListener(OnCompletionListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public Integer getTrackNum() {
        return trackNum;
    }
    public void setTrackNum(Integer trackNum) {
        this.trackNum = trackNum;
    }
}
