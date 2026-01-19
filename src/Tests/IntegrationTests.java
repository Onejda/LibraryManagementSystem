package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

/**
 * Integration Tests for Library Management System
 *
 * These tests verify the integration between multiple components:
 * - Book, Borrower, Staff, Library, DatabaseManager
 * - Database persistence and reload functionality
 *
 * Test Scenarios:
 * 1. Create Borrower and Issue Book
 * 2. Calculate Total Fine for Borrower
 * 3. View Loan History
 * 4. Login and Authentication
 * 5. Database Persistence - LOAD/RELOAD
 * 6. Reset System
 * 7. Reload from DB
 * 8. Verify Data Restored Correctly
 *
 * @author Tesi
 */
public class IntegrationTests {

    private Library library;
    private DatabaseManager dbManager;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        // Reset singletons and static counters
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);
        Clerk.setDeskCount(0);

        // Delete old DB file
        File dbFile = new File("database/library.db");
        if (dbFile.exists()) dbFile.delete();

        library = Library.getInstance();
        dbManager = DatabaseManager.getInstance();

        // Configure library
        library.setName("Test Library");
        library.setFine(20.0);
        library.setRequestExpiry(7);
        library.setReturnDeadline(5);

        // Capture output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Connect (this will seed)
        dbManager.connect();

        clearAllTables();

        Person.setIDCount(0);
        Book.setIDCount(0);
        Clerk.setDeskCount(0);
    }


    @AfterEach
    public void tearDown() {
        // Restore output
        System.setOut(originalOut);

        // Close database connection
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        File dbFile = new File("database/library.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    private void clearAllTables() {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:database/library.db");
             Statement st = c.createStatement()) {

            // Order matters because of FK relations
            st.executeUpdate("DELETE FROM HoldRequest");
            st.executeUpdate("DELETE FROM Loan");
            st.executeUpdate("DELETE FROM Clerk");
            st.executeUpdate("DELETE FROM Librarian");
            st.executeUpdate("DELETE FROM Staff");
            st.executeUpdate("DELETE FROM Book");
            st.executeUpdate("DELETE FROM Person");

        } catch (Exception e) {
            throw new RuntimeException("Failed to clear database tables", e);
        }
    }


    // ==================== INTEGRATION TEST 1: Create Borrower and Issue Book====================

    @Test
    @DisplayName("IT-1: Create Borrower and Issue Book (Complete Workflow)")
    public void testCreateBorrowerAndIssueBook() {
        // Step 1: Create Staff Member (Librarian)
        Librarian librarian = new Librarian(-1, "Test Librarian", "Office 101", 5550001, 50000, -1);
        librarian.saveToDatabase();
        Library.librarian = librarian;

        // Step 2: Create a Book
        Book book = new Book(-1, "Test Book", "Computer Science", "Test Author", false);
        book.saveToDatabase();
        library.addBookinLibrary(book);

        // Step 3: Create a Borrower
        Borrower borrower = new Borrower(-1, "John Doe", "123 Main St", 5551234);
        borrower.saveToDatabase();
        library.addBorrower(borrower);

        // Verify borrower created successfully
        assertNotNull(borrower, "Borrower should be created");
        assertTrue(borrower.getID() > 0, "Borrower should have valid ID");
        assertEquals("John Doe", borrower.getName(), "Borrower name should match");

        // Step 4: Issue the book to the borrower
        assertFalse(book.getIssuedStatus(), "Book should not be issued initially");

        // Simulate issuing book (without user input)
        book.setIssuedStatus(true);
        Loan loan = new Loan(borrower, book, librarian, null, new Date(), null, false);
        loan.saveToDatabase();
        library.addLoan(loan);
        borrower.addBorrowedBook(loan);

        // Step 5: Verify book is marked as issued
        assertTrue(book.getIssuedStatus(), "Book should be marked as issued");

        // Step 6: Verify borrower has the book in borrowed list
        ArrayList<Loan> borrowedBooks = borrower.getBorrowedBooks();
        assertEquals(1, borrowedBooks.size(), "Borrower should have 1 borrowed book");
        assertEquals(book, borrowedBooks.get(0).getBook(), "Borrowed book should match the issued book");

        // Step 7: Verify loan is recorded in library
        ArrayList<Loan> loans = library.getLoans();
        assertTrue(loans.size() > 0, "Library should have at least one loan record");

        Loan recordedLoan = loans.get(loans.size() - 1);
        assertEquals(borrower, recordedLoan.getBorrower(), "Loan borrower should match");
        assertEquals(book, recordedLoan.getBook(), "Loan book should match");
        assertEquals(librarian, recordedLoan.getIssuer(), "Loan issuer should match");
        assertNull(recordedLoan.getReturnDate(), "Book should not be returned yet");

        // Step 8: Verify data persisted in database
        ArrayList<Object[]> dbLoans = dbManager.loadAllLoans();
        assertTrue(dbLoans.size() > 0, "Database should contain loan records");

        System.out.println("✅ Integration Test 1 PASSED: Borrower created and book issued successfully");
    }

    @Test
    @DisplayName("IT-1b: Issue Book with Hold Request Queue")
    public void testIssueBooksWithHoldRequests() {
        // Setup
        Clerk clerk = new Clerk(-1, "Test Clerk", "Front Desk", 5550002, 25000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);

        Book book = new Book(-1, "Popular Book", "Fiction", "Famous Author", false);
        book.saveToDatabase();
        library.addBookinLibrary(book);

        Borrower borrower1 = new Borrower(-1, "Alice Smith", "456 Oak Ave", 5552345);
        borrower1.saveToDatabase();
        library.addBorrower(borrower1);

        Borrower borrower2 = new Borrower(-1, "Bob Johnson", "789 Pine St", 5553456);
        borrower2.saveToDatabase();
        library.addBorrower(borrower2);

        // Place hold requests
        book.placeBookOnHold(borrower1);
        book.placeBookOnHold(borrower2);

        // Verify hold requests
        assertEquals(2, book.getHoldRequests().size(), "Should have 2 hold requests");

        // Issue book - should only issue to first borrower in queue
        book.setIssuedStatus(true);

        // Service first hold request
        HoldRequest firstRequest = book.getHoldRequests().get(0);
        book.serviceHoldRequest(firstRequest);

        Loan loan = new Loan(borrower1, book, clerk, null, new Date(), null, false);
        loan.saveToDatabase();
        borrower1.addBorrowedBook(loan);
        library.addLoan(loan);

        // Verify results
        assertEquals(1, book.getHoldRequests().size(), "Should have 1 remaining hold request");
        assertEquals(1, borrower1.getBorrowedBooks().size(), "First borrower should have the book");
        assertEquals(0, borrower2.getBorrowedBooks().size(), "Second borrower should not have the book");
        assertTrue(book.getIssuedStatus(), "Book should be issued");

        System.out.println("✅ Integration Test 1b PASSED: Book issued with hold request queue");
    }

    // ==================== INTEGRATION TEST 2: Calculate Total Fine for Borrower ====================

    @Test
    @DisplayName("IT-2: Calculate Total Fine for Borrower with Multiple Loans")
    public void testCalculateTotalFineForBorrower() {
        // Setup
        Clerk clerk = new Clerk(-1, "Fine Clerk", "Front Desk", 5550003, 25000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);

        Borrower borrower = new Borrower(-1, "Late Reader", "999 Delay Dr", 5559999);
        borrower.saveToDatabase();
        library.addBorrower(borrower);

        // Create books
        Book book1 = new Book(-1, "Book One", "Subject A", "Author A", true);
        book1.saveToDatabase();
        library.addBookinLibrary(book1);

        Book book2 = new Book(-1, "Book Two", "Subject B", "Author B", true);
        book2.saveToDatabase();
        library.addBookinLibrary(book2);

        // Create loans with different overdue periods
        // Loan 1: 10 days overdue (10 - 5 deadline = 5 days late * 20 Rs = 100 Rs)
        Date issueDate1 = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        Loan loan1 = new Loan(borrower, book1, clerk, null, issueDate1, null, false);
        loan1.saveToDatabase();
        library.addLoan(loan1);
        borrower.addBorrowedBook(loan1);

        // Loan 2: 8 days overdue (8 - 5 deadline = 3 days late * 20 Rs = 60 Rs)
        Date issueDate2 = new Date(System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000));
        Loan loan2 = new Loan(borrower, book2, clerk, null, issueDate2, null, false);
        loan2.saveToDatabase();
        library.addLoan(loan2);
        borrower.addBorrowedBook(loan2);

        // Calculate fines
        double fine1 = loan1.computeFine1();
        double fine2 = loan2.computeFine1();
        double totalFine = fine1 + fine2;

        // Verify individual fines
        assertTrue(fine1 > 0, "Loan 1 should have a fine");
        assertTrue(fine2 > 0, "Loan 2 should have a fine");

        // Expected: (10-5)*20 + (8-5)*20 = 100 + 60 = 160
        double expectedFine = 160.0;
        assertEquals(expectedFine, totalFine, 0.01, "Total fine should be calculated correctly");

        // Test computeFine2 method from Library
        double libraryCalculatedFine = library.computeFine2(borrower);
        assertEquals(expectedFine, libraryCalculatedFine, 0.01, "Library's fine calculation should match");

        System.out.println("✅ Integration Test 2 PASSED: Total fine calculated correctly");
    }

    @Test
    @DisplayName("IT-2b: Calculate Fine - No Fine for On-Time Returns")
    public void testNoFineForOnTimeReturns() {
        // Setup
        Clerk clerk = new Clerk(-1, "Punctual Clerk", "Front Desk", 5550004, 25000, -1);
        clerk.saveToDatabase();

        Borrower borrower = new Borrower(-1, "On-Time Reader", "111 Prompt Pl", 5551111);
        borrower.saveToDatabase();
        library.addBorrower(borrower);

        Book book = new Book(-1, "Timely Book", "Punctuality", "Time Author", true);
        book.saveToDatabase();
        library.addBookinLibrary(book);

        // Create loan issued 3 days ago (within 5-day deadline)
        Date issueDate = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));
        Loan loan = new Loan(borrower, book, clerk, null, issueDate, null, false);
        loan.saveToDatabase();
        library.addLoan(loan);
        borrower.addBorrowedBook(loan);

        // Calculate fine
        double fine = loan.computeFine1();

        // Verify no fine
        assertEquals(0.0, fine, 0.01, "No fine should be charged for on-time returns");

        System.out.println("✅ Integration Test 2b PASSED: No fine for on-time returns");
    }

    // ==================== INTEGRATION TEST 3: View Loan History ====================

    @Test
    @DisplayName("IT-3: View Complete Loan History")
    public void testViewLoanHistory() {
        // Setup staff
        Librarian librarian = new Librarian(-1, "History Keeper", "Office 102", 5550005, 50000, -1);
        librarian.saveToDatabase();
        Library.librarian = librarian;

        Clerk clerk = new Clerk(-1, "Return Clerk", "Front Desk", 5550006, 25000, -1);
        clerk.saveToDatabase();

        // Setup borrowers
        Borrower borrower1 = new Borrower(-1, "Reader One", "100 First St", 5551001);
        borrower1.saveToDatabase();
        library.addBorrower(borrower1);

        Borrower borrower2 = new Borrower(-1, "Reader Two", "200 Second St", 5552002);
        borrower2.saveToDatabase();
        library.addBorrower(borrower2);

        // Setup books
        Book book1 = new Book(-1, "History Book 1", "History", "Historian A", true);
        book1.saveToDatabase();
        library.addBookinLibrary(book1);

        Book book2 = new Book(-1, "History Book 2", "History", "Historian B", true);
        book2.saveToDatabase();
        library.addBookinLibrary(book2);

        Book book3 = new Book(-1, "History Book 3", "History", "Historian C", false);
        book3.saveToDatabase();
        library.addBookinLibrary(book3);

        // Create loan history
        // Loan 1: Active (not returned)
        Date issueDate1 = new Date(System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000));
        Loan loan1 = new Loan(borrower1, book1, librarian, null, issueDate1, null, false);
        loan1.saveToDatabase();
        library.addLoan(loan1);
        borrower1.addBorrowedBook(loan1);

        // Loan 2: Returned with fine paid
        Date issueDate2 = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        Date returnDate2 = new Date(System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000));
        Loan loan2 = new Loan(borrower2, book2, librarian, clerk, issueDate2, returnDate2, true);
        loan2.saveToDatabase();
        library.addLoan(loan2);

        // Loan 3: Returned without fine paid
        Date issueDate3 = new Date(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000));
        Date returnDate3 = new Date();
        Loan loan3 = new Loan(borrower1, book3, clerk, clerk, issueDate3, returnDate3, false);
        loan3.saveToDatabase();
        library.addLoan(loan3);

        // Retrieve loan history
        ArrayList<Loan> loanHistory = library.getLoans();

        // Verify loan history
        assertTrue(loanHistory.size() >= 3, "Should have at least 3 loan records");

        // Verify active loan
        boolean hasActiveLoan = false;
        boolean hasReturnedLoan = false;
        for (Loan loan : loanHistory) {
            if (loan.getReturnDate() == null) {
                hasActiveLoan = true;
                assertNotNull(loan.getIssuer(), "Active loan should have issuer");
                assertNull(loan.getReceiver(), "Active loan should not have receiver");
            } else {
                hasReturnedLoan = true;
                assertNotNull(loan.getReturnDate(), "Returned loan should have return date");
                assertNotNull(loan.getReceiver(), "Returned loan should have receiver");
            }
        }

        assertTrue(hasActiveLoan, "Should have at least one active loan");
        assertTrue(hasReturnedLoan, "Should have at least one returned loan");

        // Test viewHistory output
        library.viewHistory();
        String output = outputStream.toString();
        assertTrue(output.contains("History Book"), "History output should contain book titles");

        System.out.println("✅ Integration Test 3 PASSED: Loan history viewed successfully");
    }

    // ==================== INTEGRATION TEST 4: Login and Authentication ✅ ====================

    @Test
    @DisplayName("IT-4: Login and Authentication (Multiple User Types)")
    public void testLoginAndAuthentication() {
        // Create users
        Librarian librarian = new Librarian(-1, "Admin User", "Office 103", 5550007, 50000, -1);
        librarian.saveToDatabase();
        Library.librarian = librarian;
        int librarianId = librarian.getID();
        String librarianPassword = librarian.getPassword();

        Clerk clerk = new Clerk(-1, "Clerk User", "Front Desk", 5550008, 25000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);
        int clerkId = clerk.getID();
        String clerkPassword = clerk.getPassword();

        Borrower borrower = new Borrower(-1, "Borrower User", "300 Third St", 5553003);
        borrower.saveToDatabase();
        library.addBorrower(borrower);
        int borrowerId = borrower.getID();
        String borrowerPassword = borrower.getPassword();

        // Test 1: Successful librarian login
        String input = librarianId + "\n" + librarianPassword + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Person loggedInUser = library.login();
        assertNotNull(loggedInUser, "Librarian should login successfully");
        assertTrue(loggedInUser instanceof Librarian, "Logged in user should be Librarian");
        assertEquals(librarianId, loggedInUser.getID(), "User ID should match");

        // Test 2: Successful clerk login
        input = clerkId + "\n" + clerkPassword + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        loggedInUser = library.login();
        assertNotNull(loggedInUser, "Clerk should login successfully");
        assertTrue(loggedInUser instanceof Clerk, "Logged in user should be Clerk");
        assertEquals(clerkId, loggedInUser.getID(), "User ID should match");

        // Test 3: Successful borrower login
        input = borrowerId + "\n" + borrowerPassword + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        loggedInUser = library.login();
        assertNotNull(loggedInUser, "Borrower should login successfully");
        assertTrue(loggedInUser instanceof Borrower, "Logged in user should be Borrower");
        assertEquals(borrowerId, loggedInUser.getID(), "User ID should match");

        // Test 4: Failed login with wrong password
        input = borrowerId + "\n" + "wrongpassword" + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        loggedInUser = library.login();
        assertNull(loggedInUser, "Login should fail with wrong password");

        // Test 5: Failed login with non-existent ID
        input = "99999\n" + "password" + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        loggedInUser = library.login();
        assertNull(loggedInUser, "Login should fail with non-existent ID");

        System.out.println("✅ Integration Test 4 PASSED: Login and authentication working correctly");
    }

    // ==================== INTEGRATION TEST 5: Database Persistence - LOAD/RELOAD ====================

    @Test
    @DisplayName("IT-5: Database Persistence - Save and Reload")
    public void testDatabasePersistence() throws Exception {
        // Phase 1: Create and save data
        Librarian librarian = new Librarian(-1, "Persistent Admin", "Office 104", 5550009, 50000, -1);
        librarian.saveToDatabase();
        Library.librarian = librarian;
        int librarianId = librarian.getID();

        Book book = new Book(-1, "Persistent Book", "Science", "Persistent Author", false);
        book.saveToDatabase();
        library.addBookinLibrary(book);
        int bookId = book.getID();
        String bookTitle = book.getTitle();

        Borrower borrower = new Borrower(-1, "Persistent Borrower", "400 Fourth St", 5554004);
        borrower.saveToDatabase();
        library.addBorrower(borrower);
        int borrowerId = borrower.getID();
        String borrowerName = borrower.getName();

        // Issue book
        book.setIssuedStatus(true);
        Loan loan = new Loan(borrower, book, librarian, null, new Date(), null, false);
        loan.saveToDatabase();
        library.addLoan(loan);

        // Verify initial data
        assertEquals(1, library.getBooks().size(), "Should have 1 book initially");
        assertEquals(1, library.getLoans().size(), "Should have 1 loan initially");

        // Phase 2: Close and reset
        dbManager.closeConnection();
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);

        // Phase 3: Reconnect and reload
        library = Library.getInstance();
        library.setFine(20.0);
        library.setRequestExpiry(7);
        library.setReturnDeadline(5);

        dbManager = DatabaseManager.getInstance();
        Object con = dbManager.connect();
        assertNotNull(con, "Should reconnect to database");

        // Populate from database
        library.populateLibrary(con);

        // Phase 4: Verify reloaded data
        ArrayList<Book> reloadedBooks = library.getBooks();
        assertTrue(reloadedBooks.size() > 0, "Books should be reloaded");

        Book reloadedBook = library.findBookById(bookId);
        assertNotNull(reloadedBook, "Specific book should be found");
        assertEquals(bookTitle, reloadedBook.getTitle(), "Book title should match");
        assertTrue(reloadedBook.getIssuedStatus(), "Book issued status should persist");

        Borrower reloadedBorrower = library.findBorrowerById(borrowerId);
        assertNotNull(reloadedBorrower, "Borrower should be reloaded");
        assertEquals(borrowerName, reloadedBorrower.getName(), "Borrower name should match");

        Staff reloadedStaff = library.findStaffById(librarianId);
        assertNotNull(reloadedStaff, "Librarian should be reloaded");
        assertTrue(reloadedStaff instanceof Librarian, "Staff should be Librarian type");

        ArrayList<Loan> reloadedLoans = library.getLoans();
        assertTrue(reloadedLoans.size() > 0, "Loans should be reloaded");

        System.out.println("✅ Integration Test 5 PASSED: Database persistence verified");
    }

    // ==================== INTEGRATION TEST 6: Reset System ====================

    @Test
    @DisplayName("IT-6: Reset System and Reinitialize")
    public void testResetSystem() {
        // Create initial data
        Book book = new Book(-1, "Reset Test Book", "Testing", "Reset Author", false);
        book.saveToDatabase();
        library.addBookinLibrary(book);

        Borrower borrower = new Borrower(-1, "Reset Borrower", "500 Fifth St", 5555005);
        borrower.saveToDatabase();
        library.addBorrower(borrower);

        // Verify initial state
        assertFalse(library.getBooks().isEmpty(), "Should have books before reset");
        assertFalse(library.getPersons().isEmpty(), "Should have persons before reset");

        // Reset the system
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);

        // Get new instance
        library = Library.getInstance();

        // Verify reset state
        assertTrue(library.getBooks().isEmpty(), "Books should be empty after reset");
        assertTrue(library.getLoans().isEmpty(), "Loans should be empty after reset");
        assertNull(Library.librarian, "Librarian should be null after reset");

        // Verify can reinitialize
        library.setName("Reinitialized Library");
        library.setFine(25.0);
        library.setRequestExpiry(10);
        library.setReturnDeadline(7);

        assertEquals("Reinitialized Library", library.getLibraryName(), "Should accept new configuration");
        assertEquals(25.0, library.per_day_fine, 0.01, "Should accept new fine rate");

        System.out.println("✅ Integration Test 6 PASSED: System reset successfully");
    }

    // ==================== INTEGRATION TEST 7: Reload from Database ====================

    @Test
    @DisplayName("IT-7: Reload Different Entity Types from Database")
    public void testReloadFromDatabase() throws Exception {

        // Arrange: create minimal data set (because we clear seed data in setUp)
        Librarian librarian = new Librarian(-1, "Seedless Admin", "Office 200", 5557777, 50000, -1);
        librarian.saveToDatabase();
        Library.librarian = librarian;

        Clerk clerk = new Clerk(-1, "Seedless Clerk", "Front Desk", 5558888, 25000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);

        Borrower borrower = new Borrower(-1, "Seedless Borrower", "Somewhere", 5559999);
        borrower.saveToDatabase();
        library.addBorrower(borrower);

        Book book = new Book(-1, "Seedless Book", "Testing", "Author", false);
        book.saveToDatabase();
        library.addBookinLibrary(book);

        // Act: load raw data from DB using DatabaseManager
        ArrayList<Object[]> books = dbManager.loadAllBooks();
        ArrayList<Object[]> borrowers = dbManager.loadAllBorrowers();
        ArrayList<Object[]> clerks = dbManager.loadAllClerks();
        ArrayList<Object[]> loans = dbManager.loadAllLoans(); // might be 0, that's OK

        // Assert: each entity type can be loaded
        assertNotNull(books, "Books should be loadable");
        assertNotNull(borrowers, "Borrowers should be loadable");
        assertNotNull(clerks, "Clerks should be loadable");
        assertNotNull(loans, "Loans should be loadable");

        assertTrue(books.size() > 0, "Should have books in database");
        assertTrue(borrowers.size() > 0, "Should have borrowers in database");
        assertTrue(clerks.size() > 0, "Should have clerks in database");

        // Librarian should be loadable
        Object[] librarianData = dbManager.loadLibrarian();
        assertNotNull(librarianData, "Librarian should be loadable");
        assertTrue(librarianData.length >= 7, "Librarian data should have all fields");

        // Act: populate library from DB and verify objects exist in-memory
        library.populateLibrary(dbManager.connect());

        assertFalse(library.getBooks().isEmpty(), "Library should have books after population");
        assertFalse(library.getPersons().isEmpty(), "Library should have persons after population");
        assertNotNull(Library.librarian, "Librarian should be set after population");

        System.out.println("✅ Integration Test 7 PASSED: Entities reloaded from database");
    }


    // ==================== INTEGRATION TEST 8: Verify Data Restored Correctly ====================

    @Test
    @DisplayName("IT-8: Verify Complete Data Integrity After Reload")
    public void testVerifyDataRestoredCorrectly() throws Exception {
        // Phase 1: Create complex data structure
        Librarian librarian = new Librarian(-1, "Integrity Librarian", "Office 105", 5550010, 50000, -1);
        librarian.saveToDatabase();
        Library.librarian = librarian;
        int librarianId = librarian.getID();

        Clerk clerk = new Clerk(-1, "Integrity Clerk", "Front Desk", 5550011, 25000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);
        int clerkId = clerk.getID();

        // Create books
        Book issuedBook = new Book(-1, "Issued Book", "Subject X", "Author X", true);
        issuedBook.saveToDatabase();
        library.addBookinLibrary(issuedBook);
        int issuedBookId = issuedBook.getID();

        Book availableBook = new Book(-1, "Available Book", "Subject Y", "Author Y", false);
        availableBook.saveToDatabase();
        library.addBookinLibrary(availableBook);
        int availableBookId = availableBook.getID();

        // Create borrowers
        Borrower activeBorrower = new Borrower(-1, "Active Borrower", "600 Sixth St", 5556006);
        activeBorrower.saveToDatabase();
        library.addBorrower(activeBorrower);
        int activeBorrowerId = activeBorrower.getID();

        Borrower holdBorrower = new Borrower(-1, "Hold Borrower", "700 Seventh St", 5557007);
        holdBorrower.saveToDatabase();
        library.addBorrower(holdBorrower);
        int holdBorrowerId = holdBorrower.getID();

        // Create active loan
        Date issueDate = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));
        Loan activeLoan = new Loan(activeBorrower, issuedBook, librarian, null, issueDate, null, false);
        activeLoan.saveToDatabase();
        library.addLoan(activeLoan);
        activeBorrower.addBorrowedBook(activeLoan);

        // Create hold request
        HoldRequest holdRequest = new HoldRequest(holdBorrower, issuedBook, new Date());
        holdRequest.saveToDatabase();
        issuedBook.getHoldRequestOperations().addHoldRequest(holdRequest);
        holdBorrower.addHoldRequest(holdRequest);

        // Verify initial state
        assertEquals(2, library.getBooks().size(), "Should have 2 books");
        assertEquals(1, activeBorrower.getBorrowedBooks().size(), "Active borrower should have 1 book");
        assertEquals(1, holdBorrower.getOnHoldBooks().size(), "Hold borrower should have 1 hold request");

        // Phase 2: Reset and reload
        dbManager.closeConnection();
        Library.resetInstance();
        Person.setIDCount(0);
        Book.setIDCount(0);
        Clerk.setDeskCount(0);

        library = Library.getInstance();
        library.setFine(20.0);
        library.setRequestExpiry(7);
        library.setReturnDeadline(5);

        dbManager = DatabaseManager.getInstance();
        dbManager.connect();
        library.populateLibrary(dbManager.connect());

        // Phase 3: Verify all data and relationships

        // Verify books
        Book reloadedIssuedBook = library.findBookById(issuedBookId);
        assertNotNull(reloadedIssuedBook, "Issued book should be reloaded");
        assertEquals("Issued Book", reloadedIssuedBook.getTitle(), "Book title should match");
        assertTrue(reloadedIssuedBook.getIssuedStatus(), "Book should still be issued");

        Book reloadedAvailableBook = library.findBookById(availableBookId);
        assertNotNull(reloadedAvailableBook, "Available book should be reloaded");
        assertFalse(reloadedAvailableBook.getIssuedStatus(), "Book should still be available");

        // Verify borrowers
        Borrower reloadedActiveBorrower = library.findBorrowerById(activeBorrowerId);
        assertNotNull(reloadedActiveBorrower, "Active borrower should be reloaded");
        assertEquals("Active Borrower", reloadedActiveBorrower.getName(), "Name should match");

        // Verify active loan relationship
        ArrayList<Loan> borrowedBooks = reloadedActiveBorrower.getBorrowedBooks();
        assertEquals(1, borrowedBooks.size(), "Borrower should still have 1 borrowed book");
        assertEquals(issuedBookId, borrowedBooks.get(0).getBook().getID(), "Borrowed book should match");

        // Verify hold request relationship
        Borrower reloadedHoldBorrower = library.findBorrowerById(holdBorrowerId);
        assertNotNull(reloadedHoldBorrower, "Hold borrower should be reloaded");

        ArrayList<HoldRequest> holdRequests = reloadedHoldBorrower.getOnHoldBooks();
        assertEquals(1, holdRequests.size(), "Should have 1 hold request");
        assertEquals(issuedBookId, holdRequests.get(0).getBook().getID(), "Hold request book should match");

        // Verify book has hold request
        ArrayList<HoldRequest> bookHoldRequests = reloadedIssuedBook.getHoldRequests();
        assertEquals(1, bookHoldRequests.size(), "Book should have 1 hold request");
        assertEquals(holdBorrowerId, bookHoldRequests.get(0).getBorrower().getID(),
                "Hold request borrower should match");

        // Verify staff
        Staff reloadedLibrarian = library.findStaffById(librarianId);
        assertNotNull(reloadedLibrarian, "Librarian should be reloaded");
        assertTrue(reloadedLibrarian instanceof Librarian, "Should be Librarian type");

        Staff reloadedClerk = library.findStaffById(clerkId);
        assertNotNull(reloadedClerk, "Clerk should be reloaded");
        assertTrue(reloadedClerk instanceof Clerk, "Should be Clerk type");

        // Verify loans
        ArrayList<Loan> reloadedLoans = library.getLoans();
        assertTrue(reloadedLoans.size() > 0, "Should have loans");

        boolean foundActiveLoan = false;
        for (Loan loan : reloadedLoans) {
            if (loan.getBorrower().getID() == activeBorrowerId &&
                    loan.getBook().getID() == issuedBookId) {
                foundActiveLoan = true;
                assertNull(loan.getReturnDate(), "Loan should not be returned");
                assertEquals(librarianId, loan.getIssuer().getID(), "Issuer should match");
            }
        }
        assertTrue(foundActiveLoan, "Active loan should be found");

        System.out.println("✅ Integration Test 8 PASSED: All data and relationships verified correctly");
    }
}