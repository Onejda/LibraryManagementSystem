package Tests.Unit;

import LMS.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Tests: findClerkById, findBookById, getHoldRequestExpiry, viewAllBooks
 */
public class LibraryTests_Onejda {

    private Library lib;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        // Reset singleton and create fresh library instance
        Library.resetInstance();
        lib = Library.getInstance();

        // Set up test data manually without database
        lib.setName("Test Library");

        // Create test persons directly
        Clerk clerk1 = new Clerk(2, "Jane Doe", "Front Desk", 5552345, 25000, 1);
        Borrower borrower1 = new Borrower(4, "Alice Brown", "123 Student Ave", 5554567);
        Librarian librarian = new Librarian(1, "Admin", "Library Office", 5550000, 50000, 101);

        // Add to library WITHOUT database
        Library.persons.add(clerk1);
        Library.persons.add(borrower1);
        Library.librarian = librarian;

        // Create test books directly
        Book book1 = new Book(1, "Clean Code", "Software Engineering", "Robert C. Martin", false);
        Book book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
        Book book3 = new Book(3, "Database Systems", "Databases", "Ramez Elmasri", true);

        // Add books to library WITHOUT database
        lib.addBookinLibrary(book1);
        lib.addBookinLibrary(book2);
        lib.addBookinLibrary(book3);

        // Capture console output for viewAllBooks test
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);

        // Reset library instance
        Library.resetInstance();
    }

    // ==================== findClerkById() Tests ====================

    @Test
    public void testFindClerkById_ValidClerk() {
        // Test finding an existing clerk
        Clerk clerk = lib.findClerkById(2);

        assertNotNull("Should find clerk with ID 2", clerk);
        assertEquals(2, clerk.getID());
        assertTrue("Returned object should be instance of Clerk", clerk instanceof Clerk);
        assertEquals("Jane Doe", clerk.getName());
    }

    @Test
    public void testFindClerkById_NonExistentId() {
        // Test with ID that doesn't exist
        Clerk clerk = lib.findClerkById(999);

        assertNull("Should return null for non-existent ID", clerk);
    }

    @Test
    public void testFindClerkById_LibrarianId() {
        // Test with librarian ID (should return null, not a clerk)
        Clerk clerk = lib.findClerkById(1);

        assertNull("Librarian ID should not return a Clerk object", clerk);
    }

    @Test
    public void testFindClerkById_BorrowerId() {
        // Test with borrower ID (should return null)
        Clerk clerk = lib.findClerkById(4);

        assertNull("Borrower ID should not return a Clerk object", clerk);
    }

    // ==================== findBookById() Tests ====================

    @Test
    public void testFindBookById_ValidBook() {
        // Test finding an existing book
        Book book = lib.findBookById(1);

        assertNotNull("Should find book with ID 1", book);
        assertEquals(1, book.getID());
        assertEquals("Clean Code", book.getTitle());
    }

    @Test
    public void testFindBookById_NonExistentId() {
        // Test with ID that doesn't exist
        Book book = lib.findBookById(999);

        assertNull("Should return null for non-existent ID", book);
    }

    @Test
    public void testFindBookById_NegativeId() {
        // Test with invalid negative ID
        Book book = lib.findBookById(-1);

        assertNull("Should return null for negative ID", book);
    }

    // ==================== getHoldRequestExpiry() Tests ====================

    @Test
    public void testGetHoldRequestExpiry() {
        // Test that getter returns the correct value after setting
        lib.setRequestExpiry(14);
        assertEquals(14, lib.getHoldRequestExpiry());
    }

    // ==================== viewAllBooks() Tests ====================

    @Test
    public void testViewAllBooks_WithBooks() {
        // Test that method runs without errors when books exist
        try {
            lib.viewAllBooks();

            // Verify output contains expected information
            String output = outContent.toString();
            assertTrue("Output should contain 'Books are'", output.contains("Books are"));
        } catch (Exception e) {
            fail("viewAllBooks should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testViewAllBooks_EmptyLibrary() {
        // Test with no books
        Library.resetInstance();
        Library emptyLib = Library.getInstance();

        // Reset output stream for new library
        outContent.reset();

        try {
            emptyLib.viewAllBooks();
            // Verify output indicates no books
            String output = outContent.toString();
            assertTrue("Output should contain 'no books' message", output.toLowerCase().contains("no books"));

        } catch (Exception e) {
            fail("viewAllBooks should handle empty library: " + e.getMessage());
        }
    }

    // ==================== findClerk() Tests ====================

    @Test
    public void testFindClerk_WithInvalidTextInput() {
        // Edge case: user enters non-numeric input
        String input = "abc\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Clerk clerk = lib.findClerk();

        assertNull("Should return null for invalid input", clerk);

        System.setIn(System.in);
    }

}