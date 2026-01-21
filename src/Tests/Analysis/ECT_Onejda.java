package Tests.Analysis;

import LMS.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import static org.junit.Assert.*;

// done for the method: findStaffById(int)

public class ECT_Onejda {

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