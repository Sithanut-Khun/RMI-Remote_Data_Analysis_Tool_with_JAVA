package client;

import client.gui.AnalysisClientGUI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientLauncher {
    private static final Logger logger = Logger.getLogger(ClientLauncher.class.getName());

    public static void main(String[] args) {
        System.out.println("Starting the Analysis Client...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down the Analysis Client...");
        }));

        try {
            AnalysisClientGUI.main(args);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while launching the client", e);
        }
    }
}