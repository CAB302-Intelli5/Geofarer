package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode; //Using Jackson to parse Json
import com.fasterxml.jackson.databind.ObjectMapper;


public class HintsDAO {
    private String countryData;
    private String dbPath = "src/main/resources/factbook.db"; // Path to the SQLite database
    private String query = "SELECT data FROM factbook WHERE LOWER(gec) = LOWER(?)"; // Ensure case-insensitive match
    private String gecCode;

    /**
     * Constructs a HintsDAO for a specific country
     * @param gecCode
     */
    public HintsDAO(String gecCode) {
        this.gecCode = gecCode;
    }


    private void queryFactbook() {

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, gecCode); // Pass the GEC code as is
            stmt.setString(1, gecCode.toLowerCase()); // force lower case as shape file is upper case
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) { //checks to see if there is data for the gecCode
                this.countryData = rs.getString("data");

                System.out.println("Data for " + gecCode);
            } else {
                System.out.println("No data found for " + gecCode);
            }
        } catch (SQLException e) {
            e.printStackTrace();}
    }

    /**
     * Parses the json country data and extract hints about the country
     * Extracts location, climate, continent, areaa, coastline and land boundaries
     * @return a list of the formatted country hints as strings
     * @throws JsonProcessingException if the JSON parsing fails
     */
    public List<String> getCountryHints() throws JsonProcessingException {
        List<String> countryHints = new ArrayList<>();
        queryFactbook();

        // If no data was found, return an empty list to prevent a crash.
        if (countryData == null || countryData.isEmpty()) {
            System.err.println("Could not generate hints for " + gecCode + " as no data was found.");
            return new ArrayList<>(); // Return an empty list
        }

        ObjectMapper mapper = new ObjectMapper(); //Using Jackson to parse JSON response body
        JsonNode root = mapper.readTree(countryData); //Read JSON file tree

        String location = root
                .path("Geography")
                .path("Location")
                .path("text")
                .asText();
        String climate = root
                .path("Geography")
                .path("Climate")
                .path("text")
                .asText();
        String continent = root
                .path("Geography")
                .path("Map references")
                .path("text")
                .asText();
        String totalArea = root
                .path("Geography")
                .path("Area")
                .path("total ")
                .path("text")
                .asText();
        String coastline = root
                .path("Geography")
                .path("Coastline")
                .path("text")
                .asText();
        String landBoundaries = root
                .path("Geography")
                .path("Land boundaries")
                .path("total")
                .path("text")
                .asText();

        countryHints.add("CLIMATE: " + climate);
        countryHints.add("AREA: " + totalArea);
        countryHints.add("COASTLINE: " + coastline);
        countryHints.add("LAND BOUNDARIES: " + landBoundaries);
        countryHints.add("CONTINENT: " + continent);
        countryHints.add("LOCATION DESCRIPTION: " + location);

        System.out.println("Country hints parsed and stored.");
        return countryHints;
    }

}
