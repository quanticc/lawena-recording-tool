package com.github.lawena.views.csgo;

import com.github.lawena.views.AbstractFXMLView;
import com.github.lawena.views.LauncherPresenter;
import com.github.lawena.views.LauncherView;
import org.springframework.stereotype.Component;

@Component
public class CSGOView extends AbstractFXMLView implements LauncherView {

    @Override
    public String getName() {
        return "csgo";
    }

    @Override
    public LauncherPresenter getPresenter() {
        return (LauncherPresenter) super.getPresenter();
    }
}
