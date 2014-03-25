
package com.github.iabarca.lwrt.profile;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import com.github.iabarca.lwrt.custom.CustomContent;

public interface Profile {

    public String getName();

    public void setName(String name);

    public Collection<? extends String> getDefaultLaunchOptions();

    public Collection<? extends String> getCustomLaunchOptions();

    public Path getGamePath();

    public void setGamePath(String gamePath);

    public Path getMoviePath();

    public void setMoviePath(String moviePath);

    public Resolution getResolution();

    public String getDxlevel();

    public List<CustomContent> getCustomContent();

    // Reflection field accessors

    public String getString(String key);

    public void setString(String key, String value);

    public boolean getBoolean(String key);

    public void setBoolean(String key, boolean value);

}
