package com.k2so.watcher.config;

import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyConfig {

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyCustomizer() {
        return factory -> {
            factory.setContextPath("");
            factory.addServerCustomizers(server -> {
                // Additional Jetty server customizations can be added here
            });
        };
    }
}
