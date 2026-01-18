package Tests;

import LMS.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryTests_Emi {

    private Library library;
    private InputStream originalIn;

    // =====================================================
    // Test Setup & Teardown
    // =====================================================

    @BeforeEach
    void setUp() {
        originalIn = System.in;
        Library.resetInstance();
        library = Library.getInstance();
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        Library.resetInstance();
    }

    // =====================================================
    // Tests for: setReturnDeadline(int)
    // =====================================================

    @Test
    @DisplayName("setReturnDeadline accepts positive value")
    void testSetReturnDeadlinePositive() {
        library.setReturnDeadline(7);
        assertEquals(7, library.book_return_deadline);
    }

    // ❌ Expected to FAIL – missing validation
    @Test
    @DisplayName("setReturnDeadline should reject negative values")
    void testSetReturnDeadlineNegative() {
        library.setReturnDeadline(-5);
        assertTrue(library.book_return_deadline >= 0);
    }

    // =====================================================
    // Tests for: getPersons()
    // =====================================================

    @Test
    @DisplayName("getPersons returns empty list initially")
    void testGetPersonsInitiallyEmpty() {
        assertTrue(library.getPersons().isEmpty());
    }

    @Test
    @DisplayName("getPersons returns list after adding borrower")
    void testGetPersonsAfterAddingBorrower() {
        Borrower borrower = new Borrower(1, "Ali", "Addr", 123);
        library.addBorrower(borrower);
        assertEquals(1, library.getPersons().size());
    }

    // ❌ Expected to FAIL – encapsulation issue
    @Test
    @DisplayName("External modification of getPersons should not affect library")
    void testGetPersonsEncapsulationViolation() {
        Borrower borrower = new Borrower(1, "Ali", "Addr", 123);
        library.addBorrower(borrower);

        ArrayList<Person> persons = library.getPersons();
        persons.clear();

        assertEquals(1, library.getPersons().size());
    }

    // =====================================================
    // Tests for: addBorrower(Borrower)
    // =====================================================

    @Test
    @DisplayName("addBorrower adds borrower correctly")
    void testAddBorrower() {
        Borrower borrower = new Borrower(1, "User", "Addr", 123);
        library.addBorrower(borrower);
        assertSame(borrower, library.getPersons().get(0));
    }

    // ❌ Expected to FAIL – null not validated
    @Test
    @DisplayName("addBorrower should reject null borrower")
    void testAddBorrowerNull() {
        library.addBorrower(null);
        assertTrue(library.getPersons().isEmpty());
    }

    // ❌ Expected to FAIL – duplicate IDs allowed
    @Test
    @DisplayName("addBorrower should not allow duplicate IDs")
    void testAddBorrowerDuplicateId() {
        Borrower b1 = new Borrower(1, "User1", "Addr1", 111);
        Borrower b2 = new Borrower(1, "User2", "Addr2", 222);

        library.addBorrower(b1);
        library.addBorrower(b2);

        assertEquals(1, library.getPersons().size());
    }

    // =====================================================
    // Tests for: login()
    // =====================================================

    @Test
    @DisplayName("login succeeds with valid borrower credentials")
    void testLoginValidBorrower() {
        Borrower borrower = new Borrower(100, "User", "Addr", 123);
        library.addBorrower(borrower);

        String input = "100\n" + borrower.getPassword() + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertEquals(borrower, library.login());
    }

    @Test
    @DisplayName("login fails with wrong password")
    void testLoginWrongPassword() {
        Borrower borrower = new Borrower(100, "User", "Addr", 123);
        library.addBorrower(borrower);

        String input = "100\nwrong\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertNull(library.login());
    }

    @Test
    @DisplayName("login fails with non-existent ID")
    void testLoginNonExistentId() {
        String input = "999\npass\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertNull(library.login());
    }

    // ❌ Expected to FAIL – empty password accepted
    @Test
    @DisplayName("login should reject empty password")
    void testLoginEmptyPassword() {
        Borrower borrower = new Borrower(1, "Ali", "Addr", 123);
        library.addBorrower(borrower);

        String input = "1\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertNull(library.login());
    }

    // =====================================================
    // createPerson(char)
    // =====================================================
    // NOT unit tested due to console I/O coupling
}
