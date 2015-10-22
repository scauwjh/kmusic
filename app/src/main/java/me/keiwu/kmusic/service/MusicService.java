package me.keiwu.kmusic.service;

import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import me.keiwu.kmusic.R;
import me.keiwu.kmusic.activity.MainActivity;
import me.keiwu.kmusic.constant.Constants;
import me.keiwu.kmusic.core.KMusic;

/**
 * Created by kei on 2015/10/21.
 */
public class MusicService extends Service {

    private KMusic mKMusic;
    private NotificationManager mNM;


    public MusicService() {
        initMusicPlayer();
    }


    public class MusicBinder extends Binder {

        public MusicService getService() {
            return MusicService.this;
        }

    }

    public MusicBinder musicBinder;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Constants.LOG_TAG, "music service on create!");
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Builder builder = new Builder(this);
        Intent ni = new Intent(this, MainActivity.class);
        // Context context, int requestCode, Intent intent, int flags
        PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);
        builder.setContentIntent(pi);
        builder.setSmallIcon(R.drawable.play_start);
        builder.setTicker("Foreground Service Start");
        builder.setContentTitle("Foreground Service");
        builder.setContentText("Make this service run in the foreground");
        Notification notification = builder.build();
        startForeground(1, notification);
        musicBinder = new MusicBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
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
        mKMusic = new KMusic(completionListener, errorListener);
    }
}
