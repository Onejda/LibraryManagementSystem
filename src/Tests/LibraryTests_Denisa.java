package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Tests for Library.java – Denisa
 * 20 tests
 * Methods tested:
 *  - computeFine2(Borrower)
 *  - removeBookfromLibrary(Book)
 *  - createBook(String, String, String)
 *  - setFine(double)
 *  - getBooks()
 */
public class LibraryTests_Denisa {

    private Library library;
    private Borrower borrower;
    private Clerk clerk;

    @BeforeEach
    void setUp() {
        // Reset singleton & static counters
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);
        Clerk.setDeskCount(0);

        library = Library.getInstance();

        // Initialize DB
        library.makeConnection();

        // Clear in-memory state
        library.getBooks().clear();
        library.getLoans().clear();
        library.getPersons().clear();

        library.setFine(10);
        library.setRequestExpiry(1);

        borrower = new Borrower(-1, "Denisa", "Addr", 123);
        clerk = new Clerk(-1, "Clerk", "Addr", 111, 1000, 1);

        borrower.saveToDatabase();
        clerk.saveToDatabase();

        library.addBorrower(borrower);
        library.addClerk(clerk);
    }

    // ======================= createBook =======================

    @Test
    @DisplayName("createBook - Adds book to library")
    void testCreateBook_AddsBook() {
        int initialSize = library.getBooks().size();

        library.createBook("Clean Code", "SE", "Martin");

        assertEquals(initialSize + 1, library.getBooks().size());
    }

    @Test
    @DisplayName("createBook - Stores correct data")
    void testCreateBook_StoresCorrectData() {
        library.createBook("Refactoring", "SE", "Fowler");

        Book b = library.getBooks().get(0);

        assertEquals("Refactoring", b.getTitle());
        assertEquals("SE", b.getSubject());
        assertEquals("Fowler", b.getAuthor());
    }

    @Test
    @DisplayName("createBook - Multiple books created")
    void testCreateBook_MultipleBooksCreated() {
        library.createBook("A", "S", "A");
        library.createBook("B", "S", "B");

        assertEquals(2, library.getBooks().size());
    }

    // ======================= getBooks =======================

    @Test
    @DisplayName("getBooks - Empty library returns empty list")
    void testGetBooks_Empty() {
        assertTrue(library.getBooks().isEmpty());
    }

    @Test
    @DisplayName("getBooks - Returns correct number of books")
    void testGetBooks_AfterAdd() {
        library.createBook("A", "S", "A");
        library.createBook("B", "S", "B");

        assertEquals(2, library.getBooks().size());
    }

    @Test
    @DisplayName("getBooks - Reflects live changes")
    void testGetBooks_LiveUpdate() {
        library.createBook("A", "S", "A");
        assertEquals(1, library.getBooks().size());

        library.createBook("B", "S", "B");
        assertEquals(2, library.getBooks().size());
    }


    // ======================= setFine =======================

    @Test
    @DisplayName("setFine - Sets positive fine")
    void testSetFine_Positive() {
        library.setFine(15.5);
        assertEquals(15.5, library.per_day_fine);
    }

    @Test
    @DisplayName("setFine - Allows zero fine")
    void testSetFine_Zero() {
        library.setFine(0);
        assertEquals(0, library.per_day_fine);
    }

    @Test
    @DisplayName("setFine - Allows negative fine (no validation)")
    void testSetFine_Negative() {
        library.setFine(-5);
        assertEquals(-5, library.per_day_fine);
    }

    @Test
    @DisplayName("setFine - Overwrites previous value")
    void testSetFine_MultipleUpdates() {
        library.setFine(5);
        library.setFine(20);

        assertEquals(20, library.per_day_fine);
    }

    @Test
    @DisplayName("setFine - Accepts large value")
    void testSetFine_LargeValue() {
        library.setFine(Double.MAX_VALUE);

        assertEquals(Double.MAX_VALUE, library.per_day_fine);
    }



    // ======================= removeBookfromLibrary =======================

    @Test
    @DisplayName("removeBookfromLibrary - Removes available book")
    void testRemoveBook_RemovesBook() {
        library.createBook("Temp", "X", "Y");
        Book b = library.getBooks().get(0);

        library.removeBookfromLibrary(b);

        assertFalse(library.getBooks().contains(b));
    }

    @Test
    @DisplayName("removeBookfromLibrary - Does not affect other books")
    void testRemoveBook_DoesNotAffectOtherBooks() {
        library.createBook("Book1", "S", "A");
        library.createBook("Book2", "S", "B");

        Book b1 = library.getBooks().get(0);
        Book b2 = library.getBooks().get(1);

        library.removeBookfromLibrary(b1);

        assertEquals(1, library.getBooks().size());
        assertEquals(b2, library.getBooks().get(0));
    }

    @Test
    @DisplayName("removeBookfromLibrary - Removing from empty library does nothing")
    void testRemoveBookFromEmptyLibrary() {
        Book fake = new Book(-1, "Fake", "X", "Y", false);

        assertDoesNotThrow(() -> library.removeBookfromLibrary(fake));
    }

    @Test
    @DisplayName("removeBookfromLibrary - Removes last book")
    void testRemoveBook_LastBook() {
        library.createBook("Solo", "S", "A");
        Book b = library.getBooks().get(0);

        library.removeBookfromLibrary(b);

        assertTrue(library.getBooks().isEmpty());
    }

    @Test
    @DisplayName("removeBookfromLibrary - Non-existing book safe")
    void testRemoveBook_NotInLibrary() {
        Book fake = new Book(-1, "Ghost", "X", "Y", false);

        assertDoesNotThrow(() -> library.removeBookfromLibrary(fake));
    }



    // ======================= computeFine2 =======================

    @Test
    @DisplayName("computeFine2 - No loans returns zero")
    void testComputeFine_NoLoans() {
        double fine = library.computeFine2(borrower);
        assertEquals(0.0, fine);
    }

    @Test
    @DisplayName("computeFine2 - Calculates overdue fine")
    void testComputeFine_WithLoan() {
        Book b = new Book(-1, "Clean Code", "SE", "Martin", false);
        b.saveToDatabase();
        library.addBookinLibrary(b);

        // 2 days old loan
        Date issued = new Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000);
        Date returned = new Date();

        Loan loan = new Loan(borrower, b, clerk, clerk, issued, returned, false);
        loan.saveToDatabase();
        library.addLoan(loan);

        double fine = library.computeFine2(borrower);

        assertEquals(20.0, fine, 0.01);
    }

    @Test
    @DisplayName("computeFine2 - Same day return gives zero fine")
    void testComputeFine_SameDayReturn() {
        Book b = new Book(-1, "Instant", "S", "A", false);
        b.saveToDatabase();
        library.addBookinLibrary(b);

        Date now = new Date();
        Loan loan = new Loan(borrower, b, clerk, clerk, now, now, false);
        loan.saveToDatabase();
        library.addLoan(loan);

        double fine = library.computeFine2(borrower);
        assertEquals(0.0, fine, 0.01);
    }

    @Test
    @DisplayName("computeFine2 - Ignores loans of other borrowers")
    void testComputeFine_OtherBorrowerIgnored() {
        Borrower other = new Borrower(-1, "Other", "X", 999);
        other.saveToDatabase();
        library.addBorrower(other);

        Book b = new Book(-1, "Book", "S", "A", false);
        b.saveToDatabase();
        library.addBookinLibrary(b);

        Date twoDaysAgo = new Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000);
        Date now = new Date();

        Loan loan = new Loan(other, b, clerk, clerk, twoDaysAgo, now, false);
        loan.saveToDatabase();
        library.addLoan(loan);

        double fine = library.computeFine2(borrower);

        assertEquals(0.0, fine);
    }
}
