package com.timebank;

        import org.springframework.boot.builder.SpringApplicationBuilder;
        import org.springframework.boot.web.support.SpringBootServletInitializer;

public class SpringBootStartApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TimebankApplication.class);
    }
}
