package com.akolyaev.family_tree.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Custom resource resolver that falls back to index.html for SPA routes.
 * If a requested path doesn't match an actual file, returns index.html
 * to allow client-side hash-based routing.
 */
@Component
public class SpaResourceResolver extends PathResourceResolver {

    private static final Resource INDEX_HTML = resolveIndexHtml();

    private static Resource resolveIndexHtml() {
        try {
            Resource resource = new PathMatchingResourcePatternResolver()
                    .getResource("classpath:/static/index.html");
            return resource.exists() ? resource : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected Resource getResource(String resourcePath, Resource parent) {
        try {
            Resource resource = super.getResource(resourcePath, parent);
            if (resource != null && resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (IOException e) {
            // Ignore and fall through to index.html
        }
        // Fallback to index.html for SPA routing
        return INDEX_HTML;
    }
}
