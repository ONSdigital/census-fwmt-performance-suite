package uk.gov.ons.census.fwmt.performancesuite.components;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.performancesuite.dto.CSVRecordDTO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MILLIS;

@Slf4j
@Component
public class ReportCreation {

  @Value("${storage.reportLocation}")
  private String reportFileName;
  private int large = 0;
  private int small = 0;

  public ReportCreation() {
    this.reportFileName = reportFileName;
  }

  public void readCSV(int numberOfJobs, CSVRecordDTO initalCsvRecordDTO, CSVRecordDTO finalCsvRecordDTO,
      String fileName) throws IOException {
    long endToEndTimeTaken = 0;
    long endToEndMinTimeTaken = 0;
    long endToEndMaxTimeTaken = 0;
    long adapterProcessTimeAvg;
    long adapterTotalProcessTime = 0;
    long cometProcessTime = 0;
    long cometProcessTimeAvg;

    try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {

      CsvToBean<CSVRecordDTO> csvToBean = new CsvToBeanBuilder(bufferedReader)
          .withType(CSVRecordDTO.class)
          .withIgnoreLeadingWhiteSpace(true)
          .build();

      for (CSVRecordDTO csvRecordDTO : csvToBean) {
        adapterTotalProcessTime += Long.valueOf(csvRecordDTO.getRmToCometSend());
        cometProcessTime += Long.valueOf(csvRecordDTO.getCometProcessTime());
        endToEndTimeTaken = MILLIS.between(LocalTime.parse(initalCsvRecordDTO.getRmRequestReceived()),
            LocalTime.parse(finalCsvRecordDTO.getRmRequestReceived()));
        endToEndMaxTimeTaken = getMax(Integer.parseInt(csvRecordDTO.getEndToEndTimeTaken()));
        endToEndMinTimeTaken = getMin(Integer.parseInt(csvRecordDTO.getEndToEndTimeTaken()));
      }

      cometProcessTimeAvg = cometProcessTime / numberOfJobs;
      adapterProcessTimeAvg = adapterTotalProcessTime / numberOfJobs;

      writeReport(endToEndTimeTaken, endToEndMinTimeTaken, endToEndMaxTimeTaken, adapterProcessTimeAvg,
          cometProcessTimeAvg, initalCsvRecordDTO, finalCsvRecordDTO);

    }
  }

  private void writeReport(long endToEndTimeTaken, long endToEndMinTimeTaken,
      long endToEndMaxTimeTaken, long adapterProcessTimeAvg, long cometProcessTimeAvg, CSVRecordDTO initalCsvRecordDTO,
      CSVRecordDTO finalCsvRecordDTO) {
    String headers = "CaseId, RM - Request Received, Canonical - Action Create Sent," +
        "Canonical - Create Job Received, Comet - Create Job Request, Comet - Create Job Acknowledged, "
        + "RM To Comet Send Time Taken, End To End Time Taken, Adapter Process Time (Nano Secs), Comet Process Time";

    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(reportFileName))) {
      bufferedWriter.write("Performance Suite Report \n");
      bufferedWriter.write("Adapter process avg (ms): " + adapterProcessTimeAvg + "\n");
      bufferedWriter.write("Comet process avg (ms): " + cometProcessTimeAvg + "\n");
      bufferedWriter.write("End To End Total Time Taken (ms): " + endToEndTimeTaken + "\n");
      bufferedWriter.write("Minimum End To End Time Taken (ms): " + endToEndMinTimeTaken + "\n");
      bufferedWriter.write("Maximum End To End Time Taken (ms): " + endToEndMaxTimeTaken + "\n \n");
      bufferedWriter.write("Start record: \n");
      bufferedWriter.write(headers + "\n");
      bufferedWriter.write(initalCsvRecordDTO.toString() + "\n");
      bufferedWriter.write("End record \n");
      bufferedWriter.write(headers + "\n");
      bufferedWriter.write(finalCsvRecordDTO.toString() + "\n");

    } catch (IOException e) {
      log.error("Failed to write to file {}", e);
    }
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
