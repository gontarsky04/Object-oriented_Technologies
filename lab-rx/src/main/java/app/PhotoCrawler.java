package app;

import io.reactivex.rxjava3.core.Observable;
import model.Photo;
import util.PhotoDownloader;
import util.PhotoProcessor;
import util.PhotoSerializer;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhotoCrawler {

    private static final Logger log = Logger.getLogger(PhotoCrawler.class.getName());

    private final PhotoDownloader photoDownloader;

    private final PhotoSerializer photoSerializer;

    private final PhotoProcessor photoProcessor;

    public PhotoCrawler() throws IOException {
        this.photoDownloader = new PhotoDownloader();
        this.photoSerializer = new PhotoSerializer("./photos");
        this.photoProcessor = new PhotoProcessor();
    }

    public void resetLibrary() throws IOException {
        photoSerializer.deleteLibraryContents();
    }

    public void downloadPhotoExamples() throws IOException {
        photoDownloader.getPhotoExamples()
            .subscribe(photo -> {
                photoSerializer.savePhoto(photo);
            });
    }

    public void downloadPhotosForQuery(String query) {
        photoDownloader.searchForPhotos(query)
            .map(photo -> {
                try {
                    return photoProcessor.convertToMiniature(photo);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Error converting photo to miniature", e);
                    return photo;
                }
            })
            .subscribe(
                photo -> {
                    photoSerializer.savePhoto(photo);
                },
                error -> log.log(Level.SEVERE, "Error downloading photos for query: " + query, error)
            );
    }

    public void downloadPhotosForMultipleQueries(List<String> queries) {
        Observable.fromIterable(queries)
            .flatMap(query -> photoDownloader.searchForPhotos(query))
            .map(photo -> {
                try {
                    return photoProcessor.convertToMiniature(photo);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Error converting photo to miniature", e);
                    return photo;
                }
            })
            .subscribe(
                photo -> {
                    photoSerializer.savePhoto(photo);
                },
                error -> log.log(Level.SEVERE, "Error downloading photos for multiple queries", error)
            );
    }
}
