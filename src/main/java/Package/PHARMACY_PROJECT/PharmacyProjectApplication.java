package Package.PHARMACY_PROJECT;

import Package.PHARMACY_PROJECT.Services.SerialReaderService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class PharmacyProjectApplication {

	public static void main(String[] args) {
		// Obteniendo el contexto de Spring
		System.setProperty("java.awt.headless", "true");
		ApplicationContext context = SpringApplication.run(PharmacyProjectApplication.class, args);

		// Invocación manual del servicio e PUERTO COM  (solo si @PostConstruct no funciona)
		/*SerialReaderService serialReaderService = context.getBean(SerialReaderService.class);
		serialReaderService.startSerialCommunication();¨*/
	}

}
