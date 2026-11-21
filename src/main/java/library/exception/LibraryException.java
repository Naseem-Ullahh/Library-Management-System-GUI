package library.exception;

/**
 * Base class for every error the library system reports to the user.
 *
 * <p>It is a checked exception, so callers are forced to handle it. The user
 * interface catches it and shows {@link #getMessage()} in an error dialog.</p>
 *
 * @author Muhammad Rehan Sabir
 */
public class LibraryException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message that is safe to show to the user.
     *
     * @param message a short description of what went wrong
     */
    public LibraryException(String message) {
        super(message);
    }
}
