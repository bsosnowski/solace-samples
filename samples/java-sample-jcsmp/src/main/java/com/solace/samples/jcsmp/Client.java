package com.solace.samples.jcsmp;

import static picocli.CommandLine.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.samples.jcsmp.queue.QueueBrowser;
import com.solace.samples.jcsmp.queue.QueueConsumer;
import com.solace.samples.jcsmp.queue.QueueProducer;
import com.solace.samples.jcsmp.topic.TopicPublisher;
import com.solace.samples.jcsmp.topic.TopicSubscriber;

import picocli.CommandLine;
import picocli.CommandLine.Option;

@Command(name= "java-sample-jcsmp",
         subcommands = {QueueConsumer.class, QueueProducer.class, TopicPublisher.class, TopicSubscriber.class, QueueBrowser.class},
         headerHeading = "@|bold,underline,green Usage|@:%n%n",
         synopsisHeading = "%n",
         descriptionHeading = "%n@|bold,underline,green Description|@:%n%n",
         parameterListHeading = "%n@|bold,underline,green Parameters|@:%n",
         commandListHeading = "%n@|bold,underline,green Commands|@:%n",
         optionListHeading = "%n@|bold,underline,green Options|@:%n",
         header = "Initialize Solace connection and select command to execute",
         description = "Simple @|bold,underline Solace|@ client application. It supports Queues (Consumer and Producer) and Topics (Publisher and Subscriber).")
public final class Client implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Client.class.getName());

    private Connection connection;

    @Option(names = {"-h","--host"}, required = true, description = "Solace broker hostname")
    String host;

    @Option(names = {"-p","--port"}, required = true, description = "Solace broker port")
    String port;

    @Option(names = {"-v","--vpn"}, required = true, description = "VPN name")
    String vpn;

    @Option(names = {"-usr","--username"}, required = true, description = "Client username")
    String username;

    @Option(names = {"-pwd","--password"}, required = true, description = "Client password")
    String password;

    @Option(names = { "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;
    
    public static void main(String[] args) {
        new CommandLine(new Client()).execute(args);
    }

    @Override
    public void run() {
        logger.info("Checking Solace connectivity");
        createSession();
        closeSession();
     }

     public void createSession(){
        connection = new Connection(host, port, vpn, username, password);
        connection.connect();
     }

     public void closeSession(){
         connection.getSession().closeSession();
     }

     public Connection getConnection() {
         return connection;
     }

}