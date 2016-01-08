package com.github.lawena.domain;

import java.util.*;

public class Launcher {

    private String name;
    private String icon;
    private Mode launchMode = Mode.HL2;
    private String modName;
    private String appId;
    private OperatingSystemMap gameExecutable;
    private OperatingSystemMap gameProcess;
    private String steamPath = "";
    private String gamePath = "";
    private String basePath;
    private String viewName;
    private List<ConfigFlag> flags = new ArrayList<>();
    private List<String> resourceFolders = new ArrayList<>();
    private Map<String, Object> settings = new LinkedHashMap<>();

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

    public OperatingSystemMap getGameExecutable() {
        return gameExecutable;
    }

    public void setGameExecutable(OperatingSystemMap gameExecutable) {
        this.gameExecutable = gameExecutable;
    }

    public OperatingSystemMap getGameProcess() {
        return gameProcess;
    }

    public void setGameProcess(OperatingSystemMap gameProcess) {
        this.gameProcess = gameProcess;
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

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
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
                ", gameExecutable=" + gameExecutable +
                ", gameProcess=" + gameProcess +
                ", steamPath='" + steamPath + '\'' +
                ", gamePath='" + gamePath + '\'' +
                ", basePath='" + basePath + '\'' +
                ", viewName='" + viewName + '\'' +
                ", flags=" + flags +
                ", resourceFolders=" + resourceFolders +
                ", settings=" + settings +
                ']';
    }

    public enum Mode {
        STEAM, HL2;
    }

}
