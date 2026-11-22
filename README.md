# Library Book Management System

A JavaFX desktop application for managing library books.
Software Construction (BESE-30) - Open Ended Lab, Military College of Signals, NUST.

## Features

- **Add, update and delete books** (ID, title, author), with a confirmation before deleting
- **Status tracking:** Available / Issued / Reserved, shown as coloured badges
- **Summary cards:** live counts of total, available, issued and reserved books
- **Search and filter:** search by ID, title or author, and filter by status
- **View all** with a clear message when the library is empty
- **Input validation:** no empty ID, title or author, and no duplicate ID. The field
  that caused the problem turns red and the message appears under the form
- **Automatic saving:** every change is saved to `library-data.csv` and loaded again
  the next time the program starts
- Resizable window with a sortable table (click a column heading)

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

1. **Add** - type an ID, title and author, then click *Add book* (or press Enter in the Author box).
2. **Update** - click a row (or type an ID), edit the title and/or author, click *Update*.
3. **Set status** - click a row (or type an ID), pick a status, click *Set status*.
4. **Delete** - click a row (or type an ID), click *Delete* and confirm.
5. **Search / filter** - type in the search box or pick a status above the table.
6. **View all** - clears the search and the filter. An empty library shows a message.
7. **Clear** - empties the form.

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
├── pom.xml                          build file: dependencies and plugins
├── README.md
├── library-data.csv                 created by the app when you add books (not in Git)
├── src/
│   ├── main/
│   │   ├── java/library/
│   │   │   ├── Launcher.java        starts the app from an IDE
│   │   │   ├── Main.java            JavaFX entry point
│   │   │   ├── model/               BACKEND - plain data
│   │   │   │   ├── Book.java
│   │   │   │   └── BookStatus.java
│   │   │   ├── service/             BACKEND - all rules and the in-memory books
│   │   │   │   └── LibraryService.java
│   │   │   ├── exception/           BACKEND - error types
│   │   │   │   └── LibraryException.java and its three subclasses
│   │   │   ├── storage/             BACKEND - saving and loading the CSV file
│   │   │   │   └── BookFileStore.java
│   │   │   └── ui/                  FRONTEND - what the user sees and clicks
│   │   │       └── MainView.java
│   │   └── resources/
│   │       └── styles.css           FRONTEND - colours, fonts, badges
│   └── test/java/library/           JUnit tests
│       ├── service/LibraryServiceTest.java
│       └── storage/BookFileStoreTest.java
└── docs/
    ├── javadoc/                     generated documentation
    ├── report/                      the lab report
    └── screenshots/                 screenshots used in the report
```

## Design

- **Layers:** `ui` shows the screen and calls `service`; `service` holds the
  books and enforces the rules; `storage` reads and writes the file; `model`
  holds plain data. The UI never changes data directly.
- **Data structure:** `LinkedHashMap<String, Book>` keyed by book ID. Duplicate
  checks and lookups take O(1) time and books stay in the order they were added.
  Search walks through the books once, which is O(n).
- **Errors:** the service throws `ValidationException`, `DuplicateBookException`
  or `BookNotFoundException` (all extend `LibraryException`). A
  `ValidationException` also names the field at fault, so the UI can mark it red.
  Bad input never crashes the program.
- **Storage:** books are saved as CSV (`id,title,author,status`). Commas and quotes
  in titles are escaped, damaged lines are skipped when loading, and the file is
  written to a temporary file first so a crash cannot corrupt it.

## Test cases

The eight test cases from the OEL brief are automated in `LibraryServiceTest`
(`tc1` to `tc8`) and can also be tried by hand in the window. Extra tests cover
search, status counts and saving/loading.

| # | Test case | Expected result |
|---|-----------|-----------------|
| 1 | Add a valid book | Book is added |
| 2 | Add a book with an empty title | Validation message shown, Title field turns red |
| 3 | Add a book with a duplicate ID | Error message shown, Book ID field turns red |
| 4 | View all books | All books displayed |
| 5 | View an empty library | "No books yet" message shown |
| 6 | Update an existing book | Details updated |
| 7 | Update a non-existing book | Error message shown, program keeps running |
| 8 | Delete an existing book | Book removed after confirmation |
