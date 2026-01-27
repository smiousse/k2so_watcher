package com.k2so.watcher.service;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

@Service
public class SambaBackupService {

    private static final Logger logger = LoggerFactory.getLogger(SambaBackupService.class);

    private final AppSettingsService appSettingsService;

    public SambaBackupService(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    /**
     * Check if Samba backup is enabled and configured
     */
    public boolean isEnabled() {
        return "true".equalsIgnoreCase(appSettingsService.getSettingValue("backup.samba.enabled", "false"))
                && !getHost().isEmpty()
                && !getShare().isEmpty()
                && !getUsername().isEmpty();
    }

    /**
     * Get Samba configuration values
     */
    public String getHost() {
        return appSettingsService.getSettingValue("backup.samba.host", "");
    }

    public String getShare() {
        return appSettingsService.getSettingValue("backup.samba.share", "");
    }

    public String getPath() {
        return appSettingsService.getSettingValue("backup.samba.path", "");
    }

    public String getDomain() {
        return appSettingsService.getSettingValue("backup.samba.domain", "");
    }

    public String getUsername() {
        return appSettingsService.getSettingValue("backup.samba.username", "");
    }

    public String getPassword() {
        return appSettingsService.getSettingValue("backup.samba.password", "");
    }

    /**
     * Test the Samba connection with current settings
     */
    public String testConnection() {
        if (getHost().isEmpty() || getShare().isEmpty() || getUsername().isEmpty()) {
            return "Missing required configuration: host, share, and username are required";
        }

        try (SMBClient client = new SMBClient()) {
            try (Connection connection = client.connect(getHost())) {
                AuthenticationContext ac = new AuthenticationContext(
                        getUsername(),
                        getPassword().toCharArray(),
                        getDomain().isEmpty() ? null : getDomain()
                );

                try (Session session = connection.authenticate(ac)) {
                    try (DiskShare share = (DiskShare) session.connectShare(getShare())) {
                        // Try to access the path if specified
                        String path = getPath();
                        if (!path.isEmpty()) {
                            // Normalize path (remove leading/trailing slashes, convert to backslashes)
                            path = normalizePath(path);
                            if (!path.isEmpty() && !share.folderExists(path)) {
                                return "Connection successful but path does not exist: " + path;
                            }
                        }
                        return "Connection successful";
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Samba connection test failed", e);
            return "Connection failed: " + e.getMessage();
        }
    }

    /**
     * Copy a backup file to the Samba share
     */
    public void copyBackupToSamba(Path localBackupPath) throws IOException {
        if (!isEnabled()) {
            logger.debug("Samba backup is not enabled, skipping");
            return;
        }

        String filename = localBackupPath.getFileName().toString();
        logger.info("Copying backup to Samba share: {}", filename);

        try (SMBClient client = new SMBClient()) {
            try (Connection connection = client.connect(getHost())) {
                AuthenticationContext ac = new AuthenticationContext(
                        getUsername(),
                        getPassword().toCharArray(),
                        getDomain().isEmpty() ? null : getDomain()
                );

                try (Session session = connection.authenticate(ac)) {
                    try (DiskShare share = (DiskShare) session.connectShare(getShare())) {
                        // Build the full path within the share
                        String basePath = normalizePath(getPath());
                        String fullPath = basePath.isEmpty() ? filename : basePath + "\\" + filename;

                        // Ensure the directory exists
                        if (!basePath.isEmpty()) {
                            ensureDirectoryExists(share, basePath);
                        }

                        // Write the file
                        try (File remoteFile = share.openFile(
                                fullPath,
                                EnumSet.of(AccessMask.GENERIC_WRITE),
                                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                                EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
                                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                                EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE)
                        )) {
                            try (OutputStream os = remoteFile.getOutputStream()) {
                                Files.copy(localBackupPath, os);
                            }
                        }

                        logger.info("Backup successfully copied to Samba: {}", fullPath);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to copy backup to Samba", e);
            throw new IOException("Failed to copy backup to Samba share: " + e.getMessage(), e);
        }
    }

    /**
     * Normalize a path for SMB (convert forward slashes to backslashes, remove leading/trailing slashes)
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        // Replace forward slashes with backslashes
        path = path.replace("/", "\\");
        // Remove leading and trailing backslashes
        while (path.startsWith("\\")) {
            path = path.substring(1);
        }
        while (path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Ensure a directory path exists, creating it if necessary
     */
    private void ensureDirectoryExists(DiskShare share, String path) {
        String[] parts = path.split("\\\\");
        StringBuilder currentPath = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;

            if (currentPath.length() > 0) {
                currentPath.append("\\");
            }
            currentPath.append(part);

            String pathStr = currentPath.toString();
            if (!share.folderExists(pathStr)) {
                share.mkdir(pathStr);
                logger.debug("Created directory on Samba: {}", pathStr);
            }
        }
    }
}
