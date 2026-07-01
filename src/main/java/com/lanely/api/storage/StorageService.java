package com.lanely.api.storage;

import org.springframework.core.io.Resource;

/**
 * Generic binary storage abstraction. The default implementation stores files on the local
 * filesystem; swap it for an S3/MinIO implementation later without touching business code.
 */
public interface StorageService {

    /**
     * Stores raw bytes and returns an opaque storage key used later to load or delete them.
     */
    String store(byte[] content, String contentType);

    /**
     * Loads the content previously stored under the given key.
     */
    Resource load(String key);

    /**
     * Deletes the content stored under the given key. No-op if it does not exist.
     */
    void delete(String key);
}
