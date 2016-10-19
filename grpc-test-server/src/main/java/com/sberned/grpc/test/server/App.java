package com.sberned.grpc.test.server;

import java.util.Arrays;

import com.sberned.grpc.test.server.impl.GRpcService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.sberned.grpc.test.api.Mail;
import ru.sberned.grpc.test.api.MailServiceGrpc;

@SpringBootApplication
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("App started!");
        ApplicationContext ctx = SpringApplication.run(App.class, args);
        log.info("Spring started!");
    }

    @GRpcService()
    public static class GreeterService extends MailServiceGrpc.MailServiceImplBase {
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

}
