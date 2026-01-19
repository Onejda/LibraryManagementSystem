package Tests;

import LMS.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests - Denisa
 *
 * Scenarios:
 * IT-D01 Place Hold on Issued Book
 * IT-D02 Process Hold Queue (FIFO)
 * IT-D03 Prevent Duplicate Hold Requests
 * IT-D04 Expired Hold Request Removal
 *
 * Database (CRUD - UPDATE & DELETE):
 * IT-D05 Update book issued status + verify in DB
 * IT-D06 Delete hold request + verify in DB
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTests_Denisa {

    private Library library;
    private DatabaseManager db;

    private Clerk clerk;
    private Borrower borrower1;
    private Borrower borrower2;

    private Book book;   // used in most scenarios

    @BeforeEach
    void setUp() {
        // Reset singleton + counters
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);
        Clerk.setDeskCount(0);

        // Init system
        library = Library.getInstance();
        library.setName("Test Library");
        library.setFine(20.0);
        library.setReturnDeadline(5);
        library.setRequestExpiry(7); // hold expiry days

        // DB connection (needed because Book/HoldRequest methods hit DB)
        db = DatabaseManager.getInstance();
        db.connect();

        // Create staff/borrowers using your real constructors
        clerk = new Clerk(-1, "Test Clerk", "Desk 1", 1111111, 25000.0, -1);
        borrower1 = new Borrower(-1, "Alice Brown", "Street 1", 2222222);
        borrower2 = new Borrower(-1, "Bob Wilson", "Street 2", 3333333);

        // Add persons to library (Library stores all persons in the same list)
        library.addClerk(clerk);
        library.addBorrower(borrower1);
        library.addBorrower(borrower2);

        // Create a book
        book = new Book(-1, "Denisa Test Book", "Software", "Author X", false);

        // Save book to DB and add to library in-memory list
        book.saveToDatabase();
        library.addBookinLibrary(book);
    }

    @AfterEach
    void tearDown() {
        // Best-effort cleanup
        try {
            // remove hold requests from DB if any exist
            for (HoldRequest hr : new ArrayList<>(book.getHoldRequests())) {
                hr.deleteFromDatabase();
            }
            // delete book from DB
            book.deleteFromDatabase();
        } catch (Exception ignored) {}

        if (db != null) db.closeConnection();
        Library.resetInstance();
    }

    // -------------------- Helper: check DB for a hold request row --------------------

    private boolean holdExistsInDB(int bookId, int borrowerId) {
        ArrayList<Object[]> holdData = db.loadAllHoldRequests(); // [id, bookId, borrowerId, requestDate]
        for (Object[] row : holdData) {
            int bId = (Integer) row[1];
            int brId = (Integer) row[2];
            if (bId == bookId && brId == borrowerId) return true;
        }
        return false;
    }

    // -------------------- Helper: check DB for book issued status --------------------

    private Integer getBookIssuedFlagFromDB(int bookId) {
        ArrayList<Object[]> books = db.loadAllBooks(); // used by Library.populateLibrary()
        // expected row shape: [id, title, author, subject, isIssued]
        for (Object[] row : books) {
            int id = (Integer) row[0];
            if (id == bookId) {
                // sometimes stored as Integer/Boolean depending on your loader; handle both safely
                Object issuedObj = row[4];
                if (issuedObj instanceof Boolean) return ((Boolean) issuedObj) ? 1 : 0;
                if (issuedObj instanceof Integer) return (Integer) issuedObj;
                if (issuedObj instanceof Long) return ((Long) issuedObj).intValue();
                return null;
            }
        }
        return null; // not found
    }

    // ==================== IT-D01: Place Hold on Issued Book ====================

    @Test
    @DisplayName("IT-D01: Place Hold on Issued Book")
    void testPlaceHoldOnIssuedBook() {
        // Arrange: mark as issued
        book.setIssuedStatus(true);
        assertTrue(book.getIssuedStatus());

        // Act: borrower requests a hold
        book.makeHoldRequest(borrower1);

        // Assert: hold created in memory
        assertEquals(1, book.getHoldRequests().size(), "Book should have 1 hold request");
        assertEquals(borrower1, book.getHoldRequests().get(0).getBorrower(), "Hold borrower should match");

        // Assert: borrower also has it in their on-hold list
        assertEquals(1, borrower1.getOnHoldBooks().size(), "Borrower should have 1 hold request");

        // Assert: DB row exists (query verification)
        assertTrue(
                holdExistsInDB(book.getID(), borrower1.getID()),
                "HoldRequest row should exist in DB"
        );
    }

    // ==================== IT-D02: Prevent Duplicate Hold Requests ====================

    @Test
    @DisplayName("IT-D03: Prevent Duplicate Hold Requests")
    void testPreventDuplicateHoldRequests() {
        // Act: same borrower requests twice
        book.makeHoldRequest(borrower1);
        book.makeHoldRequest(borrower1);

        // Assert: only one hold exists
        assertEquals(1, book.getHoldRequests().size(), "Duplicate hold must NOT be added");
        assertEquals(1, borrower1.getOnHoldBooks().size(), "Borrower should have only 1 on-hold record");

        // Assert: DB contains only the intended row (exists = true is enough here)
        assertTrue(holdExistsInDB(book.getID(), borrower1.getID()), "Hold should exist in DB");
    }

    // ==================== IT-D03: Process Hold Queue (FIFO) ====================

    @Test
    @DisplayName("IT-D02: Process Hold Queue (FIFO) - first hold removed, second remains")
    void testProcessHoldQueueFIFO() {
        // Arrange: create 2 holds in order
        book.placeBookOnHold(borrower1);
        book.placeBookOnHold(borrower2);

        assertEquals(2, book.getHoldRequests().size(), "Two holds should exist");
        HoldRequest first = book.getHoldRequests().get(0);
        HoldRequest second = book.getHoldRequests().get(1);

        assertEquals(borrower1, first.getBorrower(), "First in queue must be borrower1");
        assertEquals(borrower2, second.getBorrower(), "Second in queue must be borrower2");

        // Act: service first hold (your code removes first request + deletes from DB)
        book.serviceHoldRequest(first);

        // Assert: FIFO behavior
        assertEquals(1, book.getHoldRequests().size(), "Only one hold should remain");
        assertEquals(borrower2, book.getHoldRequests().get(0).getBorrower(), "Remaining should be borrower2");

        // Assert: borrower lists updated
        assertEquals(0, borrower1.getOnHoldBooks().size(), "Borrower1 hold list should be cleared");
        assertEquals(1, borrower2.getOnHoldBooks().size(), "Borrower2 still has a hold");

        // Assert: DB state (first deleted, second still present)
        assertFalse(holdExistsInDB(book.getID(), borrower1.getID()), "First hold should be deleted from DB");
        assertTrue(holdExistsInDB(book.getID(), borrower2.getID()), "Second hold should still exist in DB");
    }


    // ==================== IT-D04: Expired Hold Request Removal ====================

    @Test
    @DisplayName("IT-D04: Expired Hold Request Removal (deleted before issuing)")
    void testExpiredHoldRequestRemoval() {
        // Arrange: create an OLD request date (older than expiry = 7 days)
        long tenDaysMillis = 10L * 24 * 60 * 60 * 1000;
        Date oldDate = new Date(System.currentTimeMillis() - tenDaysMillis);

        HoldRequest expired = new HoldRequest(borrower1, book, oldDate);

        // Add expired hold to in-memory structures (same pattern used by populateLibrary)
        book.getHoldRequestOperations().addHoldRequest(expired);
        borrower1.addHoldRequest(expired);

        // Save it to DB so deletion is meaningful
        expired.saveToDatabase();
        assertTrue(holdExistsInDB(book.getID(), borrower1.getID()), "Expired hold must exist in DB before issuing");

        // Act: issuing triggers deletion of expired holds FIRST (per your Book.issueBook implementation)
        book.issueBook(borrower2, clerk);

        // Assert: expired hold removed from in-memory
        assertEquals(0, borrower1.getOnHoldBooks().size(), "Expired hold should be removed from borrower");
        assertEquals(0, book.getHoldRequests().size(), "Expired hold should be removed from book queue");

        // Assert: expired hold removed from DB
        assertFalse(holdExistsInDB(book.getID(), borrower1.getID()), "Expired hold should be deleted from DB");
    }

    // ==================== IT-D05: DB UPDATE (book issued status) ====================

    @Test
    @DisplayName("IT-D05: DB UPDATE - Update book issued status and verify in DB")
    void testDBUpdateBookIssuedStatus() {
        // Arrange: ensure book exists in DB
        Integer initialFlag = getBookIssuedFlagFromDB(book.getID());
        assertNotNull(initialFlag, "Book must exist in DB for UPDATE test");

        // Act: update via DB layer
        db.updateBookIssuedStatus(book.getID(), true);

        // Assert
        Integer updatedFlag = getBookIssuedFlagFromDB(book.getID());
        assertNotNull(updatedFlag, "Book must still exist after update");
        assertEquals(1, updatedFlag, "Book should be marked issued in DB after UPDATE");
    }

    // ==================== IT-D06: DB DELETE (hold request) ====================

    @Test
    @DisplayName("IT-D06: DB DELETE - Delete hold request and verify in DB")
    void testDBDeleteHoldRequest() {
        // Arrange: create a hold (this inserts into DB too)
        book.placeBookOnHold(borrower1);
        assertTrue(holdExistsInDB(book.getID(), borrower1.getID()), "Hold must exist in DB before DELETE");

        // Act: delete directly via DB method used by HoldRequest.deleteFromDatabase()
        db.deleteHoldRequest(book.getID(), borrower1.getID());

        // Assert: verify DB row removed
        assertFalse(holdExistsInDB(book.getID(), borrower1.getID()), "Hold must be removed from DB after DELETE");
    }
}
