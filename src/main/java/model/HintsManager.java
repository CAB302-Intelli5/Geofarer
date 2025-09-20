package model;

import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;


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
