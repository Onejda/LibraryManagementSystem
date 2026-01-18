package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for core Library class behavior.
 *
 * Focus:
 * - ID-based lookup correctness
 * - Book retrieval
 * - Console output sanity
 * - Request expiry configuration
 *
 * NOTE:
 * Some tests are EXPECTED TO FAIL and intentionally reveal
 * missing validation in the current implementation.
 *
 * @author Onejda
 */
public class LibraryTests_Onejda {

    private Library library;
    private Clerk clerk;
    private Librarian librarian;
    private Borrower borrower;
    private Book book1;
    private Book book2;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        Library.resetInstance();
        Person.setIDCount(0);
        Clerk.setDeskCount(0);
        Book.setIDCount(0);

        library = Library.getInstance();
        library.setReturnDeadline(5);
        library.setRequestExpiry(7);

        clerk = new Clerk(1, "John Doe", "Main St", 123456, 40000, 1);
        librarian = new Librarian(2, "Dr Brown", "Office", 999999, 80000, 10);
        borrower = new Borrower(3, "Alice", "Elm St", 55555);

        Library.librarian = librarian;
        library.addClerk(clerk);
        library.addBorrower(borrower);

        book1 = new Book(1, "Clean Code", "SE", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "SE", "GoF", false);

        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        Library.resetInstance();
    }

    // ==================== findClerkById ====================

    @Test
    @DisplayName("findClerkById - Valid ID returns Clerk")
    public void testFindClerkById_Valid() {
        Clerk result = library.findClerkById(1);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("findClerkById - Non-existent ID returns null")
    public void testFindClerkById_Invalid() {
        assertNull(library.findClerkById(999));
    }

    // ==================== findStaffById ====================

    @Test
    @DisplayName("findStaffById - Librarian ID returns Librarian")
    public void testFindStaffById_Librarian() {
        Staff result = library.findStaffById(2);
        assertNotNull(result);
        assertTrue(result instanceof Librarian);
    }

    @Test
    @DisplayName("findStaffById - Non-existent ID returns null")
    void testFindStaffById_NonExistentId() {
        Staff result = library.findStaffById(999);
        assertNull(result, "Should return null for unknown ID");
    }

    @Test
    @DisplayName("findStaffById - Borrower ID returns null")
    void testFindStaffById_BorrowerId() {
        Staff result = library.findStaffById(borrower.getID());
        assertNull(result, "Borrower should not be treated as Staff");
    }


    // ==================== findBookById ====================

    @Test
    @DisplayName("findBookById - Valid ID returns Book")
    public void testFindBookById_Valid() {
        Book result = library.findBookById(1);
        assertNotNull(result);
        assertEquals("Clean Code", result.getTitle());
    }

    @Test
    @DisplayName("findBookById - Invalid ID returns null")
    public void testFindBookById_Invalid() {
        assertNull(library.findBookById(999));
    }

    // ==================== viewAllBooks ====================

    @Test
    @DisplayName("viewAllBooks - Displays books when library is not empty")
    public void testViewAllBooks_DisplaysBooks() {
        System.setOut(new PrintStream(outContent));
        library.viewAllBooks();
        String output = outContent.toString();

        assertTrue(output.contains("Clean Code"));
        assertTrue(output.contains("Design Patterns"));
    }

    // ==================== setRequestExpiry / getHoldRequestExpiry ====================

    @Test
    @DisplayName("setRequestExpiry - Valid value is stored correctly")
    public void testSetRequestExpiry_Valid() {
        library.setRequestExpiry(14);
        assertEquals(14, library.getHoldRequestExpiry());
    }

    @Test
    @DisplayName("setRequestExpiry - Updates previous value")
    public void testSetRequestExpiry_Overwrite() {
        library.setRequestExpiry(10);
        library.setRequestExpiry(21);
        assertEquals(21, library.getHoldRequestExpiry());
    }

    @Test
    @DisplayName("setRequestExpiry - should reject negative values (EXPECTED FAILURE)")
    public void testSetRequestExpiry_NegativeValue() {
        library.setRequestExpiry(-5);
        assertTrue(
                library.getHoldRequestExpiry() >= 0,
                "Negative expiry values should not be accepted"
        );
    }

}
