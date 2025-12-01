package project_starter.datas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class StopsLoader {

    public static List<Stops> load(String resourcePath) {
        List<Stops> stops = new ArrayList<>();

        try (
            InputStream in = StopsLoader.class.getResourceAsStream(resourcePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))
        ) {
            String line;

            // Salta header
            br.readLine();

            while ((line = br.readLine()) != null) {
                List<String> fields = parseCsv(line);

                if (fields.size() < 6) continue;

                String stopId   = fields.get(0).trim();
                String stopCode = fields.get(1).trim();
                String stopName = fields.get(2).trim();
                String latStr   = fields.get(4).trim();
                String lonStr   = fields.get(5).trim();

                if (latStr.isEmpty() || lonStr.isEmpty()) continue;

                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);

                stops.add(new Stops(stopId, stopCode, stopName, lat, lon));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stops;
    }

    private static List<String> parseCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }
}
