package com.lanely.api.storage;

import com.lanely.api.config.StorageProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path basePath;

    public LocalStorageService(StorageProperties properties) {
        this.basePath = Paths.get(properties.basePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create storage directory: " + this.basePath, e);
        }
    }

    @Override
    public String store(byte[] content, String contentType) {
        String key = UUID.randomUUID().toString();
        Path target = resolve(key);
        try {
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store file", e);
        }
        return key;
    }

    @Override
    public Resource load(String key) {
        return new FileSystemResource(resolve(key));
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete file", e);
        }
    }

    private Path resolve(String key) {
        Path resolved = basePath.resolve(key).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return resolved;
    }
}
