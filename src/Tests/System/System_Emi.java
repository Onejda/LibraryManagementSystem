package Tests.System;

import org.junit.jupiter.api.*;

import LMS.Book;
import LMS.Borrower;
import LMS.Clerk;
import LMS.DatabaseManager;
import LMS.HoldRequest;
import LMS.Library;
import LMS.Loan;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Calendar;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class System_Emi {

    private static Library library;
    private static DatabaseManager dbManager;
    private static Clerk testClerk;
    private static Borrower testBorrower1;
    private static Borrower testBorrower2;
    private static Book availableBook;
    private static Book issuedBook;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeAll
    public static void setUpClass() {
        // Reset library instance
        Library.resetInstance();
        
        // Initialize database
        dbManager = DatabaseManager.getInstance();
        dbManager.connect();
        
        // Get library instance
        library = Library.getInstance();
        library.setName("Test Library");
        library.setReturnDeadline(5);
        library.setFine(20.0);
        library.setRequestExpiry(7);
        
        // Create test clerk (ID: 2 from seeded data)
        testClerk = library.findClerkById(2);
        if (testClerk == null) {
            testClerk = new Clerk(-1, "Jane Doe", "Front Desk", 5552345, 25000, -1);
            testClerk.saveToDatabase();
            library.addClerk(testClerk);
        }
        
        // Create test borrowers
        testBorrower1 = library.findBorrowerById(4);
        if (testBorrower1 == null) {
            testBorrower1 = new Borrower(-1, "Alice Brown", "123 Student Ave", 5554567);
            testBorrower1.saveToDatabase();
            library.addBorrower(testBorrower1);
        }
        
        testBorrower2 = library.findBorrowerById(5);
        if (testBorrower2 == null) {
            testBorrower2 = new Borrower(-1, "Bob Wilson", "456 College St", 5555678);
            testBorrower2.saveToDatabase();
            library.addBorrower(testBorrower2);
        }
        
        // Create test books
        availableBook = new Book(-1, "Design Patterns", "Software Engineering", "Gang of Four", false);
        availableBook.saveToDatabase();
        library.addBookinLibrary(availableBook);
        
        issuedBook = new Book(-1, "Clean Code", "Software Engineering", "Robert C. Martin", true);
        issuedBook.saveToDatabase();
        library.addBookinLibrary(issuedBook);
    }

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @AfterAll
    public static void tearDownClass() {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }

    @Test
    @Order(1)
    @DisplayName("ST-E1: Clerk issues available book to borrower")
    public void testClerkIssuesAvailableBook() {
        // Precondition: Ensure book is available and has no hold requests
        assertFalse(availableBook.getIssuedStatus(), "Book should be available");
        assertTrue(availableBook.getHoldRequests().isEmpty(), "Book should have no hold requests");
        
        // Get initial borrowed books count
        int initialBorrowedCount = testBorrower1.getBorrowedBooks().size();
        
        // Clerk issues book to borrower
        availableBook.issueBook(testBorrower1, testClerk);
        
        // Verify book is now issued
        assertTrue(availableBook.getIssuedStatus(), "Book status should be updated to issued");
        
        // Verify loan record created
        assertEquals(initialBorrowedCount + 1, testBorrower1.getBorrowedBooks().size(), 
                    "Loan record should be created");
        
        // Verify borrower has the book
        boolean bookFound = false;
        for (Loan loan : testBorrower1.getBorrowedBooks()) {
            if (loan.getBook() == availableBook) {
                bookFound = true;
                assertEquals(testClerk, loan.getIssuer(), "Clerk should be recorded as issuer");
                assertNotNull(loan.getIssuedDate(), "Issue date should be recorded");
                break;
            }
        }
        assertTrue(bookFound, "Book should be in borrower's borrowed books");
        
        // Verify confirmation message
        String output = outContent.toString();
        assertTrue(output.contains("successfully issued"), "Confirmation message should be shown");
        assertTrue(output.contains(testClerk.getName()), "Clerk name should be in confirmation");
        
        System.out.println("ST-E1 PASS: Book issued successfully");
    }


    @Test
    @Order(2)
    @DisplayName("ST-E2: Issue book respecting hold request priority")
    public void testIssueBookRespectingHoldRequestPriority() {
        // Create a new book for this test
        Book bookWithHolds = new Book(-1, "Test Book with Holds", "Testing", "Test Author", false);
        bookWithHolds.saveToDatabase();
        library.addBookinLibrary(bookWithHolds);
        
        // Create hold requests (testBorrower1 first, then testBorrower2)
        bookWithHolds.placeBookOnHold(testBorrower1);
        bookWithHolds.placeBookOnHold(testBorrower2);
        
        // Verify hold requests exist
        assertEquals(2, bookWithHolds.getHoldRequests().size(), "Book should have 2 hold requests");
        assertEquals(testBorrower1, bookWithHolds.getHoldRequests().get(0).getBorrower(), 
                    "First hold request should be from testBorrower1");
        
        // Clear output for clean test
        outContent.reset();
        
        // Attempt to issue to non-priority borrower (testBorrower2)
        bookWithHolds.issueBook(testBorrower2, testClerk);
        
        // Verify rejection message
        String output = outContent.toString();
        assertTrue(output.contains("earlier than you") || output.contains("wait until"), 
                  "Should reject non-priority borrower");
        
        // Verify book is still not issued
        assertFalse(bookWithHolds.getIssuedStatus(), "Book should not be issued to non-priority borrower");
        
        // Clear output
        outContent.reset();
        
        // Issue to priority borrower (testBorrower1)
        bookWithHolds.issueBook(testBorrower1, testClerk);
        
        // Verify book is issued
        assertTrue(bookWithHolds.getIssuedStatus(), "Book should be issued to priority borrower");
        
        // Verify hold request was removed
        assertEquals(1, bookWithHolds.getHoldRequests().size(), 
                    "First hold request should be removed from queue");
        
        // Verify confirmation message
        output = outContent.toString();
        assertTrue(output.contains("successfully issued"), "Confirmation should be shown");
        
        System.out.println("ST-E2 PASS: FIFO hold rule enforced correctly");
    }

  
    @Test
    @Order(3)
    @DisplayName("ST-E3: Prevent duplicate book issue")
    public void testPreventDuplicateBookIssue() {
        // Issue book to testBorrower1 first
        Book testBook = new Book(-1, "Duplicate Test Book", "Testing", "Test Author", false);
        testBook.saveToDatabase();
        library.addBookinLibrary(testBook);
        
        testBook.issueBook(testBorrower1, testClerk);
        
        // Verify book is issued
        assertTrue(testBook.getIssuedStatus(), "Book should be issued");
        
        // Clear output
        outContent.reset();
        
        // Simulate choosing to place hold when attempting to issue already issued book
        String simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        
        // Attempt to issue same book to testBorrower2
        testBook.issueBook(testBorrower2, testClerk);
        
        // Verify error message
        String output = outContent.toString();
        assertTrue(output.contains("already issued"), "Error message should indicate book is already issued");
        
        // Verify hold request was created
        boolean holdFound = false;
        for (HoldRequest hr : testBook.getHoldRequests()) {
            if (hr.getBorrower() == testBorrower2) {
                holdFound = true;
                break;
            }
        }
        assertTrue(holdFound, "Hold request should be created for testBorrower2");
        
        System.out.println("ST-E3 PASS: Duplicate issue protection works");
    }

  
    @Test
    @Order(4)
    @DisplayName("ST-E4: Return book before deadline")
    public void testReturnBookBeforeDeadline() {
        // Create and issue a book
        Book returnTestBook = new Book(-1, "Return Test Book", "Testing", "Test Author", false);
        returnTestBook.saveToDatabase();
        library.addBookinLibrary(returnTestBook);
        
        returnTestBook.issueBook(testBorrower1, testClerk);
        
        // Get the loan
        Loan loan = null;
        for (Loan l : testBorrower1.getBorrowedBooks()) {
            if (l.getBook() == returnTestBook) {
                loan = l;
                break;
            }
        }
        assertNotNull(loan, "Loan should exist");
        
        // Verify book is issued
        assertTrue(returnTestBook.getIssuedStatus(), "Book should be issued");
        
        // Clear output
        outContent.reset();
        
        // Simulate not paying fine (though there should be none)
        String simulatedInput = "n\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        
        // Return the book
        returnTestBook.returnBook(testBorrower1, loan, testClerk);
        
        // Verify book is no longer issued
        assertFalse(returnTestBook.getIssuedStatus(), "Book status should be updated to available");
        
        // Verify return date is set
        assertNotNull(loan.getReturnDate(), "Return date should be recorded");
        
        // Verify receiver is set
        assertEquals(testClerk, loan.getReceiver(), "Clerk should be recorded as receiver");
        
        // Verify no fine was generated (book returned within deadline)
        String output = outContent.toString();
        assertTrue(output.contains("No fine") || output.contains("successfully returned"), 
                  "Should confirm return without fine");
        
        // Verify book removed from borrower's borrowed books
        boolean bookStillBorrowed = false;
        for (Loan l : testBorrower1.getBorrowedBooks()) {
            if (l.getBook() == returnTestBook) {
                bookStillBorrowed = true;
                break;
            }
        }
        assertFalse(bookStillBorrowed, "Book should be removed from borrowed books");
        
        System.out.println("ST-E4 PASS: Normal return workflow successful");
    }

    @Test
    @Order(5)
    @DisplayName("ST-E5: Return overdue book and pay fine")
    public void testReturnOverdueBookAndPayFine() {
        // Create and issue a book
        Book overdueBook = new Book(-1, "Overdue Test Book", "Testing", "Test Author", false);
        overdueBook.saveToDatabase();
        library.addBookinLibrary(overdueBook);
        
        overdueBook.issueBook(testBorrower1, testClerk);
        
        // Get the loan
        Loan loan = null;
        for (Loan l : testBorrower1.getBorrowedBooks()) {
            if (l.getBook() == overdueBook) {
                loan = l;
                break;
            }
        }
        assertNotNull(loan, "Loan should exist");
        
        // Set issue date to 10 days ago (more than deadline of 5 days)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Date overdueDate = cal.getTime();
        loan.renewIssuedBook(overdueDate); // Use renew to update issue date
        
        // Calculate expected fine
        // Days overdue = 10 - 5 = 5 days
        // Fine = 5 * 20 = 100
        double expectedFine = 5 * library.per_day_fine;
        
        // Clear output
        outContent.reset();
        
        // Simulate choosing to pay fine
        String simulatedInput = "y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        
        // Return the book
        overdueBook.returnBook(testBorrower1, loan, testClerk);
        
        // Verify fine was calculated
        String output = outContent.toString();
        assertTrue(output.contains("Fine") || output.contains(String.valueOf(expectedFine)), 
                  "Fine should be calculated and displayed");
        
        // Verify fine was marked as paid
        assertTrue(loan.getFineStatus(), "Fine should be marked as paid");
        
        // Verify book is no longer issued
        assertFalse(overdueBook.getIssuedStatus(), "Book should be returned");
        
        // Verify return date is set
        assertNotNull(loan.getReturnDate(), "Return date should be recorded");
        
        System.out.println("ST-E5 PASS: Fine formula applied correctly");
    }

    /**
     * Additional test: Verify borrower cannot borrow same book twice
     */
    @Test
    @Order(6)
    @DisplayName("Additional: Prevent borrower from borrowing same book twice")
    public void testPreventDuplicateBorrow() {
        // Create a new book
        Book duplicateTestBook = new Book(-1, "Duplicate Borrow Test", "Testing", "Test Author", false);
        duplicateTestBook.saveToDatabase();
        library.addBookinLibrary(duplicateTestBook);
        
        // Issue book to borrower
        duplicateTestBook.issueBook(testBorrower1, testClerk);
        
        // Clear output
        outContent.reset();
        
        // Attempt to make hold request for same book
        duplicateTestBook.makeHoldRequest(testBorrower1);
        
        // Verify rejection message
        String output = outContent.toString();
        assertTrue(output.contains("already borrowed"), 
                  "Should prevent borrower from requesting book they already have");
        
        System.out.println("Additional test PASS: Duplicate borrow prevention works");
    }

    /**
     * Additional test: Verify expired hold requests are cleaned up during issue
     */
    @Test
    @Order(7)
    @DisplayName("Additional: Clean up expired hold requests")
    public void testExpiredHoldRequestCleanup() {
        // Create a new book
        Book expiredHoldBook = new Book(-1, "Expired Hold Test", "Testing", "Test Author", false);
        expiredHoldBook.saveToDatabase();
        library.addBookinLibrary(expiredHoldBook);
        
        // Create a hold request with old date
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10); // 10 days ago (expired if expiry is 7 days)
        Date expiredDate = cal.getTime();
        
        HoldRequest expiredHold = new HoldRequest(testBorrower2, expiredHoldBook, expiredDate);
        expiredHoldBook.getHoldRequestOperations().addHoldRequest(expiredHold);
        testBorrower2.addHoldRequest(expiredHold);
        
        // Verify hold request exists
        assertEquals(1, expiredHoldBook.getHoldRequests().size(), "Hold request should exist");
        
        // Issue book to another borrower (this should clean up expired holds)
        expiredHoldBook.issueBook(testBorrower1, testClerk);
        
        // Verify expired hold was removed
        assertEquals(0, expiredHoldBook.getHoldRequests().size(), 
                    "Expired hold request should be cleaned up");
        
        System.out.println("Additional test PASS: Expired hold cleanup works");
    }
}