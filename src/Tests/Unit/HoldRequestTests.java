package Tests.Unit;

import LMS.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Unit tests for HoldRequest class
 *
 * Tests only the methods within HoldRequest:
 * - Constructor
 * - getBorrower()
 * - getBook()
 * - getRequestDate()
 */
public class HoldRequestTests {

    private Book testBook;
    private Borrower testBorrower;
    private Date testDate;
    private HoldRequest holdRequest;

    @Before
    public void setUp() {
        testBook = new Book(-1, "Test Book", "Subject", "Author", false);
        testBorrower = new Borrower(-1, "John Doe", "123 Main St", 12345);
        testDate = new Date();
        holdRequest = new HoldRequest(testBorrower, testBook, testDate);
    }

    // ==================== Constructor & Getter Tests ====================

    @Test
    public void testConstructor_InitializesAllFields() {
        // Test that constructor properly stores all three parameters
        assertNotNull(holdRequest);
        assertEquals(testBorrower, holdRequest.getBorrower());
        assertEquals(testBook, holdRequest.getBook());
        assertEquals(testDate, holdRequest.getRequestDate());
    }

    @Test
    public void testGetBorrower() {
        assertEquals(testBorrower, holdRequest.getBorrower());
    }

    @Test
    public void testGetBook() {
        assertEquals(testBook, holdRequest.getBook());
    }

    @Test
    public void testGetRequestDate() {
        assertEquals(testDate, holdRequest.getRequestDate());
    }

    // ==================== Edge Cases (Null Handling) ====================

    @Test
    public void testConstructor_WithNullBorrower() {
        HoldRequest hr = new HoldRequest(null, testBook, testDate);

        assertNull(hr.getBorrower());
        assertEquals(testBook, hr.getBook());
        assertEquals(testDate, hr.getRequestDate());
    }

    @Test
    public void testConstructor_WithNullBook() {
        HoldRequest hr = new HoldRequest(testBorrower, null, testDate);

        assertEquals(testBorrower, hr.getBorrower());
        assertNull(hr.getBook());
        assertEquals(testDate, hr.getRequestDate());
    }

    @Test
    public void testConstructor_WithNullDate() {
        HoldRequest hr = new HoldRequest(testBorrower, testBook, null);

        assertEquals(testBorrower, hr.getBorrower());
        assertEquals(testBook, hr.getBook());
        assertNull(hr.getRequestDate());
    }

    @Test
    public void testConstructor_WithAllNull() {
        HoldRequest hr = new HoldRequest(null, null, null);

        assertNull(hr.getBorrower());
        assertNull(hr.getBook());
        assertNull(hr.getRequestDate());
    }
}