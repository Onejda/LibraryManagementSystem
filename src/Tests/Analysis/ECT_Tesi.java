package Tests.Analysis;

import LMS.Book;
import LMS.LibraryTestable;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * Equivalence Class Testing (ECT) for searchForBooks method
 *
 * EQUIVALENCE CLASSES:
 *
 * Choice Parameter:
 * - EC1 (Valid): "1", "2", "3"
 * - EC2 (Invalid): Other values
 *
 * Search Term:
 * - EC3 (Exact match): Full title/subject/author
 * - EC4 (Partial match): Substring
 * - EC5 (No match): Non-existent term
 *
 * Library State:
 * - EC6 (Has matches): Books that match
 * - EC7 (No matches): No matching books
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ECT_Tesi {

    private LibraryTestable library;
    private Book book1, book2, book3;

    @BeforeEach
    public void setUp() {
        library = new LibraryTestable();
        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
        book3 = new Book(3, "Algorithms", "Computer Science", "Cormen", false);
    }

    @Test
    @Order(1)
    @DisplayName("ECT-1: Valid choice '1' - Search by Title")
    public void testValidChoice_Title() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Clean");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @Order(2)
    @DisplayName("ECT-2: Valid choice '2' - Search by Subject")
    public void testValidChoice_Subject() {
        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        ArrayList<Book> result = library.searchForBooks("2", "Software Engineering");
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @Order(3)
    @DisplayName("ECT-3: Valid choice '3' - Search by Author")
    public void testValidChoice_Author() {
        library.addBookinLibrary(book3);
        ArrayList<Book> result = library.searchForBooks("3", "Cormen");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @Order(4)
    @DisplayName("ECT-4: Invalid choice - Non-numeric")
    public void testInvalidChoice() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("abc", "Clean");
        assertNull(result, "Invalid choice should return null");
    }

    @Test
    @Order(5)
    @DisplayName("ECT-5: Partial match search")
    public void testPartialMatch() {
        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        ArrayList<Book> result = library.searchForBooks("1", "Design");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @Order(6)
    @DisplayName("ECT-6: No match found")
    public void testNoMatch() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Nonexistent");
        assertNull(result, "No matches should return null");
    }

    @Test
    @Order(7)
    @DisplayName("ECT-7: Case insensitive search")
    public void testCaseInsensitive() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "CLEAN CODE");
        assertNotNull(result, "Search should be case-insensitive");
        assertEquals(1, result.size());
    }

    @AfterEach
    public void tearDown() {
        library = null;
        book1 = null;
        book2 = null;
        book3 = null;
    }
}
