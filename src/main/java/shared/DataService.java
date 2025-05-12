package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface DataService extends Remote {
    // Basic statistical analysis
    Map<String, Double> analyzeData(List<Double> data) throws RemoteException;
    
    // CSV file analysis
    Map<String, Object> analyzeCSV(String filePath) throws RemoteException;
    
    // Time series analysis
    Map<String, Double> timeSeriesAnalysis(Map<String, List<Double>> timeSeriesData) throws RemoteException;
    
    // Test connection
    String testConnection() throws RemoteException;
}