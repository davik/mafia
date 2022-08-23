package info.kalyan.krishi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import info.kalyan.krishi.pojos.Company;

@Repository
public interface CompanyRepository extends MongoRepository<Company, String> {
}