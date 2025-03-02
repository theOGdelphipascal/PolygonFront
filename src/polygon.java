import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class polygon {

    // Attributes
    private String key;
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private int responseCode;
    private String responseMessage;

    public polygon(String key) {
        this.key = key;
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // returns the string associated with the object
    // AKA the user input
    public String getKey() {
        return key;
    }

    // Method to fetch and return all ticker values from JSON response
    public List<String> fetchAssets(String assetClass) throws Exception {

        String url = "https://api.polygon.io/v3/reference/tickers?market=" + assetClass + "&active=true&limit=1000&apiKey=" + this.key;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Java HttpClient")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the request was successful
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch data, HTTP Code: " + response.statusCode());
        }

        // Parse the JSON response
        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode resultsNode = rootNode.path("results");

        List<String> tickers = new ArrayList<>();
        if (resultsNode.isArray()) {
            for (JsonNode node : resultsNode) {
                tickers.add(node.path("ticker").asText());
            }
        }

        return tickers;
    }

    // Method to fetch and return tickers when provided with a search term
    public List<String> fetchAssetsSearch(String assetClass, String searchTerm) throws Exception {

        String url = "https://api.polygon.io/v3/reference/tickers?market=" + assetClass + "&search=" + searchTerm + "&active=true&limit=1000&apiKey=" + this.key;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Java HttpClient")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the request was successful
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch data, HTTP Code: " + response.statusCode());
        }

        // Parse the JSON response
        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode resultsNode = rootNode.path("results");

        List<String> tickers = new ArrayList<>();
        if (resultsNode.isArray()) {
            for (JsonNode node : resultsNode) {
                tickers.add(node.path("ticker").asText());
            }
        }

        return tickers;
    }

    // method to get candle data
    public List<Object[]> getData(String ticker, String freq, String from, String to) throws Exception {

        String apiUrl = "https://api.polygon.io/v2/aggs/ticker/" + ticker + "/range/1/" + freq + "/" + from
                + "/" + to + "?adjusted=true&sort=asc&apiKey=" + this.key;
        List<Object[]> stockData = new ArrayList<>();

        // Make API request
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Get response code and message
        this.responseCode = conn.getResponseCode();
        this.responseMessage = conn.getResponseMessage();

        // Read response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        // Parse JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        if (jsonResponse.has("results")) {
            JSONArray results = jsonResponse.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject stock = results.getJSONObject(i);

                // Convert timestamp
                long timestamp = stock.getLong("t");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String formattedDate = sdf.format(timestamp);

                // Extract stock values
                double open = stock.getDouble("o");
                double high = stock.getDouble("h");
                double low = stock.getDouble("l");
                double close = stock.getDouble("c");
                long volume = stock.getLong("v");
                long weightedVolume = stock.getLong("vw");

                // Store row data
                stockData.add(new Object[]{formattedDate, open, high, low, close, volume, weightedVolume});
            }
        }
        return stockData;
    }

    // method to return latest response code
    public int getResponseCode() {
        return this.responseCode;
    }

    // method to return response method
    public String getResponseMessage() {
        return this.responseMessage;
    }
}
