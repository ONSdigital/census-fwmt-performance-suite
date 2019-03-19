package uk.gov.ons.census.fwmt.performancesuite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.performancesuite.utils.GatewayPerformanceMonitor;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws IOException, TimeoutException {
		SpringApplication.run(Application.class, args);


		GatewayPerformanceMonitor gatewayPerformanceMonitor = new GatewayPerformanceMonitor();

		gatewayPerformanceMonitor.enablePerformanceMonitor("localhost");


	}

}
