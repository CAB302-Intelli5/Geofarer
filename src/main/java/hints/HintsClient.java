package hints;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;

import com.fasterxml.jackson.databind.JsonNode; //Using Jackson to parse Json
import com.fasterxml.jackson.databind.ObjectMapper;


public class HintsClient {
    private final String region = "europe"; //To do: Change this into reference to a list of regions
    private final String gecCode = "gm"; //To do: This line of code should be deleted. Just a test.
    private final String BASE_URL = "https://raw.githubusercontent.com/factbook/factbook.json/master/"+region+"/"+gecCode+".json";
    private final String dbPath = "src/main/resources/factbook.db"; // Path to the SQLite database

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
    public String getHint(String gecCode) {
        String query = "SELECT data FROM factbook WHERE gec = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, gecCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("data");
            } else {
                return "No hint available for " + gecCode;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error fetching hint.";
        }
    }
}
