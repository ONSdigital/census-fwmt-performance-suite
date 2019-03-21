package uk.gov.ons.census.fwmt.performancesuite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.ons.census.fwmt.performancesuite.components.GatewayPerformanceMonitor;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);

		GatewayPerformanceMonitor gatewayPerformanceMonitor = new GatewayPerformanceMonitor();

		// Used for local debugging
        //gatewayPerformanceMonitor.enablePerformanceMonitor("localhost", 50000);

       if (args.length == 0) {
            throw new Exception("Invalid usage. Please run with ONE argument of expected number of jobs.");
        } else if (args.length == 1) {
            long expectedJobs = Long.parseLong(args[0]);
            gatewayPerformanceMonitor.enablePerformanceMonitor("localhost", expectedJobs);
        } else {
            throw new Exception("Invalid usage. Please run with only ONE argument of expected number of jobs.");
        }
	}
}
