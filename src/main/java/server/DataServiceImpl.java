package server;

import shared.DataService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;


import javax.swing.JTable;


public class DataServiceImpl extends UnicastRemoteObject implements DataService {
    
    public DataServiceImpl() throws RemoteException {
        super();
    }


    @Override
    public Map<String, Map<String, String>> analyzeCSV(List<Integer> selectedColumns, List<List<String>> csvData)
        throws RemoteException {

        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        if (csvData == null || csvData.isEmpty()) {
            return result;
        }

        // Assume first row contains column names
        List<String> header = csvData.get(0);

        // Transpose column-wise data
        for (Integer colIndex : selectedColumns) {
            String columnName = (colIndex < header.size()) ? header.get(colIndex) : "Column " + colIndex;
            List<String> columnData = new ArrayList<>();
            // Start from row 1 to skip header
            for (int i = 1; i < csvData.size(); i++) {
                List<String> row = csvData.get(i);
                if (colIndex < row.size()) {
                    columnData.add(row.get(colIndex));
                } else {
                    columnData.add(null);
                }
            }

            boolean isNumeric = columnData.stream().allMatch(s -> {
                if (s == null || s.trim().isEmpty()) return true;
                try {
                    Double.parseDouble(s);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });

            Map<String, String> columnStats = new LinkedHashMap<>();

            int nullCount = (int) columnData.stream().filter(s -> s == null || s.trim().isEmpty()).count();
            columnStats.put("nulls", String.valueOf(nullCount));
            columnStats.put("count", String.valueOf(columnData.size() - nullCount));

            if (isNumeric) {
                List<Double> numericValues = columnData.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(Double::parseDouble)
                    .sorted()
                    .collect(Collectors.toList());

                if (!numericValues.isEmpty()) {
                    double sum = numericValues.stream().mapToDouble(Double::doubleValue).sum();
                    double mean = sum / numericValues.size();
                    double min = Collections.min(numericValues);
                    double max = Collections.max(numericValues);
                    double variance = numericValues.stream().mapToDouble(x -> Math.pow(x - mean, 2)).average().orElse(0.0);
                    double stdDev = Math.sqrt(variance);

                    // Calculate percentiles
                    double p25 = percentile(numericValues, 25);
                    double p50 = percentile(numericValues, 50);
                    double p75 = percentile(numericValues, 75);

                    columnStats.put("sum", String.valueOf(sum));
                    columnStats.put("mean", String.valueOf(mean));
                    columnStats.put("min", String.valueOf(min));
                    columnStats.put("max", String.valueOf(max));
                    columnStats.put("stdDev", String.valueOf(stdDev));
                    columnStats.put("variance", String.valueOf(variance));
                    columnStats.put("25%", String.valueOf(p25));
                    columnStats.put("50%", String.valueOf(p50));
                    columnStats.put("75%", String.valueOf(p75));
                }
            } else {
                columnStats.put("type", "string");
            }

            result.put(columnName, columnStats);
        }

        return result;
    }

    // Helper method to calculate percentile
    private double percentile(List<Double> sortedValues, double percentile) {
        if (sortedValues == null || sortedValues.isEmpty()) return Double.NaN;
        int n = sortedValues.size();
        if (n == 1) return sortedValues.get(0);
        double rank = percentile / 100.0 * (n - 1);
        int low = (int) Math.floor(rank);
        int high = (int) Math.ceil(rank);
        if (low == high) {
            return sortedValues.get(low);
        }
        double weight = rank - low;
        return sortedValues.get(low) * (1 - weight) + sortedValues.get(high) * weight;
    }


    @Override
    public String testConnection() throws RemoteException {
        return "Server is running and ready for analysis!";
    }
}