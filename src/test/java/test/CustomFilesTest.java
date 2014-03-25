
package test;

import com.github.iabarca.lwrt.managers.Custom;
import com.github.iabarca.lwrt.managers.Profiles;
import com.github.iabarca.lwrt.tf2.TF2;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class CustomFilesTest {

    private Profiles profiles;
    private Custom customs;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public CustomFilesTest() {
        profiles = new Profiles(new TF2());
        profiles.getProfile().setGamePath("E:\\Steam\\SteamApps\\common\\Team Fortress 2\\tf");
        customs = new Custom(profiles);
    }

    @Test
    public void loadCustomPathsTest() throws IOException {
        customs.addAllCustomFromPath(Paths.get("custom"));
        System.out.println(gson.toJson(customs.getCustomFiles()));
    }
}
