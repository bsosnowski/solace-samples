package com.solace.samples.jcsmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

public class Connection {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private String host;
    private String port;
    private String vpn;
    private String username;
    private String password;

    private JCSMPSession session;

    public Connection(String host, String port, String vpn, String username, String password) {
        this.host = host;
        this.port = port;
        this.vpn = vpn;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, host + ":" + port); // host:port
        properties.setProperty(JCSMPProperties.USERNAME, username);      // client-username
        properties.setProperty(JCSMPProperties.VPN_NAME, vpn);           // message-vpn
        properties.setProperty(JCSMPProperties.PASSWORD, password);      // client-password
        try {
            session = JCSMPFactory.onlyInstance().createSession(properties);
            session.connect();
            logger.info("Connection successful. Session name: {}", session.getSessionName());
        } catch (JCSMPException e) {
            logger.error("Connection has failed: {}", e.getMessage());
        }
    }

    public JCSMPSession getSession() {
        return session;
    }

}