package com.thinkbiganalytics.cassandra.module.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by mi186020 on 2017/09/30.
 */
@Configuration
@PropertySource(value = "classpath:cassandra.properties", ignoreResourceNotFound = true)
public class CassandraSpringConfig {

    @Bean
    @ConfigurationProperties("cassandra")
    public CassandraClientConfig cassandraClientConfig() {
        return new CassandraClientConfig();
    }
}
