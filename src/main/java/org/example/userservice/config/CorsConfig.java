package org.example.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class CorsConfig {

    @Value("${app.storage.local.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.storage.local.profile-folder:profile-pictures}")
    private String profileFolder;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            // CORS is handled by API Gateway, so we disable it here to avoid conflicts
            // Only keep resource handler for profile pictures
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String profilePath = Paths.get(uploadDir, profileFolder).toAbsolutePath().toString();
                registry.addResourceHandler("/" + profileFolder + "/**")
                        .addResourceLocations("file:" + profilePath + "/");
            }
        };
    }
}
