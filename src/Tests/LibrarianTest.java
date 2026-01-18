package Tests;

import LMS.*;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class LibrarianTest {

    @BeforeEach
    void setUp() throws Exception {
        Librarian.currentOfficeNumber = 0;
        Library.librarian = null;

        if (Library.persons == null) {
            Library.persons = new ArrayList<>();
        } else {

            // Reset DatabaseManager
            Field instanceField = DatabaseManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        }
    }
    // ============================================================================
    // Constructor Tests
    // ============================================================================

    @Test
    @DisplayName("Constructor with -1 auto-generates office number")
    void testConstructor_autoGenerateOffice() {
        Librarian lib = new Librarian(-1, "John", "Address", 12345, 5000.0, -1);

        assertEquals(1, lib.officeNo);
    }

    @Test
    @DisplayName("Constructor with explicit office uses that number")
    void testConstructor_explicitOffice() {
        Librarian lib = new Librarian(-1, "Jane", "Address", 12345, 5000.0, 10);

        assertEquals(10, lib.officeNo);
    }

    @Test
    @DisplayName("Constructor increments static counter")
    void testConstructor_incrementsCounter() {
        new Librarian(-1, "Lib1", "Addr", 111, 5000.0, -1);

        assertEquals(1, Librarian.currentOfficeNumber);
    }

    // ============================================================================
    // Getter Test
    // ============================================================================

    @Test
    @DisplayName("getOfficeNo returns office number")
    void testGetOfficeNo() {
        Librarian lib = new Librarian(-1, "John", "Address", 12345, 5000.0, 5);

        assertEquals(5, lib.getOfficeNo());
    }

    // ============================================================================
    // addLibrarian Tests
    // ============================================================================

    @Test
    @DisplayName("addLibrarian adds first librarian successfully")
    void testAddLibrarian_firstLibrarian() {
        Librarian lib = new Librarian(-1, "John", "Address", 12345, 5000.0, -1);

        boolean result = Librarian.addLibrarian(lib);

        assertTrue(result);
        assertEquals(lib, Library.librarian);
    }

    @Test
    @DisplayName("addLibrarian rejects second librarian")
    void testAddLibrarian_rejectsSecond() {
        Librarian lib1 = new Librarian(-1, "First", "Address", 111, 5000.0, -1);
        Librarian lib2 = new Librarian(-1, "Second", "Address", 222, 5000.0, -1);

        Librarian.addLibrarian(lib1);
        boolean result = Librarian.addLibrarian(lib2);

        assertFalse(result);
        assertEquals(lib1, Library.librarian);
    }

    @Test
    @DisplayName("addLibrarian adds librarian to persons list")
    void testAddLibrarian_addsToPersonsList() {
        Librarian lib = new Librarian(-1, "John", "Address", 12345, 5000.0, -1);

        Librarian.addLibrarian(lib);

        assertTrue(Library.persons.contains(lib));
    }
}