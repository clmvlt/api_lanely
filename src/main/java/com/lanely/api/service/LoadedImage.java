package com.lanely.api.service;

import org.springframework.core.io.Resource;

public record LoadedImage(Resource resource, String contentType, long sizeBytes) {
}
