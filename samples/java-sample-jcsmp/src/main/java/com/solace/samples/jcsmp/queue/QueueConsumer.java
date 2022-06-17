package com.solace.samples.jcsmp.queue;

import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.samples.jcsmp.Client;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageListener;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "queue-consume", description = "Receive message from the target Queue")
public class QueueConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(QueueConsumer.class);

    @Option(names = { "-q", "--queueName" }, required = true, description = "Queue name")
    String queueName; // example: Q/tutorial

    @ParentCommand
    Client client;

    @Override
    public void run() {
        client.createSession();
        JCSMPSession session = client.getConnection().getSession();

        // set queue permissions to "consume" and access-type to "exclusive"
        final EndpointProperties endpointProps = new EndpointProperties();
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        // create queue object locally
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        // initialize flow properties
        final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
        flowProps.setEndpoint(queue);
        flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        // initialize endpoint properties
        EndpointProperties consumerEndpointProps = new EndpointProperties();
        consumerEndpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        try {
            final CountDownLatch latch = new CountDownLatch(1); // threads synchronization
            logger.info("Binding to the queue: {}. Ignoring if the queue already exists.", queueName);
            // provision queue on broker
            session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
            FlowReceiver flowReceiver = session.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage msg) {
                    logger.info("Message Received: {}", msg.dump());
                    /**
                     * When the ack mode is set to SUPPORTED_MESSAGE_ACK_CLIENT,
                     * guaranteed delivery messages are acknowledged after
                     * processing
                     */
                    msg.ackMessage();
                    // latch.countDown(); // unblock main thread
                }

                @Override
                public void onException(JCSMPException e) {
                    logger.error("Consumer received exception: {}", e.getMessage());
                    latch.countDown(); // unblock main thread
                }
            }, flowProps, consumerEndpointProps);
            // start the consumer
            flowReceiver.start();
            logger.info("Connected. Awaiting message ...");
            latch.await(); // block here until message received, and latch will flip
            // close consumer
            flowReceiver.close();
        } catch (JCSMPException e1) {
            logger.error("JCMP exception occured: {}", e1.getMessage());
        } catch (InterruptedException e2) {
            Thread.currentThread().interrupt();
            logger.error("Thread was awoken while waiting: {}", e2.getMessage());
        } finally {
            logger.info("Exiting.");
            client.closeSession();
        }

    }

}