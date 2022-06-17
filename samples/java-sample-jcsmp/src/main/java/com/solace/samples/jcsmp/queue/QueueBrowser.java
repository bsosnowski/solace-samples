package com.solace.samples.jcsmp.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.samples.jcsmp.Client;
import com.solacesystems.jcsmp.Browser;
import com.solacesystems.jcsmp.BrowserProperties;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "queue-browse", description = "Browse messages from the target Queue")
public class QueueBrowser implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(QueueBrowser.class);

    @Option(names = { "-q", "--queueName" }, required = true, description = "Queue name")
    String queueName; // example: Q/tutorial

    @ParentCommand
    Client client;

    @Override
    public void run() {
        client.createSession();
        JCSMPSession session = client.getConnection().getSession();
        // create queue object locally
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        // initialize browser properties
        BrowserProperties browserProps = new BrowserProperties();
        browserProps.setEndpoint(queue);
        browserProps.setTransportWindowSize(1);
        browserProps.setWaitTimeout(1000);
        try {
            Browser queueBrowser = session.createBrowser(browserProps);
            do {
                BytesXMLMessage msg = queueBrowser.getNext();
                if (msg != null) {
                   logger.info("Message Browsed: {}", msg.dump());
                }
            } while (queueBrowser.hasMore());
           logger.info("No more messages to browse.");
        } catch (JCSMPException e) {
           logger.error("JCMP exception occured.", e.getMessage());
        } finally {
            logger.info("Exiting.");
            client.closeSession();
        }

    }

}