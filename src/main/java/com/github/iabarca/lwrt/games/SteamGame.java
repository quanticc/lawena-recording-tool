
package com.github.iabarca.lwrt.games;

import java.nio.file.Path;
import java.util.Map;

import com.github.iabarca.lwrt.lwrt.OSInterface;
import com.github.iabarca.lwrt.profile.Profile;

public interface SteamGame {

    public String getName();

    public String getShortName();

    public String getRelativeSteamGamePath();

    public Profile createProfile(String name);

    public OSInterface getOSInterface(String osname);

    public String getRequiredGameFolderName();

    public Map<String, String> getStartsWithTagMap();
    
    public Path getLawenaCustomPath();
    
    public Path getGameCustomPath(Path steamPath);
    
    public Path getGamePath(Path steamPath);

}
