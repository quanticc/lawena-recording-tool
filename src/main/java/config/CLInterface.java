
package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLInterface {

    public String regQuery(String key, String value, int mode) {
        String result = "";
        try {
            ProcessBuilder pb = new ProcessBuilder("reg", "query", key, "/v", value);
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result = result + line + '\n';
            }
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mode == 0)
            return result.substring(result.lastIndexOf("0x") + 2,
                    result.indexOf('\n', result.lastIndexOf("0x")));
        return result.substring(result.lastIndexOf(":") - 1,
                result.indexOf('\n', result.lastIndexOf(":")));

    }

    public void regedit(String key, String value, String content) {
        try {
            ProcessBuilder pb = new ProcessBuilder("batch\\rg.bat", key, value, content);
            Process pr = pb.start();
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTf(int width, int height, String dir, int dxlevel) {
        try {
            ProcessBuilder pb = new ProcessBuilder(dir + "/steam.exe", "-applaunch", "440",
                    "-dxlevel", dxlevel + "", "-novid", "-noborder", "-noforcedmparms",
                    "-noforcemaccel", "-noforcemspd", "-console", "-high", "-noipx", "-nojoy",
                    "-sw", "-w", width + "", "-h", height + "");
            Process pr = pb.start();
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning(String prname) {
        boolean found = false;
        File file = new File("batch\\procchk.vbs");
        try {
            ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", file.getPath(), prname);
            Process pr = pb.start();
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            line = input.readLine();
            if (line != null) {
                if (line.equals(prname)) {
                    found = true;
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return found;
    }

    public void generatePreview(String skyboxfile) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder("vtfcmd\\VTFCmd.exe", "-file", "Skybox\\",
                    skyboxfile, "-output", "Skybox", "-exportformat", "png");
            Process pr = pb.start();
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
