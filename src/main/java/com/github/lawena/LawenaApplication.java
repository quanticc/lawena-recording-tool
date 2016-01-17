package com.github.lawena;

import com.github.lawena.config.LawenaProperties;
import com.github.lawena.service.PersistenceService;
import com.github.lawena.service.TaskService;
import com.github.lawena.service.fx.WatchService;
import com.github.lawena.util.FXUtils;
import com.github.lawena.views.base.BaseView;
import com.github.lawena.views.dialog.ExceptionDialog;
import javafx.application.HostServices;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@EnableConfigurationProperties(LawenaProperties.class)
@SpringBootApplication
public class LawenaApplication extends AbstractJavaFxApplicationSupport {

    private static final Logger log = LoggerFactory.getLogger(LawenaApplication.class);

    @Autowired
    private BaseView baseView;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private WatchService watchService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private LawenaProperties properties;

    @Override
    public void init() throws Exception {
        super.init();
        Thread.setDefaultUncaughtExceptionHandler(this::uncaught);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Thread.currentThread().setName("JavaFXAppThread");
        persistenceService.loadLaunchSettings();
        log.debug("Current application settings: {}", properties.toPrettyString());
        stage.setTitle("Lawena Recording Tool");
        stage.getIcons().addAll(getApplicationIcons());
        stage.setScene(new Scene(baseView.getView()));
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
        notifyPreloader(new Preloader.ProgressNotification(1.0));
        watchService.start();
    }

    private Image[] getApplicationIcons() {
        return new Image[]{
                new Image("/com/github/lawena/shortcut-32@2x.png"),
                new Image("/com/github/lawena/shortcut-32.png"),
                new Image("/com/github/lawena/shortcut-16@2x.png"),
                new Image("/com/github/lawena/shortcut-16.png")
        };
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        taskService.shutdownNow();
        FXUtils.shutdownPool();
        watchService.cancel();
        persistenceService.saveLaunchSettings();
    }

    @Lazy
    @Bean
    public HostServices hostServices() {
        return getHostServices();
    }

    public static void main(String[] args) {
        launchApp(LawenaApplication.class, args);
    }

    private void uncaught(Thread t, Throwable e) {
        log.error("Unexpected problem in " + t, e);
        ExceptionDialog.show(
                Messages.getString("ui.dialog.uncaught.title"),
                Messages.getString("ui.dialog.uncaught.header"),
                Messages.getString("ui.dialog.uncaught.content", t.toString(), e.getLocalizedMessage()), e);
    }

}
