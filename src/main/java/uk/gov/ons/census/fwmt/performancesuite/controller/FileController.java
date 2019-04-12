package uk.gov.ons.census.fwmt.performancesuite.controller;

import org.springframework.beans.factory.annotation.Value;
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
import java.nio.file.Paths;

@Controller
public class FileController {

  @Value("${storage.csvLocation}")
  private String csvFileName;
  @Value("${storage.reportLocation}")
  private String reportFileName;

  @GetMapping("/downloadReport")
  public ResponseEntity<byte[]> downloadReportData() throws IOException {
    File file = new File(reportFileName);
    final byte[] fileBytes = Files.readAllBytes(file.toPath());
    String fileName = reportFileName;
    HttpHeaders respHeaders = new HttpHeaders();
    respHeaders.setContentType(MediaType.TEXT_PLAIN);
    respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    return new ResponseEntity<>(fileBytes, respHeaders, HttpStatus.OK);
  }

  @GetMapping("/downloadCsv")
  public ResponseEntity downloadCSVData() throws IOException {
    File file = new File(csvFileName);
    final byte[] fileBytes = Files.readAllBytes(file.toPath());
    final ByteArrayResource resource = new ByteArrayResource(fileBytes);
    return ResponseEntity.ok().contentLength(fileBytes.length).contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }

  @GetMapping("/deleteCSV")
  public ResponseEntity  deleteCSV() throws IOException {
    if (true){
      Files.deleteIfExists(Paths.get(csvFileName));
      return ResponseEntity.ok("CSV deleted.");
    } else {
      return new ResponseEntity<>("File not found.", HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/deleteReport")
  public ResponseEntity deleteReport() throws IOException {
    if (true){
      Files.deleteIfExists(Paths.get(reportFileName));
      return ResponseEntity.ok("Report deleted");
    } else {
      return new ResponseEntity<>("File not found.", HttpStatus.NOT_FOUND);
    }
  }
}
