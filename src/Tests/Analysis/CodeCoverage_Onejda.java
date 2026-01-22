package Tests.Analysis;

import LMS.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Code Coverage Testing for findStaffById(int) method
 * Tests Statement, Branch, Condition, and MC/DC coverage
 */
public class CodeCoverage_Onejda {

    private Library lib;

    @Before
    public void setUp() {
        // Reset and initialize library
        Library.resetInstance();
        lib = Library.getInstance();

        // Set library configuration
        lib.setFine(20);
        lib.setRequestExpiry(7);
        lib.setReturnDeadline(5);
        lib.setName("Test Library");

        // Create test data manually without database
        Librarian librarian = new Librarian(1, "Admin", "Library Office", 5550000, 50000, 101);
        Clerk clerk = new Clerk(2, "Jane Doe", "Front Desk", 5552345, 25000, 1);
        Borrower borrower = new Borrower(4, "Alice Brown", "123 Student Ave", 5554567);

        // Add to library WITHOUT database
        Library.librarian = librarian;
        Library.persons.add(clerk);
        Library.persons.add(borrower);
    }

    @After
    public void tearDown() {
        // Clean up
        Library.resetInstance();
    }

    // ==================== STATEMENT COVERAGE TESTS ====================

    @Test
    public void testSC01_LibrarianID() {
        // SC-01: First if, return librarian
        // Covers: Line with "if (librarian != null && librarian.getID() == id)"
        //         Line with "return librarian;"

        Staff result = lib.findStaffById(1);

        assertNotNull(result);
        assertTrue(result instanceof Librarian);
        assertEquals(1, result.getID());
    }

    @Test
    public void testSC02_ClerkID() {
        // SC-02: Loop, inner if, return staff
        // Covers: Lines in for loop
        //         Line with "if (p.getID() == id && p instanceof Staff)"
        //         Line with "return (Staff) p;"

        Staff result = lib.findStaffById(2);

        assertNotNull(result);
        assertTrue(result instanceof Clerk);
        assertEquals(2, result.getID());
    }

    @Test
    public void testSC03_BorrowerID() {
        // SC-03: Loop, inner if false (borrower is not Staff)
        // Covers: For loop with condition that fails instanceof check

        Staff result = lib.findStaffById(4);

        assertNull(result);
    }

    @Test
    public void testSC04_NonExistentID() {
        // SC-04: Loop exhausted, return null
        // Covers: Complete for loop iteration
        //         Final "return null;" statement

        Staff result = lib.findStaffById(999);

        assertNull(result);
    }

    // ==================== BRANCH COVERAGE TESTS ====================

    @Test
    public void testBC01_FirstConditionTrue() {
        // Branch 1: librarian != null && librarian.getID() == id → TRUE
        // Takes TRUE branch, returns librarian

        Staff result = lib.findStaffById(1);

        assertNotNull(result);
        assertTrue(result instanceof Librarian);
    }

    @Test
    public void testBC02_FirstConditionFalse_SecondTrue() {
        // Branch 1: librarian != null && librarian.getID() == id → FALSE
        // Branch 2: p.getID() == id && p instanceof Staff → TRUE
        // Takes FALSE branch, enters loop, takes TRUE branch in loop

        Staff result = lib.findStaffById(2);

        assertNotNull(result);
        assertTrue(result instanceof Clerk);
    }

    @Test
    public void testBC03_BothConditionsFalse() {
        // Branch 1: librarian != null && librarian.getID() == id → FALSE
        // Branch 2: p.getID() == id && p instanceof Staff → FALSE
        // Takes FALSE branch twice, returns null

        Staff result = lib.findStaffById(4);

        assertNull(result);
    }

    @Test
    public void testBC04_LoopExhausted() {
        // Branch 1: FALSE (ID doesn't match librarian)
        // Branch 2: FALSE (no matching person in loop)
        // Loop completes without match, returns null

        Staff result = lib.findStaffById(999);

        assertNull(result);
    }

    // ==================== CONDITION COVERAGE TESTS ====================

    @Test
    public void testCC01_LibrarianNotNull_True() {
        // Condition: librarian != null → TRUE
        // (Librarian exists in system)

        assertNotNull(Library.librarian);
        Staff result = lib.findStaffById(1);
        assertNotNull(result);
    }

    @Test
    public void testCC02_LibrarianGetIDMatch_True() {
        // Condition: librarian.getID() == id → TRUE
        // (ID matches librarian's ID)

        Staff result = lib.findStaffById(1);
        assertEquals(1, result.getID());
    }

    @Test
    public void testCC03_LibrarianGetIDMatch_False() {
        // Condition: librarian.getID() == id → FALSE
        // (ID doesn't match librarian's ID)

        Staff result = lib.findStaffById(2);
        assertNotEquals(1, result.getID());
    }

    @Test
    public void testCC04_PersonGetIDMatch_True() {
        // Condition: p.getID() == id → TRUE
        // (ID matches a person in list)

        Staff result = lib.findStaffById(2);
        assertNotNull(result);
    }

    @Test
    public void testCC05_PersonInstanceOfStaff_True() {
        // Condition: p instanceof Staff → TRUE
        // (Person is a Staff member)

        Staff result = lib.findStaffById(2);
        assertTrue(result instanceof Staff);
    }

    @Test
    public void testCC06_PersonInstanceOfStaff_False() {
        // Condition: p instanceof Staff → FALSE
        // (Person exists but is Borrower, not Staff)

        Staff result = lib.findStaffById(4);
        assertNull(result);
    }

    @Test
    public void testCC07_LibrarianNull_False() {
        // Condition: librarian != null → FALSE
        // (No librarian in system)

        // Temporarily set librarian to null
        Library.librarian = null;

        Staff result = lib.findStaffById(1);

        assertNull(result);

        // Note: This tests the condition where librarian is null
        // In this case, the first condition fails and method continues to loop
    }

    // ==================== MC/DC COVERAGE TESTS ====================

    /**
     * Decision 1: librarian != null && librarian.getID() == id
     * Conditions: A = (librarian != null), B = (librarian.getID() == id)
     */

    @Test
    public void testMC01_Decision1_A_True_B_True() {
        // MC-01: A=True, B=True → Decision=True

        Staff result = lib.findStaffById(1);

        assertNotNull(result);
        assertEquals(1, result.getID());
        assertTrue(result instanceof Librarian);
    }

    @Test
    public void testMC02_Decision1_A_True_B_False() {
        // MC-02: A=True, B=False → Decision=False

        Staff result = lib.findStaffById(2);

        // Should not return librarian since ID doesn't match
        if (result != null) {
            assertNotEquals(1, result.getID());
        }
    }

    @Test
    public void testMC03_Decision1_A_False_B_DontCare() {
        // MC-03: A=False, B=Don't Care → Decision=False
        // When librarian is null, decision is false regardless of B

        // Set librarian to null
        Library.librarian = null;

        Staff result = lib.findStaffById(1);
        assertNull(result);
    }

    /**
     * Decision 2: p.getID() == id && p instanceof Staff
     * Conditions: A = (p.getID() == id), B = (p instanceof Staff)
     */

    @Test
    public void testMC04_Decision2_A_True_B_True() {
        // MC-04: A=True, B=True → Decision=True
        // Both conditions true, decision is true

        Staff result = lib.findStaffById(2);

        assertNotNull(result);
        assertEquals(2, result.getID());
        assertTrue(result instanceof Staff);
    }

    @Test
    public void testMC05_Decision2_A_True_B_False() {
        // MC-05: A=True, B=False → Decision=False
        // Person exists with matching ID but is not Staff

        Staff result = lib.findStaffById(4);

        assertNull(result);
        // Person with ID=4 exists (A=True) but is Borrower (B=False)
    }

    @Test
    public void testMC06_Decision2_A_False_B_DontCare() {
        // MC-06: A=False, B=Don't Care → Decision=False
        // No person with matching ID exists

        Staff result = lib.findStaffById(999);

        assertNull(result);
        // No person with ID=999 exists (A=False), so decision is false
        // regardless of whether a person would be Staff or not
    }
}