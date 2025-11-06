package com.travelreimburse.infrastructure.storage;

import com.travelreimburse.application.service.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service für File-Storage-Operationen.
 * Verwaltet das Speichern und Laden von Beleg-Dateien.
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // Erlaubte Content-Types
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // Maximale Dateigröße: 10 MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public FileStorageService(@Value("${file.upload-dir:./uploads/receipts}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new InvalidFileException("Konnte Upload-Verzeichnis nicht erstellen", ex);
        }
    }

    /**
     * Validiert eine hochzuladende Datei
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Datei ist leer");
        }

        // Prüfe Dateigröße
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    String.format("Datei ist zu groß. Maximum: %d MB", MAX_FILE_SIZE / 1024 / 1024)
            );
        }

        // Prüfe Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Ungültiger Dateityp. Erlaubt: PDF, JPG, PNG"
            );
        }

        // Prüfe Dateiendung
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileException("Dateiname ist ungültig");
        }

        String extension = getFileExtension(originalFilename);
        if (!isValidExtension(extension)) {
            throw new InvalidFileException(
                    "Ungültige Dateiendung. Erlaubt: .pdf, .jpg, .jpeg, .png"
            );
        }
    }

    /**
     * Speichert eine Datei und gibt den generierten Dateinamen zurück
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        // Generiere eindeutigen Dateinamen
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;
        } catch (IOException ex) {
            throw new InvalidFileException("Konnte Datei nicht speichern: " + fileName, ex);
        }
    }

    /**
     * Lädt eine Datei als Resource
     */
    public Path loadFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                return filePath;
            } else {
                throw new InvalidFileException("Datei nicht gefunden: " + fileName);
            }
        } catch (Exception ex) {
            throw new InvalidFileException("Datei nicht gefunden: " + fileName, ex);
        }
    }

    /**
     * Löscht eine Datei
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new InvalidFileException("Konnte Datei nicht löschen: " + fileName, ex);
        }
    }

    /**
     * Gibt den vollständigen Pfad einer gespeicherten Datei zurück
     */
    public String getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).toString();
    }

    /**
     * Extrahiert die Dateiendung aus einem Dateinamen
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex).toLowerCase();
    }

    /**
     * Prüft ob eine Dateiendung gültig ist
     */
    private boolean isValidExtension(String extension) {
        return extension.equals(".pdf") ||
                extension.equals(".jpg") ||
                extension.equals(".jpeg") ||
                extension.equals(".png");
    }
}

