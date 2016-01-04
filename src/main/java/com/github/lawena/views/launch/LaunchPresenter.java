package com.github.lawena.views.launch;

import com.github.lawena.Messages;
import com.github.lawena.domain.ValidationItem;
import com.github.lawena.event.LaunchFinishedEvent;
import com.github.lawena.event.LaunchNextStateEvent;
import com.github.lawena.event.LaunchStartedEvent;
import com.github.lawena.event.LaunchStatusUpdateEvent;
import com.github.lawena.util.FXUtils;
import com.github.lawena.util.LwrtUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class LaunchPresenter {

    private static final Logger log = LoggerFactory.getLogger(LaunchPresenter.class);

    @FXML
    private Label setupStateLabel;

    @FXML
    private Label replaceStateLabel;

    @FXML
    private Label gameStateLabel;

    @FXML
    private Label restoreStateLabel;

    @FXML
    private Accordion accordion;

    @FXML
    private TitledPane validationPane;

    @FXML
    private TableView<ValidationItem> validationTable;

    @FXML
    private TableColumn<ValidationItem, String> timeColumn;

    @FXML
    private TableColumn<ValidationItem, String> typeColumn;

    @FXML
    private TableColumn<ValidationItem, String> messageColumn;

    @FXML
    private TitledPane processingPane;

    private ObjectProperty<State> stateProperty = new SimpleObjectProperty<>(this, "state", State.IDLE);
    private ObjectProperty<ErrorLevel> errorLevelProperty = new SimpleObjectProperty<>(this, "errorLevel", ErrorLevel.NONE);
    private Node workingGraphic;
    private Node errorGraphic;
    private Node warningGraphic;

    @FXML
    private void initialize() {
        timeColumn.setCellValueFactory(m -> new ReadOnlyObjectWrapper<>(m.getValue().getTime().format(DateTimeFormatter.ISO_LOCAL_TIME)));
        timeColumn.setEditable(false);
        typeColumn.setCellValueFactory(m -> new ReadOnlyObjectWrapper<>(m.getValue().getType()));
        typeColumn.setEditable(false);
        messageColumn.setCellValueFactory(m -> new ReadOnlyObjectWrapper<>(m.getValue().getMessage()));
        messageColumn.setEditable(false);

        // unique indicators => can be reused
        ProgressIndicator indicator = new ProgressIndicator(-1);
        indicator.setPrefSize(16, 16);
        workingGraphic = indicator;
        errorGraphic = new ImageView(LwrtUtils.localImage("/fugue/cross-circle.png"));
        warningGraphic = new ImageView(LwrtUtils.localImage("/fugue/exclamation.png"));
        Platform.runLater(() -> {
            Label[] labels = {setupStateLabel, replaceStateLabel, gameStateLabel, restoreStateLabel};
            State[] states = {State.SETUP, State.REPLACE, State.GAME, State.RESTORE};
            for (int i = 0; i < labels.length; i++) {
                bindGraphicProperty(labels[i], states[i]);
            }
        });
    }

    private void bindGraphicProperty(Label label, State state) {
        BooleanBinding hasGivenState = stateProperty.isEqualTo(state);
        BooleanBinding hasLaterState = Bindings.greaterThan(
                state.ordinal(), Bindings.createIntegerBinding(() -> stateProperty.get().ordinal(), stateProperty));
        BooleanBinding hasError = errorLevelProperty.isEqualTo(ErrorLevel.ERROR);
        BooleanBinding hasWarning = errorLevelProperty.isEqualTo(ErrorLevel.WARNING);
        BooleanBinding isWorking = errorLevelProperty.isEqualTo(ErrorLevel.WORKING);
        label.graphicProperty().bind(
                Bindings.when(hasGivenState.and(hasError))
                        .then(errorGraphic)
                        .otherwise(
                                Bindings.when(hasGivenState.and(hasWarning))
                                        .then(warningGraphic)
                                        .otherwise(
                                                Bindings.when(hasGivenState.and(isWorking))
                                                        .then(workingGraphic)
                                                        .otherwise(
                                                                Bindings.when(hasLaterState)
                                                                        .then(bulletGraphic())
                                                                        .otherwise(tickGraphic())
                                                        )
                                        )
                        )
        );
    }

    private Node tickGraphic() {
        return new ImageView(LwrtUtils.localImage("/fugue/tick-circle.png"));
    }

    private Node bulletGraphic() {
        ImageView view = new ImageView();
        view.setFitHeight(16);
        view.setFitWidth(16);
        return view;
    }

    @EventListener
    private void launchStarted(LaunchStartedEvent event) {
        // signal setup state
        signalStart();
        signalNext();
        signalWorking();
        // expand validation pane
        accordion.setExpandedPane(validationPane);
        // clear existing data and save this event to table
        validationTable.getItems().clear();
    }

    @EventListener
    private void launchNextState(LaunchNextStateEvent event) {
        signalNext();
    }

    @EventListener
    private void launchFinished(LaunchFinishedEvent event) {
        event.getResult().getMessages().stream()
                .map(this::newItem)
                .forEach(item -> validationTable.getItems().add(item));
        int ordinal = event.getResult().getMessages().stream()
                .map(ValidationMessage::getSeverity)
                .map(Severity::ordinal)
                .reduce(Integer.MAX_VALUE, Math::min);
        if (ordinal == Severity.ERROR.ordinal()) {
            signalError();
        } else if (ordinal == Severity.WARNING.ordinal()) {
            signalWarning();
        } else {
            signalDone();
        }
    }

    @EventListener
    private void updateStatus(LaunchStatusUpdateEvent event) {
        String title = event.getTitle();
        String message = event.getMessage();
        double workDone = event.getWorkDone();
        double max = event.getMax();
        Platform.runLater(() -> {
            String type = title != null ? title : Messages.getString("ui.launch.validation.statusTitle");
            String text = message != null ? message : Messages.getString("ui.launch.validation.statusMessage");
            if (!Double.isNaN(workDone)) {
                if ((workDone == Math.floor(workDone)) && !Double.isInfinite(workDone)) {
                    // integral type
                    text = "[" + (long) workDone + "/" + (long) max + "] " + text;
                } else {
                    text = String.format(" (%.0f) %s", workDone / max * 100, text);
                }
            }
            ValidationItem item = new ValidationItem(type, text);
            validationTable.getItems().add(item);
        });
    }

    private ValidationItem newItem(ValidationMessage message) {
        String severity = Messages.getString("ui.launch." + message.getSeverity().name().toLowerCase());
        return new ValidationItem(severity, message.getText());
    }

    private void signalError() {
        FXUtils.ensureRunLater(() -> {
            errorLevelProperty.set(ErrorLevel.ERROR);
            accordion.setExpandedPane(validationPane);
            log.debug("Signaling error: {}/{}", stateProperty.get(), errorLevelProperty.get());
        });
    }

    private void signalWarning() {
        FXUtils.ensureRunLater(() -> {
            errorLevelProperty.set(ErrorLevel.WARNING);
            accordion.setExpandedPane(validationPane);
            log.debug("Signaling warning: {}/{}", stateProperty.get(), errorLevelProperty.get());
        });
    }

    private void signalDone() {
        FXUtils.ensureRunLater(() -> {
            errorLevelProperty.set(ErrorLevel.NONE);
            log.debug("Signaling done: {}/{}", stateProperty.get(), errorLevelProperty.get());
        });
    }

    private void signalWorking() {
        FXUtils.ensureRunLater(() -> {
            errorLevelProperty.set(ErrorLevel.WORKING);
            log.debug("Signaling working: {}/{}", stateProperty.get(), errorLevelProperty.get());
        });
    }

    private void signalNext() {
        FXUtils.ensureRunLater(() -> {
            stateProperty.set(State.values()[stateProperty.get().ordinal() + 1 % State.values().length]);
            log.debug("Signaling next state: {}/{}", stateProperty.get(), errorLevelProperty.get());
            if (stateProperty.get() == State.GAME) {
                accordion.setExpandedPane(processingPane);
            }
        });
    }

    private void signalStart() {
        FXUtils.ensureRunLater(() -> {
            stateProperty.set(State.IDLE);
            errorLevelProperty.set(ErrorLevel.NONE);
            log.debug("Signaling start: {}/{}", stateProperty.get(), errorLevelProperty.get());
        });
    }

    private enum State {
        IDLE, SETUP, REPLACE, GAME, RESTORE;
    }

    private enum ErrorLevel {
        NONE, WORKING, WARNING, ERROR;
    }
}
