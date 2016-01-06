package com.github.lawena.repository;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

public class ImageRepository {

    private static final Logger log = LoggerFactory.getLogger(ImageRepository.class);

    @Cacheable("images")
    public Image image(String location) {
        long start = System.currentTimeMillis();
        Image result = null;
        try {
            result = new Image(location);
            log.debug("Cached in {} ms: {}", System.currentTimeMillis() - start, location);
            return result;
        } catch (Exception e) {
            log.debug("Could not load image: {}", e.toString());
        }
        return null;
    }
}
