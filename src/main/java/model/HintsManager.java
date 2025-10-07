package model;

import java.sql.*;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;

/**
 * Stores and displays hints for a given country.
 */

public class HintsManager {

    List<String> countryHints;
    String fips10;
    String countryName;

    /**
     * Constructs a new HintsManager that holds a list of hints for a country.
     * @param fips10 2 character code used by the US government to represent Geopolitical Entities and Codes
     * @param countryName Country name passed from the NaturalEarth database
     */
    public HintsManager(String fips10, String countryName) {
        HintsDAO hintsDAO = new HintsDAO(fips10);
        try {
            this.countryHints = hintsDAO.getCountryHints();
            this.fips10 = fips10;
            this.countryName = countryName;
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
    }

    /**
     * Shows the next hint category depending on how many hints have been shown so far.
     * @param hintsShown How many hints have been shown so far. hintsShown = guessCount - 1
     * @return a string of the next hint for a given country
     * @throws JsonProcessingException if there is an issue with parsing JSON
     */
    public String showNextHint(int hintsShown) throws JsonProcessingException {
        String nextHint;
        if(hintsShown < 6){
            nextHint = countryHints.get(hintsShown);
        }else{
            nextHint = "No more hints left!";
        }
        String hint = ("Hint" + hintsShown + ": " +nextHint);
        String hintId = fips10 + nextHint;
        saveHint(hintId, nextHint);
        System.out.println(hint);
        return nextHint;
    }

    /**
     *
     * @param hintId HintID comes from the country's GEC/FIPs CODE and the hint number
     * @param hintText
     * @return
     */
    public boolean saveHint(String hintId, String hintText){
        LocalDateTime datetime = LocalDateTime.now();
        String insertCountry = "INSERT INTO countries(country_id, name, region) VALUES (?, ?, ?)";
        String insertHint = "INSERT INTO hints(hint_id, hint_text, date_retrieved, country_id) VALUES(?, ?, ?, ?)";
        String insertSeenHint = "INSERT INTO unlocked_hints(hint_id, user_id, seen_date) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getFactbookConnection()){
            try (PreparedStatement stmt = conn.prepareStatement(insertCountry)){
                stmt.setString(1, fips10);
                stmt.setString(2, countryName);
                stmt.setString(3, countryHints.get(4));
                System.out.println("New country saved!");
            }catch (SQLIntegrityConstraintViolationException e){ //Handles attempt to insert duplicate country
                e.printStackTrace();
                System.out.println("Country already exists in Geofarer database");
            }
            try (PreparedStatement stmt = conn.prepareStatement(insertHint)){
                stmt.setString(1, hintId);
                stmt.setString(2, hintText);
                stmt.setString(3, datetime.toString());
                stmt.setString(4, fips10);
                System.out.println("New hint saved!");
            }catch (SQLIntegrityConstraintViolationException e){ //Handles attempt to insert duplicate hint
                e.printStackTrace();
                System.out.println("Hint already exists in Geofarer database");
            }
            try (PreparedStatement stmt = conn.prepareStatement(insertSeenHint)){
                stmt.setString(1, hintId);
                //stmt.setString(2, userId);
                stmt.setString(3, datetime.toString());
                System.out.println("Hint unlocked!");
            }catch (SQLIntegrityConstraintViolationException e){
                e.printStackTrace();
                System.out.println("Hint already seen by user");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
