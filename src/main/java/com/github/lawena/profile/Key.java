package com.github.lawena.profile;

import static com.github.lawena.profile.Options.newOption;

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
  public static final Option<String> gamePath = newOption("path.game", String.class, "");
  /**
   * Full path to a folder used as output for recording frames. This path will be forwarded to
   * Source Recorder.
   */
  public static final Option<String> recordingPath = newOption("path.recording", String.class, "");
  /**
   * A path to store skybox previews.
   */
  public static final Option<String> skyPreviewSavePath = newOption("path.skyboxPreviews",
      String.class, "skybox-previews.lwf");
  /**
   * The folder where Lawena will scan for demos. If no path is specified, it will use
   * {@link #gamePath}
   */
  public static final Option<String> demosPath = newOption("path.demos", String.class, "");

  // Launch related options

  /**
   * Resolution width, to be input as launch option when the game is run. Must be higher than 640.
   */
  public static final Option<Integer> width = newOption("launch.width", Integer.class, 1280)
      .validatedBy(RangeValidator.atLeast(640));
  /**
   * Resolution height to be input as launch option when the game is run. Must be higher than 480.
   */
  public static final Option<Integer> height = newOption("launch.height", Integer.class, 720)
      .validatedBy(RangeValidator.atLeast(480));
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
      120).validatedBy(RangeValidator.atLeast(0));
  /**
   * Extra launch options provided by the user. Can be used to override other settings like
   * resolution, dxlevel or applaunch id.
   */
  public static final Option<String> launchOptions = newOption("launch.options", String.class,
      "-novid -console");
  public static final Option<Boolean> insecure = newOption("launch.insecure", Boolean.class, false);

  // File and custom resource related options

  /**
   * Name of the hud folder to load into the game. Must be present in one of the resources folders.
   */
  public static final Option<String> hud = newOption("files.hud", String.class, "hud_killnotices")
      .validatedBy(new ValuesValidator("hud_killnotices", "hud_medic", "hud_default", "custom"));
  /**
   * Name of the skybox to load into the game. Must be present in one of the resources folders.
   */
  public static final Option<String> skybox = newOption("files.skybox", String.class, "Default");
  public static final Option<List<String>> resources = newOption("files.resources",
      new TypeToken<List<String>>() {}, Arrays.asList("no_announcer_voices.vpk",
          "no_applause_sounds.vpk", "no_domination_sounds.vpk"));
  public static final Option<List<String>> extraFolders = newOption("files.extFolders",
      new TypeToken<List<String>>() {}, Arrays.asList(""));
  public static final Option<Integer> bigFolderThreshold = newOption("files.bigFolderLimit",
      Integer.class, 200);
  public static final Option<Boolean> deleteUnneededBackups = newOption(
      "files.deleteUnneededBackups", Boolean.class, true);
  public static final Option<String> loglevel = newOption("files.loglevel", String.class, "DEBUG")
      .validatedBy(new ValuesValidator("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL"));

  // config/cvar (in-game) related options

  public static final Option<Integer> framerate = newOption("cfg.framerate", Integer.class, 120)
      .validatedBy(RangeValidator.atLeast(24));
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
  public static final Option<Boolean> loadKillstreaks = newOption("vdm.loadKillstreak",
      Boolean.class, true);
  public static final Option<String> relativeKillstreakPath = newOption(
      "vdm.relativeKillstreakPath", String.class, "KillStreaks.txt");

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
  public static final Option<String> relativeDefaultGamePath = newOption("lawena.defaultGamePath",
      String.class, "SteamApps/common/Team Fortress 2/tf");
  /**
   * Required folder name for {@link #gamePath} to be validated against. For instance, Team Fortress
   * 2 uses <code>tf</code> as folder name.
   */
  public static final Option<String> gameFolderName = newOption("lawena.gameFolderName",
      String.class, "tf");

  private Key() {}

}
