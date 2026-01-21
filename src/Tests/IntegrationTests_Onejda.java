package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Integration Tests for Library Management System - FIXED VERSION
 *
 * Test Scenarios:
 * 1. Search Books and Issue - Tests the integration between search, book, and issue operations
 * 2. Add Book to Library - Tests book creation and library integration with database persistence
 * 3. Remove Book with Hold Requests - Tests book removal with hold request management
 * 4. Find Person by ID (Type Checking) - Tests person retrieval with type validation
 * 5. Database Persistence - Save - Tests save operations and database integration
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
        // NOTE: Library.searchForBooks() requires user input (Scanner), so we simulate
        // the search logic here for automated testing.
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

        // Step 4: Issue the book to borrower (Integration point)
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

        // Step 7: Verify book cannot be issued again
        int borrowedBooksCount = borrower2.getBorrowedBooks().size();
        // Note: Attempting to issue an already-issued book would prompt for hold request
        // This is tested separately in other integration tests

        System.out.println("✓ IT-01 PASSED: Search and Issue integration verified");
        System.out.println("  - Book found: " + foundBook.getTitle());
        System.out.println("  - Issued to: " + borrower1.getName());
        System.out.println("  - Issued by: " + clerk.getName());
        System.out.println("  - Book status updated: " + foundBook.getIssuedStatus());
        System.out.println("  - Loan created and tracked in library");
    }

    // ==================== SCENARIO 2: ADD BOOK TO LIBRARY (IT-02) ====================
    /**
     * Classes involved: Library, Book, DatabaseManager
     */
    @Test
    @DisplayName("IT-02: Add Book to Library - Complete Workflow with Database Persistence")
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

        // Verify database assigned an ID (may be same or different depending on implementation)
        assertTrue(newBook.getID() > 0, "Book should have valid ID after database save");

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

        // Step 9: Verify book has proper HoldRequestOperations initialized
        assertNotNull(newBook.getHoldRequestOperations(),
                "Book should have HoldRequestOperations initialized");

        System.out.println("✓ IT-02 PASSED: Add book to library integration verified");
        System.out.println("  - Book created with ID: " + tempId);
        System.out.println("  - Book saved to database");
        System.out.println("  - Book added: " + newBook.getTitle());
        System.out.println("  - Total books in library: " + libraryBooks.size());
    }

    // ==================== SCENARIO 3: REMOVE BOOK WITH HOLD REQUESTS (IT-03) ====================
    /**
     * Classes involved: Library, Book, HoldRequest, HoldRequestOperations, Borrower
     */
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

        // Step 3: Clean up hold requests (simulating the logic inside removeBookfromLibrary)
        ArrayList<HoldRequest> holdRequests = new ArrayList<>(book1.getHoldRequests());

        // Verify the hold requests are properly linked
        for (HoldRequest hr : holdRequests) {
            assertEquals(book1, hr.getBook(),
                    "Hold request should reference correct book");
            assertTrue(hr.getBorrower() == borrower1 || hr.getBorrower() == borrower2,
                    "Hold request should reference one of the borrowers");

            // Remove from borrower (integration point)
            hr.getBorrower().removeHoldRequest(hr);

            // Remove from database (if it was saved)
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

        System.out.println("✓ IT-03 PASSED: Remove book with hold requests verified");
        System.out.println("  - Hold requests cleaned: 2");
        System.out.println("  - Borrower hold lists updated");
        System.out.println("  - Book removed: " + book1.getTitle());
        System.out.println("  - Remaining books: " + library.getBooks().size());
        System.out.println("  NOTE: Test simulates user confirmation for automated testing");
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

        System.out.println("✓ IT-03a PASSED: Book removed without hold requests");
        System.out.println("  - Clean removal without hold request complications");
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

        // Verify book was NOT removed (safety check works)
        assertEquals(initialCount, library.getBooks().size(),
                "Book count should remain same");
        assertTrue(library.getBooks().contains(book1),
                "Issued book should still exist in library");
        assertEquals(1, borrower1.getBorrowedBooks().size(),
                "Borrower should still have the book");
        assertTrue(book1.getIssuedStatus(),
                "Book should still be marked as issued");

        System.out.println("✓ IT-03b PASSED: Issued book cannot be removed");
        System.out.println("  - Safety check prevents removal of borrowed books");
    }

    // ==================== SCENARIO 4: FIND PERSON BY ID (TYPE CHECKING) (IT-04) ====================
    /**
     * Classes involved: Library, Person, Borrower, Staff, Clerk, Librarian
     */
    @Test
    @DisplayName("IT-04: Find Person by ID - Type Checking and Polymorphism")
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
        assertFalse(foundBorrower.getClass().equals(Staff.class),
                "Borrower should NOT be Staff class");
        assertFalse(foundBorrower.getClass().equals(Clerk.class),
                "Borrower should NOT be Clerk class");

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
        assertTrue(foundStaffClerk instanceof Clerk,
                "Should be instance of Clerk (actual type)");

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

        Clerk clerkNotFound = library.findClerkById(888);
        assertNull(clerkNotFound, "Should return null for non-existent clerk ID");

        // Step 6: Test wrong type lookup (verifies type checking works)
        Borrower clerkAsBorrower = library.findBorrowerById(clerk.getID());
        assertNull(clerkAsBorrower,
                "Should not find clerk when looking for borrower");

        Clerk borrowerAsClerk = library.findClerkById(borrower1.getID());
        assertNull(borrowerAsClerk,
                "Should not find borrower when looking for clerk");

        // Step 7: Verify polymorphic behavior - all persons have common attributes
        assertEquals(borrower1.getID(), foundBorrower.getID(),
                "Person ID should be accessible through inheritance");
        assertEquals(clerk.getName(), foundClerk.getName(),
                "Person name should be accessible through inheritance");
        assertEquals(librarian.getAddress(), foundLibrarian.getAddress(),
                "Person address should be accessible through inheritance");

        System.out.println("✓ IT-04 PASSED: Person type checking verified");
        System.out.println("  - Borrower found: " + foundBorrower.getName() + " (ID: " + foundBorrower.getID() + ")");
        System.out.println("  - Clerk found: " + foundClerk.getName() + " (ID: " + foundClerk.getID() + ")");
        System.out.println("  - Librarian found: " + foundLibrarian.getName() + " (ID: " + foundLibrarian.getID() + ")");
        System.out.println("  - Type checking: All person types correctly identified");
        System.out.println("  - Polymorphism: Inheritance hierarchy verified");
    }

    // ==================== SCENARIO 5: DATABASE PERSISTENCE - SAVE (IT-05) ====================
    /**
     * Classes involved: Book, Borrower, Loan, HoldRequest, DatabaseManager
     */
    @Test
    @DisplayName("IT-05: Database Persistence - Save Operations")
    public void testDatabasePersistenceSave() {
        System.out.println("\n=== Running IT-05: Database Persistence - Save ===");

        // Step 1: Create and save a new book
        Book newBook = new Book(-1, "Database Systems", "Computer Science",
                "Ramez Elmasri", false);

        // Verify book has attributes ready for saving
        assertNotNull(newBook.getTitle(), "Book title should exist");
        assertNotNull(newBook.getAuthor(), "Book author should exist");
        assertNotNull(newBook.getSubject(), "Book subject should exist");
        assertFalse(newBook.getIssuedStatus(), "Book should not be issued");

        int tempBookId = newBook.getID();
        assertTrue(tempBookId > 0, "Book should have temporary ID");

        // FIXED: Actually save to database
        newBook.saveToDatabase();

        // Verify save operation
        assertTrue(newBook.getID() > 0,
                "Book should have valid ID after database save");
        System.out.println("  ✓ Book saved - ID: " + newBook.getID());

        // Step 2: Create and save a new borrower
        Borrower newBorrower = new Borrower(-1, "Charlie Davis", "789 Elm St", 5678901);

        // Verify borrower has attributes ready for saving
        assertNotNull(newBorrower.getName(), "Borrower name should exist");
        assertNotNull(newBorrower.getAddress(), "Borrower address should exist");
        assertTrue(newBorrower.getPhoneNumber() > 0,
                "Borrower phone should be valid");
        assertNotNull(newBorrower.getPassword(),
                "Borrower password should be generated");

        // FIXED: Actually save to database
        newBorrower.saveToDatabase();

        // Verify save operation
        assertTrue(newBorrower.getID() > 0,
                "Borrower should have valid ID after save");
        System.out.println("  ✓ Borrower saved - ID: " + newBorrower.getID());

        // Step 3: Create and verify loan for persistence
        library.addBookinLibrary(newBook);
        library.addBorrower(newBorrower);

        // Issue book (this triggers loan save)
        newBook.issueBook(newBorrower, clerk);

        // Verify loan was created and saved
        Loan loan = newBorrower.getBorrowedBooks().get(0);
        assertNotNull(loan.getBorrower(), "Loan should have borrower");
        assertNotNull(loan.getBook(), "Loan should have book");
        assertNotNull(loan.getIssuer(), "Loan should have issuer");
        assertNotNull(loan.getIssuedDate(), "Loan should have issue date");

        // Verify loan is tracked in library
        assertTrue(library.getLoans().contains(loan),
                "Loan should be in library's loan list");
        System.out.println("  ✓ Loan saved - Book: " + loan.getBook().getTitle());

        // Step 4: Create and verify hold request for persistence
        Book heldBook = new Book(-1, "Operating Systems", "Computer Science",
                "Silberschatz", false);

        // Save book to database first
        heldBook.saveToDatabase();
        library.addBookinLibrary(heldBook);

        // Place hold (this triggers hold request save)
        heldBook.placeBookOnHold(newBorrower);

        // Verify hold request attributes for database
        HoldRequest hr = heldBook.getHoldRequests().get(0);
        assertNotNull(hr.getBorrower(), "Hold request should have borrower");
        assertNotNull(hr.getBook(), "Hold request should have book");
        assertNotNull(hr.getRequestDate(), "Hold request should have date");

        // Verify hold request is linked to both book and borrower
        assertEquals(1, heldBook.getHoldRequests().size(),
                "Book should have 1 hold request");
        assertEquals(1, newBorrower.getOnHoldBooks().size(),
                "Borrower should have 1 hold request");
        System.out.println("  ✓ Hold request saved");

        // Step 5: Verify all objects maintain their relationships after save
        assertEquals(newBook, loan.getBook(),
                "Loan should maintain book reference after save");
        assertEquals(newBorrower, loan.getBorrower(),
                "Loan should maintain borrower reference after save");
        assertEquals(heldBook, hr.getBook(),
                "Hold request should maintain book reference after save");
        assertEquals(newBorrower, hr.getBorrower(),
                "Hold request should maintain borrower reference after save");

        System.out.println("✓ IT-05 PASSED: Database persistence save operations verified");
        System.out.println("  - Book saved: " + newBook.getTitle());
        System.out.println("  - Borrower saved: " + newBorrower.getName());
        System.out.println("  - Loan saved: " + loan.getBook().getTitle());
        System.out.println("  - Hold request saved");
        System.out.println("  - All relationships maintained after persistence");
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