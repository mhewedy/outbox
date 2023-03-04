package com.github.mhewedy.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    OutboxService outboxService(JdbcTemplate jdbcTemplate, OutboxProperties outboxProperties) {
        return new OutboxService(jdbcTemplate, outboxProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    OutboxAspect outboxAspect(OutboxService outboxService) {
        return new OutboxAspect(new ObjectMapper(), outboxService);
    }

    @Bean
    @ConditionalOnMissingBean
    OutboxScheduler outboxScheduler(OutboxService outboxService, ApplicationContext applicationContext) {
        return new OutboxScheduler(new ObjectMapper(), outboxService, applicationContext);
    }
}
