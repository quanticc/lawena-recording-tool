
package com.github.iabarca.lwrt.lwrt;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import com.github.iabarca.lwrt.games.SteamGame;
import com.github.iabarca.lwrt.managers.Profiles;
import com.github.iabarca.lwrt.profile.Profile;

/**
 * This class lets you access to all values and actions that are OS-dependent,
 * aiming to simplify the porting of some features to all systems where TF2 can
 * run (Windows, Linux and OSX)
 * 
 * @author quantic
 */
public abstract class OSInterface {

    /**
     * Main logger for the tool, by default it will write {@link Level#FINE} and
     * higher events to file and in a simpler way to the "Log" tab in the UI.
     * Also it will write {@link Level#FINER} and higher events to console it
     * it's enabled.
     */
    protected static final Logger log = Logger.getLogger("lawena");

    /**
     * Returns the necessary {@link ProcessBuilder} to launch the selected game.
     * It will be used when {@link #startTf(int, int, String)} is called.
     * 
     * @return The <code>ProcessBuilder</code> used to create a {@link Process}
     *         and launch the game with it or <code>null</code> if it couldn't
     *         be created.
     */
    public abstract ProcessBuilder getBuilderStartGame();

    /**
     * Returns the necessary {@link ProcessBuilder} to stop or kill the game
     * process, to abort its execution. It will be used when
     * {@link #killGameProcess()} is called.
     * 
     * @return The <code>ProcessBuilder</code> used to create a {@link Process}
     *         and kill the game process or <code>null</code> if it couldn't be
     *         created.
     */
    public abstract ProcessBuilder getBuilderGameProcessKiller();

    /**
     * Returns the necessary {@link ProcessBuilder} to generate a preview of a
     * VTF file, in particular in this tool, to generate a skybox preview. It
     * will be used when {@link #generatePreview(String)} is called.
     * 
     * @param skyboxFilename the filename of the skybox file to generate the
     *            preview
     * @return The <code>ProcessBuilder</code> used to create a {@link Process}
     *         and generate the skybox preview or <code>null</code> if it
     *         couldn't be created.
     */
    public abstract ProcessBuilder getBuilderVTFCmd(String skyboxFilename);

    /**
     * Returns the {@link Path} of the Steam installation (where Steam.exe,
     * steam.sh or equivalent is located)
     * 
     * @return The <code>Path</code> where Steam and game main executable, can
     *         be located.
     */
    public abstract Path getSteamPath();

    /**
     * Returns the system DirectX level if it's stored somewhere in the
     * filesystem.
     * 
     * @return a <code>String</code> representing the dxlevel value to use when
     *         launching the game or <code>null</code> if it couldn't be
     *         retrieved.
     */
    public abstract String getSystemDxLevel();

    /**
     * Checks if the game process is running.
     * 
     * @return <code>true</code> if the game process still runs in the system or
     *         <code>false</code> if it does not.
     */
    public abstract boolean isGameRunning();

    /**
     * Returns the {@link Path} where the VPK included with the Source game is
     * located. This will be used to extract skyboxes for preview generation and
     * loading, and could be used for other features like packing, extracting,
     * listing, etc.
     * 
     * @param gamePath the path where the HL2 executable for the selected Source
     *            game is located
     * @return The <code>Path</code> to the VPK tool resolved from the tfpath.
     */
    public abstract Path resolveVpkToolPath(Path gamePath);

    /**
     * Store the set DirectX level in the filesystem for future use, like with
     * the {@link #getSystemDxLevel()} method. If the operation is not supported
     * this should be implemented as a no-op method or with a simple log
     * message.
     * 
     * @param dxlevel the user-specified DirectX level
     */
    public abstract void setSystemDxLevel(String dxlevel);

    /**
     * Closes all open handles matching this path. This is useful to unlock
     * files that some process can hold preventing the restore/replace file
     * mechanism to work correctly.
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
     * Extracts VPK-packed files to a specified path only if they not exist in
     * the destination path.
     * 
     * @param gamePath The {@link Path} where HL2 main executable for the
     *            selected Source game is located
     * @param vpkname The name of the VPK file to possibly extract the files
     * @param dest The <code>Path</code> where the files will be extracted to
     * @param files The filenames included in the VPK that might be extracted
     * @throws IOException If an error occurred while creating the destination
     *             folder
     */
    public void extractIfNeeded(Path gamePath, String vpkname, Path dest, Iterable<String> files)
            throws IOException {
        List<String> fileList = new ArrayList<>();
        for (String file : files) {
            if (!Files.exists(dest.resolve(file))) {
                if (Files.exists(Paths.get(vpkname))) {
                    if (!Files.exists(dest) || !Files.isDirectory(dest)) {
                        Files.createDirectory(dest);
                    }
                    fileList.add(file);
                } else {
                    log.info("Required file was not found: " + file);
                }
            }
        }
        if (!fileList.isEmpty()) {
            extractVpkFile(gamePath, vpkname, dest, fileList);
        }
    }

    private void extractVpkFile(Path gamePath, String vpkname, Path dest, List<String> files) {
        List<String> cmds = new ArrayList<>();
        try {
            Path vpktool = resolveVpkToolPath(gamePath);
            cmds.add(vpktool.toString());
            cmds.add("x");
            cmds.add(Paths.get(vpkname).toAbsolutePath().toString());
            cmds.addAll(files);
            ProcessBuilder pb = new ProcessBuilder(cmds);
            pb.directory(dest.toFile());
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.fine("[vpk] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.info("Problem extracting contents from VPK file: " + vpkname);
        }
    }

    /**
     * Generate an preview image representing a specified skybox.
     * 
     * @param skyboxFilename The filename of the skybox to generate the preview
     * @see #getBuilderVTFCmd(String)
     */
    public void generatePreview(String skyboxFilename) {
        try {
            ProcessBuilder pb = getBuilderVTFCmd(skyboxFilename);
            log.finer("generatePreview: " + pb.command());
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.finer("[vtfcmd] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.log(Level.INFO, "Problem while generating png from vtf file", e);
        }
    }

    /**
     * List the files of a specified VPK file.
     * 
     * @param gamePath The {@link Path} where HL2 main executable for the
     *            selected Source game is located
     * @param vpkPath The <code>Path</code> where the VPK file to search is
     *            located
     * @return A {@link List} of <code>String</code>s of all the files inside
     *         the specified VPK file.
     */
    public List<String> getVpkContents(Path gamePath, Path vpkPath) {
        List<String> files = new ArrayList<>();
        try {
            Path vpktool = resolveVpkToolPath(gamePath);
            ProcessBuilder pb = new ProcessBuilder(vpktool.toString(), "l", vpkPath.toString());
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                files.add(line);
            }
            pr.waitFor();
            log.finer("[" + vpkPath.getFileName() + "] Contents scanned: " + files.size()
                    + " file(s)");
        } catch (InterruptedException | IOException e) {
            log.info("Problem retrieving contents of VPK file: " + vpkPath);
        }
        return files;
    }

    /**
     * Stop or kill the game process, whether it's being run from the tool or
     * not.
     * 
     * @see #getBuilderGameProcessKiller()
     */
    public void killGameProcess() {
        try {
            ProcessBuilder pb = getBuilderGameProcessKiller();
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.fine("[taskkill] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.info("Problem stopping game process");
        }
    }

    /**
     * Launch the selected game with some user-specified parameters.
     * 
     * @param cfg the program settings from it will retrieve values like the
     *            dxlevel and the resolution
     * @see #getBuilderStartGame()
     */
    public void startGame(Profiles profiles) {
        Profile profile = profiles.getProfile();
        SteamGame game = profiles.getGame();
        log.info("Starting " + game.getShortName() + " in " + profile.getResolution()
                + " with dxlevel " + profile.getDxlevel());
        ProcessBuilder pb = getBuilderStartGame();
        pb.command().addAll(profile.getDefaultLaunchOptions());
        pb.command().addAll(profile.getCustomLaunchOptions());
        log.finer("Launch options: " + pb.command());
        try {
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.finer("[steam] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.log(Level.INFO, "Process was interrupted", e);
        }
    }

    /**
     * Set the Look & Feel of the Graphical User Interface of the tool. By
     * default it uses the system L&F.
     */
    public void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not set the look and feel", e);
        }
    }

    /**
     * Use the Java Desktop API to open a folder of the given path.
     * 
     * @param path The folder that will be opened with the system file manager.
     *            If this is not a directory, it will open the file's parent.
     * @see Desktop#open(java.io.File)
     */
    public void openFolder(Path path) {
        Path parent = path.getParent();
        Path dir = Files.isDirectory(path) ? path : (parent != null ? parent : path);
        try {
            Desktop.getDesktop().open(dir.toFile());
        } catch (IOException e) {
            log.log(Level.INFO, "Could not open directory: " + dir, e);
        }
    }
}
