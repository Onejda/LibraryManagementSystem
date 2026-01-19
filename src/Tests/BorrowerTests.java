package Tests;

import LMS.Book;
import LMS.Borrower;
import LMS.HoldRequest;
import LMS.Loan;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class BorrowerTests {

    private Borrower borrower;

    // =====================================================
    // Test Setup
    // =====================================================

    @BeforeEach
    void setUp() {
        borrower = new Borrower(1, "John Doe", "123 Main St", 1234567890);
    }

    // =====================================================
    // Tests for: Borrower Constructor & Initialization
    // =====================================================

    @Test
    @DisplayName("Test Borrower Creation")
    void testBorrowerCreation() {
        assertNotNull(borrower);
        assertEquals(1, borrower.getID());
        assertEquals("John Doe", borrower.getName());
        assertEquals("123 Main St", borrower.getAddress());
        assertNotNull(borrower.getBorrowedBooks());
        assertNotNull(borrower.getOnHoldBooks());
        assertTrue(borrower.getBorrowedBooks().isEmpty());
        assertTrue(borrower.getOnHoldBooks().isEmpty());
    }

    // =====================================================
    // Tests for: addBorrowedBook(Loan)
    // =====================================================

    @Test
    @DisplayName("Test Add Borrowed Book")
    void testAddBorrowedBook() {
        Book book = new Book(1, "Test Book", "Fiction", "Test Author", false);
        Loan loan = new Loan(borrower, book, null, null, new java.util.Date(), null, false);

        borrower.addBorrowedBook(loan);

        assertEquals(1, borrower.getBorrowedBooks().size());
        assertTrue(borrower.getBorrowedBooks().contains(loan));
    }

    // ❌ Expected to FAIL – null not validated
    @Test
    @DisplayName("addBorrowedBook should reject null loan")
    void testAddBorrowedBookNull() {
        borrower.addBorrowedBook(null);
        assertTrue(borrower.getBorrowedBooks().isEmpty());
    }

    // ❌ Expected to FAIL – duplicates allowed
    @Test
    @DisplayName("addBorrowedBook should not allow duplicate loans")
    void testAddBorrowedBookDuplicate() {
        Book book = new Book(1, "Test Book", "Fiction", "Author", false);
        Loan loan = new Loan(borrower, book, null, null, new java.util.Date(), null, false);

        borrower.addBorrowedBook(loan);
        borrower.addBorrowedBook(loan);

        assertEquals(1, borrower.getBorrowedBooks().size());
    }

    // =====================================================
    // Tests for: removeBorrowedBook(Loan)
    // =====================================================

    @Test
    @DisplayName("Test Remove Borrowed Book")
    void testRemoveBorrowedBook() {
        Book book = new Book(1, "Test Book", "Fiction", "Test Author", false);
        Loan loan = new Loan(borrower, book, null, null, new java.util.Date(), null, false);

        borrower.addBorrowedBook(loan);
        assertEquals(1, borrower.getBorrowedBooks().size());

        borrower.removeBorrowedBook(loan);
        assertEquals(0, borrower.getBorrowedBooks().size());
    }

    // =====================================================
    // Tests for: addHoldRequest(HoldRequest)
    // =====================================================

    @Test
    @DisplayName("Test Add Hold Request")
    void testAddHoldRequest() {
        Book book = new Book(1, "Test Book", "Fiction", "Test Author", true);
        HoldRequest hr = new HoldRequest(borrower, book, new java.util.Date());

        borrower.addHoldRequest(hr);

        assertEquals(1, borrower.getOnHoldBooks().size());
        assertTrue(borrower.getOnHoldBooks().contains(hr));
    }

    // =====================================================
    // Tests for: removeHoldRequest(HoldRequest)
    // =====================================================

    @Test
    @DisplayName("Test Remove Hold Request")
    void testRemoveHoldRequest() {
        Book book = new Book(1, "Test Book", "Fiction", "Test Author", true);
        HoldRequest hr = new HoldRequest(borrower, book, new java.util.Date());

        borrower.addHoldRequest(hr);
        assertEquals(1, borrower.getOnHoldBooks().size());

        borrower.removeHoldRequest(hr);
        assertEquals(0, borrower.getOnHoldBooks().size());
    }

    // =====================================================
    // Tests for: getBorrowedBooks()
    // =====================================================

    @Test
    @DisplayName("Test Get Borrowed Books Returns Correct List")
    void testGetBorrowedBooks() {
        Book book1 = new Book(1, "Book 1", "Fiction", "Author 1", false);
        Book book2 = new Book(2, "Book 2", "Science", "Author 2", false);

        Loan loan1 = new Loan(borrower, book1, null, null, new java.util.Date(), null, false);
        Loan loan2 = new Loan(borrower, book2, null, null, new java.util.Date(), null, false);

        borrower.addBorrowedBook(loan1);
        borrower.addBorrowedBook(loan2);

        ArrayList<Loan> borrowedBooks = borrower.getBorrowedBooks();
        assertEquals(2, borrowedBooks.size());
        assertTrue(borrowedBooks.contains(loan1));
        assertTrue(borrowedBooks.contains(loan2));
    }

    // ❌ Expected to FAIL – encapsulation violation
    @Test
    @DisplayName("External modification of borrowedBooks should not affect borrower")
    void testBorrowedBooksEncapsulationViolation() {
        Book book = new Book(1, "Test Book", "Fiction", "Author", false);
        Loan loan = new Loan(borrower, book, null, null, new java.util.Date(), null, false);

        borrower.addBorrowedBook(loan);

        ArrayList<Loan> borrowed = borrower.getBorrowedBooks();
        borrowed.clear();

        assertEquals(1, borrower.getBorrowedBooks().size());
    }

    // =====================================================
    // Tests for: getOnHoldBooks()
    // =====================================================

    @Test
    @DisplayName("Test Get On Hold Books Returns Correct List")
    void testGetOnHoldBooks() {
        Book book1 = new Book(1, "Book 1", "Fiction", "Author 1", true);
        Book book2 = new Book(2, "Book 2", "Science", "Author 2", true);

        HoldRequest hr1 = new HoldRequest(borrower, book1, new java.util.Date());
        HoldRequest hr2 = new HoldRequest(borrower, book2, new java.util.Date());

        borrower.addHoldRequest(hr1);
        borrower.addHoldRequest(hr2);

        ArrayList<HoldRequest> onHoldBooks = borrower.getOnHoldBooks();
        assertEquals(2, onHoldBooks.size());
        assertTrue(onHoldBooks.contains(hr1));
        assertTrue(onHoldBooks.contains(hr2));
    }

    // =====================================================
    // Tests for: printInfo()
    // =====================================================

    @Test
    @DisplayName("printInfo does not throw exception and prints borrower info")
    void testPrintInfo() {
        assertDoesNotThrow(() -> {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            borrower.printInfo();

            System.setOut(System.out);
            assertTrue(outContent.toString().contains("John Doe"));
        });
    }
}
