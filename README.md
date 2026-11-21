# Library Book Management System

A JavaFX desktop application for managing library books.
Software Construction (BESE-30) - Open Ended Lab, Military College of Signals, NUST.

## Features

- Add a book (ID, title, author)
- View all books (with a message when the library is empty)
- Update book details (title and author)
- Delete a book (with a confirmation question)
- Set status: Available / Issued / Reserved
- Input validation (no empty ID, title or author; no duplicate ID) with clear error messages

## Requirements

- JDK 17 or newer
- Maven 3.8 or newer
- Git

In Visual Studio Code, install the **Extension Pack for Java** (it includes
*Maven for Java*). The first build needs an internet connection so Maven can
download JavaFX and JUnit.

## How to run

```bash
mvn javafx:run
```

Or open the project folder in VS Code, open `src/main/java/library/Launcher.java`
and press **Run**. (`Launcher` is used instead of `Main` because JavaFX
applications started directly from some IDEs fail with "JavaFX runtime
components are missing".)

## How to use the application

1. **Add** - type an ID, title and author, then click *Add*.
2. **Update** - click a row (or type an ID), edit the title and/or author, click *Update*.
3. **Set status** - click a row (or type an ID), pick a status, click *Set status*.
4. **Delete** - click a row (or type an ID), click *Delete* and confirm.
5. **View all** - reloads the table; an empty library shows a message.
6. **Clear** - empties the form.

Update, Delete and Set status always work on the ID in the *Book ID* box.
Clicking a table row simply copies that book into the form.

## How to run the tests

```bash
mvn test
```

## How to generate the Javadoc

```bash
mvn javadoc:javadoc
```

The HTML is written to `docs/javadoc/`. Open `docs/javadoc/index.html` in a browser.

## Project structure

```
library-management-system/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/library/
│   │   ├── Launcher.java            starts the app from an IDE
│   │   ├── Main.java                JavaFX entry point
│   │   ├── model/                   Book, BookStatus
│   │   ├── service/                 LibraryService (all rules and storage)
│   │   ├── exception/               LibraryException and its three subclasses
│   │   └── ui/                      MainView (window, table, form, dialogs)
│   └── test/java/library/service/   LibraryServiceTest (JUnit 4)
└── docs/javadoc/                    generated documentation
```

## Design

- **Layers:** `ui` shows the screen and calls `service`; `service` holds the
  books and enforces the rules; `model` holds plain data. The UI never changes
  data directly.
- **Data structure:** `LinkedHashMap<String, Book>` keyed by book ID. Duplicate
  checks and lookups take O(1) time and books stay in the order they were added.
- **Errors:** the service throws `ValidationException`, `DuplicateBookException`
  or `BookNotFoundException` (all extend `LibraryException`). The UI catches
  `LibraryException` and shows its message in an error dialog, so bad input
  never crashes the program.
- **Storage:** books are kept in memory only; they are lost when the program closes.

## Test cases

The eight test cases from the OEL brief are automated in `LibraryServiceTest`
(`tc1` to `tc8`) and can also be tried by hand in the window.

| # | Test case | Expected result |
|---|-----------|-----------------|
| 1 | Add a valid book | Book is added |
| 2 | Add a book with an empty title | Validation message shown |
| 3 | Add a book with a duplicate ID | Error message shown |
| 4 | View all books | All books displayed |
| 5 | View an empty library | Appropriate message shown |
| 6 | Update an existing book | Details updated |
| 7 | Update a non-existing book | Handled gracefully |
| 8 | Delete an existing book | Book removed |
