package me.keiwu.kmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import android.support.annotation.Nullable;

import me.keiwu.kmusic.core.KMusic;

/**
 * Created by kei on 2015/10/21.
 */
public class MusicService extends Service {

    private KMusic kMusic;





    public MusicService() {
        initMusicPlayer();
    }




    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void initMusicPlayer() {
        OnCompletionListener completionListener = new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        };
        OnErrorListener errorListener = new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        };
        kMusic = new KMusic(completionListener, errorListener);
    }
}
