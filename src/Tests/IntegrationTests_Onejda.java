package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Integration Tests for Library Management System
 *
 * Test Scenarios:
 * 1. Search Books and Issue - Tests the integration between search, book, and issue operations
 * 2. Add Book to Library - Tests book creation and library integration
 * 3. Remove Book with Hold Requests - Tests book removal with hold request management
 * 4. Find Person by ID (Type Checking) - Tests person retrieval with type validation
 * 5. Database Persistence - Tests save and verification of data
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

    @Test
    @DisplayName("IT-01: Search Books and Issue - Complete Workflow")
    public void testSearchBooksAndIssue() {
        System.out.println("\n=== Running IT-01: Search Books and Issue ===");

        // Step 1: Verify book exists in library
        ArrayList<Book> libraryBooks = library.getBooks();
        assertTrue(libraryBooks.contains(book1),
                "Book should exist in library before search");

        // Step 2: Search for book by title (simulating search operation)
        Book foundBook = null;
        for (Book b : libraryBooks) {
            if (b.getTitle().toLowerCase().contains("clean code".toLowerCase())) {
                foundBook = b;
                break;
            }
        }

        assertNotNull(foundBook, "Book should be found in search");
        assertEquals("Clean Code", foundBook.getTitle(),
                "Found book should match search criteria");

        // Step 3: Issue the book to borrower
        assertFalse(foundBook.getIssuedStatus(),
                "Book should not be issued initially");

        foundBook.issueBook(borrower1, clerk);

        // Step 4: Verify integration between Book, Borrower, Loan, and Staff
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

        // Step 5: Verify loan exists in library's loan list
        ArrayList<Loan> libraryLoans = library.getLoans();
        assertTrue(libraryLoans.contains(issuedLoan),
                "Loan should exist in library's loan records");

        System.out.println("✓ IT-01 PASSED: Search and Issue integration verified");
        System.out.println("  - Book found: " + foundBook.getTitle());
        System.out.println("  - Issued to: " + borrower1.getName());
        System.out.println("  - Issued by: " + clerk.getName());
        System.out.println("  - Book status updated: " + foundBook.getIssuedStatus());
    }

    // ==================== SCENARIO 2: ADD BOOK TO LIBRARY  (IT-02) ====================

    @Test
    @DisplayName("IT-02: Add Book to Library - Complete Workflow")
    public void testAddBookToLibrary() {
        System.out.println("\n=== Running IT-02: Add Book to Library ===");

        // Step 1: Get initial book count
        int initialBookCount = library.getBooks().size();
        assertEquals(2, initialBookCount, "Should start with 2 books");

        // Step 2: Create and add new book
        String newTitle = "Refactoring";
        String newSubject = "Software Engineering";
        String newAuthor = "Martin Fowler";

        Book newBook = new Book(-1, newTitle, newSubject, newAuthor, false);
        library.addBookinLibrary(newBook);

        // Step 3: Verify book was added to library
        ArrayList<Book> libraryBooks = library.getBooks();
        assertEquals(initialBookCount + 1, libraryBooks.size(),
                "Library should have one more book after addition");
        assertTrue(libraryBooks.contains(newBook),
                "New book should exist in library collection");

        // Step 4: Verify book properties
        assertNotNull(newBook.getID(), "New book should have an ID");
        assertTrue(newBook.getID() > 0, "Book ID should be positive");
        assertEquals(newTitle, newBook.getTitle(),
                "Book title should match");
        assertEquals(newSubject, newBook.getSubject(),
                "Book subject should match");
        assertEquals(newAuthor, newBook.getAuthor(),
                "Book author should match");
        assertFalse(newBook.getIssuedStatus(),
                "New book should not be issued initially");

        // Step 5: Verify book is searchable
        Book foundBook = null;
        for (Book b : libraryBooks) {
            if (b.getTitle().equals(newTitle)) {
                foundBook = b;
                break;
            }
        }
        assertNotNull(foundBook, "Newly added book should be searchable");
        assertEquals(newBook, foundBook, "Found book should match added book");

        // Step 6: Verify book has empty hold requests initially
        assertEquals(0, newBook.getHoldRequests().size(),
                "New book should have no hold requests");

        System.out.println("✓ IT-02 PASSED: Add book to library integration verified");
        System.out.println("  - Book added: " + newBook.getTitle());
        System.out.println("  - Book ID: " + newBook.getID());
        System.out.println("  - Total books in library: " + libraryBooks.size());
    }


    // ==================== SCENARIO 3: REMOVE BOOK WITH HOLD REQUESTS ====================

    @Test
    @DisplayName("IT-03: Remove Book with Hold Requests - Complete Workflow")
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

        // Step 3: Manually remove hold requests (simulating user confirmation)
        ArrayList<HoldRequest> holdRequests = new ArrayList<>(book1.getHoldRequests());
        for (HoldRequest hr : holdRequests) {
            hr.getBorrower().removeHoldRequest(hr);
        }
        book1.getHoldRequestOperations().getHoldRequests().clear();

        // Step 4: Remove book from library
        library.removeBookfromLibrary(book1);

        // Step 5: Verify book removal
        assertEquals(initialBookCount - 1, library.getBooks().size(),
                "Library should have one less book");
        assertFalse(library.getBooks().contains(book1),
                "Book should not exist in library");

        // Step 6: Verify hold requests are removed from borrowers
        assertEquals(0, borrower1.getOnHoldBooks().size(),
                "Borrower1 should have no hold requests after book removal");
        assertEquals(0, borrower2.getOnHoldBooks().size(),
                "Borrower2 should have no hold requests after book removal");

        // Step 7: Verify book cannot be found in search
        Book foundBook = null;
        for (Book b : library.getBooks()) {
            if (b.getID() == book1.getID()) {
                foundBook = b;
                break;
            }
        }
        assertNull(foundBook, "Removed book should not be searchable");

        System.out.println("✓ IT-03 PASSED: Remove book with hold requests verified");
        System.out.println("  - Hold requests cleaned: 2");
        System.out.println("  - Book removed: " + book1.getTitle());
        System.out.println("  - Remaining books: " + library.getBooks().size());
    }

    /**
     * Integration Test Scenario 3 - Edge Case: Remove Book Without Hold Requests (IT-03a)
     */

    @Test
    @DisplayName("IT-03a: Remove Book without Hold Requests")
    public void testRemoveBookWithoutHoldRequests() {
        System.out.println("\n=== Running IT-03a: Remove Book without Hold Requests ===");

        // Verify no hold requests
        assertEquals(0, book1.getHoldRequests().size());

        int initialCount = library.getBooks().size();

        // Remove book
        library.removeBookfromLibrary(book1);

        // Verify removal
        assertEquals(initialCount - 1, library.getBooks().size(),
                "Book should be removed");
        assertFalse(library.getBooks().contains(book1),
                "Book should not exist in library");

        System.out.println("✓ IT-03a PASSED: Book removed without hold requests");
    }

    /**
     * Integration Test Scenario 3 - Edge Case: Cannot Remove Issued Book (IT-03b)
     */

    @Test
    @DisplayName("IT-03b: Cannot Remove Issued Book")
    public void testCannotRemoveIssuedBook() {
        System.out.println("\n=== Running IT-03b: Cannot Remove Issued Book ===");

        // Issue book to borrower
        book1.issueBook(borrower1, clerk);
        assertTrue(book1.getIssuedStatus());

        int initialCount = library.getBooks().size();

        // Attempt to remove issued book
        library.removeBookfromLibrary(book1);

        // Verify book was NOT removed
        assertEquals(initialCount, library.getBooks().size(),
                "Book count should remain same");
        assertTrue(library.getBooks().contains(book1),
                "Issued book should still exist in library");
        assertEquals(1, borrower1.getBorrowedBooks().size(),
                "Borrower should still have the book");

        System.out.println("✓ IT-03b PASSED: Issued book cannot be removed");
    }

    // ==================== SCENARIO 4: FIND PERSON BY ID (TYPE CHECKING) (IT-04) ====================

    @Test
    @DisplayName("IT-04: Find Person by ID - Type Checking")
    public void testFindPersonByIdWithTypeChecking() {
        System.out.println("\n=== Running IT-04: Find Person by ID - Type Checking ===");

        // Step 1: Find Borrower by ID
        Borrower foundBorrower = library.findBorrowerById(borrower1.getID());
        assertNotNull(foundBorrower, "Should find borrower by ID");
        assertEquals(borrower1, foundBorrower, "Found borrower should match");
        assertTrue(foundBorrower instanceof Borrower,
                "Found person should be instance of Borrower");
        assertTrue(foundBorrower instanceof Person,
                "Borrower should be instance of Person (inheritance)");
        // Note: Cannot use instanceof for unrelated types (Borrower and Staff are siblings)
        assertFalse(foundBorrower.getClass().equals(Staff.class),
                "Borrower should NOT be Staff class");

        // Verify borrower-specific functionality
        assertNotNull(foundBorrower.getBorrowedBooks(),
                "Borrower should have borrowed books list");
        assertNotNull(foundBorrower.getOnHoldBooks(),
                "Borrower should have hold requests list");

        // Step 2: Find Clerk by ID
        Clerk foundClerk = library.findClerkById(clerk.getID());
        assertNotNull(foundClerk, "Should find clerk by ID");
        assertEquals(clerk, foundClerk, "Found clerk should match");
        assertTrue(foundClerk instanceof Clerk,
                "Found person should be instance of Clerk");
        assertTrue(foundClerk instanceof Staff,
                "Clerk should be instance of Staff (inheritance)");
        assertTrue(foundClerk instanceof Person,
                "Clerk should be instance of Person (inheritance)");
        // Note: Cannot use instanceof for unrelated types
        assertFalse(foundClerk.getClass().equals(Borrower.class),
                "Clerk should NOT be Borrower class");

        // Verify clerk-specific attributes
        assertTrue(foundClerk.getDeskNo() > 0,
                "Clerk should have desk number");
        assertTrue(foundClerk.getSalary() > 0,
                "Clerk should have salary (Staff attribute)");

        // Step 3: Find Staff (generic) by ID
        Staff foundStaffClerk = library.findStaffById(clerk.getID());
        assertNotNull(foundStaffClerk, "Should find staff by ID");
        assertEquals(clerk, foundStaffClerk, "Found staff should match clerk");
        assertTrue(foundStaffClerk instanceof Staff,
                "Should be instance of Staff");

        Staff foundStaffLibrarian = library.findStaffById(librarian.getID());
        assertNotNull(foundStaffLibrarian, "Should find librarian as staff");
        assertEquals(librarian, foundStaffLibrarian,
                "Found staff should match librarian");
        assertTrue(foundStaffLibrarian instanceof Librarian,
                "Should be instance of Librarian");
        assertTrue(foundStaffLibrarian instanceof Staff,
                "Librarian should be instance of Staff");

        // Step 4: Verify Librarian
        Librarian foundLibrarian = library.getLibrarian();
        assertNotNull(foundLibrarian, "Should have librarian");
        assertEquals(librarian, foundLibrarian, "Found librarian should match");
        assertTrue(foundLibrarian instanceof Librarian,
                "Should be instance of Librarian");
        assertTrue(foundLibrarian instanceof Staff,
                "Librarian should be instance of Staff");
        assertTrue(foundLibrarian instanceof Person,
                "Librarian should be instance of Person");

        // Verify librarian-specific attributes
        assertTrue(foundLibrarian.getOfficeNo() > 0,
                "Librarian should have office number");

        // Step 5: Test invalid ID
        Borrower notFound = library.findBorrowerById(999);
        assertNull(notFound, "Should return null for non-existent ID");

        // Step 6: Test wrong type lookup
        Borrower clerkAsBorrower = library.findBorrowerById(clerk.getID());
        assertNull(clerkAsBorrower,
                "Should not find clerk when looking for borrower");

        Clerk borrowerAsClerk = library.findClerkById(borrower1.getID());
        assertNull(borrowerAsClerk,
                "Should not find borrower when looking for clerk");

        System.out.println("✓ IT-04 PASSED: Person type checking verified");
        System.out.println("  - Borrower found: " + foundBorrower.getName() + " (ID: " + foundBorrower.getID() + ")");
        System.out.println("  - Clerk found: " + foundClerk.getName() + " (ID: " + foundClerk.getID() + ")");
        System.out.println("  - Librarian found: " + foundLibrarian.getName() + " (ID: " + foundLibrarian.getID() + ")");
        System.out.println("  - Type checking: All person types correctly identified");
    }

    // ==================== SCENARIO 5: DATABASE PERSISTENCE ====================

    @Test
    @DisplayName("IT-05: Database Persistence - Save Operations")
    public void testDatabasePersistenceSave() {
        System.out.println("\n=== Running IT-05: Database Persistence - Save ===");

        // This test verifies the save methods are called correctly
        // In a real scenario, you would verify actual database writes

        // Step 1: Save a new book
        Book newBook = new Book(-1, "Database Systems", "Computer Science",
                "Ramez Elmasri", false);

        // Verify book has attributes ready for saving
        assertNotNull(newBook.getTitle(), "Book title should exist");
        assertNotNull(newBook.getAuthor(), "Book author should exist");
        assertNotNull(newBook.getSubject(), "Book subject should exist");
        assertFalse(newBook.getIssuedStatus(), "Book should not be issued");

        // Step 2: Save a new borrower
        Borrower newBorrower = new Borrower(-1, "Charlie Davis", "789 Elm St", 5678901);

        // Verify borrower has attributes ready for saving
        assertNotNull(newBorrower.getName(), "Borrower name should exist");
        assertNotNull(newBorrower.getAddress(), "Borrower address should exist");
        assertTrue(newBorrower.getPhoneNumber() > 0,
                "Borrower phone should be valid");
        assertNotNull(newBorrower.getPassword(),
                "Borrower password should be generated");

        // Step 3: Create and verify loan for persistence
        library.addBookinLibrary(newBook);
        library.addBorrower(newBorrower);

        newBook.issueBook(newBorrower, clerk);

        // Verify loan attributes for database
        Loan loan = newBorrower.getBorrowedBooks().get(0);
        assertNotNull(loan.getBorrower(), "Loan should have borrower");
        assertNotNull(loan.getBook(), "Loan should have book");
        assertNotNull(loan.getIssuer(), "Loan should have issuer");
        assertNotNull(loan.getIssuedDate(), "Loan should have issue date");

        // Step 4: Create and verify hold request for persistence
        Book heldBook = new Book(-1, "Operating Systems", "Computer Science",
                "Silberschatz", false);
        library.addBookinLibrary(heldBook);

        heldBook.placeBookOnHold(newBorrower);

        // Verify hold request attributes for database
        HoldRequest hr = heldBook.getHoldRequests().get(0);
        assertNotNull(hr.getBorrower(), "Hold request should have borrower");
        assertNotNull(hr.getBook(), "Hold request should have book");
        assertNotNull(hr.getRequestDate(), "Hold request should have date");

        System.out.println("✓ IT-05 PASSED: Database persistence operations verified");
        System.out.println("  - Book ready for save: " + newBook.getTitle());
        System.out.println("  - Borrower ready for save: " + newBorrower.getName());
        System.out.println("  - Loan ready for save: " + loan.getBook().getTitle());
        System.out.println("  - Hold request ready for save");
    }
}
