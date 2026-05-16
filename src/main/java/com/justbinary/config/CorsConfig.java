package com.justbinary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // ── ALLOWED ORIGINS ──────────────────────────────────────
        // Allows ALL origins (safe for development)
        config.setAllowedOriginPatterns(java.util.List.of("*"));

        // ── ALLOWED HTTP METHODS ─────────────────────────────────
        config.setAllowedMethods(java.util.List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        // ── ALLOWED HEADERS ──────────────────────────────────────
        config.setAllowedHeaders(java.util.List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // ── EXPOSED HEADERS ──────────────────────────────────────
        config.setExposedHeaders(java.util.List.of(
            "Authorization",
            "Content-Disposition"
        ));

        // ── CREDENTIALS ──────────────────────────────────────────
        config.setAllowCredentials(true);

        // ── PREFLIGHT CACHE ──────────────────────────────────────
        config.setMaxAge(3600L);

        // ── APPLY TO ALL ROUTES ──────────────────────────────────
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}