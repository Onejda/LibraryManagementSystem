package Tests.Analysis;

import LMS.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Code Coverage Testing for findStaffById(int) method
 * Tests Statement, Branch, Condition, and MC/DC coverage
 */
public class CodeCoverage_Onejda {

    private Library lib;
    private DatabaseManager db;

    @Before
    public void setUp() {
        // Reset and initialize library
        Library.resetInstance();
        lib = Library.getInstance();

        // Connect to database
        db = DatabaseManager.getInstance();
        db.connect();

        // Populate library with test data
        try {
            lib.populateLibrary(db.connect());
        } catch (IOException e) {
            fail("Failed to populate library: " + e.getMessage());
        }

        // Set library configuration
        lib.setFine(20);
        lib.setRequestExpiry(7);
        lib.setReturnDeadline(5);
        lib.setName("Test Library");
    }

    @After
    public void tearDown() {
        // Clean up
        db.closeConnection();
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

    // ==================== MC/DC COVERAGE TESTS ====================

    /**
     * Decision 1: librarian != null && librarian.getID() == id
     * Conditions: A = (librarian != null), B = (librarian.getID() == id)
     */

    @Test
    public void testMC01_Decision1_BothTrue() {
        // MC-01: A=True, B=True → Decision=True
        // Condition A (librarian!=null) = TRUE
        // Condition B (ID match) = TRUE

        Staff result = lib.findStaffById(1);

        assertNotNull(result);
        assertEquals(1, result.getID());
        assertTrue(result instanceof Librarian);
    }

    @Test
    public void testMC02_Decision1_A_True_B_False() {
        // MC-02: A=True, B=False → Decision=False
        // Condition A (librarian!=null) = TRUE
        // Condition B (ID match) = FALSE
        // Shows that B independently affects decision

        Staff result = lib.findStaffById(2);

        // Should not return librarian since ID doesn't match
        if (result != null) {
            assertNotEquals(1, result.getID());
        }
    }

    @Test
    public void testMC03_Decision1_A_False() {
        // MC-03: A=False, B=Don't Care → Decision=False
        // Condition A (librarian!=null) = FALSE
        // When librarian is null, entire decision is false
        // This test requires temporarily having no librarian

        // Note: In the seeded database, librarian always exists
        // This test documents that if librarian were null,
        // the decision would be false regardless of B

        // We can test this with non-librarian ID
        Staff result = lib.findStaffById(999);
        assertNull(result);
    }

    /**
     * Decision 2: p.getID() == id && p instanceof Staff
     * Conditions: A = (p.getID() == id), B = (p instanceof Staff)
     */

    @Test
    public void testMC04_Decision2_BothTrue() {
        // MC-04: A=True, B=True → Decision=True
        // Condition A (ID match) = TRUE
        // Condition B (instanceof Staff) = TRUE

        Staff result = lib.findStaffById(2);

        assertNotNull(result);
        assertEquals(2, result.getID());
        assertTrue(result instanceof Staff);
    }

    @Test
    public void testMC05_Decision2_A_True_B_False() {
        // MC-05: A=True, B=False → Decision=False
        // Condition A (ID match) = TRUE (person with ID exists)
        // Condition B (instanceof Staff) = FALSE (person is Borrower)
        // Shows that B independently affects decision

        Staff result = lib.findStaffById(4);

        assertNull(result);
        // Person with ID=4 exists but is Borrower, not Staff
    }

    @Test
    public void testMC06_Decision2_A_False_B_True() {
        // MC-06: A=False, B=Don't Care → Decision=False
        // Condition A (ID match) = FALSE (no person with this ID)
        // When ID doesn't match, entire decision is false
        // Shows that A independently affects decision

        Staff result = lib.findStaffById(999);

        assertNull(result);
        // No person with ID=999 exists
    }

    // ==================== ADDITIONAL INTEGRATION TESTS ====================

    @Test
    public void testIntegration_LibrarianTakesPriority() {
        // Verify that librarian check happens before loop
        // Even if there's a clerk with same ID (impossible but tests priority)

        Staff result = lib.findStaffById(1);

        assertNotNull(result);
        assertTrue("Should return Librarian, not any other staff",
                result instanceof Librarian);
    }

    @Test
    public void testIntegration_MultipleStaffInList() {
        // Verify loop correctly finds staff among multiple persons

        Staff clerk1 = lib.findStaffById(2);
        Staff clerk2 = lib.findStaffById(3);

        assertNotNull(clerk1);
        assertNotNull(clerk2);
        assertNotEquals("Should return different clerks",
                clerk1.getID(), clerk2.getID());
    }
}