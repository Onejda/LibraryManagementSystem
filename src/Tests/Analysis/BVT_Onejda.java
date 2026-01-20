package Tests.Analysis;

import LMS.Library;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BVT_Onejda {

    @Test
    public void testBVT01_NegativeExpiry() {
        // Test Case BVT-01: Input = -1
        Library lib = Library.getInstance();
        lib.setRequestExpiry(-1);
        assertEquals(-1, lib.getHoldRequestExpiry());
        // Documented: Negative values accepted due to missing validations
    }

    @Test
    public void testBVT02_ZeroExpiry() {
        // Test Case BVT-02: Input = 0
        Library lib = Library.getInstance();
        lib.setRequestExpiry(0);
        assertEquals(0, lib.getHoldRequestExpiry());
    }

    @Test
    public void testBVT03_MinimumValid() {
        // Test Case BVT-03: Input = 1
        Library lib = Library.getInstance();
        lib.setRequestExpiry(1);
        assertEquals(1, lib.getHoldRequestExpiry());
    }

    @Test
    public void testBVT04_JustAboveMinimum() {
        // Test Case BVT-04: Input = 2
        Library lib = Library.getInstance();
        lib.setRequestExpiry(2);
        assertEquals(2, lib.getHoldRequestExpiry());
    }

    @Test
    public void testBVT05_TypicalValue() {
        // Test Case BVT-05: Input = 7
        Library lib = Library.getInstance();
        lib.setRequestExpiry(7);
        assertEquals(7, lib.getHoldRequestExpiry());
    }

    @Test
    public void testBVT06_LargePositive() {
        // Test Case BVT-06: Input = 365
        Library lib = Library.getInstance();
        lib.setRequestExpiry(365);
        assertEquals(365, lib.getHoldRequestExpiry());
    }
}
