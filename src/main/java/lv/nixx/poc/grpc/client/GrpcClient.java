package lv.nixx.poc.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lv.nixx.poc.grpc.proto.MessageServiceGrpc;
import lv.nixx.poc.grpc.proto.Request;
import lv.nixx.poc.grpc.proto.Response;

import java.util.concurrent.TimeUnit;

public class GrpcClient {

    private final ManagedChannel channel;

    private final MessageServiceGrpc.MessageServiceBlockingStub blockingStub;

    public GrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = MessageServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void sendMessage(String name) {

        Request request = Request.newBuilder()
                .setName(name)
                .build();

        Response response = blockingStub.processMessage(request);
        System.out.println("Message response: " + response.getMessage() + ":" + response.getDateTime());
    }

    public static void main(String[] args) throws Exception {
        GrpcClient client = new GrpcClient("localhost", 50051);
        try {
            String name = "Name:" + System.currentTimeMillis();
            client.sendMessage(name);
        } finally {
            client.shutdown();
        }
    }

}
