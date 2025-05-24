package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;



public interface DataService extends Remote {
    // Basic statistical analysis
    Map<String, Map<String, String>> analyzeCSV(List<String> columnNames, List<List<String>> csvData) throws RemoteException;
    
}