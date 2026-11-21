package library.model;

/**
 * The possible states of a library book.
 *
 * <p>Each constant carries a human-readable label that is shown in the
 * user interface.</p>
 *
 * @author Muhammad Rehan Sabir
 */
public enum BookStatus {

    /** The book is on the shelf and can be borrowed. */
    AVAILABLE("Available"),

    /** The book has been issued to a member. */
    ISSUED("Issued"),

    /** The book is reserved for a member. */
    RESERVED("Reserved");

    private final String label;

    /**
     * Creates a status constant with its display label.
     *
     * @param label the text shown to the user for this status
     */
    BookStatus(String label) {
        this.label = label;
    }

    /**
     * Returns the display label of this status.
     *
     * @return the label, for example {@code "Available"}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the display label so the status prints nicely in combo boxes.
     *
     * @return the same value as {@link #getLabel()}
     */
    @Override
    public String toString() {
        return label;
    }
}
