package com.neoteric.starter.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neoteric.starter.jersey.JerseyDefaultsAutoConfiguration;
import com.neoteric.starter.request.params.RequestParametersFilter;
import com.neoteric.starter.request.tracing.RequestIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.JerseyProperties;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JerseyProperties.class)
@AutoConfigureAfter({JacksonAutoConfiguration.class, JerseyDefaultsAutoConfiguration.class})
public class RequestAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(RequestAutoConfiguration.class);

    @Autowired
    JerseyProperties jerseyProperties;

    @Bean
    FilterRegistrationBean registerRequestIdFilter() {
        return new FilterRegistrationBean(new RequestIdFilter(jerseyProperties.getApplicationPath()));
    }

    @Bean
    FilterRegistrationBean registerRequestParamsFilter(ObjectMapper objectMapper) throws Exception {
        return new FilterRegistrationBean(new RequestParametersFilter(objectMapper, jerseyProperties.getApplicationPath()));
    }
}