package driver;

import app.CrawlerApp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.StreamSupport;

public class GoogleSearchDriver {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    public static List<String> searchForImages(String query) throws IOException, InterruptedException {
        var htmlQuery = query.replace(' ', '+');

        var searchSource = downloadUrlSource(htmlQuery);
        return extractPhotoUrls(searchSource);
    }

    private static String downloadUrlSource(String searchQuery) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        HttpResponse<String> response = sendRequest(client, getGoogleSearchUri(searchQuery));
        return response.body();
    }

    private static URI getGoogleSearchUri(String searchQuery) {
        var queryRequest = String.format("https://customsearch.googleapis.com/customsearch/v1?key=%s&cx=b09e01979881f4e9c&searchType=image&q=%s", CrawlerApp.GOOGLE_CUSTOM_SEARCH_API_KEY, searchQuery);
        return URI.create(queryRequest);
    }

    private static HttpResponse<String> sendRequest(HttpClient client, URI uri) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        return client.send(request,
                HttpResponse.BodyHandlers.ofString());
    }

    private static List<String> extractPhotoUrls(String responseJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode items = rootNode.get("items");

        if (items == null) {
            throw new IllegalArgumentException("Invalid response format:\n" + responseJson);
        }

        return StreamSupport.stream(items.spliterator(), false)
                .map(itemNode -> itemNode.get("link").asText())
                .toList();
    }
}
