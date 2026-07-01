package com.lanely.api.service;

import org.springframework.core.io.Resource;

public record LoadedDocument(Resource resource, String contentType, long sizeBytes, String originalFilename) {
}
