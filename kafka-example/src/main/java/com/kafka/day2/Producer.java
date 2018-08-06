package com.kafka.day2;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class Producer extends Thread {
	private final KafkaProducer<Integer, String> producer;
	private String topic;
	private Boolean isAsync;
	private Boolean interrupt;
	private String name;

	public Producer(String topic, Boolean isAsync, String name) {
		this.topic = topic;
		this.isAsync = isAsync;
		interrupt = false;
		this.name = name;
		Properties properties = new Properties();
		// localhost:2181
		properties.setProperty("bootstrap.servers",
				KafkaProperties.KAFKA_SERVER_URL + ":" + KafkaProperties.KAFKA_SERVER_PORT);
		properties.setProperty("client.id", KafkaProperties.PRODUCER_CLIENT_ID);
		// org.apache.kafka.common.serialization.IntegerSerializer;
		properties.setProperty("key.serializer", IntegerSerializer.class.getName());
		properties.setProperty("value.serializer", StringSerializer.class.getName());
		producer = new KafkaProducer<Integer, String>(properties);
	}

	@Override
	public void run() {
		// produce either sync or async
		int messageNo = 1;
		while (!interrupt) {
			String messageString = "Message from producer " + this.name + " Count : " + messageNo;
			if (isAsync) {
				// send asynchronously
				ProducerRecord<Integer, String> producerRecord = new ProducerRecord<Integer, String>(this.topic,
						messageNo, messageString);
				long startTime = System.currentTimeMillis();
				producer.send(producerRecord, new ProducerMessageCallable(startTime, messageNo, messageString));
			} else {
				try {
					// synchronous
					ProducerRecord<Integer, String> producerRecord = new ProducerRecord<Integer, String>(this.topic,
							messageNo, messageString);

					producer.send(producerRecord).get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			messageNo++;
		}
	}

	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}
}
