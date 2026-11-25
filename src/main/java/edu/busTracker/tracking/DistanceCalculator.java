package edu.busTracker.tracking;

import edu.busTracker.model.Stop;

/**
 * Calculates geographical distances using the Haversine formula.
 * Demonstrates: method overloading (Unit 2 Polymorphism).
 */
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Overload 1: Distance between raw coordinates (lat1, lon1, lat2, lon2).
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Overload 2: Distance between two Stop objects.
     */
    public static double distance(Stop stop1, Stop stop2) {
        if (stop1 == null || stop2 == null || stop1.getLatitude() == null || stop2.getLatitude() == null) {
            return 0.0;
        }
        return distance(stop1.getLatitude(), stop1.getLongitude(),
                        stop2.getLatitude(), stop2.getLongitude());
    }
}
