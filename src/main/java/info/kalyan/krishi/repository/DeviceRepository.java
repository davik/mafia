package info.kalyan.krishi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import info.kalyan.krishi.pojos.Device;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {
}