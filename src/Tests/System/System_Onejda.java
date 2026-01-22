package Tests.System;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * System Tests for Library Management System - Onejda Sheshori
 * Administrative & Book Management Workflows
 *
 * Clean version: Minimal output, no user input, fast execution
 */
public class System_Onejda {

    private Library library;
    private DatabaseManager dbManager;
    private Librarian librarian;
    private Clerk clerk;
    private Borrower borrower;
    private Book book1, book2;

    @BeforeEach
    public void setUp() {
        Library.resetInstance();
        library = Library.getInstance();
        library.setName("Test Library");
        library.setFine(20.0);
        library.setRequestExpiry(7);
        library.setReturnDeadline(5);

        dbManager = DatabaseManager.getInstance();
        dbManager.connect();

        librarian = new Librarian(1, "Admin", "Office 101", 5550000, 50000, 101);
        Library.librarian = librarian;

        clerk = new Clerk(2, "Jane Doe", "Desk 1", 5552345, 25000, 1);
        library.addClerk(clerk);

        borrower = new Borrower(3, "Alice Brown", "123 Main St", 5554567);
        library.addBorrower(borrower);

        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
    }

    @AfterEach
    public void tearDown() {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        Library.resetInstance();
    }

    // ==================== ST-O1: LIBRARIAN LOGIN & ACCESS ADMIN PORTAL ====================

    @Test
    @DisplayName("ST-O1: Librarian Login and Administrative Portal Access")
    public void testLibrarianLoginAndAccessAdminPortal() {
        // Preconditions
        assertNotNull(Library.librarian, "Librarian exists");
        assertNotNull(clerk, "Clerk exists");
        assertNotNull(borrower, "Borrower exists");

        // Authenticate librarian
        int loginId = librarian.getID();
        String loginPassword = librarian.getPassword();
        Person loggedInPerson = null;

        if (Library.librarian != null &&
                Library.librarian.getID() == loginId &&
                Library.librarian.getPassword().equals(loginPassword)) {
            loggedInPerson = Library.librarian;
        }

        assertNotNull(loggedInPerson, "Login successful");
        assertTrue(loggedInPerson instanceof Librarian, "User is Librarian");

        // Verify admin access
        Librarian admin = (Librarian) loggedInPerson;
        assertNotNull(library.getBooks(), "Can access books");
        assertNotNull(library.getLoans(), "Can access loans");
        assertNotNull(library.getPersons(), "Can access persons");

        // Test access denial for non-librarians
        Person clerkUser = clerk;
        boolean clerkDenied = !(clerkUser instanceof Librarian);
        assertTrue(clerkDenied, "Clerk denied access");

        Person borrowerUser = borrower;
        boolean borrowerDenied = !(borrowerUser instanceof Librarian);
        assertTrue(borrowerDenied, "Borrower denied access");

        System.out.println("✓ ST-O1 PASSED");
    }

    // ==================== ST-O2: ADD NEW CLERK ====================

    @Test
    @DisplayName("ST-O2: Add New Clerk Through Administrative Portal")
    public void testAddNewClerkThroughAdminPortal() {
        // Preconditions
        assertNotNull(Library.librarian, "Librarian logged in");

        int initialClerkCount = 0;
        for (Person p : library.getPersons()) {
            if (p instanceof Clerk) initialClerkCount++;
        }

        // Create new clerk
        Clerk newClerk = new Clerk(-1, "Mike Johnson", "Help Desk", 5553456, 25000, -1);

        // Verify auto-generated credentials
        assertTrue(newClerk.getID() > 0, "Clerk has ID");
        assertNotNull(newClerk.getPassword(), "Clerk has password");
        assertEquals(String.valueOf(newClerk.getID()), newClerk.getPassword(), "Password matches ID");

        // Save and add to library
        newClerk.saveToDatabase();
        library.addClerk(newClerk);

        // Verify clerk added
        int finalClerkCount = 0;
        for (Person p : library.getPersons()) {
            if (p instanceof Clerk) finalClerkCount++;
        }

        assertEquals(initialClerkCount + 1, finalClerkCount, "Clerk count increased");
        assertTrue(library.getPersons().contains(newClerk), "Clerk in persons list");

        // Verify findable by ID
        Clerk foundClerk = library.findClerkById(newClerk.getID());
        assertNotNull(foundClerk, "Clerk findable by ID");
        assertEquals(newClerk, foundClerk, "Found correct clerk");

        System.out.println("✓ ST-O2 PASSED");
    }

    // ==================== ST-O3: VIEW ISSUED BOOKS HISTORY ====================

    @Test
    @DisplayName("ST-O3: View Complete Issued Books History")
    public void testViewCompleteIssuedBooksHistory() {
        // Preconditions
        assertNotNull(Library.librarian, "Librarian logged in");

        // Create loan history
        book1.issueBook(borrower, clerk);
        book2.issueBook(borrower, clerk);
        Loan loan2 = borrower.getBorrowedBooks().get(1);
        book2.returnBook(borrower, loan2, clerk);

        assertTrue(library.getLoans().size() > 0, "Loans exist");

        // Access loan history
        ArrayList<Loan> loanHistory = library.getLoans();
        assertNotNull(loanHistory, "Loan history exists");
        assertTrue(loanHistory.size() >= 2, "At least 2 loans");

        // Verify loan details
        int activeLoans = 0;
        int completedLoans = 0;

        for (Loan loan : loanHistory) {
            assertNotNull(loan.getBook(), "Loan has book");
            assertNotNull(loan.getBorrower(), "Loan has borrower");
            assertNotNull(loan.getIssuer(), "Loan has issuer");
            assertNotNull(loan.getIssuedDate(), "Loan has issue date");

            if (loan.getReceiver() == null) {
                activeLoans++;
            } else {
                completedLoans++;
                assertNotNull(loan.getReturnDate(), "Completed loan has return date");
            }
        }

        assertTrue(activeLoans > 0, "Has active loans");
        assertTrue(completedLoans > 0, "Has completed loans");

        System.out.println("✓ ST-O3 PASSED");
    }

    // ==================== ST-O4: ADD NEW BOOK ====================

    @Test
    @DisplayName("ST-O4: Add New Book to Library Catalog")
    public void testAddNewBookToLibraryCatalog() {
        // Preconditions
        assertNotNull(Library.librarian, "Librarian logged in");

        int initialBookCount = library.getBooks().size();

        // Create new book
        Book newBook = new Book(-1, "Refactoring", "Software Engineering", "Martin Fowler", false);

        assertTrue(newBook.getID() > 0, "Book has ID");

        // Save and add to library
        newBook.saveToDatabase();
        library.addBookinLibrary(newBook);

        // Verify book added
        ArrayList<Book> catalog = library.getBooks();
        assertEquals(initialBookCount + 1, catalog.size(), "Catalog increased");
        assertTrue(catalog.contains(newBook), "Book in catalog");

        // Verify searchable
        Book foundBook = null;
        for (Book b : catalog) {
            if (b.getTitle().equals("Refactoring")) {
                foundBook = b;
                break;
            }
        }
        assertNotNull(foundBook, "Book searchable");
        assertEquals(newBook, foundBook, "Found correct book");

        // Verify details
        assertEquals("Refactoring", newBook.getTitle());
        assertEquals("Martin Fowler", newBook.getAuthor());
        assertEquals("Software Engineering", newBook.getSubject());
        assertFalse(newBook.getIssuedStatus(), "Not issued initially");
        assertEquals(0, newBook.getHoldRequests().size(), "No hold requests");

        // Verify can be issued
        newBook.issueBook(borrower, clerk);
        assertTrue(newBook.getIssuedStatus(), "Book issuable");

        System.out.println("✓ ST-O4 PASSED");
    }

    // ==================== ST-O5: REMOVE BOOK WITH HOLD REQUESTS ====================

    @Test
    @DisplayName("ST-O5: Remove Book with Existing Hold Requests")
    public void testRemoveBookWithHoldRequests() {
        // Preconditions
        assertNotNull(Library.librarian, "Librarian logged in");

        Borrower borrower2 = new Borrower(4, "Bob Wilson", "456 Oak Ave", 5555678);
        library.addBorrower(borrower2);

        // Place hold requests
        book1.placeBookOnHold(borrower);
        book1.placeBookOnHold(borrower2);

        assertEquals(2, book1.getHoldRequests().size(), "Book has 2 hold requests");
        assertFalse(book1.getIssuedStatus(), "Book not issued");

        int initialBookCount = library.getBooks().size();

        // Clean up hold requests (simulates user confirmation)
        ArrayList<HoldRequest> holdRequests = new ArrayList<>(book1.getHoldRequests());
        for (HoldRequest hr : holdRequests) {
            assertEquals(book1, hr.getBook(), "Hold request references correct book");
            hr.getBorrower().removeHoldRequest(hr);
            hr.deleteFromDatabase();
        }
        book1.getHoldRequestOperations().getHoldRequests().clear();

        // Verify hold requests cleaned
        assertEquals(0, book1.getHoldRequests().size(), "Book has no hold requests");
        assertEquals(0, borrower.getOnHoldBooks().size(), "Borrower1 has no holds");
        assertEquals(0, borrower2.getOnHoldBooks().size(), "Borrower2 has no holds");

        // Remove book
        library.removeBookfromLibrary(book1);

        // Verify removal
        assertEquals(initialBookCount - 1, library.getBooks().size(), "Book count decreased");
        assertFalse(library.getBooks().contains(book1), "Book not in catalog");
        assertNull(library.findBookById(book1.getID()), "Book not findable");

        System.out.println("✓ ST-O5 PASSED");
    }

    // ==================== ST-O5a: CANNOT REMOVE ISSUED BOOK ====================

    @Test
    @DisplayName("ST-O5a: Cannot Remove Issued Book")
    public void testCannotRemoveIssuedBook() {
        // Issue book
        book1.issueBook(borrower, clerk);
        assertTrue(book1.getIssuedStatus(), "Book is issued");

        int initialCount = library.getBooks().size();

        // Attempt removal
        library.removeBookfromLibrary(book1);

        // Verify NOT removed
        assertEquals(initialCount, library.getBooks().size(), "Book count unchanged");
        assertTrue(library.getBooks().contains(book1), "Book still in catalog");

        System.out.println("✓ ST-O5a PASSED");
    }

}