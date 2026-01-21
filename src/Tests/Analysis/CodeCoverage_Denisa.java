package Tests.Analysis;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Code Coverage Testing for computeFine2(Borrower borrower) method from Library class
 * Coverage Goals:
 * - Statement Coverage: 100%
 * - Branch Coverage: 100%
 * - Condition Coverage: 100%
 * - MC/DC Coverage: Satisfied (both conditions tested for independence)
 *
 * Test Cases:
 * CC-01: No loans in system
 * CC-02: Borrower has no loans (other borrowers have loans)
 * CC-03: Borrower has one loan with fine
 * CC-04: Borrower has multiple loans with various fines
 */
public class CodeCoverage_Denisa {

    private Library library;
    private Borrower borrower1;
    private Borrower borrower2;
    private Borrower borrower3;
    private Book book1, book2, book3, book4, book5;
    private Staff clerk;

    @BeforeEach
    public void setUp() {
        // Reset and initialize library
        Library.resetInstance();
        library = Library.getInstance();
        library.setName("Test Library");
        library.setFine(20.0);  // Rs 20 per day
        library.setReturnDeadline(5);  // 5 days deadline
        library.setRequestExpiry(7);

        // Create borrowers
        borrower1 = new Borrower(1, "Alice Brown", "123 Main St", 5551111);
        borrower2 = new Borrower(2, "Bob Wilson", "456 Oak Ave", 5552222);
        borrower3 = new Borrower(3, "Carol Davis", "789 Pine Rd", 5553333);

        library.addBorrower(borrower1);
        library.addBorrower(borrower2);
        library.addBorrower(borrower3);

        // Create books
        book1 = new Book(1, "Clean Code", "Software Engineering", "Robert Martin", false);
        book2 = new Book(2, "Design Patterns", "Software Engineering", "Gang of Four", false);
        book3 = new Book(3, "Database Systems", "Databases", "Ramez Elmasri", false);
        book4 = new Book(4, "Algorithms", "Computer Science", "Cormen", false);
        book5 = new Book(5, "Operating Systems", "Computer Science", "Silberschatz", false);

        library.addBookinLibrary(book1);
        library.addBookinLibrary(book2);
        library.addBookinLibrary(book3);
        library.addBookinLibrary(book4);
        library.addBookinLibrary(book5);

        // Create staff
        clerk = new Clerk(10, "Jane Clerk", "Staff Office", 5559999, 25000, 1);
    }

    @AfterEach
    public void tearDown() {
        Library.resetInstance();
    }

    /**
     * CC-01: No loans in system
     *
     * Coverage:
     * - Statement: Covers initialization, print headers, loop condition (false), return
     * - Branch: Loop entry = False (no loans)
     * - Condition: i < loans.size() = False (0 < 0)
     *
     * Expected: totalFine = 0.0
     */
    @Test
    @DisplayName("CC-01: No loans in system - should return 0.0")
    public void testComputeFine2_NoLoans() {
        // No loans added to library - loans list is empty

        double totalFine = library.computeFine2(borrower1);

        assertEquals(0.0, totalFine, 0.01,
                "Total fine should be 0.0 when no loans exist in system");

        // Verify loans list is empty
        assertEquals(0, library.getLoans().size(), "Loans list should be empty");
    }

    /**
     * CC-02: Borrower has no loans (but other borrowers have loans)
     *
     * Coverage:
     * - Statement: Covers loop entry, borrower check condition (false path)
     * - Branch: Loop entry = True, Borrower match = False
     * - Condition: i < loans.size() = True, l.getBorrower() == borrower = False
     *
     * Expected: totalFine = 0.0
     */
    @Test
    @DisplayName("CC-02: Borrower has no loans - other borrowers have loans")
    public void testComputeFine2_BorrowerHasNoLoans() {
        // Add 3 loans for OTHER borrowers (borrower2 and borrower3), not borrower1

        // Loan 1: borrower2, 10 days ago, not returned (5 days overdue → 100.0 fine)
        Date issueDate1 = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        Loan loan1 = new Loan(borrower2, book1, clerk, null, issueDate1, null, false);
        library.addLoan(loan1);

        // Loan 2: borrower3, 8 days ago, returned 2 days ago (1 day overdue → 20.0 fine)
        Date issueDate2 = new Date(System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000));
        Date returnDate2 = new Date(System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000));
        Loan loan2 = new Loan(borrower3, book2, clerk, clerk, issueDate2, returnDate2, false);
        library.addLoan(loan2);

        // Loan 3: borrower2 again, 6 days ago, returned yesterday (no overdue → 0.0 fine)
        Date issueDate3 = new Date(System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000));
        Date returnDate3 = new Date(System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000));
        Loan loan3 = new Loan(borrower2, book3, clerk, clerk, issueDate3, returnDate3, false);
        library.addLoan(loan3);

        // Compute fine for borrower1 who has NO loans
        double totalFine = library.computeFine2(borrower1);

        assertEquals(0.0, totalFine, 0.01,
                "Total fine should be 0.0 when borrower has no loans");

        // Verify loans exist but none belong to borrower1
        assertEquals(3, library.getLoans().size(), "Should have 3 loans in system");
        assertTrue(library.getLoans().stream().noneMatch(l -> l.getBorrower() == borrower1),
                "None of the loans should belong to borrower1");
    }

    /**
     * CC-03: Borrower has one loan with fine
     *
     * Coverage:
     * - Statement: Covers borrower match (true), fine calculation, print statements
     * - Branch: Loop entry = True, Borrower match = True
     * - Condition: Both conditions = True at different iterations
     *
     * Expected: totalFine = 40.0 (2 days overdue × 20.0)
     */
    @Test
    @DisplayName("CC-03: Borrower has one loan with fine - 2 days overdue")
    public void testComputeFine2_OneLoanWithFine() {
        // Add 3 loans: 1 for borrower1 (target), 2 for others

        // Loan 1: borrower2, no fine
        Date issueDate1 = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));
        Date returnDate1 = new Date();
        Loan loan1 = new Loan(borrower2, book1, clerk, clerk, issueDate1, returnDate1, false);
        library.addLoan(loan1);

        // Loan 2: borrower1 (TARGET), 7 days ago, returned today (2 days overdue → 40.0)
        Date issueDate2 = new Date(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000));
        Date returnDate2 = new Date();
        Loan loan2 = new Loan(borrower1, book2, clerk, clerk, issueDate2, returnDate2, false);
        library.addLoan(loan2);

        // Loan 3: borrower3, no fine
        Date issueDate3 = new Date(System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000));
        Date returnDate3 = new Date();
        Loan loan3 = new Loan(borrower3, book3, clerk, clerk, issueDate3, returnDate3, false);
        library.addLoan(loan3);

        // Compute fine for borrower1
        double totalFine = library.computeFine2(borrower1);

        assertEquals(40.0, totalFine, 0.01,
                "Total fine should be 40.0 (2 days overdue × 20.0)");
    }

    /**
     * CC-04: Borrower has multiple loans with various fines
     *
     * Coverage:
     * - Statement: All statements covered with multiple iterations
     * - Branch: Both loop entry and both borrower match branches
     * - Condition: Multiple true/false combinations
     * - MC/DC: Shows independence of both conditions
     *
     * Expected: totalFine = 80.0 (20.0 + 60.0 + 0.0)
     */
    @Test
    @DisplayName("CC-04: Borrower has multiple loans - test accumulation")
    public void testComputeFine2_MultipleLoans() {
        // Add 5 loans: 3 for borrower1 (target), 2 for others

        // Loan 1: borrower1 - 6 days ago, returned today (1 day overdue → 20.0)
        Date issueDate1 = new Date(System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000));
        Date returnDate1 = new Date();
        Loan loan1 = new Loan(borrower1, book1, clerk, clerk, issueDate1, returnDate1, false);
        library.addLoan(loan1);

        // Loan 2: borrower2 - should be skipped
        Date issueDate2 = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        Date returnDate2 = new Date();
        Loan loan2 = new Loan(borrower2, book2, clerk, clerk, issueDate2, returnDate2, false);
        library.addLoan(loan2);

        // Loan 3: borrower1 - 8 days ago, returned today (3 days overdue → 60.0)
        Date issueDate3 = new Date(System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000));
        Date returnDate3 = new Date();
        Loan loan3 = new Loan(borrower1, book3, clerk, clerk, issueDate3, returnDate3, false);
        library.addLoan(loan3);

        // Loan 4: borrower1 - 4 days ago, returned today (no overdue → 0.0)
        Date issueDate4 = new Date(System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000));
        Date returnDate4 = new Date();
        Loan loan4 = new Loan(borrower1, book4, clerk, clerk, issueDate4, returnDate4, false);
        library.addLoan(loan4);

        // Loan 5: borrower3 - should be skipped
        Date issueDate5 = new Date(System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000));
        Date returnDate5 = new Date();
        Loan loan5 = new Loan(borrower3, book5, clerk, clerk, issueDate5, returnDate5, false);
        library.addLoan(loan5);

        // Compute fine for borrower1
        double totalFine = library.computeFine2(borrower1);

        assertEquals(80.0, totalFine, 0.01,
                "Total fine should be 80.0 (20.0 + 60.0 + 0.0)");

        // Verify correct number of loans processed
        long borrower1LoansCount = library.getLoans().stream()
                .filter(l -> l.getBorrower() == borrower1)
                .count();
        assertEquals(3, borrower1LoansCount, "Borrower1 should have exactly 3 loans");
    }

    /**
     * Additional Test: Borrower with fine already paid
     * Tests that computeFine1() returns 0 when finePaid = true
     */
    @Test
    @DisplayName("CC-Extra: Loan with fine already paid - should contribute 0.0")
    public void testComputeFine2_FineAlreadyPaid() {
        // Loan for borrower1: 10 days overdue but fine already paid
        Date issueDate = new Date(System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();
        Loan loan = new Loan(borrower1, book1, clerk, clerk, issueDate, returnDate, true);
        library.addLoan(loan);

        double totalFine = library.computeFine2(borrower1);

        assertEquals(0.0, totalFine, 0.01,
                "Total fine should be 0.0 when fine is already paid");
    }

    /**
     * Additional Test: Mix of paid and unpaid fines
     */
    @Test
    @DisplayName("CC-Extra: Mix of paid and unpaid fines - only count unpaid")
    public void testComputeFine2_MixedPaidUnpaid() {
        // Loan 1: borrower1, overdue, NOT paid (60.0)
        Date issueDate1 = new Date(System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000));
        Date returnDate1 = new Date();
        Loan loan1 = new Loan(borrower1, book1, clerk, clerk, issueDate1, returnDate1, false);
        library.addLoan(loan1);

        // Loan 2: borrower1, overdue, PAID (should be 0.0)
        Date issueDate2 = new Date(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        Date returnDate2 = new Date();
        Loan loan2 = new Loan(borrower1, book2, clerk, clerk, issueDate2, returnDate2, true);
        library.addLoan(loan2);

        // Loan 3: borrower1, overdue, NOT paid (40.0)
        Date issueDate3 = new Date(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000));
        Date returnDate3 = new Date();
        Loan loan3 = new Loan(borrower1, book3, clerk, clerk, issueDate3, returnDate3, false);
        library.addLoan(loan3);

        double totalFine = library.computeFine2(borrower1);

        // Should be 60.0 + 0.0 + 40.0 = 100.0
        assertEquals(100.0, totalFine, 0.01,
                "Should only count unpaid fines: 60.0 + 40.0 = 100.0");
    }

    /**
     * MC/DC Test: Demonstrates independence of loop entry condition
     * Compares CC-01 (loop false) vs CC-02 (loop true)
     */
    @Test
    @DisplayName("MC/DC-01: Loop entry condition independence test")
    public void testMCDC_LoopEntryIndependence() {
        // First scenario: No loans (condition false)
        double fineNoLoans = library.computeFine2(borrower1);
        assertEquals(0.0, fineNoLoans, 0.01, "No loans should give 0.0");

        // Add loans (condition becomes true)
        Date issueDate = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();
        Loan loan = new Loan(borrower2, book1, clerk, clerk, issueDate, returnDate, false);
        library.addLoan(loan);

        // Second scenario: Loans exist (condition true)
        double fineWithLoans = library.computeFine2(borrower1);

        // Outcome should still be 0.0 (borrower1 has no loans)
        // But loop was entered this time
        assertEquals(0.0, fineWithLoans, 0.01,
                "Loop entered but no matching borrower - still 0.0");

        // The key: changing ONLY i < loans.size() from false to true
        // changed whether loop body executed (MC/DC independence proven)
    }

    /**
     * MC/DC Test: Demonstrates independence of borrower match condition
     * Compares CC-02 (match false) vs CC-03 (match true)
     */
    @Test
    @DisplayName("MC/DC-02: Borrower match condition independence test")
    public void testMCDC_BorrowerMatchIndependence() {
        // Add one loan for borrower2
        Date issueDate = new Date(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000));
        Date returnDate = new Date();
        Loan loan = new Loan(borrower2, book1, clerk, clerk, issueDate, returnDate, false);
        library.addLoan(loan);

        // Scenario 1: Check borrower1 (match = false)
        double fineBorrower1 = library.computeFine2(borrower1);
        assertEquals(0.0, fineBorrower1, 0.01, "Borrower1 has no loans - 0.0");

        // Scenario 2: Check borrower2 (match = true)
        double fineBorrower2 = library.computeFine2(borrower2);
        assertEquals(40.0, fineBorrower2, 0.01, "Borrower2 has overdue loan - 40.0");

        // The key: Only the borrower match condition changed
        // Outcome changed from 0.0 to 40.0 (MC/DC independence proven)
    }
}
