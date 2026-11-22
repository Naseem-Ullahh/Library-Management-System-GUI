package library.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import library.exception.LibraryException;
import library.exception.ValidationException;
import library.model.Book;
import library.model.BookStatus;
import library.service.LibraryService;
import library.storage.BookFileStore;

import java.io.IOException;
import java.util.List;

/**
 * The main window of the application (the front end).
 *
 * <p>The window has four parts: a header, four summary cards (total,
 * available, issued, reserved), a form on the left and a searchable table of
 * books on the right, plus a status bar at the bottom. Colours and fonts come
 * from {@code styles.css}.</p>
 *
 * <p>This class only builds the screen and reacts to clicks. Every rule
 * (validation, duplicate check, search) lives in {@link LibraryService}.</p>
 *
 * @author Muhammad Rehan Sabir
 */
public class MainView {

    private static final String ALL_STATUSES = "All statuses";
    private static final String ERROR_STYLE = "field-error";

    private final LibraryService service;
    private final BookFileStore store;
    private final BorderPane root = new BorderPane();

    private final TextField idField = new TextField();
    private final TextField titleField = new TextField();
    private final TextField authorField = new TextField();
    private final ComboBox<BookStatus> statusBox = new ComboBox<>();

    private final Button addButton = new Button("Add book");
    private final Button updateButton = new Button("Update");
    private final Button statusButton = new Button("Set status");
    private final Button deleteButton = new Button("Delete");
    private final Button clearButton = new Button("Clear");
    private final Button viewAllButton = new Button("View all");

    private final TextField searchField = new TextField();
    private final ComboBox<String> filterBox = new ComboBox<>();

    private final Label totalValue = new Label("0");
    private final Label availableValue = new Label("0");
    private final Label issuedValue = new Label("0");
    private final Label reservedValue = new Label("0");

    private final TableView<Book> table = new TableView<>();
    private final Label formErrorLabel = new Label();
    private final Label messageLabel = new Label();
    private final Label saveLabel = new Label();

    /**
     * Builds the window contents for the given library and loads the books
     * that were saved earlier.
     *
     * @param service the library service that stores the books
     * @param store   the file the books are saved to and loaded from
     */
    public MainView(LibraryService service, BookFileStore store) {
        this.service = service;
        this.store = store;
        buildLayout();
        connectActions();
        loadSavedBooks();
        refreshAll();
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

    // ------------------------------------------------------------------
    // Building the screen
    // ------------------------------------------------------------------

    /**
     * Puts the header, the content area and the status bar into the window.
     */
    private void buildLayout() {
        root.getStyleClass().add("root-pane");
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        root.setBottom(buildStatusBar());
    }

    /**
     * Creates the blue header with the application name.
     *
     * @return the header
     */
    private VBox buildHeader() {
        Label title = new Label("Library Book Management System");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Add, update, search and track the status of every book");
        subtitle.getStyleClass().add("app-subtitle");

        VBox header = new VBox(2, title, subtitle);
        header.getStyleClass().add("header");
        return header;
    }

    /**
     * Creates the area under the header: summary cards above the form and table.
     *
     * @return the content area
     */
    private VBox buildContent() {
        HBox cards = new HBox(14,
                createCard("Total books", totalValue, "card-total"),
                createCard("Available", availableValue, "card-available"),
                createCard("Issued", issuedValue, "card-issued"),
                createCard("Reserved", reservedValue, "card-reserved"));

        HBox main = new HBox(16, buildFormPanel(), buildTablePanel());
        VBox.setVgrow(main, Priority.ALWAYS);

        VBox content = new VBox(16, cards, main);
        content.setPadding(new Insets(18, 24, 18, 24));
        return content;
    }

    /**
     * Creates one summary card: a big number with a caption below it.
     *
     * @param caption    the text under the number, for example {@code "Issued"}
     * @param valueLabel the label that shows the number
     * @param styleClass the CSS class that gives the number its colour
     * @return the card
     */
    private VBox createCard(String caption, Label valueLabel, String styleClass) {
        valueLabel.getStyleClass().add("stat-value");
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("stat-caption");

        VBox card = new VBox(2, valueLabel, captionLabel);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    /**
     * Creates the panel on the left with the book form and the action buttons.
     *
     * @return the form panel
     */
    private VBox buildFormPanel() {
        Label title = new Label("Book details");
        title.getStyleClass().add("panel-title");

        idField.setPromptText("e.g. B001");
        titleField.setPromptText("Book title");
        authorField.setPromptText("Author name");

        statusBox.getItems().addAll(BookStatus.values());
        statusBox.setValue(BookStatus.AVAILABLE);
        statusBox.setMaxWidth(Double.MAX_VALUE);

        addButton.getStyleClass().add("button-primary");
        deleteButton.getStyleClass().add("button-danger");
        HBox mainButtons = new HBox(8, addButton, updateButton, statusButton);
        HBox otherButtons = new HBox(8, deleteButton, clearButton);

        Label hint = new Label("Click a row in the table to load it here. "
                + "Update, Set status and Delete work on the Book ID.");
        hint.getStyleClass().add("hint-label");
        hint.setWrapText(true);

        formErrorLabel.getStyleClass().add("form-error");
        formErrorLabel.setWrapText(true);
        hideFormError();

        VBox panel = new VBox(12, title,
                fieldGroup("Book ID", idField),
                fieldGroup("Title", titleField),
                fieldGroup("Author", authorField),
                fieldGroup("Status", statusBox),
                mainButtons, otherButtons, formErrorLabel, hint);
        panel.getStyleClass().add("panel");
        panel.setMinWidth(320);
        panel.setPrefWidth(320);
        panel.setMaxWidth(320);
        return panel;
    }

    /**
     * Puts a small caption above an input control.
     *
     * @param labelText the caption
     * @param field     the input control
     * @return the caption and the control, stacked
     */
    private VBox fieldGroup(String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(4, label, field);
    }

    /**
     * Creates the panel on the right with the search tools and the table.
     *
     * @return the table panel
     */
    private VBox buildTablePanel() {
        Label title = new Label("Books");
        title.getStyleClass().add("panel-title");

        searchField.setPromptText("Search by ID, title or author");
        searchField.setPrefWidth(250);

        filterBox.getItems().add(ALL_STATUSES);
        for (BookStatus status : BookStatus.values()) {
            filterBox.getItems().add(status.getLabel());
        }
        filterBox.setValue(ALL_STATUSES);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, title, spacer, searchField, filterBox, viewAllButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox panel = new VBox(12, toolbar, table);
        panel.getStyleClass().add("panel");
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    /**
     * Sets up the table columns. Column widths follow the window width, and
     * the status column shows a coloured badge.
     */
    private void buildTable() {
        TableColumn<Book, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        idColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.13));

        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        titleColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.37));

        TableColumn<Book, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAuthor()));
        authorColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.28));

        TableColumn<Book, BookStatus> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStatus()));
        statusColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.17));
        statusColumn.setCellFactory(column -> new TableCell<Book, BookStatus>() {
            @Override
            protected void updateItem(BookStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status.getLabel());
                    badge.getStyleClass().addAll("badge", "badge-" + status.name().toLowerCase());
                    setText(null);
                    setGraphic(badge);
                }
            }
        });

        table.getColumns().add(idColumn);
        table.getColumns().add(titleColumn);
        table.getColumns().add(authorColumn);
        table.getColumns().add(statusColumn);
    }

    /**
     * Creates the bar at the bottom of the window. It shows messages on the
     * left and the save status on the right.
     *
     * @return the status bar
     */
    private HBox buildStatusBar() {
        messageLabel.getStyleClass().add("status-text");
        saveLabel.getStyleClass().add("status-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(messageLabel, spacer, saveLabel);
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    // ------------------------------------------------------------------
    // Reacting to the user
    // ------------------------------------------------------------------

    /**
     * Connects the buttons, the search tools and the table selection to their
     * handlers.
     */
    private void connectActions() {
        addButton.setOnAction(event -> onAdd());
        updateButton.setOnAction(event -> onUpdate());
        statusButton.setOnAction(event -> onSetStatus());
        deleteButton.setOnAction(event -> onDelete());
        clearButton.setOnAction(event -> clearForm());
        viewAllButton.setOnAction(event -> onViewAll());

        // Pressing Enter in the last field adds the book.
        authorField.setOnAction(event -> onAdd());

        // Typing in a form field removes the red error mark.
        idField.textProperty().addListener((observable, oldText, newText) -> hideFormError());
        titleField.textProperty().addListener((observable, oldText, newText) -> hideFormError());
        authorField.textProperty().addListener((observable, oldText, newText) -> hideFormError());

        // Typing in the search box or changing the filter narrows the table.
        searchField.textProperty().addListener((observable, oldText, newText) -> refreshTable());
        filterBox.setOnAction(event -> refreshTable());

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
            persist();
            refreshAll();
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
            persist();
            refreshAll();
            messageLabel.setText("Updated book " + book.getId() + ".");
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
            persist();
            refreshAll();
            messageLabel.setText("Book " + book.getId() + " is now " + book.getStatus() + ".");
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
                persist();
                refreshAll();
                clearForm();
                messageLabel.setText("Deleted book " + book.getId() + ". " + describeLibrary());
            }
        } catch (LibraryException error) {
            showError(error);
        }
    }

    /**
     * Handles the View all button: removes the search text and the status
     * filter so every book is shown.
     */
    private void onViewAll() {
        searchField.clear();
        filterBox.setValue(ALL_STATUSES);
        refreshTable();
        messageLabel.setText(describeLibrary());
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
        hideFormError();
    }

    /**
     * Shows an error under the form and marks the field that caused it in red.
     *
     * @param error the problem to show to the user
     */
    private void showError(LibraryException error) {
        hideFormError();
        formErrorLabel.setText(error.getMessage());
        formErrorLabel.setVisible(true);
        formErrorLabel.setManaged(true);

        TextField culprit = fieldFor(error);
        if (culprit != null) {
            culprit.getStyleClass().add(ERROR_STYLE);
        }
        messageLabel.setText("Error: " + error.getMessage());
    }

    /**
     * Removes the error message and the red marks from the form.
     */
    private void hideFormError() {
        formErrorLabel.setText("");
        formErrorLabel.setVisible(false);
        formErrorLabel.setManaged(false);
        idField.getStyleClass().remove(ERROR_STYLE);
        titleField.getStyleClass().remove(ERROR_STYLE);
        authorField.getStyleClass().remove(ERROR_STYLE);
    }

    /**
     * Finds the form field that an error is about.
     *
     * @param error the error that was reported
     * @return the field to mark in red, or {@code null} if no single field is to blame
     */
    private TextField fieldFor(LibraryException error) {
        if (error instanceof ValidationException) {
            String field = ((ValidationException) error).getField();
            if (ValidationException.FIELD_TITLE.equals(field)) {
                return titleField;
            }
            if (ValidationException.FIELD_AUTHOR.equals(field)) {
                return authorField;
            }
            if (ValidationException.FIELD_ID.equals(field)) {
                return idField;
            }
            return null;
        }
        // A duplicate ID or a book that is not found are both about the ID.
        return idField;
    }

    // ------------------------------------------------------------------
    // Saving and loading
    // ------------------------------------------------------------------

    /**
     * Loads the books that were saved the last time the program ran.
     */
    private void loadSavedBooks() {
        try {
            service.loadBooks(store.load());
            saveLabel.setText("Auto-save file: " + store.getFile().getFileName());
        } catch (IOException error) {
            saveLabel.setText("Could not read " + store.getFile().getFileName());
        }
    }

    /**
     * Saves all books to the file. Called after every change.
     */
    private void persist() {
        try {
            store.save(service.getAllBooks());
            saveLabel.setText("Saved to " + store.getFile().getFileName());
        } catch (IOException error) {
            saveLabel.setText("Save failed: " + error.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Refreshing what is shown
    // ------------------------------------------------------------------

    /**
     * Refreshes both the table and the summary cards.
     */
    private void refreshAll() {
        refreshTable();
        refreshCards();
    }

    /**
     * Reloads the table rows using the current search text and status filter.
     */
    private void refreshTable() {
        List<Book> shown = service.searchBooks(searchField.getText(), selectedFilterStatus());
        table.setItems(FXCollections.observableArrayList(shown));

        if (service.getBookCount() == 0) {
            table.setPlaceholder(new Label("No books yet. Add your first book using the form."));
        } else {
            table.setPlaceholder(new Label("No books match your search."));
        }
    }

    /**
     * Updates the four numbers in the summary cards.
     */
    private void refreshCards() {
        totalValue.setText(String.valueOf(service.getBookCount()));
        availableValue.setText(String.valueOf(service.countByStatus(BookStatus.AVAILABLE)));
        issuedValue.setText(String.valueOf(service.countByStatus(BookStatus.ISSUED)));
        reservedValue.setText(String.valueOf(service.countByStatus(BookStatus.RESERVED)));
    }

    /**
     * Converts the choice in the filter box to a status.
     *
     * @return the chosen status, or {@code null} if "All statuses" is chosen
     */
    private BookStatus selectedFilterStatus() {
        String choice = filterBox.getValue();
        for (BookStatus status : BookStatus.values()) {
            if (status.getLabel().equals(choice)) {
                return status;
            }
        }
        return null;
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
        return "The library has " + count + (count == 1 ? " book." : " books.");
    }
}
