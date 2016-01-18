package com.github.lawena.task;

import com.github.lawena.Messages;
import com.github.lawena.util.FXUtils;
import com.github.lawena.util.StringUtils;
import com.threerings.getdown.data.Resource;
import com.threerings.getdown.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class UpdateSetupTask extends LawenaTask<List<Resource>> {

    private static final Logger log = LoggerFactory.getLogger(UpdateSetupTask.class);
    private static final String DIGEST_FILE = "digest.txt";
    private static final File APP_DIR = new File("").getAbsoluteFile();
    private static final File TEMP_DIR = new File(APP_DIR, "tmp");

    private final Map<String, Object> properties;
    private final URL appbase;
    private final long version;

    private Map<String, String> digests = new HashMap<>();
    private String metaDigest = "";

    public UpdateSetupTask(Map<String, Object> properties, URL appbase, long version) {
        this.properties = properties;
        this.appbase = appbase;
        this.version = version;
    }

    @Override
    protected List<Resource> call() throws Exception {
        updateTitle(Messages.getString("ui.tasks.update.title"));
        updateMessage(Messages.getString("ui.tasks.update.setupMessage"));

        List<Resource> resources = new ArrayList<>();
        parseResources("code", false, resources);
        parseResources("ucode", true, resources);
        parseResources("resource", false, resources);
        parseResources("uresource", true, resources);
        for (String auxgroup : parseList("auxgroups")) {
            if (isAuxGroupActive(auxgroup)) {
                parseResources(auxgroup + ".code", false, resources);
                parseResources(auxgroup + ".ucode", true, resources);
                parseResources(auxgroup + ".resource", false, resources);
                parseResources(auxgroup + ".uresource", true, resources);
            }
        }

        updateMessage(Messages.getString("ui.tasks.update.verifyMessage"));
        // get the target digest.txt file - we will be checking CURRENT resources against this
        File tempVersionDir = new File(TEMP_DIR, "" + version);
        Resource digest = new Resource(DIGEST_FILE, new URL(appbase, DIGEST_FILE), new File(tempVersionDir, DIGEST_FILE), false);
        DownloadTask subTask = new DownloadTask(digest);
        new Thread(subTask).start();
        boolean gotDigest;
        try {
            gotDigest = subTask.get().size() == 1;
        } catch (InterruptedException | ExecutionException e) {
            gotDigest = subTask.getPartialResults().size() == 1;
        }
        // if the digest.txt can't be downloaded or is invalid, all files will be downloaded
        if (gotDigest) {
            StringBuilder data = new StringBuilder();
            File digestFile = digest.getLocal();
            for (String[] pair : ConfigUtil.parsePairs(digestFile, false)) {
                if (pair[0].equals(DIGEST_FILE)) {
                    metaDigest = pair[1];
                    break;
                }
                digests.put(pair[0], pair[1]);
                data.append(pair[0]).append(" = ").append(pair[1]).append("\n");
            }
            MessageDigest md = getMessageDigest();
            byte[] contents = data.toString().getBytes("UTF-8");
            String md5 = StringUtils.hexlate(md.digest(contents));
            if (!md5.equals(metaDigest)) {
                throw new IOException(String.format("Invalid digest file: computed=%s expected=%s", md5, metaDigest));
            }
            digest.erase();
        }

        // determine what resources do not match the given version, then
        // transform failures into resources with new destination (tmp/versionNumber/resourcePath)
        return verifyResources(resources).stream()
                .map(r -> new Resource(r.getPath(), r.getRemote(), new File(tempVersionDir, r.getPath()), r.shouldUnpack()))
                .collect(Collectors.toList());
    }

    private void parseResources(String name, boolean unpack, List<Resource> list) {
        String[] resources = ConfigUtil.getMultiValue(properties, name);
        if (resources == null) {
            return;
        }
        for (String resource : resources) {
            try {
                list.add(createResource(resource, unpack));
            } catch (Exception e) {
                log.warn("Invalid resource '{}'. {}", resource, e.toString());
            }
        }
    }

    private Resource createResource(String path, boolean unpack)
            throws MalformedURLException {
        URL remoteUrl = new URL(appbase, path.replace(" ", "%20"));
        return new Resource(path, remoteUrl, new File(APP_DIR, path), unpack);
    }

    private String[] parseList(String name) {
        String value = (String) properties.get(name);
        return (value == null) ? new String[0] : StringUtils.parseStringArray(value);
    }

    public boolean isAuxGroupActive(String auxgroup) {
        return new File(APP_DIR, auxgroup + ".dat").exists();
    }

    private List<Resource> verifyResources(List<Resource> activeResources) throws InterruptedException {
        List<Resource> failures = new ArrayList<>();
        // total up the file size of the resources to validate
        long totalSize = activeResources.stream().map(Resource::getLocal).map(File::length).reduce(0L, Long::sum);
        updateProgress(0L, totalSize);
        for (Resource rsrc : activeResources) {
            if (isCancelled()) {
                throw new InterruptedException("Task cancelled");
            }
            updateMessage(Messages.getString("ui.tasks.update.validatingMessage", rsrc.getPath()));
            long partialProgress = rsrc.getLocal().length();
            try {
                if (validateResource(rsrc)) {
                    continue;
                }
            } catch (Exception e) {
                log.info("Failure validating resource {}. {}", rsrc, e.toString());
            } finally {
                incrementProgress(partialProgress, totalSize);
            }
            failures.add(rsrc);
        }
        return failures;
    }

    private void incrementProgress(long by, long total) {
        updateProgress(FXUtils.ensureRunAndGet(() -> (long) getProgress() + by, by), total);
    }

    /**
     * Computes the MD5 hash of the specified resource and compares it with the value parsed from
     * the digest file. Logs a message if the resource fails validation.
     *
     * @return true if the resource is valid, false if it failed the digest check or if an I/O
     * error was encountered during the validation process.
     */
    private boolean validateResource(Resource resource) {
        try {
            String computed = resource.computeDigest(getMessageDigest(), null);
            String expected = digests.get(resource.getPath());
            if (computed.equals(expected)) {
                return true;
            }
            log.info("Resource failed digest check: {} (computed='{}', expected='{}')", resource, computed, expected);
        } catch (Throwable t) {
            log.info("Resource failed digest check: {} (exception='{}')", resource, t.toString());
        }
        return false;
    }

    /**
     * Obtains an appropriate message digest instance for use by the Getdown system.
     */
    public static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("JVM does not support MD5. Huh?");
        }
    }
}
