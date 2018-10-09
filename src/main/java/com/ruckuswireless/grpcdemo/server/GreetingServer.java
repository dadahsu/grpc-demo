package com.ruckuswireless.grpcdemo.server;

import com.ruckuswireless.grpcdemo.HelloServiceGrpc;
import com.ruckuswireless.grpcdemo.HelloWorldProto;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GreetingServer {

    private Server server;

    public void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new HelloImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GreetingServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final GreetingServer server = new GreetingServer();
        server.start();
        server.blockUntilShutdown();
    }


    static class HelloImpl extends HelloServiceGrpc.HelloServiceImplBase {
        /**
         * Unary implementation
         *
         * The server echo clients request and append with "Welcome!" at the end.
         */
        @Override
        public void sayHello(HelloWorldProto.HelloRequest request, StreamObserver<HelloWorldProto.HelloResponse> responseObserver) {
            System.out.println("[sayHello] Server received: " + request.getGreeting());
            responseObserver.onNext(HelloWorldProto.HelloResponse.newBuilder().setReply(
                    request.getGreeting() + "\n" + "Welcome!").build());
            responseObserver.onCompleted();
        }

        /**
         * Client streaming implementation
         *
         * The server would buffer clients' requests. Reply only one response when clients complete stubs.
         *
         * @param responseObserver
         * @return
         */
        @Override
        public io.grpc.stub.StreamObserver<com.ruckuswireless.grpcdemo.HelloWorldProto.HelloRequest> lotsOfGreetings(
                io.grpc.stub.StreamObserver<com.ruckuswireless.grpcdemo.HelloWorldProto.HelloResponse> responseObserver) {

            final StreamObserver<HelloWorldProto.HelloRequest> streamObserver = new StreamObserver<HelloWorldProto.HelloRequest>() {
                StringBuffer sb = new StringBuffer();

                @Override
                public void onNext(HelloWorldProto.HelloRequest value) {
                    System.out.println("[lotsOfGreetings] Server received: " + value.getGreeting());
                    sb.append(value.getGreeting());
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Stub onError");
                    responseObserver.onCompleted();
                }

                @Override
                public void onCompleted() {
                    System.out.println("Stub onCompleted");
                    responseObserver.onNext(HelloWorldProto.HelloResponse.newBuilder().setReply(
                            sb.toString() + "\n" + "Welcome!").build());
                    responseObserver.onCompleted();
                }
            };
            return streamObserver;
        }

        /**
         * Server streaming implementation
         *
         * The server splits responses into a single word, and reply to clients once a word.
         *
         * @param request
         * @param responseObserver
         */
        public void lotsOfReplies(com.ruckuswireless.grpcdemo.HelloWorldProto.HelloRequest request,
                                  io.grpc.stub.StreamObserver<com.ruckuswireless.grpcdemo.HelloWorldProto.HelloResponse> responseObserver) {
            System.out.println("[lotsOfReplies] Server received: " + request.getGreeting());
            String message = request.getGreeting() + "\n" + "Welcome!";

            for (int i = 0; i < message.length(); i++) {
                responseObserver.onNext(HelloWorldProto.HelloResponse.newBuilder()
                        .setReply(message.substring(i, i + 1)).build());
            }

            responseObserver.onCompleted();
        }

        /**
         * Bidirectional implementation
         *
         * The server sends "Hello?" to clients. Like "lotsOfGreetings" the server would buffer client
         * request and send to clients by one request.
         * Finally the server sends "Byebye!" and complete stubs.
         *
         * @param responseObserver
         * @return
         */
        public io.grpc.stub.StreamObserver<com.ruckuswireless.grpcdemo.HelloWorldProto.HelloRequest> bidiHello(
                io.grpc.stub.StreamObserver<com.ruckuswireless.grpcdemo.HelloWorldProto.HelloResponse> responseObserver) {
            final StreamObserver<HelloWorldProto.HelloRequest> streamObserver =
                    new StreamObserver<HelloWorldProto.HelloRequest>() {
                StringBuffer sb = new StringBuffer();

                @Override
                public void onNext(HelloWorldProto.HelloRequest value) {
                    System.out.println("[bidiHello] Server received: " + value.getGreeting());
                    sb.append(value.getGreeting());
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Stub onError");
                    responseObserver.onCompleted();
                }

                @Override
                public void onCompleted() {
                    System.out.println("Stub onCompleted");
                    responseObserver.onNext(HelloWorldProto.HelloResponse.newBuilder().setReply(
                            sb.toString() + "\n" + "Welcome!").build());
                    responseObserver.onNext(HelloWorldProto.HelloResponse.newBuilder().setReply("Byebye!").build());
                    responseObserver.onCompleted();
                }
            };
            responseObserver.onNext(HelloWorldProto.HelloResponse.newBuilder().setReply("Hello?").build());
            return streamObserver;
        }
    }
}
