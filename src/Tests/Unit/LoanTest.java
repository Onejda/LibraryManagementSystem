package Tests.Unit;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Unit Tests for Loan class - Public Methods
 *
 * Methods Under Test (from Loan.java):
 * 1. getLoanId()
 * 2. getBook()
 * 3. getIssuer()
 * 4. getReceiver()
 * 5. getIssuedDate()
 * 6. getReturnDate()
 * 7. getBorrower()
 * 8. getFineStatus()
 * 9. setLoanId(int id)
 * 10. setReturnedDate(Date dReturned)
 * 11. setFineStatus(boolean fStatus)
 * 12. setReceiver(Staff r)
 *
 * Excluded:
 * - computeFine1(): covered in BVT_Denisa
 * - renewIssuedBook(): on system testing
 */

public class LoanTest {

    private Library library;
    private Borrower borrower;
    private Book book;
    private Staff issuer;
    private Staff receiver;
    private Date issueDate;
    private Date returnDate;
    private Loan loan;

    @BeforeEach
    void setUp() {
        Library.resetInstance();
        library = Library.getInstance();
        library.setFine(20.0);
        library.setReturnDeadline(5);

        borrower = new Borrower(1, "Test Borrower", "Test Address", 123456);
        book = new Book(1, "Test Book", "CS", "Author", false);
        issuer = new Clerk(2, "Issuer Clerk", "Office", 111111, 25000, 1);
        receiver = new Clerk(3, "Receiver Clerk", "Office", 222222, 25000, 2);

        issueDate = new Date(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000));
        returnDate = new Date();

        loan = new Loan(borrower, book, issuer, receiver, issueDate, returnDate, false);
    }

    @AfterEach
    void tearDown() {
        Library.resetInstance();
    }

    // ==================== getLoanId ====================

    @Test
    @DisplayName("getLoanId: Default value is -1")
    void testGetLoanId_Default() {
        assertEquals(-1, loan.getLoanId());
    }

    @Test
    @DisplayName("getLoanId: Returns set value")
    void testGetLoanId_Set() {
        loan.setLoanId(10);
        assertEquals(10, loan.getLoanId());
    }

    // ==================== getBook ====================

    @Test
    @DisplayName("getBook: Returns correct book")
    void testGetBook() {
        assertSame(book, loan.getBook());
    }

    // ==================== getIssuer ====================

    @Test
    @DisplayName("getIssuer: Returns issuer staff")
    void testGetIssuer() {
        assertSame(issuer, loan.getIssuer());
    }

    // ==================== getReceiver ====================

    @Test
    @DisplayName("getReceiver: Returns receiver staff")
    void testGetReceiver() {
        assertSame(receiver, loan.getReceiver());
    }

    @Test
    @DisplayName("getReceiver: Null when not returned")
    void testGetReceiver_Null() {
        Loan activeLoan = new Loan(borrower, book, issuer, null, issueDate, null, false);
        assertNull(activeLoan.getReceiver());
    }

    // ==================== getIssuedDate ====================

    @Test
    @DisplayName("getIssuedDate: Returns correct issue date")
    void testGetIssuedDate() {
        assertEquals(issueDate, loan.getIssuedDate());
    }

    // ==================== getReturnDate ====================

    @Test
    @DisplayName("getReturnDate: Returns correct return date")
    void testGetReturnDate() {
        assertEquals(returnDate, loan.getReturnDate());
    }

    @Test
    @DisplayName("getReturnDate: Null when not returned")
    void testGetReturnDate_Null() {
        Loan activeLoan = new Loan(borrower, book, issuer, null, issueDate, null, false);
        assertNull(activeLoan.getReturnDate());
    }

    // ==================== getBorrower ====================

    @Test
    @DisplayName("getBorrower: Returns borrower")
    void testGetBorrower() {
        assertSame(borrower, loan.getBorrower());
    }

    // ==================== getFineStatus ====================

    @Test
    @DisplayName("getFineStatus: Initially false")
    void testGetFineStatus_Default() {
        assertFalse(loan.getFineStatus());
    }

    @Test
    @DisplayName("getFineStatus: True when paid")
    void testGetFineStatus_Paid() {
        Loan paidLoan = new Loan(borrower, book, issuer, receiver, issueDate, returnDate, true);
        assertTrue(paidLoan.getFineStatus());
    }

    // ==================== setLoanId ====================

    @Test
    @DisplayName("setLoanId: Accepts positive value")
    void testSetLoanId_Positive() {
        loan.setLoanId(5);
        assertEquals(5, loan.getLoanId());
    }

    @Test
    @DisplayName("setLoanId: Accepts zero")
    void testSetLoanId_Zero() {
        loan.setLoanId(0);
        assertEquals(0, loan.getLoanId());
    }

    @Test
    @DisplayName("setLoanId: Accepts negative value")
    void testSetLoanId_Negative() {
        loan.setLoanId(-5);
        assertEquals(-5, loan.getLoanId());
    }

    // ==================== setReturnedDate ====================

    @Test
    @DisplayName("setReturnedDate: Sets return date")
    void testSetReturnedDate() {
        Date newDate = new Date();
        loan.setReturnedDate(newDate);
        assertEquals(newDate, loan.getReturnDate());
    }

    @Test
    @DisplayName("setReturnedDate: Accepts null")
    void testSetReturnedDate_Null() {
        loan.setReturnedDate(null);
        assertNull(loan.getReturnDate());
    }

    // ==================== setFineStatus ====================

    @Test
    @DisplayName("setFineStatus: Marks fine as paid")
    void testSetFineStatus_True() {
        loan.setFineStatus(true);
        assertTrue(loan.getFineStatus());
    }

    @Test
    @DisplayName("setFineStatus: Marks fine as unpaid")
    void testSetFineStatus_False() {
        loan.setFineStatus(true);
        loan.setFineStatus(false);
        assertFalse(loan.getFineStatus());
    }

    // ==================== setReceiver ====================

    @Test
    @DisplayName("setReceiver: Sets receiver")
    void testSetReceiver() {
        Loan activeLoan = new Loan(borrower, book, issuer, null, issueDate, null, false);
        activeLoan.setReceiver(receiver);
        assertSame(receiver, activeLoan.getReceiver());
    }

    @Test
    @DisplayName("setReceiver: Accepts null")
    void testSetReceiver_Null() {
        loan.setReceiver(null);
        assertNull(loan.getReceiver());
    }

}
