package ru.sberned.grpc.test.client;

import io.grpc.ManagedChannel;
import ru.sberned.grpc.test.api.messaging.MailRequest;
import ru.sberned.grpc.test.api.messaging.MailResponse;
import ru.sberned.grpc.test.api.messaging.MailServiceGrpc;

public class SimpleExample {
    public static void execute(final ManagedChannel channel) {
        MailResponse response = new GRpcTemplate().execute(channel, () -> {

            MailServiceGrpc.MailServiceBlockingStub greeterFutureStub =
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

    private static class GRpcTemplate {

        public <T> T execute(ManagedChannel channel, ChannelAction<T> action) {
            try {
                return action.execute();
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
        T execute();
    }
}
