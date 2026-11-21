package library.service;

import library.exception.BookNotFoundException;
import library.exception.DuplicateBookException;
import library.exception.ValidationException;
import library.model.Book;
import library.model.BookStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link LibraryService}.
 *
 * <p>Tests {@code tc1} to {@code tc8} match the eight test cases listed in
 * the OEL brief. The remaining tests cover extra edge cases.</p>
 *
 * @author Muhammad Rehan Sabir
 */
public class LibraryServiceTest {

    private LibraryService service;

    /**
     * Gives every test a fresh, empty library.
     */
    @Before
    public void setUp() {
        service = new LibraryService();
    }

    // ---------- The eight OEL test cases ----------

    /** TC1: adding a valid book stores it as Available. */
    @Test
    public void tc1_addValidBook() throws Exception {
        Book added = service.addBook("B001", "Clean Code", "Robert Martin");

        assertEquals("B001", added.getId());
        assertEquals("Clean Code", added.getTitle());
        assertEquals("Robert Martin", added.getAuthor());
        assertEquals(BookStatus.AVAILABLE, added.getStatus());
        assertEquals(1, service.getBookCount());
    }

    /** TC2: a book with an empty title is rejected with a message. */
    @Test
    public void tc2_addBookWithEmptyTitle() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.addBook("B001", "", "Robert Martin"));

        assertEquals("Title cannot be empty.", ex.getMessage());
        assertEquals(0, service.getBookCount());
    }

    /** TC3: adding a second book with the same ID is rejected. */
    @Test
    public void tc3_addBookWithDuplicateId() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");

        DuplicateBookException ex = assertThrows(DuplicateBookException.class,
                () -> service.addBook("B001", "Other Title", "Other Author"));

        assertEquals("A book with ID 'B001' already exists.", ex.getMessage());
        assertEquals(1, service.getBookCount());
        assertEquals("Clean Code", service.findBook("B001").getTitle());
    }

    /** TC4: viewing all books returns every book in the order added. */
    @Test
    public void tc4_viewAllBooks() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");
        service.addBook("B002", "Effective Java", "Joshua Bloch");
        service.addBook("B003", "Head First Java", "Kathy Sierra");

        List<Book> all = service.getAllBooks();

        assertEquals(3, all.size());
        assertEquals("B001", all.get(0).getId());
        assertEquals("B002", all.get(1).getId());
        assertEquals("B003", all.get(2).getId());
    }

    /** TC5: an empty library returns an empty list (the UI shows a message). */
    @Test
    public void tc5_viewEmptyLibrary() {
        assertTrue(service.getAllBooks().isEmpty());
        assertEquals(0, service.getBookCount());
    }

    /** TC6: updating an existing book changes its title and author. */
    @Test
    public void tc6_updateExistingBook() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");

        Book updated = service.updateBook("B001", "Clean Architecture", "Robert C. Martin");

        assertEquals("Clean Architecture", updated.getTitle());
        assertEquals("Robert C. Martin", updated.getAuthor());
        assertEquals("Clean Architecture", service.findBook("B001").getTitle());
    }

    /** TC7: updating an ID that does not exist is handled with an exception. */
    @Test
    public void tc7_updateNonExistingBook() {
        BookNotFoundException ex = assertThrows(BookNotFoundException.class,
                () -> service.updateBook("B999", "Some Title", "Some Author"));

        assertEquals("No book found with ID 'B999'.", ex.getMessage());
        assertEquals(0, service.getBookCount());
    }

    /** TC8: deleting an existing book removes it. */
    @Test
    public void tc8_deleteExistingBook() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");
        service.addBook("B002", "Effective Java", "Joshua Bloch");

        service.deleteBook("B001");

        assertEquals(1, service.getBookCount());
        assertThrows(BookNotFoundException.class, () -> service.findBook("B001"));
        assertNotNull(service.findBook("B002"));
    }

    // ---------- Extra edge cases ----------

    /** An empty author is rejected. */
    @Test
    public void addBookWithEmptyAuthor() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.addBook("B001", "Clean Code", ""));

        assertEquals("Author cannot be empty.", ex.getMessage());
    }

    /** An empty ID is rejected. */
    @Test
    public void addBookWithEmptyId() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.addBook("", "Clean Code", "Robert Martin"));

        assertEquals("Book ID cannot be empty.", ex.getMessage());
    }

    /** Input made only of spaces counts as empty, and null is rejected too. */
    @Test
    public void whitespaceOnlyAndNullAreRejected() {
        assertThrows(ValidationException.class,
                () -> service.addBook("B001", "   ", "Robert Martin"));
        assertThrows(ValidationException.class,
                () -> service.addBook("B001", "Clean Code", null));
        assertEquals(0, service.getBookCount());
    }

    /** Surrounding spaces are removed, so " B001 " and "B001" are the same ID. */
    @Test
    public void inputIsTrimmedAndTrimmedIdsCollide() throws Exception {
        Book added = service.addBook("  B001  ", "  Clean Code ", " Robert Martin ");

        assertEquals("B001", added.getId());
        assertEquals("Clean Code", added.getTitle());
        assertEquals("Robert Martin", added.getAuthor());
        assertThrows(DuplicateBookException.class,
                () -> service.addBook("B001", "Another", "Someone"));
    }

    /** A failed update (empty title) leaves the stored book unchanged. */
    @Test
    public void failedUpdateDoesNotChangeBook() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");

        assertThrows(ValidationException.class,
                () -> service.updateBook("B001", "", "New Author"));

        Book stored = service.findBook("B001");
        assertEquals("Clean Code", stored.getTitle());
        assertEquals("Robert Martin", stored.getAuthor());
    }

    /** Deleting an ID that does not exist is handled with an exception. */
    @Test
    public void deleteNonExistingBook() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");

        assertThrows(BookNotFoundException.class, () -> service.deleteBook("B404"));
        assertEquals(1, service.getBookCount());
    }

    /** The status can be changed to Issued, then Reserved, then Available. */
    @Test
    public void setStatusChangesStatus() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");

        assertEquals(BookStatus.ISSUED, service.setStatus("B001", BookStatus.ISSUED).getStatus());
        assertEquals(BookStatus.RESERVED, service.setStatus("B001", BookStatus.RESERVED).getStatus());
        assertEquals(BookStatus.AVAILABLE, service.setStatus("B001", BookStatus.AVAILABLE).getStatus());
    }

    /** Setting the status of a missing book, or with no status, is rejected. */
    @Test
    public void setStatusErrors() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");

        assertThrows(BookNotFoundException.class,
                () -> service.setStatus("B999", BookStatus.ISSUED));
        assertThrows(ValidationException.class,
                () -> service.setStatus("B001", null));
        assertEquals(BookStatus.AVAILABLE, service.findBook("B001").getStatus());
    }

    /** Changing the list returned by getAllBooks does not change the library. */
    @Test
    public void getAllBooksReturnsACopy() throws Exception {
        service.addBook("B001", "Clean Code", "Robert Martin");

        service.getAllBooks().clear();

        assertEquals(1, service.getBookCount());
    }

    /** findBook returns the very same stored object. */
    @Test
    public void findBookReturnsStoredBook() throws Exception {
        Book added = service.addBook("B001", "Clean Code", "Robert Martin");

        assertSame(added, service.findBook("B001"));
    }
}
