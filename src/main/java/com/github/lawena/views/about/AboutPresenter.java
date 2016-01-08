package com.github.lawena.views.about;

import javafx.application.HostServices;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import org.pegdown.*;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

@Component
public class AboutPresenter {

    private static final Logger log = LoggerFactory.getLogger(AboutPresenter.class);

    @Autowired
    private HostServices hostServices;

    @FXML
    private WebView webView;

    @FXML
    private void initialize() {
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                interceptAnchors(webView.getEngine().getDocument());
            }
        });
        loadContent();
    }

    private void loadContent() {
        String result;
        try {
            result = streamToString(getClass().getResourceAsStream("credits.md"));
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
                        + "<link rel=\"stylesheet\" href=\"" + getClass().getResource("markdownpad-github.css") + "\">\n"
                        + "</head>\n"
                        + "<body>\n"
                        + html
                        + "</body>\n"
                        + "</html>");
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

    private String streamToString(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }
            return sb.toString();
        }
    }
}
