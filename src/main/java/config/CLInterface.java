
package config;

import java.io.*;
import java.util.logging.Logger;

public class CLInterface {

    private static final Logger log = Logger.getLogger("lwrt");

    Runtime rt;

    public CLInterface() {
        rt = Runtime.getRuntime();
    }

    public String regQuery(String key, String value, int mode) {
        String result = "";
        try {
            Process pr = rt.exec("reg query " + key + " /v " + value);
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
            Process pr = rt.exec("batch\\rg.bat " + key + " " + value + " " + content);
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTf(int width, int height, String dir, int dxlevel) {
        try {
            String command = "\""
                    + dir
                    + "\\Steam.exe\""
                    + " -applaunch 440 -dxlevel "
                    + dxlevel
                    + " -novid -noborder -noforcedmparms -noforcemaccel -noforcemspd -console -high -noipx -nojoy -sw -w "
                    + width + " -h " + height;
            log.info("Launching TF2: " + command);
            Process pr = rt.exec(command);
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning(String prname) {
        boolean found = false;
        File file = new File("batch\\procchk.vbs");
        try {
            Process p = rt.exec("cscript //NoLogo " + "\"" + file.getPath() + "\" " + prname);
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
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
            Process pr = rt.exec("\"vtfcmd\\VTFCmd.exe\" -file \"Skybox\\" + skyboxfile
                    + "\" -output \"Skybox\" -exportformat \"png\"");
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
