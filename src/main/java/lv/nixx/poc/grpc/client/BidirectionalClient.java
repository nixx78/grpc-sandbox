package lv.nixx.poc.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lv.nixx.poc.grpc.proto.MessageServiceGrpc;
import lv.nixx.poc.grpc.proto.Request;
import lv.nixx.poc.grpc.proto.Response;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class BidirectionalClient {

    private final MessageServiceGrpc.MessageServiceStub asyncStub;
    private final ManagedChannel channel;

    public BidirectionalClient(ManagedChannel channel) {
        asyncStub = MessageServiceGrpc.newStub(channel);
        this.channel = channel;
    }

    public void sendMessages() {

        StreamObserver<Request> requestObserver = asyncStub.bidirectionalStreaming (new StreamObserver<>() {

            @Override
            public void onNext(Response response) {
                System.out.println("Received response: " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error receiving response: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed the communication.");
                channel.shutdown();
            }
        });

        try (Scanner scanner = new Scanner(System.in)) {
            String message;
            while (true) {
                System.out.print("Enter message (type 'exit' to stop): ");
                message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
                requestObserver.onNext(Request.newBuilder().setName(message).build());
            }
        } finally {
            requestObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BidirectionalClient client = new BidirectionalClient(channel);
        client.sendMessages();

        channel.awaitTermination(5, TimeUnit.SECONDS);
        channel.shutdown();
    }

}

