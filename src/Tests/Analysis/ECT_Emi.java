package Tests.Analysis;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ECT_Emi {

    private LibraryTestable library;

    @BeforeEach
    void setUp() {
        Library.resetInstance();
        library = new LibraryTestable();
    }

    // EC-1: 'c' → Clerk
    @Test
    @DisplayName("EC-1: 'c' creates Clerk")
    void EC1_CreateClerk() {
        Person p = library.createPersonTestable(
                'c',
                "Alice",
                "Street 1",
                123456,
                500.0
        );
        assertTrue(p instanceof Clerk);
    }

    // EC-2: 'l' → Librarian
    @Test
    @DisplayName("EC-2: 'l' creates Librarian")
    void EC2_CreateLibrarian() {
        Person p = library.createPersonTestable(
                'l',
                "Bob",
                "Street 2",
                987654,
                800.0
        );
        assertTrue(p instanceof Librarian);
    }

    // EC-3: 'b' → Borrower
    @Test
    @DisplayName("EC-3: 'b' creates Borrower")
    void EC3_CreateBorrower() {
        Person p = library.createPersonTestable(
                'b',
                "Charlie",
                "Street 3",
                111111,
                0
        );
        assertTrue(p instanceof Borrower);
    }

    // EC-4: Invalid input → Excpected to FAIL
    @Test
    @DisplayName("EC-4: invalid character causes failure")
    void EC4_InvalidCharacterFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            library.createPersonTestable(
                    '2',
                    "Diana",
                    "Street 4",
                    222222,
                    0
            );
        });
    }
}
