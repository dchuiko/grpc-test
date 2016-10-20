package ru.sberned.grpc.test.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.sberned.grpc.test.api.MailRequest;
import ru.sberned.grpc.test.api.MailResponse;
import ru.sberned.grpc.test.api.MailServiceGrpc;
import ru.sberned.grpc.test.api.MailServiceGrpc.MailServiceBlockingStub;

public class ClientApp {

    public static void main(String[] args) {
        System.out.println("Start Client 1.0");

        MailResponse response = new Executor().execute(channel -> {
            MailServiceBlockingStub greeterFutureStub =
                    MailServiceGrpc.newBlockingStub(channel);
            MailRequest request = MailRequest.newBuilder()
                                             .setTo("aaa@bbb.ru")
                                             .setFrom("from@from.com")
                                             .setSubject("subj")
                                             .setContent("content")
                                             .build();
            return greeterFutureStub.send(request);
        });
        System.out.println(response);
    }

    private static class Executor {

        public <T> T execute(ChannelAction<T> action) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("localhost", 6565)
                    .usePlaintext(true)
                    .build();
            try {
                return action.execute(channel);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            } finally {
                try {
                    channel.shutdown();
                } catch (Throwable e) {
                    // ignore
                }
            }
        }
    }

    private interface ChannelAction<T> {
        T execute(ManagedChannel channel);
    }

}
