package com.bers.services.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Store a file and return its URL
     *
     * @param file      The file to store
     * @param directory The directory to store the file (e.g., "parcels", "proof-of-delivery")
     * @return The URL or path to access the file
     */
    String storeFile(MultipartFile file, String directory);

    /**
     * Delete a file by its filename
     *
     * @param filename  The name of the file to delete
     * @param directory The directory where the file is stored
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteFile(String filename, String directory);

    /**
     * Get the full path to a file
     *
     * @param filename  The name of the file
     * @param directory The directory where the file is stored
     * @return The full file path
     */
    String getFilePath(String filename, String directory);
}
