package Tests;

import LMS.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class LibrarianTest {

    @BeforeEach
    void setUp() throws Exception {
        // Reset static counters and singletons
        Librarian.currentOfficeNumber = 0;
        Library.librarian = null;

        // Ensure persons list is initialized and clean
        if (Library.persons == null) {
            Library.persons = new ArrayList<>();
        } else {
            Library.persons.clear();
        }

        // Reset DatabaseManager singleton (if used internally)
        Field instanceField = DatabaseManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    // Constructor Tests

    @Test
    @DisplayName("Constructor with -1 office number auto-generates office number")
    void constructor_autoGeneratesOfficeNumber() {
        Librarian librarian = new Librarian(-1, "John", "Address", 12345, 5000.0, -1);

        assertEquals(1, librarian.officeNo);
    }

    @Test
    @DisplayName("Constructor with explicit office number uses provided value")
    void constructor_usesExplicitOfficeNumber() {
        Librarian librarian = new Librarian(-1, "Jane", "Address", 12345, 5000.0, 10);

        assertEquals(10, librarian.officeNo);
    }

    @Test
    @DisplayName("Constructor increments static office counter")
    void constructor_incrementsOfficeCounter() {
        new Librarian(-1, "Lib1", "Address", 111, 5000.0, -1);

        assertEquals(1, Librarian.currentOfficeNumber);
    }

    // Getter Tests

    @Test
    @DisplayName("getOfficeNo returns correct office number")
    void getOfficeNo_returnsCorrectValue() {
        Librarian librarian = new Librarian(-1, "John", "Address", 12345, 5000.0, 5);

        assertEquals(5, librarian.getOfficeNo());
    }

    // addLibrarian Tests

    @Test
    @DisplayName("addLibrarian successfully adds the first librarian")
    void addLibrarian_firstLibrarianAdded() {
        Librarian librarian = new Librarian(-1, "John", "Address", 12345, 5000.0, -1);

        boolean result = Librarian.addLibrarian(librarian);

        assertTrue(result);
        assertEquals(librarian, Library.librarian);
    }

    @Test
    @DisplayName("addLibrarian rejects adding a second librarian")
    void addLibrarian_secondLibrarianRejected() {
        Librarian first = new Librarian(-1, "First", "Address", 111, 5000.0, -1);
        Librarian second = new Librarian(-1, "Second", "Address", 222, 5000.0, -1);

        Librarian.addLibrarian(first);
        boolean result = Librarian.addLibrarian(second);

        assertFalse(result);
        assertEquals(first, Library.librarian);
    }

    @Test
    @DisplayName("addLibrarian adds librarian to Library persons list")
    void addLibrarian_addedToPersonsList() {
        Librarian librarian = new Librarian(-1, "John", "Address", 12345, 5000.0, -1);

        Librarian.addLibrarian(librarian);

        assertTrue(Library.persons.contains(librarian));
    }
}
