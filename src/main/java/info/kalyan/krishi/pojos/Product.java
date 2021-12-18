package info.kalyan.krishi.pojos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
	@Id
	public String id;
	public String name = "";
	public String manufacturer = "";
	public List<WareHouseStock> openingStocks = new ArrayList<>();
}