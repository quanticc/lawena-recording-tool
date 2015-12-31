package com.github.lawena.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Launcher {

    private String name;
    private String icon;
    private Mode launchMode = Mode.HL2;
    private String modName;
    private String appId;
    private OperatingSystemMap processName;
    private String steamPath = "";
    private String gamePath = "";
    private String basePath;
    private String viewName;
    private List<ConfigFlag> flags = new ArrayList<>();
    private List<String> resourceFolders = new ArrayList<>();

    public Launcher() {

    }

    public Launcher(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Mode getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(Mode launchMode) {
        this.launchMode = launchMode;
    }

    public String getModName() {
        return modName;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public OperatingSystemMap getProcessName() {
        return processName;
    }

    public void setProcessName(OperatingSystemMap processName) {
        this.processName = processName;
    }

    public String getSteamPath() {
        return steamPath;
    }

    public void setSteamPath(String steamPath) {
        this.steamPath = steamPath;
    }

    public String getGamePath() {
        return gamePath;
    }

    public void setGamePath(String gamePath) {
        this.gamePath = gamePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<ConfigFlag> getFlags() {
        return flags;
    }

    public void setFlags(List<ConfigFlag> flags) {
        this.flags = flags;
    }

    public List<String> getResourceFolders() {
        return resourceFolders;
    }

    public void setResourceFolders(List<String> resourceFolders) {
        this.resourceFolders = resourceFolders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Launcher launcher = (Launcher) o;
        return Objects.equals(name, launcher.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Launcher [name=" + name + "]";
    }

    public String toFullString() {
        return "Launcher [" +
                "name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", launchMode=" + launchMode +
                ", modName='" + modName + '\'' +
                ", appId='" + appId + '\'' +
                ", processName=" + processName +
                ", steamPath='" + steamPath + '\'' +
                ", gamePath='" + gamePath + '\'' +
                ", basePath='" + basePath + '\'' +
                ", viewName='" + viewName + '\'' +
                ", flags=" + flags +
                ", resourceFolders=" + resourceFolders +
                ']';
    }

    public enum Mode {
        STEAM, HL2;
    }

}
