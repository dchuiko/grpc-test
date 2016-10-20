package ru.sberned.grpc.test.client;

import io.grpc.ManagedChannel;

public class StreamingExample {
    public static void execute(ManagedChannel channel, String user) {
        new ChatBot(user, 3, channel).start();
    }
}
