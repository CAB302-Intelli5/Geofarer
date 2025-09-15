package hints;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HintsClient {
    private final String BASE_URL = "https://raw.githubusercontent.com/factbook/factbook.json/master/europe/gm.json";
    private final HttpClient client;

    public HintsClient() {
        client = HttpClient.newHttpClient();
    }

    public String findAllGermany() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
