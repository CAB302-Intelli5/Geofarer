package hints;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode; //Using Jackson to parse Json
import com.fasterxml.jackson.databind.ObjectMapper;


public class HintsClient {

    private String BASE_URL;
    // To do: Change it so the json file being parsed is different based on the target country
    // The naming structure of the json files are [region]/[GEC codes].json
    private final HttpClient client;

    public HintsClient(String region, String gecCode) {
        this.BASE_URL = "https://raw.githubusercontent.com/factbook/factbook.json/master/"+region+"/"+gecCode+".json";
        client = HttpClient.newHttpClient();
    }

    public String findAll() throws IOException, InterruptedException { //Currently, only calls Germany's json file.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //return response.body();


        ObjectMapper mapper = new ObjectMapper(); //Using Jackson to parse JSON response body
        JsonNode root = mapper.readTree(response.body());

        String location = root
                .path("Geography")
                .path("Location")
                .path("text")
                .asText();
        String economicOverview = root
                .path("Economy")
                .path("Economic overview")
                .path("text")
                .asText();

        String climate = root
                .path("Environment")
                .path("Climate")
                .path("text")
                .asText();

        String terrain = root
                .asText();

        String borderCountries = root
                .asText();

        String obesity = root
                .asText();

        String governmentType = root
                .asText();

        String currency = root
                .asText();

        String urbanization = root
                .asText();

        return location;
    }
}
