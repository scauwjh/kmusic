package me.keiwu.kmusic.activity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import me.keiwu.kmusic.R;
import me.keiwu.kmusic.constant.Constants;
import me.keiwu.kmusic.core.MusicManager;
import me.keiwu.kmusic.core.KMusic;
import me.keiwu.kmusic.service.MusicService;


/**
 * Created by kei on 2015/10/10.
 */
public class MainActivity extends Activity implements OnClickListener {


    private ImageButton btnPlay;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnFavourite;
    private ImageView btnOrder;


    private Integer order;
    private Integer musicId;
    private Integer maxMusicId;

    private Intent intent;


    private KMusic kMusic;

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
        intent = new Intent(this, MusicService.class);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
//        bindService(intent)
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
                break;
            case R.id.btn_order:
                actionOrder();
                break;
            default:
                break;
        }
    }


    private void userInit() {
        this.order = Constants.ORDER_INIT;
        OnCompletionListener completionListener = new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(Constants.LOG_TAG, "onCompletion");
                doOnCompletion();
            }
        };
        OnErrorListener errorListener = new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i(Constants.LOG_TAG, "onError: what=" + what + " extra=" + extra);
                return false;
            }
        };
        kMusic = new KMusic(completionListener, errorListener);
        setPlaybackButtonDrawable();
        initMusicInfo();
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

        btnOrder = (ImageView) findViewById(R.id.btn_order);
        btnOrder.setOnClickListener(this);
    }

    private void actionPlay() {
        Log.i(Constants.LOG_TAG, "play action");
        playback();
    }

    private void actionNext() {
        Log.i(Constants.LOG_TAG, "next action");
        ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
        if (musicList == null || musicList.size() <= 0)
            return;
        this.maxMusicId = musicList.size() - 1;
        Integer musicId = getNextMusicId(this.maxMusicId);
        String musicPath = musicList.get(musicId);
        initPlay(musicPath, musicId);
    }

    private void actionPrevious() {
        Log.i(Constants.LOG_TAG, "previous action");
        ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
        if (musicList == null || musicList.size() <= 0)
            return;
        this.maxMusicId = musicList.size() - 1;
        Integer musicId = getPrevMusicId(this.maxMusicId);
        String musicPath = musicList.get(musicId);
        initPlay(musicPath, musicId);
    }

    private void actionFavourite() {
        Log.i(Constants.LOG_TAG, "favourite action");
        // just for test
        kMusic.seekTo(240000);
    }

    private void actionOrder() {
        this.order ++;
        if (Constants.ORDER_REPEAT_ALL.equals(this.order)) {
            btnOrder.setImageResource(R.drawable.bottom_btn_repeat_all);
        } else if (Constants.ORDER_RANDOM.equals(this.order)) {
            btnOrder.setImageResource(R.drawable.bottom_btn_random);
        } else if (Constants.ORDER_REPEAT_ONE.equals(this.order)) {
            btnOrder.setImageResource(R.drawable.bottom_btn_repeat_one);
        } else {
            this.order = Constants.ORDER_INIT;
            btnOrder.setImageResource(R.drawable.bottom_btn_repeat_all);
        }
        Log.i(Constants.LOG_TAG, "order action" + this.order);
    }


    private void doOnCompletion() {
        if (Constants.ORDER_REPEAT_ONE.equals(this.order)) {
            this.musicId = this.musicId -- >= 0 ? this.musicId : 0;
        }
        actionNext();
    }


    private void setPlaybackButtonDrawable() {
        Drawable drawable;
        if (kMusic.isReady() && kMusic.isPlaying()) {
            drawable = getResources().getDrawable(R.drawable.play_start);
            btnPlay.setBackground(drawable);
            return;
        }
        drawable = getResources().getDrawable(R.drawable.play_pause);
        btnPlay.setBackground(drawable);
    }

    private void playback() {
        if (!kMusic.isReady()){
            ArrayList<String> musicList = MusicManager.getInstance().getMusicList();
            if (musicList == null || musicList.size() <= 0)
                return;
            this.maxMusicId = musicList.size() - 1;
            Integer musicId = getInitMusicId(this.maxMusicId);
            String musicPath = musicList.get(musicId);
            initPlay(musicPath, musicId);
            return;
        }
        kMusic.playback();
        setPlaybackButtonDrawable();
    }

    private void initPlay(String musicPath, Integer musicId) {
        String title = musicPath.substring(musicPath.lastIndexOf("/") + 1, musicPath.lastIndexOf("."));
        String desc = musicPath.substring(musicPath.lastIndexOf("/") + 1);
        reSetMusicInfo(title, desc);
        kMusic.initPlay(musicPath, musicId);
        setPlaybackButtonDrawable();
    }

    private void reSetMusicInfo(String title, String desc) {
        TextView musicTitle = (TextView)findViewById(R.id.music_title);
        TextView musicDesc = (TextView)findViewById(R.id.music_desc);
        TextView progressTime = (TextView)findViewById(R.id.progress_time);
        TextView endTime = (TextView)findViewById(R.id.end_time);
        musicTitle.setText(title);
        musicDesc.setText(desc);
        progressTime.setText("00:00");
        Integer duration = this.kMusic.getDuration() / 1000;
        String min = duration / 60 > 9 ? "" + duration / 60  : "0" + duration / 60;
        String sec = duration % 60 > 9 ? "" + duration % 60  : "0" + duration % 60;
        endTime.setText(min + ":" + sec);
    }


    private void initMusicInfo() {
        TextView musicTitle = (TextView)findViewById(R.id.music_title);
        TextView musicDesc = (TextView)findViewById(R.id.music_desc);
        musicTitle.setMovementMethod(ScrollingMovementMethod.getInstance());
        musicDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private Integer getInitMusicId(Integer maxId) {
        if (Constants.ORDER_RANDOM.equals(this.order)) {
            this.musicId = (int) (Math.random() * maxId);
        } else {
            this.musicId = 0;
        }
        return this.musicId;
    }

    private Integer getNextMusicId(Integer maxId) {
        if (Constants.ORDER_RANDOM.equals(this.order)) {
            this.musicId = (int) (Math.random() * maxId);
        } else if (this.musicId < maxId){
            this.musicId ++;
        } else {
            this.musicId = 0;
        }
        return this.musicId;
    }

    private Integer getPrevMusicId(Integer maxId) {
        if (Constants.ORDER_RANDOM.equals(this.order)) {
            this.musicId = (int) (Math.random() * maxId);
        } else if (this.musicId <= 0) {
            this.musicId = maxId;
        } else {
            this.musicId --;
        }
        return this.musicId;
    }
}