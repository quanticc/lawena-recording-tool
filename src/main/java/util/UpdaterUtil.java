
package util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdaterUtil {

    private static final Logger log = Logger.getLogger("lawena");

    private ClassLoader cl;

    public UpdaterUtil() {
        try {
            URL[] urls = new URL[] {
                    new File("code/getdown-client.jar").toURI().toURL()
            };
            cl = new URLClassLoader(urls);
        } catch (MalformedURLException e) {
            log.log(Level.INFO, "URL is malformed!", e);
        }
    }

    public void cleanupUnusedFiles() {
        try {
            Class<?> cls = cl.loadClass("com.threerings.getdown.util.ConfigUtil");
            Method parseConfig = cls.getMethod("parseConfig", File.class, boolean.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) parseConfig.invoke(null, new File(
                    "getdown.txt"), false);
            Method getMultiValue = cls.getMethod("getMultiValue", Map.class, String.class);
            String[] toDelete = (String[]) getMultiValue.invoke(null, config, "delete");
            for (String path : toDelete) {
                try {
                    if (Files.deleteIfExists(Paths.get(path))) {
                        log.info("Deleted deprecated file: " + path);
                    }
                } catch (IOException e) {
                    log.log(Level.INFO, "Could not delete deprecated file: " + path, e);
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.log(Level.INFO, "Could not remove unused files", e);
        }
    }

    public void updateLauncher() {
        try {
            Class<?> cls = cl.loadClass("com.threerings.getdown.util.LaunchUtil");
            Method upgradeLauncher = cls.getMethod("upgradeGetdown", File.class, File.class,
                    File.class);
            File oldLauncher = new File("../lawena-old.exe");
            File curLauncher = new File("../lawena.exe");
            File newLauncher = new File("code/lawena-new.exe");
            Logger logger = Logger.getLogger("com.threerings.getdown");
            logger.setParent(Logger.getLogger("lawena"));
            upgradeLauncher.invoke(null, oldLauncher, curLauncher, newLauncher);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.log(Level.INFO, "Could not update launcher executable", e);
        }
    }

}
