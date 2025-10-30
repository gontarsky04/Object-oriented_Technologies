package app;

import java.io.IOException;
import java.util.List;

public class CrawlerApp {

    public static final String GOOGLE_CUSTOM_SEARCH_API_KEY = "AIzaSyBqO3K_KCjtd_N0P3ODF0XwcVLl4CTOXGo";

    private static final List<String> TOPICS = List.of("Agent Cooper", "Sherlock", "Poirot", "Detective Monk");


    public static void main(String[] args) throws IOException {
        PhotoCrawler photoCrawler = new PhotoCrawler();
        photoCrawler.resetLibrary();
//        photoCrawler.downloadPhotoExamples();
        photoCrawler.downloadPhotosForQuery("bicycle");
        photoCrawler.downloadPhotosForMultipleQueries(TOPICS);
    }
}