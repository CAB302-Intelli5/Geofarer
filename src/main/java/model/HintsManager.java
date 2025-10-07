package model;

import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Manages the retrieval and display of the hints for a specific country
 * fetches the hints from the database and provides methods to display hints one at a time
 */
public class HintsManager {

    List<String> countryHints;

    /**
     * Constructs a new HintsManager that holds a list of hints for a country.
     * @param gecCode 2 character code used by the US government to represent Geopolitical Entities and Codes
     */
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
    public String showNextHint(int hintsShown) throws JsonProcessingException {
        String nextHint;
        if(hintsShown < 6){
            nextHint = countryHints.get(hintsShown);
        }else{
            nextHint = "No more hints left!";
        }
        String hint = ("Hint" + hintsShown + ": " +nextHint);
        System.out.println(hint);
        return nextHint;
    }
}
