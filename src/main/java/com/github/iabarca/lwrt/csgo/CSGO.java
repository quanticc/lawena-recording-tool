
package com.github.iabarca.lwrt.csgo;

import java.nio.file.Path;
import java.util.Map;

import com.github.iabarca.lwrt.games.SteamGame;
import com.github.iabarca.lwrt.lwrt.OSInterface;
import com.github.iabarca.lwrt.profile.Profile;

public class CSGO implements SteamGame {

    @Override
    public String getName() {
        return "Counter-Strike: Global Offensive";
    }

    @Override
    public String getShortName() {
        return "CS:GO";
    }

    @Override
    public String getRelativeSteamGamePath() {
        return "SteamApps/common/Counter-Strike Global Offensive/csgo";
    }

    @Override
    public Profile createProfile(String name) {
        return null;
    }

    @Override
    public OSInterface getOSInterface(String osname) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequiredGameFolderName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getStartsWithTagMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path getLawenaCustomPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path getGameCustomPath(Path steamPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path getGamePath(Path steamPath) {
        // TODO Auto-generated method stub
        return null;
    }

}
