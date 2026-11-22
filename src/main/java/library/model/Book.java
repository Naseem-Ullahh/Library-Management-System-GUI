package library.model;

/**
 * Represents one book in the library.
 *
 * <p>A book has a unique ID that never changes, a title, an author and a
 * {@link BookStatus}. This class only stores data; all rules (validation,
 * duplicate checks) are enforced by
 * {@link library.service.LibraryService}.</p>
 *
 * @author Naseem, Amaan, Waseem
 */
public class Book {

    private final String id;
    private String title;
    private String author;
    private BookStatus status;

    /**
     * Creates a new book that is {@link BookStatus#AVAILABLE}.
     *
     * @param id     the unique book ID
     * @param title  the book title
     * @param author the author's name
     */
    public Book(String id, String title, String author) {
        this(id, title, author, BookStatus.AVAILABLE);
    }

    /**
     * Creates a new book with a given status.
     *
     * @param id     the unique book ID
     * @param title  the book title
     * @param author the author's name
     * @param status the initial status of the book
     */
    public Book(String id, String title, String author, BookStatus status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
    }

    /**
     * Returns the unique ID of this book.
     *
     * @return the book ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the title of this book.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Changes the title of this book.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the author of this book.
     *
     * @return the author's name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Changes the author of this book.
     *
     * @param author the new author's name
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the current status of this book.
     *
     * @return the status
     */
    public BookStatus getStatus() {
        return status;
    }

    /**
     * Changes the status of this book.
     *
     * @param status the new status
     */
    public void setStatus(BookStatus status) {
        this.status = status;
    }

    /**
     * Returns a one-line description of the book, useful for debugging.
     *
     * @return text such as {@code B001 | Clean Code | Robert Martin | Available}
     */
    @Override
    public String toString() {
        return id + " | " + title + " | " + author + " | " + status;
    }
}
