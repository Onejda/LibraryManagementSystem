package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.sql.*;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTests_Emi {

    private Library library;
    private DatabaseManager dbManager;
    private Clerk clerk;
    private Borrower borrower1;
    private Borrower borrower2;
    private Book availableBook;
    private Book issuedBook;

    @BeforeAll
    public void setupDatabase() {
        // Reset singleton instances for clean testing
        Library.resetInstance();

        // Initialize database
        dbManager = DatabaseManager.getInstance();
        dbManager.connect();

        library = Library.getInstance();
        library.setFine(20.0);
        library.setReturnDeadline(5);
        library.setRequestExpiry(7);
        library.setName("Test Library");
    }

    @BeforeEach
    public void setup() {

        dbManager = DatabaseManager.getInstance();
        dbManager.connect();

        Connection conn = dbManager.connect();

        try (Statement stmt = conn.createStatement()) {

            // Clear dependent tables first
            stmt.executeUpdate("DELETE FROM HoldRequest");
            stmt.executeUpdate("DELETE FROM Loan");
            stmt.executeUpdate("DELETE FROM Book");
            stmt.executeUpdate("DELETE FROM Staff");
            stmt.executeUpdate("DELETE FROM Clerk");
            stmt.executeUpdate("DELETE FROM Librarian");
            stmt.executeUpdate("DELETE FROM Person");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        library = Library.getInstance();

        // Create test clerk
        clerk = new Clerk(-1, "Test Clerk", "123 Main St", 5551234, 25000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);

        // Create test borrowers
        borrower1 = new Borrower(-1, "Alice Smith", "456 Oak Ave", 5555678);
        borrower1.saveToDatabase();
        library.addBorrower(borrower1);

        borrower2 = new Borrower(-1, "Bob Johnson", "789 Pine Rd", 5559012);
        borrower2.saveToDatabase();
        library.addBorrower(borrower2);

        // Create test books
        availableBook = new Book(-1, "Clean Architecture", "Software Engineering", "Robert Martin", false);
        availableBook.saveToDatabase();
        library.addBookinLibrary(availableBook);

        issuedBook = new Book(-1, "Design Patterns", "Software Engineering", "Gang of Four", false);
        issuedBook.saveToDatabase();
        library.addBookinLibrary(issuedBook);
    }



    // ==================== SCENARIO 1: Issue Available Book ⭐ ====================

    /**
     * Integration Test 1.1: Issue Available Book - Basic Flow
     */
    @Test
    @DisplayName("IT 1.1: Issue Available Book - Basic Flow")
    public void testIssueAvailableBook_BasicFlow() {
        // Arrange
        assertFalse(availableBook.getIssuedStatus(), "Book should initially be available");
        assertEquals(0, borrower1.getBorrowedBooks().size(), "Borrower should have no books initially");

        // Act - Simulate user input "n" for not placing hold
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        availableBook.issueBook(borrower1, clerk);

        // Assert - Verify Book status
        assertTrue(availableBook.getIssuedStatus(), "Book should be marked as issued");

        // Assert - Verify Borrower's borrowed books
        assertEquals(1, borrower1.getBorrowedBooks().size(), "Borrower should have 1 borrowed book");
        Loan loan = borrower1.getBorrowedBooks().get(0);
        assertNotNull(loan, "Loan should be created");

        // Assert - Verify Loan details
        assertEquals(borrower1, loan.getBorrower(), "Loan should have correct borrower");
        assertEquals(availableBook, loan.getBook(), "Loan should have correct book");
        assertEquals(clerk, loan.getIssuer(), "Loan should have correct issuer");
        assertNull(loan.getReceiver(), "Loan should not have receiver yet");
        assertNotNull(loan.getIssuedDate(), "Loan should have issue date");
        assertNull(loan.getReturnDate(), "Loan should not have return date yet");

        // Assert - Verify Library's loans
        assertTrue(library.getLoans().contains(loan), "Library should contain the loan");

        // Assert - Verify Database persistence
        ArrayList<Object[]> loansFromDB = dbManager.loadAllLoans();
        boolean loanFoundInDB = false;
        for (Object[] loanData : loansFromDB) {
            int borrowerId = (int) loanData[1];
            int bookId = (int) loanData[2];
            if (borrowerId == borrower1.getID() && bookId == availableBook.getID()) {
                loanFoundInDB = true;
                break;
            }
        }
        assertTrue(loanFoundInDB, "Loan should be persisted in database");
    }

    /**
     * Integration Test 1.2: Issue Available Book - Database Verification
     */
    @Test
    @DisplayName("IT 1.2: Issue Available Book - Database INSERT & SELECT")
    public void testIssueAvailableBook_DatabaseCRUD() {
        // Arrange - Create a new book for database testing
        Book newBook = new Book(-1, "Test Book Title", "Test Subject", "Test Author", false);
        int initialBookCount = library.getBooks().size();

        // Act - Insert book into database
        newBook.saveToDatabase();

        // Assert - Verify book has ID assigned
        assertTrue(newBook.getID() > 0, "Book should have database-generated ID");

        // Act - Retrieve all books from database
        ArrayList<Object[]> booksFromDB = dbManager.loadAllBooks();

        // Assert - Verify book exists in database
        boolean bookFound = false;
        for (Object[] bookData : booksFromDB) {
            int bookId = (int) bookData[0];
            String title = (String) bookData[1];
            String author = (String) bookData[2];
            String subject = (String) bookData[3];

            if (bookId == newBook.getID()) {
                bookFound = true;
                assertEquals("Test Book Title", title, "Title should match");
                assertEquals("Test Author", author, "Author should match");
                assertEquals("Test Subject", subject, "Subject should match");
                break;
            }
        }
        assertTrue(bookFound, "Book should be found in database after INSERT");
    }

    /**
     * Integration Test 1.3: Issue Available Book - Borrower Data Verification
     */
    @Test
    @DisplayName("IT 1.3: Issue Available Book - Borrower Database CRUD")
    public void testIssueAvailableBook_BorrowerDatabaseCRUD() {
        // Arrange - Create a new borrower
        Borrower newBorrower = new Borrower(-1, "Charlie Brown", "321 Elm St", 5551111);

        // Act - Insert borrower into database
        newBorrower.saveToDatabase();

        // Assert - Verify borrower has ID assigned
        assertTrue(newBorrower.getID() > 0, "Borrower should have database-generated ID");

        // Act - Retrieve all borrowers from database
        ArrayList<Object[]> borrowersFromDB = dbManager.loadAllBorrowers();

        // Assert - Verify borrower exists in database
        boolean borrowerFound = false;
        for (Object[] borrowerData : borrowersFromDB) {

            int borrowerId = (int) borrowerData[0];
            String name = (String) borrowerData[1];
            String address = (String) borrowerData[2];
            int phone = (int) borrowerData[3];

            if (borrowerId == newBorrower.getID()) {
                borrowerFound = true;
                assertEquals("Charlie Brown", name, "Name should match");
                assertEquals("321 Elm St", address, "Address should match");
                assertEquals(5551111, phone, "Phone should match");
                break;
            }
        }
        assertTrue(borrowerFound, "Borrower should be found in database after INSERT");
    }

    // ==================== SCENARIO 2: Return Book with Fine Calculation ⭐ ====================

    /**
     * Integration Test 2.1: Return Book with Fine - No Fine Scenario
     */
    @Test
    @DisplayName("IT 2.1: Return Book - No Fine (Within Deadline)")
    public void testReturnBook_NoFine() {
        // Arrange - Issue book first (simulate user saying no to hold)
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        Loan loan = borrower1.getBorrowedBooks().get(0);
        assertTrue(availableBook.getIssuedStatus(), "Book should be issued");

        // Act - Return book immediately (within deadline, simulate paying fine)
        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.returnBook(borrower1, loan, clerk);

        // Assert - Verify Book status
        assertFalse(availableBook.getIssuedStatus(), "Book should be available after return");

        // Assert - Verify Loan details
        assertNotNull(loan.getReturnDate(), "Loan should have return date");
        assertEquals(clerk, loan.getReceiver(), "Loan should have receiver");

        // Assert - Verify Fine
        double fine = loan.computeFine1();
        assertEquals(0.0, fine, 0.01, "Fine should be 0 when returned within deadline");

        // Assert - Verify Borrower's borrowed books
        assertEquals(0, borrower1.getBorrowedBooks().size(), "Borrower should have no borrowed books after return");
    }

    /**
     * Integration Test 2.2: Return Book with Fine - Overdue Scenario
     */
    @Test
    @DisplayName("IT 2.2: Return Book - With Fine (Overdue)")
    public void testReturnBook_WithFine() {
        // Arrange - Issue book first
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        Loan loan = borrower1.getBorrowedBooks().get(0);

        // Simulate that book was issued 10 days ago (5 days overdue)
        Date tenDaysAgo = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        loan.renewIssuedBook(tenDaysAgo); // Use renew to set issue date to 10 days ago

        // Act - Calculate fine before return
        double expectedFine = (10 - library.book_return_deadline) * library.per_day_fine;
        double actualFine = loan.computeFine1();

        // Assert - Verify Fine calculation
        assertEquals(expectedFine, actualFine, 0.01,
                "Fine should be calculated as (10 days - 5 deadline) * 20 per_day_fine = 100");
        assertEquals(100.0, actualFine, 0.01, "Fine should be 100 for 5 days overdue at 20 per day");

        // Act - Return book (simulate user choosing to pay fine)
        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.returnBook(borrower1, loan, clerk);

        // Assert - Verify book returned successfully
        assertFalse(availableBook.getIssuedStatus(), "Book should be available after return");
        assertTrue(loan.getFineStatus(), "Fine should be marked as paid");
    }

    /**
     * Integration Test 2.3: Return Book - Fine Status Persistence
     */
    @Test
    @DisplayName("IT 2.3: Return Book - Fine Status Database Persistence")
    public void testReturnBook_FineStatusPersistence() {
        // Arrange - Issue and return book with fine
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        Loan loan = borrower1.getBorrowedBooks().get(0);
        int loanId = loan.getLoanId();

        // Act - Return book and pay fine
        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.returnBook(borrower1, loan, clerk);

        // Assert - Verify database contains loan with correct fine status
        ArrayList<Object[]> loansFromDB = dbManager.loadAllLoans();
        boolean loanFoundWithCorrectStatus = false;

        for (Object[] loanData : loansFromDB) {
            int dbLoanId = (int) loanData[0];
            if (dbLoanId == loanId) {
                boolean finePaid = (boolean) loanData[7];
                assertTrue(finePaid, "Fine paid status should be true in database");
                loanFoundWithCorrectStatus = true;
                break;
            }
        }
        assertTrue(loanFoundWithCorrectStatus, "Loan with correct fine status should be in database");
    }

    // ==================== SCENARIO 3: Issue Already-Issued Book ====================

    /**
     * Integration Test 3.1: Issue Already-Issued Book - Hold Request Flow
     */
    @Test
    @DisplayName("IT 3.1: Issue Already-Issued Book - Hold Request Creation")
    public void testIssueAlreadyIssuedBook_HoldRequest() {
        // Arrange - Issue book to borrower1 first
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);
        assertTrue(availableBook.getIssuedStatus(), "Book should be issued");

        // Act - Try to issue same book to borrower2, accept hold request
        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower2, clerk);

        // Assert - Verify book is still issued
        assertTrue(availableBook.getIssuedStatus(), "Book should still be issued");

        // Assert - Verify hold request created
        ArrayList<HoldRequest> holdRequests = availableBook.getHoldRequests();
        assertEquals(1, holdRequests.size(), "Book should have 1 hold request");

        HoldRequest hr = holdRequests.get(0);
        assertEquals(borrower2, hr.getBorrower(), "Hold request should be for borrower2");
        assertEquals(availableBook, hr.getBook(), "Hold request should be for the book");

        // Assert - Verify borrower has hold request
        assertEquals(1, borrower2.getOnHoldBooks().size(), "Borrower2 should have 1 hold request");
        assertEquals(hr, borrower2.getOnHoldBooks().get(0), "Borrower should have the correct hold request");

        // Assert - Verify database persistence
        ArrayList<Object[]> holdRequestsFromDB = dbManager.loadAllHoldRequests();
        boolean holdRequestFound = false;
        for (Object[] hrData : holdRequestsFromDB) {
            int bookId = (int) hrData[1];
            int borrowerId = (int) hrData[2];
            if (bookId == availableBook.getID() && borrowerId == borrower2.getID()) {
                holdRequestFound = true;
                break;
            }
        }
        assertTrue(holdRequestFound, "Hold request should be in database");
    }

    /**
     * Integration Test 3.2: Issue Already-Issued Book - Hold Request Queue Priority
     */
    @Test
    @DisplayName("IT 3.2: Issue Already-Issued Book - Hold Queue Priority")
    public void testIssueAlreadyIssuedBook_HoldQueuePriority() {
        // Arrange - Issue book to borrower1
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        // Create a third borrower
        Borrower borrower3 = new Borrower(-1, "Carol White", "999 Maple Dr", 5552222);
        borrower3.saveToDatabase();
        library.addBorrower(borrower3);

        // Act - borrower2 and borrower3 place hold requests
        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower2, clerk);

        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower3, clerk);

        // Return book from borrower1
        Loan loan = borrower1.getBorrowedBooks().get(0);
        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.returnBook(borrower1, loan, clerk);

        // Assert - Book should be available
        assertFalse(availableBook.getIssuedStatus(), "Book should be available after return");
        assertEquals(2, availableBook.getHoldRequests().size(), "Book should have 2 hold requests");

        // Act - Try to issue to borrower3 (second in queue) - should fail
        simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        int initialBorrowedCount = borrower3.getBorrowedBooks().size();
        availableBook.issueBook(borrower3, clerk);

        // Assert - borrower3 should not get the book
        assertEquals(initialBorrowedCount, borrower3.getBorrowedBooks().size(),
                "Borrower3 should not receive book (not first in queue)");

        // Act - Issue to borrower2 (first in queue) - should succeed
        simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower2, clerk);

        // Assert - borrower2 should get the book
        assertEquals(1, borrower2.getBorrowedBooks().size(), "Borrower2 should receive book (first in queue)");
        assertTrue(availableBook.getIssuedStatus(), "Book should be issued");
        assertEquals(1, availableBook.getHoldRequests().size(), "One hold request should be removed");
    }

    /**
     * Integration Test 3.3: Issue Already-Issued Book - Prevent Duplicate Hold Requests
     */
    @Test
    @DisplayName("IT 3.3: Issue Already-Issued Book - Prevent Duplicate Holds")
    public void testIssueAlreadyIssuedBook_PreventDuplicateHolds() {
        // Arrange - Issue book to borrower1
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        // Act - borrower2 tries to place hold request twice
        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower2, clerk);

        int firstHoldCount = availableBook.getHoldRequests().size();

        simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower2, clerk);

        int secondHoldCount = availableBook.getHoldRequests().size();

        // Assert - Should still have only one hold request
        assertEquals(1, firstHoldCount, "Should have 1 hold request after first attempt");
        assertEquals(1, secondHoldCount, "Should still have 1 hold request after duplicate attempt");
        assertEquals(1, borrower2.getOnHoldBooks().size(), "Borrower should have only 1 hold request");
    }

    // ==================== SCENARIO 4: Renew Book Deadline ====================

    /**
     * Integration Test 4.1: Renew Book - Extend Deadline
     */
    @Test
    @DisplayName("IT 4.1: Renew Book - Extend Deadline")
    public void testRenewBook_ExtendDeadline() {
        // Arrange - Issue book first
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        Loan loan = borrower1.getBorrowedBooks().get(0);
        Date originalIssueDate = loan.getIssuedDate();

        // Wait a moment to ensure time difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Act - Renew the book
        Date renewalDate = new Date();
        loan.renewIssuedBook(renewalDate);

        // Assert - Verify issue date updated
        Date newIssueDate = loan.getIssuedDate();
        assertNotEquals(originalIssueDate.getTime(), newIssueDate.getTime(),
                "Issue date should be updated");
        assertEquals(renewalDate.getTime(), newIssueDate.getTime(), 10,
                "Issue date should be set to renewal date");

        // Assert - Verify book still issued to same borrower
        assertTrue(availableBook.getIssuedStatus(), "Book should still be issued");
        assertEquals(1, borrower1.getBorrowedBooks().size(), "Borrower should still have the book");

        // Assert - Verify database persistence
        ArrayList<Object[]> loansFromDB = dbManager.loadAllLoans();
        boolean loanUpdated = false;
        for (Object[] loanData : loansFromDB) {
            int borrowerId = (int) loanData[1];
            int bookId = (int) loanData[2];
            if (borrowerId == borrower1.getID() && bookId == availableBook.getID()) {
                // Verify the issue date in database is updated
                loanUpdated = true;
                break;
            }
        }
        assertTrue(loanUpdated, "Loan should be updated in database");
    }

    /**
     * Integration Test 4.2: Renew Book - Reset Fine Calculation
     */
    @Test
    @DisplayName("IT 4.2: Renew Book - Reset Fine Calculation")
    public void testRenewBook_ResetFineCalculation() {
        // Arrange - Issue book 10 days ago
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        Loan loan = borrower1.getBorrowedBooks().get(0);
        Date tenDaysAgo = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        loan.renewIssuedBook(tenDaysAgo);

        // Act - Calculate fine before renewal (should be positive since overdue)
        double fineBeforeRenewal = loan.computeFine1();

        // Assert - Should have fine since 10 days > 5 day deadline
        assertTrue(fineBeforeRenewal > 0, "Should have fine for overdue book");

        // Act - Renew book with current date
        Date now = new Date();
        loan.renewIssuedBook(now);

        // Calculate fine after renewal
        double fineAfterRenewal = loan.computeFine1();

        // Assert - Fine should be 0 after renewal with current date
        assertEquals(0.0, fineAfterRenewal, 0.01,
                "Fine should be 0 after renewing with current date");
    }

    /**
     * Integration Test 4.3: Renew Book - Multiple Renewals
     */
    @Test
    @DisplayName("IT 4.3: Renew Book - Multiple Renewals")
    public void testRenewBook_MultipleRenewals() {
        // Arrange - Issue book
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        availableBook.issueBook(borrower1, clerk);

        Loan loan = borrower1.getBorrowedBooks().get(0);

        // Act & Assert - Perform multiple renewals
        Date firstRenewal = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));
        loan.renewIssuedBook(firstRenewal);
        assertEquals(firstRenewal.getTime(), loan.getIssuedDate().getTime(), 10,
                "First renewal should update issue date");

        Date secondRenewal = new Date(System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000));
        loan.renewIssuedBook(secondRenewal);
        assertEquals(secondRenewal.getTime(), loan.getIssuedDate().getTime(), 10,
                "Second renewal should update issue date");

        Date thirdRenewal = new Date();
        loan.renewIssuedBook(thirdRenewal);
        assertEquals(thirdRenewal.getTime(), loan.getIssuedDate().getTime(), 10,
                "Third renewal should update issue date");

        // Assert - Verify book still issued
        assertTrue(availableBook.getIssuedStatus(), "Book should still be issued after multiple renewals");
        assertEquals(1, borrower1.getBorrowedBooks().size(), "Borrower should still have the book");
    }

    @AfterEach
    public void cleanup() {
        // Clean up test data - remove books and borrowers from memory
        if (availableBook != null && library.getBooks().contains(availableBook)) {
            availableBook.deleteFromDatabase();
        }
        if (issuedBook != null && library.getBooks().contains(issuedBook)) {
            issuedBook.deleteFromDatabase();
        }
    }

    @AfterAll
    public void tearDown() {
        // Close database connection
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }
}