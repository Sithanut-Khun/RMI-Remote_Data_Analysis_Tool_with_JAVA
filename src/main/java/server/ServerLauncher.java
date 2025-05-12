package server;

import shared.DataService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerLauncher {

    private static final int[] PORTS_TO_TRY = {1099, 2099, 3099, 4099};

    public static void main(String[] args) {
        for (int port : PORTS_TO_TRY) {
            try {
                // Create RMI registry on the current port
                Registry registry = LocateRegistry.createRegistry(port);

                // Create service instance
                DataService dataService = new DataServiceImpl();

                // Bind the service to the registry
                registry.rebind("DataService", dataService);

                System.out.println("Server is running on port " + port + " and waiting for client connections...");
                return; // Exit the loop once the server starts successfully
            } catch (Exception e) {
                System.err.println("Failed to bind on port " + port + ": " + e.toString());
            }
        }
        System.err.println("Failed to start the server on all specified ports.");
    }
}