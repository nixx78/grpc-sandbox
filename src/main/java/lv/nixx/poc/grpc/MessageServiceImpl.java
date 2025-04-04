package lv.nixx.poc.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lv.nixx.poc.grpc.proto.MessageServiceGrpc;
import lv.nixx.poc.grpc.proto.Request;
import lv.nixx.poc.grpc.proto.Response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;

class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {

    @Override
    public void processMessage(Request request, StreamObserver<Response> responseObserver) {

        String name = request.getName();
        String greeting = "Hello, " + name;

        System.out.printf("Thread [" + Thread.currentThread().getName() + "] Request from client [%s]%n", greeting);

        Timestamp timestamp = getTimestamp();

        Response response = Response.newBuilder()
                .setMessage(greeting)
                .setDateTime(timestamp)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Request> processMessagesAsStream(StreamObserver<Response> responseObserver) {
        return new StreamObserver<>() {

            private final Collection<Request> allMessages = new ArrayList<>();

            @Override
            public void onNext(Request request) {
                System.out.println(Thread.currentThread().getName() + ": Message received from client: " + request.getName());
                allMessages.add(request);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error receiving message: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println(Thread.currentThread().getName() + ": client finish message send, send response to client back");
                responseObserver.onNext(Response.newBuilder()
                        .setMessage("Server receive all messages, count " + allMessages.size())
                        .setDateTime(getTimestamp())
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    private static Timestamp getTimestamp() {
        LocalDateTime localDateTime = LocalDateTime.now();

        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

}
