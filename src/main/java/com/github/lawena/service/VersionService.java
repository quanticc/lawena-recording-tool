package com.github.lawena.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lawena.Messages;
import com.github.lawena.domain.Branch;
import com.github.lawena.domain.Build;
import com.github.lawena.domain.UpdateResult;
import com.github.lawena.task.DownloadTask;
import com.github.lawena.task.UpdateSetupTask;
import com.github.lawena.util.LwrtUtils;
import com.jcabi.manifests.Manifests;
import com.threerings.getdown.data.Resource;
import com.threerings.getdown.util.ConfigUtil;
import com.threerings.getdown.util.LaunchUtil;
import javafx.collections.ObservableList;
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
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.lawena.util.LwrtUtils.now;

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

    private final TaskService taskService;
    private final ObjectMapper mapper;

    private final File appDir = new File("").getAbsoluteFile();
    private final Map<String, String> version = new LinkedHashMap<>();
    private final Map<String, Object> getdown = new LinkedHashMap<>();
    private final List<Branch> branches = new ArrayList<>();
    private LocalDateTime lastCheck = LocalDateTime.now();
    private boolean standalone = false;

    @Autowired
    public VersionService(TaskService taskService, ObjectMapper mapper) {
        this.taskService = taskService;
        this.mapper = mapper;
    }

    @PostConstruct
    private void configure() {
        String implVersion = getManifestString(IMPLEMENTATION_VERSION, "custom-built");
        if (Files.exists(PROPERTIES_PATH)) {
            Properties gradle = new Properties();
            try {
                gradle.load(new FileInputStream(PROPERTIES_PATH.toFile()));
                implVersion = gradle.getProperty("version");
            } catch (IOException ignored) {
            }
        }
        version.put(IMPLEMENTATION_VERSION, implVersion);
        String implBuild = now("yyyyMMddHHmmss");
        String manifestImplBuild = getManifestString(IMPLEMENTATION_BUILD, implBuild);
        version.put(IMPLEMENTATION_BUILD, manifestImplBuild.matches("^[0-9]+$") ? manifestImplBuild : implBuild);
        version.put(GIT_DESCRIBE, getManifestString(GIT_DESCRIBE, implVersion));
        version.put(GIT_COMMIT, getManifestString(GIT_COMMIT, "?"));
        version.putAll(loadGitData());
        getdown.putAll(loadGetdown(GETDOWN_PATH));
        version.put(CURRENT_VERSION, getCurrentVersion());
        version.put(CURRENT_BRANCH, getCurrentBranchName());
        log.info("{}", version);
        deleteOutdatedResources();
        upgradeLauncher();
        upgradeGetdown();
        createExtraJvmArgsFile();
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

    private SortedSet<Build> buildSingleBranchMap(Branch branch) {
        SortedSet<Build> value = Optional.ofNullable(branch.getBuilds()).orElse(new TreeSet<>());
        if (needsDownload(branch)) {
            Resource resource = branchResource(branch);
            if (download(resource)) {
                value = readBuilds(resource);
            }
        }
        branch.setBuilds(value);
        return value;
    }

    private Map<Branch, SortedSet<Build>> buildBranchMap(List<Branch> branches) {
        // init our map with existing values if present
        Map<Branch, SortedSet<Build>> map = branches.stream()
                .collect(Collectors.toMap(Function.identity(),
                        b -> Optional.ofNullable(b.getBuilds()).orElse(new TreeSet<>())));
        // now collect all resources to download
        List<Resource> resources = map.keySet().stream()
                .filter(this::needsDownload)
                .map(this::branchResource)
                .collect(Collectors.toList());
        // do the deed
        DownloadTask task = new DownloadTask(resources);
        taskService.submitTask(task);
        ObservableList<Resource> result;
        try {
            result = task.get();
        } catch (InterruptedException | ExecutionException e) {
            log.info("Download was interrupted due to {}, retrieving partial results", e.toString());
            result = task.getPartialResults();
        }
        // successful downloads are inspected, get build data from them and map it
        result.forEach(resource ->
                map.entrySet().stream()
                        .filter(e -> e.getKey().getName().equalsIgnoreCase(resource.getPath() + "-builds.txt"))
                        .findAny()
                        .ifPresent(entry -> entry.setValue(readBuilds(resource))
                        )
        );
        // save the builds to their respective Branch mapping
        map.entrySet().stream().forEach(e -> e.getKey().setBuilds(e.getValue()));
        return map;
    }

    private boolean needsDownload(Branch branch) {
        if (branch == null)
            throw new IllegalArgumentException("Must set a branch");
        // download if not already cached, must not be standalone, must have a non-empty url
        return branch.getBuilds() == null
                && !branch.equals(Branch.STANDALONE)
                && branch.getUrl() != null && !branch.getUrl().isEmpty();
    }

    private Resource branchResource(Branch branch) {
        try {
            String name = branch.getName() + "-builds.txt";
            File local = new File(name).getAbsoluteFile();
            URL url = new URL(branch.getUrl() + name);
            return new Resource(local.getName(), url, local, false);
        } catch (MalformedURLException e) {
            log.warn("Invalid URL: {}", e.toString());
            return null;
        }
    }

    private SortedSet<Build> readBuilds(Resource res) {
        SortedSet<Build> builds = new TreeSet<>();
        File local = res.getLocal();
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
        return builds;
    }

//    private SortedSet<Build> getBuildList(Branch branch) {
//        if (branch == null)
//            throw new IllegalArgumentException("Must set a branch");
//        SortedSet<Build> builds = branch.getBuilds();
//        if (builds != null) {
//            return builds;
//        }
//        builds = new TreeSet<>();
//        if (branch.equals(Branch.STANDALONE)) {
//            return builds;
//        }
//        if (branch.getUrl() == null || branch.getUrl().isEmpty()) {
//            log.warn("Invalid url for branch {}", branch);
//            return builds;
//        }
//        String name = "buildlist.txt";
//        builds = new TreeSet<>();
//        try {
//            File local = new File(name).getAbsoluteFile();
//            URL url = new URL(branch.getUrl() + name);
//            Resource res = new Resource(local.getName(), url, local, false);
//            if (download(res)) {
//                try {
//                    for (String line : Files.readAllLines(local.toPath(), Charset.forName("UTF-8"))) {
//                        String[] data = line.split(";");
//                        if (data.length == 2) {
//                            builds.add(new Build(data[0], data[1], data[1], Long.parseLong(data[0])));
//                        } else if (data.length == 3) {
//                            builds.add(new Build(data[0], data[1], data[2], Long.parseLong(data[0])));
//                        } else {
//                            log.warn("Invalid build format: {}", Arrays.asList(data));
//                        }
//                    }
//                } catch (IOException e) {
//                    log.warn("Could not read lines from file: " + e);
//                }
//                res.erase();
//                try {
//                    Files.deleteIfExists(local.toPath());
//                } catch (IOException e) {
//                    log.warn("Could not delete file", e);
//                }
//            }
//        } catch (MalformedURLException e) {
//            log.warn("Invalid URL: " + e);
//        }
//        branch.setBuilds(builds);
//        return builds;
//    }

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

    private void createExtraJvmArgsFile() {
        Path path = Paths.get("extra.txt");
        if (!Files.exists(path)) {
            try {
                Files.write(path, Arrays.asList("#Custom Lawena and JVM arguments", "#-Xmx64m"), Charset.forName("UTF-8"),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                log.warn("Could not create example extra.txt file", e);
            }
        }
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
                    buildBranchMap(list.stream()
                            .filter(b -> b.getType() == Branch.Type.SNAPSHOT)
                            .collect(Collectors.toList())
                    );
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

    private boolean download(Resource res) {
        DownloadTask task = new DownloadTask(res);
        taskService.submitTask(task);
        try {
            return task.get().size() == 1;
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Download task failed: {}", e.toString());
            return false;
        }
    }

    public UpdateResult checkForUpdates() {
        Branch branch = getCurrentBranch();
        log.debug("Current build: {}/{}", branch.getName(), getVersion());
        SortedSet<Build> buildList = buildSingleBranchMap(branch);
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

    public Map<String, Object> getGetdown() {
        return getdown;
    }

    public String getAppbase() {
        String appbase = (String) getdown.get("appbase");
        // make sure there's a trailing slash
        if (!appbase.endsWith("/")) {
            appbase = appbase + "/";
        }
        return appbase;
    }

    public boolean upgradeInBackground(Build build) {
        log.info("Preparing files for update: {}", build);
        String appbase = (String) getdown.get("appbase");
        long version = build.getTimestamp();
        // make sure there's a trailing slash
        if (!appbase.endsWith("/")) {
            appbase = appbase + "/";
        }
        URL url = null;
        try {
            url = new URL(appbase.replace("%VERSION%", "" + version));
        } catch (MalformedURLException e) {
            log.warn("Bad url format: {}", e.toString());
            return false;
        }
        DownloadTask downloadTask = null;
        try {
            UpdateSetupTask setupTask = new UpdateSetupTask(getdown, url, version);
            taskService.submitTask(setupTask);
            downloadTask = new DownloadTask(setupTask.get());
            taskService.submitTask(downloadTask);
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Update operation was interrupted: {}", e.toString());
        }
        if (downloadTask != null) {
            ObservableList<Resource> successful;
            try {
                successful = downloadTask.get();
            } catch (InterruptedException | ExecutionException e) {
                log.info("Some files not downloaded due to {}. " +
                        "They will be downloaded upon application restart", e.toString());
                successful = downloadTask.getPartialResults();
            }
            log.info("Successfully downloaded: {}", successful);
        }
        return upgradeApplication(build);
    }


    public boolean upgradeApplication(Build build) {
        try {
            return LaunchUtil.updateVersionAndRelaunch(appDir, "getdown-client.jar", build.getName());
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
