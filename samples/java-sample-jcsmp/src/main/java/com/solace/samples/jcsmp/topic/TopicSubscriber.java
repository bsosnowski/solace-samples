package com.solace.samples.jcsmp.topic;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.samples.jcsmp.Client;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "topic-subscribe", description = "Subscribe to the target Topic")
public class TopicSubscriber implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TopicSubscriber.class);

    @Option(names = { "-q", "--topicName" }, required = true, description = "Topic name")
    String topicName;

    @ParentCommand
    Client client;

    @Override
    public void run() {
        client.createSession();
        JCSMPSession session = client.getConnection().getSession();
        try {
            final CountDownLatch latch = new CountDownLatch(1); // used for synchronizing b/w threads
            // create topic locally
            final Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
            /** callback handler for publishing events */
            final XMLMessageConsumer consumer = session.getMessageConsumer(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage msg) {
                    logger.info("Message Received: {}", msg.dump());
                    // latch.countDown(); // unblock main thread
                }

                @Override
                public void onException(JCSMPException e) {
                    logger.error("Consumer received exception: {}", e.getMessage());
                    latch.countDown(); // unblock main thread
                }
            });
            // subscrube to the topic
            session.addSubscription(topic);
            // start consuming
            consumer.start();
            logger.info("Connected. Awaiting message ...");
            latch.await(); // block here until message received, and latch will flip
            // close consumer
            consumer.close();
        } catch (JCSMPException e1) {
            logger.error("JCMP exception occured: {}", e1);
        } catch (InterruptedException e2) {
            Thread.currentThread().interrupt();
            logger.info("Thread was awoken while waiting {}", e2);
        } finally {
            logger.info("Exiting.");
            client.closeSession();
        }

    }

}