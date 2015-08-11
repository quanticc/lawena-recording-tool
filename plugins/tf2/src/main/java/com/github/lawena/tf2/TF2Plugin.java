package com.github.lawena.tf2;

import com.github.lawena.tf2.skybox.Skybox;
import com.github.lawena.tf2.skybox.SkyboxStore;
import com.github.lawena.util.ExternalString;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class TF2Plugin extends Plugin {

    // keys to UI elements that can be externalized
    public static final List<ExternalString> CAPTURES = ExternalString.from(Messages::getString, Arrays.asList("SourceRecorderTGA",
            "SourceRecorderJPEG",
            "SrcDemo2Managed",
            "SrcDemo2Standalone"));
    public static final List<ExternalString> DXLEVELS = ExternalString.from(Messages::getString, Arrays.asList("DxLevel80",
            "DxLevel81",
            "DxLevel90",
            "DxLevel95",
            "DxLevel98"));
    public static final List<ExternalString> HUDS = ExternalString.from(Messages::getString, Arrays.asList("HudMinimal",
            "HudBasic",
            "HudCustom"));
    public static final List<ExternalString> VIEWMODELS = ExternalString.from(Messages::getString, Arrays.asList("VmSwitchOn",
            "VmSwitchOff",
            "VmSwitchDefault"));

    private Properties config = new Properties();
    private SkyboxStore skyboxes = new SkyboxStore();

    public TF2Plugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static Optional<ExternalString> exists(List<ExternalString> list, String key) {
        Objects.requireNonNull(key);
        return list.stream().filter(s -> key.equals(s.getKey())).findFirst();
    }

    public Properties getConfig() {
        return config;
    }

    public SkyboxStore getSkyboxes() {
        return skyboxes;
    }

    public Skybox getSkybox(String key) {
        return skyboxes.getSkybox(key).orElse(Skybox.DEFAULT);
    }

    public ExternalString dxlevel(String key) {
        return exists(DXLEVELS, key).orElse(config.dxlevelProperty().get());
    }

    public ExternalString capture(String key) {
        return exists(CAPTURES, key).orElse(config.captureModeProperty().get());
    }

    public ExternalString hud(String key) {
        return exists(HUDS, key).orElse(config.hudProperty().get());
    }

    public ExternalString vmodel(String key) {
        return exists(VIEWMODELS, key).orElse(config.viewmodelSwitchProperty().get());
    }
}
