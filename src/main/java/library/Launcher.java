package library;

/**
 * Starts the application from an IDE.
 *
 * <p>When the class that contains {@code main} extends
 * {@code javafx.application.Application}, some IDEs fail with the error
 * "JavaFX runtime components are missing". This small class does not extend
 * {@code Application}, so it can be run directly (for example with the Run
 * button in Visual Studio Code) and simply hands over to {@link Main}.</p>
 *
 * @author Naseem, Amaan, Waseem
 */
public class Launcher {

    /**
     * Creates a launcher. The class is only used through its
     * {@link #main(String[])} method.
     */
    public Launcher() {
        super();
    }

    /**
     * Starts the application by calling {@link Main#main(String[])}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
