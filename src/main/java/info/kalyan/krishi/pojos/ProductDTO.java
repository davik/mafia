package info.kalyan.krishi.pojos;

import java.util.List;
import java.util.ArrayList;

public class ProductDTO {
    public String name = "";
    public String manufacturer = "";
    public List<WareHouseStock> openingStocks = new ArrayList<WareHouseStock>();
}