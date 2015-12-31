package com.github.lawena.config;

import com.github.lawena.views.log.LogAppender;
import com.github.lawena.views.log.LogView;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class LoggingConfiguration {

    private static final String DEBUG_PATTERN = "\\b(DEBUG)\\b";
    private static final String INFO_PATTERN = "\\b(INFO)\\b";
    private static final String WARN_PATTERN = "\\b(WARN(ING)?)\\b";
    private static final String ERROR_PATTERN = "\\b(ERROR)\\b";
    private static final String TIME_PATTERN = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
    private static final Pattern PATTERN = Pattern.compile("(?<DEBUG>" + DEBUG_PATTERN + ")"
            + "|(?<INFO>" + INFO_PATTERN + ")" + "|(?<WARN>" + WARN_PATTERN + ")" + "|(?<ERROR>"
            + ERROR_PATTERN + ")" + "|(?<TIME>" + TIME_PATTERN + ")");

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("DEBUG") != null ? "debug" : matcher.group("INFO") != null ? "info"
                            : matcher.group("WARN") != null ? "warn" : matcher.group("ERROR") != null ? "error"
                            : matcher.group("TIME") != null ? "time"
                            : matcher.group("COMMENT") != null ? "comment" : null;
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @Bean
    @ConditionalOnBean(LogView.class)
    public LogAppender logAppender(StyleClassedTextArea styleClassedTextArea) {
        ch.qos.logback.classic.Logger rootLog =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
        LogAppender appender = new LogAppender(rootLog.getLoggerContext(), styleClassedTextArea);
        rootLog.addAppender(appender);
        return appender;
    }

    @Bean
    @ConditionalOnBean(LogView.class)
    public StyleClassedTextArea styleClassedTextArea() {
        StyleClassedTextArea area = new StyleClassedTextArea();
        area.setWrapText(false);
        area.setEditable(false);
        area.textProperty().addListener((obs, oldText, newText) -> {
            area.setStyleSpans(0, computeHighlighting(newText));
        });
        return area;
    }

}
