package com.sberned.grpc.test.server.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("grpc")
public class GRpcServerProperties {
    private int port = 6565;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}