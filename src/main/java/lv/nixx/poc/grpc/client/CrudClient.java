package lv.nixx.poc.grpc.client;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lv.nixx.poc.grpc.crud.PeopleServiceGrpc;
import lv.nixx.poc.grpc.crud.PeopleServiceProto.*;

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
            System.out.println("Initial People list: " + peopleList);

            try {
                System.out.println("Try to get not existing person");
                client.blockingStub.getPerson(GetPersonRequest.newBuilder().setId(777).build());
            } catch (StatusRuntimeException ex) {
                System.out.println(ex.getStatus() + ":" + ex.getMessage());
            }

            PersonResponse person = client.blockingStub.getPerson(GetPersonRequest.newBuilder().setId(1).build());
            System.out.printf("Get Person by id [%s] [%s]%n", 1, person.getPerson());

            PersonResponse newPerson = client.blockingStub.createPerson(CreatePersonRequest.newBuilder()
                    .setName("New name")
                    .setSurname("New surname")
                    .build());

            System.out.println("New person created: " + newPerson);

            int id = newPerson.getPerson().getId();
            client.blockingStub.updatePerson(UpdatePersonRequest.newBuilder()
                            .setPerson(Person.newBuilder()
                                    .setId(id)
                                    .setName("Updated name")
                                    .setSurname("Updated surname")
                                    .build())
                    .build());

            System.out.println("People list after update: " + client.blockingStub.listPersons(Empty.newBuilder().build()).getPeopleList());

            try {
                client.blockingStub.deletePerson(DeletePersonRequest.newBuilder().setId(id).build());
            } catch (StatusRuntimeException ex) {
                System.out.println(ex.getStatus() + ":" + ex.getMessage());
            }
            System.out.println("People list after delete: " + client.blockingStub.listPersons(Empty.newBuilder().build()).getPeopleList());

        } finally {
            client.shutdown();
        }
    }

}
