package library.exception;

/**
 * Thrown when a book is added with an ID that already exists in the library.
 *
 * @author Muhammad Rehan Sabir
 */
public class DuplicateBookException extends LibraryException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a duplicate-ID error.
     *
     * @param id the ID that is already in use
     */
    public DuplicateBookException(String id) {
        super("A book with ID '" + id + "' already exists.");
    }
}
