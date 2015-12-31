package com.github.lawena.views.tf2;

import com.github.lawena.views.AbstractFXMLView;
import com.github.lawena.views.LauncherPresenter;
import com.github.lawena.views.LauncherView;
import org.springframework.stereotype.Component;

@Component
public class TF2View extends AbstractFXMLView implements LauncherView {

    @Override
    public final String getName() {
        return "tf2";
    }

    @Override
    public LauncherPresenter getPresenter() {
        return (LauncherPresenter) super.getPresenter();
    }

}
