package com.sberned.grpc.test.server.impl;

import io.grpc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Collections.singletonList;

public class GRpcServersAwaitRunnable implements Runnable {
    private final Logger log = LoggerFactory.getLogger(GRpcServersAwaitRunnable.class);

    private final List<Server> servers;

    public GRpcServersAwaitRunnable(Server server) {
        this(singletonList(server));
    }

    public GRpcServersAwaitRunnable(List<Server> servers) {
        this.servers = servers;
    }

    @Override
    public void run() {
        servers.forEach(server -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                log.error("GRpc server stopped with error", e);
            }
        });
    }
}
