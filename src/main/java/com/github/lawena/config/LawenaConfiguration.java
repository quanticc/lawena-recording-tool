package com.github.lawena.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.lawena.config.mixins.Dimension2DMixIn;
import com.github.lawena.service.FileService;
import com.github.lawena.service.Profiles;
import com.github.lawena.service.ValidationService;
import com.github.lawena.task.LaunchTask;
import com.github.lawena.task.LawenaTask;
import com.github.lawena.util.CustomTaskProgressViewSkin;
import com.github.lawena.views.base.BasePresenter;
import com.github.lawena.views.dialog.NewProfileDialog;
import javafx.concurrent.Task;
import javafx.geometry.Dimension2D;
import org.controlsfx.control.TaskProgressView;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
public class LawenaConfiguration {

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                .addMixIn(Dimension2D.class, Dimension2DMixIn.class);
    }

    @Bean
    @Scope("prototype")
    public LaunchTask launcherTask(LawenaProperties properties, FileService fileService,
                                   ValidationService validationService, BasePresenter basePresenter,
                                   ApplicationEventPublisher publisher) {
        return new LaunchTask(properties, fileService, validationService, basePresenter, publisher);
    }

    @Bean
    public TaskProgressView<Task<?>> taskProgressView() {
        TaskProgressView<Task<?>> taskProgressView = new TaskProgressView<>();
        taskProgressView.setGraphicFactory(t -> {
            // allow customized graphic via LawenaTask
            if (t instanceof LawenaTask) {
                LawenaTask<?> lawenaTask = (LawenaTask<?>) t;
                return lawenaTask.getImageView();
            }
            return LawenaTask.getGenericImageView();
        });
        taskProgressView.setSkin(new CustomTaskProgressViewSkin<>(taskProgressView));
        return taskProgressView;
    }

    @Lazy
    @Bean
    public NewProfileDialog newProfileDialog(Profiles profiles) {
        return new NewProfileDialog(profiles);
    }

}
