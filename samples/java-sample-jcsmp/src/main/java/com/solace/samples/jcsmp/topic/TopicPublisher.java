package com.solace.samples.jcsmp.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.samples.jcsmp.Client;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "topic-publish", description = "Publish message to the target Topic")
public class TopicPublisher implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TopicPublisher.class);

    @Option(names = { "-t", "--topicName" }, required = true, description = "Topic name")
    String topicName;

    @Option(names = { "-m", "--message" }, required = true, description = "Message")
    String message;

    @ParentCommand
    Client client;

    @Override
    public void run() {
        client.createSession();
        JCSMPSession session = client.getConnection().getSession();

        final Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);

        try {
            /** callback handler for publishing events */
            XMLMessageProducer producer = session
                    .getMessageProducer(new JCSMPStreamingPublishCorrelatingEventHandler() {
                        @Override
                        public void responseReceived(String messageID) {
                            logger.info("Producer received response for msg ID: {}", messageID);
                        }

                        @Override
                        public void handleError(String messageID, JCSMPException e, long timestamp) {
                            logger.error("Producer received error for msg: {}@{} - {}", messageID, timestamp, e.getMessage());

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
            TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            msg.setText(message);
            logger.info("Connected. About to send message: \"{}\" to Topic: \"{}\"", message, topic.getName());
            producer.send(msg, topic);
            logger.info("Message sent. Exiting.");
        } catch (JCSMPException e) {
            logger.error("JCMP exception occured: {}", e.getMessage());
        } finally {
            client.closeSession();
        }

    }

}