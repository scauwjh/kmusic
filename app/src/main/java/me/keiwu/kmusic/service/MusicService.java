package me.keiwu.kmusic.service;

import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Intent;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import me.keiwu.kmusic.R;
import me.keiwu.kmusic.activity.MainActivity;
import me.keiwu.kmusic.constant.Constants;
import me.keiwu.kmusic.core.KMusic;
import me.keiwu.kmusic.core.MusicManager;

/**
 * Created by kei on 2015/10/21.
 */
public class MusicService extends Service {

    public  static Boolean isRunning;

    private KMusic mKMusic;
    private Integer mOrder;
    private Integer mIndex;
    private String mTitle;
    private String mDescription;

    private OnCompletionListener mCompletionListener;
    private OnErrorListener mErrorListener;
    private Intent mProgressIntent;

    private final MusicBinder musicBinder = new MusicBinder();

    private class ProgressThread implements Runnable {
        public ProgressThread() {
            mProgressIntent = new Intent(MainActivity.class.getName());
        }
        @Override
        public void run() {
            while (isRunning) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                    if (!isPlaying())
                        continue;
                    mProgressIntent.putExtra(Constants.INTENT_EXTRA_DURATION, getDuration());
                    mProgressIntent.putExtra(Constants.INTENT_EXTRA_PROGRESS, getCurrentPosition());
                    sendBroadcast(mProgressIntent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.i(Constants.LOG_TAG, "InterruptedException: " + e.getMessage());
                }
            }
        }
    }


    public MusicService() {
        isRunning = true;
        mIndex = 0;
        mOrder = Constants.ORDER_REPEAT_ALL;
        mTitle = Constants.DEFAULT_MUSIC_TITL;
        mDescription = Constants.DEFAULT_MUSIC_DESC;
    }

    public class MusicBinder extends Binder {

        public MusicService getService(OnCompletionListener completionListener,
            OnErrorListener errorListener) {
            initPlayer(completionListener, errorListener);
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Constants.LOG_TAG, "onCreate start");
        ProgressThread progress = new ProgressThread();
        new Thread(progress).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        isRunning = false;
    }


    public void initPlayer(OnCompletionListener completionListener,
            OnErrorListener errorListener) {
        if (mKMusic == null) {
            Log.i(Constants.LOG_TAG, "mKMusic is null, create a new object");
            mKMusic = new KMusic(completionListener, errorListener);
        }
    }



    public Integer playback() {
        if (!mKMusic.isReady()) {
            ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
            if (musicList == null || musicList.size() <= 0)
                return Constants.PLAY_RET_ERR;
            Integer index = getInitIndex(musicList.size() - 1);
            String musicPath = musicList.get(index);
            if (!initPlay(musicPath))
                return Constants.PLAY_RET_ERR;
            return Constants.PLAY_RET_INIT;
        }
        if (!mKMusic.playback())
            return Constants.PLAY_RET_ERR;
        return mKMusic.isPlaying() ? Constants.PLAY_RET_PLAY : Constants.PLAY_RET_PAUSE;
    }


    public Boolean playNext() {
        return playNext(false);
    }
    public Boolean playNext(Boolean userSelect) {
        ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
        if (musicList == null || musicList.size() <= 0)
            return false;
        Integer index = mIndex;
        if (!Constants.ORDER_REPEAT_ONE.equals(mOrder) || userSelect) {
            index = getNextIndex(musicList.size() - 1);
        }
        String musicPath = musicList.get(index);
        return initPlay(musicPath);
    }

    public Boolean playPrevious() {
        return playPrevious(false);
    }
    public Boolean playPrevious(Boolean userSelect) {
        ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
        if (musicList == null || musicList.size() <= 0)
            return false;
        Integer index = mIndex;
        if (!Constants.ORDER_REPEAT_ONE.equals(mOrder) || userSelect) {
            index = getPrevIndex(musicList.size() - 1);
        }
        String musicPath = musicList.get(index);
        return initPlay(musicPath);
    }

    public Integer changeOrder() {
        mOrder = (mOrder + 1) % Constants.ORDER_MOD;
        Log.i(Constants.LOG_TAG, "order=" + mOrder);
        return mOrder;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Integer getDuration() {
        return mKMusic.getDuration();
    }

    public Integer getCurrentPosition() {
        return mKMusic.getCurrentPosition();
    }

    public Boolean isPlaying() {
        return mKMusic.isReady() && mKMusic.isPlaying();
    }

    public void release() {
        mKMusic.release();
        mKMusic = null;
    }

    public void initNotification() {
        Builder builder = new Builder(this);
        Intent intent = new Intent(this, MainActivity.class);
        // Context context, int requestCode, Intent intent, int flags
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.play_start);
        builder.setAutoCancel(true);
        // builder.setTicker("hello world");
        builder.setContentTitle(mTitle);
        builder.setContentText(mDescription);
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    public void removeNotification() {

    }




    /************************************************************************
     * Private methods
     ***********************************************************************/
    private Boolean initPlay(String musicPath) {
        if (!mKMusic.initPlay(musicPath))
            return false;
        mTitle = musicPath.substring(musicPath.lastIndexOf("/") + 1,
            musicPath.lastIndexOf("."));
        mDescription = musicPath.substring(musicPath.lastIndexOf("/") + 1);
        return true;
    }

    private Integer getInitIndex(Integer maxIndex) {
        if (Constants.ORDER_RANDOM.equals(mOrder)) {
            mIndex = (int) (Math.random() * maxIndex);
        } else {
            mIndex = 0;
        }
        return mIndex;
    }

    private Integer getNextIndex(Integer maxIndex) {
        if (Constants.ORDER_RANDOM.equals(mOrder)) {
            mIndex = (int) (Math.random() * maxIndex);
        } else if (mIndex < maxIndex){
            mIndex ++;
        } else {
            mIndex = 0;
        }
        return mIndex;
    }

    private Integer getPrevIndex(Integer maxIndex) {
        if (Constants.ORDER_RANDOM.equals(mOrder)) {
            mIndex = (int) (Math.random() * maxIndex);
        } else if (mIndex <= 0) {
            mIndex = maxIndex;
        } else {
            mIndex --;
        }
        return mIndex;
    }

}



// String title = musicPath.substring(musicPath.lastIndexOf("/") + 1, musicPath.lastIndexOf("."));
//         String desc = musicPath.substring(musicPath.lastIndexOf("/") + 1);
//         reSetMusicInfo(title, desc);
//         mKMusic.initPlay(musicPath);
//         setPlaybackButtonDrawable();