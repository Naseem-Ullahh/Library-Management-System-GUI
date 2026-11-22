package library;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import library.service.LibraryService;
import library.ui.MainView;

/**
 * JavaFX entry point of the Library Book Management System.
 *
 * <p>It creates the {@link LibraryService} that holds the books, builds the
 * main window around it and shows it.</p>
 *
 * @author Naseem, Amaan, Waseem
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
        MainView view = new MainView(service);

        stage.setTitle("Library Book Management System");
        stage.setScene(new Scene(view.getRoot(), 900, 600));
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
