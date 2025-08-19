package com.ishidaw.snakefxgl.Utils;

import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.dsl.FXGL;

public class Play {

    public void playBGM(String music) {
        Music bgm = FXGL.getAssetLoader().loadMusic(music);
        FXGL.getAudioPlayer().loopMusic(bgm);
    }

    public void stopBGM() {
        FXGL.getAudioPlayer().stopAllMusic();
    }
}
