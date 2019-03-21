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

    private String[] asRecord(){
        String[] asRecord = new String[6];
        asRecord[0] = caseId;
        asRecord[1] = rmRequestReceived;
        asRecord[2] = canonicalActionCreateSent;
        asRecord[3] = canonicalCreateJobReceived;
        asRecord[4] = cometCreateJobRequest;
        asRecord[5] = cometCreateJobAcknowledged;
        return asRecord;
    }

    @Override
    public String toString(){
        String[] asRecord = asRecord();
        String record = "";
        record = asRecord[0];
        record = record + "," + asRecord[1];
        record = record + "," + asRecord[2];
        record = record + "," + asRecord[3];
        record = record + "," + asRecord[4];
        record = record + "," + asRecord[5];
        return record;
    }
}
