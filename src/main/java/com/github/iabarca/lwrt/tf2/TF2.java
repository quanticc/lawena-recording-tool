
package com.github.iabarca.lwrt.tf2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.iabarca.lwrt.games.SteamGame;
import com.github.iabarca.lwrt.lwrt.LinuxInterface;
import com.github.iabarca.lwrt.lwrt.OSInterface;
import com.github.iabarca.lwrt.lwrt.OSXInterface;
import com.github.iabarca.lwrt.profile.Profile;

public class TF2 implements SteamGame {

    private Map<String, String> tagMap;

    @Override
    public String getName() {
        return "Team Fortress 2";
    }

    @Override
    public String getShortName() {
        return "TF2";
    }

    @Override
    public String getRelativeSteamGamePath() {
        return "SteamApps/common/Team Fortress 2/tf";
    }

    @Override
    public Profile createProfile(String name) {
        return new TF2Profile(name);
    }

    @Override
    public OSInterface getOSInterface(String osname) {
        if (osname.contains("Windows")) {
            return new TF2WindowsInterface();
        } else if (osname.contains("Linux")) {
            return new LinuxInterface();
        } else if (osname.contains("OS X")) {
            return new OSXInterface();
        } else {
            throw new UnsupportedOperationException("OS not supported");
        }
    }

    @Override
    public String getRequiredGameFolderName() {
        return "tf";
    }

    @Override
    public Map<String, String> getStartsWithTagMap() {
        if (tagMap == null) {
            tagMap = new LinkedHashMap<>();
            tagMap.put("resource/", "hud");
            tagMap.put("scripts/", "hud");
            tagMap.put("materials/console/", "hud");
            tagMap.put("materials/vgui/", "hud");
            tagMap.put("materials/skybox/", "skybox");
            tagMap.put("particles/", "particle");
            tagMap.put("cfg/", "cfg");
        }
        return tagMap;
    }

    @Override
    public Path getLawenaCustomPath() {
        return Paths.get("tf/custom");
    }

    @Override
    public Path getGameCustomPath(Path steamPath) {
        return getGamePath(steamPath).resolve("custom");
    }

    @Override
    public Path getGamePath(Path steamPath) {
        return steamPath.resolve(getRelativeSteamGamePath());
    }

}
