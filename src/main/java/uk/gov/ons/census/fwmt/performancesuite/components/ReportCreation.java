package uk.gov.ons.census.fwmt.performancesuite.components;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.performancesuite.dto.CSVRecordDTO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class ReportCreation {

  private int large = 0;
  private int small = 0;

  public void readCSV(int numberOfJobs, String fileName) throws IOException {
    long endToEndTimeTaken = 0;
    long endToEndMinTimeTaken = 0;
    long endToEndMaxTimeTaken = 0;
    long adapterProcessTimeAvg;
    long adapterTotalProcessTime = 0;
    long cometProcessTime = 0;
    long cometProcessTimeAvg;
    float endToEndDouble = 0.00f;

    String reportFile = createReportFile();

    try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {

      CsvToBean<CSVRecordDTO> csvToBean = new CsvToBeanBuilder(bufferedReader)
          .withType(CSVRecordDTO.class)
          .withIgnoreLeadingWhiteSpace(true)
          .build();

      for (CSVRecordDTO csvRecordDTO : csvToBean) {
        adapterTotalProcessTime += Long.valueOf(csvRecordDTO.getRmToCometSend());
        cometProcessTime += Long.valueOf(csvRecordDTO.getCometProcessTime());
        endToEndTimeTaken += Long.valueOf(csvRecordDTO.getEndToEndTimeTaken());
        endToEndDouble = endToEndTimeTaken / 1000000000f;
        endToEndMaxTimeTaken = getMax(Integer.parseInt(csvRecordDTO.getEndToEndTimeTaken()));
        endToEndMinTimeTaken = getMin(Integer.parseInt(csvRecordDTO.getEndToEndTimeTaken()));
      }

      cometProcessTimeAvg = cometProcessTime / numberOfJobs;
      adapterProcessTimeAvg = adapterTotalProcessTime / numberOfJobs;

      writeReport(reportFile, endToEndDouble, endToEndMinTimeTaken, endToEndMaxTimeTaken, adapterProcessTimeAvg,
          cometProcessTimeAvg);

    }
  }

  private void writeReport(String reportFileName, float endToEndDouble, long endToEndMinTimeTaken,
      long endToEndMaxTimeTaken, long adapterProcessTimeAvg, long cometProcessTimeAvg) {
    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(reportFileName))) {
      bufferedWriter.write("Performance Suite Report \n");
      bufferedWriter.write("Adapter process avg (ms): " + adapterProcessTimeAvg + "\n");
      bufferedWriter.write("Comet process avg (ms): " + cometProcessTimeAvg + "\n");
      bufferedWriter.write("End To End Total Time Taken (secs): " + endToEndDouble + "\n");
      bufferedWriter.write("Minimum End To End Time Taken (ms): " + endToEndMinTimeTaken  + "\n");
      bufferedWriter.write("Maximum End To End Tie Taken: (ms)" + endToEndMaxTimeTaken);
    } catch (IOException e) {
      log.error("Failed to write to file {}", e);
    }
  }

  private String createReportFile() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Date currentDateTime = new Date();

    return "src/main/resources/report/" + "Performance_Test_Analysis_Report_"
        + dateFormat.format(currentDateTime) + ".txt";
  }

  private int getMax(int input) {
    if (large == 0) {
      large = input;
    } else if (input > large) {
      large = input;
    }
    return large;
  }

  private int getMin(int input) {
    if (small == 0) {
      small = input;
    } else if (input < small) {
      small = input;
    }
    return small;
  }
}
