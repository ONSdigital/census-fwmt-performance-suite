package uk.gov.ons.census.fwmt.performancesuite.utils;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static uk.gov.ons.census.fwmt.events.config.GatewayEventQueueConfig.GATEWAY_EVENTS_EXCHANGE;
import static uk.gov.ons.census.fwmt.events.config.GatewayEventQueueConfig.GATEWAY_EVENTS_ROUTING_KEY;

@Slf4j
@Component
public class GatewayPerformanceMonitor {

    private ArrayList<String> eventTimeStampArrayList = new ArrayList<>();
    private String tempCaseId = null;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static Map<String, List<LocalTime>> eventTimeStampMap = null;
    private Channel channel = null;
    private Connection connection = null;


    public void enablePerformanceMonitor(String rabbitLocation) throws IOException, TimeoutException {
        System.out.println("Listening for events..");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitLocation);
        connection = factory.newConnection();
        channel = connection.createChannel();

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date currentDateTime = new Date();

        String  newFileName = "Performance_Test_" + dateFormat.format(currentDateTime) + ".csv";
        File file = new File("src/main/resources/results/" + newFileName);


        FileWriter fileWriter = new FileWriter(file);
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFileFormat);
        csvPrinter.printRecord("CaseId", "TimeStamp1", "TimeStamp2", "TimeStamp3", "TimeStamp4", "TimeStamp5");

        channel.exchangeDeclare(GATEWAY_EVENTS_EXCHANGE, "fanout", true);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, GATEWAY_EVENTS_EXCHANGE, GATEWAY_EVENTS_ROUTING_KEY);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                GatewayEventDTO gatewayEventDTO = OBJECT_MAPPER.readValue(message.getBytes(), GatewayEventDTO.class);

                addEvent(gatewayEventDTO, csvPrinter);

            }
        };
        channel.basicConsume(queueName, true, consumer);

//        fileWriter.flush();
//        fileWriter.close();

//        csvPrinter.flush();
//        csvPrinter.close();

    }

    public void addEvent(GatewayEventDTO gatewayEventDTO, CSVPrinter csvPrinter) throws IOException {
        if (tempCaseId == null) {
            tempCaseId = gatewayEventDTO.getCaseId();
            eventTimeStampArrayList.add(tempCaseId);
        } else if (!tempCaseId.equals(gatewayEventDTO.getCaseId())) {
            tempCaseId = gatewayEventDTO.getCaseId();
            eventTimeStampArrayList.add(tempCaseId);
        }

        if (tempCaseId.equals(gatewayEventDTO.getCaseId()) && eventTimeStampArrayList.size() != 5) {
            eventTimeStampArrayList.add(String.valueOf(gatewayEventDTO.getLocalTime()));
        } else if (tempCaseId.equals(gatewayEventDTO.getCaseId()) && eventTimeStampArrayList.size() == 5) {
            // may print in stupid format?
            String[] eventArray = eventTimeStampArrayList.toArray(new String[5]);
            csvPrinter.printRecord((Object[]) eventArray);
            eventTimeStampMap.clear();
        }
    }


    public CSVPrinter createCSVPrinter() throws IOException {

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date currentDateTime = new Date();

        String  newFileName = "Performance_Test_" + dateFormat.format(currentDateTime) + ".csv";

        FileWriter fileWriter = new FileWriter(newFileName);
         CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFileFormat);
        csvPrinter.printRecord("CaseId", "TimeStamp1", "TimeStamp2", "TimeStamp3", "TimeStamp4", "TimeStamp5");

        return  csvPrinter;
    }
    private void createCsvRow(ArrayList eventTimeStampList) {

    }


}
