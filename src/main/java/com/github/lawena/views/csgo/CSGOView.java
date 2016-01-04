package com.github.lawena.views.csgo;

import com.github.lawena.views.AbstractFXMLView;
import com.github.lawena.views.GamePresenter;
import com.github.lawena.views.GameView;
import org.springframework.stereotype.Component;

@Component
public class CSGOView extends AbstractFXMLView implements GameView {

    @Override
    public String getName() {
        return "csgo";
    }

    @Override
    public GamePresenter getPresenter() {
        return (GamePresenter) super.getPresenter();
    }
}
