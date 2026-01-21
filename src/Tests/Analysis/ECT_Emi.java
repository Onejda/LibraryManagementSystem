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

    // EC-1: 'c' → Valid Clerk
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

    // EC-2: 'l' → Valid Librarian
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

    // EC-3: 'b' → Default Borrower
    @Test
    @DisplayName("EC-3: 'b' defaults to Borrower")
    void EC3_DefaultBorrower_b() {
        Person p = library.createPersonTestable(
                'b',
                "Charlie",
                "Street 3",
                111111,
                0
        );

        assertTrue(p instanceof Borrower);
    }

    // EC-4: '2' → Default Borrower
    @Test
    @DisplayName("EC-4: '2' defaults to Borrower")
    void EC4_DefaultBorrower_digit() {
        Person p = library.createPersonTestable(
                '2',
                "Diana",
                "Street 4",
                222222,
                0
        );

        assertTrue(p instanceof Borrower);
    }

    // EC-5: 'C' → Default Borrower (case-sensitive)
    @Test
    @DisplayName("EC-5: 'C' defaults to Borrower (case-sensitive)")
    void EC5_DefaultBorrower_uppercase() {
        Person p = library.createPersonTestable(
                'C',
                "Eve",
                "Street 5",
                333333,
                0
        );

        assertTrue(p instanceof Borrower);
    }
}
