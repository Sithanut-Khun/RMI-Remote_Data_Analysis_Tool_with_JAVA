package server;

import shared.DataService;
import util.LoggingUtil;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    public static void main(String[] args) {
        LoggingUtil.setupLogger("server.log"); // Initialize logging

        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");

            Registry registry = LocateRegistry.getRegistry(1099);
            DataService dataService = new DataServiceImpl();
            registry.rebind("DataService", dataService);

            logger.info("Server bound DataService to registry on port 1099 (localhost)");
            logger.info("Server is running. Press Ctrl+C to stop.");

            new CountDownLatch(1).await(); // Keep server running

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Server exception: " + e.getMessage(), e);
        }
    }
}
