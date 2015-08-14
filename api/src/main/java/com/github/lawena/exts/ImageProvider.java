package com.github.lawena.exts;

import com.github.lawena.Controller;

import javafx.scene.image.Image;
import ro.fortsoft.pf4j.ExtensionPoint;

public interface ImageProvider extends ExtensionPoint {

    void install(Controller parent);

    void remove(Controller parent);

    Image get(String key);

}
