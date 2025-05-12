package server;

import shared.DataService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class DataServiceImpl extends UnicastRemoteObject implements DataService {
    
    public DataServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Map<String, Double> analyzeData(List<Double> data) throws RemoteException {
        Map<String, Double> results = new HashMap<>();
        
        if (data == null || data.isEmpty()) {
            return results;
        }
        
        // Basic statistics
        double sum = data.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / data.size();
        double min = Collections.min(data);
        double max = Collections.max(data);
        
        // Standard deviation
        double variance = data.stream()
            .mapToDouble(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        results.put("count", (double) data.size());
        results.put("sum", sum);
        results.put("mean", mean);
        results.put("min", min);
        results.put("max", max);
        results.put("stdDev", stdDev);
        
        return results;
    }

    @Override
    public Map<String, Object> analyzeCSV(String filePath) throws RemoteException {
        Map<String, Object> results = new HashMap<>();
        // Implementation for CSV analysis would go here
        // This could include reading the file, parsing columns, etc.
        try (Scanner scanner = new Scanner(new java.io.File(filePath))) {
            List<Double> data = new ArrayList<>();
            while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            try {
                data.add(Double.parseDouble(line.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid lines
            }
            }
            results.put("analysis", analyzeData(data));
        } catch (java.io.FileNotFoundException e) {
            results.put("error", "File not found: " + filePath);
        } catch (Exception e) {
            results.put("error", "An error occurred while processing the file: " + e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, Double> timeSeriesAnalysis(Map<String, List<Double>> timeSeriesData) throws RemoteException {
        Map<String, Double> results = new HashMap<>();
        // Implementation for time series analysis
        // This could include calculating trends, seasonality, etc.
        if (timeSeriesData == null || timeSeriesData.isEmpty()) {
            return results;
        }

        for (Map.Entry<String, List<Double>> entry : timeSeriesData.entrySet()) {
            String seriesName = entry.getKey();
            List<Double> data = entry.getValue();

            if (data == null || data.isEmpty()) {
            continue;
            }

            // Calculate mean for the time series
            double sum = data.stream().mapToDouble(Double::doubleValue).sum();
            double mean = sum / data.size();

            // Calculate trend (simple linear regression slope)
            int n = data.size();
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += data.get(i);
            sumXY += i * data.get(i);
            sumX2 += i * i;
            }
            double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

            results.put(seriesName + "_mean", mean);
            results.put(seriesName + "_trend", slope);
        }
        return results;
    }

    @Override
    public String testConnection() throws RemoteException {
        return "Server is running and ready for analysis!";
    }
}