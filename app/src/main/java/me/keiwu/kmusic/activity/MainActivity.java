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
public class MainActivity extends Activity implements OnClickListener {

    public static Boolean seekBarSync;
    public static Boolean seekBarOnTouching;

    private ImageButton mPlayButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ImageButton mFavouriteButton;
    private ImageView mOrderButton;
    private SeekBar mSeekBar;

    private TextView mMusicTitle;
    private TextView mMusicDesc;
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

    public MainActivity() {
        seekBarSync = false;
        seekBarOnTouching = false;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.LOG_TAG, "MainActivity onCreate");
        initUI();
        initView();
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
    }

    private void initView() {
        Log.i(Constants.LOG_TAG, "initView");
        // text view
        mMusicTitle = (TextView) findViewById(R.id.music_title);
        mMusicDesc = (TextView) findViewById(R.id.music_desc);
        mMusicTitle.setMovementMethod(ScrollingMovementMethod.getInstance());
        mMusicDesc.setMovementMethod(ScrollingMovementMethod.getInstance());

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
        mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
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
        setOrderButton(mService.getOrder());
        setMusicInfo();
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
        setOrderButton(order);
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
        Message msg =new Message();
        msg.what = Constants.MAIN_HANDLER_MUSIC_INFO;
        msg.obj = null;
        mHandler.sendMessage(msg);
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

    private void setOrderButton(Integer order) {
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

    private void setMusicInfo() {
        if (mService == null)
            return;
        Integer duration = mService.getDuration();
        String endTime = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
        mMusicTitle.setText(mService.getTitle());
        mMusicDesc.setText(mService.getDescription());
        mProgressTime.setText("00:00");
        mEndTime.setText(endTime);
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (Constants.MAIN_HANDLER_PROGRESS.equals(msg.what)) {
                String data = (String) msg.obj;
                mProgressTime.setText(data);
                return;
            }
            if (Constants.MAIN_HANDLER_MUSIC_INFO.equals(msg.what)) {
                setMusicInfo();
                return;
            }
        }
    };



    private class ProgressThread implements Runnable {
        @Override
        public void run() {
            while (seekBarSync) {
                // Log.i(Constants.LOG_TAG, "seekBarSync...");
                Message msg =new Message();
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                    if (!mService.isPlaying() || seekBarOnTouching)
                        continue;
                    Integer position = mService.getCurrentPosition();
                    Integer duration = mService.getDuration();
                    mSeekBar.setProgress(position*100 / duration);
                    updateProgressTimeMsg(msg, position);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.i(Constants.LOG_TAG, "InterruptedException: " + e.getMessage());
                }
            }
        }
    }

    private void updateProgressTimeMsg(Message msg, Integer position) {
        msg.obj = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(position),
            TimeUnit.MILLISECONDS.toSeconds(position) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position))
        );
        msg.what = Constants.MAIN_HANDLER_PROGRESS;
        mHandler.sendMessage(msg);
    }


    private void seekBarSyncStart() {
        seekBarSync = true;
        ProgressThread progress = new ProgressThread();
        new Thread(progress).start();
    }

    private void seekBarSyncStop() {
        seekBarSync = false;
    }

    private OnSeekBarChangeListener mSeekBarListener =new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Integer position = mService.getDuration() * seekBar.getProgress() / 100;
            mService.seekTo(position);
            Message msg = new Message();
            updateProgressTimeMsg(msg, position);
            seekBarOnTouching = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBarOnTouching = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {

        }
    };
}