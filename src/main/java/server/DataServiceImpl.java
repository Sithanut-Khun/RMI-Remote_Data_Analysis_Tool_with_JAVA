package server;

import shared.DataService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class DataServiceImpl extends UnicastRemoteObject implements DataService {
    private static final Logger logger = Logger.getLogger(DataServiceImpl.class.getName());
    
    public DataServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Map<String, Map<String, String>> analyzeCSV(List<String> columnNames, List<List<String>> csvData)
        throws RemoteException {

        logger.info("Received analyze request from client");
        logger.info("Starting CSV analysis...");
        logger.info("Analyzing columns: " + columnNames);   


        // Validate input
        if (columnNames == null || csvData == null) {
            logger.severe("Input parameters cannot be null");
            throw new RemoteException("Input parameters cannot be null");
        }

        if (columnNames.isEmpty() || csvData.isEmpty()) {
            logger.warning("Empty column names or CSV data");
            return new LinkedHashMap<>();
        }

        // Verify column count matches data
        int expectedColumns = columnNames.size();
        for (List<String> row : csvData) {
            if (row.size() != expectedColumns) {
                throw new RemoteException(
                    String.format("Column count mismatch. Expected %d columns, found %d", 
                    expectedColumns, row.size())
                );
            }
        }

        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        // Process each column
        for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
            String columnName = columnNames.get(colIndex);
            List<String> columnData = extractColumnData(csvData, colIndex);
            
            Map<String, String> columnStats = calculateColumnStatistics(columnName, columnData);
            result.put(columnName, columnStats);
        }
        logger.info("Analysis complete, returning results to client.");
        return result;
    }

    private List<String> extractColumnData(List<List<String>> csvData, int colIndex) {
        List<String> columnData = new ArrayList<>(csvData.size());
        for (List<String> row : csvData) {
            columnData.add(row.get(colIndex));
        }
        return columnData;
    }

    private Map<String, String> calculateColumnStatistics(String columnName, List<String> columnData) {
        Map<String, String> stats = new LinkedHashMap<>();
        
        // Basic counts
        int nullCount = (int) columnData.stream()
            .filter(s -> s == null || s.trim().isEmpty())
            .count();
        
        stats.put("count", String.valueOf(columnData.size() - nullCount));
        stats.put("nulls", String.valueOf(nullCount));

        // Check if numeric
        boolean isNumeric = columnData.stream()
            .filter(s -> s != null && !s.trim().isEmpty())
            .allMatch(this::isNumeric);

        if (isNumeric) {
            calculateNumericStatistics(columnData, stats);
        } else {
            stats.put("type", "string");
            stats.put("unique", String.valueOf(
                columnData.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .distinct()
                    .count()
            ));
        }

        return stats;
    }

    private void calculateNumericStatistics(List<String> columnData, Map<String, String> stats) {
        List<Double> numericValues = columnData.stream()
            .filter(s -> s != null && !s.trim().isEmpty())
            .map(Double::parseDouble)
            .sorted()
            .collect(Collectors.toList());

        if (numericValues.isEmpty()) {
            return;
        }

        double sum = numericValues.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / numericValues.size();
        double min = numericValues.get(0);
        double max = numericValues.get(numericValues.size() - 1);
        
        stats.put("sum", String.format("%.4f", sum));
        stats.put("mean", String.format("%.4f", mean));
        stats.put("min", String.format("%.4f", min));
        stats.put("max", String.format("%.4f", max));
        stats.put("type", "numeric");

        // Additional stats for numeric columns
        if (numericValues.size() > 1) {
            double variance = numericValues.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
            double stdDev = Math.sqrt(variance);
            
            stats.put("stdDev", String.format("%.4f", stdDev));
            stats.put("variance", String.format("%.4f", variance));
            stats.put("25%", String.format("%.4f", percentile(numericValues, 25)));
            stats.put("50%", String.format("%.4f", percentile(numericValues, 50)));
            stats.put("75%", String.format("%.4f", percentile(numericValues, 75)));
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double percentile(List<Double> sortedValues, double percentile) {
        int n = sortedValues.size();
        double rank = percentile / 100.0 * (n - 1);
        int low = (int) Math.floor(rank);
        int high = (int) Math.ceil(rank);
        
        if (low == high) {
            return sortedValues.get(low);
        }
        
        double weight = rank - low;
        return sortedValues.get(low) * (1 - weight) + sortedValues.get(high) * weight;
    }

    // @Override
    // public String testConnection() throws RemoteException {
    //     return "Server is running and ready for analysis!";
    // }
}