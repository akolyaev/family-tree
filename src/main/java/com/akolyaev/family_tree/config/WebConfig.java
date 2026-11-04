package com.akolyaev.family_tree.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for serving the SPA (Single Page Application) static files.
 * 
 * Serves frontend build output from / and falls back to index.html for
 * all routes that don't match actual static files (SPA routing support).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SpaResourceResolver spaResourceResolver;

    public WebConfig(SpaResourceResolver spaResourceResolver) {
        this.spaResourceResolver = spaResourceResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(spaResourceResolver);
    }
}
