package com.github.lawena.config;

import com.github.lawena.repository.ImageRepository;
import com.github.lawena.service.Profiles;
import com.github.lawena.views.base.ProfileCell;
import com.github.lawena.views.launchers.FxLauncherCell;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public ImageRepository imageRepository() {
        return new ImageRepository();
    }

    @Bean
    @Scope("prototype")
    public FxLauncherCell fxLauncherCell() {
        return new FxLauncherCell(imageRepository());
    }

    @Bean
    @Scope("prototype")
    public ProfileCell profileCell(Profiles profiles) {
        return new ProfileCell(profiles, imageRepository());
    }
}
