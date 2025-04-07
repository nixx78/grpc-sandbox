package lv.nixx.poc.grpc.client;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lv.nixx.poc.grpc.crud.PeopleServiceGrpc;
import lv.nixx.poc.grpc.crud.PeopleServiceProto.GetPersonRequest;
import lv.nixx.poc.grpc.crud.PeopleServiceProto.ListPersonsResponse;
import lv.nixx.poc.grpc.crud.PeopleServiceProto.Person;
import lv.nixx.poc.grpc.crud.PeopleServiceProto.PersonResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrudClient {

    private final ManagedChannel channel;

    private final PeopleServiceGrpc.PeopleServiceBlockingStub blockingStub;

    public CrudClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = PeopleServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        CrudClient client = new CrudClient("localhost", 50051);
        try {

            ListPersonsResponse response = client.blockingStub.listPersons(Empty.newBuilder().build());
            List<Person> peopleList = response.getPeopleList();
            System.out.println("People list: " + peopleList);

            try {
                client.blockingStub.getPerson(GetPersonRequest.newBuilder().setId(777).build());
            } catch (StatusRuntimeException ex) {
                System.out.println(ex.getStatus() + ":" + ex.getMessage());
            }

            System.out.println("--------------------------------");
            PersonResponse person = client.blockingStub.getPerson(GetPersonRequest.newBuilder().setId(1).build());

            System.out.println(person.getPerson());
        } finally {
            client.shutdown();
        }
    }

}
