package com.github.lawena.views;

import javafx.scene.Parent;

public interface LauncherView {

    String getName();

    Parent getView();

    LauncherPresenter getPresenter();

}
