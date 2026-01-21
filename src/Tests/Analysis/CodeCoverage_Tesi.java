package Tests.Analysis;

import LMS.LibraryTestable;
import LMS.Book;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * MC/DC Testing for searchForBooks method
 *
 * This test suite provides COMPLETE coverage :
 * - Statement Coverage: 100%
 * - Branch Coverage: 100%
 * - Condition Coverage: 100%
 * - MC/DC Coverage: 100%
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
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CodeCoverage_Tesi {

    private LibraryTestable library;
    private Book book1, book2;

    @BeforeEach
    public void setUp() {
        library = new LibraryTestable();
        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
    }

    // ==================================================================================
    // DECISION 1: CHOICE VALIDATION
    // Coverage Type: BRANCH COVERAGE + CONDITION COVERAGE + MC/DC
    // Tests: choice.equals("1") || choice.equals("2") || choice.equals("3")
    // ==================================================================================

    @Test
    @Order(1)
    @DisplayName("CCT-1: choice='1' makes decision TRUE")
    public void testChoiceCondition_First() {
        // COVERAGE: Branch (valid path), Condition (choice.equals("1") = TRUE)
        // MC/DC: Tests that condition A independently affects decision
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Clean");
        assertNotNull(result, "choice='1' should make decision TRUE");
    }

    @Test
    @Order(2)
    @DisplayName("CCT-2: choice='2' makes decision TRUE")
    public void testChoiceCondition_Second() {
        // COVERAGE: Branch (valid path), Condition (choice.equals("2") = TRUE)
        // MC/DC: Tests that condition B independently affects decision
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("2", "Software");
        assertNotNull(result, "choice='2' should make decision TRUE");
    }

    @Test
    @Order(3)
    @DisplayName("CCT-3: choice='3' makes decision TRUE")
    public void testChoiceCondition_Third() {
        // COVERAGE: Branch (valid path), Condition (choice.equals("3") = TRUE)
        // MC/DC: Tests that condition C independently affects decision
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("3", "Martin");
        assertNotNull(result, "choice='3' should make decision TRUE");
    }

    @Test
    @Order(4)
    @DisplayName("CCT-4: Invalid choice makes decision FALSE")
    public void testChoiceCondition_AllFalse() {
        // COVERAGE: Branch (invalid path), Condition (all conditions = FALSE)
        // MC/DC: Tests that all conditions FALSE makes decision FALSE
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("4", "Clean");
        assertNull(result, "Invalid choice should make decision FALSE");
    }

    // ==================================================================================
    // DECISION 2A: TITLE MATCH CHECKING (choice = "1")
    // Coverage Type: BRANCH COVERAGE + CONDITION COVERAGE + MC/DC
    // Tests: book.getTitle().toLowerCase().contains(title.toLowerCase())
    // ==================================================================================

    @Test
    @Order(5)
    @DisplayName("CCT-5: Title contains term - Match TRUE")
    public void testTitleMatchCondition_True() {
        // COVERAGE: Branch (match found), Condition (title.contains = TRUE)
        // MC/DC: Tests that title match condition independently affects adding to results
        // STATEMENT: Executes matchedBooks.add(b) statement
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Clean");
        assertNotNull(result, "Match condition TRUE should add to results");
        assertEquals(1, result.size());
    }

    @Test
    @Order(6)
    @DisplayName("CCT-6: Title doesn't contain term - Match FALSE")
    public void testTitleMatchCondition_False() {
        // COVERAGE: Branch (no match), Condition (title.contains = FALSE)
        // MC/DC: Tests that title no-match condition independently prevents adding to results
        // STATEMENT: Skips matchedBooks.add(b) statement
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Nonexistent");
        assertNull(result, "Match condition FALSE should not add to results");
    }

    // ==================================================================================
    // DECISION 2B: SUBJECT MATCH CHECKING (choice = "2")
    // Coverage Type: BRANCH COVERAGE + CONDITION COVERAGE + MC/DC
    // Tests: book.getSubject().toLowerCase().contains(subject.toLowerCase())
    // ==================================================================================

    @Test
    @Order(7)
    @DisplayName("CCT-7: Subject contains term - Match TRUE")
    public void testSubjectMatchCondition_True() {
        // COVERAGE: Branch (match found), Condition (subject.contains = TRUE)
        // MC/DC: Tests that subject match condition independently affects adding to results
        // STATEMENT: Executes matchedBooks.add(b) for subject search
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("2", "Engineering");
        assertNotNull(result, "Subject match should add to results");
        assertEquals(1, result.size());
    }

    @Test
    @Order(8)
    @DisplayName("CCT-8: Subject doesn't contain term - Match FALSE")
    public void testSubjectMatchCondition_False() {
        // COVERAGE: Branch (no match), Condition (subject.contains = FALSE)
        // MC/DC: Tests that subject no-match condition independently prevents adding
        // STATEMENT: Skips matchedBooks.add(b) for subject search
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("2", "History");
        assertNull(result, "Subject no match should return null");
    }

    // ==================================================================================
    // DECISION 2C: AUTHOR MATCH CHECKING (choice = "3")
    // Coverage Type: BRANCH COVERAGE + CONDITION COVERAGE + MC/DC
    // Tests: book.getAuthor().toLowerCase().contains(author.toLowerCase())
    // ==================================================================================

    @Test
    @Order(9)
    @DisplayName("CCT-9: Author contains term - Match TRUE")
    public void testAuthorMatchCondition_True() {
        // COVERAGE: Branch (match found), Condition (author.contains = TRUE)
        // MC/DC: Tests that author match condition independently affects adding to results
        // STATEMENT: Executes matchedBooks.add(b) for author search
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("3", "Martin");
        assertNotNull(result, "Author match should add to results");
        assertEquals(1, result.size());
    }

    @Test
    @Order(10)
    @DisplayName("CCT-10: Author doesn't contain term - Match FALSE")
    public void testAuthorMatchCondition_False() {
        // COVERAGE: Branch (no match), Condition (author.contains = FALSE)
        // MC/DC: Tests that author no-match condition independently prevents adding
        // STATEMENT: Skips matchedBooks.add(b) for author search
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("3", "Shakespeare");
        assertNull(result, "Author no match should return null");
    }

    // ==================================================================================
    // DECISION 3: RESULT CHECK
    // Coverage Type: BRANCH COVERAGE + CONDITION COVERAGE + MC/DC
    // Tests: !matchedBooks.isEmpty()
    // ==================================================================================

    @Test
    @Order(11)
    @DisplayName("CCT-11: matchedBooks NOT empty - Return ArrayList")
    public void testResultCondition_NotEmpty() {
        // COVERAGE: Branch (return ArrayList), Condition (!isEmpty = TRUE)
        // MC/DC: Tests that non-empty results independently causes ArrayList return
        // STATEMENT: Executes return matchedBooks statement
        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        ArrayList<Book> result = library.searchForBooks("1", "Code");
        assertNotNull(result, "Non-empty results should return ArrayList");
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(12)
    @DisplayName("CCT-12: matchedBooks empty - Return null")
    public void testResultCondition_Empty() {
        // COVERAGE: Branch (return null), Condition (isEmpty = TRUE)
        // MC/DC: Tests that empty results independently causes null return
        // STATEMENT: Executes return null statement
        library.addBookinLibrary(book1);
        ArrayList<Book> result = library.searchForBooks("1", "Nonexistent");
        assertNull(result, "Empty results should return null");
    }

    // ==================================================================================
    // COVERAGE SUMMARY
    // ==================================================================================
    /*
     * STATEMENT COVERAGE: 100%
     * All statements in searchForBooks method are executed:
     * - Variable declarations (tests 1-12)
     * - Choice validation (tests 1-4)
     * - Search term assignment (tests 1-12)
     * - Loop through books (tests 5-12)
     * - Match checking for title/subject/author (tests 5-10)
     * - Adding to matchedBooks (tests 5,7,9,11)
     * - Result check (tests 11-12)
     * - Return statements (tests 1-12)
     *
     * BRANCH COVERAGE: 100%
     * All branches tested with both TRUE and FALSE outcomes:
     * - Choice valid/invalid: TRUE (1,2,3) | FALSE (4)
     * - Title match: TRUE (5) | FALSE (6)
     * - Subject match: TRUE (7) | FALSE (8)
     * - Author match: TRUE (9) | FALSE (10)
     * - Results empty: TRUE (12) | FALSE (11)
     *
     * CONDITION COVERAGE: 100%
     * All boolean conditions tested as both TRUE and FALSE:
     * - choice.equals("1"): TRUE (1) | FALSE (2,3,4)
     * - choice.equals("2"): TRUE (2) | FALSE (1,3,4)
     * - choice.equals("3"): TRUE (3) | FALSE (1,2,4)
     * - title.contains: TRUE (5) | FALSE (6)
     * - subject.contains: TRUE (7) | FALSE (8)
     * - author.contains: TRUE (9) | FALSE (10)
     * - !isEmpty: TRUE (11) | FALSE (12)
     *
     * MC/DC COVERAGE: 100%
     * Each condition independently affects its decision outcome:
     * - Decision 1 (choice validation): Tests 1-4 show each OR condition matters
     * - Decision 2a (title match): Tests 5-6 show match condition independence
     * - Decision 2b (subject match): Tests 7-8 show match condition independence
     * - Decision 2c (author match): Tests 9-10 show match condition independence
     * - Decision 3 (result check): Tests 11-12 show isEmpty condition independence
     *
     * TOTAL TESTS: 12
     * ALL COVERAGE METRICS: 100%
     */

    @AfterEach
    public void tearDown() {
        library = null;
        book1 = null;
        book2 = null;
    }
}