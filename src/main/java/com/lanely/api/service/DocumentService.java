package com.lanely.api.service;

import com.lanely.api.config.DocumentProperties;
import com.lanely.api.entity.Document;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.repository.DocumentRepository;
import com.lanely.api.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("application/pdf", "image/png", "image/jpeg", "image/webp");

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final long maxSizeBytes;

    public DocumentService(StorageService storageService, DocumentRepository documentRepository,
                           DocumentProperties properties) {
        this.storageService = storageService;
        this.documentRepository = documentRepository;
        this.maxSizeBytes = properties.maxSizeBytes();
    }

    @Transactional
    public Document upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("error.document.required");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded document", e);
        }
        String contentType = file.getContentType() == null ? null : file.getContentType().toLowerCase();
        return store(bytes, contentType, file.getOriginalFilename());
    }

    @Transactional
    public Document store(byte[] bytes, String contentType, String originalFilename) {
        if (bytes == null || bytes.length == 0) {
            throw new BadRequestException("error.document.required");
        }
        String normalizedContentType = contentType == null ? null : contentType.toLowerCase();
        if (normalizedContentType == null || !ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BadRequestException("error.document.unsupported-type", ALLOWED_CONTENT_TYPES);
        }
        if (bytes.length > maxSizeBytes) {
            throw new BadRequestException("error.document.too-large", maxSizeBytes);
        }

        String key = storageService.store(bytes, normalizedContentType);
        Document document = new Document();
        document.setStorageKey(key);
        document.setContentType(normalizedContentType);
        document.setOriginalFilename(originalFilename);
        document.setSizeBytes(bytes.length);
        return documentRepository.save(document);
    }

    @Transactional
    public void delete(Document document) {
        if (document == null) {
            return;
        }
        storageService.delete(document.getStorageKey());
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public LoadedDocument getContent(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.not-found"));
        return new LoadedDocument(storageService.load(document.getStorageKey()), document.getContentType(),
                document.getSizeBytes(), document.getOriginalFilename());
    }
}
