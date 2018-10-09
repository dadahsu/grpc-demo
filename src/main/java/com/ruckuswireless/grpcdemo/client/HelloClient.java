package com.ruckuswireless.grpcdemo.client;

import com.ruckuswireless.grpcdemo.HelloServiceGrpc;
import com.ruckuswireless.grpcdemo.HelloWorldProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class HelloClient {
    private final ManagedChannel channel;
    private final HelloServiceGrpc.HelloServiceBlockingStub stub;

    public HelloClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = HelloServiceGrpc.newBlockingStub(channel);
    }

    public String sayHi(String greeting) {
        return stub.sayHello(HelloWorldProto.HelloRequest.newBuilder().setGreeting(greeting).build()).getReply();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        HelloClient client = new HelloClient("127.0.0.1",50051);
        System.out.println(client.sayHi("Hi! I'm Jimmy."));
        client.shutdown();
    }
}
