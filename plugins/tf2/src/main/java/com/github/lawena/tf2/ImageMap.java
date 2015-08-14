package com.github.lawena.tf2;

import com.github.lawena.Controller;
import com.github.lawena.exts.ImageProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import ro.fortsoft.pf4j.Extension;

@Extension
public class ImageMap implements ImageProvider {
    private static final Logger log = LoggerFactory.getLogger(ImageMap.class);

    private Map<String, Image> store = new HashMap<>();
    private Controller controller;

    @Override
    public void install(Controller c) {
        controller = c;
    }

    @Override
    public void remove(Controller controller) {
        store.clear();
    }

    @Override
    public Image get(String key) {
        return store.computeIfAbsent(key, this::load);
    }

    private Image load(String location) {
        URL url = controller.getModel().getPluginManager().getPlugin("TF2Plugin")
                .getPluginClassLoader().getResource(location);
        log.debug("Loading from URL: {}", url);
        if (url != null) {
            try {
                return new Image(url.openStream());
            } catch (IOException e) {
                log.warn("Could not open stream to load image", e);
            }
        }
        return null;
    }
}
