package com.example.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat 11 defaults to accepting only 10 parts (files + fields put
 * together) in one multipart request, as a hardening measure against
 * multipart-bomb style DoS attacks. A single bulk-cover upload is
 * easily 80+ files at once (one spreadsheet plus a whole folder of
 * jpgs), so the default rejects a perfectly normal admin upload before
 * it even reaches ProductController.bulkUpload - see
 * spring.servlet.multipart.max-request-size in application.properties
 * for the separate, already-generous total SIZE limit; this is about
 * the part COUNT instead, which that property does not control.
 */
@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private static final int MAX_MULTIPART_PART_COUNT = 500;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers((Connector connector) ->
                connector.setMaxPartCount(MAX_MULTIPART_PART_COUNT));
    }
}
