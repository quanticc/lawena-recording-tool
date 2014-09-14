package com.github.lawena.profile;

import static com.github.lawena.profile.Options.newOption;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.google.gson.reflect.TypeToken;

/**
 * Convenience class acting as a holder of all options available.
 * 
 * @author Ivan
 *
 */
public class Key {

  // Pathnames

  /**
   * Full path to the game base path. Must finish with {@link #gameFolderName}.
   */
  public static final Option<File> gamePath = newOption("path.game", File.class, new File(""));
  /**
   * Full path to a folder used as output for recording frames. This path will be forwarded to
   * Source Recorder.
   */
  public static final Option<File> recordingPath = newOption("path.recording", File.class,
      new File(""));
  /**
   * A path to store skybox previews.
   */
  public static final Option<File> skyPreviewSavePath = newOption("path.skyboxPreviews",
      File.class, new File("skybox-previews.lwf"));
  /**
   * The folder where Lawena will scan for demos. If no path is specified, it will use
   * {@link #gamePath}
   */
  public static final Option<File> demosPath = newOption("path.demos", File.class, new File(""));

  // Launch related options

  /**
   * Resolution width, to be input as launch option when the game is run. Must be higher than 640.
   */
  public static final Option<Integer> width = newOption("launch.width", Integer.class, 1280)
      .validatedBy(new RangeValidator(640, Integer.MAX_VALUE));
  /**
   * Resolution height to be input as launch option when the game is run. Must be higher than 480.
   */
  public static final Option<Integer> height = newOption("launch.height", Integer.class, 720)
      .validatedBy(new RangeValidator(480, Integer.MAX_VALUE));
  /**
   * DirectX Level to be used with the engine when the game launches. Must be equals to one of the
   * following values: 80, 81, 90, 95, 98.
   */
  public static final Option<String> dxlevel = newOption("launch.dxlevel", String.class, "98")
      .validatedBy(new ValuesValidator("80", "81", "90", "95", "98"));
  /**
   * Seconds to wait for the game before launch routine is aborted. Use 0 to disable.
   */
  public static final Option<Integer> launchTimeout = newOption("launch.timeout", Integer.class,
      120).validatedBy(new RangeValidator(0, Integer.MAX_VALUE));
  /**
   * Extra launch options provided by the user. Can be used to override other settings like
   * resolution, dxlevel or applaunch id.
   */
  public static final Option<String> launchOptions = newOption("launch.options", String.class,
      "-novid -console");

  // File and custom resource related options

  /**
   * Name of the hud folder to load into the game. Must be present in one of the resources folders.
   */
  public static final Option<String> hud = newOption("files.hud", String.class, "killnotices");
  /**
   * Name of the skybox to load into the game. Must be present in one of the resources folders.
   */
  public static final Option<String> skybox = newOption("files.skybox", String.class, "Default");
  public static final Option<List<String>> resources = newOption("files.resources",
      new TypeToken<List<String>>() {}, Arrays.asList("no_announcer_voices.vpk",
          "no_applause_sounds.vpk", "no_domination_sounds.vpk"));
  public static final Option<List<String>> extraFolders = newOption("files.extFolders",
      new TypeToken<List<String>>() {}, Arrays.asList(""));

  // config/cvar (in-game) related options

  public static final Option<Integer> framerate = newOption("cfg.framerate", Integer.class, 120)
      .validatedBy(new RangeValidator(24, Integer.MAX_VALUE));
  public static final Option<String> viewmodelSwitch = newOption("cfg.viewmodels", String.class,
      "on").validatedBy(new ValuesValidator("on", "off", "default"));
  public static final Option<Integer> viewmodelFov = newOption("cfg.vmodelFov", Integer.class, 70);
  public static final Option<Boolean> motionBlur =
      newOption("cfg.motionBlur", Boolean.class, false);
  public static final Option<Boolean> noCrosshair = newOption("cfg.noCrosshair", Boolean.class,
      true);
  public static final Option<Boolean> noCrosshairSwitch = newOption("cfg.noCrosshairLock",
      Boolean.class, true);
  public static final Option<Boolean> noDamageNumbers = newOption("cfg.noDamageNumbers",
      Boolean.class, false);
  public static final Option<Boolean> noHitsounds = newOption("cfg.noHitsounds", Boolean.class,
      true);
  public static final Option<Boolean> noVoice = newOption("cfg.noVoice", Boolean.class, true);
  public static final Option<Boolean> hudMinmode =
      newOption("cfg.hudMinmode", Boolean.class, false);
  public static final Option<Boolean> hudPlayerModel = newOption("cfg.hudPlayerModel",
      Boolean.class, false);
  public static final Option<String> extConVars = newOption("cfg.extConVars", String.class,
      "// Add Extra ConVars here, for example:\n// volume 0.05");

  // VDM options

  public static final Option<Boolean> vdmNoSkipToTick = newOption("vdm.noSkipToTick",
      Boolean.class, false);

  // Source Recorder options

  public static final Option<String> recorderVideoFormat = newOption("recorder.videoFormat",
      String.class, "TGA").validatedBy(new ValuesValidator("TGA", "JPEG"));
  public static final Option<Integer> recorderJpegQuality = newOption("recorder.jpegQuality",
      Integer.class, 95).validatedBy(new RangeValidator(1, 100));

  // Lawena options, special type

  /**
   * Default Steam application ID to launch. To be used as a launch parameter in -applaunch if the
   * user does not override it and has a default value of 440 (Team Fortress 2).
   */
  public static final Option<Integer> applaunch = newOption("lawena.applaunch", Integer.class, 440);
  /**
   * Default game path relative to Steam path. For example, in Team Fortress 2 this path is:
   * <code>SteamApps/common/Team Fortress 2/tf</code>
   * 
   * @see com.github.lawena.os.OSInterface#getSteamPath()
   */
  public static final Option<String> relativeGamePath = newOption("lawena.gamePath", String.class,
      "SteamApps/common/Team Fortress 2/tf");
  /**
   * Required folder name for {@link #gamePath} to be validated against. For instance, Team Fortress
   * 2 uses <code>tf</code> as folder name.
   */
  public static final Option<String> gameFolderName = newOption("lawena.gameFolderName",
      String.class, "tf");

  public static final Option<File> lawenaGameFolder = newOption("lawena.contentFolder", File.class,
      new File("lwrt/tf"));

  private Key() {}

}
