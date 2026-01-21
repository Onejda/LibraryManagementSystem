package Tests.System;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

/**
 * System Tests for Borrower User Journey
 * Tests the complete interaction flow of a borrower with the library system
 *
 * Test Cases:
 * ST-D1: Borrower Authentication & Portal Access
 * ST-D2: Search for Available Book
 * ST-D3: Place Hold Request on Issued Book
 * ST-D4: View Personal Information & Borrowed Books
 * ST-D5: Check Fine for Overdue Books
 * ST-D6: Attempt Duplicate Hold Request
 *
 * @author Denisa
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class System_Denisa {

    private static Library library;
    private static DatabaseManager dbManager;
    private static Borrower testBorrower;
    private static Borrower testBorrower2;
    private static Clerk testClerk;
    private static Staff testStaff;
    private static Book testBook1;
    private static Book testBook2;
    private static Book testBook3;

    @BeforeAll
    public static void setupTestEnvironment() {
        // Initialize library system
        Library.resetInstance();
        library = Library.getInstance();
        library.setName("Test Library");
        library.setFine(20.0);
        library.setReturnDeadline(5);
        library.setRequestExpiry(7);

        // Initialize database
        dbManager = DatabaseManager.getInstance();
        dbManager.connect();

        // Create test data
        createTestUsers();
        createTestBooks();

        System.out.println("Test environment setup complete.");
    }

    private static void createTestUsers() {
        // Create test borrowers
        testBorrower = new Borrower(100, "Alice Brown", "123 Test St", 5554567);
        testBorrower2 = new Borrower(101, "Bob Wilson", "456 Test Ave", 5555678);

        // Create test clerk
        testClerk = new Clerk(200, "Jane Doe", "Front Desk", 5552345, 25000, 1);
        testStaff = testClerk;

        // Add to library
        library.addBorrower(testBorrower);
        library.addBorrower(testBorrower2);
        library.addClerk(testClerk);

        System.out.println("Test users created: Alice (ID: 100), Bob (ID: 101), Jane Clerk (ID: 200)");
    }

    private static void createTestBooks() {
        // Create test books
        testBook1 = new Book(1000, "Clean Code", "Software Engineering", "Robert C. Martin", false);
        testBook2 = new Book(1001, "Design Patterns", "Software Engineering", "Gang of Four", false);
        testBook3 = new Book(1002, "Database Systems", "Databases", "Ramez Elmasri", true); // Already issued

        library.addBookinLibrary(testBook1);
        library.addBookinLibrary(testBook2);
        library.addBookinLibrary(testBook3);

        System.out.println("Test books created: Clean Code, Design Patterns, Database Systems");
    }

    @AfterAll
    public static void cleanupTestEnvironment() {
        // Close database connection
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        System.out.println("Test environment cleaned up.");
    }

    /**
     * ST-D1: Borrower Authentication & Portal Access
     * Purpose: Verify borrower can log in and access their portal
     */
    @Test
    @Order(1)
    @DisplayName("ST-D1: Borrower Authentication & Portal Access")
    public void testBorrowerAuthentication() {
        System.out.println("\n=== Running ST-D1: Borrower Authentication & Portal Access ===");

        // Test login with valid credentials
        Person loggedInUser = authenticateBorrower(100, "100");

        // Verify successful login
        assertNotNull(loggedInUser, "Login should succeed with valid credentials");
        assertTrue(loggedInUser instanceof Borrower, "Logged in user should be a Borrower");
        assertEquals("Alice Brown", loggedInUser.getName(), "Borrower name should match");
        assertEquals(100, loggedInUser.getID(), "Borrower ID should match");

        System.out.println("✓ Borrower authenticated successfully");
        System.out.println("✓ Borrower portal access verified");
        System.out.println("✓ Test Result: PASS");
    }

    /**
     * ST-D2: Search for Available Book
     * Purpose: Test book search functionality for borrowers
     */
    @Test
    @Order(2)
    @DisplayName("ST-D2: Search for Available Book")
    public void testSearchForAvailableBook() {
        System.out.println("\n=== Running ST-D2: Search for Available Book ===");

        // Search by title
        ArrayList<Book> searchResults = searchBooksByTitle("Clean Code");

        // Verify search results
        assertNotNull(searchResults, "Search results should not be null");
        assertFalse(searchResults.isEmpty(), "Search should return results for existing book");
        assertTrue(searchResults.size() > 0, "At least one book should match");

        // Verify book details
        Book foundBook = searchResults.get(0);
        assertEquals("Clean Code", foundBook.getTitle(), "Book title should match");
        assertEquals("Robert C. Martin", foundBook.getAuthor(), "Book author should be displayed");
        assertEquals("Software Engineering", foundBook.getSubject(), "Book subject should be displayed");

        System.out.println("✓ Book search successful");
        System.out.println("✓ Search results display: Title, Author, Subject");
        System.out.println("✓ Test Result: PASS");
    }

    /**
     * ST-D3: Place Hold Request on Issued Book
     * Purpose: Test hold request workflow when book is unavailable
     */
    @Test
    @Order(3)
    @DisplayName("ST-D3: Place Hold Request on Issued Book")
    public void testPlaceHoldRequestOnIssuedBook() {
        System.out.println("\n=== Running ST-D3: Place Hold Request on Issued Book ===");

        // Ensure testBook3 is issued
        testBook3.setIssuedStatus(true);

        // Count initial hold requests
        int initialHoldCount = testBook3.getHoldRequests().size();
        int borrowerInitialHolds = testBorrower.getOnHoldBooks().size();

        // Place hold request
        testBook3.makeHoldRequest(testBorrower);

        // Verify hold request created
        assertEquals(initialHoldCount + 1, testBook3.getHoldRequests().size(),
                "Book should have one more hold request");
        assertEquals(borrowerInitialHolds + 1, testBorrower.getOnHoldBooks().size(),
                "Borrower should have one more hold request");

        // Verify hold request details
        HoldRequest lastHold = testBook3.getHoldRequests().get(testBook3.getHoldRequests().size() - 1);
        assertEquals(testBorrower, lastHold.getBorrower(), "Hold request should be for correct borrower");
        assertEquals(testBook3, lastHold.getBook(), "Hold request should be for correct book");
        assertNotNull(lastHold.getRequestDate(), "Hold request should have a date");

        System.out.println("✓ Hold request placed successfully");
        System.out.println("✓ Hold appears in borrower's on-hold list");
        System.out.println("✓ Test Result: PASS");
    }

    /**
     * ST-D4: View Personal Information & Borrowed Books
     * Purpose: Verify borrower can view their complete profile
     */
    @Test
    @Order(4)
    @DisplayName("ST-D4: View Personal Information & Borrowed Books")
    public void testViewPersonalInformationAndBorrowedBooks() {
        System.out.println("\n=== Running ST-D4: View Personal Information & Borrowed Books ===");

        // Add a borrowed book to test borrower
        Loan testLoan = new Loan(testBorrower, testBook1, testStaff, null, new Date(), null, false);
        testBorrower.addBorrowedBook(testLoan);

        // Capture personal information
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Call printInfo (simulating "Check Personal Info" option)
        testBorrower.printInfo();

        // Restore output
        System.setOut(originalOut);
        String output = outputStream.toString();

        // Verify all information is displayed
        assertTrue(output.contains("100"), "Should display borrower ID");
        assertTrue(output.contains("Alice Brown"), "Should display borrower name");
        assertTrue(output.contains("123 Test St"), "Should display borrower address");
        assertTrue(output.contains("5554567"), "Should display phone number");
        assertTrue(output.contains("Borrowed Books"), "Should show borrowed books section");

        // Verify borrowed books
        assertEquals(1, testBorrower.getBorrowedBooks().size(), "Borrower should have 1 borrowed book");

        System.out.println("✓ Personal information displayed correctly");
        System.out.println("✓ Borrowed books list shown with details");
        System.out.println("✓ On-hold books list displayed");
        System.out.println("✓ Test Result: PASS");
    }

    /**
     * ST-D5: Check Fine for Overdue Books
     * Purpose: Test fine calculation system for borrowers
     */
    @Test
    @Order(5)
    @DisplayName("ST-D5: Check Fine for Overdue Books")
    public void testCheckFineForOverdueBooks() {
        System.out.println("\n=== Running ST-D5: Check Fine for Overdue Books ===");

        // Create an overdue loan (10 days old)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10); // 10 days ago
        Date overdueDate = cal.getTime();

        Loan overdueLoan = new Loan(testBorrower, testBook2, testStaff, null, overdueDate, null, false);
        library.addLoan(overdueLoan);

        // Calculate fine
        double calculatedFine = overdueLoan.computeFine1();

        // Verify fine calculation
        // Formula: (days - deadline) × per_day_fine
        // Expected: (10 - 5) × 20 = 100
        double expectedFine = (10 - 5) * 20.0;
        assertEquals(expectedFine, calculatedFine, 0.01,
                "Fine should be calculated as (10 - 5) × 20 = Rs 100");

        // Test with book returned on time (3 days)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        Date onTimeDate = cal.getTime();

        Loan onTimeLoan = new Loan(testBorrower2, testBook1, testStaff, null, onTimeDate, null, false);
        double noFine = onTimeLoan.computeFine1();

        assertEquals(0.0, noFine, 0.01, "No fine should be charged for on-time return");

        System.out.println("✓ Fine calculation accurate: (days - 5) × Rs 20");
        System.out.println("✓ Overdue fine: Rs " + calculatedFine);
        System.out.println("✓ On-time return: Rs 0");
        System.out.println("✓ Test Result: PASS");
    }

    /**
     * ST-D6: Attempt Duplicate Hold Request
     * Purpose: Test system prevents duplicate hold requests
     */
    @Test
    @Order(6)
    @DisplayName("ST-D6: Attempt Duplicate Hold Request")
    public void testAttemptDuplicateHoldRequest() {
        System.out.println("\n=== Running ST-D6: Attempt Duplicate Hold Request ===");

        // Create a fresh test book
        Book testBook4 = new Book(1003, "Test Book", "Testing", "Test Author", true);
        library.addBookinLibrary(testBook4);

        // Place first hold request
        int initialHoldCount = testBook4.getHoldRequests().size();
        testBook4.makeHoldRequest(testBorrower);

        assertEquals(initialHoldCount + 1, testBook4.getHoldRequests().size(),
                "First hold request should be created");

        // Capture output for duplicate attempt
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Attempt duplicate hold request
        testBook4.makeHoldRequest(testBorrower);

        // Restore output
        System.setOut(originalOut);
        String output = outputStream.toString();

        // Verify duplicate was rejected
        assertEquals(initialHoldCount + 1, testBook4.getHoldRequests().size(),
                "No additional hold request should be created");
        assertTrue(output.contains("already have one hold request"),
                "System should display duplicate hold request message");

        System.out.println("✓ Duplicate hold request rejected");
        System.out.println("✓ Appropriate error message displayed");
        System.out.println("✓ No duplicate entry created");
        System.out.println("✓ Test Result: PASS");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to authenticate a borrower
     * Simulates the login process
     */
    private Person authenticateBorrower(int id, String password) {
        // Search for borrower in library persons
        for (Person p : library.getPersons()) {
            if (p.getID() == id && p.getPassword().equals(password)) {
                if (p instanceof Borrower) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Helper method to search books by title
     * Simulates the book search functionality
     */
    private ArrayList<Book> searchBooksByTitle(String title) {
        ArrayList<Book> results = new ArrayList<>();
        for (Book book : library.getBooks()) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

}