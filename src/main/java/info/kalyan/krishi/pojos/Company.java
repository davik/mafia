package info.kalyan.krishi.pojos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "company")
public class Company {
    @Id
    public String id;
    public String companyName;
    public String companyCode;
}
