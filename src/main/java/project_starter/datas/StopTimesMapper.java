package project_starter.datas;

import java.util.List;

public class StopTimesMapper {

    public static List<StopTime> getStopTimesForStop(String stopId) {
        return StopTimesLoader.getStopTimesForStop(stopId);
    }
}
