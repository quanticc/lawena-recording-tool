package com.github.lawena.task;

import com.github.lawena.Messages;
import com.github.lawena.util.LwrtUtils;
import javafx.application.Platform;
import javafx.scene.web.WebView;
import org.pegdown.*;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class WebViewLoader extends LawenaTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(WebViewLoader.class);

    private final LoadSpec loadSpec;

    public WebViewLoader(LoadSpec loadSpec) {
        this.loadSpec = loadSpec;
    }

    @Override
    protected Void call() throws Exception {
        updateTitle(Messages.getString("ui.tasks.web.title"));
        int progress = 0;
        Map<WebView, String> map = loadSpec.getViewUrlMap();
        for (Map.Entry<WebView, String> entry : map.entrySet()) {
            updateMessage(Messages.getString("ui.tasks.web.message", entry.getValue()));
            updateProgress(++progress, map.size());
            loadContent(entry.getValue(), entry.getKey());
        }
        return null;
    }

    private void loadContent(String from, WebView to) {
        String result;
        try {
            URL url = new URL(from);
            result = LwrtUtils.streamToString(url.openStream());
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
        Path path = loadSpec.getBasePath();
        List<URL> stylesheets = loadSpec.getStylesheets();
        String base = (path != null) ? ("<base href=\"" + path.getParent().toUri().toString() + "\">\n") : "";
        String link;
        if (!stylesheets.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            stylesheets.forEach(url -> builder.append("<link rel=\"stylesheet\" href=\"").append(url).append("\">\n"));
            link = builder.toString();
        } else {
            link = "";
        }
        Platform.runLater(() -> to.getEngine().loadContent(
                "<!DOCTYPE html>\n"
                        + "<html>\n"
                        + "<head>\n"
                        + link
                        + base
                        + "</head>\n"
                        + "<body>\n"
                        + html
                        + "</body>\n"
                        + "</html>"));
    }

    public static class LoadSpec {

        private final Map<WebView, String> viewUrlMap;
        private final List<URL> stylesheets;
        private Path basePath;

        public LoadSpec() {
            this(new HashMap<>(), new ArrayList<>(), null);
        }

        public LoadSpec(Map<WebView, String> viewUrlMap) {
            this(viewUrlMap, new ArrayList<>(), null);
        }

        public LoadSpec(Map<WebView, String> viewUrlMap, List<URL> stylesheets, Path basePath) {
            this.viewUrlMap = viewUrlMap;
            this.stylesheets = stylesheets;
            this.basePath = basePath;
        }

        public Map<WebView, String> getViewUrlMap() {
            return viewUrlMap;
        }

        public Path getBasePath() {
            return basePath;
        }

        public void setBasePath(Path basePath) {
            this.basePath = basePath;
        }

        public List<URL> getStylesheets() {
            return stylesheets;
        }
    }
}
