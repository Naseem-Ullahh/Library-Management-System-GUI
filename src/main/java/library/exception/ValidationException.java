package library.exception;

/**
 * Thrown when user input is not acceptable, for example an empty title,
 * an empty author or an empty book ID.
 *
 * <p>The exception also remembers which input field caused the problem, so the
 * user interface can highlight that field.</p>
 *
 * @author Naseem, Amaan, Waseem
 */
public class ValidationException extends LibraryException {

    private static final long serialVersionUID = 1L;

    /** Field name used when the book ID is the problem. */
    public static final String FIELD_ID = "Book ID";

    /** Field name used when the title is the problem. */
    public static final String FIELD_TITLE = "Title";

    /** Field name used when the author is the problem. */
    public static final String FIELD_AUTHOR = "Author";

    /** Field name used when the status is the problem. */
    public static final String FIELD_STATUS = "Status";

    /** The input field that caused this error; may be {@code null}. */
    private final String field;

    /**
     * Creates a validation error that is not tied to one field.
     *
     * @param message what is wrong with the input
     */
    public ValidationException(String message) {
        this(message, null);
    }

    /**
     * Creates a validation error for one input field.
     *
     * @param message what is wrong with the input, for example
     *                {@code "Title cannot be empty."}
     * @param field   the field that caused the problem, for example
     *                {@link #FIELD_TITLE}; may be {@code null}
     */
    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
    }

    /**
     * Returns the field that caused this error.
     *
     * @return one of the {@code FIELD_} constants, or {@code null} if the
     *         error is not tied to a single field
     */
    public String getField() {
        return field;
    }
}
