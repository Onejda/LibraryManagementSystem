package Tests.Analysis;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Boundary Value Testing for computeFine1() method from Loan class
 *
 * Testing Strategy: Tests the boundaries of overdue days calculation
 * - Critical boundary at deadline (0 days overdue)
 * - Below minimum (early returns, negative overdue)
 * - Just above minimum (1 day overdue)
 * - Normal values (moderate overdue)
 * - Large values (significant overdue)
 * - Very large values (extreme overdue)
 */

public class BVT_Denisa {

    private Library library;
    private Borrower testBorrower;
    private Book testBook;
    private Staff testStaff;

    // Default library configuration
    private static final double PER_DAY_FINE = 20.0;
    private static final int BOOK_RETURN_DEADLINE = 5;

    @BeforeEach
    public void setUp() {
        // Reset singleton and initialize library
        Library.resetInstance();
        library = Library.getInstance();
        library.setFine(PER_DAY_FINE);
        library.setReturnDeadline(BOOK_RETURN_DEADLINE);
        library.setRequestExpiry(7);
        library.setName("Test Library");

        // Create test entities
        testBorrower = new Borrower(1, "Test Borrower", "123 Test St", 5551234);
        testBook = new Book(1, "Test Book", "Computer Science", "Test Author", false);
        testStaff = new Clerk(2, "Test Clerk", "456 Staff Ave", 5555678, 25000, 1);
    }

    @AfterEach
    public void tearDown() {
        Library.resetInstance();
    }

    /**
     * BVT-01: Below minimum boundary - Early return (2 days before deadline)
     * Days after issue: 3, Deadline: 5, Overdue: -2
     * Expected: 0.0 (no fine for early return)
     */
    @Test
    @DisplayName("BVT-01: Early return - 2 days before deadline")
    public void testComputeFine_EarlyReturn() {
        // Calculate date 3 days ago
        Date issueDate = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();

        Loan loan = new Loan(testBorrower, testBook, testStaff, testStaff, issueDate, returnDate, false);

        double fine = loan.computeFine1();

        assertEquals(0.0, fine, 0.01,
                "Early return (3 days, deadline 5) should result in 0.0 fine");
    }

    /**
     * BVT-02: Minimum boundary - Exact deadline (0 days overdue)
     * Days after issue: 5, Deadline: 5, Overdue: 0
     * Expected: 0.0 (no fine at exact deadline)
     */
    @Test
    @DisplayName("BVT-02: Return at exact deadline - 0 days overdue")
    public void testComputeFine_ExactDeadline() {
        // Calculate date exactly 5 days ago
        Date issueDate = new Date(System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();

        Loan loan = new Loan(testBorrower, testBook, testStaff, testStaff, issueDate, returnDate, false);

        double fine = loan.computeFine1();

        assertEquals(0.0, fine, 0.01,
                "Return at deadline (5 days, deadline 5) should result in 0.0 fine");
    }

    /**
     * BVT-03: Just above minimum - 1 day overdue
     * Days after issue: 6, Deadline: 5, Overdue: 1
     * Expected: 20.0 (1 day × 20.0 per day)
     */
    @Test
    @DisplayName("BVT-03: One day overdue - first fine boundary")
    public void testComputeFine_OneDayOverdue() {
        // Calculate date 6 days ago
        Date issueDate = new Date(System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();

        Loan loan = new Loan(testBorrower, testBook, testStaff, testStaff, issueDate, returnDate, false);

        double fine = loan.computeFine1();

        assertEquals(20.0, fine, 0.01,
                "1 day overdue should result in 20.0 fine (1 × 20.0)");
    }

    /**
     * BVT-04: Normal value - 5 days overdue
     * Days after issue: 10, Deadline: 5, Overdue: 5
     * Expected: 100.0 (5 days × 20.0 per day)
     */
    @Test
    @DisplayName("BVT-04: Five days overdue - normal operational value")
    public void testComputeFine_FiveDaysOverdue() {
        // Calculate date 10 days ago
        Date issueDate = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();

        Loan loan = new Loan(testBorrower, testBook, testStaff, testStaff, issueDate, returnDate, false);

        double fine = loan.computeFine1();

        assertEquals(100.0, fine, 0.01,
                "5 days overdue should result in 100.0 fine (5 × 20.0)");
    }

    /**
     * BVT-05: Large valid value - 30 days overdue
     * Days after issue: 35, Deadline: 5, Overdue: 30
     * Expected: 600.0 (30 days × 20.0 per day)
     */
    @Test
    @DisplayName("BVT-05: Thirty days overdue - large value test")
    public void testComputeFine_ThirtyDaysOverdue() {
        // Calculate date 35 days ago
        Date issueDate = new Date(System.currentTimeMillis() - (35L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();

        Loan loan = new Loan(testBorrower, testBook, testStaff, testStaff, issueDate, returnDate, false);

        double fine = loan.computeFine1();

        assertEquals(600.0, fine, 0.01,
                "30 days overdue should result in 600.0 fine (30 × 20.0)");
    }

    /**
     * BVT-06: Very large value - 360 days overdue (1 year)
     * Days after issue: 365, Deadline: 5, Overdue: 360
     * Expected: 7200.0 (360 days × 20.0 per day)
     */
    @Test
    @DisplayName("BVT-06: One year overdue - very large value test")
    public void testComputeFine_OneYearOverdue() {
        // Calculate date 365 days ago
        Date issueDate = new Date(System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();

        Loan loan = new Loan(testBorrower, testBook, testStaff, testStaff, issueDate, returnDate, false);

        double fine = loan.computeFine1();

        assertEquals(7200.0, fine, 0.01,
                "360 days overdue should result in 7200.0 fine (360 × 20.0)");
    }

    /**
     * BVT-07: Special case - Fine already paid
     * Expected: 0.0 (regardless of overdue days)
     */
    @Test
    @DisplayName("BVT-07: Fine already paid - should return 0.0")
    public void testComputeFine_AlreadyPaid() {
        // Create overdue loan (10 days overdue) but marked as paid
        Date issueDate = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();

        Loan loan = new Loan(testBorrower, testBook, testStaff, testStaff, issueDate, returnDate, true);

        double fine = loan.computeFine1();

        assertEquals(0.0, fine, 0.01,
                "Fine already paid should return 0.0 regardless of overdue days");
    }

    /**
     * Additional test: Book not yet returned (dateReturned is null)
     * Should calculate fine based on current date
     */
    @Test
    @DisplayName("BVT-Extra: Book not returned yet - calculate current fine")
    public void testComputeFine_NotYetReturned() {
        // Issue date 10 days ago, no return date (still borrowed)
        Date issueDate = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));

        Loan loan = new Loan(testBorrower, testBook, testStaff, null, issueDate, null, false);

        double fine = loan.computeFine1();

        // Should be approximately 100.0 (5 days overdue from 10-day loan with 5-day deadline)
        assertTrue(fine >= 95.0 && fine <= 105.0,
                "Current fine for unreturned book (10 days ago) should be around 100.0");
    }
}