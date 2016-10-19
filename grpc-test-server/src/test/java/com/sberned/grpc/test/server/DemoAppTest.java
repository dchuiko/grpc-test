package com.sberned.grpc.test.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;
import ru.sberned.grpc.test.api.Mail;
import ru.sberned.grpc.test.api.MailServiceGrpc;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {App.class}, webEnvironment = DEFINED_PORT)
public class DemoAppTest {

    private ManagedChannel channel;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Before
    public void setup() {
        channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext(true).build();
    }

    @After
    public void tearDown() {
        channel.shutdown();
    }

    @Test
    public void simpleGreeting() throws ExecutionException, InterruptedException {
        final MailServiceGrpc.MailServiceFutureStub greeterFutureStub = MailServiceGrpc.newFutureStub(channel);
        final Mail.MailDTO helloRequest =
                Mail.MailDTO.newBuilder().setTo("aaa@bbb.ru").setFrom("from@from.com").setSubject("subj").setContent(
                        "content").build();
        final boolean reply = greeterFutureStub.send(helloRequest).get().getResult();
        assertTrue(reply);

    }
}