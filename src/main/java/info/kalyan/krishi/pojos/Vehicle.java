package info.kalyan.krishi.pojos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vehicles")
public class Vehicle {

    public enum Status {
        WHITE, RED, GREEN;
    }

    @Id
    public String id;
    @Indexed
    public String registrationNumber;
    public String assetDesc;
    public String customerName;
    public String agent;
    public Status status = Status.WHITE;

    public Vehicle() {
    }

    public Vehicle(String registrationNumber, String assetDesc, String customerName, String agent, Status status) {
        this.registrationNumber = registrationNumber;
        this.assetDesc = assetDesc;
        this.customerName = customerName;
        this.agent = agent;
        this.status = status;
    }
}
