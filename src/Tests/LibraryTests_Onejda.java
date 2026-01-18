package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for Library class methods:
 * - findClerkById(int id)
 * - findStaffById(int id)
 * - findBookById(int id)
 * - viewAllBooks()
 * - setRequestExpiry(int hrExpiry)
 * - getHoldRequestExpiry()
 *
 *
 * @author Onejda
 */
public class LibraryTests_Onejda {

    private Library library;
    private Clerk clerk1;
    private Clerk clerk2;
    private Librarian librarian;
    private Borrower borrower;
    private Book book1;
    private Book book2;
    private Book book3;

    // For capturing console output
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        // Reset singleton and static counters
        Library.resetInstance();
        Person.setIDCount(0);
        Clerk.setDeskCount(0);
        Book.setIDCount(0);

        // Get fresh library instance
        library = Library.getInstance();
        library.setReturnDeadline(5);
        library.setFine(20.0);
        library.setRequestExpiry(7);

        // Create test data
        clerk1 = new Clerk(1, "John Doe", "123 Main St", 1234567890, 50000.0, 1);
        clerk2 = new Clerk(2, "Jane Smith", "456 Oak Ave", 987654, 55000.0, 2);
        librarian = new Librarian(3, "Dr. Brown", "789 Elm St", 1111111111, 80000.0, 1);
        borrower = new Borrower(4, "Alice Johnson", "321 Pine Rd", 222222);

        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
        book3 = new Book(3, "Introduction to Algorithms", "Algorithms", "Thomas Cormen", true);

        // Add to library
        library.addClerk(clerk1);
        library.addClerk(clerk2);
        Library.librarian = librarian;
        library.addBorrower(borrower);

        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        library.addBookinLibrary(book3);
    }

    @AfterEach
    public void tearDown() {
        Library.resetInstance();
        System.setOut(originalOut);
    }

    // ==================== findClerkById() Tests (4 tests) ====================

    @Test
    @DisplayName("findClerkById - Valid ID returns correct clerk")
    public void testFindClerkById_ValidId_ReturnsClerk() {
        // Act
        Clerk result = library.findClerkById(1);

        // Assert
        assertNotNull(result, "Clerk should be found");
        assertEquals(1, result.getID(), "Clerk ID should match");
        assertEquals("John Doe", result.getName(), "Clerk name should match");
    }

    @Test
    @DisplayName("findClerkById - Non-existent ID returns null")
    public void testFindClerkById_NonExistentId_ReturnsNull() {
        // Act
        Clerk result = library.findClerkById(999);

        // Assert
        assertNull(result, "Should return null for non-existent clerk ID");
    }

    @Test
    @DisplayName("findClerkById - Borrower ID returns null (wrong type)")
    public void testFindClerkById_BorrowerIdGiven_ReturnsNull() {
        // Act
        Clerk result = library.findClerkById(4); // Borrower ID

        // Assert
        assertNull(result, "Should return null when ID belongs to a Borrower");
    }

    @Test
    @DisplayName("findClerkById - Finds correct clerk when multiple exist")
    public void testFindClerkById_MultipleClerks_FindsCorrectOne() {
        // Act
        Clerk result = library.findClerkById(2);

        // Assert
        assertNotNull(result, "Should find the second clerk");
        assertEquals(2, result.getID(), "Should return correct clerk ID");
        assertEquals("Jane Smith", result.getName(), "Should return correct clerk name");
    }

    // ==================== findStaffById() Tests (4 tests) ====================

    @Test
    @DisplayName("findStaffById - Librarian ID returns Librarian")
    public void testFindStaffById_LibrarianId_ReturnsLibrarian() {
        // Act
        Staff result = library.findStaffById(3);

        // Assert
        assertNotNull(result, "Librarian should be found");
        assertEquals(3, result.getID(), "Librarian ID should match");
        assertTrue(result instanceof Librarian, "Result should be instance of Librarian");
    }

    @Test
    @DisplayName("findStaffById - Clerk ID returns Clerk")
    public void testFindStaffById_ClerkId_ReturnsClerk() {
        // Act
        Staff result = library.findStaffById(1);

        // Assert
        assertNotNull(result, "Clerk should be found");
        assertEquals(1, result.getID(), "Clerk ID should match");
        assertTrue(result instanceof Clerk, "Result should be instance of Clerk");
    }

    @Test
    @DisplayName("findStaffById - Borrower ID returns null (not Staff)")
    public void testFindStaffById_BorrowerId_ReturnsNull() {
        // Act
        Staff result = library.findStaffById(4);

        // Assert
        assertNull(result, "Should return null for Borrower ID (not a Staff)");
    }

    @Test
    @DisplayName("findStaffById - Non-existent ID returns null")
    public void testFindStaffById_NonExistentId_ReturnsNull() {
        // Act
        Staff result = library.findStaffById(999);

        // Assert
        assertNull(result, "Should return null for non-existent staff ID");
    }

    // ==================== findBookById() Tests (5 tests) ====================

    @Test
    @DisplayName("findBookById - Valid ID returns correct book")
    public void testFindBookById_ValidId_ReturnsBook() {
        // Act
        Book result = library.findBookById(1);

        // Assert
        assertNotNull(result, "Book should be found");
        assertEquals(1, result.getID(), "Book ID should match");
        assertEquals("Clean Code", result.getTitle(), "Book title should match");
    }

    @Test
    @DisplayName("findBookById - Non-existent ID returns null")
    public void testFindBookById_NonExistentId_ReturnsNull() {
        // Act
        Book result = library.findBookById(999);

        // Assert
        assertNull(result, "Should return null for non-existent book ID");
    }

    @Test
    @DisplayName("findBookById - Finds issued book correctly")
    public void testFindBookById_IssuedBook_ReturnsBook() {
        // Act
        Book result = library.findBookById(3); // book3 is issued

        // Assert
        assertNotNull(result, "Should find issued book");
        assertEquals(3, result.getID(), "Book ID should match");
        assertTrue(result.getIssuedStatus(), "Book should be marked as issued");
    }

    @Test
    @DisplayName("findBookById - Finds available book correctly")
    public void testFindBookById_AvailableBook_ReturnsBook() {
        // Act
        Book result = library.findBookById(1); // book1 is not issued

        // Assert
        assertNotNull(result, "Should find available book");
        assertFalse(result.getIssuedStatus(), "Book should be marked as available");
    }

    @Test
    @DisplayName("findBookById - Empty library returns null")
    public void testFindBookById_EmptyBooksList_ReturnsNull() {
        // Arrange
        Library.resetInstance();
        library = Library.getInstance();

        // Act
        Book result = library.findBookById(1);

        // Assert
        assertNull(result, "Should return null when no books exist");
    }

    // ==================== viewAllBooks() Tests (3 tests) ====================

    @Test
    @DisplayName("viewAllBooks - Displays all books when books exist")
    public void testViewAllBooks_BooksExist_DisplaysBooks() {
        // Arrange
        System.setOut(new PrintStream(outContent));

        // Act
        library.viewAllBooks();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Books are:"), "Should display books header");
        assertTrue(output.contains("Clean Code"), "Should display first book");
        assertTrue(output.contains("Design Patterns"), "Should display second book");
        assertTrue(output.contains("Introduction to Algorithms"), "Should display third book");
    }

    @Test
    @DisplayName("viewAllBooks - Shows message when library is empty")
    public void testViewAllBooks_EmptyLibrary_DisplaysMessage() {
        // Arrange
        Library.resetInstance();
        library = Library.getInstance();
        System.setOut(new PrintStream(outContent));

        // Act
        library.viewAllBooks();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Currently, Library has no books."),
                "Should display 'no books' message");
    }

    @Test
    @DisplayName("viewAllBooks - Displays table headers and formatting")
    public void testViewAllBooks_WithBooks_DisplaysProperFormat() {
        // Arrange
        System.setOut(new PrintStream(outContent));

        // Act
        library.viewAllBooks();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Title"), "Should display 'Title' header");
        assertTrue(output.contains("Author"), "Should display 'Author' header");
        assertTrue(output.contains("Subject"), "Should display 'Subject' header");
        assertTrue(output.contains("0-"), "Should display index for first book");
    }

    // ==================== setRequestExpiry() Tests (3 tests) ====================

    @Test
    @DisplayName("setRequestExpiry - Sets valid expiry value correctly")
    public void testSetRequestExpiry_ValidValue_SetsCorrectly() {
        // Arrange
        int expectedExpiry = 14;

        // Act
        library.setRequestExpiry(expectedExpiry);

        // Assert
        assertEquals(expectedExpiry, library.getHoldRequestExpiry(),
                "Hold request expiry should be set correctly");
    }

    @Test
    @DisplayName("setRequestExpiry - Updates previous value")
    public void testSetRequestExpiry_UpdateExisting_OverwritesPrevious() {
        // Arrange
        library.setRequestExpiry(7);

        // Act
        library.setRequestExpiry(10);

        // Assert
        assertEquals(10, library.getHoldRequestExpiry(),
                "Should overwrite previous expiry value");
    }

    @Test
    @DisplayName("setRequestExpiry - Handles boundary value (zero)")
    public void testSetRequestExpiry_ZeroValue_SetsToZero() {
        // Act
        library.setRequestExpiry(0);

        // Assert
        assertEquals(0, library.getHoldRequestExpiry(),
                "Should accept zero as valid expiry");
    }

    // ==================== getHoldRequestExpiry() Tests (3 tests) ====================

    @Test
    @DisplayName("getHoldRequestExpiry - Returns value after setting")
    public void testGetHoldRequestExpiry_AfterSetting_ReturnsCorrectValue() {
        // Arrange
        int expectedExpiry = 21;
        library.setRequestExpiry(expectedExpiry);

        // Act
        int result = library.getHoldRequestExpiry();

        // Assert
        assertEquals(expectedExpiry, result,
                "Should return the expiry value that was set");
    }

    @Test
    @DisplayName("getHoldRequestExpiry - Returns default value from setup")
    public void testGetHoldRequestExpiry_DefaultValue_Returns7() {
        // Act
        int result = library.getHoldRequestExpiry();

        // Assert
        assertEquals(7, result,
                "Should return default value set in setUp (7 days)");
    }

    @Test
    @DisplayName("getHoldRequestExpiry - Setter and getter work together")
    public void testGetHoldRequestExpiry_WithSetter_Integration() {
        // Test multiple values
        int[] testValues = {1, 14, 30};

        for (int testValue : testValues) {
            // Act
            library.setRequestExpiry(testValue);
            int result = library.getHoldRequestExpiry();

            // Assert
            assertEquals(testValue, result,
                    "Getter should return value set by setter for: " + testValue);
        }
    }
}