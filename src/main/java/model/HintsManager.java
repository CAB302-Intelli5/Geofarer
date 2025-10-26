package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import utils.SessionManager;

import java.time.LocalDate;

/**
 * Stores and displays hints for a given country.
 */

public class HintsManager {

    List<String> countryHints;
    String fips10;
    String countryName;

    /**
     * Constructs a new HintsManager that holds a list of hints for a country.
     *
     * @param fips10      2 character code used by the US government to represent Geopolitical Entities and Codes
     * @param countryName Country name passed from the NaturalEarth database
     */
    public HintsManager(String fips10, String countryName) {
        HintsDAO hintsDAO = new HintsDAO(fips10);
        try {
            this.countryHints = hintsDAO.getCountryHints();
            this.fips10 = fips10;
            this.countryName = countryName;
            saveCountry();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the next hint category depending on how many hints have been shown so far.
     *
     * @param hintsShown How many hints have been shown so far. hintsShown = guessCount - 1
     * @return a string of the next hint for a given country
     * @throws JsonProcessingException if there is an issue with parsing JSON
     */
    public String showNextHint(int hintsShown) throws JsonProcessingException {
        String nextHint;
        if (hintsShown < 6) {
            nextHint = countryHints.get(hintsShown);
        } else {
            nextHint = "No more hints left!";
        }
        String hint = ("Hint" + hintsShown + ": " + nextHint);
        System.out.println(hint);
        return nextHint;
    }

    /**
     * Saves hint records to database to be referenced in unlocked_hints
     * @return
     */
    public boolean saveHints() {
        LocalDate seenDate = LocalDate.now();
        String insertHint = "INSERT INTO hints(hint_id, hint_type, hint_text, date_retrieved, country_id) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            for (int i = 0; i < countryHints.size(); i++) {
                String[] hintSplit = countryHints.get(i).split(":", 2);
                String hintType = hintSplit[0].trim();
                String hintBody = hintSplit[1].trim();
                String hintId = fips10 + i;
                //Saves hint to hint table in the Geofarer database
                try (PreparedStatement stmt = conn.prepareStatement(insertHint)) {
                    stmt.setString(1, hintId);
                    stmt.setString(2, hintType);
                    stmt.setString(3, hintBody);
                    stmt.setString(4, seenDate.toString());
                    stmt.setString(5, fips10);
                    stmt.executeUpdate();
                    System.out.println("New hint saved!");

                } catch (SQLException e) { //Handles attempt to insert duplicate hint
                    if (e.getMessage().contains("UNIQUE")) {
                        //Don't print stack trace if unique constraint failing. Intended behaviour.
                        System.out.println("Hint already exists in Geofarer database");
                        break;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Unlocks all of a given country's hints for the user
     * @return
     */
    public boolean unlockAllHints(){
        String insertSeenHint = "INSERT INTO unlocked_hints(hint_id, user_id, seen_date) VALUES (?, ?, ?)";
        LocalDate seenDate = LocalDate.now();

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            //Only unlock hint if logged in
            if(SessionManager.getInstance().isLoggedIn()) {
                String userId = (SessionManager.getInstance().getCurrentUserId()).toString();
                for (int i = 0; i < countryHints.size(); i++) {
                    String hintId = fips10 + i;
                    try (PreparedStatement stmt = conn.prepareStatement(insertSeenHint)) {
                        stmt.setString(1, hintId);
                        stmt.setString(2, userId);
                        stmt.setString(3, seenDate.toString());
                        stmt.executeUpdate();
                        System.out.println("Hint unlocked!");
                    } catch (SQLException e) {
                        if (e.getMessage().contains("UNIQUE")) {
                            //Don't print stack trace if unique constraint failing. Intended behaviour.
                            System.out.println("Hint already seen by user");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Saves the country that the HintsManager is being instantiated for in the Geofarer database
     * @return
     */
    private boolean saveCountry() {
        //Saves country record in countries table in the Geofarer database
        String insertCountry = "INSERT INTO countries(country_id, name, region) VALUES (?, ?, ?)";
        String region = countryHints.get(4).split(":", 2)[1].trim();
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(insertCountry)) {
                stmt.setString(1, fips10);
                stmt.setString(2, countryName);
                stmt.setString(3, region);
                stmt.executeUpdate();
                System.out.println("New country saved!");
            } catch (SQLException e) { //Handles attempt to insert duplicate country
                if (e.getMessage().contains("UNIQUE")) {
                    //Don't print stack trace if unique constraint failing. Intended behaviour.
                    System.out.println("Country already exists in Geofarer database");
                } else {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Retrieves hints the user has unlocked from the database
     * @return
     */
    public List<String> getUnlockedHints(String fips10) {
        this.fips10 = fips10;
        List<String> unlockedHints = new ArrayList<>();
        String selectUnlockedHints = """
                SELECT 
                    h.hint_type,
                    h.hint_text
                FROM hints h
                JOIN unlocked_hints uh
                ON h.hint_id=uh.hint_id
                WHERE uh.user_id=? AND h.country_id=?
                """;
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String userId = (SessionManager.getInstance().getCurrentUserId()).toString();
            System.out.println("Accessing unlocked hints for user id:" + userId + "and country: " + fips10);
                try (PreparedStatement stmt = conn.prepareStatement(selectUnlockedHints)) {
                    stmt.setString(1, userId);
                    stmt.setString(2, fips10);
                    System.out.println("Loading country hints...");
                    ResultSet rs = stmt.executeQuery();
                    while(rs.next()){
                        unlockedHints.add(rs.getString("hint_type") + ": " + rs.getString("hint_text"));
                    }
                    System.out.println("Loaded unlocked hint!");
                } catch (SQLException e) {
                    if (e.getMessage().contains("UNIQUE")) {
                        //Don't print stack trace if unique constraint failing. Intended behaviour.
                        System.err.println("Error loading unlocked hints for user");
                    } else {
                        e.printStackTrace();
                    }
                }
        }catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error connecting to database");
        }
        return unlockedHints;
    }
}