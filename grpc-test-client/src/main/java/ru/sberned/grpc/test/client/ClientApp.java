package ru.sberned.grpc.test.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClientApp {

    public static void main(String[] args) {
        System.out.println("Start Client 1.0");

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 6565)
                .usePlaintext(true)
                .build();

        if (args.length > 0) {
            StreamingExample.execute(channel, args[0]);
        } else {
            SimpleExample.execute(channel);
        }
    }

}
