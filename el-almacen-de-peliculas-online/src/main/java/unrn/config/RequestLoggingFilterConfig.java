package unrn.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import unrn.api.RawBodyLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

    @Bean
    public FilterRegistrationBean<RawBodyLoggingFilter> rawBodyLoggingFilter() {
        FilterRegistrationBean<RawBodyLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RawBodyLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}