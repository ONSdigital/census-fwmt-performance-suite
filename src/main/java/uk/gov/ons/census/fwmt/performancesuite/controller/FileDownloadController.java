package uk.gov.ons.census.fwmt.performancesuite.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Controller
public class FileDownloadController {

  @GetMapping("/downloadReport")
  public ResponseEntity<byte[]> downloadReportData() throws IOException {
    File file = new File("src/main/resources/report/Performance_Test_Report.txt");
    final byte[] fileBytes = Files.readAllBytes(file.toPath());
    String fileName = "src/main/resources/report/Performance_Test_Report.txt";
    HttpHeaders respHeaders = new HttpHeaders();
    respHeaders.setContentType(MediaType.TEXT_PLAIN);
    respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    return new ResponseEntity<>(fileBytes,respHeaders, HttpStatus.OK);
  }

  @GetMapping("/downloadCsv")
  public ResponseEntity downloadCSVData() throws IOException {
    File file = new File("src/main/resources/csv/Performance_Test_CSV.csv");
      final byte[] fileBytes = Files.readAllBytes(file.toPath());
      final ByteArrayResource resource = new ByteArrayResource(fileBytes);
      return ResponseEntity.ok().contentLength(fileBytes.length).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
  }
}
