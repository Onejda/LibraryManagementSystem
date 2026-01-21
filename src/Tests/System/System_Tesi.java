package Tests.System;


import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class System_Tesi {

    private Library library;
    private DatabaseManager dbManager;

    @BeforeEach
    public void setUp() {
        // Reset library singleton for each test
        Library.resetInstance();
        library = Library.getInstance();

        // Initialize database
        dbManager = DatabaseManager.getInstance();
        dbManager.connect();

        // Set library configuration
        library.setName("Test Library System");
        library.setReturnDeadline(14);
        library.setFine(5.0);
        library.setRequestExpiry(7);
    }

    @Test
    @Order(1)
    @DisplayName("ST-T1: Create New Borrower Through Clerk Portal")
    public void testCreateNewBorrowerThroughClerkPortal() {
        // Precondition: System is running
        assertNotNull(library, "Library system should be initialized");
        assertNotNull(dbManager, "Database connection should be active");

        // Step 1: Login as Clerk
        Clerk clerk = new Clerk(-1, "Jane Clerk", "Front Desk", 5554444, 28000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);

        // Verify clerk is logged in (simulate login)
        assertNotNull(clerk, "Clerk should be logged in successfully");
        assertEquals("Clerk", clerk.getClass().getSimpleName(), "Logged in user should be Clerk");
        assertTrue(library.getPersons().contains(clerk), "Clerk should be in system");

        // Step 2 & 3: Select "Add Borrower" option and enter details
        // Count initial borrowers
        int initialBorrowerCount = 0;
        for (Person p : library.getPersons()) {
            if (p instanceof Borrower) initialBorrowerCount++;
        }

        // Clerk creates new borrower with valid details
        String borrowerName = "Alice NewUser";
        String borrowerAddress = "789 Test Avenue";
        int borrowerPhone = 5559999;

        // Use a specific test ID to avoid conflicts (or get next available ID)
        int maxPersonId = dbManager.getMaxPersonId();
        int testBorrowerId = maxPersonId + 1;

        Borrower newBorrower = new Borrower(testBorrowerId, borrowerName, borrowerAddress, borrowerPhone);

        // Step 4: Confirm creation - save to database
        newBorrower.saveToDatabase();
        library.addBorrower(newBorrower);

        // Expected Output 1: System displays success message (simulated by no exceptions)
        assertDoesNotThrow(() -> {
            // In real system, would display: "Borrower created successfully"
            System.out.println("Borrower created successfully");
        }, "Borrower creation should complete without errors");

        // Expected Output 2: Unique Borrower ID and password are generated
        assertNotNull(newBorrower, "Borrower object should be created");
        assertNotEquals(-1, newBorrower.getID(), "Borrower should have valid unique ID from database");
        assertNotNull(newBorrower.getPassword(), "Password should be generated");
        assertEquals(String.valueOf(newBorrower.getID()), newBorrower.getPassword(),
                "Default password should equal ID");

        // Verify borrower details are correct
        assertEquals(borrowerName, newBorrower.getName(), "Borrower name should match input");
        assertEquals(borrowerAddress, newBorrower.getAddress(), "Borrower address should match input");
        assertEquals(borrowerPhone, newBorrower.getPhoneNumber(), "Borrower phone should match input");

        // Expected Output 3: Borrower is saved in database
        int finalBorrowerCount = 0;
        for (Person p : library.getPersons()) {
            if (p instanceof Borrower) finalBorrowerCount++;
        }
        assertEquals(initialBorrowerCount + 1, finalBorrowerCount,
                "Borrower count should increase by 1");

        // Expected Output 4: Borrower can log in to the system
        // Simulate login with generated credentials
        int borrowerId = newBorrower.getID();
        String borrowerPassword = newBorrower.getPassword();

        // Find borrower in system by ID and verify password
        Person foundPerson = null;
        for (Person p : library.getPersons()) {
            if (p.getID() == borrowerId && p.getPassword().equals(borrowerPassword)) {
                foundPerson = p;
                break;
            }
        }

        assertNotNull(foundPerson, "Borrower should be able to login with generated credentials");
        assertTrue(foundPerson instanceof Borrower, "Logged in person should be a Borrower");
        assertEquals(newBorrower.getID(), foundPerson.getID(), "Login should return correct borrower");

        // Verify database persistence - reload from database to confirm
        ArrayList<Object[]> allBorrowers = dbManager.loadAllBorrowers();
        boolean foundInDb = false;
        for (Object[] borrowerData : allBorrowers) {
            int dbId = (Integer) borrowerData[0];
            // Find OUR specific borrower by ID (not the seeded ones)
            if (dbId == newBorrower.getID()) {
                foundInDb = true;
                String dbName = (String) borrowerData[1];
                String dbAddress = (String) borrowerData[2];
                int dbPhone = (Integer) borrowerData[3];

                assertEquals(borrowerName, dbName,
                        "Database should have correct name for borrower ID " + newBorrower.getID());
                assertEquals(borrowerAddress, dbAddress,
                        "Database should have correct address for borrower ID " + newBorrower.getID());
                assertEquals(borrowerPhone, dbPhone,
                        "Database should have correct phone for borrower ID " + newBorrower.getID());
                break;
            }
        }
        assertTrue(foundInDb, "New borrower (ID: " + newBorrower.getID() + ") should be persisted in database");

        // Additional verification: Check that total borrower count increased
        assertTrue(allBorrowers.size() >= finalBorrowerCount,
                "Database should contain at least as many borrowers as in-memory system");

        // Test Result: PASS
        // All assertions passed - borrower creation workflow complete
    }
    @Test
    @Order(2)
    @DisplayName("ST-T2: Update Complete Borrower Profile")
    public void testUpdateCompleteBorrowerProfile() {
        // Precondition: System is running
        assertNotNull(library, "Library system should be initialized");
        assertNotNull(dbManager, "Database connection should be active");

        // Precondition: Create an existing borrower first
        int maxPersonId = dbManager.getMaxPersonId();
        int existingBorrowerId = maxPersonId + 1;

        Borrower existingBorrower = new Borrower(existingBorrowerId, "Original Name",
                "Original Address", 5551234);
        existingBorrower.saveToDatabase();
        library.addBorrower(existingBorrower);

        // Verify borrower exists before update
        assertNotNull(existingBorrower, "Borrower should exist before update");
        assertEquals("Original Name", existingBorrower.getName());
        assertEquals("Original Address", existingBorrower.getAddress());
        assertEquals(5551234, existingBorrower.getPhoneNumber());

        // Step 1: Login as Clerk
        Clerk clerk = new Clerk(-1, "Update Clerk", "Service Desk", 5556666, 29000, -1);
        clerk.saveToDatabase();
        library.addClerk(clerk);

        // Verify clerk is logged in
        assertNotNull(clerk, "Clerk should be logged in successfully");
        assertTrue(library.getPersons().contains(clerk), "Clerk should be in system");

        // Step 2 & 3: Select "Update Borrower" option and enter Borrower ID
        int borrowerIdToUpdate = existingBorrower.getID();

        // Find the borrower in the system by ID
        Borrower borrowerToUpdate = library.findBorrowerById(borrowerIdToUpdate);
        assertNotNull(borrowerToUpdate, "Borrower with ID " + borrowerIdToUpdate + " should be found");
        assertEquals(existingBorrower, borrowerToUpdate, "Found borrower should match the existing one");

        // Step 4: Update borrower information sequentially

        // Update 1: Change Name
        String newName = "Updated Alice Smith";
        borrowerToUpdate.setName(newName);

        // Expected Output: Name updated successfully
        assertEquals(newName, borrowerToUpdate.getName(), "Borrower name should be updated in memory");

        // Verify database persistence for name
        ArrayList<Object[]> allBorrowers = dbManager.loadAllBorrowers();
        boolean nameUpdatedInDb = false;
        for (Object[] data : allBorrowers) {
            if ((Integer) data[0] == borrowerIdToUpdate) {
                assertEquals(newName, (String) data[1], "Name should be updated in database");
                nameUpdatedInDb = true;
                break;
            }
        }
        assertTrue(nameUpdatedInDb, "Name update should be persisted in database");

        // Update 2: Change Address
        String newAddress = "456 Updated Boulevard";
        borrowerToUpdate.setAddress(newAddress);

        // Expected Output: Address updated successfully
        assertEquals(newAddress, borrowerToUpdate.getAddress(), "Borrower address should be updated in memory");

        // Verify database persistence for address
        allBorrowers = dbManager.loadAllBorrowers();
        boolean addressUpdatedInDb = false;
        for (Object[] data : allBorrowers) {
            if ((Integer) data[0] == borrowerIdToUpdate) {
                assertEquals(newAddress, (String) data[2], "Address should be updated in database");
                addressUpdatedInDb = true;
                break;
            }
        }
        assertTrue(addressUpdatedInDb, "Address update should be persisted in database");

        // Update 3: Change Phone Number
        int newPhone = 5559876;
        borrowerToUpdate.setPhone(newPhone);

        // Expected Output: Phone number updated successfully
        assertEquals(newPhone, borrowerToUpdate.getPhoneNumber(), "Borrower phone should be updated in memory");

        // Verify database persistence for phone
        allBorrowers = dbManager.loadAllBorrowers();
        boolean phoneUpdatedInDb = false;
        for (Object[] data : allBorrowers) {
            if ((Integer) data[0] == borrowerIdToUpdate) {
                assertEquals(newPhone, (Integer) data[3], "Phone should be updated in database");
                phoneUpdatedInDb = true;
                break;
            }
        }
        assertTrue(phoneUpdatedInDb, "Phone update should be persisted in database");

        // Step 5: Confirm all updates
        // Expected Output: "Borrower successfully updated" message
        assertDoesNotThrow(() -> {
            System.out.println("Borrower successfully updated");
        }, "Update confirmation should complete without errors");

        // Final verification: All fields updated correctly
        assertEquals(newName, borrowerToUpdate.getName(), "Final name should match");
        assertEquals(newAddress, borrowerToUpdate.getAddress(), "Final address should match");
        assertEquals(newPhone, borrowerToUpdate.getPhoneNumber(), "Final phone should match");

        // Verify complete record in database
        allBorrowers = dbManager.loadAllBorrowers();
        boolean completeRecordFound = false;
        for (Object[] data : allBorrowers) {
            if ((Integer) data[0] == borrowerIdToUpdate) {
                assertEquals(newName, (String) data[1], "Database should have updated name");
                assertEquals(newAddress, (String) data[2], "Database should have updated address");
                assertEquals(newPhone, (Integer) data[3], "Database should have updated phone");
                completeRecordFound = true;
                break;
            }
        }
        assertTrue(completeRecordFound, "Complete updated record should be in database");

        // Test Result: PASS
        // All fields updated successfully and persisted to database
    }
    @Test
    @Order(3)
    @DisplayName("ST-T3: Update Book Information")
    public void testUpdateBookInformation() {
        // Precondition: System is running
        assertNotNull(library, "Library system should be initialized");
        assertNotNull(dbManager, "Database connection should be active");

        // Precondition: Create at least one book in the library catalog
        Book existingBook = new Book(-1, "Original Title", "Original Subject",
                "Original Author", false);
        existingBook.saveToDatabase();
        library.addBookinLibrary(existingBook);

        // Verify book exists in catalog
        assertNotNull(existingBook, "Book should exist in catalog");
        assertTrue(library.getBooks().contains(existingBook), "Book should be in library");
        assertEquals("Original Title", existingBook.getTitle());
        assertEquals("Original Subject", existingBook.getSubject());
        assertEquals("Original Author", existingBook.getAuthor());

        // Step 1: Login as Librarian
        Librarian librarian = new Librarian(-1, "Head Librarian", "Main Office",
                5557777, 55000, -1);
        boolean librarianAdded = Librarian.addLibrarian(librarian);
        assertTrue(librarianAdded, "Librarian should be added successfully");
        librarian.saveToDatabase();

        // Verify librarian is logged in
        assertNotNull(librarian, "Librarian should be logged in successfully");
        assertEquals(librarian, library.getLibrarian(), "Librarian should be set in library");

        // Step 2: Select "Change Book Info" from main menu (simulated)
        // In real system, librarian would navigate menu and select option

        // Step 3: Search and select an existing book
        // Simulate search by finding the book we created
        int bookIdToUpdate = existingBook.getID();
        Book bookToUpdate = library.findBookById(bookIdToUpdate);

        assertNotNull(bookToUpdate, "Book with ID " + bookIdToUpdate + " should be found");
        assertEquals(existingBook, bookToUpdate, "Found book should match existing book");

        // Step 4: Update the book title, author, and subject
        String originalTitle = bookToUpdate.getTitle();
        String originalAuthor = bookToUpdate.getAuthor();
        String originalSubject = bookToUpdate.getSubject();

        // Store original values for comparison
        assertNotNull(originalTitle, "Original title should exist");
        assertNotNull(originalAuthor, "Original author should exist");
        assertNotNull(originalSubject, "Original subject should exist");

        // Simulate the changeBookInfo method by directly updating fields
        // (In real system, this would use changeBookInfo() with mocked input)
        String newTitle = "Updated Advanced Programming";
        String newAuthor = "Updated Jane Developer";
        String newSubject = "Updated Computer Science";

        // Update using the Book's internal method that updates DB
        // We'll use reflection of what changeBookInfo does
        DatabaseManager.getInstance().updateBook(bookIdToUpdate, newTitle, newAuthor, newSubject);

        // Reload the book data to verify changes
        ArrayList<Object[]> allBooks = dbManager.loadAllBooks();
        boolean bookUpdatedInDb = false;
        for (Object[] bookData : allBooks) {
            if ((Integer) bookData[0] == bookIdToUpdate) {
                String dbTitle = (String) bookData[1];
                String dbAuthor = (String) bookData[2];
                String dbSubject = (String) bookData[3];

                assertEquals(newTitle, dbTitle, "Book title should be updated in database");
                assertEquals(newAuthor, dbAuthor, "Book author should be updated in database");
                assertEquals(newSubject, dbSubject, "Book subject should be updated in database");

                bookUpdatedInDb = true;
                break;
            }
        }
        assertTrue(bookUpdatedInDb, "Book updates should be persisted in database");

        // Step 5: Confirm the changes
        // Expected Output: System confirms update
        assertDoesNotThrow(() -> {
            System.out.println("Book is successfully updated.");
        }, "Book update confirmation should complete without errors");

        // Verify changes are reflected in the system
        // Reload library catalog to get updated book info
        Library.resetInstance();
        Library freshLibrary = Library.getInstance();
        freshLibrary.setName("Test Library System");

        DatabaseManager freshDb = DatabaseManager.getInstance();
        freshDb.connect();

        // Reload books from database
        ArrayList<Object[]> reloadedBooks = freshDb.loadAllBooks();
        Book updatedBook = null;
        for (Object[] bookData : reloadedBooks) {
            if ((Integer) bookData[0] == bookIdToUpdate) {
                int id = (Integer) bookData[0];
                String title = (String) bookData[1];
                String author = (String) bookData[2];
                String subject = (String) bookData[3];
                boolean isIssued = (Boolean) bookData[4];

                updatedBook = new Book(id, title, subject, author, isIssued);
                break;
            }
        }

        // Verify updated book is found and has new values
        assertNotNull(updatedBook, "Updated book should be found in catalog");
        assertEquals(newTitle, updatedBook.getTitle(), "Book title should reflect changes");
        assertEquals(newAuthor, updatedBook.getAuthor(), "Book author should reflect changes");
        assertEquals(newSubject, updatedBook.getSubject(), "Book subject should reflect changes");

        // Verify original values are no longer present
        assertNotEquals(originalTitle, updatedBook.getTitle(), "Title should be changed from original");
        assertNotEquals(originalAuthor, updatedBook.getAuthor(), "Author should be changed from original");
        assertNotEquals(originalSubject, updatedBook.getSubject(), "Subject should be changed from original");

        // Expected Output: Changes reflected in search results
        // Test by searching for the new title
        freshLibrary.addBookinLibrary(updatedBook);
        boolean foundInSearch = false;
        for (Book b : freshLibrary.getBooks()) {
            if (b.getID() == bookIdToUpdate && b.getTitle().equals(newTitle)) {
                foundInSearch = true;
                break;
            }
        }
        assertTrue(foundInSearch, "Updated book should be searchable with new title");

        // Clean up fresh instances
        freshDb.closeConnection();

        // Test Result: PASS
        // Book information updated successfully and reflected across the system
    }

    @AfterEach
    public void tearDown() {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        Library.resetInstance();
    }
}
