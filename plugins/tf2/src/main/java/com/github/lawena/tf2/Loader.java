package com.github.lawena.tf2;

import com.github.lawena.exts.GameProvider;
import com.github.lawena.game.SourceGame;

import ro.fortsoft.pf4j.Extension;

@Extension
public class Loader implements GameProvider {

    @Override
    public final SourceGame getGame() {
        return SourceGame.load(getClass().getResourceAsStream("tf2.json"));
    }
}
