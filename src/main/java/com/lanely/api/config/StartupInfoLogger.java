package com.lanely.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class StartupInfoLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupInfoLogger.class);

    private final Environment env;

    public StartupInfoLogger(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        String appName = env.getProperty("spring.application.name", "application");
        String protocol = Boolean.parseBoolean(env.getProperty("server.ssl.enabled")) ? "https" : "http";
        String port = env.getProperty("server.port", "8080");
        String contextPath = normalizeContextPath(env.getProperty("server.servlet.context-path", ""));
        String swaggerPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
        String apiDocsPath = env.getProperty("springdoc.api-docs.path", "/v3/api-docs");

        String[] activeProfiles = env.getActiveProfiles();
        String profiles = activeProfiles.length == 0 ? "default" : String.join(", ", activeProfiles);

        String hostAddress = resolveHostAddress();
        long startupMs = event.getTimeTaken() != null ? event.getTimeTaken().toMillis() : -1;
        String datasourceUrl = env.getProperty("spring.datasource.url", "n/a");

        log.info("""

                ------------------------------------------------------------
                  {} is up and running!
                ------------------------------------------------------------
                  Profile(s)   : {}
                  Startup time : {} ms
                  Database     : {}
                  API          : {}://{}:{}{}
                  Swagger UI   : {}://{}:{}{}{}
                  API docs     : {}://{}:{}{}{}
                ------------------------------------------------------------""",
                appName,
                profiles,
                startupMs,
                datasourceUrl,
                protocol, hostAddress, port, contextPath,
                protocol, hostAddress, port, contextPath, swaggerPath,
                protocol, hostAddress, port, contextPath, apiDocsPath);
    }

    private String normalizeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank() || contextPath.equals("/")) {
            return "";
        }
        return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }

    private String resolveHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not resolve host address, falling back to localhost");
            return "localhost";
        }
    }
}
