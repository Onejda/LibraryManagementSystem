package Tests.Analysis;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * Code Coverage & MC/DC Tests for login() method
 *
 * Coverage Achieved:
 * - Statement Coverage: 100%
 * - Branch Coverage: 100%
 * - Condition Coverage: 100%
 * - MC/DC Coverage: Satisfied
 *
 * The testable version loginTestable(int id, String password)
 * is used to remove Scanner dependency while preserving logic.
 *
 * Author: Emi
 */
public class CodeCoverage_Emi {

    private LibraryTestable library;
    private Librarian librarian;
    private Borrower borrower;

    private String librarianPassword;
    private String borrowerPassword;

    @BeforeEach
    void setUp() {
        Library.resetInstance(); // resets static fields

        //re-initialize static persons list
        Library.persons = new ArrayList<>();

        library = new LibraryTestable();

        // Create Librarian
        librarian = new Librarian(1, "Lib", "Addr", 111, 1000, -1);
        Librarian.addLibrarian(librarian);
        librarianPassword = librarian.getPassword(); // "1"

        // Create Borrower
        borrower = new Borrower(2, "Bor", "Addr", 222);
        Library.persons.add(borrower);
        borrowerPassword = borrower.getPassword(); // "2"
    }

    /*
     * =====================================================
     * STATEMENT COVERAGE TESTS
     * =====================================================
     */

    // SC-01: Valid librarian ID & password
    @Test
    void SC01_ValidLibrarianLogin() {
        Person p = library.loginTestable(1, librarianPassword);
        assertNotNull(p);
        assertTrue(p instanceof Librarian);
    }

    // SC-02: Librarian ID with wrong password
    @Test
    void SC02_LibrarianWrongPassword() {
        Person p = library.loginTestable(1, "wrong");
        assertNull(p);
    }

    // SC-03: Valid borrower ID & password
    @Test
    void SC03_ValidBorrowerLogin() {
        Person p = library.loginTestable(2, borrowerPassword);
        assertNotNull(p);
        assertTrue(p instanceof Borrower);
    }

    // SC-04: Borrower ID with wrong password
    @Test
    void SC04_BorrowerWrongPassword() {
        Person p = library.loginTestable(2, "wrong");
        assertNull(p);
    }

    // SC-05: No matching ID
    @Test
    void SC05_NoMatchingID() {
        Person p = library.loginTestable(99, "any");
        assertNull(p);
    }

    /*
     * =====================================================
     * MC/DC – Decision 1
     * librarian != null && librarian.getID() == id
     * =====================================================
     */

    // MC-01: librarian != null = true, ID match = true
    @Test
    void MC01_LibrarianExistsAndIDMatches() {
        Person p = library.loginTestable(1, librarianPassword);
        assertNotNull(p);
    }

    // MC-02: librarian != null = true, ID match = false
    @Test
    void MC02_LibrarianExistsIDMismatch() {
        Person p = library.loginTestable(99, librarianPassword);
        assertNull(p);
    }

    @Test
    void MC03_LibrarianIsNull() {
        // Remove librarian reference
        Library.librarian = null;

        // ALSO remove librarian from persons list
        Library.persons.clear();

        Person p = library.loginTestable(1, librarianPassword);
        assertNull(p);
    }

    /*
     * =====================================================
     * MC/DC – Decision 2
     * p.getID() == id && password matches borrower
     * =====================================================
     */

    // MC-04: ID match = true, password match = true
    @Test
    void MC04_BorrowerIDAndPasswordMatch() {
        Person p = library.loginTestable(2, borrowerPassword);
        assertNotNull(p);
    }

    // MC-05: ID match = true, password match = false
    @Test
    void MC05_BorrowerPasswordMismatch() {
        Person p = library.loginTestable(2, "wrong");
        assertNull(p);
    }

    // MC-06: ID match = false, password match = true
    @Test
    void MC06_BorrowerIDMismatch() {
        Person p = library.loginTestable(99, borrowerPassword);
        assertNull(p);
    }
}
