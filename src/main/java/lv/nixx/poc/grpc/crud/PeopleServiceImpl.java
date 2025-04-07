package lv.nixx.poc.grpc.crud;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lv.nixx.poc.grpc.crud.PeopleServiceProto.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class PeopleServiceImpl extends PeopleServiceGrpc.PeopleServiceImplBase {

    Map<Integer, Person> personMap = new HashMap<>();

    public PeopleServiceImpl() {

        Person person1 = Person.newBuilder()
                .setId(1)
                .setName("Alice")
                .setSurname("Wonderland")
                .setDateOfBirth(toTimestamp(LocalDate.of(1993, 5, 21)))
                .build();

        Person person2 = Person.newBuilder()
                .setId(2)
                .setName("Bob")
                .setSurname("Dilan")
                .setDateOfBirth(toTimestamp(LocalDate.of(1998, 11, 2)))
                .build();

        personMap.put(person1.getId(), person1);
        personMap.put(person2.getId(), person2);
    }

    @Override
    public void createPerson(CreatePersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        //TODO Implement this method
    }

    @Override
    public void getPerson(GetPersonRequest request, StreamObserver<PersonResponse> responseObserver) {

        int id = request.getId();
        Person person = personMap.get(id);

        if (person == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Person with id " + id + " not found")
                    .asRuntimeException());
        } else {
            responseObserver.onNext(PersonResponse.newBuilder().setPerson(person).build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void updatePerson(UpdatePersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        //TODO Implement this method
    }

    @Override
    public void deletePerson(DeletePersonRequest request, StreamObserver<Empty> responseObserver) {
        //TODO Implement this method
    }

    @Override
    public void listPersons(Empty request, StreamObserver<ListPersonsResponse> responseObserver) {

        ListPersonsResponse response = ListPersonsResponse.newBuilder()
                .addAllPeople(personMap.values())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Timestamp toTimestamp(LocalDate localDate) {
        return Timestamps.fromMillis(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

}
