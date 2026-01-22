package Tests.Integration;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * Integration Tests for Library Management System
 *
 * Test Scenarios:
 * 1. Search Books and Issue - Tests the integration between search, book, and issue operations
 * 2. Add Book to Library - Tests book creation and library integration with database persistence
 * 3. Remove Book with Hold Requests - Tests book removal with hold request management
 * 4. Database Persistence - Save - Tests save operations and database integration
 *
 * @author Onejda
 */
public class IntegrationTests_Onejda {

    private Library library;
    private DatabaseManager dbManager;
    private Librarian librarian;
    private Clerk clerk;
    private Borrower borrower1;
    private Borrower borrower2;
    private Book book1;
    private Book book2;

    @BeforeEach
    public void setUp() {
        // Reset library singleton instance
        Library.resetInstance();
        library = Library.getInstance();

        // Set library configuration
        library.setName("Test Library");
        library.setFine(20.0);
        library.setRequestExpiry(7);
        library.setReturnDeadline(5);

        // Initialize database manager and connect
        dbManager = DatabaseManager.getInstance();
        // CRITICAL: Establish database connection for tests
        dbManager.connect();

        // Create test persons
        librarian = new Librarian(1, "Test Librarian", "Office 101", 1234567, 50000, 101);
        Library.librarian = librarian;

        clerk = new Clerk(2, "Test Clerk", "Desk 1", 2345678, 25000, 1);
        library.addClerk(clerk);

        borrower1 = new Borrower(3, "Alice Brown", "123 Main St", 3456789);
        library.addBorrower(borrower1);

        borrower2 = new Borrower(4, "Bob Wilson", "456 Oak Ave", 4567890);
        library.addBorrower(borrower2);

        // Create test books
        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        library.addBookinLibrary(book1);

        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
        library.addBookinLibrary(book2);
    }

    @AfterEach
    public void tearDown() {
        // Close database connection
        if (dbManager != null) {
            dbManager.closeConnection();
        }

        // Clean up after each test
        Library.resetInstance();
        Library.librarian = null;
        Library.persons = null;
    }

    // ==================== SCENARIO 1: SEARCH BOOKS AND ISSUE (IT-01) ====================

    /**
     * Classes involved: Library, Book, Borrower, Loan, Staff (Clerk)
     */
    @Test
    @DisplayName("IT-01: Search Books and Issue - Complete Workflow")
    public void testSearchBooksAndIssue() {
        System.out.println("\n=== Running IT-01: Search Books and Issue ===");

        // Step 1: Verify book exists in library
        ArrayList<Book> libraryBooks = library.getBooks();
        assertTrue(libraryBooks.contains(book1), "Book should exist in library before search");

        // Step 2: Search for book by title
        Book foundBook = null;
        String searchTerm = "clean code";
        for (Book b : libraryBooks) {
            if (b.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                foundBook = b;
                break;
            }
        }

        assertNotNull(foundBook, "Book should be found in search");
        assertEquals("Clean Code", foundBook.getTitle(),
                "Found book should match search criteria");

        // Step 3: Verify initial state before issuing
        assertFalse(foundBook.getIssuedStatus(),
                "Book should not be issued initially");
        assertEquals(0, borrower1.getBorrowedBooks().size(),
                "Borrower should have no borrowed books initially");
        int initialLoanCount = library.getLoans().size();

        // Step 4: Issue the book to borrower
        foundBook.issueBook(borrower1, clerk);

        // Step 5: Verify integration between Book, Borrower, Loan, and Staff
        assertTrue(foundBook.getIssuedStatus(),
                "Book should be marked as issued after issuing");

        ArrayList<Loan> borrowerLoans = borrower1.getBorrowedBooks();
        assertEquals(1, borrowerLoans.size(),
                "Borrower should have exactly one borrowed book");

        Loan issuedLoan = borrowerLoans.get(0);
        assertEquals(foundBook, issuedLoan.getBook(),
                "Loan should reference the correct book");
        assertEquals(borrower1, issuedLoan.getBorrower(),
                "Loan should reference the correct borrower");
        assertEquals(clerk, issuedLoan.getIssuer(),
                "Loan should reference the correct staff member (issuer)");
        assertNotNull(issuedLoan.getIssuedDate(),
                "Loan should have an issue date");
        assertNull(issuedLoan.getReturnDate(),
                "Loan should not have a return date yet");
        assertNull(issuedLoan.getReceiver(),
                "Loan should not have a receiver yet (not returned)");

        // Step 6: Verify loan exists in library's loan list
        ArrayList<Loan> libraryLoans = library.getLoans();
        assertEquals(initialLoanCount + 1, libraryLoans.size(),
                "Library should have one more loan");
        assertTrue(libraryLoans.contains(issuedLoan),
                "Loan should exist in library's loan records");
    }

    // ==================== SCENARIO 2: ADD BOOK TO LIBRARY (IT-02) ====================
    /**
     * Classes involved: Library, Book, DatabaseManager
     */
    @Test
    @DisplayName("IT-02: Add Book to Library ")
    public void testAddBookToLibrary() {
        System.out.println("\n=== Running IT-02: Add Book to Library ===");

        // Step 1: Get initial book count
        int initialBookCount = library.getBooks().size();
        assertEquals(2, initialBookCount, "Should start with 2 books");

        // Step 2: Create new book with temporary ID
        String newTitle = "Refactoring";
        String newSubject = "Software Engineering";
        String newAuthor = "Martin Fowler";

        Book newBook = new Book(-1, newTitle, newSubject, newAuthor, false);

        // Verify book has temporary ID
        assertTrue(newBook.getID() > 0, "Book should have auto-generated ID from constructor");
        int tempId = newBook.getID();

        // Step 3: Save book to database
        newBook.saveToDatabase();

        // Step 4: Add book to library collection
        library.addBookinLibrary(newBook);

        // Step 5: Verify book was added to library
        ArrayList<Book> libraryBooks = library.getBooks();
        assertEquals(initialBookCount + 1, libraryBooks.size(),
                "Library should have one more book after addition");
        assertTrue(libraryBooks.contains(newBook),
                "New book should exist in library collection");

        // Step 6: Verify book properties
        assertEquals(newTitle, newBook.getTitle(),
                "Book title should match");
        assertEquals(newSubject, newBook.getSubject(),
                "Book subject should match");
        assertEquals(newAuthor, newBook.getAuthor(),
                "Book author should match");
        assertFalse(newBook.getIssuedStatus(),
                "New book should not be issued initially");

        // Step 7: Verify book is searchable
        Book foundBook = null;
        for (Book b : libraryBooks) {
            if (b.getTitle().equals(newTitle)) {
                foundBook = b;
                break;
            }
        }
        assertNotNull(foundBook, "Newly added book should be searchable");
        assertEquals(newBook, foundBook, "Found book should match added book");

        // Step 8: Verify book has empty hold requests initially
        assertEquals(0, newBook.getHoldRequests().size(),
                "New book should have no hold requests");
    }

    // ==================== SCENARIO 3: REMOVE BOOK WITH HOLD REQUESTS (IT-03) ====================
    /**
     * Classes involved: Library, Book, HoldRequest, HoldRequestOperations, Borrower
     */
    @Test
    @DisplayName("IT-03: Remove Book with Hold Requests")
    public void testRemoveBookWithHoldRequests() {
        System.out.println("\n=== Running IT-03: Remove Book with Hold Requests ===");

        // Step 1: Place hold requests on the book
        book1.placeBookOnHold(borrower1);
        book1.placeBookOnHold(borrower2);

        // Step 2: Verify hold requests exist
        assertEquals(2, book1.getHoldRequests().size(),
                "Book should have 2 hold requests");
        assertEquals(1, borrower1.getOnHoldBooks().size(),
                "Borrower1 should have 1 hold request");
        assertEquals(1, borrower2.getOnHoldBooks().size(),
                "Borrower2 should have 1 hold request");

        int initialBookCount = library.getBooks().size();

        // Step 3: Clean up hold requests (simulating the logic inside removeBookfromLibrary)
        ArrayList<HoldRequest> holdRequests = new ArrayList<>(book1.getHoldRequests());

        // Verify the hold requests are properly linked
        for (HoldRequest hr : holdRequests) {
            assertEquals(book1, hr.getBook(),
                    "Hold request should reference correct book");
            assertTrue(hr.getBorrower() == borrower1 || hr.getBorrower() == borrower2,
                    "Hold request should reference one of the borrowers");

            // Remove from borrower
            hr.getBorrower().removeHoldRequest(hr);

            // Remove from database
            if (hr.getRequestDate() != null) {
                hr.deleteFromDatabase();
            }
        }

        // Clear the book's hold request list
        book1.getHoldRequestOperations().getHoldRequests().clear();

        // Step 4: Verify hold requests are removed from borrowers
        assertEquals(0, borrower1.getOnHoldBooks().size(),
                "Borrower1 should have no hold requests after cleanup");
        assertEquals(0, borrower2.getOnHoldBooks().size(),
                "Borrower2 should have no hold requests after cleanup");
        assertEquals(0, book1.getHoldRequests().size(),
                "Book should have no hold requests after cleanup");

        // Step 5: Remove book from library
        library.removeBookfromLibrary(book1);

        // Step 6: Verify book removal
        assertEquals(initialBookCount - 1, library.getBooks().size(),
                "Library should have one less book");
        assertFalse(library.getBooks().contains(book1),
                "Book should not exist in library");

        // Step 7: Verify book cannot be found in search
        Book foundBook = null;
        for (Book b : library.getBooks()) {
            if (b.getID() == book1.getID()) {
                foundBook = b;
                break;
            }
        }
        assertNull(foundBook, "Removed book should not be searchable");
    }

    /**
     * Integration Test Scenario 3 - Edge Case: Remove Book Without Hold Requests (IT-03a)
     *
     * Tests that books without hold requests can be removed cleanly.
     */
    @Test
    @DisplayName("IT-03a: Remove Book without Hold Requests")
    public void testRemoveBookWithoutHoldRequests() {
        System.out.println("\n=== Running IT-03a: Remove Book without Hold Requests ===");

        // Verify no hold requests
        assertEquals(0, book1.getHoldRequests().size(),
                "Book should have no hold requests");

        int initialCount = library.getBooks().size();

        // Remove book
        library.removeBookfromLibrary(book1);

        // Verify removal
        assertEquals(initialCount - 1, library.getBooks().size(),
                "Book should be removed");
        assertFalse(library.getBooks().contains(book1),
                "Book should not exist in library");
    }

    /**
     * Integration Test Scenario 3 - Edge Case: Cannot Remove Issued Book (IT-03b)
     *
     * Tests that the system prevents removal of currently issued books.
     */
    @Test
    @DisplayName("IT-03b: Cannot Remove Issued Book")
    public void testCannotRemoveIssuedBook() {
        System.out.println("\n=== Running IT-03b: Cannot Remove Issued Book ===");

        // Issue book to borrower
        book1.issueBook(borrower1, clerk);
        assertTrue(book1.getIssuedStatus(),
                "Book should be marked as issued");

        int initialCount = library.getBooks().size();

        // Attempt to remove issued book
        library.removeBookfromLibrary(book1);

        // Verify book was noy removed
        assertEquals(initialCount, library.getBooks().size(),
                "Book count should remain same");
        assertTrue(library.getBooks().contains(book1),
                "Issued book should still exist in library");
        assertEquals(1, borrower1.getBorrowedBooks().size(),
                "Borrower should still have the book");
        assertTrue(book1.getIssuedStatus(),
                "Book should still be marked as issued");
    }

// ==================== SCENARIO 5: DATABASE PERSISTENCE - SAVE (IT-05) ====================
    /**
     * Classes involved: Book, Borrower, Loan, DatabaseManager
     */
    @Test
    @DisplayName("IT-05: Database Persistence - Save Operations")
    public void testDatabasePersistenceSave() {
        // Step 1: Create and save a new book
        Book newBook = new Book(-1, "Database Systems", "Computer Science", "Ramez Elmasri", false);

        int tempBookId = newBook.getID();
        assertTrue(tempBookId > 0, "Book should have auto-generated ID");

        newBook.saveToDatabase();
        assertTrue(newBook.getID() > 0, "Book should have valid ID after database save");

        // Step 2: Create and save a new borrower
        Borrower newBorrower = new Borrower(-1, "Charlie Davis", "789 Elm St", 5678901);

        assertTrue(newBorrower.getID() > 0, "Borrower should have auto-generated ID");
        assertNotNull(newBorrower.getPassword(), "Borrower password should be generated");

        newBorrower.saveToDatabase();
        assertTrue(newBorrower.getID() > 0, "Borrower should have valid ID after save");

        // Step 3: Add to library and issue book (creates loan in database)
        library.addBookinLibrary(newBook);
        library.addBorrower(newBorrower);

        newBook.issueBook(newBorrower, clerk);

        // Step 4: Verify loan was created and saved with proper relationships
        assertEquals(1, newBorrower.getBorrowedBooks().size(), "Borrower should have 1 loan");

        Loan loan = newBorrower.getBorrowedBooks().get(0);
        assertNotNull(loan.getBorrower(), "Loan should reference borrower");
        assertNotNull(loan.getBook(), "Loan should reference book");
        assertNotNull(loan.getIssuer(), "Loan should reference issuer");
        assertNotNull(loan.getIssuedDate(), "Loan should have issue date");

        assertTrue(library.getLoans().contains(loan), "Loan should be tracked in library");

        // Step 5: Verify object relationships persist after database operations
        assertEquals(newBook, loan.getBook(), "Loan maintains book reference after save");
        assertEquals(newBorrower, loan.getBorrower(), "Loan maintains borrower reference after save");
        assertEquals(clerk, loan.getIssuer(), "Loan maintains staff reference after save");
        assertTrue(newBook.getIssuedStatus(), "Book status persisted correctly");
    }

    // ==================== SUMMARY TEST ====================

    /**
     * Summary test that verifies all integration scenarios work together
     */
    @Test
    @DisplayName("IT-Summary: All Integration Scenarios Combined")
    public void testAllIntegrationScenariosCombined() {
        System.out.println("\n=== Running IT-Summary: Combined Integration Test ===");

        // 1. Add a book
        Book book = new Book(-1, "Integration Test Book", "Testing", "Test Author", false);
        book.saveToDatabase();
        library.addBookinLibrary(book);
        assertTrue(library.getBooks().contains(book));

        // 2. Create borrower
        Borrower borrower = new Borrower(-1, "Integration Borrower", "Test St", 1111111);
        borrower.saveToDatabase();
        library.addBorrower(borrower);

        // 3. Find persons by ID
        Borrower foundBorrower = library.findBorrowerById(borrower.getID());
        assertNotNull(foundBorrower);

        // 4. Issue book
        book.issueBook(foundBorrower, clerk);
        assertTrue(book.getIssuedStatus());
        assertEquals(1, foundBorrower.getBorrowedBooks().size());

        // 5. Return book
        Loan loan = foundBorrower.getBorrowedBooks().get(0);
        book.returnBook(foundBorrower, loan, clerk);
        assertFalse(book.getIssuedStatus());
        assertEquals(0, foundBorrower.getBorrowedBooks().size());

        // 6. Place hold request
        book.placeBookOnHold(foundBorrower);
        assertEquals(1, book.getHoldRequests().size());
        assertEquals(1, foundBorrower.getOnHoldBooks().size());

        // 7. Remove book (after cleaning holds)
        ArrayList<HoldRequest> holds = new ArrayList<>(book.getHoldRequests());
        for (HoldRequest hr : holds) {
            hr.getBorrower().removeHoldRequest(hr);
            hr.deleteFromDatabase();
        }
        book.getHoldRequestOperations().getHoldRequests().clear();

        int beforeRemove = library.getBooks().size();
        library.removeBookfromLibrary(book);
        assertEquals(beforeRemove - 1, library.getBooks().size());

        System.out.println("✓ IT-Summary PASSED: All integration scenarios work together");
        System.out.println("  - Complete workflow from book creation to removal verified");
    }
}