package MSMQ;

import Main.MergeSort;
import Main.Utils;

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

public class Consumer {
	
	// either connect to the remote ActiveMQ running on the PI, or on the localhost
	private static String url = "failover:(tcp://169.254.1.1:61616,tcp://localhost:61616)";

	public static void main(String args[]) throws Exception {
		
		System.out.println("Consumer MSMQ 2");
			
		while (true) {
			// Consume stringArray
			String unsortedStringIntegers = receive();		
			String[] stringArray = unsortedStringIntegers.split(" ");
			int[] array = Utils.stringArrayToIntArray(stringArray);
			
			// Forkjoin sort
			MergeSort sorter = new MergeSort(array);
			array = sorter.forkjoin();
			
			// Put results in queue
			String sortedStringIntegers = Utils.arrayToString(array);
			send(sortedStringIntegers);
		}
	}
	
	private static String receive() {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		Connection connection;
		String str = null;
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination_fromQueue = session.createQueue(Producer.QUEUES[0]);
			MessageConsumer consumer = session.createConsumer(destination_fromQueue);
			
			// Receiving message
			Message message = consumer.receive();
			System.out.println("Message Received");
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				str = textMessage.getText();
			}
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return str;
	}
	
	private static void send(String str) {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		Connection connection;
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(Producer.QUEUES[1]);
			MessageProducer producer = session.createProducer(destination);
			
			// Create message
			TextMessage message = session.createTextMessage(str);
			
			// Send message to broker
			producer.send(message);
			System.out.println("Message Sent");
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}