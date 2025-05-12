package client;

import shared.DataService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ConnectionTest {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            DataService service = (DataService) registry.lookup("DataService");
            System.out.println("Test successful! Server says: " + service.testConnection());
        } catch (Exception e) {
            System.err.println("Connection failed:");
            e.printStackTrace();
        }
    }
}