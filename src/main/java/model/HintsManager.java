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
     * Constructs a hints manager for a given country
     * fetches the country hints using {@link HintsDAO}
     * @param gecCode the GEC / FIPS10 code of the country
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
     * Shows the next hint in the stored list based on the hints already shown.
     * If all the hints have been shown return the message indicating no more hints
     * @param hintsShown the number of hints already shown
     * @return a string of the next hint in the list
     * @throws JsonProcessingException an exception if there is an error actually receiving the hints
     */
    public String showNextHint(int hintsShown) throws JsonProcessingException {
        String nextHint;
        if(hintsShown < 6){
            nextHint = countryHints.get(hintsShown);
        }else{
            nextHint = "No more hints left!";
        }
        System.out.println("Hint" + hintsShown + ": " +nextHint);
        return nextHint;
    }
}
