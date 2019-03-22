package uk.gov.ons.census.fwmt.performancesuite.components;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.performancesuite.dto.CSVRecordDTO;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class ReportCreation {
  private PrintWriter writer;

  public void readCSV(int numberOfJobs) throws IOException {
    long endToEndTimeTaken = 0;
    long endToEndMinTimeTaken = 0;
    long endToEndMaxTimeTaken = 0;
    long adapterProcessTimeAvg;
    long adapterTotalProcessTime = 0;
    long cometProcessTime = 0;
    long cometProcessTimeAvg;

    createReportFile();

    try (
        Reader reader = Files.newBufferedReader(Paths.get(GatewayPerformanceMonitor.fileName));
    ) {
      CsvToBean<CSVRecordDTO> csvToBean = new CsvToBeanBuilder(reader)
          .withType(CSVRecordDTO.class)
          .withIgnoreLeadingWhiteSpace(true)
          .build();

      for (CSVRecordDTO csvRecordDTO : csvToBean) {
        adapterTotalProcessTime += Long.valueOf(csvRecordDTO.getRmToCometSend());
        cometProcessTime += Long.valueOf(csvRecordDTO.getCometProcessTime());
        endToEndTimeTaken += Long.valueOf(csvRecordDTO.getEndToEndTimeTaken());
        endToEndMaxTimeTaken = getMax(Integer.parseInt(csvRecordDTO.getEndToEndTimeTaken()));
        endToEndMinTimeTaken = getMin(Integer.parseInt(csvRecordDTO.getEndToEndTimeTaken()));
      }
      cometProcessTimeAvg = cometProcessTime / numberOfJobs;
      adapterProcessTimeAvg = adapterTotalProcessTime / numberOfJobs;

      writer.println("Performance Suite Report \n");
      writer.println("Adapter process avg: " + adapterProcessTimeAvg);
      writer.println("Comet process avg: " + cometProcessTimeAvg);
      writer.println("End To End Total Time Taken: " + endToEndTimeTaken);
      writer.println("Minimum End To End Time Taken: " + endToEndMinTimeTaken);
      writer.println("Maximum End To End Tie Taken: " + endToEndMaxTimeTaken);

      writer.flush();
      writer.close();
    }
  }

  private int large = 0;
  private int getMax(int input) {
    if (large == 0) {
      large = input;
    } else if (input > large) {
      large = input;
    }
    return large;
  }

  private int small = 0;
  private int getMin(int input) {
    if (small == 0) {
      small = input;
    } else if (input < small) {
      small = input;
    }
    return small;
  }

  private void createReportFile() throws IOException {
    String reportFileName;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Date currentDateTime = new Date();

    reportFileName = "src/main/resources/results/" +  "Performance_Test_Analysis_Report_" + dateFormat.format(currentDateTime) + ".txt";
    writer = new PrintWriter(reportFileName, StandardCharsets.UTF_8);
  }
}
