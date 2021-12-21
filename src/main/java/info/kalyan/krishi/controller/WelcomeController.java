package info.kalyan.krishi.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import info.kalyan.krishi.pojos.Counter;
import info.kalyan.krishi.pojos.Product;
import info.kalyan.krishi.pojos.ProductDTO;
import info.kalyan.krishi.pojos.StockDTO;
import info.kalyan.krishi.pojos.Vehicle;
import info.kalyan.krishi.pojos.Voucher;
import info.kalyan.krishi.pojos.VoucherDTO;
import info.kalyan.krishi.pojos.Warehouse;
import info.kalyan.krishi.pojos.WarehouseDTO;
import info.kalyan.krishi.pojos.WareHouseStock;
import info.kalyan.krishi.pojos.Voucher.VoucherType;
import info.kalyan.krishi.pojos.Vendor;
import info.kalyan.krishi.pojos.VendorDTO;
import info.kalyan.krishi.repository.CounterRepository;
import info.kalyan.krishi.repository.ProductRepository;
import info.kalyan.krishi.repository.VehicleRepository;
import info.kalyan.krishi.repository.VendorRepository;
import info.kalyan.krishi.repository.VoucherRepository;
import info.kalyan.krishi.repository.WarehouseRepository;

@Controller
public class WelcomeController {

	public WelcomeController(VendorRepository vendorRepo, CounterRepository counterRepo, WarehouseRepository whRepo,
			ProductRepository productRepo, VoucherRepository voucherRepo, VehicleRepository vehicleRepo) {
		super();
		this.vendorRepo = vendorRepo;
		this.counterRepo = counterRepo;
		this.whRepo = whRepo;
		this.productRepo = productRepo;
		this.voucherRepo = voucherRepo;
		this.vehicleRepo = vehicleRepo;
		populateProductCache();
		populateVendorCache();
		populateWarehouseCache();
	}

	// inject via application.properties
	@Value("${app.welcome.message}")
	private String message = "";

	@Value("${app.welcome.title}")
	private String title = "";

	private final String alert = "alert alert-danger";

	@Autowired
	public final VendorRepository vendorRepo;
	@Autowired
	public final CounterRepository counterRepo;
	@Autowired
	public final WarehouseRepository whRepo;
	@Autowired
	public final ProductRepository productRepo;
	@Autowired
	public final VoucherRepository voucherRepo;
	@Autowired
	public final VehicleRepository vehicleRepo;

	public HashMap<String, Product> productCache;
	public HashMap<String, Vendor> vendorCache;
	public HashMap<String, Warehouse> warehouseCache;

	public void populateProductCache() {
		List<Product> products = productRepo.findAll();
		if (productCache == null) {
			productCache = new HashMap<>();
		}
		for (Product product : products) {
			productCache.put(product.id, product);
		}
	}

	public void populateVendorCache() {
		List<Vendor> vendors = vendorRepo.findAll();
		if (vendorCache == null) {
			vendorCache = new HashMap<>();
		}
		for (Vendor vd : vendors) {
			vendorCache.put(vd.id, vd);
		}
	}

	public void populateWarehouseCache() {
		List<Warehouse> warehouses = whRepo.findAll();
		if (warehouseCache == null) {
			warehouseCache = new HashMap<>();
		}
		for (Warehouse wh : warehouses) {
			warehouseCache.put(wh.id, wh);
		}
	}

	@GetMapping(path = "/")
	public String welcome(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		if (request.getRemoteUser().equals("admin")) {
			model.put("admin", true);
		} else {
			model.put("admin", false);
		}

		return "welcome";
	}

	@GetMapping(path = "/upload")
	public String upload(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		if (request.getRemoteUser().equals("admin")) {
			model.put("admin", true);
		} else {
			model.put("admin", false);
		}

		return "upload";
	}

	@PostMapping(value = "/uploadFile")
	@ResponseBody
	public String uploadFile(@RequestParam("file") MultipartFile file)
			throws UnsupportedEncodingException, IOException {
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();

		List<String[]> allData = csvReader.readAll();
		List<Vehicle> vehicles = new ArrayList<>();

		try {
			for (String[] strings : allData) {
				if (strings[0].equals(""))
					continue;
				Vehicle vh = new Vehicle(strings[0].trim(), strings[1].trim(), strings[2].trim(), strings[3].trim(),
						Vehicle.Status.WHITE);
				vehicles.add(vh);
			}
		} catch (Exception e) {
			return "Error in uploading file or wrong file format";
		}

		vehicleRepo.saveAll(vehicles);
		return "File uploaded successfully";
	}

	@GetMapping(path = "/markedVehicles")
	@ResponseBody
	public List<Vehicle> markedVehicles() {

		List<Vehicle> vhs = vehicleRepo.findAllByStatusIn(Arrays.asList(Vehicle.Status.RED, Vehicle.Status.GREEN));
		return vhs;
	}

	@GetMapping(path = "/vehicleDetails")
	@ResponseBody
	public List<Vehicle> vehicleDetails(@RequestParam("registrationNumber") String registrationNumber) {
		List<Vehicle> vh = vehicleRepo
				.findAllByRegistrationNumberLikeOrderByRegistrationNumberAsc(registrationNumber.toUpperCase());
		return vh;
	}

	@GetMapping(path = "/vehicleQuery")
	@ResponseBody
	public List<Vehicle> vehicleQuery(@RequestParam("q") String queryString) {

		List<Vehicle> vh = vehicleRepo
				.findAllByRegistrationNumberLikeOrderByRegistrationNumberAsc(queryString.toUpperCase());
		return vh;
	}

	@GetMapping(path = "/changeStatus")
	@ResponseBody
	public String changeStatus(@RequestParam("registrationNumber") String registrationNumber,
			@RequestParam("status") Vehicle.Status status) {
		List<Vehicle> vh = vehicleRepo
				.findAllByRegistrationNumberLikeOrderByRegistrationNumberAsc(registrationNumber.toUpperCase());
		for (Vehicle v : vh) {
			v.status = status;
			vehicleRepo.save(v);
		}

		return "Status Changed";
	}

	// test 5xx errors
	@GetMapping("/5xx")
	public String serviceUnavailable() {
		throw new RuntimeException("ABC");
	}

	@PostMapping(path = "/create")
	public String create(Map<String, Object> model, @RequestBody VendorDTO vendor, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (vendor.name.isEmpty() || vendor.mobile.isEmpty()) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		String idPrefix = "vendor";
		// Counter is used to get the next id to be assigned for a new vendor based on
		// session and course
		Optional<Counter> oct = counterRepo.findById(idPrefix);
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = idPrefix;
			ct.nextId++;
		} else {
			ct = oct.get();
		}
		Vendor vd = new Vendor();
		// Increment and save the counter config
		vd.id = String.format("%03d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		vd.name = vendor.name;
		vd.aadhaar = vendor.aadhaar;
		vd.mobile = vendor.mobile;
		vd.email = vendor.email;
		vd.address1 = vendor.address1;
		vd.creditBalance = vendor.openingBalance;

		// Add Opening Balance Voucher
		Voucher voucher = new Voucher();
		voucher.voucherId = GetNextVoucherId();
		voucher.transactionDate = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));
		voucher.voucherType = VoucherType.OPENING_BALANCE;
		voucher.value = vendor.openingBalance;
		voucher.vendorId = vd.id;
		voucher.vendorName = vendor.name;

		voucherRepo.save(voucher);
		vendorRepo.save(vd);

		populateVendorCache();

		model.put("alert", "alert alert-success");
		model.put("result", "Vendor Registered Successfully!");
		return "create";
	}

	@PostMapping(path = "/createWarehouse")
	public String warehouseCreate(Map<String, Object> model, @RequestBody WarehouseDTO wareHouseDTO,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (wareHouseDTO.name.isEmpty() || wareHouseDTO.location.isEmpty()) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		Warehouse existingWarehouse = whRepo.findByName(wareHouseDTO.name);
		if (existingWarehouse != null) {
			model.put("alert", alert);
			model.put("result", "Warehouse " + wareHouseDTO.name + " already exists!");
			return "create";
		}
		String idPrefix = "warehouse";
		// Counter is used to get the next id to be assigned for a new warehouse
		Optional<Counter> oct = counterRepo.findById(idPrefix);
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = idPrefix;
			ct.nextId++;
		} else {
			ct = oct.get();
		}
		Warehouse wHouse = new Warehouse();
		wHouse.name = wareHouseDTO.name;
		wHouse.location = wareHouseDTO.location;
		// Increment and save the counter config
		wHouse.id = String.format("%03d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		whRepo.save(wHouse);
		populateWarehouseCache();

		model.put("alert", "alert alert-success");
		model.put("result", "Warehouse Registered Successfully!");
		return "create";
	}

	@PostMapping(path = "/createProduct")
	public String productCreate(Map<String, Object> model, @RequestBody ProductDTO productDTO,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (productDTO.name.isEmpty() || productDTO.manufacturer.isEmpty()) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		Product existingProduct = productRepo.findByNameAndManufacturer(productDTO.name, productDTO.manufacturer);
		if (existingProduct != null) {
			model.put("alert", alert);
			model.put("result", "Product " + productDTO.name + " already exists!");
			return "create";
		}
		String idPrefix = "product";
		// Counter is used to get the next id to be assigned for a new warehouse
		Optional<Counter> oct = counterRepo.findById(idPrefix);
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = idPrefix;
			ct.nextId++;
		} else {
			ct = oct.get();
		}
		Product product = new Product();
		product.name = productDTO.name;
		product.manufacturer = productDTO.manufacturer;
		product.openingStocks = productDTO.openingStocks;
		// Increment and save the counter config
		product.id = String.format("%03d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		productRepo.save(product);

		populateProductCache();

		model.put("alert", "alert alert-success");
		model.put("result", "Product Registered Successfully!");
		return "create";
	}

	@GetMapping(path = "/registration")
	public String getRegistrationPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		model.put("warehouses", warehouseCache.values());
		return "registration";
	}

	@GetMapping(path = "/contact")
	public String getContactPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		return "contact";
	}

	@GetMapping(path = "/vendors")
	public String getVendorsPage(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		String agent = "BAJAJ";
		String inputFileName = "";
		FileReader fileReader = null;
		CSVReader csvReader = null;
		List<String[]> allData = null;
		List<Vehicle> vehicles = new ArrayList<>();

		switch (agent) {
			case "BAJAJ":
				inputFileName = "C:\\Users\\avik\\Downloads\\BAJAJ_REPO_ELIGIBLE_LIST_DEC21.csv";
				fileReader = new FileReader(inputFileName);
				csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
				allData = csvReader.readAll();

				for (String[] strings : allData) {
					if (strings[3].equals(""))
						continue;
					Vehicle vh = new Vehicle(strings[3].trim(), strings[6], strings[1], agent, Vehicle.Status.WHITE);
					vehicles.add(vh);
				}
				break;
			case "RKG":
				inputFileName = "C:\\Users\\avik\\Downloads\\RKG.csv";
				fileReader = new FileReader(inputFileName);
				csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
				allData = csvReader.readAll();
				for (String[] strings : allData) {
					if (strings[1].equals(""))
						continue;
					Vehicle vh = new Vehicle(strings[1].trim(), strings[2], "", agent, Vehicle.Status.WHITE);
					vehicles.add(vh);
				}
				break;
		}
		vehicleRepo.saveAll(vehicles);
		return "vendors";
	}

	@GetMapping(path = "/stock")
	public String getStockPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		List<StockDTO> stocks = new ArrayList<>();
		for (Entry<String, Product> entry : productCache.entrySet()) {
			StockDTO stock = new StockDTO();
			stock.productId = entry.getKey();
			stock.productName = entry.getValue().name + "(" + entry.getValue().manufacturer + ")";

			List<Voucher> prodVouchers = voucherRepo.findByProductId(entry.getValue().id);

			HashMap<String, Integer> whSpecificStock = new HashMap<>();
			for (Entry<String, Warehouse> whs : warehouseCache.entrySet()) {
				int openingStock = 0;
				for (WareHouseStock whst : entry.getValue().openingStocks) {
					if (whst.wareHouseName.equals(whs.getValue().name)) {
						openingStock = whst.openingStock;
						break;
					}
				}
				whSpecificStock.put(whs.getKey(), openingStock);
			}

			for (Voucher vc : prodVouchers) {
				// For Direct Sales Stock update is not required
				if (vc.warehouseId.equals("-1")) {
					continue;
				}
				int updatedStock = whSpecificStock.get(vc.warehouseId);
				if (vc.voucherType == VoucherType.PURCHASE) {
					updatedStock += vc.quantity;
				} else if (vc.voucherType == VoucherType.SALE) {
					updatedStock -= vc.quantity;
				}
				whSpecificStock.put(vc.warehouseId, updatedStock);
			}

			stock.currentStocks.addAll(whSpecificStock.values());
			stocks.add(stock);
		}
		model.put("stocks", stocks);
		model.put("warehouses", warehouseCache.values());
		return "stock";
	}

	@GetMapping(path = "/payment")
	public String getPaymentPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		return "payment";
	}

	@GetMapping(path = "/report")
	public String getReportPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		return "report";
	}

	@GetMapping(value = "/accountDetails")
	public String getVoucherDetails(Map<String, Object> model, @RequestParam(name = "id") String vendorId,
			@RequestParam(name = "from") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") DateTime from,
			@RequestParam(name = "to") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") DateTime to,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		Optional<Vendor> ovd = vendorRepo.findById(vendorId);
		if (!ovd.isPresent()) {
			model.put("alert", alert);
			model.put("result", "Vendor not found!");
		} else {
			Vendor vendor = ovd.get();
			// For Account Details page
			to = to.plusDays(1);
			List<Voucher> vouchers = voucherRepo.findByTransactionDateBetweenAndVendorId(from, to, vendorId);
			model.put("vendor", new VendorDTO(vendor, vouchers));

			// For Voucher Form
			model.put("products", productCache.values());
			Warehouse wh = new Warehouse("-1", "Direct", "For Transfer Only");
			List<Warehouse> whList = new ArrayList<>();
			whList.addAll(warehouseCache.values());
			whList.add(wh);
			model.put("warehouses", whList);
			model.put("voucherType", getNames(VoucherType.class));
		}
		return "accountDetails";
	}

	@GetMapping(value = "/stockDetails")
	public String getStockDetails(Map<String, Object> model, @RequestParam(name = "id") String productId,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		Product product = productCache.get(productId);
		if (null == product) {
			model.put("alert", alert);
			model.put("result", "Product not found!");
		} else {
			// For Product Details page
			List<Voucher> vouchers = voucherRepo.findByProductId(productId);
			List<VoucherDTO> voucherDTOs = new ArrayList<>();
			for (Voucher vc : vouchers) {
				// Stock will not be shown for direct sales
				if (vc.warehouseId.equals("-1")) {
					continue;
				}
				VoucherDTO vdto = new VoucherDTO(vc);
				voucherDTOs.add(vdto);
			}

			ProductDTO pdto = new ProductDTO();
			pdto.name = product.name;
			pdto.manufacturer = product.manufacturer;

			model.put("product", pdto);
			model.put("vouchers", voucherDTOs);
		}
		return "productDetails";
	}

	@PostMapping(path = "/createVoucher")
	public String setVoucherDetails(Map<String, Object> model, @RequestParam(name = "id") String vendorId,
			@RequestBody Voucher voucher, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		Optional<Vendor> ovd = vendorRepo.findById(vendorId);
		if (!ovd.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "vendor not found!");
			return "create";
		} else {
			Vendor vendor = ovd.get();
			if (voucher.value == 0) {
				model.put("alert", alert);
				model.put("result", "Please fill the mandatory fields!");
				return "create";
			}

			voucher.voucherId = GetNextVoucherId();
			if (voucher.transactionDate == null) {
				voucher.transactionDate = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));
			}
			if (voucher.voucherType == VoucherType.PURCHASE || voucher.voucherType == VoucherType.RECEIPT) {
				vendor.creditBalance += voucher.value;
			} else if (voucher.voucherType == VoucherType.PAYMENT || voucher.voucherType == VoucherType.SALE) {
				vendor.creditBalance -= voucher.value;
			}
			voucher.balance = vendor.creditBalance;
			voucher.vendorId = vendorId;
			voucher.vendorName = vendorCache.get(vendorId).name;
			if (!voucher.productId.isEmpty()) {
				Product pd = productCache.get(voucher.productId);
				voucher.productName = pd.name;
				voucher.manufacturer = pd.manufacturer;
			}
			if (!voucher.warehouseId.isEmpty()) {
				if (!voucher.warehouseId.equals("-1")) {
					Warehouse wh = warehouseCache.get(voucher.warehouseId);
					voucher.warehouseName = wh.name;
				} else {
					voucher.warehouseName = "DIRECT";
				}
			}
			voucherRepo.save(voucher);
			vendorRepo.save(vendor);
		}
		model.put("alert", "alert alert-success");
		model.put("result", "Information Recorded Successfully!");
		return "accountDetails";
	}

	public int getFiscalYear(Calendar calendar) {
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		return (month > Calendar.MARCH) ? year : year - 1;
	}

	public void populateCommonPageFields(Map<String, Object> model, HttpServletRequest request) {
		model.put("title", title);
		model.put("message", message);
		model.put("user", request.getRemoteUser());
	}

	@GetMapping(value = "/stockReport")
	public void generateStockReport(Map<String, Object> model,
			@RequestParam(name = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime from,
			@RequestParam(name = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime to,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		if (from == null || to == null) {
			return;
		}

		List<Voucher> vouchers = voucherRepo.findByTransactionDateBetween(from, to.plusDays(1));

		String outputFileName = "C:\\Users\\polaris2\\" + "paydue.csv";
		File reportFile = new File(outputFileName);

		try {
			// create FileWriter object with file as parameter
			FileWriter outputfile = new FileWriter(reportFile);

			// create CSVWriter object filewriter object as parameter
			CSVWriter writer = new CSVWriter(outputfile);

			// create a List which contains String array
			List<String[]> data = new ArrayList<>();
			data.add(new String[] { "Date", "Name", "Mobile", "Rate", "Quantity", "Unit", "Value" });
			double totalStock = 0;
			double totalValue = 0;

			if (null != vouchers) {
				for (Voucher voucher : vouchers) {
					Vendor vendor = vendorCache.get(voucher.vendorId);
					if (voucher.voucherType == VoucherType.PURCHASE && voucher.transactionDate.isAfter(from)
							&& voucher.transactionDate.isBefore(to.plusDays(1))) {
						data.add(new String[] { voucher.transactionDate.toDate().toString(), vendor.name, vendor.mobile,
								Double.toString(voucher.rate), Double.toString(voucher.quantity), voucher.unit,
								Double.toString(voucher.value) });
						totalStock += voucher.quantity;
						totalValue += voucher.value;
					}
				}
			}

			data.add(new String[] { "", "", "", "", "", "", "" });
			data.add(new String[] { "Total", "", "", "", Double.toString(totalStock), "Kg",
					Double.toString(totalValue) });
			writer.writeAll(data);

			// closing writer connection
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "StockReport" + "_" + from.getDayOfMonth() + "_" + from.getMonthOfYear() + "_"
				+ to.getDayOfMonth() + "_" + to.getMonthOfYear() + ".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + reportFileName + "\""));
		response.setContentLength((int) reportFile.length());
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile))) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		}
		model.put("alert", "alert alert-success");
		model.put("result", "Report Generated Successfully!");
	}

	public static String[] getNames(Class<? extends Enum<?>> e) {
		return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
	}

	public String GetNextVoucherId() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
		// Fetch the Payment or Money Receipt ID counter
		int year = getFiscalYear(calendar);
		Optional<Counter> oct = counterRepo.findById(year + "-" + String.valueOf(year + 1).substring(2));
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = year + "-" + String.valueOf(year + 1).substring(2);
			ct.nextId++;

		} else {
			ct = oct.get();
		}

		String nextID = ct.id + "/" + String.format("%05d", ct.nextId);

		// Save in DB
		ct.nextId++;
		counterRepo.save(ct);

		return nextID;
	}

}