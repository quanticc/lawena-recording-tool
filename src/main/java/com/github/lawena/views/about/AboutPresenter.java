package com.github.lawena.views.about;

import com.github.lawena.Messages;
import com.github.lawena.domain.Branch;
import com.github.lawena.service.VersionService;
import com.github.lawena.util.LwrtUtils;
import javafx.application.HostServices;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import org.pegdown.*;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

import static com.github.lawena.util.LwrtUtils.interceptAnchors;

@Component
public class AboutPresenter {

    private static final Logger log = LoggerFactory.getLogger(AboutPresenter.class);

    @Autowired
    private HostServices hostServices;
    @Autowired
    private VersionService versionService;

    @FXML
    private WebView webView;
    @FXML
    private Label versionLabel;

    @FXML
    private void initialize() {
        String implVersion = versionService.getImplementationVersion();
        String buildNumber = versionService.getVersion();
        Branch branch = versionService.getCurrentBranch();
        versionLabel.setText(Messages.getString("ui.about.version", implVersion, formatBuild(buildNumber), formatBranch(branch)));
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                interceptAnchors(webView.getEngine().getDocument(), href -> hostServices.showDocument(href));
            }
        });
        loadContent();
    }

    private String formatBuild(String buildNumber) {
        if (buildNumber.equals("0")) {
            return Messages.getString("ui.about.localBuild");
        } else {
            return Messages.getString("ui.about.build", buildNumber);
        }
    }

    private String formatBranch(Branch branch) {
        if (branch.equals(Branch.STANDALONE)) {
            return Messages.getString("ui.about.standalone");
        } else {
            return Messages.getString("ui.about.branch", branch.getName());
        }
    }

    private void loadContent() {
        String result;
        try {
            result = LwrtUtils.streamToString(getClass().getResourceAsStream("credits.md"));
        } catch (IOException e) {
            log.warn("Could not load stream", e);
            result = "";
        }
        PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);
        RootNode rootNode = processor.parseMarkdown(result.toCharArray());
        String html = new ToHtmlSerializer(new LinkRenderer(),
                Collections.<String, VerbatimSerializer>emptyMap(),
                PegDownPlugins.NONE.getHtmlSerializerPlugins())
                .toHtml(rootNode);
        webView.getEngine().loadContent(
                "<!DOCTYPE html>\n"
                        + "<html>\n"
                        + "<head>\n"
                        + "<link rel=\"stylesheet\" href=\"" + getClass().getResource("../markdownpad-github.css") + "\">\n"
                        + "</head>\n"
                        + "<body>\n"
                        + html
                        + "</body>\n"
                        + "</html>");
    }
}
