package com.sberned.grpc.test.server.service;

import com.google.protobuf.Timestamp;
import com.sberned.grpc.test.server.impl.GRpcService;
import com.sberned.grpc.test.server.service.interceptor.LogInterceptor;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sberned.grpc.test.api.messaging.ChatMessage;
import ru.sberned.grpc.test.api.messaging.ChatMessageFromServer;
import ru.sberned.grpc.test.api.messaging.ChatServiceGrpc;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@GRpcService(interceptors = LogInterceptor.class)
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static Set<StreamObserver<ChatMessageFromServer>> responseObservers = new CopyOnWriteArraySet<>();

    /**
     * То, что возвращается - это стрим тех данных, которые будут обработаны
     * То, что приходит в качестве параметра - это результат
     */
    @Override
    public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessageFromServer> responseObserver) {
        responseObservers.add(responseObserver);

        return new StreamObserver<ChatMessage>() {
            @Override
            public void onNext(ChatMessage value) {
                log.info("Received chat message from: " + value.getFrom() +
                        " with content: " + value.getMessage());

                ChatMessageFromServer message = ChatMessageFromServer.newBuilder()
                                                                     .setMessage(value)
                                                                     .setTimestamp(currentTimeStamp())
                                                                     .build();
                responseObservers.forEach(observer -> observer.onNext(message));
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error during streaming", t);
                responseObservers.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                log.info("Client streaming completed");
                responseObservers.remove(responseObserver);
                responseObserver.onCompleted();
            }
        };
    }

    private Timestamp.Builder currentTimeStamp() {
        return Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000);
    }

}
