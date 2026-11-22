package library.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import library.exception.LibraryException;
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
 * @author Naseem, Amaan, Waseem
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
        connectActions();
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
     * Connects the buttons and the table selection to their handlers.
     */
    private void connectActions() {
        addButton.setOnAction(event -> onAdd());
        updateButton.setOnAction(event -> onUpdate());
        deleteButton.setOnAction(event -> onDelete());
        statusButton.setOnAction(event -> onSetStatus());
        clearButton.setOnAction(event -> clearForm());
        viewAllButton.setOnAction(event -> onViewAll());

        // Clicking a row copies that book into the form.
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldBook, newBook) -> {
            if (newBook != null) {
                idField.setText(newBook.getId());
                titleField.setText(newBook.getTitle());
                authorField.setText(newBook.getAuthor());
                statusBox.setValue(newBook.getStatus());
            }
        });
    }

    /**
     * Handles the Add button: adds the book typed in the form.
     */
    private void onAdd() {
        try {
            Book book = service.addBook(idField.getText(), titleField.getText(), authorField.getText());
            refreshTable();
            clearForm();
            messageLabel.setText("Added \"" + book.getTitle() + "\" (" + book.getId() + "). "
                    + describeLibrary());
        } catch (LibraryException error) {
            showError(error);
        }
    }

    /**
     * Handles the Update button: changes the title and author of the book
     * whose ID is typed in the form.
     */
    private void onUpdate() {
        try {
            Book book = service.updateBook(idField.getText(), titleField.getText(), authorField.getText());
            refreshTable();
            messageLabel.setText("Updated book " + book.getId() + ".");
        } catch (LibraryException error) {
            showError(error);
        }
    }

    /**
     * Handles the Delete button: asks for confirmation, then removes the
     * book whose ID is typed in the form.
     */
    private void onDelete() {
        try {
            Book book = service.findBook(idField.getText());

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete \"" + book.getTitle() + "\" (" + book.getId() + ")?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm delete");
            confirm.setHeaderText(null);

            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                service.deleteBook(book.getId());
                refreshTable();
                clearForm();
                messageLabel.setText("Deleted book " + book.getId() + ". " + describeLibrary());
            }
        } catch (LibraryException error) {
            showError(error);
        }
    }

    /**
     * Handles the Set status button: gives the book whose ID is typed in the
     * form the status chosen in the combo box.
     */
    private void onSetStatus() {
        try {
            Book book = service.setStatus(idField.getText(), statusBox.getValue());
            refreshTable();
            messageLabel.setText("Book " + book.getId() + " is now " + book.getStatus() + ".");
        } catch (LibraryException error) {
            showError(error);
        }
    }

    /**
     * Handles the View all button: reloads the table and, if the library has
     * no books, tells the user so.
     */
    private void onViewAll() {
        refreshTable();
        messageLabel.setText(describeLibrary());
        if (service.getBookCount() == 0) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, "The library is empty. There are no books to show.");
            info.setTitle("Empty library");
            info.setHeaderText(null);
            info.showAndWait();
        }
    }

    /**
     * Empties the form fields and resets the status choice.
     */
    private void clearForm() {
        idField.clear();
        titleField.clear();
        authorField.clear();
        statusBox.setValue(BookStatus.AVAILABLE);
        table.getSelectionModel().clearSelection();
    }

    /**
     * Shows an error dialog with the message of the given exception.
     *
     * @param error the problem to show to the user
     */
    private void showError(LibraryException error) {
        Alert alert = new Alert(Alert.AlertType.ERROR, error.getMessage());
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
        messageLabel.setText("Error: " + error.getMessage());
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
