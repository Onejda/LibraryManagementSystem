package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

/**
 * Unit tests for HoldRequest class
 *
 * Total: 7 tests
 * Focus: Constructor, getters, print method
 *
 * @author Onejda
 */
public class HoldRequestTests {

    private HoldRequest holdRequest;
    private Borrower borrower;
    private Book book;
    private Date requestDate;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        // Reset static counters
        Book.setIDCount(0);
        Person.setIDCount(0);

        // Create test data
        borrower = new Borrower(1, "Alice Johnson", "123 Main St", 1234567890);
        book = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        requestDate = new Date();

        // Create hold request
        holdRequest = new HoldRequest(borrower, book, requestDate);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        Book.setIDCount(0);
        Person.setIDCount(0);
    }

    // ==================== Constructor Test (1 test) ====================

    @Test
    @DisplayName("Constructor - Creates hold request with all fields set correctly")
    public void testConstructor_SetsAllFieldsCorrectly() {
        // Arrange
        Borrower testBorrower = new Borrower(2, "Bob Smith", "456 Oak Ave", 98765432);
        Book testBook = new Book(2, "Design Patterns", "Software", "GoF", false);
        Date testDate = new Date();

        // Act
        HoldRequest hr = new HoldRequest(testBorrower, testBook, testDate);

        // Assert
        assertEquals(testBorrower, hr.getBorrower(), "Borrower should be set correctly");
        assertEquals(testBook, hr.getBook(), "Book should be set correctly");
        assertEquals(testDate, hr.getRequestDate(), "Request date should be set correctly");
    }

    // ==================== Getter Methods Tests (3 tests) ====================

    @Test
    @DisplayName("getBorrower - Returns correct borrower")
    public void testGetBorrower_ReturnsCorrectBorrower() {
        // Act
        Borrower result = holdRequest.getBorrower();

        // Assert
        assertNotNull(result, "Borrower should not be null");
        assertEquals(borrower, result, "Should return the correct borrower");
        assertEquals("Alice Johnson", result.getName(), "Borrower name should match");
    }

    @Test
    @DisplayName("getBook - Returns correct book")
    public void testGetBook_ReturnsCorrectBook() {
        // Act
        Book result = holdRequest.getBook();

        // Assert
        assertNotNull(result, "Book should not be null");
        assertEquals(book, result, "Should return the correct book");
        assertEquals("Clean Code", result.getTitle(), "Book title should match");
    }

    @Test
    @DisplayName("getRequestDate - Returns correct date")
    public void testGetRequestDate_ReturnsCorrectDate() {
        // Act
        Date result = holdRequest.getRequestDate();

        // Assert
        assertNotNull(result, "Request date should not be null");
        assertEquals(requestDate, result, "Should return the correct request date");
    }

    // ==================== print() Method Tests (2 tests) ====================

    @Test
    @DisplayName("print - Displays hold request information correctly")
    public void testPrint_DisplaysCorrectInformation() {
        // Arrange
        System.setOut(new PrintStream(outContent));

        // Act
        holdRequest.print();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Clean Code"), "Output should contain book title");
        assertTrue(output.contains("Alice Johnson"), "Output should contain borrower name");
        assertFalse(output.isEmpty(), "Output should not be empty");
    }

    // ==================== Object Integrity Tests (2 tests) ====================

    @Test
    @DisplayName("HoldRequest - Maintains references to same objects")
    public void testHoldRequest_MaintainsObjectReferences() {
        // Act
        Borrower retrievedBorrower = holdRequest.getBorrower();
        Book retrievedBook = holdRequest.getBook();

        // Assert
        assertSame(borrower, retrievedBorrower, "Should return same borrower instance");
        assertSame(book, retrievedBook, "Should return same book instance");
    }

    @Test
    @DisplayName("HoldRequest - Can be created with same book for different borrowers")
    public void testHoldRequest_SameBookDifferentBorrowers() {
        // Arrange
        Borrower borrower2 = new Borrower(2, "Bob Wilson", "789 Pine St", 555555);
        Date date2 = new Date();

        // Act
        HoldRequest hr1 = new HoldRequest(borrower, book, requestDate);
        HoldRequest hr2 = new HoldRequest(borrower2, book, date2);

        // Assert
        assertSame(book, hr1.getBook(), "First request should reference the book");
        assertSame(book, hr2.getBook(), "Second request should reference same book");
        assertNotSame(hr1.getBorrower(), hr2.getBorrower(), "Borrowers should be different");
    }
}