package Tests.Unit;

import LMS.Staff;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class StaffTests {

    private Staff staff;

    // =====================================================
    // Test Setup
    // =====================================================

    @BeforeEach
    void setUp() {
        staff = new Staff(1, "Jane Smith", "456 Oak Ave", 987654210, 50000.0);
    }

    // =====================================================
    // Tests for: Staff Constructor & Getters
    // =====================================================

    @Test
    @DisplayName("Test Staff Creation")
    void testStaffCreation() {
        assertNotNull(staff);
        assertEquals(1, staff.getID());
        assertEquals("Jane Smith", staff.getName());
        assertEquals("456 Oak Ave", staff.getAddress());
        assertEquals(50000.0, staff.getSalary());
    }

    @Test
    @DisplayName("Test Get Salary")
    void testGetSalary() {
        assertEquals(50000.0, staff.getSalary());
    }

    // =====================================================
    // Tests for: setSalary(double)
    // =====================================================

    @Test
    @DisplayName("Test Set Salary")
    void testSetSalary() {
        staff.setSalary(60000.0);
        assertEquals(60000.0, staff.getSalary());
    }

    @Test
    @DisplayName("Test Set Salary with Different Values")
    void testSetSalaryVariousValues() {
        staff.setSalary(0.0);
        assertEquals(0.0, staff.getSalary());

        staff.setSalary(100000.0);
        assertEquals(100000.0, staff.getSalary());

        staff.setSalary(25.50);
        assertEquals(25.50, staff.getSalary());
    }

    // ❌ Expected to FAIL – negative salary allowed
    @Test
    @DisplayName("setSalary should reject negative values")
    void testSetSalaryNegative() {
        staff.setSalary(-1000.0);
        assertTrue(staff.getSalary() >= 0);
    }

    // ❌ Expected to FAIL – unrealistic salary allowed
    @Test
    @DisplayName("setSalary should reject unrealistic values")
    void testSetSalaryUnrealistic() {
        staff.setSalary(1_000_000_000.0);
        assertTrue(staff.getSalary() <= 1_000_000);
    }

    // =====================================================
    // Tests for: printInfo()
    // =====================================================

    @Test
    @DisplayName("printInfo includes staff name and salary")
    void testPrintInfoIncludesSalary() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        staff.printInfo();

        System.setOut(System.out);
        String output = outContent.toString();

        assertTrue(output.contains("Jane Smith"));
        assertTrue(output.contains("50000.0"));
    }
}
