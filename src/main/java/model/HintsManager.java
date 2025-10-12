package model;

import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;

import utils.SessionManager;

import java.time.LocalDate;

/**
 * Manages the retrieval and display of the hints for a specific country
 * fetches the hints from the database and provides methods to display hints one at a time
 */
public class HintsManager {

    List<String> countryHints;

    public HintsManager(String gecCode) {
        HintsDAO hintsDAO = new HintsDAO(gecCode);
        try {
            this.countryHints = hintsDAO.getCountryHints();
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
    public String showNextHint(int hintsShown, String countryName) throws JsonProcessingException {
        String nextHint;

        String hintId = fips10 + hintsShown;
        if(hintsShown < 6){
            nextHint = countryHints.get(hintsShown);
            saveHint(hintId, nextHint, countryName);
        }else{
            nextHint = "No more hints left!";
        }

        String hint = ("Hint" + hintsShown + ": " +nextHint);
        System.out.println(hint);
        return nextHint;
    }

    /**
     * Creates a record in the database that links an unlocked/seen hint to a user, if unique.
     * @param hintId HintID comes from the country's GEC/FIPs CODE and the hint number
     * @param hintText The string of the hint being saved
     * @param countryName String of the country name from the Natural Earth database
     * @return
     */
    public boolean saveHint(String hintId, String hintText, String countryName){
        LocalDate seenDate = LocalDate.now();
        String insertCountry = "INSERT INTO countries(country_id, name, region) VALUES (?, ?, ?)";
        String insertHint = "INSERT INTO hints(hint_id, hint_type, hint_text, date_retrieved, country_id) VALUES(?, ?, ?, ?, ?)";
        String insertSeenHint = "INSERT INTO unlocked_hints(hint_id, user_id, seen_date) VALUES (?, ?, ?)";

        String[] hintSplit = hintText.split(":", 2);
        String hintType = hintSplit[0].trim();
        String hintBody = hintSplit[1].trim();
        String region = countryHints.get(4).split(":", 2)[1].trim();

        try (Connection conn = DBConnection.getInstance().getConnection()){
            //Saves country record in countries table in the Geofarer database
            try (PreparedStatement stmt = conn.prepareStatement(insertCountry)){
                stmt.setString(1, fips10);
                stmt.setString(2, countryName);
                stmt.setString(3, region);
                stmt.executeUpdate();
                System.out.println("New country saved!");
            }catch (SQLException e){ //Handles attempt to insert duplicate country
                if (e.getMessage().contains("UNIQUE")){
                    //Don't print stack trace if unique constraint failing. Intended behaviour.
                    System.out.println("Country already exists in Geofarer database");
                }else {
                    e.printStackTrace();
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(insertHint)){
                //Saves hint to hint table in the Geofarer database
                stmt.setString(1, hintId);
                stmt.setString(2, hintType);
                stmt.setString(3, hintBody);
                stmt.setString(4, seenDate.toString());
                stmt.setString(5, fips10);
                stmt.executeUpdate();
                System.out.println("New hint saved!");
            }catch (SQLException e){ //Handles attempt to insert duplicate hint
                if (e.getMessage().contains("UNIQUE")){
                    //Don't print stack trace if unique constraint failing. Intended behaviour.
                    System.out.println("Hint already exists in Geofarer database");
                }else {
                    e.printStackTrace();
                }
            }
            //Only save unlocked hint if logged in
            if(SessionManager.getInstance().isLoggedIn()) {
                String userId = (SessionManager.getInstance().getCurrentUserId()).toString();
                try (PreparedStatement stmt = conn.prepareStatement(insertSeenHint)) {
                    //No hint text duplicates in the database. Users and hints are linked together using foreign IDs.
                    stmt.setString(1, hintId);
                    stmt.setString(2, userId);
                    stmt.setString(3, seenDate.toString());
                    stmt.executeUpdate();
                    System.out.println("Hint unlocked!");
                } catch (SQLException e) {
                    if (e.getMessage().contains("UNIQUE")){
                        //Don't print stack trace if unique constraint failing. Intended behaviour.
                        System.out.println("Hint already seen by user");
                    }else {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
