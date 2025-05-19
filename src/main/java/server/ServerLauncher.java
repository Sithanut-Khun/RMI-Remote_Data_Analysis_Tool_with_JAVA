package server;

import shared.DataService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

public class ServerLauncher {
 
    public static void main(String[] args) {
        try {
            // Force stub to use localhost
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");

            // Get existing registry (already started via rmiregistry)
            Registry registry = LocateRegistry.getRegistry(1099);

            // Create service instance and export it
            DataService dataService = new DataServiceImpl();

            // Bind the stub to the registry
            registry.rebind("DataService", dataService);
            System.out.println("✔ DataService bound to registry");

            System.out.println("✅ Server bound DataService to registry on port 1099 (localhost)");


            // --- KEEP SERVER RUNNING ---
            System.out.println("Server is running. Press Ctrl+C to stop.");
            new java.util.concurrent.CountDownLatch(1).await();
        } catch (Exception e) {
            System.err.println("❌ Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
}
