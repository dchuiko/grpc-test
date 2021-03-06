package com.sberned.grpc.test.server.service;

import com.sberned.grpc.test.server.impl.GRpcService;
import com.sberned.grpc.test.server.service.interceptor.LogInterceptor;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ru.sberned.grpc.test.api.messaging.MailRequest;
import ru.sberned.grpc.test.api.messaging.MailResponse;
import ru.sberned.grpc.test.api.messaging.MailServiceGrpc;

@GRpcService(interceptors = LogInterceptor.class)
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

            // послать значение
            responseObserver.onNext(replyBuilder.build());

        } else {
            responseObserver
                    // послать исключение
                    .onError(Status.FAILED_PRECONDITION
                            .augmentDescription("We don't use Json anymore!")
                            .asException()
                    );
        }

        // обязательно нужно позвать один раз, когда результат сформирован
        // иначе вызов будет "висеть" или блокироваться со стороны клиента
        responseObserver.onCompleted();
    }
}
