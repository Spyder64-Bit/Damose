package project_starter.model;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.transit.realtime.GtfsRealtime;

import project_starter.datas.StopTripMapper;
import project_starter.datas.TripUpdateRecord;

public class GTFSFetcher {
    private static final String VEHICLE_POSITIONS_URL =
        "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";

    private static final String TRIP_UPDATES_URL =
        "https://romamobilita.it/sites/default/files/rome_rtgtfs_trip_updates_feed.pb";

    // --- Helpers ---
    private static String norm(String s) {
        return s == null ? null : s.trim();
    }

    private static String normalizeTripId(String id) {
        if (id == null) return null;
        // rimuove prefissi tipo "0#" o "1#"
        return id.replaceAll("^[0#]+", "").trim();
    }

    private static long normalizeEpoch(long raw) {
        if (raw <= 0) return -1;
        if (raw >= 1_000_000_000_000L) {       // millisecondi
            return raw / 1_000;
        } else if (raw >= 10_000_000_000L) {   // decisecondi
            return raw / 10;
        } else if (raw >= 1_000_000_000L) {    // secondi
            return raw;
        } else {
            return -1;
        }
    }

    // --- Vehicle positions ---
    public static List<VehiclePosition> fetchBusPositions() {
        List<VehiclePosition> positions = new ArrayList<>();
        try (InputStream inputStream = new URL(VEHICLE_POSITIONS_URL).openStream()) {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(inputStream);
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasVehicle()) continue;

                GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
                String tripId = normalizeTripId(vehicle.getTrip().getTripId());
                String vehicleId = norm(vehicle.getVehicle().getId());
                double lat = vehicle.getPosition().getLatitude();
                double lon = vehicle.getPosition().getLongitude();
                int stopSeq = vehicle.hasCurrentStopSequence() ? vehicle.getCurrentStopSequence() : -1;

                positions.add(new VehiclePosition(
                    tripId, vehicleId,
                    new org.jxmapviewer.viewer.GeoPosition(lat, lon),
                    stopSeq
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return positions;
    }

    // --- Trip updates ---
    public static List<TripUpdateRecord> fetchTripUpdates(StopTripMapper stopTripMapper) {
        List<TripUpdateRecord> updates = new ArrayList<>();
        try (InputStream inputStream = new URL(TRIP_UPDATES_URL).openStream()) {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(inputStream);

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasTripUpdate()) continue;

                GtfsRealtime.TripUpdate tu = entity.getTripUpdate();
                String tripId = normalizeTripId(tu.getTrip().getTripId());

                for (GtfsRealtime.TripUpdate.StopTimeUpdate stu : tu.getStopTimeUpdateList()) {
                    String stopId = stu.hasStopId() ? norm(stu.getStopId()) : null;

                    // fallback: se stopId non matcha, usa stop_sequence
                    if ((stopId == null || !stopTripMapper.isKnownStopId(stopId)) && stu.hasStopSequence()) {
                        int seq = stu.getStopSequence();
                        String mappedStop = stopTripMapper.getStopIdByTripAndSequence(tripId, seq);
                        if (mappedStop != null) stopId = mappedStop;
                    }

                    long raw = -1;
                    if (stu.hasArrival() && stu.getArrival().hasTime()) {
                        raw = stu.getArrival().getTime();
                    } else if (stu.hasDeparture() && stu.getDeparture().hasTime()) {
                        raw = stu.getDeparture().getTime();
                    }

                    long arrivalEpoch = normalizeEpoch(raw);
                    if (stopId != null && arrivalEpoch > 0) {
                        updates.add(new TripUpdateRecord(tripId, stopId, arrivalEpoch));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updates;
    }
}
