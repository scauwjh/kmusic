package me.keiwu.kmusic;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import me.keiwu.kmusic.constant.Constants;
import me.keiwu.kmusic.core.directory.MusicManager;
import me.keiwu.kmusic.core.kMusic.KMusic;


/**
 * Created by kei on 2015/10/10.
 */
public class MainActivity extends Activity implements OnClickListener {


    private ImageButton btnPlay;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnFavourite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.LOG_TAG, "onCreate");
        setContentView(R.layout.activity_main);
        // Immersed status bar need SDK 4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setOnClickListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        userInit();
    }

    @Override
    public void onClick(View view) {
        Log.i(Constants.LOG_TAG, "onClick");
        Integer id = view.getId();
        switch (id) {
            case R.id.btn_play:
                actionPlay();
                break;
            case R.id.btn_previous:
                actionPrevious();
                break;
            case R.id.btn_next:
                actionNext();
                break;
            case R.id.btn_favourite:
                actionFavourite();
            default:
                break;
        }
    }


    private void userInit() {
        OnCompletionListener completionListener = new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                actionNext();
            }
        };
        KMusic kMusic = KMusic.getInstance();
        kMusic.setOnCompleteListener(completionListener);
        setPlaybackButtonDrawable(kMusic);
    }

    private void setOnClickListener() {
        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);

        btnPrevious = (ImageButton) findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(this);

        btnNext = (ImageButton) findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);

        btnFavourite = (ImageButton) findViewById(R.id.btn_favourite);
        btnFavourite.setOnClickListener(this);
    }

    private void actionPlay() {
        Log.i(Constants.LOG_TAG, "play action");
        ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
        if (musicList == null || musicList.size() <= 0)
            return;
        String initMusicPath = musicList.get(0);
        playback(initMusicPath, 0);
    }

    private void actionNext() {
        Log.i(Constants.LOG_TAG, "next action");
        KMusic kMusic = KMusic.getInstance();
        ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
        if (musicList == null || musicList.size() <= 0)
            return;
        Integer trackNum = kMusic.getTrackNum() + 1;
        if (trackNum > musicList.size())
            trackNum = 0;
        String musicPath = musicList.get(trackNum);
        initPlay(musicPath, trackNum);
    }

    private void actionPrevious() {
        Log.i(Constants.LOG_TAG, "previous action");
        KMusic kMusic = KMusic.getInstance();
        ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
        if (musicList == null || musicList.size() <= 0)
            return;
        Integer trackNum = kMusic.getTrackNum() - 1;
        if (trackNum < 0)
            trackNum = musicList.size();
        String musicPath = musicList.get(trackNum);
        initPlay(musicPath, trackNum);
    }

    private void actionFavourite() {
        Log.i(Constants.LOG_TAG, "favourite action");
        // just for test
        KMusic.getInstance().seekTo(240000);
    }


    private void setPlaybackButtonDrawable(KMusic kMusic) {
        Drawable drawable;
        if (kMusic.isReady() && kMusic.isPlaying()) {
            drawable = getResources().getDrawable(R.drawable.play_start);
            btnPlay.setBackground(drawable);
            return;
        }
        drawable = getResources().getDrawable(R.drawable.play_pause);
        btnPlay.setBackground(drawable);
    }

    private void playback(String initMusicPath, Integer initTrackNum) {
        KMusic kMusic = KMusic.getInstance();
        if (!kMusic.isReady()){
            initPlay(initMusicPath, initTrackNum);
            return;
        }
        kMusic.playback();
        setPlaybackButtonDrawable(kMusic);
    }

    private void initPlay(String musicPath, Integer initTrackNum) {
        String title = musicPath.substring(musicPath.lastIndexOf("/") + 1, musicPath.lastIndexOf("."));
        String desc = musicPath.substring(musicPath.lastIndexOf("/") + 1);
        setMusicInfo(title, desc);
        KMusic kMusic = KMusic.getInstance();
        kMusic.initPlay(musicPath, initTrackNum);
        setPlaybackButtonDrawable(kMusic);
    }

    private void setMusicInfo(String title, String desc) {
        TextView musicTitle = (TextView)findViewById(R.id.music_title);
        musicTitle.setText(title);
        TextView musicDesc = (TextView)findViewById(R.id.music_desc);
        musicDesc.setText(desc);
    }

}