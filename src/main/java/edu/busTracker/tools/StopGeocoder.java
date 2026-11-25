package edu.busTracker.tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Command-line geocoding & route geometry tool.
 * Queries OpenStreetMap Nominatim with rate limiting (1 req/sec) and bounding box validation.
 * Queries OSRM for driving route geometry.
 *
 * Run with:
 *   mvn exec:java -Dexec.mainClass="edu.busTracker.tools.StopGeocoder"
 */
public class StopGeocoder {

    private static final String USER_AGENT = "SRMAP-CollegeBusTracker/1.0 (transport-project@srmap.edu.in)";
    private static final double MIN_LAT = 15.8;
    private static final double MAX_LAT = 16.8;
    private static final double MIN_LON = 80.2;
    private static final double MAX_LON = 80.9;

    public static void main(String[] args) {
        System.out.println("=== College Bus Tracking System — Stop Geocoder ===");
        System.out.println("Bounding box: Lat [" + MIN_LAT + ", " + MAX_LAT + "], Lon [" + MIN_LON + ", " + MAX_LON + "]");

        Path outDir = Paths.get("data/generated");
        Path routeDir = Paths.get("data/generated/route_geometry");

        try {
            Files.createDirectories(outDir);
            Files.createDirectories(routeDir);
        } catch (IOException e) {
            System.err.println("Could not create generated directories: " + e.getMessage());
            return;
        }

        Map<String, String> stopTownHints = new LinkedHashMap<>();
        stopTownHints.put("Sai Baba Temple", "Tenali");
        stopTownHints.put("Tenali Bus Stand", "Tenali");
        stopTownHints.put("Duggirala", "Duggirala, Guntur");
        stopTownHints.put("Ravindrapadu", "Duggirala, Guntur");
        stopTownHints.put("Pedavadlapudi", "Mangalagiri, Guntur");
        stopTownHints.put("Ratnalacheruvu", "Guntur");
        stopTownHints.put("Chinnaravuri Park", "Tenali");
        stopTownHints.put("Anjaneyaswamy Temple", "Tenali");
        stopTownHints.put("Billal Hotel", "Tenali");
        stopTownHints.put("Kotha Vanthana", "Tenali");
        stopTownHints.put("Sivaji Chowk", "Tenali");
        stopTownHints.put("Mythri Hospital", "Tenali");
        stopTownHints.put("Chakali Cheruvu", "Tenali");
        stopTownHints.put("Lakshmi Theater", "Tenali");
        stopTownHints.put("Kativaram", "Tenali");
        stopTownHints.put("Machikalapudi", "Guntur");
        stopTownHints.put("Mangalagiri Old Bus Stand", "Mangalagiri");
        stopTownHints.put("SRM University-AP (Campus)", "Neerukonda, Mangalagiri");

        StringBuilder csv = new StringBuilder("stop_name,lat,lon,display_name,status\n");

        for (Map.Entry<String, String> entry : stopTownHints.entrySet()) {
            String stop = entry.getKey();
            String hint = entry.getValue();
            System.out.print("Geocoding '" + stop + "' (" + hint + ")... ");

            try {
                String query = "SRM University-AP (Campus)".equals(stop) ?
                        "SRM University-AP, Neerukonda, Mangalagiri, Andhra Pradesh, India" :
                        stop + ", " + hint + ", Andhra Pradesh, India";

                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String urlStr = "https://nominatim.openstreetmap.org/search?format=jsonv2&q=" + encoded + "&limit=3";

                URI uri = URI.create(urlStr);
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String json = reader.lines().reduce("", (a, b) -> a + b);
                        if (json.contains("\"lat\"") && json.contains("\"lon\"")) {
                            // Extract first result lat/lon
                            double lat = extractNumber(json, "\"lat\":\"", "\"");
                            double lon = extractNumber(json, "\"lon\":\"", "\"");
                            if (lat >= MIN_LAT && lat <= MAX_LAT && lon >= MIN_LON && lon <= MAX_LON) {
                                System.out.println("OK: " + lat + ", " + lon);
                                csv.append(escapeCsv(stop)).append(",").append(lat).append(",").append(lon)
                                   .append(",\"AUTO nominatim\",AUTO\n");
                            } else {
                                System.out.println("OUT OF BOUNDS (" + lat + ", " + lon + ") -> PENDING");
                                csv.append(escapeCsv(stop)).append(",,,,PENDING\n");
                            }
                        } else {
                            System.out.println("NO RESULTS -> PENDING");
                            csv.append(escapeCsv(stop)).append(",,,,PENDING\n");
                        }
                    }
                } else {
                    System.out.println("HTTP " + conn.getResponseCode() + " -> PENDING");
                    csv.append(escapeCsv(stop)).append(",,,,PENDING\n");
                }

                // Rate limiting: 1 second between calls as required by Nominatim usage policy
                Thread.sleep(1100);

            } catch (Exception e) {
                System.out.println("OFFLINE/ERROR (" + e.getMessage() + ") -> PENDING");
                csv.append(escapeCsv(stop)).append(",,,,PENDING\n");
            }
        }

        try {
            Files.writeString(outDir.resolve("stops_geocoded.csv"), csv.toString(), StandardCharsets.UTF_8);
            System.out.println("Successfully generated data/generated/stops_geocoded.csv");
        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }

    private static double extractNumber(String json, String startTag, String endTag) {
        int idx = json.indexOf(startTag);
        if (idx == -1) return 0.0;
        int start = idx + startTag.length();
        int end = json.indexOf(endTag, start);
        return Double.parseDouble(json.substring(start, end));
    }

    private static String escapeCsv(String s) {
        return s.contains(",") ? "\"" + s + "\"" : s;
    }
}
