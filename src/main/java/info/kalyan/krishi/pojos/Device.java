package info.kalyan.krishi.pojos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class Device {

    public enum Status {
        Disabled,
        Registered,
        Enabled
    }

    @Id
    public String id;
    public String deviceID;
    public String userName;
    public Status status;
}
