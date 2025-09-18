package hints;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode; //Using Jackson to parse Json
import com.fasterxml.jackson.databind.ObjectMapper;


public class HintsClient {
    private final String region = "europe"; //To do: Change this into reference to a list of regions
    private final String gecCode = "gm"; //To do: This line of code should be deleted. Just a test.
    private final String BASE_URL = "https://raw.githubusercontent.com/factbook/factbook.json/master/"+region+"/"+gecCode+".json";
    // To do: Change it so the json file being parsed is different based on the target country
    // The naming structure of the json files are [region]/[GEC codes].json
    private final HttpClient client;

    public HintsClient() {
        client = HttpClient.newHttpClient();
    }

    public String findAllGermany() throws IOException, InterruptedException { //Currently, only calls from Germany's json file.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //return response.body();


        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        //String location = root
        return root
                .path("Geography")
                .path("Location")
                .path("text")
                .asText();
    }
}
