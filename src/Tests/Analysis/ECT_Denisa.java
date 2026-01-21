package Tests.Analysis;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * Equivalence Class Testing for getBooks() method from Library class
 *
 * Equivalence Classes:
 * - EC1: Empty Library (size = 0)
 * - EC2: Single Book (size = 1)
 * - EC3: Multiple Books (size > 1, typical: 2-20)
 * - EC4: Large Collection (size >= 100)
 *
 * Testing Strategy: Test one representative from each equivalence class
 * to verify consistent behavior across all members of the class
 */

public class ECT_Denisa {

    private Library library;

    @BeforeEach
    public void setUp() {
        // Reset singleton and initialize fresh library
        Library.resetInstance();
        library = Library.getInstance();
        library.setName("Test Library");
        library.setFine(20.0);
        library.setReturnDeadline(5);
        library.setRequestExpiry(7);
    }

    @AfterEach
    public void tearDown() {
        Library.resetInstance();
    }

    /**
     * ECT-01: Equivalence Class 1 - Empty Library
     * Input: booksInLibrary.size() = 0
     * Expected: Empty ArrayList returned
     */
    @Test
    @DisplayName("ECT-01: Empty library - should return empty ArrayList")
    public void testGetBooks_EmptyLibrary() {
        // No books added - library is empty
        ArrayList<Book> books = library.getBooks();

        assertNotNull(books, "getBooks() should not return null");
        assertEquals(0, books.size(), "Empty library should return ArrayList with size 0");
        assertTrue(books.isEmpty(), "isEmpty() should return true for empty library");
    }

    /**
     * ECT-02: Equivalence Class 2 - Single Book
     * Input: booksInLibrary.size() = 1
     * Expected: ArrayList with exactly 1 Book object
     */
    @Test
    @DisplayName("ECT-02: Single book in library - should return ArrayList with 1 element")
    public void testGetBooks_SingleBook() {
        // Add exactly one book
        Book book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        library.addBookinLibrary(book1);

        ArrayList<Book> books = library.getBooks();

        assertNotNull(books, "getBooks() should not return null");
        assertEquals(1, books.size(), "Library with 1 book should return ArrayList of size 1");
        assertFalse(books.isEmpty(), "isEmpty() should return false");
        assertEquals("Clean Code", books.get(0).getTitle(), "Book title should match");
        assertSame(book1, books.get(0), "Should return the exact same Book object");
    }

    /**
     * ECT-03: Equivalence Class 3 - Multiple Books (5 books)
     * Input: booksInLibrary.size() = 5
     * Expected: ArrayList with 5 Book objects
     */
    @Test
    @DisplayName("ECT-03: Five books in library - multiple books equivalence class")
    public void testGetBooks_FiveBooks() {
        // Add 5 books to library
        Book book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        Book book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
        Book book3 = new Book(3, "Database Systems", "Databases", "Ramez Elmasri", false);
        Book book4 = new Book(4, "Algorithms", "Computer Science", "Cormen", false);
        Book book5 = new Book(5, "Operating Systems", "Computer Science", "Silberschatz", false);

        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        library.addBookinLibrary(book3);
        library.addBookinLibrary(book4);
        library.addBookinLibrary(book5);

        ArrayList<Book> books = library.getBooks();

        assertNotNull(books, "getBooks() should not return null");
        assertEquals(5, books.size(), "Library with 5 books should return ArrayList of size 5");

        // Verify all books are present
        assertTrue(books.contains(book1), "Should contain book1");
        assertTrue(books.contains(book2), "Should contain book2");
        assertTrue(books.contains(book3), "Should contain book3");
        assertTrue(books.contains(book4), "Should contain book4");
        assertTrue(books.contains(book5), "Should contain book5");
    }

    /**
     * ECT-04: Equivalence Class 3 - Multiple Books (15 books)
     * Input: booksInLibrary.size() = 15
     * Expected: ArrayList with 15 Book objects
     *
     * This tests a different representative from EC3 to ensure
     * consistent behavior across the equivalence class
     */
    @Test
    @DisplayName("ECT-04: Fifteen books in library - larger multiple books test")
    public void testGetBooks_FifteenBooks() {
        // Add 15 books
        for (int i = 1; i <= 15; i++) {
            Book book = new Book(i, "Book " + i, "Subject " + i, "Author " + i, false);
            library.addBookinLibrary(book);
        }

        ArrayList<Book> books = library.getBooks();

        assertNotNull(books, "getBooks() should not return null");
        assertEquals(15, books.size(), "Library with 15 books should return ArrayList of size 15");
        assertFalse(books.isEmpty(), "isEmpty() should return false");

        // Verify books are accessible
        assertEquals("Book 1", books.get(0).getTitle(), "First book should be accessible");
        assertEquals("Book 15", books.get(14).getTitle(), "Last book should be accessible");
    }

    /**
     * ECT-05: Equivalence Class 4 - Large Collection (100 books)
     * Input: booksInLibrary.size() = 100
     * Expected: ArrayList with 100 Book objects
     *
     * Tests the method with a large dataset to ensure no performance
     * or capacity issues
     */
    @Test
    @DisplayName("ECT-05: One hundred books - large collection test")
    public void testGetBooks_LargeCollection() {
        // Add 100 books
        for (int i = 1; i <= 100; i++) {
            Book book = new Book(i, "Book " + i, "Subject " + (i % 10), "Author " + (i % 20), false);
            library.addBookinLibrary(book);
        }

        ArrayList<Book> books = library.getBooks();

        assertNotNull(books, "getBooks() should not return null");
        assertEquals(100, books.size(), "Library with 100 books should return ArrayList of size 100");

        // Verify first and last books
        assertEquals("Book 1", books.get(0).getTitle(), "First book should be correct");
        assertEquals("Book 100", books.get(99).getTitle(), "Last book should be correct");

        // Verify all books are unique objects
        long uniqueIds = books.stream().map(Book::getID).distinct().count();
        assertEquals(100, uniqueIds, "All 100 books should have unique IDs");
    }

}