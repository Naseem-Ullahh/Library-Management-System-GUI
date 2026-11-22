package library.storage;

import library.model.Book;
import library.model.BookStatus;
import library.service.LibraryService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link BookFileStore}.
 *
 * @author Muhammad Rehan Sabir
 */
public class BookFileStoreTest {

    /** A temporary folder that JUnit deletes after every test. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path file;
    private BookFileStore store;

    /**
     * Prepares a store that points to a file inside the temporary folder.
     */
    @Before
    public void setUp() {
        file = new File(folder.getRoot(), "books.csv").toPath();
        store = new BookFileStore(file);
    }

    /** Books that are saved can be loaded back unchanged. */
    @Test
    public void saveThenLoadReturnsTheSameBooks() throws Exception {
        List<Book> books = Arrays.asList(
                new Book("B001", "Clean Code", "Robert Martin"),
                new Book("B002", "Effective Java", "Joshua Bloch", BookStatus.ISSUED),
                new Book("B003", "Head First Java", "Kathy Sierra", BookStatus.RESERVED));

        store.save(books);
        List<Book> loaded = store.load();

        assertEquals(3, loaded.size());
        for (int i = 0; i < books.size(); i++) {
            assertEquals(books.get(i).getId(), loaded.get(i).getId());
            assertEquals(books.get(i).getTitle(), loaded.get(i).getTitle());
            assertEquals(books.get(i).getAuthor(), loaded.get(i).getAuthor());
            assertEquals(books.get(i).getStatus(), loaded.get(i).getStatus());
        }
    }

    /** Commas and quotation marks inside a field survive saving and loading. */
    @Test
    public void commasAndQuotesAreEscaped() throws Exception {
        Book tricky = new Book("B001", "Hello, \"World\"", "Smith, John", BookStatus.AVAILABLE);

        store.save(Arrays.asList(tricky));
        List<Book> loaded = store.load();

        assertEquals(1, loaded.size());
        assertEquals("Hello, \"World\"", loaded.get(0).getTitle());
        assertEquals("Smith, John", loaded.get(0).getAuthor());
    }

    /** A file that does not exist yet loads as an empty list. */
    @Test
    public void missingFileLoadsAsEmptyList() throws Exception {
        assertFalse(Files.exists(file));

        assertTrue(store.load().isEmpty());
    }

    /** Saving an empty list creates a file that loads as an empty list. */
    @Test
    public void emptyLibraryRoundTrip() throws Exception {
        store.save(Arrays.<Book>asList());

        assertTrue(Files.exists(file));
        assertTrue(store.load().isEmpty());
    }

    /** Saving again replaces the old contents instead of adding to them. */
    @Test
    public void saveReplacesOldContents() throws Exception {
        store.save(Arrays.asList(new Book("B001", "Clean Code", "Robert Martin"),
                new Book("B002", "Effective Java", "Joshua Bloch")));
        store.save(Arrays.asList(new Book("B009", "Only Book", "Only Author")));

        List<Book> loaded = store.load();

        assertEquals(1, loaded.size());
        assertEquals("B009", loaded.get(0).getId());
    }

    /** Damaged lines are skipped while good lines are still loaded. */
    @Test
    public void damagedLinesAreSkipped() throws Exception {
        Files.write(file, Arrays.asList(
                "id,title,author,status",
                "B001,Clean Code,Robert Martin,AVAILABLE",
                "this line is not a book",
                "B002,Effective Java,Joshua Bloch,NOT_A_STATUS",
                "",
                "B003,Head First Java,Kathy Sierra,RESERVED"), StandardCharsets.UTF_8);

        List<Book> loaded = store.load();

        assertEquals(2, loaded.size());
        assertEquals("B001", loaded.get(0).getId());
        assertEquals("B003", loaded.get(1).getId());
        assertEquals(BookStatus.RESERVED, loaded.get(1).getStatus());
    }

    /** The file starts with a header line and stores the status by name. */
    @Test
    public void fileHasHeaderAndStatusName() throws Exception {
        store.save(Arrays.asList(new Book("B001", "Clean Code", "Robert Martin", BookStatus.ISSUED)));

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        assertEquals("id,title,author,status", lines.get(0));
        assertEquals("B001,Clean Code,Robert Martin,ISSUED", lines.get(1));
    }

    /** A library that is saved can be restored by a brand-new library (restart). */
    @Test
    public void savedLibraryIsRestoredByNewService() throws Exception {
        LibraryService first = new LibraryService();
        first.addBook("B001", "Clean Code", "Robert Martin");
        first.addBook("B002", "Effective Java", "Joshua Bloch");
        first.setStatus("B002", BookStatus.ISSUED);
        store.save(first.getAllBooks());

        LibraryService second = new LibraryService();
        second.loadBooks(store.load());

        assertEquals(2, second.getBookCount());
        assertEquals("Clean Code", second.findBook("B001").getTitle());
        assertEquals(BookStatus.ISSUED, second.findBook("B002").getStatus());
        assertEquals(1, second.countByStatus(BookStatus.AVAILABLE));
    }

    /** No temporary file is left behind after saving. */
    @Test
    public void noTemporaryFileRemains() throws Exception {
        store.save(Arrays.asList(new Book("B001", "Clean Code", "Robert Martin")));

        assertFalse(Files.exists(file.resolveSibling("books.csv.tmp")));
    }
}
