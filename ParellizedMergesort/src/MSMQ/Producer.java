package MSMQ;

import java.util.Arrays;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import Main.EventProfiler;
import Main.MergeSort;
import Main.Utils;

public class Producer {
	
	// either connect to the remote ActiveMQ running on the PI, or on the localhost
	private static String url = "failover:(tcp://169.254.1.1:61616,tcp://localhost:61616)";
	protected static final String[] QUEUES = {
			"testQueue1",
			"testQueue2"
	};

	public static void main(String[] args) throws JMSException {

		System.out.println("Producer MSMQ 2");
		
		// Initializing profiler
		EventProfiler logger = new EventProfiler(true);

		// Create sorted array
		int[] original = new int[
//         500_000
//		 1_000_000
		 2_000_000
//		 4_000_000
//		 8_000_000
//		 16_000_000
//		 24_000_000
//		 48_000_000
		// 96_000_000
		];
		Utils.fillArray(original);

		// Shuffle array & converting to String
		int[] array = original.clone();
		Utils.shuffleArray(array);
		
		// Chopping arrays into subarrays
		int[][] arrays = Utils.chopArray(array, 16);
		
		// Convert arrays to strings
		String[] stringIntegerArrays = new String[arrays.length];
		for (int i = 0; i < arrays.length; i++) {
			stringIntegerArrays[i] = Utils.arrayToString(arrays[i]);
		}
		
		// Start logging
		System.out.print("Starting MSMQ... aaand ");
		logger.start();

		// Delegating work to nodes
		Arrays.stream(stringIntegerArrays).forEach((i) -> {
			send(i);
		});
		
		// Waiting for result and convert back to int[] 
		String[] sortedStringIntegerArrays = new String[arrays.length];
		for (int i = 0; i < arrays.length; i++) {
			sortedStringIntegerArrays[i] = receive();
			String[] stringIntegers = sortedStringIntegerArrays[i].split(" ");
			arrays[i] = Utils.stringArrayToIntArray(stringIntegers);
		}
		
		// Paste chopped sorted arrays
		array = Utils.pasteArrays(arrays);
		
		// Forkjoin sort
		MergeSort sorter = new MergeSort(array);
        array = sorter.forkjoin();
        
        // Validate results and print time
        Utils.checkArray(array);	
		logger.time();
	}

	private static void send(String str) {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		Connection connection;
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(QUEUES[0]);
			MessageProducer producer = session.createProducer(destination);
			
			// Create message
			TextMessage message = session.createTextMessage(str);
			System.out.println("\tCreated Message");
			
			// Send message to broker
			producer.send(message);
			System.out.println("\tMessage Sent");
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private static String receive() {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		Connection connection;
		String result = null;
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(QUEUES[1]);
			MessageConsumer consumer = session.createConsumer(destination);
			
			// Receive message
			Message message = consumer.receive();
			System.out.println("\tMessage Received");
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				result = textMessage.getText();
			}
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return result;
	}
}