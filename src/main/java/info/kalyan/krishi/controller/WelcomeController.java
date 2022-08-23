package info.kalyan.krishi.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import info.kalyan.krishi.pojos.Counter;
import info.kalyan.krishi.pojos.Device;
import info.kalyan.krishi.pojos.Vehicle;
import info.kalyan.krishi.repository.CounterRepository;
import info.kalyan.krishi.repository.VehicleRepository;

@Controller
public class WelcomeController {

	public WelcomeController(CounterRepository counterRepo, VehicleRepository vehicleRepo) {
		super();
		this.counterRepo = counterRepo;
		this.vehicleRepo = vehicleRepo;
	}

	// inject via application.properties
	@Value("${app.welcome.message}")
	private String message = "";

	@Value("${app.welcome.title}")
	private String title = "";

	private final String alert = "alert alert-danger";

	@Autowired
	public final CounterRepository counterRepo;
	@Autowired
	public final VehicleRepository vehicleRepo;

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

	@GetMapping(path = "/delete")
	public String delete(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		if (request.getRemoteUser().equals("admin")) {
			model.put("admin", true);
		} else {
			model.put("admin", false);
		}

		List<String> agents = vehicleRepo.findDistinctAgent();
		model.put("agents", agents);

		return "delete";
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
				Vehicle vh = new Vehicle(strings[0].trim().toUpperCase(), strings[1].trim(), strings[2].trim(),
						strings[3].trim(),
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
	public Vehicle vehicleDetails(@RequestParam("id") String id) {
		Vehicle vh = vehicleRepo.findById(id).get();
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
	public String changeStatus(@RequestParam("id") String id,
			@RequestParam("status") Vehicle.Status status) {
		Vehicle vh = vehicleRepo.findById(id).get();
		vh.status = status;
		vehicleRepo.save(vh);
		return "Status Changed";
	}

	@GetMapping(path = "/deleteAll")
	@ResponseBody
	public String deleteAll(@RequestParam("agent") String agent) {
		vehicleRepo.deleteAllByAgent(agent);
		return "Successfully deleted all entries";
	}

	@PostMapping(path = "deviceRegistration")
	@ResponseBody
	public String registerDevice(@RequestBody Device device) {

		return "Success";
	}

	// test 5xx errors
	@GetMapping("/5xx")
	public String serviceUnavailable() {
		throw new RuntimeException("ABC");
	}

	@GetMapping(path = "/contact")
	public String getContactPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		return "contact";
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