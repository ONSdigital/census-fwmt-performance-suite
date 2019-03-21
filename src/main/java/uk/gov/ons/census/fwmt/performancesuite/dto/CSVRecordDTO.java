package uk.gov.ons.census.fwmt.performancesuite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CSVRecordDTO {
  private String caseId;
  private String rmRequestReceived;
  private String canonicalActionCreateSent;
  private String canonicalCreateJobReceived;
  private String cometCreateJobRequest;
  private String cometCreateJobAcknowledged;
  private String rmToCometSend;
  private String endToEndTimeTaken;
  private String adapterProcessTime;
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
