package library.service;

import library.exception.BookNotFoundException;
import library.exception.DuplicateBookException;
import library.exception.ValidationException;
import library.model.Book;
import library.model.BookStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all books and enforces the rules of the library.
 *
 * <p>Books are stored in a {@link LinkedHashMap} keyed by book ID. A map
 * makes the duplicate-ID check and every lookup O(1), and it keeps the books
 * in the order they were added. The user interface never touches the map
 * directly; it only calls the methods of this class.</p>
 *
 * @author Naseem, Amaan, Waseem
 */
public class LibraryService {

    private final Map<String, Book> books = new LinkedHashMap<>();

    /**
     * Creates an empty library with no books.
     */
    public LibraryService() {
        // The books map is created in its field declaration.
    }

    /**
     * Adds a new book after validating the input.
     *
     * <p>Leading and trailing spaces are removed from every field. The new
     * book starts as {@code AVAILABLE}.</p>
     *
     * @param id     the unique book ID
     * @param title  the book title
     * @param author the author's name
     * @return the book that was added
     * @throws ValidationException    if the ID, title or author is empty
     * @throws DuplicateBookException if a book with this ID already exists
     */
    public Book addBook(String id, String title, String author)
            throws ValidationException, DuplicateBookException {
        String cleanId = requireText(id, "Book ID");
        String cleanTitle = requireText(title, "Title");
        String cleanAuthor = requireText(author, "Author");

        if (books.containsKey(cleanId)) {
            throw new DuplicateBookException(cleanId);
        }

        Book book = new Book(cleanId, cleanTitle, cleanAuthor);
        books.put(cleanId, book);
        return book;
    }

    /**
     * Returns every book in the order it was added.
     *
     * <p>The returned list is a copy, so changing it does not change the
     * library.</p>
     *
     * @return a new list of all books; empty if the library has no books
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    /**
     * Returns the number of books in the library.
     *
     * @return the book count
     */
    public int getBookCount() {
        return books.size();
    }

    /**
     * Finds a book by its ID.
     *
     * @param id the book ID to look for
     * @return the matching book
     * @throws ValidationException   if the ID is empty
     * @throws BookNotFoundException if no book has this ID
     */
    public Book findBook(String id) throws ValidationException, BookNotFoundException {
        String cleanId = requireText(id, "Book ID");
        Book book = books.get(cleanId);
        if (book == null) {
            throw new BookNotFoundException(cleanId);
        }
        return book;
    }

    /**
     * Changes the title and author of an existing book.
     *
     * <p>All input is validated before anything is changed, so a failed
     * update never leaves a book half-modified. The ID and the status are not
     * changed by this method.</p>
     *
     * @param id        the ID of the book to update
     * @param newTitle  the new title
     * @param newAuthor the new author
     * @return the updated book
     * @throws ValidationException   if the ID, title or author is empty
     * @throws BookNotFoundException if no book has this ID
     */
    public Book updateBook(String id, String newTitle, String newAuthor)
            throws ValidationException, BookNotFoundException {
        String cleanTitle = requireText(newTitle, "Title");
        String cleanAuthor = requireText(newAuthor, "Author");
        Book book = findBook(id);

        book.setTitle(cleanTitle);
        book.setAuthor(cleanAuthor);
        return book;
    }

    /**
     * Removes a book from the library.
     *
     * @param id the ID of the book to delete
     * @return the book that was removed
     * @throws ValidationException   if the ID is empty
     * @throws BookNotFoundException if no book has this ID
     */
    public Book deleteBook(String id) throws ValidationException, BookNotFoundException {
        Book book = findBook(id);
        books.remove(book.getId());
        return book;
    }

    /**
     * Sets the status of an existing book to Available, Issued or Reserved.
     *
     * @param id     the ID of the book
     * @param status the new status
     * @return the updated book
     * @throws ValidationException   if the ID is empty or the status is null
     * @throws BookNotFoundException if no book has this ID
     */
    public Book setStatus(String id, BookStatus status)
            throws ValidationException, BookNotFoundException {
        if (status == null) {
            throw new ValidationException("Please choose a status.");
        }
        Book book = findBook(id);
        book.setStatus(status);
        return book;
    }

    /**
     * Checks that a text field is not empty and returns it without
     * surrounding spaces.
     *
     * @param value     the text to check
     * @param fieldName the field name used in the error message
     * @return the trimmed text
     * @throws ValidationException if the text is null, empty or only spaces
     */
    private String requireText(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }
}
