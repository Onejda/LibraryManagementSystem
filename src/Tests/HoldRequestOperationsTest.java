package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Unit Tests for HoldRequestOperations
 * Covered:
 *  - addHoldRequest
 *  - removeHoldRequest
 *  - removeSpecificHoldRequest
 *  - getHoldRequests
 *  - hasHoldRequests
 *  - getHoldRequestCount
 */

public class HoldRequestOperationsTest {

    private HoldRequestOperations ops;
    private Borrower borrower;
    private Book book;
    private HoldRequest hr1;
    private HoldRequest hr2;

    @BeforeEach
    void setUp() {
        // Reset singleton & counters
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);

        Library library = Library.getInstance();
        library.makeConnection();

        ops = new HoldRequestOperations();

        borrower = new Borrower(-1, "Denisa", "Addr", 123);
        borrower.saveToDatabase();

        book = new Book(-1, "Clean Code", "SE", "Martin", false);
        book.saveToDatabase();

        hr1 = new HoldRequest(borrower, book, new Date());
        hr2 = new HoldRequest(borrower, book, new Date());
    }

    // ======================= addHoldRequest =======================

    @Test
    @DisplayName("addHoldRequest - Adds request to list")
    void testAddHoldRequest() {
        ops.addHoldRequest(hr1);

        assertEquals(1, ops.getHoldRequestCount());
        assertTrue(ops.getHoldRequests().contains(hr1));
    }

    @Test
    @DisplayName("addHoldRequest - Multiple requests added")
    void testAddHoldRequest_Multiple() {
        ops.addHoldRequest(hr1);
        ops.addHoldRequest(hr2);

        assertEquals(2, ops.getHoldRequestCount());
    }

    // ======================= hasHoldRequests =======================

    @Test
    @DisplayName("hasHoldRequests - False when empty")
    void testHasHoldRequests_Empty() {
        assertFalse(ops.hasHoldRequests());
    }

    @Test
    @DisplayName("hasHoldRequests - True when not empty")
    void testHasHoldRequests_NotEmpty() {
        ops.addHoldRequest(hr1);
        assertTrue(ops.hasHoldRequests());
    }

    // ======================= getHoldRequestCount =======================

    @Test
    @DisplayName("getHoldRequestCount - Correct count")
    void testGetHoldRequestCount() {
        ops.addHoldRequest(hr1);
        ops.addHoldRequest(hr2);

        assertEquals(2, ops.getHoldRequestCount());
    }

    // ======================= removeHoldRequest =======================

    @Test
    @DisplayName("removeHoldRequest - Removes first (FIFO)")
    void testRemoveHoldRequest_FIFO() {
        ops.addHoldRequest(hr1);
        ops.addHoldRequest(hr2);

        ops.removeHoldRequest();

        assertEquals(1, ops.getHoldRequestCount());
        assertFalse(ops.getHoldRequests().contains(hr1));
        assertTrue(ops.getHoldRequests().contains(hr2));
    }

    @Test
    @DisplayName("removeHoldRequest - Safe on empty list")
    void testRemoveHoldRequest_Empty() {
        assertDoesNotThrow(() -> ops.removeHoldRequest());
        assertEquals(0, ops.getHoldRequestCount());
    }

    // ======================= removeSpecificHoldRequest =======================

    @Test
    @DisplayName("removeSpecificHoldRequest - Removes matching request")
    void testRemoveSpecificHoldRequest_Found() {
        ops.addHoldRequest(hr1);
        ops.addHoldRequest(hr2);

        ops.removeSpecificHoldRequest(hr2);

        assertEquals(1, ops.getHoldRequestCount());
        assertFalse(ops.getHoldRequests().contains(hr2));
    }

    @Test
    @DisplayName("removeSpecificHoldRequest - Does nothing if request not present")
    void testRemoveSpecificHoldRequest_NotFound() {
        ops.addHoldRequest(hr1);

        HoldRequest fake = new HoldRequest(borrower, book, new Date());

        assertDoesNotThrow(() -> ops.removeSpecificHoldRequest(fake));
        assertEquals(1, ops.getHoldRequestCount());
    }

    // ======================= getHoldRequests =======================

    @Test
    @DisplayName("getHoldRequests - Returns internal list")
    void testGetHoldRequests() {
        ops.addHoldRequest(hr1);

        assertEquals(1, ops.getHoldRequests().size());
    }
}
