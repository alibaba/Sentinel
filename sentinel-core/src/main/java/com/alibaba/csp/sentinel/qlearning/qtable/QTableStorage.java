package com.alibaba.csp.sentinel.qlearning.qtable;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QTableStorage {

    public synchronized static void save(ConcurrentHashMap<String, double[]> qTable, String filePath) {

        try (
                PrintStream output = new PrintStream(new File(filePath));) {

            for (Map.Entry entry : qTable.entrySet()) {
                String key = (String) entry.getKey();
                double[] value = (double[]) entry.getValue();
                String line = key + " " + value[0] + " " + value[1];
                output.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static ConcurrentHashMap<String, double[]> read(String pathname) throws IOException {
        ConcurrentHashMap<String, double[]> qTable = new ConcurrentHashMap<>();

        File file = new File(pathname);
        if (!file.exists()) {
            try {
                file.createNewFile();
                return qTable;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(pathname);
                 BufferedReader br = new BufferedReader(reader)
            ) {
                String line;

                while ((line = br.readLine()) != null) {
                    String[] lineList = line.split(" ");
                    double[] temp = new double[]{Double.valueOf(lineList[1]), Double.valueOf(lineList[2])};
                    qTable.put(lineList[0], temp);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return qTable;
    }
}