package com.github.lawena.service;

import com.github.lawena.Messages;
import com.github.lawena.util.LaunchException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.lawena.event.LaunchStatusUpdateEvent.updateEvent;
import static org.eclipse.jgit.lib.Constants.*;

@Service
public class GitService {

    private static final Logger log = LoggerFactory.getLogger(GitService.class);

    static final String LAWENA_BRANCH_NAME = "lawena";
    static final String LAWENA_BRANCH_REF = R_HEADS + LAWENA_BRANCH_NAME;
    static final String PRE_LAUNCH_TAG = "last-pre-launch";
    static final String POST_LAUNCH_TAG = "last-post-launch";

    private final ValidationService validationService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public GitService(ValidationService validationService, ApplicationEventPublisher publisher) {
        this.validationService = validationService;
        this.publisher = publisher;
    }

    static Git openOrCreate(File gitDir, File directory) throws IOException, GitAPIException {
        Git git;
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder()
                .addCeilingDirectory(gitDir)
                .findGitDir(gitDir);
        if (repositoryBuilder.getGitDir() == null) {
            log.debug("Creating new repository: {}", directory);
            git = Git.init()
                    .setGitDir(gitDir)
                    .setDirectory(directory)
                    .setBare(false)
                    .call();
        } else {
            git = new Git(repositoryBuilder.build());
        }
        return git;
    }

    static Git openExisting(File gitDir) throws IOException, LaunchException {
        Git git;
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder()
                .addCeilingDirectory(gitDir)
                .findGitDir(gitDir);
        if (repositoryBuilder.getGitDir() == null) {
            throw new LaunchException("Invalid repository state");
        } else {
            git = new Git(repositoryBuilder.build());
        }
        return git;
    }

    public void setup() throws LaunchException {
        publisher.publishEvent(updateEvent(this, Messages.getString("ui.base.tasks.launch.setup")));
        Path gamePath = validationService.getGamePath();
        Path gitPath = gamePath.resolve(DOT_GIT);
        log.debug("[Setup] Checking repository: {}", gitPath);
        // Initial validation for gamePath - it should have been validated before anyway
        if (!Files.exists(gamePath) || !Files.isDirectory(gamePath)) {
            throw new LaunchException("Invalid game path: " + gamePath);
        }
        try (Git git = openOrCreate(gitPath.toFile(), gamePath.toFile())) {
            initialCommitIfHeadless(git);
            createBranchIfMissing(git);
            rollback(git); // ensures HEAD pointing to "lawena" branch
            commitAndUpdateTag(git, PRE_LAUNCH_TAG, "All user files that were changed until launching a game with Lawena"); // save user files
            git.checkout().setName(PRE_LAUNCH_TAG).call(); // detach HEAD so it points to last-pre-launch instead of lawena
            status(git);
        } catch (IOException | GitAPIException e) {
            throw new LaunchException("Failed to replace files", e);
        }
    }

    public void replace() throws LaunchException {
        publisher.publishEvent(updateEvent(this, Messages.getString("ui.base.tasks.launch.replacing")));
        Path gamePath = validationService.getGamePath();
        Path gitPath = gamePath.resolve(DOT_GIT);
        log.debug("[Replace] Checking repository: {}", gitPath);
        // Initial validation for gamePath - it should have been validated before anyway
        if (!Files.exists(gamePath) || !Files.isDirectory(gamePath)) {
            throw new LaunchException("Invalid game path: " + gamePath);
        }
        try (Git git = openExisting(gitPath.toFile())) {
            // must fail if there are no commits or missing branches
            // only advance if HEAD is detached @ PRE_LAUNCH_TAG
            String describe = Optional.ofNullable(git.describe().call()).orElse("");
            if (!describe.startsWith(PRE_LAUNCH_TAG)) {
                throw new LaunchException("Repository is not ready to save replaced files");
            }
            // TODO: allow optional full commit (don't use by default, takes a lot of time)
            //commitAndUpdateTag(git, POST_LAUNCH_TAG, "Files replaced by Lawena at the game folder"); // save launch files
            logAndUpdateTag(git, POST_LAUNCH_TAG, "Launched the game using Lawena"); // only commit a single file (for performance)
            status(git);
        } catch (IOException | GitAPIException e) {
            throw new LaunchException("Failed to replace files", e);
        }
    }

    public void restore() throws LaunchException {
        publisher.publishEvent(updateEvent(this, Messages.getString("ui.base.tasks.launch.restoring")));
        Path gamePath = validationService.getGamePath();
        Path gitPath = gamePath.resolve(DOT_GIT);
        log.debug("[Restore] Checking repository: {}", gitPath);
        // Initial validation for gamePath - it should have been validated before anyway
        if (!Files.exists(gamePath) || !Files.isDirectory(gamePath)) {
            throw new LaunchException("Invalid game path: " + gamePath);
        }
        try (Git git = openOrCreate(gitPath.toFile(), gamePath.toFile())) {
            initialCommitIfHeadless(git);
            createBranchIfMissing(git);
            rollback(git);
            status(git);
        } catch (IOException | GitAPIException e) {
            throw new LaunchException("Failed to restore files", e);
        }
    }

    void initialCommitIfHeadless(Git git) throws IOException, GitAPIException {
        Path workTreePath = git.getRepository().getWorkTree().toPath();
        Ref head = git.getRepository().getRef(HEAD);
        if (head == null || head.getObjectId() == null) {
            log.debug("Creating initial commit");
            List<String> initFile = Arrays.asList("Lawena Recording Tool", "Git repository for file handling operations");
            Files.createDirectories(workTreePath.resolve("lawena"));
            Files.write(workTreePath.resolve("lawena/README.txt"), initFile, Charset.forName("UTF-8"));
            git.add().addFilepattern("/lawena/README.txt").call(); // git add /lawena/README.txt
            git.commit().setCommitter("Lawena Recording Tool", "lawena@localhost").setMessage("Initial commit").call();
        }
    }

    void createBranchIfMissing(Git git) throws GitAPIException {
        boolean branchMissing = git.branchList().call().stream()
                .noneMatch(ref -> ref.getName().equals(R_HEADS + "lawena"));
        if (branchMissing) {
            log.debug("Creating lawena branch");
            git.branchCreate().setName("lawena").call();
        }
    }

    void rollback(Git git) throws IOException, GitAPIException, LaunchException {
        String fullBranch = git.getRepository().getFullBranch();
        String describe = Optional.ofNullable(git.describe().call()).orElse("");
        headStatus(git);
        if (fullBranch.startsWith(R_HEADS)) {
            log.debug("Repository in setup state");
            if (!fullBranch.equals(LAWENA_BRANCH_REF)) {
                // HEAD @ master or any other branch except lawena
                // file changes are still important, so no force or clean
                Ref ref = git.checkout().setName(LAWENA_BRANCH_NAME).call();
                log.debug("Checked out {}", ref);
            }
            // HEAD @ lawena
            // just make sure we have the right .gitignore before committing anything
            writeGitIgnore(git);
        } else {
            if (describe.startsWith(PRE_LAUNCH_TAG)) {
                // detached HEAD @ last-pre-launch
                log.debug("Repository is in pre-launch state");
                writeGitIgnore(git);
                // this is expensive and non essential, but still commit to be sure
                commitAndUpdateTag(git, PRE_LAUNCH_TAG, "Preparing repository for rollback after pre-launch");
            } else if (describe.startsWith(POST_LAUNCH_TAG)) {
                // detached HEAD @ last-post-launch
                // clean files not in index
                log.debug("Repository is in post-launch state");
                writeGitIgnore(git);
                Set<String> cleaned = git.clean().setCleanDirectories(true).call();
                cleaned.forEach(file -> log.debug("Cleaned: {}", file));
            } else {
                throw new LaunchException("Invalid repository state");
            }
            // return to original lawena branch
            Ref ref = git.checkout().setName(LAWENA_BRANCH_NAME).call();
            log.debug("Checked out {}", ref);
            git.checkout().setAllPaths(true).call(); // and checkout from index (git checkout -f lawena)
            writeGitIgnore(git);
            Set<String> cleaned = git.clean().setCleanDirectories(true).call(); // cleaning extra files not in index
            cleaned.forEach(file -> log.debug("Cleaned: {}", file));
        }
    }

    void writeGitIgnore(Git git) throws IOException {
        Path workTreePath = git.getRepository().getWorkTree().toPath();
        Path ignoreFile = workTreePath.resolve(DOT_GIT_IGNORE);
        List<String> ignoreLines = Arrays.asList("/*", "!.gitignore", "!/cfg/", "!/custom/", "!/lawena/");
        Files.createDirectories(ignoreFile.getParent());
        Files.write(ignoreFile, ignoreLines, Charset.forName("UTF-8"));
    }

    void commitAndUpdateTag(Git git, String tagName, String commitMessage) throws GitAPIException, IOException {
        long start = System.currentTimeMillis();
        if (shortStatusMap(git).isEmpty()) {
            log.debug("Working directory is clean, skipping commit");
        } else {
            git.add().addFilepattern(".").call(); // add new and modified files since last commit: git add .
            git.add().setUpdate(true).addFilepattern(".").call(); // add deleted files: git add -u .
            git.commit()
                    .setCommitter("Lawena Recording Tool", "lawena@localhost")
                    .setMessage(commitMessage)
                    .call();
            Ref tag = git.tag()
                    .setForceUpdate(true)
                    .setName(tagName)
                    .call();
            log.debug("Updated tag: {}", tag);
        }
        log.debug("Commit operation took {} ms", System.currentTimeMillis() - start);
    }

    void logAndUpdateTag(Git git, String tagName, String commitMessage) throws GitAPIException, IOException {
        Path workTreePath = git.getRepository().getWorkTree().toPath();
        String filePattern = "lawena/replace-log.txt";
        Path ignoreFile = workTreePath.resolve(filePattern);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<String> ignoreLines = Collections.singletonList(now + " Replacing Lawena files");
        Files.createDirectories(ignoreFile.getParent());
        Files.write(ignoreFile, ignoreLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        git.add().addFilepattern(filePattern).call(); // add new and modified files since last commit: git add .
        git.commit()
                .setCommitter("Lawena Recording Tool", "lawena@localhost")
                .setMessage(commitMessage)
                .call();
        Ref tag = git.tag()
                .setForceUpdate(true)
                .setName(tagName)
                .call();
        log.debug("Updated tag: {}", tag);
    }

    private Map<String, StatusColumns> shortStatusMap(Git git) throws GitAPIException {
        Status status = git.status().call();
        Map<String, StatusColumns> result = new TreeMap<>();
        //status.getUncommittedChanges().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).staged = '?');
        //status.getUntracked().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).unstaged = '?');
        status.getUntrackedFolders().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).set("??"));
        status.getAdded().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).staged = 'A');
        status.getChanged().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).staged = 'M');
        status.getModified().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).unstaged = 'M');
        status.getMissing().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).unstaged = 'D');
        status.getRemoved().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).staged = 'D');
        status.getConflicting().stream().forEach(f -> result.computeIfAbsent(f, k -> new StatusColumns()).staged = 'X');
        return result;
    }

    private void status(Git git) throws GitAPIException, IOException {
        Map<String, StatusColumns> result = shortStatusMap(git);
        StringBuilder builder = new StringBuilder();
        result.forEach((f, s) -> builder.append('\n').append(s.staged).append(s.unstaged).append(" ").append(f));
        headStatus(git);
        if (result.isEmpty()) {
            log.debug("Nothing to commit, working directory clean");
        } else {
            log.debug("Uncommitted changes: {}", builder.toString());
        }
    }

    private void headStatus(Git git) throws IOException, GitAPIException {
        String branch = git.getRepository().getBranch();
        String fullBranch = git.getRepository().getFullBranch();
        String describe = Optional.ofNullable(git.describe().call()).orElse("");
        String resolved = git.getRepository().resolve(fullBranch).getName();
        String headInfo = fullBranch.startsWith(R_HEADS) ? "on branch " + branch : "detached";
        log.debug("HEAD {} [{}] {}", headInfo, resolved.substring(0, 7), fullBranch.startsWith(R_HEADS) || describe.isEmpty() ? "" : "(" + describe + ")");
    }

    private static class StatusColumns {
        private char staged = ' ';
        private char unstaged = ' ';

        public void set(String status) {
            if (status.length() > 0) {
                this.staged = status.charAt(0);
            }
            if (status.length() > 1) {
                this.unstaged = status.charAt(1);
            }
        }
    }
}
