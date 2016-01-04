package com.github.lawena.views.tf2;

import com.github.lawena.views.AbstractFXMLView;
import com.github.lawena.views.GamePresenter;
import com.github.lawena.views.GameView;
import org.springframework.stereotype.Component;

@Component
public class TF2View extends AbstractFXMLView implements GameView {

    @Override
    public final String getName() {
        return "tf2";
    }

    @Override
    public GamePresenter getPresenter() {
        return (GamePresenter) super.getPresenter();
    }

}
