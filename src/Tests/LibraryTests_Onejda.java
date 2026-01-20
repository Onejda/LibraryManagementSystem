package Tests;

import LMS.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for Library class methods
 * Tests: findClerkById, findBookById, getHoldRequestExpiry, viewAllBooks
 * Note: findStaffById and setRequestExpiry already tested in Part 2 Analysis
 */
public class LibraryTests_Onejda {

    private Library lib;
    private DatabaseManager db;

    @Before
    public void setUp() {
        Library.resetInstance();
        lib = Library.getInstance();

        db = DatabaseManager.getInstance();
        db.connect();

        try {
            lib.populateLibrary(db.connect());
        } catch (IOException e) {
            fail("Setup failed: " + e.getMessage());
        }

        lib.setName("Test Library");
    }

    @After
    public void tearDown() {
        db.closeConnection();
        Library.resetInstance();
    }

    // ==================== findClerkById() Tests ====================

    @Test
    public void testFindClerkById_ValidClerk() {
        // Test finding an existing clerk
        Clerk clerk = lib.findClerkById(2);

        assertNotNull(clerk);
        assertEquals(2, clerk.getID());
        assertTrue(clerk instanceof Clerk);
    }

    @Test
    public void testFindClerkById_NonExistentId() {
        // Test with ID that doesn't exist
        Clerk clerk = lib.findClerkById(999);

        assertNull(clerk);
    }

    @Test
    public void testFindClerkById_LibrarianId() {
        // Test with librarian ID (should return null, not a clerk)
        Clerk clerk = lib.findClerkById(1);

        assertNull(clerk);
    }

    @Test
    public void testFindClerkById_BorrowerId() {
        // Test with borrower ID (should return null)
        Clerk clerk = lib.findClerkById(4);

        assertNull(clerk);
    }

    // ==================== findBookById() Tests ====================

    @Test
    public void testFindBookById_ValidBook() {
        // Test finding an existing book
        Book book = lib.findBookById(1);

        assertNotNull(book);
        assertEquals(1, book.getID());
    }

    @Test
    public void testFindBookById_NonExistentId() {
        // Test with ID that doesn't exist
        Book book = lib.findBookById(999);

        assertNull(book);
    }

    @Test
    public void testFindBookById_NegativeId() {
        // Test with invalid negative ID
        Book book = lib.findBookById(-1);

        assertNull(book);
    }

    // ==================== getHoldRequestExpiry() Tests ====================

    @Test
    public void testGetHoldRequestExpiry() {
        // Test that getter returns the correct request expiry value
        lib.setRequestExpiry(14);

        assertEquals(14, lib.getHoldRequestExpiry());
    }

    // ==================== viewAllBooks() Tests ====================

    @Test
    public void testViewAllBooks_WithBooks() {
        // Test that method runs without errors when books exist
        // This method prints to console, so we just verify no exception

        try {
            lib.viewAllBooks();
            // If we get here, method executed successfully
            assertTrue(true);
        } catch (Exception e) {
            fail("viewAllBooks should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testViewAllBooks_EmptyLibrary() {
        // Test with no books
        Library.resetInstance();
        Library emptyLib = Library.getInstance();

        try {
            emptyLib.viewAllBooks();
            // Should print "no books" message without error
            assertTrue(true);
        } catch (Exception e) {
            fail("viewAllBooks should handle empty library: " + e.getMessage());
        }
    }

    // ==================== findClerk() Test ====================

    @Test
    public void testFindClerk_WithValidInput() {
        // Simulate user entering clerk ID "2"
        String input = "2\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Clerk clerk = lib.findClerk();

        assertNotNull("Should find clerk with ID 2", clerk);
        assertEquals(2, clerk.getID());

        // Reset System.in to avoid affecting other tests
        System.setIn(System.in);
    }

    @Test
    public void testFindClerk_WithInvalidInput() {
        // Test that method handles invalid input gracefully
        // Simulate user entering non-numeric input
        String input = "abc\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Clerk clerk = lib.findClerk();

        // Method catches exception and returns null
        assertNull("Should return null for invalid input", clerk);

        // Reset System.in
        System.setIn(System.in);
    }

    // ==================== findClerk() Tests ====================

    @Test
    public void testFindClerk_WithValidClerkId() {
        // Positive case: user enters valid clerk ID
        String input = "2\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Clerk clerk = lib.findClerk();

        assertNotNull("Should find clerk with ID 2", clerk);
        assertEquals(2, clerk.getID());

        System.setIn(System.in);
    }

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

    @Test
    public void testFindClerk_WithLibrarianId() {
        // Edge case: user enters librarian's ID (wrong type)
        String input = "1\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Clerk clerk = lib.findClerk();

        assertNull("Should return null when given librarian ID", clerk);

        System.setIn(System.in);
    }

    @Test
    public void testFindClerk_WithBorrowerId() {
        // Edge case: user enters borrower's ID (wrong type)
        String input = "4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Clerk clerk = lib.findClerk();

        assertNull("Should return null when given borrower ID", clerk);

        System.setIn(System.in);
    }

    @Test
    public void testFindClerk_WithNonExistentId() {
        // Edge case: user enters ID that doesn't exist
        String input = "999\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Clerk clerk = lib.findClerk();

        assertNull("Should return null when ID doesn't exist", clerk);

        System.setIn(System.in);
    }
}
