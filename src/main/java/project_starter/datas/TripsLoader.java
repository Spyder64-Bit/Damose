package project_starter.datas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader per il file trips.txt (GTFS statico).
 */
public class TripsLoader {

    public static List<Trips> load(String resourcePath) {
        List<Trips> trips = new ArrayList<>();

        try (
            InputStream in = TripsLoader.class.getResourceAsStream(resourcePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))
        ) {
            String line;

            // Salta header
            br.readLine();

            while ((line = br.readLine()) != null) {
                List<String> fields = parseCsv(line);

                // trips.txt ha almeno 10 colonne
                if (fields.size() < 10) continue;

                String routeId       = fields.get(0).trim();
                String serviceId     = fields.get(1).trim();
                String tripId        = fields.get(2).trim();
                String tripHeadsign  = fields.get(3).replace("\"", "").trim();
                String tripShortName = fields.get(4).trim();

                int directionId = 0;
                try {
                    if (!fields.get(5).isEmpty()) {
                        directionId = Integer.parseInt(fields.get(5));
                    }
                } catch (NumberFormatException ignored) {}

                String shapeId = fields.get(7).trim();

                // Costruisci Trips con tripShortName
                trips.add(new Trips(routeId, serviceId, tripId, tripHeadsign, tripShortName, directionId, shapeId));
            }

        } catch (Exception e) {
            System.err.println("Errore nel caricamento di trips.txt: " + e.getMessage());
            e.printStackTrace();
        }

        return trips;
    }

    /**
     * Parser CSV semplice che gestisce campi con virgolette.
     */
    private static List<String> parseCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
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
    }}