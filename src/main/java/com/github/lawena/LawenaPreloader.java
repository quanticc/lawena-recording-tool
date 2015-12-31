/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lawena;

import com.github.lawena.views.dialog.ExceptionDialog;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LawenaPreloader extends Preloader {

    private static final Logger log = LoggerFactory.getLogger(LawenaPreloader.class);

    private Stage stage;
    private ProgressIndicator progressIndicator;

    public void start(Stage stage) throws Exception {
        this.stage = stage;
        progressIndicator = new ProgressIndicator(-1);
        Scene scene = new Scene(progressIndicator, 50, 50);
        progressIndicator.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(null);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
//        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
//        }
    }

    @Override
    public boolean handleErrorNotification(ErrorNotification info) {
        log.debug("Received error notification: {}", info.toString());
        Throwable e = info.getCause();
        ExceptionDialog.show(
                Messages.getString("ui.dialog.uncaught.title"),
                Messages.getString("ui.dialog.uncaught.header"),
                e.getLocalizedMessage(), e);
        return true;
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            ProgressNotification notification = (ProgressNotification) info;
            double progress = notification.getProgress();
            progressIndicator.setProgress(progress);
            if (progress == 1.0) {
                log.debug("Application is ready");
                stage.hide();
            }
        }
    }
}
