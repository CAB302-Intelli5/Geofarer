package utils;

import model.Database;
import model.MapService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * A one-time utility to populate the 'countries' table from the shapefile.
 * It uses the existing MapService to load feature data.
 */
public class CountryDataImporter {

    public static void main(String[] args) {
        System.out.println("Starting country data import...");

        //Load all country features from the shapefile
        MapService mapService = new MapService();
        List<MapService.FeatureInfo> features = mapService.loadShapefileData();

        if (features == null || features.isEmpty()) {
            System.err.println("No features loaded from shapefile. Aborting.");
            return;
        }

        System.out.println("Found " + features.size() + " features in the shapefile.");

        //Define the SQL for inserting data
        // "INSERT OR IGNORE" will skip any country whose country_id (primary key) already exists.
        String insertSQL = "INSERT OR IGNORE INTO countries (country_id, name, region) VALUES (?, ?, ?);";

        //Connect to the database and perform the batch insert
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            conn.setAutoCommit(false); // Use a transaction for efficiency

            int countriesAdded = 0;
            for (MapService.FeatureInfo feature : features) {
                // Skip features with invalid or missing FIPS codes
                if (feature.fips10 == null || feature.fips10.trim().isEmpty() || feature.fips10.equals("XX")) {
                    System.out.println("Skipping feature with invalid FIPS code: " + feature.name);
                    continue;
                }

                pstmt.setString(1, feature.fips10);
                pstmt.setString(2, feature.name);
                pstmt.setString(3, feature.continent);
                pstmt.addBatch();
                countriesAdded++;
            }

            // Execute the batch of insert statements
            int[] updateCounts = pstmt.executeBatch();
            conn.commit(); // Finalize the transaction

            System.out.println("Import complete.");
            System.out.println("Processed " + countriesAdded + " valid countries.");
            System.out.println(updateCounts.length + " rows were affected in the database.");
        } catch (Exception e) {
            System.err.println("An error occurred during database import:");
            e.printStackTrace();
        }
    }
}