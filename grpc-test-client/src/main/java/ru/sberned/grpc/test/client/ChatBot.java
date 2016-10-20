package ru.sberned.grpc.test.client;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import ru.sberned.grpc.test.api.messaging.ChatMessage;
import ru.sberned.grpc.test.api.messaging.ChatMessageFromServer;
import ru.sberned.grpc.test.api.messaging.ChatServiceGrpc;
import ru.sberned.grpc.test.api.messaging.ChatServiceGrpc.ChatServiceStub;

public class ChatBot {
    private final String user;
    private final int numberOfMessages;
    private final StreamObserver<ChatMessage> chat;

    public ChatBot(String user, int numberOfMessages, ManagedChannel channel) {
        this.user = user;
        this.numberOfMessages = numberOfMessages;

        ChatServiceStub chatService = ChatServiceGrpc.newStub(channel);
        chat = chatService.chat(new StreamObserver<ChatMessageFromServer>() {
            @Override
            public void onNext(ChatMessageFromServer value) {
                final String from = value.getMessage().getFrom();
                // показываем сообщение, если не от себя
                if (!user.equals(from)) {
                    System.out.println(Thread.currentThread().getName() +
                            " - Client " + user + " got chat message from: " + from + " value: " +
                            value.getMessage().getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                System.out.println("Client " + user + " Disconnected by Error");
            }

            @Override
            public void onCompleted() {
                System.out.println("Client " + user + " Completed");
            }
        });
    }

    public void start() {
        try {
            for (int i = 0; i < numberOfMessages; i++) {
                try {
                    Thread.sleep((int) (1000 * Math.random()));
                    chat.onNext(ChatMessage.newBuilder()
                                           .setFrom(user)
                                           .setMessage("msg " + (i + 1))
                                           .build());
                } catch (Exception e) {
                    chat.onError(e);
                    throw new RuntimeException(e);
                }
            }

            Thread.sleep(3000);

            // перестаем слушать и отсылать
            chat.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
