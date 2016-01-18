package com.github.lawena.task;

import com.github.lawena.Messages;
import com.github.lawena.util.FXUtils;
import com.github.lawena.util.LwrtUtils;
import com.threerings.getdown.data.Resource;
import com.threerings.getdown.util.ConnectionUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Task to download a collection of resources from a HTTP server, providing progress report through properties.
 * Based on Downloader class from Getdown (com.threerings.getdown.net.Downloader)
 */
public class DownloadTask extends LawenaTask<ObservableList<Resource>> {

    private static final Logger log = LoggerFactory.getLogger(DownloadTask.class);
    protected static final long UPDATE_DELAY = 500L;

    protected final List<Resource> resources;
    protected final Map<Resource, Long> sizes = new HashMap<>();
    protected final Map<Resource, Long> downloaded = new HashMap<>();
    protected final byte[] buffer = new byte[4096];
    protected long start;
    protected long lastUpdate;

    private final SmoothValue smoothTime = new SmoothValue();
    private final SmoothValue smoothRate = new SmoothValue();

    protected final ReadOnlyObjectWrapper<ObservableList<Resource>> partialResults =
            new ReadOnlyObjectWrapper<>(FXCollections.observableArrayList(new ArrayList<>()));
    protected final ReadOnlyLongWrapper downloadedSize = new ReadOnlyLongWrapper(1L);
    protected final ReadOnlyLongWrapper totalSize = new ReadOnlyLongWrapper(1L);
    protected final ReadOnlyLongWrapper seconds = new ReadOnlyLongWrapper(1L);
    protected final ReadOnlyLongWrapper bytesPerSecond = new ReadOnlyLongWrapper(0L);
    protected final ReadOnlyIntegerWrapper percentCompleted = new ReadOnlyIntegerWrapper(0);
    protected final ReadOnlyLongWrapper remainingSeconds = new ReadOnlyLongWrapper(0L);

    public DownloadTask(Resource resource) {
        this(Collections.singletonList(resource));
    }

    public DownloadTask(List<Resource> resources) {
        this.resources = resources;
        initMetaProperties();
    }

    private void initMetaProperties() {
        bytesPerSecond.bind(
                Bindings.when(seconds.isEqualTo(0L)).then(0L)
                        .otherwise(downloadedSize.divide(seconds))
        );
        percentCompleted.bind(Bindings.min(100,
                Bindings.when(totalSize.isEqualTo(0L)).then(0)
                        .otherwise(downloadedSize.multiply(100f).divide(totalSize))
                )
        );
        remainingSeconds.bind(
                Bindings.when(bytesPerSecond.lessThanOrEqualTo(0L).or(totalSize.isEqualTo(0L))).then(-1L)
                        .otherwise(totalSize.subtract(downloadedSize).divide(bytesPerSecond))
        );
    }

    @Override
    protected ObservableList<Resource> call() throws Exception {
        updateTitle(Messages.getString("ui.tasks.download.title"));
        updateMessage(Messages.getString("ui.tasks.download.resolvingMessage"));
        // first compute the total size of our download
        for (Resource resource : resources) {
            discoverSize(resource);
        }

        // make a note of the time at which we started the download
        start = System.currentTimeMillis();

        // now actually download the files
        for (Resource resource : resources) {
            download(resource);
        }

        ObservableList<Resource> result = partialResults.get();
        log.debug("Downloaded resources: {} {}", result.size(), result);
        return result;
    }

    @Override
    protected void done() {
        CompletableFuture.runAsync(
                () -> log.debug("Download task finished with status: {}", getState()),
                Platform::runLater);
    }

    protected void download(Resource rsrc) throws InterruptedException {
        // make sure the resource's target directory exists
        File parent = new File(rsrc.getLocal().getParent());
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                log.warn("Failed to create target directory for resource '{}'. Download will certainly fail.", rsrc);
            }
        }
        try {
            doDownload(rsrc);
        } catch (IOException e) {
            log.warn("Unable to download {}: {}", rsrc, e.toString());
        }
    }

    protected void doDownload(Resource rsrc) throws IOException, InterruptedException {
        // download the resource from the specified URL
        URLConnection conn = ConnectionUtil.open(rsrc.getRemote());
        conn.connect();

        // make sure we got a satisfactory response code
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection hcon = (HttpURLConnection) conn;
            if (hcon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unable to download resource " + rsrc.getRemote() + ": " +
                        hcon.getResponseCode());
            }
        }

        long actualSize = conn.getContentLength();
        log.info("Downloading: {} ({})", rsrc.getRemote(), humanizeBytes(actualSize));
        long currentSize = 0L;
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(rsrc.getLocal())) {

            int read;
            // read in the file data
            while ((read = in.read(buffer)) != -1) {
                // write it out to our local copy
                out.write(buffer, 0, read);
                // note that we've downloaded some data
                currentSize += read;

                updateObserver(rsrc, currentSize, actualSize);
                long eta = remainingSeconds.get();
                long bps = bytesPerSecond.get();
                String status = String.format("%s (%s %s %s%s) %s", rsrc.getPath(), humanizeBytes(currentSize),
                        Messages.getString("ui.tasks.download.of"), humanizeBytes(actualSize),
                        bps <= 0 ? "" : String.format(" @ %s/s", humanizeRate(bps)),
                        eta <= 0 ? "" : humanizeTime(eta));
                updateMessage(status);
            }
            FXUtils.runAndWait(() -> partialResults.get().add(rsrc));
        }
    }

    private String humanizeTime(long totalSeconds) {
        Duration duration = Duration.ofSeconds(smoothTime.smooth(totalSeconds));
        return Messages.getString("ui.tasks.download.remaining", LwrtUtils.shortFormatDuration(duration));
    }

    private String humanizeRate(long bps) {
        return LwrtUtils.humanizeBytes(smoothRate.smooth(bps));
    }

    private String humanizeBytes(long bytes) {
        return LwrtUtils.humanizeBytes(bytes);
    }

    /**
     * Notes the amount of data needed to download the given resource..
     */
    protected void discoverSize(Resource rsrc) throws IOException {
        sizes.put(rsrc, Math.max(checkSize(rsrc), 0L));
    }

    protected long checkSize(Resource rsrc) throws IOException {
        URLConnection conn = ConnectionUtil.open(rsrc.getRemote());
        try {
            // if we're accessing our data via HTTP, we only need a HEAD request
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection hcon = (HttpURLConnection) conn;
                hcon.setRequestMethod("HEAD");
                hcon.connect();
                // make sure we got a satisfactory response code
                if (hcon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Unable to check up-to-date for " +
                            rsrc.getRemote() + ": " + hcon.getResponseCode());
                }
            }
            return conn.getContentLength();
        } finally {
            // let it be known that we're done with this connection
            conn.getInputStream().close();
        }
    }

    /**
     * Periodically called by the protocol-specific downloaders to update their progress. This
     * should be called at least once for each resource to be downloaded, with the total downloaded
     * size for that resource. It can also be called periodically along the way for each resource
     * to communicate incremental progress.
     *
     * @param rsrc        the resource currently being downloaded.
     * @param currentSize the number of bytes currently downloaded for said resource.
     * @param actualSize  the size reported for this resource now that we're actually downloading
     *                    it. Some web servers lie about Content-length when doing a HEAD request, so by reporting
     *                    updated sizes here we can recover from receiving bogus information in the earlier {@link
     *                    #checkSize} phase.
     */
    protected void updateObserver(Resource rsrc, long currentSize, long actualSize) throws IOException, InterruptedException {
        sizes.put(rsrc, actualSize = Math.max(actualSize, sizes.get(rsrc)));
        downloaded.put(rsrc, Math.min(actualSize, currentSize));

        // notify the observer if it's been sufficiently long since our last notification
        long now = System.currentTimeMillis();
        if ((now - lastUpdate) >= UPDATE_DELAY) {
            lastUpdate = now;
            seconds.set((now - start) / 1000L);
            totalSize.set(sizes.values().stream().reduce(0L, Long::sum));
            downloadedSize.set(downloaded.values().stream().reduce(0L, Long::sum));
            if (isCancelled()) {
                throw new InterruptedException();
            }
            updateProgress(percentCompleted.get(), 100);
        }
    }

    public final ObservableList<Resource> getPartialResults() {
        return partialResults.get();
    }

    public final ReadOnlyObjectProperty<ObservableList<Resource>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
    }

    public final ReadOnlyLongProperty downloadedSizeProperty() {
        return downloadedSize.getReadOnlyProperty();
    }

    public final ReadOnlyLongProperty totalSizeProperty() {
        return totalSize.getReadOnlyProperty();
    }

    public ReadOnlyLongProperty bytesPerSecondProperty() {
        return bytesPerSecond.getReadOnlyProperty();
    }

    public ReadOnlyLongProperty remainingSecondsProperty() {
        return remainingSeconds.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty percentCompletedProperty() {
        return percentCompleted.getReadOnlyProperty();
    }

    static class SmoothValue {
        private int attempt = 0;
        private long[] remains = new long[4];
        private long lastMessageUpdate = 0;
        private long lastMessageUpdateAttempt = 0;

        public long smooth(long input) {
            lastMessageUpdateAttempt = System.currentTimeMillis();
            if (lastMessageUpdateAttempt - lastMessageUpdate >= 500L) {
                remains[attempt++ % remains.length] = input;
                lastMessageUpdate = lastMessageUpdateAttempt;
            }
            long smooth = 0;
            int values = Math.min(attempt, remains.length);
            for (int ii = 0; ii < values; ii++) {
                smooth += remains[ii];
            }
            smooth /= values;
            return smooth;
        }
    }
}
