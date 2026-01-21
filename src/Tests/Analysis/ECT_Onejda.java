package Tests.Analysis;

import LMS.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

// Equivalence Class Testing for the method: findStaffById(int)

public class ECT_Onejda {

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

    @Test
    public void testECT01_ValidLibrarian() {
        // ECT-01: ID matches the librarian
        Staff result = lib.findStaffById(1);

        assertNotNull(result);
        assertTrue(result instanceof Librarian);
    }

    @Test
    public void testECT02_ValidClerk() {
        // ECT-02: ID matches a staff member (Clerk)
        Staff result = lib.findStaffById(2);

        assertNotNull(result);
        assertTrue(result instanceof Clerk);
    }

    @Test
    public void testECT03_BorrowerNotStaff() {
        // ECT-03: ID exists but belongs to a non-staff person (Borrower)
        Staff result = lib.findStaffById(4);

        assertNull(result);
    }

    @Test
    public void testECT04_NonExistentID() {
        // ECT-04: ID does not exist in system
        Staff result = lib.findStaffById(999);

        assertNull(result);
    }

    @Test
    public void testECT05_InvalidNegativeID() {
        // ECT-05: Invalid (negative) ID
        Staff result = lib.findStaffById(-1);

        assertNull(result);
    }
}