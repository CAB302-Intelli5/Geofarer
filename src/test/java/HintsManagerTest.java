import model.HintsManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HintsManagerTest {
    private static final String GECCODE = "AU"; //Austria
    private static final String CLIMATE_HINT = "CLIMATE: temperate; continental, cloudy; cold winters with frequent rain and some snow in lowlands and snow in mountains; moderate summers with occasional showers";
    private static final String AREA_HINT = "AREA: 83,871 sq km";
    private static final String COASTLINE_HINT = "COASTLINE: 0 km (landlocked)";
    private static final String LANDBOUNDARIES_HINT = "LAND BOUNDARIES: 2,524 km";
    private static final String CONTINENT_HINT = "CONTINENT: Europe";
    private static final String LOCATION_HINT = "LOCATION DESCRIPTION: Central Europe, north of Italy and Slovenia";

    private HintsManager austriaHints;

    @BeforeEach
    public void setUp() {
        this.austriaHints = new HintsManager(GECCODE);
    }

    @Test
    public void testHint1(){
        try {
            assertEquals(CLIMATE_HINT, austriaHints.showNextHint(0));
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testHint2(){
        try {
            assertEquals(AREA_HINT, austriaHints.showNextHint(1));
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testHint3(){
        try {
            assertEquals(COASTLINE_HINT, austriaHints.showNextHint(2));
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testHint4(){
        try {
            assertEquals(LANDBOUNDARIES_HINT, austriaHints.showNextHint(3));
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testHint5(){
        try {
            assertEquals(CONTINENT_HINT, austriaHints.showNextHint(4));
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testHint6(){
        try {
            assertEquals(LOCATION_HINT, austriaHints.showNextHint(5));
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
    }
}
