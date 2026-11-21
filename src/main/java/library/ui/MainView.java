package library.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import library.model.Book;
import library.model.BookStatus;
import library.service.LibraryService;

/**
 * The main window of the application.
 *
 * <p>The window has three parts: a form with buttons at the top, a table of
 * books in the centre and a status bar at the bottom. This class only builds
 * the screen and reacts to clicks; every rule (validation, duplicate check)
 * lives in {@link LibraryService}.</p>
 *
 * @author Muhammad Rehan Sabir
 */
public class MainView {

    private final LibraryService service;
    private final BorderPane root = new BorderPane();

    private final TextField idField = new TextField();
    private final TextField titleField = new TextField();
    private final TextField authorField = new TextField();
    private final ComboBox<BookStatus> statusBox = new ComboBox<>();

    private final Button addButton = new Button("Add");
    private final Button updateButton = new Button("Update");
    private final Button deleteButton = new Button("Delete");
    private final Button statusButton = new Button("Set status");
    private final Button clearButton = new Button("Clear");
    private final Button viewAllButton = new Button("View all");

    private final TableView<Book> table = new TableView<>();
    private final Label messageLabel = new Label();

    /**
     * Builds the window contents for the given library.
     *
     * @param service the library service that stores the books
     */
    public MainView(LibraryService service) {
        this.service = service;
        buildLayout();
        refreshTable();
        messageLabel.setText(describeLibrary());
    }

    /**
     * Returns the root node to put inside a {@code Scene}.
     *
     * @return the root layout of the window
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Arranges the form, the table and the status bar inside the window.
     */
    private void buildLayout() {
        Label heading = new Label("Library Book Management System");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox top = new VBox(12, heading, buildForm(), buildButtons());
        top.setPadding(new Insets(15));
        root.setTop(top);

        buildTable();
        BorderPane.setMargin(table, new Insets(0, 15, 0, 15));
        root.setCenter(table);

        HBox bottom = new HBox(messageLabel);
        bottom.setPadding(new Insets(10, 15, 10, 15));
        root.setBottom(bottom);
    }

    /**
     * Creates the input form (ID, title, author and status).
     *
     * @return the form layout
     */
    private GridPane buildForm() {
        idField.setPromptText("e.g. B001");
        titleField.setPromptText("Book title");
        authorField.setPromptText("Author name");
        idField.setPrefWidth(280);
        titleField.setPrefWidth(280);
        authorField.setPrefWidth(240);

        statusBox.getItems().addAll(BookStatus.values());
        statusBox.setValue(BookStatus.AVAILABLE);
        statusBox.setPrefWidth(240);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, new Label("Book ID:"), idField, new Label("Status:"), statusBox);
        form.addRow(1, new Label("Title:"), titleField, new Label("Author:"), authorField);
        return form;
    }

    /**
     * Creates the row of action buttons.
     *
     * @return the button row
     */
    private HBox buildButtons() {
        return new HBox(8, addButton, updateButton, deleteButton,
                statusButton, clearButton, viewAllButton);
    }

    /**
     * Sets up the table columns and the message shown when it has no rows.
     */
    private void buildTable() {
        TableColumn<Book, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        idColumn.setPrefWidth(110);

        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        titleColumn.setPrefWidth(320);

        TableColumn<Book, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAuthor()));
        authorColumn.setPrefWidth(220);

        TableColumn<Book, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().getLabel()));
        statusColumn.setPrefWidth(120);

        table.getColumns().add(idColumn);
        table.getColumns().add(titleColumn);
        table.getColumns().add(authorColumn);
        table.getColumns().add(statusColumn);
        table.setPlaceholder(new Label("No books in the library."));
    }

    /**
     * Reloads the table rows from the library service.
     */
    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(service.getAllBooks()));
    }

    /**
     * Describes how many books the library holds, for the status bar.
     *
     * @return a short sentence such as {@code "Showing 3 books."}
     */
    private String describeLibrary() {
        int count = service.getBookCount();
        if (count == 0) {
            return "The library is empty. Add a book to get started.";
        }
        return "Showing " + count + (count == 1 ? " book." : " books.");
    }
}
