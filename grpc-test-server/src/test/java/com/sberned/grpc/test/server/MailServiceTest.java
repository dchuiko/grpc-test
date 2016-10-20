package com.sberned.grpc.test.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;
import ru.sberned.grpc.test.api.MailRequest;
import ru.sberned.grpc.test.api.MailResponse;
import ru.sberned.grpc.test.api.MailServiceGrpc;
import ru.sberned.grpc.test.api.MailServiceGrpc.MailServiceFutureStub;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {App.class}, webEnvironment = DEFINED_PORT)
public class MailServiceTest {
    private ManagedChannel channel;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Before
    public void setup() {
        // создаем клиент: без ssl/tlc
        channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                                       .usePlaintext(true)
                                       .build();
    }

    @Test
    public void shouldSend() throws ExecutionException, InterruptedException {
        MailServiceFutureStub greeterFutureStub =
                MailServiceGrpc.newFutureStub(channel);
        MailRequest request = MailRequest.newBuilder().setTo("aaa@bbb.ru")
                                              .setFrom("from@from.com")
                                              .setSubject("subj")
                                              .setContent("content")
                                              .build();
        // заставляем Future заблокироваться
        MailResponse response = greeterFutureStub.send(request).get();

        assertTrue(response.getResult());
        assertTrue(response.getInfo().contains("aaa@bbb.ru"));
    }

    @Test
    public void shouldException() {
        // здесь используем блокирующий вызов
        final MailServiceGrpc.MailServiceBlockingStub greeterFutureStub =
                MailServiceGrpc.newBlockingStub(channel);
        MailRequest request = MailRequest.newBuilder().setTo("aaa@bbb.ru")
                                              .setFrom("from@from.com")
                                              .setSubject("hello Json")
                                              .setContent("content")
                                              .build();
        try {
            MailResponse response = greeterFutureStub.send(request);
        } catch (StatusRuntimeException e) {
            assertEquals(Status.FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
            assertTrue(e.getStatus().getDescription().toLowerCase().contains("json"));
        }
    }

    @After
    public void tearDown() {
        // останавливаем
        channel.shutdown();
    }
}