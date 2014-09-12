package com.github.lawena.os;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import net.tomahawk.ExtensionsFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtSettings;
import com.github.lawena.model.LwrtSettings.Key;
import com.github.lawena.util.Util;

/**
 * This class lets you access to all values and actions that are OS-dependent, aiming to simplify
 * the porting of some features to all systems where TF2 can run (Windows, Linux and OSX)
 */
public abstract class OSInterface {

  private static final Logger log = LoggerFactory.getLogger(OSInterface.class);

  /**
   * Returns the necessary {@link ProcessBuilder} to launch TF2. It will be used when
   * {@link #startTf(int, int, String)} is called.
   * 
   * @return The <code>ProcessBuilder</code> used to create a {@link Process} and launch TF2 with it
   *         or <code>null</code> if it couldn't be created.
   */
  public abstract ProcessBuilder getBuilderStartTF2();

  /**
   * Returns the necessary {@link ProcessBuilder} to stop or kill the TF2 process, to abort its
   * execution. It will be used when {@link #killTf2Process()} is called.
   * 
   * @return The <code>ProcessBuilder</code> used to create a {@link Process} and kill the TF2
   *         process or <code>null</code> if it couldn't be created.
   */
  public abstract ProcessBuilder getBuilderTF2ProcessKiller();

  /**
   * Returns the {@link Path} of the Steam installation (where Steam.exe, steam.sh or equivalent is
   * located)
   * 
   * @return The <code>Path</code> where Steam and TF2 main executable, can be located.
   */
  public abstract Path getSteamPath();

  /**
   * Returns the system DirectX level if it's stored somewhere in the filesystem.
   * 
   * @return a <code>String</code> representing the dxlevel value to use when launching TF2 or
   *         <code>null</code> if it couldn't be retrieved.
   */
  public abstract String getSystemDxLevel();

  /**
   * Checks if the TF2 process is running.
   * 
   * @return <code>true</code> if TF2 process still runs in the system or <code>false</code> if it
   *         does not.
   */
  public abstract boolean isRunningTF2();

  /**
   * Store the set DirectX level in the filesystem for future use, like with the
   * {@link #getSystemDxLevel()} method. If the operation is not supported this should be
   * implemented as a no-op method or with a simple log message.
   * 
   * @param dxlevel the user-specified DirectX level
   */
  public abstract void setSystemDxLevel(String dxlevel);

  /**
   * Closes all open handles matching this path. This is useful to unlock files that some process
   * can hold preventing the restore/replace file mechanism to work correctly.
   * 
   * @param path the directory where handles are being closed
   */
  public abstract void closeHandles(Path path);

  /**
   * Deletes a file using OS-level commands.
   * 
   * @param path the path to delete
   */
  public abstract void delete(Path path);

  /**
   * Stop or kill the TF2 process, whether it's being run from the tool or not.
   * 
   * @see #getBuilderTF2ProcessKiller()
   */
  public void killTf2Process() {
    try {
      ProcessBuilder pb = getBuilderTF2ProcessKiller();
      Process p = pb.start();
      try (BufferedReader input = Util.newProcessReader(p)) {
        String line;
        while ((line = input.readLine()) != null) {
          log.trace("[taskkill] " + line);
        }
      }
      p.waitFor();
    } catch (InterruptedException | IOException e) {
      log.warn("Problem stopping TF2 process", e);
    }
  }

  /**
   * Launch TF2 with some user-specified parameters.
   * 
   * @param cfg the program settings from it will retrieve values like the dxlevel and the
   *        resolution
   * @see #getBuilderStartTF2()
   */
  public void startTf(LwrtSettings cfg) {
    try {
      String opts = cfg.getString(Key.LaunchOptions);
      Map<String, String> options = new LinkedHashMap<>();
      // these options will be overridden if the user wants
      options.put("-applaunch", "440");
      options.put("-dxlevel", cfg.getDxlevel());
      options.put("-w", cfg.getWidth() + "");
      options.put("-h", cfg.getHeight() + "");
      String[] params = opts.trim().replaceAll("\\s+", " ").split(" ");
      for (int i = 0; i < params.length; i++) {
        String key = params[i];
        String value = "";
        if (key.startsWith("-") || key.startsWith("+")) {
          if (i + 1 < params.length) {
            String next = params[i + 1];
            if (next.matches("^-?\\d+$") || (!next.startsWith("-") && !next.startsWith("+"))) {
              value = next;
              i++;
            }
          }
          options.put(key, value);
        } else {
          log.warn("Discarding invalid launch parameter: {}", key);
        }
      }
      boolean fs = options.containsKey("-full") || options.containsKey("-fullscreen");
      if (fs) {
        options.remove("-sw");
        options.remove("-window");
        options.remove("-startwindowed");
        options.remove("-windowed");
      } else {
        options.put("-sw", "");
        options.put("-noborder", "");
      }
      if (options.containsKey("-width")) {
        options.remove("-w");
      }
      if (options.containsKey("-height")) {
        options.remove("-h");
      }
      // prepare list of options
      log.info("Launching Steam AppID {} in {}x{} {} with dxlevel {}", options.get("-applaunch"),
          options.get("-w"), options.get("-h"), fs ? "Fullscreen" : "Windowed",
          options.get("-dxlevel"));
      log.debug("Parameters: {}", options);
      ProcessBuilder pb = getBuilderStartTF2();
      pb.command().add("-applaunch");
      pb.command().add(options.get("-applaunch"));
      options.remove("-applaunch");
      for (Entry<String, String> e : options.entrySet()) {
        pb.command().add(e.getKey());
        pb.command().add(e.getValue());
      }
      // keeping this for compatibility
      if (cfg.getInsecure()) {
        log.debug("Using -insecure in launch options");
        pb.command().add("-insecure");
      }
      Process p = pb.start();
      try (BufferedReader input = Util.newProcessReader(p)) {
        String line;
        while ((line = input.readLine()) != null) {
          log.trace("[steam] " + line);
        }
      }
      p.waitFor();
    } catch (InterruptedException | IOException e) {
      log.warn("Process was interrupted", e);
    }
  }

  /**
   * Set the Look & Feel of the Graphical User Interface of the tool. By default it uses the system
   * L&F.
   */
  public void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      log.warn("Could not set the look and feel", e);
    }
  }

  /**
   * Use the Java Desktop API to open a folder of the given path.
   * 
   * @param path The folder that will be opened with the system file manager. If this is not a
   *        directory, it will open the file's parent.
   * @see Desktop#open(java.io.File)
   */
  public void openFolder(Path path) {
    Path parent = path.getParent();
    Path dir = Files.isDirectory(path) ? path : (parent != null ? parent : path);
    try {
      Desktop.getDesktop().open(dir.toFile());
    } catch (IOException e) {
      log.warn("Could not open directory: " + dir, e);
    }
  }

  public abstract String getVTFCmdLocation();

  /**
   * Display a dialog to retrieve a single folder selected by the user.
   * 
   * @param parent the parent component of the dialog
   * @param title the new <code>String</code> for the title bar
   * @param path the current directory to point to
   * @return the selected folder or <code>null</code> if none was chosen
   */
  public String chooseSingleFolder(Frame parent, String title, String path) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(title);
    chooser.setCurrentDirectory(new File(path));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileHidingEnabled(false);
    int answer = chooser.showOpenDialog(parent);
    if (answer == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().toString();
    } else {
      return null;
    }
  }

  /**
   * Display a dialog to let the user choose a location to save a file.
   * 
   * @param parent the parent component of the dialog
   * @param title the new <code>String</code> for the title bar
   * @param path the current directory to point to
   * @param filters a <code>List<code> of <code>ExtensionsFilter</code> to add to the choosable file
   *        filter list
   * @return the selected file or <code>null</code> if none was chosen
   */
  public String chooseSaveFile(Frame parent, String title, String path,
      List<ExtensionsFilter> filters) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(title);
    chooser.setCurrentDirectory(new File(path));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileHidingEnabled(false);
    for (ExtensionsFilter filter : filters) {
      chooser.addChoosableFileFilter(filter);
    }
    int answer = chooser.showSaveDialog(parent);
    if (answer == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().toString();
    } else {
      return null;
    }
  }

  /**
   * Display a dialog to retrieve a single file selected by the user.
   * 
   * @param parent the parent component of the dialog
   * @param title the new <code>String</code> for the title bar
   * @param path the current directory to point to
   * @param filters a <code>List<code> of <code>ExtensionsFilter</code> to add to the choosable file
   *        filter list
   * @return the selected file or <code>null</code> if none was chosen
   */
  public String chooseSingleFile(Frame parent, String title, String path,
      List<ExtensionsFilter> filters) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(title);
    chooser.setCurrentDirectory(new File(path));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileHidingEnabled(false);
    for (ExtensionsFilter filter : filters) {
      chooser.addChoosableFileFilter(filter);
    }
    int answer = chooser.showOpenDialog(parent);
    if (answer == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().toString();
    } else {
      return null;
    }
  }

}
