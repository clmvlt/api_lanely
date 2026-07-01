package com.lanely.api.controller;

import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.service.ImageService;
import com.lanely.api.service.LoadedImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/images")
@Tag(name = "Images", description = "Serve stored images by id (generic, reusable for any pictured entity)")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get an image by id",
            description = "Streams the raw bytes of a stored image with its content type. Public (the id is an unguessable UUID), so it "
                    + "can be used directly in an <img src> tag. Responses are cacheable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image bytes returned",
                    content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Image not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "Identifier of the image", example = "aaaa1111-bbbb-2222-cccc-3333dddd4444")
            @PathVariable UUID id) {
        LoadedImage image = imageService.getContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .contentLength(image.sizeBytes())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(image.resource());
    }
}
