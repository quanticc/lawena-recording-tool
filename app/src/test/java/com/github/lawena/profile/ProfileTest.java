package com.github.lawena.profile;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Application;
import javafx.stage.Stage;

public class ProfileTest {

    @BeforeClass
    public static void initJFX() {
        Thread t = new Thread("JavaFX Init Thread") {
            @Override
            public void run() {
                Application.launch(AsNonApp.class, new String[0]);
            }
        };
        t.setDaemon(true);
        t.start();
    }

    @Test
    public void testDefaultProfile() {
        Profiles profiles = new AppProfiles();
        Assert.assertEquals("Default", profiles.getSelected().getName());
        Assert.assertTrue(profiles.containsByName("Default"));
        Assert.assertEquals(profiles.findByName("Default").get(), profiles.getSelected());
    }

    @Test
    public void testDuplication() {
        Profiles profiles = new AppProfiles();
        profiles.create(440, "Profile 1");
        Assert.assertTrue(profiles.containsByName("Profile 1"));
        profiles.duplicate(profiles.findByName("Profile 1").get());
        Assert.assertTrue(profiles.containsByName("Profile 1 - Copy"));
        profiles.duplicate(profiles.findByName("Profile 1").get());
        Assert.assertTrue(profiles.containsByName("Profile 1 - Copy (2)"));
        profiles.duplicate(profiles.findByName("Profile 1 - Copy").get());
        Assert.assertTrue(profiles.containsByName("Profile 1 - Copy (3)"));
        profiles.duplicate(profiles.findByName("Profile 1 - Copy (3)").get());
        Assert.assertTrue(profiles.containsByName("Profile 1 - Copy (4)"));
    }

    @Test
    public void testEmptyGuard() {
        Profiles profiles = new AppProfiles();
        Assert.assertEquals(1, profiles.profilesProperty().get().size());
        Assert.assertTrue(profiles.remove(profiles.findByName("Default").get()));
        Assert.assertEquals(1, profiles.profilesProperty().get().size());
    }

    public static class AsNonApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            // noop
        }
    }
}
