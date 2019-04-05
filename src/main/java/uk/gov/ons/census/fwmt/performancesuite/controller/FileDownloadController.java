package uk.gov.ons.census.fwmt.performancesuite.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;

@Controller
public class FileDownloadController {

  @RequestMapping("/downloadCsv")
  public StreamingResponseBody downloadCsv() {
    return outputStream -> {
      File file = new File("src/main/resources/csv/Performance_Test_CSV.csv");
      FileInputStream stream = new FileInputStream(file);
      IOUtils.copy(stream, outputStream);
    };
  }
}
