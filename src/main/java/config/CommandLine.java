
package config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface CommandLine {

    public void generatePreview(String skyboxFilename);

    public boolean isTf2Running();

    public String getSystemDxLevel();

    public void setSystemDxLevel(String dxlevel);

    public String getSteamPath();

    public void startTf(int width, int height, String dxlevel);

    public List<String> getVpkContents(Path tfpath, Path vpkpath);

    public void extractIfNeeded(Path tfpath, String vpkname, Path dest, String... files) throws IOException;

    public void killTf2Process();

}
