package lv.nixx.poc.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.concurrent.Executors;

public class GrpcServerRunner {

    private Server server;

    private void start() throws Exception {
        int port = 50051;

        int threadPoolSize = 5;
        var threadPool = Executors.newFixedThreadPool(threadPoolSize);

        server = ServerBuilder.forPort(port)
                .addService(new MessageServiceImpl())
                .addService(new CalculationServiceImpl())
                .executor(threadPool)
                .build()
                .start();

        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            GrpcServerRunner.this.stop();
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
        final GrpcServerRunner server = new GrpcServerRunner();
        server.start();
        server.blockUntilShutdown();
    }

}

