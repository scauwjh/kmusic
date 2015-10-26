package me.keiwu.kmusic.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import me.keiwu.kmusic.R;
import me.keiwu.kmusic.constant.Constants;
import me.keiwu.kmusic.service.MusicService;
import me.keiwu.kmusic.service.MusicService.MusicBinder;


/**
 * Created by kei on 2015/10/10.
 */
public class MainActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {

    public static Boolean isRunning;

    private ImageButton mPlayButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ImageButton mFavouriteButton;
    private ImageView mOrderButton;
    private SeekBar mSeekBar;
    private TextView mProgressTime;
    private TextView mEndTime;

    // private BroadcastReceiver mSeekBarReceiver;
    private MusicService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(Constants.LOG_TAG, "connected");
            MusicBinder binder = (MusicBinder) service;
            mService = binder.getService();
            serviceConectInit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.LOG_TAG, "MainActivity onCreate");
        initUI();
        initListener();
        // bind service
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, mConnection, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        seekBarSyncStop();
        mService.initNotification();
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


    private void initUI() {
        setContentView(R.layout.activity_main);
        // Immersed status bar need SDK 4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        TextView musicTitle = (TextView) findViewById(R.id.music_title);
        TextView musicDesc = (TextView) findViewById(R.id.music_desc);
        musicTitle.setMovementMethod(ScrollingMovementMethod.getInstance());
        musicDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void initListener() {
        Log.i(Constants.LOG_TAG, "initListener");
        // text view
        mProgressTime = (TextView) findViewById(R.id.progress_time);
        mEndTime = (TextView) findViewById(R.id.end_time);

        // onclick listener
        mPlayButton = (ImageButton) findViewById(R.id.btn_play);
        mPlayButton.setOnClickListener(this);

        mPreviousButton = (ImageButton) findViewById(R.id.btn_previous);
        mPreviousButton.setOnClickListener(this);

        mNextButton = (ImageButton) findViewById(R.id.btn_next);
        mNextButton.setOnClickListener(this);

        mFavouriteButton = (ImageButton) findViewById(R.id.btn_favourite);
        mFavouriteButton.setOnClickListener(this);

        mOrderButton = (ImageView) findViewById(R.id.btn_order);
        mOrderButton.setOnClickListener(this);

        // seek bar listeners
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(100);
        // mSeekBarReceiver = new BroadcastReceiver(){
        //     @Override
        //     public void onReceive(Context context, Intent intent) {
        //         Integer progress = intent.getIntExtra(Constants.INTENT_EXTRA_PROGRESS, 0);
        //         Integer duration = intent.getIntExtra(Constants.INTENT_EXTRA_DURATION, 1);
        //         mSeekBar.setProgress(progress*100 / duration);
        //     }
        // };
        // registerReceiver(mSeekBarReceiver, new IntentFilter(MainActivity.class.getName()));
    }

    private void serviceConectInit() {
        // complete and error listener
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
        mService.initPlayer(completionListener, errorListener);
        seekBarSyncStart();
        setPlaybackButton(mService.isPlaying());
    }

    // TODO:
    // change to callback
    private void actionPlay() {
        reSetDrawable(mService.playback());
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
    }



    // TODO
    // save 10 music history
    private void doOnCompletion() {
        if (!mService.playNext())
            return;
        setPlaybackButton(true);
        setMusicInfo();
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
        TextView musicTitle = (TextView) findViewById(R.id.music_title);
        TextView musicDesc = (TextView) findViewById(R.id.music_desc);

        Integer duration = mService.getDuration() / 1000;
        String min = duration / 60 > 9 ? "" + duration / 60  : "0" + duration / 60;
        String sec = duration % 60 > 9 ? "" + duration % 60  : "0" + duration % 60;

        musicTitle.setText(mService.getTitle());
        musicDesc.setText(mService.getDescription());
        mProgressTime.setText("00:00");
        mEndTime.setText(min + ":" + sec);
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    String data = (String) msg.obj;
                    mProgressTime.setText(data);
                    break;
                default:
                    break;
            }
        }
    };



    private class ProgressThread implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                // Log.i(Constants.LOG_TAG, "isRunning...");
                Message msg =new Message();
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                    if (!mService.isPlaying())
                        continue;
                    Integer position = mService.getCurrentPosition() / 1000;
                    Integer duration = mService.getDuration() / 1000;
                    mSeekBar.setProgress(position*100 / duration);
                    // Log.i(Constants.LOG_TAG, position + "," + duration);
                    //
                    String min = position / 60 > 9 ? "" + position / 60  : "0" + position / 60;
                    String sec = position % 60 > 9 ? "" + position % 60  : "0" + position % 60;
                    msg.obj = min + ":" + sec;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.i(Constants.LOG_TAG, "InterruptedException: " + e.getMessage());
                }
            }
        }
    }

    private void seekBarSyncStart() {
        isRunning = true;
        ProgressThread progress = new ProgressThread();
        new Thread(progress).start();
    }

    private void seekBarSyncStop() {
        isRunning = false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Integer duration = mService.getDuration();
        // if (!mService.seekTo(duration * progress / 100))
        //     return;
        // seekBar.setProgress(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}