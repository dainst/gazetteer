package org.dainst.gazetteer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = GazetteerApplication.class)
public class GazetteerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(
        SpringApplicationBuilder builder
    ) {
        return builder.sources(GazetteerApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GazetteerApplication.class, args);
    }
}
