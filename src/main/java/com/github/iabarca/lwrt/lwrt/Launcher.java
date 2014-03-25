
package com.github.iabarca.lwrt.lwrt;

import java.util.Map;
import java.util.logging.Logger;

import com.github.iabarca.lwrt.csgo.CSGO;
import com.github.iabarca.lwrt.games.SteamGame;
import com.github.iabarca.lwrt.managers.Profiles;
import com.github.iabarca.lwrt.tf2.TF2;
import com.github.iabarca.lwrt.util.Updates;
import com.github.iabarca.lwrt.util.Utils;

public class Launcher {

    private static final Logger log = Logger.getLogger("lawena");

    public Launcher(Map<String, String> args) {
        retrieveVersionData(args);
        log.finer("Performing post-update tasks");
        Updates updater = new Updates();
        updater.updateLauncher();
        updater.cleanupUnusedFiles();
        updater.loadChannels();
        log.finer("Loading Profiles");
        Profiles profiles = new Profiles(getSelectedGame(args.get("game")));
        profiles.getArguments().putAll(args);
        Lwrt lwrt = new Lwrt();
        lwrt.setProfiles(profiles);
        lwrt.setUpdatesManager(updater);
        lwrt.startUI();
    }

    private SteamGame getSelectedGame(String game) {
        if (game != null) {
            if (game.equals("tf")) {
                return new TF2();
            } else if (game.equals("csgo")) {
                return new CSGO();
            }
        } else {
            // TODO: default selection
            return new TF2();
        }
        throw new IllegalArgumentException("Invalid game: " + game);
    }

    private void retrieveVersionData(Map<String, String> args) {
        log.finer("Retrieving version data from JAR");
        String impl = this.getClass().getPackage().getImplementationVersion();
        if (impl != null) {
            args.put("version", impl);
        } else {
            args.put("version", "");
        }
        String[] arr = impl.split("-");
        args.put("shortVersion", arr[0] + (arr.length > 1 ? "-" + arr[1] : ""));
        args.put("build",
                Utils.getManifestString("Implementation-Build", Utils.now("yyyyMMddHHmmss")));
    }

}
