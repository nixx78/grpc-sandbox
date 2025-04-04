package lv.nixx.poc.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lv.nixx.poc.grpc.proto.MessageServiceGrpc;
import lv.nixx.poc.grpc.proto.Request;
import lv.nixx.poc.grpc.proto.Response;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class GrpcStreamClient {

    public static void main(String[] args) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        MessageServiceGrpc.MessageServiceStub asyncStub = MessageServiceGrpc.newStub(channel);

        StreamObserver<Request> requestObserver = asyncStub.processMessagesAsStream(new StreamObserver<>() {
            @Override
            public void onNext(Response response) {
                System.out.println("Response from server: " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("OnCompleted.");

                try {
                    channel.awaitTermination(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                channel.shutdown();
            }
        });

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter message (enter 'exit' to exit):");

        while (true) {
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                requestObserver.onCompleted();
                break;
            }
            requestObserver.onNext(Request.newBuilder().setName(input).build());
        }

        TimeUnit.SECONDS.sleep(5);
    }

}
