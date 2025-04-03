package lv.nixx.poc.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import io.grpc.stub.StreamObserver;
import lv.nixx.poc.grpc.proto.HelloRequest;
import lv.nixx.poc.grpc.proto.HelloResponse;

import lv.nixx.poc.grpc.proto.HelloServiceGrpc;

public class GrpcServer {

    private Server server;

    private void start() throws Exception {
        int port = 50051;

        server = ServerBuilder.forPort(port)
                .addService(new HelloImpl())
                .build()
                .start();

        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            GrpcServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    // Await termination on the main thread since the grpc library uses daemon threads.
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        final GrpcServer server = new GrpcServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class HelloImpl extends HelloServiceGrpc.HelloServiceImplBase {

        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

            String name = request.getName();
            String greeting = "Hello, " + name;

            System.out.printf("Request from client [%s]%n", greeting);

            HelloResponse response = HelloResponse.newBuilder()
                    .setMessage(greeting)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    }
}

