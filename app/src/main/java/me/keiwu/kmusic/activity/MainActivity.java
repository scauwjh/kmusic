package me.keiwu.kmusic.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import me.keiwu.kmusic.R;
import me.keiwu.kmusic.constant.Constants;
import me.keiwu.kmusic.core.KMusic;
import me.keiwu.kmusic.service.MusicService;
import me.keiwu.kmusic.service.MusicService.MusicBinder;


/**
 * Created by kei on 2015/10/10.
 */
public class MainActivity extends Activity implements OnClickListener, OnTouchListener {


    private ImageButton mPlayButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ImageButton mFavouriteButton;
    private ImageView mOrderButton;

    private KMusic mKMusic;
    private MusicService mService;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(Constants.LOG_TAG, "connected");
            MusicBinder binder = (MusicBinder) service;
            mService = binder.getService();
            userInit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


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
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        setOnClickListener();
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    private void userInit() {
        initInfo();
        initKMusic();
        setPlaybackButton(mService.isPlaying());
    }


    private void initInfo() {
        Log.i(Constants.LOG_TAG, "initInfo");
        TextView musicTitle = (TextView)findViewById(R.id.music_title);
        TextView musicDesc = (TextView)findViewById(R.id.music_desc);
        musicTitle.setMovementMethod(ScrollingMovementMethod.getInstance());
        musicDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void initKMusic() {
        Log.i(Constants.LOG_TAG, "initKMusic");
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
        mKMusic = mService.getInitKMusic(completionListener, errorListener);
    }



    private void setOnClickListener() {
        mPlayButton = (ImageButton) findViewById(R.id.btn_play);
        mPlayButton.setOnClickListener(this);
        mPlayButton.setOnTouchListener(this);

        mPreviousButton = (ImageButton) findViewById(R.id.btn_previous);
        mPreviousButton.setOnClickListener(this);

        mNextButton = (ImageButton) findViewById(R.id.btn_next);
        mNextButton.setOnClickListener(this);

        mFavouriteButton = (ImageButton) findViewById(R.id.btn_favourite);
        mFavouriteButton.setOnClickListener(this);

        mOrderButton = (ImageView) findViewById(R.id.btn_order);
        mOrderButton.setOnClickListener(this);
    }

    // TODO:
    // change to callback
    private void actionPlay() {
        reSetDrawable(mService.playback());
    }

    // TODO
    // state bar
    private void reSetDrawable(Integer retCode) {
        if (Constants.PLAY_RET_PLAY.equals(retCode)) {
            setPlaybackButton(true);
        } else if (Constants.PLAY_RET_PAUSE.equals(retCode)) {
            setPlaybackButton(false);
        } else if (Constants.PLAY_RET_INIT.equals(retCode)) {
            setPlaybackButton(true);
            setMusicInfo();
        }
    }

    // TODO:
    // change to callback
    private void actionNext() {
        if (!mService.playNext(true))
            return;
        setPlaybackButton(true);
        setMusicInfo();
    }

    // TODO:
    // change to callback
    private void actionPrevious() {
        if (!mService.playPrevious(true))
            return;
        setPlaybackButton(true);
        setMusicInfo();
    }

    // TODO:
    // change to callback
    private void actionOrder() {
        Integer order = mService.changeOrder();
        if (Constants.ORDER_REPEAT_ALL.equals(order)) {
            mOrderButton.setImageResource(R.drawable.bottom_btn_repeat_all);
        } else if (Constants.ORDER_REPEAT_ONE.equals(order)) {
            mOrderButton.setImageResource(R.drawable.bottom_btn_repeat_one);
        } else if (Constants.ORDER_RANDOM.equals(order)) {
            mOrderButton.setImageResource(R.drawable.bottom_btn_random);
        } else {
            // TODO
            // Constants.ORDER_NO_REPEAT
            mOrderButton.setImageResource(R.drawable.bottom_btn_repeat_all);
        }
    }

    // just for test
    private void actionFavourite() {
        Log.i(Constants.LOG_TAG, "favourite action");
        mKMusic.seekTo(240000);
    }



    // TODO
    // save 10 music history
    private void doOnCompletion() {
        if (!mService.playNext())
            return;
        setPlaybackButton(true);
        setMusicInfo();
    }


    private void setPlaybackButton(Boolean isPlay) {
        Drawable drawable;
        if (isPlay) {
            drawable = getResources().getDrawable(R.drawable.play_start);
            mPlayButton.setBackground(drawable);
            return;
        }
        drawable = getResources().getDrawable(R.drawable.play_pause);
        mPlayButton.setBackground(drawable);
    }

    private void setMusicInfo() {
        TextView musicTitle = (TextView)findViewById(R.id.music_title);
        TextView musicDesc = (TextView)findViewById(R.id.music_desc);
        TextView progressTime = (TextView)findViewById(R.id.progress_time);
        TextView endTime = (TextView)findViewById(R.id.end_time);

        Integer duration = mService.getDuration() / 1000;
        String min = duration / 60 > 9 ? "" + duration / 60  : "0" + duration / 60;
        String sec = duration % 60 > 9 ? "" + duration % 60  : "0" + duration % 60;

        musicTitle.setText(mService.getTitle());
        musicDesc.setText(mService.getDescription());
        progressTime.setText("00:00");
        endTime.setText(min + ":" + sec);
    }
}