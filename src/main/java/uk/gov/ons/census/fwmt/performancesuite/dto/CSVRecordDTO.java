package uk.gov.ons.census.fwmt.performancesuite.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CSVRecordDTO {
  @CsvBindByName(column = "CaseId")
  private String caseId;
  @CsvBindByName(column = "RM - Request Received")
  private String rmRequestReceived;
  @CsvBindByName(column = "Canonical - Action Create Sent")
  private String canonicalActionCreateSent;
  @CsvBindByName(column = "Canonical - Create Job Received")
  private String canonicalCreateJobReceived;
  @CsvBindByName(column = "Comet - Create Job Request")
  private String cometCreateJobRequest;
  @CsvBindByName(column = "Comet - Create Job Acknowledged")
  private String cometCreateJobAcknowledged;
  @CsvBindByName(column = "RM To Comet Send Time Taken")
  private String rmToCometSend;
  @CsvBindByName(column = "End To End Time Taken")
  private String endToEndTimeTaken;
  @CsvBindByName(column = "Adapter Process Time (Nano Secs)")
  private String adapterProcessTime;
  @CsvBindByName(column = "Comet Process Time")
  private String cometProcessTime;

  private String[] asRecord() {
    String[] asRecord = new String[10];
    asRecord[0] = caseId;
    asRecord[1] = rmRequestReceived;
    asRecord[2] = canonicalActionCreateSent;
    asRecord[3] = canonicalCreateJobReceived;
    asRecord[4] = cometCreateJobRequest;
    asRecord[5] = cometCreateJobAcknowledged;
    asRecord[6] = rmToCometSend;
    asRecord[7] = endToEndTimeTaken;
    asRecord[8] = adapterProcessTime;
    asRecord[9] = cometProcessTime;
    return asRecord;
  }

  @Override
  public String toString() {
    String[] asRecord = asRecord();
    String record;
    record = asRecord[0];
    record = record + "," + asRecord[1];
    record = record + "," + asRecord[2];
    record = record + "," + asRecord[3];
    record = record + "," + asRecord[4];
    record = record + "," + asRecord[5];
    record = record + "," + asRecord[6];
    record = record + "," + asRecord[7];
    record = record + "," + asRecord[8];
    record = record + "," + asRecord[9];
    return record;
  }
}
