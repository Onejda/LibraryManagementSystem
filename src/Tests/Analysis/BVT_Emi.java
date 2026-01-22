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

        // assume a valid default deadline already exists
        library.setReturnDeadline(5);
    }

    // TC1 – Below minimum (EXPECTED TO FAIL due to no validation)
    @Test
    void TC1_BelowMinimum() {
        library.setReturnDeadline(0);
        assertNotEquals(0, library.book_return_deadline,
                "Below-minimum value should not be accepted");
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

    // TC6 – Above maximum (EXPECTED TO FAIL due to no validation)
    @Test
    void TC6_AboveMaximum() {
        library.setReturnDeadline(31);
        assertNotEquals(31, library.book_return_deadline,
                "Above-maximum value should not be accepted");
    }
}
