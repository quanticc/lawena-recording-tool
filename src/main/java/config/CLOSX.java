
package config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CLOSX implements CommandLine {

    @Override
    public void generatePreview(String skyboxFilename) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isTf2Running() {
        String processName = "hl2_osx";
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getSystemDxLevel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSystemDxLevel(String dxlevel) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSteamPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void startTf(int width, int height, String dxlevel) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getVpkContents(Path tfpath, Path vpkpath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void extractIfNeeded(Path tfpath, String vpkname, Path dest, String... files)
            throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void killTf2Process() {
        // TODO Auto-generated method stub
        
    }

}
