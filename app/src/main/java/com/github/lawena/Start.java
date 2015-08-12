package com.github.lawena;

import com.github.lawena.dialog.ExceptionDialog;
import com.github.lawena.i18n.Messages;
import com.github.lawena.security.PluginPolicy;
import com.github.lawena.util.LwrtUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.security.Policy;
import java.util.NoSuchElementException;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Start extends Application {
    private static final Logger log = LoggerFactory.getLogger(Start.class);
    private static final int SPLASH_WIDTH = 281;
    private static final int SPLASH_HEIGHT = 63;

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;

    public static void main(String[] args) {
        Policy.setPolicy(new PluginPolicy());
        System.setSecurityManager(new SecurityManager());
        launch(args);
    }

    private static void uncaught(Thread t, Throwable e) {
        log.error("Unexpected problem in " + t, e); //$NON-NLS-1$
        ExceptionDialog
                .show(
                        Messages.getString("Main.uncaughtExceptionTitle"), //$NON-NLS-1$
                        Messages.getString("Start.uncaughtExceptionHeader"), //$NON-NLS-1$
                        String.format(
                                Messages.getString("Start.uncaughtExceptionContent"), t.toString(), e.getLocalizedMessage()), e); //$NON-NLS-1$
    }

    @Override
    public void init() throws Exception {
        Platform.setImplicitExit(true);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Thread.setDefaultUncaughtExceptionHandler(Start::uncaught);

        ImageView splash =
                new ImageView(new Image(getClass().getResource("/splash.png").toExternalForm())); //$NON-NLS-1$
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label();
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-padding: 5; -fx-background-color: cornsilk; -fx-border-width:5; "); //$NON-NLS-1$
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Task<Optional<Model>> preload = new Task<Optional<Model>>() {
            @Override
            protected Optional<Model> call() throws Exception {
                updateMessage(Messages.getString("Start.Initializing")); //$NON-NLS-1$
                try {
                    Model model = new AppModel(getParameters());
                    updateMessage(Messages.getString("Start.Ready")); //$NON-NLS-1$
                    return Optional.of(model);
                } catch (Exception e) {
                    updateMessage("Exception at launch: " + e.toString());
                    log.error("Could not initialize model", e);
                }
                return Optional.empty();
            }
        };
        showSplash(primaryStage, preload, () -> showMainStage(preload.valueProperty()));
        new Thread(preload).start();
    }

    private void showMainStage(ReadOnlyObjectProperty<Optional<Model>> model) {
        log.debug("Preparing user interface"); //$NON-NLS-1$
        Stage mainStage = new Stage();
        mainStage.setOnCloseRequest(evt -> Platform.exit());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("BaseView.fxml"));
        mainStage.setTitle("Lawena Recording Tool"); // NON-NLS
        mainStage.getIcons().addAll(LwrtUtils.image("/cap-64px.png"), LwrtUtils.image("/cap-48px.png"), // NON-NLS
                LwrtUtils.image("/cap-32px.png"), LwrtUtils.image("/cap-16px.png")); // NON-NLS
        try {
            mainStage.setScene(new Scene((Pane) loader.load()));
            Controller control = loader.getController();
            control.setStage(mainStage);
            control.setModel(model.get().get());
        } catch (NoSuchElementException e) {
            log.error("Startup was interrupted due to an error");
        } catch (IOException e) {
            log.warn("Could not show the user interface", e); //$NON-NLS-1$
        }
        Platform.runLater(mainStage::show);
    }

    private void showSplash(final Stage initStage, Task<?> task,
                            InitCompletionHandler initCompletionHandler) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();
                initCompletionHandler.complete();
            }
        });

        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();
    }

    @Override
    public void stop() throws Exception {
        log.debug("Stopping application"); //$NON-NLS-1$
    }

    public interface InitCompletionHandler {
        void complete();
    }

}
