package lv.nixx.poc.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

import lv.nixx.poc.grpc.proto.HelloRequest;
import lv.nixx.poc.grpc.proto.HelloResponse;
import lv.nixx.poc.grpc.proto.HelloServiceGrpc;

public class GrpcClient {

    private final ManagedChannel channel;

    private final HelloServiceGrpc.HelloServiceBlockingStub blockingStub;

    public GrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();

        HelloResponse response = blockingStub.sayHello(request);
        System.out.println("Greeting: " + response.getMessage());
    }

    public static void main(String[] args) throws Exception {
        GrpcClient client = new GrpcClient("localhost", 50051);
        try {
            String name = "Name:" + System.currentTimeMillis();
            client.greet(name);
        } finally {
            client.shutdown();
        }
    }

}
