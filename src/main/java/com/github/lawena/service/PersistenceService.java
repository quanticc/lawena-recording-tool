package com.github.lawena.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lawena.config.LawenaProperties;
import com.github.lawena.domain.AppProfile;
import com.github.lawena.domain.Launcher;
import com.github.lawena.util.LwrtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersistenceService {

    private static final Logger log = LoggerFactory.getLogger(PersistenceService.class);

    private final LawenaProperties properties;
    private final Profiles profiles;
    private final ObjectMapper mapper;

    @Autowired
    public PersistenceService(LawenaProperties properties, Profiles profiles, ObjectMapper mapper) {
        this.properties = properties;
        this.profiles = profiles;
        this.mapper = mapper;
    }

    /**
     * Attempts to load Lawena settings from a specified file defined by the <code>lawena.profilesPath</code> property
     * which must point to a JSON file with the profiles and launcher data.
     * <p>
     * The data will be validated for integrity,
     * profiles must have a valid launcher defined in the same file, the selected profile must be defined in the same
     * file and fields "selected", "profiles" and "launchers" must not be empty or null.
     * <p>
     * If the data could not be loaded or is not valid, it will use the values defined in the application configuration
     * files and validated again. In case problems persist, the loaded values will fallback to defaults.
     *
     * @return an object containing the loaded properties
     */
    public LawenaProperties loadLaunchSettings() {
        Path path = Paths.get(properties.getProfilesPath());
        String selected = properties.getSelected();
        List<AppProfile> profileList = properties.getProfiles();
        List<Launcher> launcherList = properties.getLaunchers();
        if (Files.exists(path)) {
            // attempt to deserialize json with profile and launcher data
            // if the data is invalid or could not be loaded, fallback to YAML configuration files
            try {
                LawenaProperties data = mapper.readValue(path.toFile(), LawenaProperties.class);
                validate(data.getSelected(), data.getProfiles(), data.getLaunchers());
                selected = data.getSelected();
                launcherList = data.getLaunchers();
                profileList = data.getProfiles();
                copyProperties(data, properties);
                log.info("Loaded launch configuration from: {}", path);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid configuration file at {} due to {}", path, e.toString());
            } catch (IOException e) {
                log.debug("Could not load launch configuration file from: {} due to {}", path, e.toString());
            }
        } else {
            log.info("No launch configuration file present: {}", path);
        }
        // validate YAML configuration data (provided by user or the application)
        // if it still invalid, default to hard-coded values
        try {
            validate(selected, profileList, launcherList);
            profiles.launchersProperty().setAll(launcherList);
            profiles.profilesProperty().setAll(profileList);
            profiles.selectedProperty().set(profiles.findByName(properties.getSelected()).get());
        } catch (IllegalArgumentException e) {
            log.warn("Launch configuration is not valid: {}", e.toString());
        }
        return refreshedProperties();
    }

    private static void copyProperties(LawenaProperties src, LawenaProperties dest) {
        Optional.ofNullable(src.getSteamPath()).ifPresent(dest::setSteamPath);
        dest.setLaunchTimeout(src.getLaunchTimeout());
        dest.setLastSkippedVersion(src.getLastSkippedVersion());
    }

    public LawenaProperties refreshedProperties() {
        properties.setLaunchers(profiles.launchersProperty().stream().collect(Collectors.toList()));
        properties.setProfiles(profiles.profilesProperty().stream().map(p -> (AppProfile) p).collect(Collectors.toList()));
        properties.setSelected(profiles.getSelected().getName());
        return properties;
    }

    private void validate(String selected, List<AppProfile> profiles, List<Launcher> launchers) {
        if (isNullOrEmpty(selected) || isNullOrEmpty(profiles) || isNullOrEmpty(launchers)) {
            throw new IllegalArgumentException("No null or empty fields allowed");
        }
        if (!profiles.stream().filter(p -> p.getName().equals(selected)).findAny().isPresent()) {
            throw new IllegalArgumentException("Selected profile does not exist");
        }
        if (profiles.stream().map(p -> new Launcher(p.getLauncher())).anyMatch(l -> !launchers.contains(l))) {
            throw new IllegalArgumentException("Profiles must have a defined launcher");
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public void saveLaunchSettings() {
        Path path = Paths.get(properties.getProfilesPath());
        try {
            LawenaProperties data = new LawenaProperties();
            data.setSelected(profiles.getSelected().getName());
            profiles.profilesProperty().stream().map(p -> (AppProfile) p).forEach(p -> data.getProfiles().add(p));
            profiles.launchersProperty().stream().forEach(p -> data.getLaunchers().add(p));
            copyProperties(properties, data);
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
            log.info("Saving launch configuration to file: {}", path);
        } catch (IOException e) {
            log.debug("Could not save launch configuration: {}", e.toString());
        }
    }

    public void tryAutodetectFolders() {
        // TODO try to do this in other operating systems
        if (LwrtUtils.isWindows()) {
            // first try to get a Steam path automatically

        }
    }
}
