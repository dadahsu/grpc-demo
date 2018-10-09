package com.ruckuswireless.grpcdemo.client;

import com.ruckuswireless.grpcdemo.HelloServiceGrpc;
import com.ruckuswireless.grpcdemo.HelloWorldProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BidiHelloClient {
    private final ManagedChannel channel;
    private final HelloServiceGrpc.HelloServiceStub stub;

    private static CountDownLatch done = new CountDownLatch(1);

    private final io.grpc.stub.StreamObserver<HelloWorldProto.HelloRequest> requestStreamObserver;
    private final io.grpc.stub.StreamObserver<HelloWorldProto.HelloResponse> responseStreamObserver =
            new io.grpc.stub.StreamObserver<HelloWorldProto.HelloResponse>() {

        StringBuffer sb = new StringBuffer();

        @Override
        public void onNext(HelloWorldProto.HelloResponse value) {
            System.out.println("Server response: " + value.getReply());

            if ("Hello?".equals(value.getReply())) {
                String greeting = "Hi! I'm Jimmy.";
                for (int i = 0; i < greeting.length(); i++) {
                    sayHi(greeting.substring(i, i+1));
                }
                completeStub();
            } else {
                sb.append(value.getReply());
            }

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

    public BidiHelloClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = HelloServiceGrpc.newStub(channel);
        this.requestStreamObserver = stub.bidiHello(responseStreamObserver);
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
        BidiHelloClient client = new BidiHelloClient("127.0.0.1", 50051);

        done.await();
        client.shutdown();
    }
}
