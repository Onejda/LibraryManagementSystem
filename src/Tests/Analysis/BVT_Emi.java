package Tests.Analysis;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Boundary Value Testing for setReturnDeadline(int)
 */
public class BVT_Emi {

    private Library library;

    @BeforeEach
    void setUp() {
        Library.resetInstance();
        library = Library.getInstance();
    }

    // TC1 – Below minimum
    @Test
    void TC1_BelowMinimum() {
        library.setReturnDeadline(0);
        assertEquals(0, library.book_return_deadline);
    }

    // TC2 – Minimum boundary
    @Test
    void TC2_MinimumBoundary() {
        library.setReturnDeadline(1);
        assertEquals(1, library.book_return_deadline);
    }

    // TC3 – Minimum + 1
    @Test
    void TC3_MinimumPlusOne() {
        library.setReturnDeadline(2);
        assertEquals(2, library.book_return_deadline);
    }

    // TC4 – Maximum - 1
    @Test
    void TC4_MaximumMinusOne() {
        library.setReturnDeadline(29);
        assertEquals(29, library.book_return_deadline);
    }

    // TC5 – Maximum boundary
    @Test
    void TC5_MaximumBoundary() {
        library.setReturnDeadline(30);
        assertEquals(30, library.book_return_deadline);
    }

    // TC6 – Above maximum
    @Test
    void TC6_AboveMaximum() {
        library.setReturnDeadline(31);
        assertEquals(31, library.book_return_deadline);
    }
}
