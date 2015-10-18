package me.keiwu.kmusic;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;

import me.keiwu.kmusic.constant.Constants;
import me.keiwu.kmusic.core.KMusic;


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
        Log.i(Constants.LOG_INFO_TAG, "onCreate");
        setContentView(R.layout.activity_main);
        // Immersed status bar need SDK 4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setOnClickListener();
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


    @Override
    public void onClick(View view) {
        Log.i(Constants.LOG_INFO_TAG, "onClick");
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


    private void actionPlay() {
        KMusic kmusic = KMusic.getInstance();
        Integer state = kmusic.getState();
        if (Constants.KMUSIC_STATE_PLAY.equals(state)) {
            Log.i(Constants.LOG_INFO_TAG, "pause action");
            Drawable drawable = getResources().getDrawable(R.drawable.play_pause);
            btnPlay.setBackground(drawable);
            // @WARN: need to sync the operation
            kmusic.setState(Constants.KMUSIC_STATE_PAUSE);
        } else {
            Log.i(Constants.LOG_INFO_TAG, "play action");
            Drawable drawable = getResources().getDrawable(R.drawable.play_start);
            btnPlay.setBackground(drawable);
            // @WARN: need to sync the operation
            kmusic.setState(Constants.KMUSIC_STATE_PLAY);
        }
    }

    private void actionNext() {
        Log.i(Constants.LOG_INFO_TAG, "next action");
    }

    private void actionPrevious() {
        Log.i(Constants.LOG_INFO_TAG, "previous action");
    }

    private void actionFavourite() {
        Log.i(Constants.LOG_INFO_TAG, "favourite action");
    }



}