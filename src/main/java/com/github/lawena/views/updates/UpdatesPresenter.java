package com.github.lawena.views.updates;

import com.github.lawena.Messages;
import com.github.lawena.domain.UpdateResult;
import com.github.lawena.repository.ImageRepository;
import com.github.lawena.service.TaskService;
import com.github.lawena.service.VersionService;
import com.github.lawena.task.UpdatesChecker;
import com.github.lawena.task.WebViewLoader;
import com.github.lawena.util.FXUtils;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class UpdatesPresenter {

    private static final Logger log = LoggerFactory.getLogger(UpdatesPresenter.class);
    private static final String HOME_URL = "https://raw.githubusercontent.com/wiki/iabarca/lawena-recording-tool/Home.md";
    private static final String LOG_URL = "https://raw.githubusercontent.com/wiki/iabarca/lawena-recording-tool/Changelog.md";

    @Autowired
    private VersionService versionService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HostServices hostServices;
    @Autowired
    private ImageRepository imageRepository;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab infoTab;
    @FXML
    private WebView infoWebView;
    @FXML
    private WebView changesWebView;

    private NotificationPane resultPane;

    @FXML
    private void initialize() {
        resultPane = new NotificationPane(infoWebView);
        infoTab.setContent(resultPane);
        infoWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                interceptAnchors(infoWebView.getEngine().getDocument());
            }
        });
        changesWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                interceptAnchors(changesWebView.getEngine().getDocument());
            }
        });
        UpdatesChecker checker = new UpdatesChecker(versionService);
        WebViewLoader.LoadSpec loadSpec = new WebViewLoader.LoadSpec();
        Map<WebView, String> map = new LinkedHashMap<>();
        map.put(infoWebView, HOME_URL);
        map.put(changesWebView, LOG_URL);
        loadSpec.getViewUrlMap().putAll(map);
        loadSpec.getStylesheets().add(getClass().getResource("../markdownpad-github.css"));
        taskService.submitTask(new WebViewLoader(loadSpec));
        taskService.submitTask(checker);
        CompletableFuture.supplyAsync(() -> {
            try {
                return checker.get();
            } catch (InterruptedException | ExecutionException e) {
                return UpdateResult.notFound(e.toString());
            }
        }).thenAcceptAsync(result -> {
            log.info("{}", result.toString());
            if (result.getStatus() == UpdateResult.Status.UPDATE_AVAILABLE) {
                resultPane.setText(result.getMessage());
                resultPane.setGraphic(new ImageView(imageRepository.image("/com/github/lawena/fugue/exclamation-24.png")));
                resultPane.getActions().add(new Action(Messages.getString("ui.updates.updateNow"), event -> {
                    if (versionService.upgradeInBackground(result.getDetails())) {
                        Stage stage = (Stage) tabPane.getScene().getWindow();
                        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    } else {
                        FXUtils.showWarning(Messages.getString("ui.updates.failedTitle"),
                                Messages.getString("ui.updates.failedHeader"),
                                Messages.getString("ui.updates.failedContent"));
                    }
                }));
                resultPane.show();
            }
        }, Platform::runLater);
    }

    private void interceptAnchors(Document document) {
        NodeList nodeList = document.getElementsByTagName("a");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            EventTarget eventTarget = (EventTarget) node;
            eventTarget.addEventListener("click", evt -> {
                EventTarget target = evt.getCurrentTarget();
                HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                String href = anchorElement.getHref();
                log.debug("Opening browser at: {}", href);
                hostServices.showDocument(href);
                evt.preventDefault();
            }, false);
        }
    }
}
