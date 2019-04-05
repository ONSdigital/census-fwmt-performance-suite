package uk.gov.ons.census.fwmt.performancesuite.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.performancesuite.dto.CSVRecordDTO;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.NANOS;
import static uk.gov.ons.census.fwmt.events.config.GatewayEventQueueConfig.GATEWAY_EVENTS_EXCHANGE;
import static uk.gov.ons.census.fwmt.events.config.GatewayEventQueueConfig.GATEWAY_EVENTS_ROUTING_KEY;

@Slf4j
@Component
public class GatewayPerformanceMonitor {

  private static final Object lock = new Object();
  private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static String fileName;
  private final AtomicLong counter = new AtomicLong();
  private final AtomicBoolean isJobComplete = new AtomicBoolean(false);
  private final AtomicLong expectedMessageCount = new AtomicLong();
  @Autowired
  private ReportCreation reportCreation = new ReportCreation();
  private Map<String, CSVRecordDTO> csvRecordDTOMap = new HashMap<>();
  private PrintWriter writer;

  public void enablePerformanceMonitor(String rabbitLocation, long receivedMessageCounted)
      throws IOException, TimeoutException, InterruptedException {
    expectedMessageCount.set(receivedMessageCounted);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitLocation);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(GATEWAY_EVENTS_EXCHANGE, "fanout", true);
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, GATEWAY_EVENTS_EXCHANGE, GATEWAY_EVENTS_ROUTING_KEY);

    createFile();

    log.info("Listening for " + expectedMessageCount + " events...");

    Thread.sleep(5000);
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {

        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
            DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"));
        module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
        OBJECT_MAPPER = Jackson2ObjectMapperBuilder.json()
            .modules(module)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

        String message = new String(body, StandardCharsets.UTF_8);
        GatewayEventDTO gatewayEventDTO = OBJECT_MAPPER.readValue(message.getBytes(), GatewayEventDTO.class);
        log.info(String.valueOf(gatewayEventDTO));

        addEvent(gatewayEventDTO);
      }
    };
    channel.basicConsume(queueName, true, consumer);

    while (!isJobComplete.get()) {
      Thread.sleep(1000);
    }
    reportCreation.readCSV(Math.toIntExact(receivedMessageCounted), fileName);
  }

  private void addEvent(GatewayEventDTO gatewayEventDTO) {
    if (!csvRecordDTOMap.containsKey(gatewayEventDTO.getCaseId())) {
      CSVRecordDTO dto = new CSVRecordDTO();
      dto.setCaseId(gatewayEventDTO.getCaseId());
      csvRecordDTOMap.put(gatewayEventDTO.getCaseId(), dto);
    }
    final CSVRecordDTO csvRecordDTO = csvRecordDTOMap.get(gatewayEventDTO.getCaseId());

    switch (gatewayEventDTO.getEventType()) {
    case "RM - Request Received":
      csvRecordDTO.setRmRequestReceived(String.valueOf(gatewayEventDTO.getLocalTime()));
      break;
    case "Canonical - Action Create Sent":
      csvRecordDTO.setCanonicalActionCreateSent(String.valueOf(gatewayEventDTO.getLocalTime()));
      break;
    case "Canonical - Create Job Received":
      csvRecordDTO.setCanonicalCreateJobReceived(String.valueOf(gatewayEventDTO.getLocalTime()));
      break;
    case "Comet - Create Job Request":
      csvRecordDTO.setCometCreateJobRequest(String.valueOf(gatewayEventDTO.getLocalTime()));
      break;
    case "Comet - Create Job Acknowledged":
      csvRecordDTO.setCometCreateJobAcknowledged(String.valueOf(gatewayEventDTO.getLocalTime()));
      LocalTime rmRequestReceived = LocalTime.parse(csvRecordDTO.getRmRequestReceived());
      LocalTime actionCreateSend = LocalTime.parse(csvRecordDTO.getCanonicalActionCreateSent());
      LocalTime canonicalRecieved = LocalTime.parse(csvRecordDTO.getCanonicalCreateJobReceived());
      LocalTime cometCreateJobAcknowledge = LocalTime.parse((csvRecordDTO.getCometCreateJobAcknowledged()));
      LocalTime cometCreateJobSend = LocalTime.parse(csvRecordDTO.getCometCreateJobRequest());

      String rmToCometSend = String.valueOf(MILLIS.between(rmRequestReceived, cometCreateJobSend));
      String endToEndTimeTaken = String.valueOf(MILLIS.between(rmRequestReceived, cometCreateJobAcknowledge));
      String adapterProcessTime = String.valueOf(NANOS.between(rmRequestReceived, actionCreateSend));
      String cometProcessTime = String.valueOf(MILLIS.between(cometCreateJobSend, cometCreateJobAcknowledge));

      csvRecordDTO.setRmToCometSend(rmToCometSend);
      csvRecordDTO.setEndToEndTimeTaken(endToEndTimeTaken);
      csvRecordDTO.setAdapterProcessTime(adapterProcessTime);
      csvRecordDTO.setCometProcessTime(cometProcessTime);

      printRecord(csvRecordDTO);
      csvRecordDTOMap.remove(gatewayEventDTO.getCaseId());
      long count = counter.incrementAndGet();

      if (count == expectedMessageCount.get()) {
        closeFile();
        log.info("Secondary testing complete. Processed: " + count + " messages.");
        isJobComplete.set(true);
      }
      break;
    }
  }

  private void createFile() throws IOException {
    fileName = "src/main/resources/csv/Performance_Test_CSV.csv";
    writer = new PrintWriter("src/main/resources/csv/Performance_Test_CSV.csv");
    String headers = "CaseId, RM - Request Received, Canonical - Action Create Sent," +
        "Canonical - Create Job Received, Comet - Create Job Request, Comet - Create Job Acknowledged, "
        + "RM To Comet Send Time Taken, End To End Time Taken, Adapter Process Time (Nano Secs), Comet Process Time";
    writer.println(headers);
  }

  private void closeFile() {
    synchronized (lock) {
      writer.flush();
      writer.close();
    }
  }

  private void printRecord(CSVRecordDTO csvRecordDTO) {
    synchronized (lock) {
      writer.println(csvRecordDTO.toString());
    }
  }
}
