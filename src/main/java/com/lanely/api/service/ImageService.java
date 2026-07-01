package com.lanely.api.service;

import com.lanely.api.config.ImageProperties;
import com.lanely.api.entity.Image;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.repository.ImageRepository;
import com.lanely.api.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp", "image/gif");

    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private final long maxSizeBytes;

    public ImageService(StorageService storageService, ImageRepository imageRepository, ImageProperties properties) {
        this.storageService = storageService;
        this.imageRepository = imageRepository;
        this.maxSizeBytes = properties.maxSizeBytes();
    }

    @Transactional
    public Image upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("error.image.required");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded image", e);
        }
        String contentType = file.getContentType() == null ? null : file.getContentType().toLowerCase();
        return store(bytes, contentType, file.getOriginalFilename());
    }

    @Transactional
    public Image store(byte[] bytes, String contentType, String originalFilename) {
        if (bytes == null || bytes.length == 0) {
            throw new BadRequestException("error.image.required");
        }
        String normalizedContentType = contentType == null ? null : contentType.toLowerCase();
        if (normalizedContentType == null || !ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BadRequestException("error.image.unsupported-type", ALLOWED_CONTENT_TYPES);
        }
        if (bytes.length > maxSizeBytes) {
            throw new BadRequestException("error.image.too-large", maxSizeBytes);
        }

        String key = storageService.store(bytes, normalizedContentType);
        Image image = new Image();
        image.setStorageKey(key);
        image.setContentType(normalizedContentType);
        image.setOriginalFilename(originalFilename);
        image.setSizeBytes(bytes.length);
        return imageRepository.save(image);
    }

    @Transactional
    public Optional<Image> tryStore(byte[] bytes, String contentType, String originalFilename) {
        try {
            return Optional.of(store(bytes, contentType, originalFilename));
        } catch (RuntimeException ex) {
            log.warn("Could not store external image ({}): {}", originalFilename, ex.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public void delete(Image image) {
        if (image == null) {
            return;
        }
        storageService.delete(image.getStorageKey());
        imageRepository.delete(image);
    }

    @Transactional(readOnly = true)
    public LoadedImage getContent(UUID id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.image.not-found"));
        return new LoadedImage(storageService.load(image.getStorageKey()), image.getContentType(), image.getSizeBytes());
    }
}
