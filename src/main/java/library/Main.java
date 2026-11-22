package library;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import library.service.LibraryService;
import library.storage.BookFileStore;
import library.ui.MainView;

import java.net.URL;
import java.nio.file.Paths;

/**
 * JavaFX entry point of the Library Book Management System.
 *
 * <p>It creates the {@link LibraryService} that holds the books and the
 * {@link BookFileStore} that saves them to {@code library-data.csv}, builds
 * the main window around them, applies the stylesheet and shows it.</p>
 *
 * @author Naseem Ullah
 */
public class Main extends Application {

    /**
     * Creates the application object. JavaFX calls this constructor itself
     * when {@link #main(String[])} runs.
     */
    public Main() {
        super();
    }

    /**
     * Builds and shows the main window. Called by the JavaFX runtime.
     *
     * @param stage the primary window provided by JavaFX
     */
    @Override
    public void start(Stage stage) {
        LibraryService service = new LibraryService();
        BookFileStore store = new BookFileStore(Paths.get("library-data.csv"));
        MainView view = new MainView(service, store);

        Scene scene = new Scene(view.getRoot(), 1100, 700);
        URL stylesheet = getClass().getResource("/styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        stage.setTitle("Library Book Management System");
        stage.setMinWidth(960);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Starts the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
