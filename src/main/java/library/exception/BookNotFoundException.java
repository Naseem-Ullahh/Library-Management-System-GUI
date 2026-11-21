package library.exception;

/**
 * Thrown when an operation refers to a book ID that is not in the library,
 * for example updating or deleting a book that does not exist.
 *
 * @author Muhammad Rehan Sabir
 */
public class BookNotFoundException extends LibraryException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a book-not-found error.
     *
     * @param id the ID that could not be found
     */
    public BookNotFoundException(String id) {
        super("No book found with ID '" + id + "'.");
    }
}
