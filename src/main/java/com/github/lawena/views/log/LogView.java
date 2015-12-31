package com.github.lawena.views.log;

import com.github.lawena.views.AbstractFXMLView;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "lawena.extras.richTextLog", name = "enabled", havingValue = "true")
public class LogView extends AbstractFXMLView {
}
