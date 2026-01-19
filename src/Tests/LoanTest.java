package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Unit Tests for Loan.java – Denisa
 * 17 tests
 * Covered:
 *  - Constructor
 *  - Getters & setters
 *  - computeFine1()
 *  - renewIssuedBook() (state change)
 */
public class LoanTest {

    private Library library;
    private Borrower borrower;
    private Clerk issuer;
    private Clerk receiver;
    private Book book;
    private Loan loan;

    @BeforeEach
    void setUp() {
        // Reset system
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);
        Clerk.setDeskCount(0);

        library = Library.getInstance();
        library.makeConnection();

        library.getBooks().clear();
        library.getLoans().clear();
        library.getPersons().clear();

        library.setFine(10);          // per_day_fine
        library.setRequestExpiry(1);  // book_return_deadline

        borrower = new Borrower(-1, "Denisa", "Addr", 111);
        issuer   = new Clerk(-1, "Issuer", "Addr", 222, 1200, 1);
        receiver = new Clerk(-1, "Receiver", "Addr", 333, 1200, 2);

        borrower.saveToDatabase();
        issuer.saveToDatabase();
        receiver.saveToDatabase();

        library.addBorrower(borrower);
        library.addClerk(issuer);
        library.addClerk(receiver);

        book = new Book(-1, "Clean Code", "SE", "Martin", false);
        book.saveToDatabase();
        library.addBookinLibrary(book);

        Date issued = new Date(System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000);
        Date returned = new Date();

        loan = new Loan(borrower, book, issuer, receiver, issued, returned, false);
    }

    // ===================== Constructor & Getters =====================

    @Test
    @DisplayName("Constructor initializes loan correctly")
    void testConstructor() {
        assertNotNull(loan);
    }

    @Test
    void testGetBorrower() {
        assertEquals(borrower, loan.getBorrower());
    }

    @Test
    void testGetBook() {
        assertEquals(book, loan.getBook());
    }

    @Test
    void testGetIssuer() {
        assertEquals(issuer, loan.getIssuer());
    }

    @Test
    void testGetReceiver() {
        assertEquals(receiver, loan.getReceiver());
    }

    @Test
    void testGetIssuedDate() {
        assertNotNull(loan.getIssuedDate());
    }

    @Test
    void testGetReturnDate() {
        assertNotNull(loan.getReturnDate());
    }

    // ===================== Setters =====================

    @Test
    void testSetLoanId() {
        loan.setLoanId(10);
        assertEquals(10, loan.getLoanId());
    }

    @Test
    void testSetReturnedDate() {
        Date now = new Date();
        loan.setReturnedDate(now);
        assertEquals(now, loan.getReturnDate());
    }

    @Test
    void testSetFineStatus() {
        loan.setFineStatus(true);
        assertTrue(loan.getFineStatus());
    }

    @Test
    void testSetReceiver() {
        loan.setReceiver(receiver);
        assertEquals(receiver, loan.getReceiver());
    }

    // ===================== computeFine1 =====================

    @Test
    @DisplayName("computeFine1 - Overdue loan generates fine")
    void testComputeFine_Overdue() {
        library.setReturnDeadline(1);
        library.setFine(10);

        double fine = loan.computeFine1();

        // 3 days - 1 day deadline = 2 days * 10
        assertEquals(20.0, fine, 0.01);
    }


    @Test
    @DisplayName("computeFine1 - No fine if returned within deadline")
    void testComputeFine_WithinDeadline() {
        Date issued = new Date(System.currentTimeMillis() - 12 * 60 * 60 * 1000);
        Loan freshLoan = new Loan(borrower, book, issuer, receiver, issued, new Date(), false);

        double fine = freshLoan.computeFine1();
        assertEquals(0.0, fine);
    }

    @Test
    @DisplayName("computeFine1 - Fine is zero if already paid")
    void testComputeFine_FinePaid() {
        loan.setFineStatus(true);

        double fine = loan.computeFine1();
        assertEquals(0.0, fine);
    }

    @Test
    @DisplayName("computeFine1 - Null return date uses current date")
    void testComputeFine_NullReturnDate() {
        Loan openLoan = new Loan(
                borrower,
                book,
                issuer,
                null,
                new Date(System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000),
                null,
                false
        );

        double fine = openLoan.computeFine1();
        assertTrue(fine > 0);
    }

    @Test
    @DisplayName("computeFine1 - Book not returned yet uses current date")
    void testComputeFine_NotReturnedYet() {
        // deadline = 1 day, fine = 10
        library.setReturnDeadline(1);
        library.setFine(10);

        // Issued 3 days ago, not returned
        Date issued = new Date(System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000);

        Loan loan = new Loan(borrower, book, issuer, null, issued, null, false);

        double fine = loan.computeFine1();

        // (3 days - 1 day deadline) * 10 = 20
        assertEquals(20.0, fine, 0.01);
    }



    // ===================== renewIssuedBook =====================

    @Test
    @DisplayName("renewIssuedBook updates issued date")
    void testRenewIssuedBook() {
        Date newDate = new Date();
        loan.renewIssuedBook(newDate);

        assertEquals(newDate, loan.getIssuedDate());
    }

    // ===================== payFine =====================
    // Not unit-tested due to Scanner input
    // Documented as manual / system test
}
