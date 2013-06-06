
package config;

public interface CommandLine {

    public void generatePreview(String skyboxFilename);

    public boolean isTf2Running();

    public String getSystemDxLevel();

    public void setSystemDxLevel(String dxlevel);

    public String getSteamPath();

    public Process startTf(int width, int height, String dxlevel);

}
