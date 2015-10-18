package me.keiwu.kmusic.core;

import me.keiwu.kmusic.constant.Constants;

/**
 * Created by kei on 2015/10/18.
 */
public class KMusic {

    private Integer state = Constants.KMUSIC_STATE_PAUSE;

    private KMusic() {}

    private static class Singleton {
        private static final KMusic INSTANCE = new KMusic();
    }

    public static final KMusic getInstance() {
        return Singleton.INSTANCE;
    }

    public Integer getState() {
        return this.state;
    }
    public void setState(Integer state) {
        this.state = state;
    }
}
