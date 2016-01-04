package com.github.lawena.views;

import javafx.scene.Parent;

public interface GameView {

    String getName();

    Parent getView();

    GamePresenter getPresenter();

}
