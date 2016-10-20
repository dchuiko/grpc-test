package com.sberned.grpc.test.server.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

@Configuration
@EnableConfigurationProperties(GRpcServerProperties.class)
public class GRpcAutoConfiguration {
    private final AbstractApplicationContext applicationContext;
    private final GRpcServerProperties gRpcServerProperties;

    @Autowired
    public GRpcAutoConfiguration(AbstractApplicationContext applicationContext,
                                 GRpcServerProperties gRpcServerProperties) {
        this.applicationContext = applicationContext;
        this.gRpcServerProperties = gRpcServerProperties;
    }


    @Bean
    @ConditionalOnBean(annotation = GRpcService.class)
    public GRpcServerRunner grpcServerRunner() {
        return new GRpcServerRunner(applicationContext, gRpcServerProperties);
    }
}