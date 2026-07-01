package com.lanely.api.mapper;

import com.lanely.api.entity.Image;

public final class ImageMapper {

    private ImageMapper() {
    }

    /**
     * Relative URL the client can use in an &lt;img src&gt; (prepend the API base URL).
     * Returns null when there is no image.
     */
    public static String url(Image image) {
        return image == null ? null : "/images/" + image.getId();
    }
}
