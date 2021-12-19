package info.kalyan.krishi.repository;

import info.kalyan.krishi.pojos.Vehicle;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Vehicle findByRegistrationNumber(String registrationNumber);

    List<Vehicle> findAllByRegistrationNumberLike(String registrationNumber);
}