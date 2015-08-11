package com.github.lawena.files;

/**
 * The strategy used to backup user folders.
 *
 * @author Ivan
 */
public enum BackupMode {

    /**
     * Always perform a backup, regardless of size.
     */
    ALWAYS,
    /**
     * Ask the user to perform a backup when a folder exceeds a size threshold defined by the
     * profile key "<code>backup.warningSize</code>" in megabytes.
     */
    SOMETIMES,
    /**
     * Never perform a backup of your user and custom folders. (!)
     */
    NEVER,
    /**
     * Abort the launch and restore procedure.
     */
    ABORT;

    public static BackupMode from(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return BackupMode.SOMETIMES;
        }
    }

}
