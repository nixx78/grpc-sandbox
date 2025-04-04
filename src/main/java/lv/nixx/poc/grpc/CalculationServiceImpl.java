package lv.nixx.poc.grpc;

import io.grpc.stub.StreamObserver;
import lv.nixx.poc.grpc.proto.CalculationRequest;
import lv.nixx.poc.grpc.proto.CalculationResponse;
import lv.nixx.poc.grpc.proto.CalculationServiceGrpc;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

public class CalculationServiceImpl extends CalculationServiceGrpc.CalculationServiceImplBase {

    @Override
    public StreamObserver<CalculationRequest> calculate(StreamObserver<CalculationResponse> responseObserver) {

        return new StreamObserver<>() {

            AtomicInteger calculationCount = new AtomicInteger(0);

            @Override
            public void onNext(CalculationRequest request) {
                System.out.println(Thread.currentThread().getName() + ": Received request from client: \n " + request.toString());


                var firstNumberBd = new BigDecimal(request.getFirstNumber());
                var secondNumberBd = new BigDecimal(request.getSecondNumber());

                CalculationResponse response = CalculationResponse.newBuilder()
                        .setFirstNumber(request.getFirstNumber())
                        .setSecondNumber(request.getSecondNumber())
                        .setResult("" + firstNumberBd.add(secondNumberBd))
                        .build();

                responseObserver.onNext(response);
                calculationCount.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error receiving message: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Server finished processing messages from client. Send final message to client");

                responseObserver.onNext(CalculationResponse.newBuilder()
                        .setTotalResult("FinalMessage, calculation count:" + calculationCount.get())
                        .build());

                responseObserver.onCompleted();
            }
        };
    }
}
