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

            // Transpose column-wise data
            for (Integer colIndex : selectedColumns) {
                List<String> columnData = new ArrayList<>();
                for (List<String> row : csvData) {
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
                    .collect(Collectors.toList());


                    if (!numericValues.isEmpty()) {
                        double sum = numericValues.stream().mapToDouble(Double::doubleValue).sum();
                        double mean = sum / numericValues.size();
                        double min = Collections.min(numericValues);
                        double max = Collections.max(numericValues);
                        double variance = numericValues.stream().mapToDouble(x -> Math.pow(x - mean, 2)).average().orElse(0.0);
                        double stdDev = Math.sqrt(variance);

                        columnStats.put("sum", String.valueOf(sum));
                        columnStats.put("mean", String.valueOf(mean));
                        columnStats.put("min", String.valueOf(min));
                        columnStats.put("max", String.valueOf(max));
                        columnStats.put("stdDev", String.valueOf(stdDev));
                        columnStats.put("variance", String.valueOf(variance));
                    }
                } else {
                    columnStats.put("type", "string");
                }

                result.put("Column " + colIndex, columnStats);
            }

        return result;
    }


    @Override
    public String testConnection() throws RemoteException {
        return "Server is running and ready for analysis!";
    }
}