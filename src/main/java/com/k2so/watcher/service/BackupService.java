package com.k2so.watcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${k2so.backup.directory:./backups}")
    private String backupDirectory;

    @Value("${k2so.backup.max-files:10}")
    private int maxBackupFiles;

    public BackupService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates a backup of the database and returns the backup file path
     */
    public Path createBackup() throws Exception {
        // Ensure backup directory exists
        Path backupDir = Paths.get(backupDirectory);
        Files.createDirectories(backupDir);

        String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);
        String backupFileName = "k2so_backup_" + timestamp + ".zip";
        Path backupPath = backupDir.resolve(backupFileName);

        // Export database to SQL script
        Path sqlFile = backupDir.resolve("backup_" + timestamp + ".sql");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String exportSql = String.format("SCRIPT TO '%s'", sqlFile.toAbsolutePath().toString());
            stmt.execute(exportSql);
            logger.info("Database exported to SQL: {}", sqlFile);
        }

        // Create ZIP file containing the SQL backup
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupPath.toFile()))) {
            ZipEntry entry = new ZipEntry("database.sql");
            zos.putNextEntry(entry);

            byte[] sqlContent = Files.readAllBytes(sqlFile);
            zos.write(sqlContent);
            zos.closeEntry();

            // Add metadata
            ZipEntry metaEntry = new ZipEntry("metadata.txt");
            zos.putNextEntry(metaEntry);
            String metadata = String.format("Backup created: %s%nApplication: K2SO Watcher%nVersion: 1.0.0%n",
                    LocalDateTime.now().toString());
            zos.write(metadata.getBytes());
            zos.closeEntry();
        }

        // Delete temporary SQL file
        Files.deleteIfExists(sqlFile);

        // Cleanup old backups
        cleanupOldBackups();

        logger.info("Backup created successfully: {}", backupPath);
        return backupPath;
    }

    /**
     * Creates a backup and returns it as a byte array for download
     */
    public byte[] createBackupForDownload() throws Exception {
        Path backupPath = createBackup();
        byte[] content = Files.readAllBytes(backupPath);
        return content;
    }

    /**
     * Restores the database from a backup file
     */
    public void restoreFromBackup(InputStream backupInputStream) throws Exception {
        // Create temp directory for extraction
        Path tempDir = Files.createTempDirectory("k2so_restore_");
        Path sqlFile = null;

        try {
            // Extract ZIP file
            try (ZipInputStream zis = new ZipInputStream(backupInputStream)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("database.sql")) {
                        sqlFile = tempDir.resolve("database.sql");
                        try (FileOutputStream fos = new FileOutputStream(sqlFile.toFile())) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }

            if (sqlFile == null || !Files.exists(sqlFile)) {
                throw new IllegalArgumentException("Invalid backup file: database.sql not found");
            }

            // Restore database from SQL script
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Drop all existing objects first
                stmt.execute("DROP ALL OBJECTS");

                // Run the SQL script to restore
                String restoreSql = String.format("RUNSCRIPT FROM '%s'", sqlFile.toAbsolutePath().toString());
                stmt.execute(restoreSql);

                logger.info("Database restored successfully from backup");
            }

        } finally {
            // Cleanup temp directory
            if (tempDir != null) {
                try (Stream<Path> walk = Files.walk(tempDir)) {
                    walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                }
            }
        }
    }

    /**
     * Lists available backup files
     */
    public List<BackupInfo> listBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        Path backupDir = Paths.get(backupDirectory);

        if (!Files.exists(backupDir)) {
            return backups;
        }

        try (Stream<Path> files = Files.list(backupDir)) {
            backups = files
                    .filter(p -> p.toString().endsWith(".zip"))
                    .filter(p -> p.getFileName().toString().startsWith("k2so_backup_"))
                    .map(p -> {
                        try {
                            return new BackupInfo(
                                    p.getFileName().toString(),
                                    Files.size(p),
                                    Files.getLastModifiedTime(p).toInstant()
                            );
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(b -> b != null)
                    .sorted(Comparator.comparing(BackupInfo::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error listing backups", e);
        }

        return backups;
    }

    /**
     * Deletes a specific backup file
     */
    public boolean deleteBackup(String filename) {
        if (!filename.startsWith("k2so_backup_") || !filename.endsWith(".zip")) {
            return false;
        }

        Path backupPath = Paths.get(backupDirectory, filename);
        try {
            return Files.deleteIfExists(backupPath);
        } catch (IOException e) {
            logger.error("Error deleting backup: {}", filename, e);
            return false;
        }
    }

    /**
     * Downloads a specific backup file
     */
    public byte[] downloadBackup(String filename) throws IOException {
        if (!filename.startsWith("k2so_backup_") || !filename.endsWith(".zip")) {
            throw new IllegalArgumentException("Invalid backup filename");
        }

        Path backupPath = Paths.get(backupDirectory, filename);
        if (!Files.exists(backupPath)) {
            throw new FileNotFoundException("Backup file not found: " + filename);
        }

        return Files.readAllBytes(backupPath);
    }

    /**
     * Cleanup old backups, keeping only the most recent ones
     */
    private void cleanupOldBackups() {
        List<BackupInfo> backups = listBackups();

        if (backups.size() > maxBackupFiles) {
            List<BackupInfo> toDelete = backups.subList(maxBackupFiles, backups.size());
            for (BackupInfo backup : toDelete) {
                deleteBackup(backup.getFilename());
                logger.info("Deleted old backup: {}", backup.getFilename());
            }
        }
    }

    /**
     * Backup information DTO
     */
    public static class BackupInfo {
        private final String filename;
        private final long size;
        private final java.time.Instant createdAt;

        public BackupInfo(String filename, long size, java.time.Instant createdAt) {
            this.filename = filename;
            this.size = size;
            this.createdAt = createdAt;
        }

        public String getFilename() {
            return filename;
        }

        public long getSize() {
            return size;
        }

        public String getSizeFormatted() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }

        public java.time.Instant getCreatedAt() {
            return createdAt;
        }

        public String getCreatedAtFormatted() {
            return java.time.LocalDateTime.ofInstant(createdAt, java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
