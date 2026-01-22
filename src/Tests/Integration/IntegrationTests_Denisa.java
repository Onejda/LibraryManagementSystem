package Tests.Integration;

import LMS.*;
import org.junit.jupiter.api.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for Library Management System
 * Test Scenarios:
 * 1. Place Hold on Issued Book (FIFO verification)
 * 2. Prevent Duplicate Hold Requests
 * 3. Process Hold Queue (FIFO processing)
 * 4. Expired Hold Request Removal
 * 5. Database UPDATE - Book Issued Status
 * 6. Database DELETE - Hold Request Removal
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests_Denisa {

    private static Library library;
    private static DatabaseManager dbManager;
    private static Connection dbConnection;

    // Database test objects (created once, reused)
    private static int dbTestBookId = -1;
    private static int dbTestBorrowerId = -1;

    private Book testBook1;
    private Book testBook2;
    private Borrower borrower1;
    private Borrower borrower2;
    private Borrower borrower3;
    private Staff clerk;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeAll
    static void setUpClass() {
        // One-time setup
        Library.resetInstance();
        library = Library.getInstance();
        dbManager = DatabaseManager.getInstance();
        dbConnection = dbManager.connect();

        library.setFine(20);
        library.setRequestExpiry(7);
        library.setReturnDeadline(5);
        library.setName("Test Library");

        // Create test data in database for IT-D-05 and IT-D-06
        try {
            // Create a test book in database
            PreparedStatement ps = dbConnection.prepareStatement(
                    "INSERT INTO Book(title, author, subject, isIssued) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, "DB Test Book");
            ps.setString(2, "Test Author");
            ps.setString(3, "Testing");
            ps.setInt(4, 0);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                dbTestBookId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            // Create a test borrower in database
            PreparedStatement ps2 = dbConnection.prepareStatement(
                    "INSERT INTO Person(name, password, address, phoneNo, type) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps2.setString(1, "DB Test Borrower");
            ps2.setString(2, "test123");
            ps2.setString(3, "Test Address");
            ps2.setInt(4, 9999999);
            ps2.setString(5, "Borrower");
            ps2.executeUpdate();

            ResultSet rs2 = ps2.getGeneratedKeys();
            if (rs2.next()) {
                dbTestBorrowerId = rs2.getInt(1);
            }
            rs2.close();
            ps2.close();

            System.out.println("Test database objects created: Book ID=" + dbTestBookId + ", Borrower ID=" + dbTestBorrowerId);

        } catch (SQLException e) {
            System.err.println("Failed to create test database objects: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        // Create fresh test data for each test (in-memory)
        testBook1 = new Book(-1, "Test Book 1", "Testing", "Test Author", false);
        library.addBookinLibrary(testBook1);

        testBook2 = new Book(-1, "Test Book 2", "Testing", "Test Author", false);
        library.addBookinLibrary(testBook2);

        borrower1 = new Borrower(-1, "Borrower One", "Address 1", 1111111);
        library.addBorrower(borrower1);

        borrower2 = new Borrower(-1, "Borrower Two", "Address 2", 2222222);
        library.addBorrower(borrower2);

        borrower3 = new Borrower(-1, "Borrower Three", "Address 3", 3333333);
        library.addBorrower(borrower3);

        clerk = new Clerk(-1, "Test Clerk", "Clerk Address", 5555555, 25000, -1);
        library.addClerk((Clerk) clerk);

        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        // Restore console output
        System.setOut(originalOut);

        // Clean up in-memory objects
        if (testBook1 != null) {
            library.getBooks().remove(testBook1);
            testBook1.getHoldRequests().clear();
        }
        if (testBook2 != null) {
            library.getBooks().remove(testBook2);
            testBook2.getHoldRequests().clear();
        }

        if (borrower1 != null) {
            library.getPersons().remove(borrower1);
            borrower1.getBorrowedBooks().clear();
            borrower1.getOnHoldBooks().clear();
        }
        if (borrower2 != null) {
            library.getPersons().remove(borrower2);
            borrower2.getBorrowedBooks().clear();
            borrower2.getOnHoldBooks().clear();
        }
        if (borrower3 != null) {
            library.getPersons().remove(borrower3);
            borrower3.getBorrowedBooks().clear();
            borrower3.getOnHoldBooks().clear();
        }

        if (clerk != null) {
            library.getPersons().remove(clerk);
        }

        library.getLoans().clear();
    }

    @AfterAll
    static void tearDownClass() {
        // Clean up database test objects
        try {
            if (dbTestBookId != -1) {
                PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM Book WHERE id = ?");
                ps.setInt(1, dbTestBookId);
                ps.executeUpdate();
                ps.close();
            }
            if (dbTestBorrowerId != -1) {
                PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM Person WHERE id = ?");
                ps.setInt(1, dbTestBorrowerId);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }

        // Close connection
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }

    // ==================== HELPER METHODS ====================

    private Integer getBookIssuedFlagFromDB(int bookId) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement(
                    "SELECT isIssued FROM Book WHERE id = ?"
            );
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();

            Integer result = null;
            if (rs.next()) {
                result = rs.getInt("isIssued");
            }
            rs.close();
            ps.close();
            return result;

        } catch (SQLException e) {
            System.err.println("DB query failed: " + e.getMessage());
            return null;
        }
    }

    private boolean holdExistsInDB(int bookId, int borrowerId) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement(
                    "SELECT COUNT(*) FROM HoldRequest WHERE bookId = ? AND borrowerId = ?"
            );
            ps.setInt(1, bookId);
            ps.setInt(2, borrowerId);
            ResultSet rs = ps.executeQuery();

            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
            rs.close();
            ps.close();
            return exists;

        } catch (SQLException e) {
            System.err.println("DB query failed: " + e.getMessage());
            return false;
        }
    }

    // ==================== IT-D-01: PLACE HOLD ON ISSUED BOOK ====================

    @Test
    @Order(1)
    @DisplayName("IT-D-01: Place Hold on Issued Book - FIFO Verification")
    void testPlaceHoldOnIssuedBook() {
        System.out.println("\n=== Test IT-D-01: Place Hold on Issued Book ===");

        testBook1.isIssued = true;
        Loan loan = new Loan(borrower1, testBook1, clerk, null, new Date(), null, false);
        borrower1.addBorrowedBook(loan);
        library.addLoan(loan);

        HoldRequest hr1 = new HoldRequest(borrower2, testBook1, new Date());
        testBook1.getHoldRequestOperations().addHoldRequest(hr1);
        borrower2.addHoldRequest(hr1);

        HoldRequest hr2 = new HoldRequest(borrower3, testBook1, new Date());
        testBook1.getHoldRequestOperations().addHoldRequest(hr2);
        borrower3.addHoldRequest(hr2);

        ArrayList<HoldRequest> holdRequests = testBook1.getHoldRequests();
        assertEquals(2, holdRequests.size(), "Should have 2 hold requests");
        assertEquals(borrower2, holdRequests.get(0).getBorrower(), "First hold should be borrower2 (FIFO)");
        assertEquals(borrower3, holdRequests.get(1).getBorrower(), "Second hold should be borrower3 (FIFO)");
        assertTrue(testBook1.getIssuedStatus(), "Book should remain issued");
        assertEquals(1, borrower2.getOnHoldBooks().size(), "Borrower2 should have 1 hold request");
        assertEquals(1, borrower3.getOnHoldBooks().size(), "Borrower3 should have 1 hold request");

        System.out.println("✓ PASS: Hold requests placed in FIFO order");
    }

    // ==================== IT-D-02: PREVENT DUPLICATE HOLD REQUESTS ====================

    @Test
    @Order(2)
    @DisplayName("IT-D-02: Prevent Duplicate Hold Requests")
    void testPreventDuplicateHoldRequests() {
        System.out.println("\n=== Test IT-D-02: Prevent Duplicate Hold Requests ===");

        testBook1.isIssued = true;
        Loan loan = new Loan(borrower1, testBook1, clerk, null, new Date(), null, false);
        library.addLoan(loan);

        HoldRequest hr1 = new HoldRequest(borrower2, testBook1, new Date());
        testBook1.getHoldRequestOperations().addHoldRequest(hr1);
        borrower2.addHoldRequest(hr1);

        int initialHoldCount = testBook1.getHoldRequests().size();
        testBook1.makeHoldRequest(borrower2);
        int finalHoldCount = testBook1.getHoldRequests().size();

        assertEquals(initialHoldCount, finalHoldCount, "Hold count should not increase");
        assertEquals(1, testBook1.getHoldRequests().size(), "Should only have 1 hold request");
        assertEquals(1, borrower2.getOnHoldBooks().size(), "Borrower should only have 1 hold request");

        String output = outputStream.toString();
        assertTrue(output.contains("already have one hold request"), "Should display duplicate message");

        System.out.println("✓ PASS: Duplicate hold request prevented");
    }

    // ==================== IT-D-03: PROCESS HOLD QUEUE (FIFO) ====================

    @Test
    @Order(3)
    @DisplayName("IT-D-03: Process Hold Queue - FIFO Processing")
    void testProcessHoldQueueFIFO() {
        System.out.println("\n=== Test IT-D-03: Process Hold Queue (FIFO) ===");

        HoldRequest hr1 = new HoldRequest(borrower1, testBook1, new Date());
        testBook1.getHoldRequestOperations().addHoldRequest(hr1);
        borrower1.addHoldRequest(hr1);

        HoldRequest hr2 = new HoldRequest(borrower2, testBook1, new Date());
        testBook1.getHoldRequestOperations().addHoldRequest(hr2);
        borrower2.addHoldRequest(hr2);

        HoldRequest hr3 = new HoldRequest(borrower3, testBook1, new Date());
        testBook1.getHoldRequestOperations().addHoldRequest(hr3);
        borrower3.addHoldRequest(hr3);

        assertEquals(3, testBook1.getHoldRequests().size(), "Should have 3 hold requests");

        ByteArrayInputStream input = new ByteArrayInputStream("n\n".getBytes());
        System.setIn(input);

        testBook1.issueBook(borrower2, clerk);
        assertFalse(testBook1.getIssuedStatus(), "Book should not be issued to borrower2");
        assertEquals(3, testBook1.getHoldRequests().size(), "All holds should remain");

        testBook1.issueBook(borrower1, clerk);
        assertTrue(testBook1.getIssuedStatus(), "Book should be issued to borrower1");
        assertEquals(2, testBook1.getHoldRequests().size(), "One hold should be removed");
        assertEquals(borrower2, testBook1.getHoldRequests().get(0).getBorrower(), "Borrower2 should now be first");

        System.out.println("✓ PASS: FIFO order maintained");
    }

    // ==================== IT-D-04: EXPIRED HOLD REQUEST REMOVAL ====================

    @Test
    @Order(4)
    @DisplayName("IT-D-04: Expired Hold Request Removal")
    void testExpiredHoldRequestRemoval() {
        System.out.println("\n=== Test IT-D-04: Expired Hold Request Removal ===");

        library.setRequestExpiry(0);

        Date oldDate = new Date(System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000));
        HoldRequest expiredRequest = new HoldRequest(borrower1, testBook1, oldDate);
        testBook1.getHoldRequestOperations().addHoldRequest(expiredRequest);
        borrower1.addHoldRequest(expiredRequest);

        HoldRequest recentRequest = new HoldRequest(borrower2, testBook1, new Date());
        testBook1.getHoldRequestOperations().addHoldRequest(recentRequest);
        borrower2.addHoldRequest(recentRequest);

        assertEquals(2, testBook1.getHoldRequests().size(), "Should have 2 holds initially");

        ByteArrayInputStream input = new ByteArrayInputStream("n\n".getBytes());
        System.setIn(input);

        testBook1.issueBook(borrower3, clerk);

        assertEquals(1, testBook1.getHoldRequests().size(), "Only 1 hold should remain");
        assertEquals(borrower2, testBook1.getHoldRequests().get(0).getBorrower(), "Non-expired should remain");
        assertEquals(0, borrower1.getOnHoldBooks().size(), "Expired hold removed from borrower1");
        assertEquals(1, borrower2.getOnHoldBooks().size(), "Valid hold remains for borrower2");

        System.out.println("✓ PASS: Expired hold request removed");
    }

    // ==================== IT-D-05: DATABASE UPDATE ====================

    @Test
    @Order(5)
    @DisplayName("IT-D-05: Database UPDATE - Book Issued Status Verification")
    void testDBUpdateBookIssuedStatus() {
        System.out.println("\n=== Test IT-D-05: DB UPDATE - Book Issued Status ===");

        try {
            // Use the test book created in @BeforeAll
            assertNotEquals(-1, dbTestBookId, "Test book must exist");

            // Query initial state
            Integer initialFlag = getBookIssuedFlagFromDB(dbTestBookId);
            assertNotNull(initialFlag, "Test book must exist in database");
            System.out.println("Initial DB state: isIssued = " + initialFlag);

            // Act: UPDATE to issued (true)
            dbManager.updateBookIssuedStatus(dbTestBookId, true);

            // Assert: Verify UPDATE
            Integer updatedFlag = getBookIssuedFlagFromDB(dbTestBookId);
            assertNotNull(updatedFlag, "Book must still exist after UPDATE");
            assertEquals(1, updatedFlag, "Book should be marked as issued (isIssued=1)");

            // Act: UPDATE back to available (false)
            dbManager.updateBookIssuedStatus(dbTestBookId, false);

            // Assert: Verify second UPDATE
            Integer finalFlag = getBookIssuedFlagFromDB(dbTestBookId);
            assertEquals(0, finalFlag, "Book should be marked as available (isIssued=0)");

            System.out.println("✓ PASS: UPDATE operations verified in database");
            System.out.println("✓ DatabaseManager.updateBookIssuedStatus() integration confirmed");

        } catch (Exception e) {
            fail("Database UPDATE integration failed: " + e.getMessage());
        }
    }

    // ==================== IT-D-06: DATABASE DELETE ====================

    @Test
    @Order(6)
    @DisplayName("IT-D-06: Database DELETE - Hold Request Removal Verification")
    void testDBDeleteHoldRequest() {
        System.out.println("\n=== Test IT-D-06: DB DELETE - Hold Request ===");

        try {
            // Use test objects created in @BeforeAll
            assertNotEquals(-1, dbTestBookId, "Test book must exist");
            assertNotEquals(-1, dbTestBorrowerId, "Test borrower must exist");

            // Insert a hold request to delete
            dbManager.insertHoldRequest(dbTestBookId, dbTestBorrowerId, new Date());

            // Verify hold exists
            boolean existsBefore = holdExistsInDB(dbTestBookId, dbTestBorrowerId);
            assertTrue(existsBefore, "Hold request must exist before DELETE");
            System.out.println("Hold request found in database (before DELETE)");

            // Act: DELETE
            dbManager.deleteHoldRequest(dbTestBookId, dbTestBorrowerId);

            // Assert: Verify DELETE
            boolean existsAfter = holdExistsInDB(dbTestBookId, dbTestBorrowerId);
            assertFalse(existsAfter, "Hold request must be removed after DELETE");

            System.out.println("✓ PASS: DELETE operation verified in database");
            System.out.println("✓ DatabaseManager.deleteHoldRequest() integration confirmed");

        } catch (Exception e) {
            fail("Database DELETE integration failed: " + e.getMessage());
        }
    }
}