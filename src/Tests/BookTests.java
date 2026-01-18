package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Unit tests for Book class
 *
 * Total: 20 tests
 *
 * @author Onejda
 */
public class BookTests {

    private Book book1;
    private Book book2;
    private Borrower borrower1;
    private Borrower borrower2;
    private DatabaseManager dbManager;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // NOTE:
    // Some Book methods depend on database interaction.
    // A lightweight database connection is initialized to allow
    // these methods to execute correctly during unit testing.

    @BeforeEach
    public void setUp() {
        // Reset static counters
        Book.setIDCount(0);
        Person.setIDCount(0);

        // Initialize database connection for methods that need it
        // This creates an in-memory test database
        dbManager = DatabaseManager.getInstance();
        dbManager.connect(); // Creates database/library.db

        // Create test books
        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", true);

        // Create test borrowers
        borrower1 = new Borrower(1, "Alice", "123 Main St", 1234567890);
        borrower2 = new Borrower(2, "Bob", "456 Oak Ave", 987654);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        // Close database connection
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        Book.setIDCount(0);
        Person.setIDCount(0);
    }

    // ==================== Constructor Tests (5 tests) ====================

    @Test
    @DisplayName("Constructor - Creates book with provided ID")
    public void testConstructor_WithProvidedId_SetsCorrectly() {
        // Arrange & Act
        Book book = new Book(10, "Test Book", "Test Subject", "Test Author", false);

        // Assert
        assertEquals(10, book.getID(), "Book ID should match provided ID");
        assertEquals("Test Book", book.getTitle(), "Title should be set correctly");
        assertEquals("Test Subject", book.getSubject(), "Subject should be set correctly");
        assertEquals("Test Author", book.getAuthor(), "Author should be set correctly");
        assertFalse(book.getIssuedStatus(), "Issued status should be false");
    }

    @Test
    @DisplayName("Constructor - Auto-generates ID when -1 is provided")
    public void testConstructor_WithMinusOne_AutoGeneratesId() {
        // Arrange
        Book.setIDCount(5);

        // Act
        Book book = new Book(-1, "Auto ID Book", "Subject", "Author", false);

        // Assert
        assertEquals(6, book.getID(), "Should auto-generate ID as 6 (currentIdNumber + 1)");
    }

    @Test
    @DisplayName("Constructor - Creates book with issued status true")
    public void testConstructor_IssuedBook_StatusSetCorrectly() {
        // Act
        Book book = new Book(5, "Issued Book", "Subject", "Author", true);

        // Assert
        assertTrue(book.getIssuedStatus(), "Issued status should be true");
    }

    @Test
    @DisplayName("Constructor - Should reject negative ID values (EXPECTED TO FAIL)")
    void testConstructor_NegativeId_ShouldBeRejected() {
        Book book = new Book(-5, "Invalid Book", "Invalid", "Invalid", false);

        assertTrue(
                book.getID() >= 0,
                "Negative book IDs should not be accepted"
        );
    }

    @Test
    @DisplayName("Constructor - Should reject null title (EXPECTED TO FAIL)")
    void testConstructor_NullTitle_ShouldBeRejected() {
        Book book = new Book(1, null, "SE", "Author", false);

        assertNotNull(
                book.getTitle(),
                "Book title should not be null"
        );
    }


    // ==================== Getter Methods Tests (5 tests) ====================

    @Test
    @DisplayName("getTitle - Returns correct title")
    public void testGetTitle_ReturnsCorrectTitle() {
        // Act & Assert
        assertEquals("Clean Code", book1.getTitle(), "Should return correct book title");
    }

    @Test
    @DisplayName("getAuthor - Returns correct author")
    public void testGetAuthor_ReturnsCorrectAuthor() {
        // Act & Assert
        assertEquals("Robert Martin", book1.getAuthor(), "Should return correct author");
    }

    @Test
    @DisplayName("getSubject - Returns correct subject")
    public void testGetSubject_ReturnsCorrectSubject() {
        // Act & Assert
        assertEquals("Software Engineering", book1.getSubject(), "Should return correct subject");
    }

    @Test
    @DisplayName("getID - Returns correct book ID")
    public void testGetID_ReturnsCorrectId() {
        // Act & Assert
        assertEquals(1, book1.getID(), "Should return correct book ID");
    }

    @Test
    @DisplayName("getIssuedStatus - Returns correct issued status")
    public void testGetIssuedStatus_ReturnsCorrectStatus() {
        // Act & Assert
        assertFalse(book1.getIssuedStatus(), "Book1 should not be issued");
        assertTrue(book2.getIssuedStatus(), "Book2 should be issued");
    }

    // ==================== setIssuedStatus() Tests (3 tests) ====================

    @Test
    @DisplayName("setIssuedStatus - Changes status from false to true")
    public void testSetIssuedStatus_FromFalseToTrue_ChangesStatus() {
        // Arrange
        assertFalse(book1.getIssuedStatus(), "Initial status should be false");

        // Act
        book1.setIssuedStatus(true);

        // Assert
        assertTrue(book1.getIssuedStatus(), "Status should be changed to true");
    }

    @Test
    @DisplayName("setIssuedStatus - Changes status from true to false")
    public void testSetIssuedStatus_FromTrueToFalse_ChangesStatus() {
        // Arrange
        assertTrue(book2.getIssuedStatus(), "Initial status should be true");

        // Act
        book2.setIssuedStatus(false);

        // Assert
        assertFalse(book2.getIssuedStatus(), "Status should be changed to false");
    }

    @Test
    @DisplayName("setIssuedStatus - Can be called multiple times")
    public void testSetIssuedStatus_MultipleCalls_UpdatesCorrectly() {
        // Act & Assert
        book1.setIssuedStatus(true);
        assertTrue(book1.getIssuedStatus(), "Should be true after first set");

        book1.setIssuedStatus(false);
        assertFalse(book1.getIssuedStatus(), "Should be false after second set");

        book1.setIssuedStatus(true);
        assertTrue(book1.getIssuedStatus(), "Should be true after third set");
    }

    // ==================== getHoldRequests() Tests (3 tests) ====================

    @Test
    @DisplayName("getHoldRequests - Returns empty list initially")
    public void testGetHoldRequests_InitiallyEmpty() {
        // Act
        ArrayList<HoldRequest> requests = book1.getHoldRequests();

        // Assert
        assertNotNull(requests, "Hold requests list should not be null");
        assertTrue(requests.isEmpty(), "Hold requests should be empty initially");
    }

    @Test
    @DisplayName("getHoldRequests - Returns list after adding hold request")
    public void testGetHoldRequests_AfterAddingRequest_ReturnsNonEmpty() {
        // Arrange
        HoldRequest hr = new HoldRequest(borrower1, book1, new Date());
        book1.getHoldRequestOperations().addHoldRequest(hr);

        // Act
        ArrayList<HoldRequest> requests = book1.getHoldRequests();

        // Assert
        assertFalse(requests.isEmpty(), "Hold requests should not be empty");
        assertEquals(1, requests.size(), "Should have exactly 1 hold request");
    }

    @Test
    @DisplayName("getHoldRequests - Returns list with multiple requests")
    public void testGetHoldRequests_MultipleRequests_ReturnsAll() {
        // Arrange
        HoldRequest hr1 = new HoldRequest(borrower1, book1, new Date());
        HoldRequest hr2 = new HoldRequest(borrower2, book1, new Date());
        book1.getHoldRequestOperations().addHoldRequest(hr1);
        book1.getHoldRequestOperations().addHoldRequest(hr2);

        // Act
        ArrayList<HoldRequest> requests = book1.getHoldRequests();

        // Assert
        assertEquals(2, requests.size(), "Should have 2 hold requests");
    }

    // ==================== printHoldRequests() Tests (2 tests) ====================

    @Test
    @DisplayName("printHoldRequests - Displays message when no requests")
    public void testPrintHoldRequests_NoRequests_DisplaysMessage() {
        // Arrange
        System.setOut(new PrintStream(outContent));

        // Act
        book1.printHoldRequests();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("No Hold Requests"),
                "Should display 'No Hold Requests' message");
    }

    @Test
    @DisplayName("printHoldRequests - Displays requests when they exist")
    public void testPrintHoldRequests_WithRequests_DisplaysRequests() {
        // Arrange
        HoldRequest hr = new HoldRequest(borrower1, book1, new Date());
        book1.getHoldRequestOperations().addHoldRequest(hr);
        System.setOut(new PrintStream(outContent));

        // Act
        book1.printHoldRequests();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Hold Requests are:"),
                "Should display hold requests header");
        assertTrue(output.contains("No."), "Should display column headers");
    }

    // ==================== printInfo() Test (1 test) ====================

    @Test
    @DisplayName("printInfo - Displays book information correctly")
    public void testPrintInfo_DisplaysCorrectInformation() {
        // Arrange
        System.setOut(new PrintStream(outContent));

        // Act
        book1.printInfo();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Clean Code"), "Should display title");
        assertTrue(output.contains("Robert Martin"), "Should display author");
        assertTrue(output.contains("Software Engineering"), "Should display subject");
    }

    // ==================== Static ID Counter Tests (1 test) ====================

    @Test
    @DisplayName("setIDCount and getIDCount - Work together correctly")
    public void testIdCounter_SetAndGet_WorkTogether() {
        // Arrange & Act
        Book.setIDCount(100);

        // Assert
        assertEquals(100, Book.getIDCount(), "Should return the set ID count");

        // Create new book with -1 to test auto-increment
        Book newBook = new Book(-1, "New Book", "Subject", "Author", false);
        assertEquals(101, newBook.getID(), "New book should have ID 101");
    }
}