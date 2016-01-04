package com.github.lawena.views;

import com.github.lawena.domain.Profile;
import org.controlsfx.validation.ValidationResult;

public interface GamePresenter {

    void bind(Profile profile);

    void unbind(Profile profile);

    ValidationResult validate(Profile profile);

}
