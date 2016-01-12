package com.github.lawena.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lawena.Messages;
import com.github.lawena.domain.Branch;
import com.github.lawena.domain.Build;
import com.github.lawena.domain.UpdateResult;
import com.github.lawena.util.LwrtUtils;
import com.jcabi.manifests.Manifests;
import com.threerings.getdown.data.Resource;
import com.threerings.getdown.net.Downloader;
import com.threerings.getdown.net.HTTPDownloader;
import com.threerings.getdown.util.ConfigUtil;
import com.threerings.getdown.util.LaunchUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VersionService {

    private static final Logger log = LoggerFactory.getLogger(VersionService.class);
    private static final String DEFAULT_BRANCHES = "https://dl.dropboxusercontent.com/u/74380/lwrt/5/channels.json";
    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";
    private static final String IMPLEMENTATION_BUILD = "Implementation-Build";
    private static final String GIT_DESCRIBE = "Git-Describe";
    private static final String GIT_COMMIT = "Git-Commit";
    private static final String CURRENT_VERSION = "Current-Version";
    private static final String CURRENT_BRANCH = "Current-Branch";
    private static final Path GETDOWN_PATH = Paths.get("getdown.txt");
    private static final Path PROPERTIES_PATH = Paths.get("gradle.properties");

    private final ObjectMapper mapper;
    private final Map<String, String> version = new LinkedHashMap<>();
    private final Map<String, Object> getdown = new LinkedHashMap<>();
    private final List<Branch> branches = new ArrayList<>();
    private LocalDateTime lastCheck = LocalDateTime.now();
    private boolean standalone = false;

    @Autowired
    public VersionService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    private void configure() {
        String versionFallback = "custom-built";
        if (Files.exists(PROPERTIES_PATH)) {
            Properties gradle = new Properties();
            try {
                gradle.load(new FileInputStream(PROPERTIES_PATH.toFile()));
                versionFallback = gradle.getProperty("version");
            } catch (IOException ignored) {
            }
        }
        version.put(IMPLEMENTATION_VERSION, getManifestString(IMPLEMENTATION_VERSION, versionFallback));
        version.put(IMPLEMENTATION_BUILD, getManifestString(IMPLEMENTATION_BUILD, "?"));
        version.put(GIT_DESCRIBE, getManifestString(GIT_DESCRIBE, version.get(IMPLEMENTATION_VERSION)));
        version.put(GIT_COMMIT, getManifestString(GIT_COMMIT, "?"));
        version.putAll(loadGitData());
        getdown.putAll(loadGetdown(GETDOWN_PATH));
        version.put(CURRENT_VERSION, getCurrentVersion());
        version.put(CURRENT_BRANCH, getCurrentBranchName());
        log.info("{}", version);
    }

    private String getManifestString(String key, String defaultValue) {
        return Manifests.exists(key) ? Manifests.read(key) : defaultValue;
    }

    private Map<String, String> loadGitData() {
        Map<String, String> result = new HashMap<>();
        File gitDir = new File(".git");
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder()
                    .addCeilingDirectory(gitDir)
                    .findGitDir(gitDir);
            if (repositoryBuilder.getGitDir() != null) {
                Git git = new Git(repositoryBuilder.build());
                String describe = git.describe().call();
                if (describe != null) {
                    result.put(GIT_DESCRIBE, describe);
                }
                String commit = git.getRepository().resolve("HEAD").getName();
                result.put(GIT_COMMIT, commit.substring(0, 7));
            }
        } catch (IOException | GitAPIException e) {
            log.debug("Could not retrieve Git repository info: {}", e.toString());
        }
        return result;
    }

    private Map<String, Object> loadGetdown(Path path) {
        try {
            return ConfigUtil.parseConfig(path.toFile(), false);
        } catch (IOException e) {
            standalone = true;
            return Collections.emptyMap();
        }
    }

    public String getVersion() {
        return version.get(CURRENT_VERSION);
    }

    public String getBranch() {
        return version.get(CURRENT_BRANCH);
    }

    public String getImplementationVersion() {
        return version.get(IMPLEMENTATION_VERSION);
    }

    public String getImplementationBuild() {
        return version.get(IMPLEMENTATION_BUILD);
    }

    public String getGitDescribe() {
        return version.get(GIT_DESCRIBE);
    }

    public String getGitCommit() {
        return version.get(GIT_COMMIT);
    }

    public void clear() {
        branches.clear();
    }

    private static SortedSet<Build> getBuildList(Branch branch) {
        if (branch == null)
            throw new IllegalArgumentException("Must set a branch");
        SortedSet<Build> builds = branch.getBuilds();
        if (builds != null) {
            return builds;
        }
        builds = new TreeSet<>();
        if (branch.equals(Branch.STANDALONE)) {
            return builds;
        }
        if (branch.getUrl() == null || branch.getUrl().isEmpty()) {
            log.warn("Invalid url for branch {}", branch);
            return builds;
        }
        String name = "buildlist.txt";
        builds = new TreeSet<>();
        try {
            File local = new File(name).getAbsoluteFile();
            URL url = new URL(branch.getUrl() + name);
            Resource res = new Resource(local.getName(), url, local, false);
            if (download(res)) {
                try {
                    for (String line : Files.readAllLines(local.toPath(), Charset.forName("UTF-8"))) {
                        String[] data = line.split(";");
                        if (data.length == 2) {
                            builds.add(new Build(data[0], data[1], data[1], Long.parseLong(data[0])));
                        } else if (data.length == 3) {
                            builds.add(new Build(data[0], data[1], data[2], Long.parseLong(data[0])));
                        } else {
                            log.warn("Invalid build format: {}", Arrays.asList(data));
                        }
                    }
                } catch (IOException e) {
                    log.warn("Could not read lines from file: " + e);
                }
                res.erase();
                try {
                    Files.deleteIfExists(local.toPath());
                } catch (IOException e) {
                    log.warn("Could not delete file", e);
                }
            }
        } catch (MalformedURLException e) {
            log.warn("Invalid URL: " + e);
        }
        branch.setBuilds(builds);
        return builds;
    }

    /**
     * Retrieves the current development branch the installation is in. This method might trigger
     * {@link #getBranches()} to update available branches
     *
     * @return the current {@link Branch}
     */
    public Branch getCurrentBranch() {
        String branchName = getCurrentBranchName();
        for (Branch branch : getBranches()) {
            if (branch.getName().equals(branchName)) {
                return branch;
            }
        }
        return Branch.STANDALONE;
    }

    public String getCurrentBranchName() {
        String[] value = getMultiValue(getdown, "channel");
        if (value.length == 0)
            return "standalone";
        return value[0];
    }

    private String getCurrentVersion() {
        String[] value = getMultiValue(getdown, "version");
        if (value.length == 0)
            return "0";
        return value[0];
    }

    private void deleteOutdatedResources() {
        String[] toDelete = getMultiValue(getdown, "delete");
        for (String path : toDelete) {
            try {
                if (Files.deleteIfExists(Paths.get(path))) {
                    log.debug("Deleted outdated file: " + path);
                }
            } catch (IOException e) {
                log.warn("Could not delete outdated file", e);
            }
        }
    }

    private static String[] getMultiValue(Map<String, Object> data, String name) {
        // safe way to call this and avoid NPEs
        String[] array = ConfigUtil.getMultiValue(data, name);
        if (array == null)
            return new String[0];
        return array;
    }

    private static void upgrade(String desc, File oldgd, File curgd, File newgd) {
        if (!newgd.exists() || newgd.length() == curgd.length()
                || LwrtUtils.compareCreationTime(newgd, curgd) == 0) {
            log.debug("Resource {} is up to date", desc);
            return;
        }
        log.info("Upgrade {} with {}...", desc, newgd);
        try {
            Files.deleteIfExists(oldgd.toPath());
        } catch (IOException e) {
            log.warn("Could not delete old path: " + e);
        }
        if (!curgd.exists() || curgd.renameTo(oldgd)) {
            if (newgd.renameTo(curgd)) {
                try {
                    Files.deleteIfExists(oldgd.toPath());
                } catch (IOException e) {
                    log.warn("Could not delete old path: " + e);
                }
                try (InputStream in = new FileInputStream(curgd);
                     OutputStream out = new FileOutputStream(newgd)) {
                    LwrtUtils.copy(in, out);
                } catch (IOException e) {
                    log.warn("Problem copying {} back: {}", desc, e);
                }
                return;
            }
            log.warn("Unable to rename to {}", oldgd);
            if (!oldgd.renameTo(curgd)) {
                log.warn("Could not rename {} to {}", oldgd, curgd);
            }
        }
        log.info("Attempting to upgrade by copying over " + curgd + "...");
        try (InputStream in = new FileInputStream(newgd);
             OutputStream out = new FileOutputStream(curgd)) {
            LwrtUtils.copy(in, out);
        } catch (IOException e) {
            log.warn("Brute force copy method also failed", e);
        }
    }

    private static void upgradeLauncher() {
        File oldgd = new File("../lawena-old.exe");
        File curgd = new File("../lawena.exe");
        File newgd = new File("code/lawena-new.exe");
        upgrade("Lawena launcher", oldgd, curgd, newgd);
    }

    private static void upgradeGetdown() {
        File oldgd = new File("getdown-client-old.jar");
        File curgd = new File("getdown-client.jar");
        File newgd = new File("code/getdown-client-new.exe");
        upgrade("Lawena updater", oldgd, curgd, newgd);
    }

    public List<Branch> getBranches() {
        if (branches.isEmpty()) {
            branches.addAll(loadBranches());
        }
        return branches;
    }

    private List<Branch> loadBranches() {
        String[] value = getMultiValue(getdown, "channels");
        String url = value.length > 0 ? value[0] : DEFAULT_BRANCHES;
        File file = new File("channels.json").getAbsoluteFile();
        List<Branch> list = Collections.emptyList();
        try {
            Resource res = new Resource(file.getName(), new URL(url), file, false);
            lastCheck = LocalDateTime.now();
            if (download(res)) {
                try (Reader reader = Files.newBufferedReader(file.toPath(), Charset.forName("UTF-8"))) {
                    TypeReference token = new TypeReference<List<Branch>>() {
                    };
                    list = mapper.readValue(reader, token);
                    for (Branch branch : list) {
                        if (branch.getType() == Branch.Type.SNAPSHOT) {
                            getBuildList(branch);
                        }
                    }
                    log.debug("Found: {}", list);
                } catch (FileNotFoundException e) {
                    log.info("No latest version file found");
                } catch (IOException e) {
                    log.warn("Invalid latest version file found: {}", e.toString());
                }
                res.erase();
            }
        } catch (MalformedURLException e) {
            log.warn("Invalid URL: " + e);
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.debug("Could not delete branches file: " + e);
        }
        return list;
    }

    private static boolean download(Resource... res) {
        return new HTTPDownloader(Arrays.asList(res), new Downloader.Observer() {

            @Override
            public void resolvingDownloads() {
            }

            @Override
            public boolean downloadProgress(int percent, long remaining) {
                return !Thread.currentThread().isInterrupted();
            }

            @Override
            public void downloadFailed(Resource rsrc, Exception e) {
                log.warn("Download failed: {}", e.toString());
            }
        }).download();
    }

    public void fileCleanup() {
        deleteOutdatedResources();
        upgradeLauncher();
        upgradeGetdown();
    }

    public UpdateResult checkForUpdates() {
        Branch branch = getCurrentBranch();
        log.debug("Current build: {}/{}", branch.getName(), getVersion());
        SortedSet<Build> buildList = getBuildList(branch);
        if (buildList.isEmpty()) {
            return UpdateResult.notFound(Messages.getString("ui.updates.noUpdatesFound"));
        }
        Build latest = buildList.first();
        try {
            long current = Long.parseLong(getCurrentVersion());
            if (current < latest.getTimestamp()) {
                return UpdateResult.found(latest, Messages.getString("ui.updates.newVersionAvailable", latest.getVersion()));
            } else {
                return UpdateResult.latest(Messages.getString("ui.updates.hasLatestVersion"));
            }
        } catch (NumberFormatException e) {
            log.warn("Bad version format: {}", getCurrentVersion());
            return UpdateResult.found(latest, Messages.getString("ui.updates.newVersionAvailable", latest.getVersion()));
        }
    }

    public boolean upgradeApplication(Build build) {
        try {
            return LaunchUtil.updateVersionAndRelaunch(new File("").getAbsoluteFile(),
                    "getdown-client.jar", build.getName());
        } catch (IOException e) {
            log.warn("Could not complete the upgrade", e);
        }
        return false;
    }

    /**
     * A standalone installation means that no deployment descriptor file was found on the application
     * folder.
     *
     * @return <code>true</code> if this install is standalone or <code>false</code> if it is not
     */
    public boolean isStandalone() {
        return standalone;
    }

    public boolean createVersionFile(String version) {
        Path path = Paths.get("version.txt");
        try {
            path = Files.write(path, Collections.singletonList(version), Charset.defaultCharset());
            log.debug("Version file created at {}", path.toAbsolutePath());
            return true;
        } catch (IOException e) {
            log.warn("Could not create version file", e);
            return false;
        }
    }

    public LocalDateTime getLastCheck() {
        return lastCheck;
    }

    public void switchBranch(Branch newBranch) throws IOException {
        String appbase = newBranch.getUrl() + "latest/";
        try {
            Files.copy(GETDOWN_PATH, Paths.get("getdown.bak.txt"));
        } catch (IOException e) {
            log.warn("Could not backup updater metadata file", e);
        }
        List<String> lines = new ArrayList<>();
        lines.add("appbase = " + appbase);
        try {
            for (String line : Files.readAllLines(GETDOWN_PATH, Charset.forName("UTF-8"))) {
                if (line.startsWith("ui.")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            log.warn("Could not read current updater metadata file", e);
        }
        Files.write(GETDOWN_PATH, lines, Charset.defaultCharset());
        log.info("New updater metadata file created");
    }
}
