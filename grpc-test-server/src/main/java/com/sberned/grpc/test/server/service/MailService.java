package com.sberned.grpc.test.server.service;

import com.sberned.grpc.test.server.impl.GRpcService;
import io.grpc.stub.StreamObserver;
import ru.sberned.grpc.test.api.Mail;
import ru.sberned.grpc.test.api.MailServiceGrpc;

@GRpcService
public class MailService extends MailServiceGrpc.MailServiceImplBase {
    @Override
    public void send(Mail.MailDTO request, StreamObserver<Mail.MailServiceResponse> responseObserver) {
        final Mail.MailServiceResponse.Builder replyBuilder = Mail.MailServiceResponse.newBuilder().setResult(true);
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }

    //        @Override
//        public void sayHello(Mail.MailDTO request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
//            final GreeterOuterClass.HelloReply.Builder replyBuilder = GreeterOuterClass.HelloReply.newBuilder().setMessage("Hello " + request.getName());
//            responseObserver.onNext(replyBuilder.build());
//            responseObserver.onCompleted();
//        }
}
