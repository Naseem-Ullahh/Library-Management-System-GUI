package library.storage;

import library.model.Book;
import library.model.BookStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves books to a CSV text file and loads them back.
 *
 * <p>Each line of the file holds one book: {@code id,title,author,status}.
 * Fields that contain a comma or a quotation mark are wrapped in quotation
 * marks, and a quotation mark inside a field is written twice. The status is
 * stored by its constant name, for example {@code ISSUED}.</p>
 *
 * <p>This class only deals with files. The rules of the library stay in
 * {@link library.service.LibraryService}.</p>
 *
 * @author Naseem Ullah
 */
public class BookFileStore {

    private static final String HEADER = "id,title,author,status";

    private final Path file;

    /**
     * Creates a store that reads and writes the given file.
     *
     * @param file the CSV file; it does not have to exist yet
     */
    public BookFileStore(Path file) {
        this.file = file;
    }

    /**
     * Returns the file this store uses.
     *
     * @return the path of the CSV file
     */
    public Path getFile() {
        return file;
    }

    /**
     * Writes all given books to the file, replacing its old contents.
     *
     * <p>The data is first written to a temporary file next to the real one
     * and then moved over it, so a crash while saving cannot leave a
     * half-written file.</p>
     *
     * @param books the books to save
     * @throws IOException if the file cannot be written
     */
    public void save(List<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Book book : books) {
            lines.add(escape(book.getId()) + ","
                    + escape(book.getTitle()) + ","
                    + escape(book.getAuthor()) + ","
                    + book.getStatus().name());
        }

        Path absolute = file.toAbsolutePath();
        Path temporary = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        Files.write(temporary, lines, StandardCharsets.UTF_8);
        Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Reads the books from the file.
     *
     * <p>A missing file is not an error: it simply means nothing has been
     * saved yet. Lines that are damaged (wrong number of fields or an unknown
     * status) are skipped.</p>
     *
     * @return the books in the file; an empty list if the file does not exist
     * @throws IOException if the file exists but cannot be read
     */
    public List<Book> load() throws IOException {
        List<Book> books = new ArrayList<>();
        if (!Files.exists(file)) {
            return books;
        }

        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty() || line.equals(HEADER)) {
                continue;
            }
            List<String> fields = splitCsvLine(line);
            if (fields.size() != 4) {
                continue;
            }
            try {
                BookStatus status = BookStatus.valueOf(fields.get(3).trim());
                books.add(new Book(fields.get(0), fields.get(1), fields.get(2), status));
            } catch (IllegalArgumentException unknownStatus) {
                // Skip this line: the status is not Available, Issued or Reserved.
            }
        }
        return books;
    }

    /**
     * Prepares one value for the CSV file.
     *
     * @param value the text to write
     * @return the text, wrapped in quotation marks if it needs them
     */
    private static String escape(String value) {
        String clean = value.replace("\r", " ").replace("\n", " ");
        if (clean.contains(",") || clean.contains("\"")) {
            return "\"" + clean.replace("\"", "\"\"") + "\"";
        }
        return clean;
    }

    /**
     * Splits one CSV line into its fields, understanding quotation marks.
     *
     * @param line the line to split
     * @return the fields of the line, without their surrounding quotation marks
     */
    private static List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (insideQuotes) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    insideQuotes = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                insideQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
