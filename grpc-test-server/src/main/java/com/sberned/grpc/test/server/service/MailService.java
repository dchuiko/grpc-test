package com.sberned.grpc.test.server.service;

import com.sberned.grpc.test.server.impl.GRpcService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ru.sberned.grpc.test.api.MailRequest;
import ru.sberned.grpc.test.api.MailResponse;
import ru.sberned.grpc.test.api.MailServiceGrpc;

@GRpcService
public class MailService extends MailServiceGrpc.MailServiceImplBase {
    /**
     * Важный момент: на сервере всегда работаем в
     * асинхронном режиме, тк каждый конкретный
     * клиент может сам выбирать стиль взаимодействия:
     * синхронный или асинхроннный
     */
    @Override
    public void send(MailRequest request, StreamObserver<MailResponse> responseObserver) {
        if(!request.getSubject().toLowerCase().contains("json")) {
            MailResponse.Builder replyBuilder =
                 MailResponse.newBuilder()
                             .setResult(true)
                             .setInfo("Mail request " + request.toString() + " served");
             responseObserver.onNext(replyBuilder.build());

        } else {
            responseObserver
                    .onError(Status.FAILED_PRECONDITION
                            .augmentDescription("We don't use Json anymore!")
                            .asException()
                    );
        }

        responseObserver.onCompleted();
    }
}
