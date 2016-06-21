package com.github.lawena.service;

import com.github.lawena.config.Constants;
import com.github.lawena.config.LawenaProperties;
import com.github.lawena.domain.DataValidationMessage;
import com.github.lawena.domain.Launcher;
import com.github.lawena.domain.Profile;
import com.github.lawena.util.LaunchException;
import com.github.lawena.util.LwrtUtils;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

@Service
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    private final LawenaProperties properties;
    private final Profiles profiles;

    @Autowired
    public ValidationService(LawenaProperties properties, Profiles profiles) {
        this.properties = properties;
        this.profiles = profiles;
    }

    public ValidationResult validate() {
        return validate(getSelectedProfile());
    }

    public ValidationResult validate(Profile profile) {
        log.info("Validating settings");
        Launcher launcher = profiles.getLauncher(profile).get();
        /*
        KEY CHECKS
        **********
        I. UI SETTINGS
        --------------
        - Delegate to the correct presenter, validate UI controls
         */
        ValidationResult result = profiles.getPresenter(launcher).validate(profile);
        /*
        II. APPLICATION SETTINGS
        -----------------------
        1. The game must be executable
            a. if launcher mode is "STEAM" it's critical that steamPath AND gamePath are valid
                - steamPath can be overridden by profile (lawena.steamPath)
                - also by the launcher (launcher#getSteamPath)
                - by the application (settings#getSteamPath) <--- default way to set it
                - fallback to application.properties (lawenaProperties#getSteamPath)
            b. if launcher mode is "HL2" it's critical that gamePath is valid
                - valid means that launcher#processName is configured and existing
                - which implies that gamePath must exist and be a directory
         */
        Launcher.Mode mode = launcher.getLaunchMode();
        if (mode == null) {
            result.add(new DataValidationMessage(DataValidationMessage.Type.missingLaunchMode));
        } else {
            switch (mode) {
                case STEAM:
                    validateSteamLauncher(profile).ifPresent(result::add);
                    // no break needed, STEAM needs both validations
                case HL2:
                    validateSourceLauncher(profile).ifPresent(result::add);
            }
        }

        /*
        III. GAME SETTINGS (profile values, extra checks)
        -------------------------------------------------
        - Delegate to the correct presenter if needed
        - (ALL) recorder.mode must match one of CAPTURE_MODES
        - (ALL) launch.dxlevel must match one of DIRECTX_LEVELS
        - (TF2) tf2.hud must match one of HUDS
         */
        //validateContains(profile, "recorder.mode", Constants.CAPTURE_MODES, ExternalString::getKey).ifPresent(result::add);
        //validateContains(profile, "launch.dxlevel", Constants.DIRECTX_LEVELS, ExternalString::getKey).ifPresent(result::add);
        //validateContains(profile, "tf2.hud", Constants.HUDS, ExternalString::getKey).ifPresent(result::add);

        return result;
    }

    private Optional<ValidationMessage> validateString(Profile profile, String key) {
        try {
            getString(profile, key);
        } catch (LaunchException e) {
            return Optional.of(new DataValidationMessage(DataValidationMessage.Type.genericError, key));
        }
        return Optional.empty();
    }

    private <T> Optional<ValidationMessage> validateContains(Profile profile, String key, List<T> list, Function<T, String> mapper) {
        try {
            String value = getString(profile, key);
            if (list.stream().map(mapper).noneMatch(value::equals)) {
                return Optional.of(new DataValidationMessage(DataValidationMessage.Type.genericError, key));
            }
        } catch (LaunchException e) {
            return Optional.of(new DataValidationMessage(DataValidationMessage.Type.genericError, key));
        }
        return Optional.empty();
    }

    private Optional<ValidationMessage> validateSteamLauncher(Profile profile) {
        try {
            getSteamPath(profile);
            return Optional.empty();
        } catch (LaunchException e) {
            return Optional.of(new DataValidationMessage(DataValidationMessage.Type.missingSteamPath));
        }
    }

    public Path getSteamExecutable() throws LaunchException {
        try {
            return getSteamPath().toRealPath().resolve(Constants.STEAM_APP_NAME.get());
        } catch (IOException e) {
            throw new LaunchException("Could not get real steam path", e);
        }
    }

    public Path getSteamPath() throws LaunchException {
        return getSteamPath(getSelectedProfile());
    }

    public Path getSteamPath(Profile profile) throws LaunchException {
        String key = "lawena.steamPath";
        Launcher launcher = profiles.getLauncher(profile).get();
        Path path = LwrtUtils.tryGetPath(profile.get(key).map(Object::toString).orElse(null))
                .filter(LwrtUtils::isValidSteamPath)
                .orElseGet(
                        () -> LwrtUtils.tryGetPath(launcher.getSteamPath())
                                .filter(LwrtUtils::isValidSteamPath)
                                .orElseGet(
                                        () -> LwrtUtils.tryGetPath(properties.getSteamPath())
                                                .filter(LwrtUtils::isValidSteamPath)
                                                .orElse(null)
                                )
                );
        if (path == null) {
            throw new LaunchException("Invalid steam path");
        }
        return path;
    }

    private Optional<ValidationMessage> validateSourceLauncher(Profile profile) {
        try {
            getGamePath(profile);
            return Optional.empty();
        } catch (LaunchException e) {
            return Optional.of(new DataValidationMessage(DataValidationMessage.Type.missingGamePath));
        }
    }

    public Path getGameExecutable() throws LaunchException {
        Launcher launcher = profiles.getLauncher(getSelectedProfile()).get();
        try {
            return getGamePath().toRealPath().getParent().resolve(launcher.getGameExecutable().get());
        } catch (IOException e) {
            throw new LaunchException("Could not get real game path", e);
        }
    }

    public String getGameProcess() {
        return profiles.getLauncher(getSelectedProfile()).get().getGameProcess().get();
    }

    public Path getGamePath() throws LaunchException {
        return getGamePath(getSelectedProfile());
    }

    public Path getGamePath(Profile profile) throws LaunchException {
        Launcher launcher = profiles.getLauncher(profile).get();
        Path path = LwrtUtils.tryGetPath(launcher.getGamePath())
                .filter(p -> hasGameExecutable(p, launcher))
                .orElse(null);
        if (path == null) {
            throw new LaunchException("Invalid game path");
        }
        return path;
    }

    private boolean hasGameExecutable(Path gamePath, Launcher launcher) {
        return LwrtUtils.isValidGamePath(gamePath, launcher.getGameExecutable().get());
    }

    public Path getBasePath() throws LaunchException {
        return getBasePath(getSelectedProfile());
    }

    public Path getBasePath(Profile profile) throws LaunchException {
        Launcher launcher = profiles.getLauncher(profile).get();
        return LwrtUtils.tryGetPath(launcher.getBasePath()).map(Constants.LWRT_PATH::resolve).orElseThrow(() -> new LaunchException("Invalid base path"));
    }

    public int getInteger(String key) throws LaunchException {
        return getInteger(getSelectedProfile(), key);
    }

    public int getInteger(Profile profile, String key) throws LaunchException {
        return mapAndGet(profile, key, o -> (Integer) o);
    }

    public <T> T mapAndGet(String key, Function<Object, T> mapper) throws LaunchException {
        return mapAndGet(getSelectedProfile(), key, mapper);
    }

    public <T> T mapAndGet(Profile profile, String key, Function<Object, T> mapper) throws LaunchException {
        try {
            return profile.get(key).map(mapper).get();
        } catch (ClassCastException | NoSuchElementException e) {
            throw new LaunchException("Invalid value in profile " + profile.getName() + ": " + key + " -> " + profile.get(key), e);
        }
    }

    public Path getPath(Profile profile, String key) throws LaunchException {
        try {
            return LwrtUtils.tryGetPath(getString(profile, key, Object::toString)).get();
        } catch (NullPointerException | ClassCastException | NoSuchElementException e) {
            throw new LaunchException("Invalid value in profile " + profile.getName() + ": " + key + " -> " + profile.get(key), e);
        }
    }

    public String getString(String key) throws LaunchException {
        return getString(getSelectedProfile(), key, Object::toString);
    }

    public String getString(Profile profile, String key) throws LaunchException {
        return getString(profile, key, Object::toString);
    }

    public String getString(Profile profile, String key, Function<Object, String> mapper) throws LaunchException {
        try {
            return profile.get(key).map(mapper).get();
        } catch (NullPointerException | ClassCastException | NoSuchElementException e) {
            throw new LaunchException("Invalid value in profile " + profile.getName() + ": " + key + " -> " + profile.get(key), e);
        }
    }

    public Profile getSelectedProfile() {
        return profiles.getSelected();
    }

    public Launcher getSelectedLauncher() {
        return profiles.getLauncher(getSelectedProfile()).get();
    }
}
