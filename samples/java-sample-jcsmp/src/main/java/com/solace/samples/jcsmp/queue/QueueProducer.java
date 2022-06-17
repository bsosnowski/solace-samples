package com.solace.samples.jcsmp.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageProducer;

import com.solace.samples.jcsmp.Client;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "queue-produce", description = "Send message to the target Queue")
public class QueueProducer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(QueueProducer.class);

    @Option(names = { "-q", "--queueName" }, required = true, description = "Queue name")
    String queueName;

    @Option(names = { "-m", "--message" }, required = true, description = "Message")
    String message;

    @ParentCommand
    Client client;

    @Override
    public void run() {
        client.createSession();
        JCSMPSession session = client.getConnection().getSession();
        final EndpointProperties endpointProps = new EndpointProperties();
        // set queue permissions to "consume" and access-type to "exclusive"
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        // create queue object locally
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        // provision queue on broker
        try {
            session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
            /** callback handler for sending events */
            final XMLMessageProducer producer = session
                    .getMessageProducer(new JCSMPStreamingPublishCorrelatingEventHandler() {
                        @Override
                        public void responseReceived(String messageID) {
                            logger.info("Producer received response for msg ID: {}", messageID);
                        }

                        @Override
                        public void handleError(String messageID, JCSMPException e, long timestamp) {
                            logger.error("Producer received error for msg: {}@{} - {}", messageID, timestamp, e);
                        }

                        @Override
                        public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void responseReceivedEx(Object key) {
                            // TODO Auto-generated method stub
                        }
                    });
            // session is now opened
            logger.info("Connected. About to send message: \"{}\" to Queue: \"{}\"", message, queue.getName());
            TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            msg.setDeliveryMode(DeliveryMode.PERSISTENT);
            msg.setText(message);
            producer.send(msg, queue);
            logger.info("Message sent");
            producer.close();
        } catch (JCSMPException e) {
            logger.error("JCMP exception occured: {}", e.getMessage());
        } finally {
            logger.info("Exiting.");
            client.closeSession();
        }

    }

}