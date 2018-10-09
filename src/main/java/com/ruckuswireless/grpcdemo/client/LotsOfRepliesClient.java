package com.ruckuswireless.grpcdemo.client;

import com.ruckuswireless.grpcdemo.HelloServiceGrpc;
import com.ruckuswireless.grpcdemo.HelloWorldProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LotsOfRepliesClient {
    private final ManagedChannel channel;
    private final HelloServiceGrpc.HelloServiceStub stub;

    private static CountDownLatch done = new CountDownLatch(1);

    private final io.grpc.stub.StreamObserver<HelloWorldProto.HelloResponse> responseStreamObserver =
            new io.grpc.stub.StreamObserver<HelloWorldProto.HelloResponse>() {

        StringBuffer sb = new StringBuffer();

        @Override
        public void onNext(HelloWorldProto.HelloResponse value) {
            System.out.println("Server response: " + value.getReply());
            sb.append(value.getReply());
        }

        @Override
        public void onError(Throwable t) {
            System.err.println("onError");
            done.countDown();
        }

        @Override
        public void onCompleted() {
            System.out.println("onCompleted:" + sb.toString());
            done.countDown();
        }
    };

    public LotsOfRepliesClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = HelloServiceGrpc.newStub(channel);
    }

    public void sayHi(String greeting) {
        System.out.println("Client send: " + greeting);
        stub.lotsOfReplies(HelloWorldProto.HelloRequest.newBuilder().setGreeting(greeting).build(), responseStreamObserver);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        LotsOfRepliesClient client = new LotsOfRepliesClient("127.0.0.1", 50051);
        client.sayHi("Hi! I'm Jimmy.");
        done.await();
        client.shutdown();
    }
}
