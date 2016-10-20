package com.sberned.grpc.test.server.impl;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class GRpcServerRunner implements CommandLineRunner, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(GRpcServerRunner.class);

    private final AbstractApplicationContext applicationContext;
    private final GRpcServerProperties gRpcServerProperties;
    private Server server;

    @Autowired
    public GRpcServerRunner(AbstractApplicationContext applicationContext,
                            GRpcServerProperties gRpcServerProperties) {
        this.applicationContext = applicationContext;
        this.gRpcServerProperties = gRpcServerProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting gRPC Server");

        // все начинается с билдера
        final ServerBuilder<?> serverBuilder = ServerBuilder.forPort(gRpcServerProperties.getPort());

        Collection<ServerInterceptor> globalInterceptors =
                typedBeansWithAnnotation(ServerInterceptor.class, GRpcGlobalInterceptor.class);

        // ищем аннотированные GRpcService и добавляем их в билдер
        typedBeansWithAnnotation(BindableService.class, GRpcService.class).forEach(gRpcService -> {
            ServerServiceDefinition gRpcServiceDefinition =
                    createGRpcServiceDefinition(globalInterceptors, gRpcService);

            // добавляем найденный сервис в билдер
            serverBuilder.addService(gRpcServiceDefinition);

            log.info("'{}' service has been registered.", gRpcService.getClass().getName());
        });

        // запускаем сервер
        server = serverBuilder.build().start();

        log.info("gRPC Server started, listening on port {}", gRpcServerProperties.getPort());

        startDaemonAwaitThread();
    }

    /**
     * Создаем дескриптор gRPC сервиса
     */
    private ServerServiceDefinition createGRpcServiceDefinition(Collection<ServerInterceptor> globalInterceptors,
                                                                BindableService bindableService) {
        GRpcService gRpcServiceAnnotation = bindableService.getClass().getAnnotation(GRpcService.class);

        ServerServiceDefinition gRpcServiceDefinition = bindableService.bindService();
        // включаем interceptor-ы
        gRpcServiceDefinition = bindInterceptors(gRpcServiceDefinition, gRpcServiceAnnotation, globalInterceptors);
        return gRpcServiceDefinition;
    }

    /**
     * Привязываем к gRPC бину глобальные и частные interceptor-ы
     */
    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition,
                                                     GRpcService gRpcService,
                                                     Collection<ServerInterceptor> globalInterceptors) {

        // создаем частные interceptor-ы
        Stream<? extends ServerInterceptor> custom =
                Stream.of(gRpcService.interceptors()).map(this::createInterceptor);
        // добавляем глобальные interceptor-ы, если включена опция в аннотации
        Stream<ServerInterceptor> global = gRpcService.applyGlobalInterceptors() ?
                                                  globalInterceptors.stream() :
                                                  Stream.empty();
        // получаем объединенный список interceptor-ов
        List<ServerInterceptor> interceptors = Stream.concat(global, custom).distinct().collect(toList());
        return ServerInterceptors.intercept(serviceDefinition, interceptors);
    }

    /**
     * Создает bean interceptor, если нужно
     */
    private ServerInterceptor createInterceptor(Class<? extends ServerInterceptor> interceptorClass) {
        try {
            final String[] interceptorBeanNames =
                    applicationContext.getBeanNamesForType(interceptorClass);
            return 0 < interceptorBeanNames.length ?
                   applicationContext.getBean(interceptorClass) :
                   interceptorClass.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create interceptor instance.", e);
        }
    }


    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(new GRpcServersAwaitRunnable(server), "grpc-await-thread-0");
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    private <T> Collection<T> typedBeansWithAnnotation(Class<T> beanType,
                                                       Class<? extends Annotation> annotationType) {
        final ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

        return Stream.of(applicationContext.getBeanNamesForType(beanType)).filter(name -> {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name);
            if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                return metadata.isAnnotated(annotationType.getName());
            }
            return null != beanFactory.findAnnotationOnBean(name, annotationType);
        }).map(name -> beanFactory.getBean(name, beanType)).collect(toList());
    }

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down gRPC server ...");

        // при остановке spring-а, останавливаем gRPC сервер
        Optional.ofNullable(server).ifPresent(Server::shutdown);

        log.info("gRPC server stopped.");
    }
}
