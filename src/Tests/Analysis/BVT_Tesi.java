package Tests.Analysis;

import LMS.LibraryTestable;
import LMS.Book;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * Boundary Value Testing (BVT) for searchForBooks method
 *
 * BOUNDARY VALUE ANALYSIS:
 *
 * Variable 1: choice (String: "1", "2", "3")
 * - Below minimum: "0"
 * - Minimum: "1"
 * - Maximum: "3"
 * - Above maximum: "4"
 *
 * Variable 2: booksInLibrary.size()
 * - Empty: 0 books
 * - Normal: 2 books
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BVT_Tesi {

    private LibraryTestable library;
    private Book book1, book2;

    @BeforeEach
    public void setUp() {
        library = new LibraryTestable();
        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
    }

    @Test
    @Order(1)
    @DisplayName("BVT-1: Choice below minimum (0) - should return null")
    public void testChoice_BelowMinimum() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("0", "Clean");
        assertNull(result, "Invalid choice '0' should return null");
    }

    @Test
    @Order(2)
    @DisplayName("BVT-2: Choice at minimum (1) - should work")
    public void testChoice_Minimum() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Clean");
        assertNotNull(result, "Valid choice '1' should return results");
        assertEquals(1, result.size());
    }

    @Test
    @Order(3)
    @DisplayName("BVT-3: Choice at maximum (3) - should work")
    public void testChoice_Maximum() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("3", "Martin");
        assertNotNull(result, "Valid choice '3' should return results");
        assertEquals(1, result.size());
    }

    @Test
    @Order(4)
    @DisplayName("BVT-4: Choice above maximum (4) - should return null")
    public void testChoice_AboveMaximum() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("4", "Clean");
        assertNull(result, "Invalid choice '4' should return null");
    }

    @Test
    @Order(5)
    @DisplayName("BVT-5: Empty library - should return null")
    public void testLibrarySize_Empty() {
        // Don't add any books
        ArrayList<Book> result = library.searchForBooks("1", "Clean");
        assertNull(result, "Empty library should return null");
    }

    @Test
    @Order(6)
    @DisplayName("BVT-6: Library with multiple books - should find matches")
    public void testLibrarySize_Multiple() {
        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        ArrayList<Book> result = library.searchForBooks("2", "Software");
        assertNotNull(result);
        assertEquals(2, result.size(), "Should find both books with Software subject");
    }

    @AfterEach
    public void tearDown() {
        library = null;
        book1 = null;
        book2 = null;
    }
}