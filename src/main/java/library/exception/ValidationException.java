package library.exception;

/**
 * Thrown when user input is not acceptable, for example an empty title,
 * an empty author or an empty book ID.
 *
 * @author Naseem, Amaan, Waseem
 */
public class ValidationException extends LibraryException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a validation error.
     *
     * @param message what is wrong with the input, for example
     *                {@code "Title cannot be empty."}
     */
    public ValidationException(String message) {
        super(message);
    }
}
