package Tests.Analysis;

import LMS.LibraryTestable;
import LMS.Book;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * MC/DC Testing for searchForBooks method
 *
 * KEY DECISIONS:
 *
 * Decision 1: Choice validation
 * D1 = choice.equals("1") || choice.equals("2") || choice.equals("3")
 *
 * Decision 2: Match checking (for each search type)
 * D2 = book field contains search term (case insensitive)
 *
 * Decision 3: Result check
 * D3 = !matchedBooks.isEmpty()
 *
 * MC/DC requires each condition to independently affect the decision.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MCDC_Tesi {

    private LibraryTestable library;
    private Book book1, book2;

    @BeforeEach
    public void setUp() {
        library = new LibraryTestable();
        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
    }

    // ========== Decision 1: Choice Validation ==========

    @Test
    @Order(1)
    @DisplayName("MCDC-1: choice='1' makes decision TRUE")
    public void testChoiceCondition_First() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Clean");
        assertNotNull(result, "choice='1' should make decision TRUE");
    }

    @Test
    @Order(2)
    @DisplayName("MCDC-2: choice='2' makes decision TRUE")
    public void testChoiceCondition_Second() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("2", "Software");
        assertNotNull(result, "choice='2' should make decision TRUE");
    }

    @Test
    @Order(3)
    @DisplayName("MCDC-3: choice='3' makes decision TRUE")
    public void testChoiceCondition_Third() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("3", "Martin");
        assertNotNull(result, "choice='3' should make decision TRUE");
    }

    @Test
    @Order(4)
    @DisplayName("MCDC-4: Invalid choice makes decision FALSE")
    public void testChoiceCondition_AllFalse() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("4", "Clean");
        assertNull(result, "Invalid choice should make decision FALSE");
    }

    // ========== Decision 2: Match Checking ==========

    @Test
    @Order(5)
    @DisplayName("MCDC-5: Title contains term - Match TRUE")
    public void testMatchCondition_True() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Clean");
        assertNotNull(result, "Match condition TRUE should add to results");
        assertEquals(1, result.size());
    }

    @Test
    @Order(6)
    @DisplayName("MCDC-6: Title doesn't contain term - Match FALSE")
    public void testMatchCondition_False() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Nonexistent");
        assertNull(result, "Match condition FALSE should not add to results");
    }

    // ========== Decision 3: Result Check ==========

    @Test
    @Order(7)
    @DisplayName("MCDC-7: matchedBooks NOT empty - Return ArrayList")
    public void testResultCondition_NotEmpty() {
        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        ArrayList<Book> result = library.searchForBooks("1", "Code");
        assertNotNull(result, "Non-empty results should return ArrayList");
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(8)
    @DisplayName("MCDC-8: matchedBooks empty - Return null")
    public void testResultCondition_Empty() {
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Nonexistent");
        assertNull(result, "Empty results should return null");
    }

    @AfterEach
    public void tearDown() {
        library = null;
        book1 = null;
        book2 = null;
    }
}
