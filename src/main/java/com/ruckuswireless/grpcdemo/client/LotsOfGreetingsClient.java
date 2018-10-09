package com.ruckuswireless.grpcdemo.client;

import com.ruckuswireless.grpcdemo.HelloServiceGrpc;
import com.ruckuswireless.grpcdemo.HelloWorldProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LotsOfGreetingsClient {
    private final ManagedChannel channel;
    private final HelloServiceGrpc.HelloServiceStub stub;

    private static CountDownLatch done = new CountDownLatch(1);

    private final io.grpc.stub.StreamObserver<HelloWorldProto.HelloRequest> requestStreamObserver;
    private final io.grpc.stub.StreamObserver<HelloWorldProto.HelloResponse> responseStreamObserver =
            new io.grpc.stub.StreamObserver<HelloWorldProto.HelloResponse>() {

        @Override
        public void onNext(HelloWorldProto.HelloResponse value) {
            System.out.println("Server response: " + value.getReply());
        }

        @Override
        public void onError(Throwable t) {
            System.err.println("onError");
            done.countDown();
        }

        @Override
        public void onCompleted() {
            System.out.println("onCompleted");
            done.countDown();
        }
    };

    public LotsOfGreetingsClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = HelloServiceGrpc.newStub(channel);
        this.requestStreamObserver = stub.lotsOfGreetings(responseStreamObserver);
    }

    public void sayHi(String greeting) {
        System.out.println("Client send: " + greeting);
        requestStreamObserver.onNext(HelloWorldProto.HelloRequest.newBuilder().setGreeting(greeting).build());
    }

    public void completeStub() {
        requestStreamObserver.onCompleted();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        LotsOfGreetingsClient client = new LotsOfGreetingsClient("127.0.0.1", 50051);
        String greeting = "Hi! I'm Jimmy.";
        for (int i = 0; i < greeting.length(); i++) {
            client.sayHi(greeting.substring(i, i+1));
        }
        client.completeStub();
        done.await();
        client.shutdown();
    }
}
