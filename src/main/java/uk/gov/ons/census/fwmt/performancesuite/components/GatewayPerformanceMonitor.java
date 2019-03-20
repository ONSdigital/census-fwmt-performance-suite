package uk.gov.ons.census.fwmt.performancesuite.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.performancesuite.dto.CSVRecordDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static uk.gov.ons.census.fwmt.events.config.GatewayEventQueueConfig.GATEWAY_EVENTS_EXCHANGE;
import static uk.gov.ons.census.fwmt.events.config.GatewayEventQueueConfig.GATEWAY_EVENTS_ROUTING_KEY;

@Slf4j
@Component
public class GatewayPerformanceMonitor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Map<String, CSVRecordDTO> csvRecordDTOMap = new HashMap<>();
    private String[] headers = {"CaseId", "RM - Request Received", "Canonical - Action Create Sent",
            "Canonical - Create Job Received", "Comet - Create Job Request", "Comet - Create Job Acknowledged"};

    public void enablePerformanceMonitor(String rabbitLocation, int expectedMessageCount) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitLocation);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(GATEWAY_EVENTS_EXCHANGE, "fanout", true);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, GATEWAY_EVENTS_EXCHANGE, GATEWAY_EVENTS_ROUTING_KEY);

        CSVPrinter csvPrinter = getCsvPrinter();

        log.info("Listening for " + expectedMessageCount + " events..");
        for (int count = 0; count <= expectedMessageCount; count++) {
            int finalCount = count;
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    GatewayEventDTO gatewayEventDTO = OBJECT_MAPPER.readValue(message.getBytes(), GatewayEventDTO.class);

                    addEvent(gatewayEventDTO, csvPrinter);
                    /*
                    if(finalCount == expectedMessageCount) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            log.error("Exited prematurely");
                        }
                        System.exit(0);
                    }
                    */
                }

            };
            channel.basicConsume(queueName, true, consumer);
        }
    }

    private CSVPrinter getCsvPrinter() throws IOException {
        File file = createFile();

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator());
        FileWriter fileWriter = new FileWriter(file);
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFileFormat);
        csvPrinter.printRecord((Object[]) headers);
        csvPrinter.flush();

        return csvPrinter;
    }

    private File createFile() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date currentDateTime = new Date();

        String newFileName = "Performance_Test_" + dateFormat.format(currentDateTime) + ".csv";
        return new File("src/main/resources/results/" + newFileName);
    }

    private void addEvent(GatewayEventDTO gatewayEventDTO, CSVPrinter csvPrinter) throws IOException {
        if (!csvRecordDTOMap.containsKey(gatewayEventDTO.getCaseId())) {
            CSVRecordDTO dto = new CSVRecordDTO();
            dto.setCaseId(gatewayEventDTO.getCaseId());
            csvRecordDTOMap.put(gatewayEventDTO.getCaseId(), dto);
        }
        final CSVRecordDTO csvRecordDTO = csvRecordDTOMap.get(gatewayEventDTO.getCaseId());

        switch (gatewayEventDTO.getEventType()) {
            case "RM - Request Received":
                csvRecordDTO.setRmRequestReceived(gatewayEventDTO.getLocalTime());
                break;
            case "Canonical - Action Create Sent":
                csvRecordDTO.setCanonicalActionCreateSent(gatewayEventDTO.getLocalTime());
                break;
            case "Canonical - Create Job Received":
                csvRecordDTO.setCanonicalCreateJobReceived(gatewayEventDTO.getLocalTime());
                break;
            case "Comet - Create Job Request":
                csvRecordDTO.setCometCreateJobRequest(gatewayEventDTO.getLocalTime());
                break;
            case "Comet - Create Job Acknowledged":
                csvRecordDTO.setCometCreateJobAcknowledged(gatewayEventDTO.getLocalTime());
                csvPrinter.printRecord((Object[]) csvRecordDTO.asRecord());
                csvPrinter.flush();
                break;
        }
    }
}
