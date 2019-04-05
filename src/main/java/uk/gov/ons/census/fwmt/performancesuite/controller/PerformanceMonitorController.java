package uk.gov.ons.census.fwmt.performancesuite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.ons.census.fwmt.performancesuite.components.GatewayPerformanceMonitor;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Controller
public class PerformanceMonitorController {

  @Autowired
  private GatewayPerformanceMonitor gatewayPerformanceMonitor;
  @Value("${rabbitmq.rabbitLocation}")
  String rabbitLocation;

  @GetMapping("/enablePerformanceMonitor")
  public String enablePerformanceMonitor(@RequestParam("expectedJobs") int expectedJobs) throws InterruptedException, TimeoutException, IOException {
    enablePerformanceMonitor(rabbitLocation, expectedJobs);

    return "Enabled performance monitor";
  }

  @Async("threadPoolTaskExecutor")
  public void enablePerformanceMonitor(String rabbitLocation, int expectedJobs)
      throws InterruptedException, TimeoutException, IOException {
      gatewayPerformanceMonitor.enablePerformanceMonitor(rabbitLocation, expectedJobs);
  }
}
