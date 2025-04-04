package lv.nixx.poc.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lv.nixx.poc.grpc.proto.CalculationRequest;
import lv.nixx.poc.grpc.proto.CalculationResponse;
import lv.nixx.poc.grpc.proto.CalculationServiceGrpc;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class BidirectionalCalculationClient {

    private final CalculationServiceGrpc.CalculationServiceStub asyncStub;
    private final ManagedChannel channel;

    public BidirectionalCalculationClient(ManagedChannel channel) {
        asyncStub = CalculationServiceGrpc.newStub(channel);
        this.channel = channel;
    }

    public void sendMessages() {

        StreamObserver<CalculationRequest> requestObserver = asyncStub.calculate(new StreamObserver<>() {

            @Override
            public void onNext(CalculationResponse response) {
                System.out.println("Received response: " + response);
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
                System.out.print("Enter numbers (type 'exit' to stop): ");
                message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }

                String[] parts = message.split("\\s+"); // Разбиваем строку по пробелу

                if (parts.length != 2) {
                    System.out.println("Please enter exactly two numbers.");
                    continue;
                }

                var num1 = parts[0];
                var num2 = parts[1];

                requestObserver.onNext(CalculationRequest.newBuilder()
                        .setFirstNumber(num1)
                        .setSecondNumber(num2)
                        .build());

            }
        } finally {
            requestObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BidirectionalCalculationClient client = new BidirectionalCalculationClient(channel);
        client.sendMessages();

        channel.awaitTermination(5, TimeUnit.SECONDS);
        channel.shutdown();
    }

}

