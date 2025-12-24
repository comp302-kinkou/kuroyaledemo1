package com.example.kuroyale.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.File;
import java.io.FileWriter;

public class NetworkConfig {
    private static NetworkConfig instance;
    private Properties properties;
    private static final String CONFIG_FILE = "network config.txt";

    // Default values
    private int defaultPort = 8080;
    private int connectionTimeout = 5000;
    private int reconnectAttempts = 3;
    private int syncInterval = 100;
    private int maxMessageSize = 1024;

    private NetworkConfig() {
        properties = new Properties();
        loadConfig();
    }

    public static synchronized NetworkConfig getInstance() {
        if (instance == null) {
            instance = new NetworkConfig();
        }
        return instance;
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);

                // Parse values
                defaultPort = Integer.parseInt(properties.getProperty("DEFAULT_PORT", "8080"));
                connectionTimeout = Integer.parseInt(properties.getProperty("CONNECTION_TIMEOUT", "5000"));
                reconnectAttempts = Integer.parseInt(properties.getProperty("RECONNECT_ATTEMPTS", "3"));
                syncInterval = Integer.parseInt(properties.getProperty("SYNC_INTERVAL", "100"));
                maxMessageSize = Integer.parseInt(properties.getProperty("MAX_MESSAGE_SIZE", "1024"));

                System.out.println("Network config loaded from " + CONFIG_FILE);
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading network config: " + e.getMessage());
                // Fallback to defaults is automatic since fields are initialized
            }
        } else {
            System.out.println("Network config file not found. Using defaults.");
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write("DEFAULT_PORT=" + defaultPort + "\n");
            writer.write("CONNECTION_TIMEOUT=" + connectionTimeout + "\n");
            writer.write("RECONNECT_ATTEMPTS=" + reconnectAttempts + "\n");
            writer.write("SYNC_INTERVAL=" + syncInterval + "\n");
            writer.write("MAX_MESSAGE_SIZE=" + maxMessageSize + "\n");
            System.out.println("Created default network config file.");
        } catch (IOException e) {
            System.err.println("Failed to create default config file: " + e.getMessage());
        }
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getReconnectAttempts() {
        return reconnectAttempts;
    }

    public int getSyncInterval() {
        return syncInterval;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    // For verification debugging
    public String dump() {
        return String.format("Port=%d, Timeout=%d, Attempts=%d, Interval=%d, MaxSize=%d",
                defaultPort, connectionTimeout, reconnectAttempts, syncInterval, maxMessageSize);
    }
}
