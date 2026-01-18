package Tests;

import LMS.Clerk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClerkTest {

    @BeforeEach
    void resetDeskCounter() {
        Clerk.currentDeskNumber = 0;  // Fixed: uppercase D
    }

    @Test
    @DisplayName("CLERK-01: Auto desk assignment gives sequential desk numbers")
    void constructorAutoDesk_assignsSequentialNumbers() {
        Clerk c1 = new Clerk(-1, "Alice", "Addr", 123, 500.0, -1);
        Clerk c2 = new Clerk(-1, "Bob", "Addr", 456, 600.0, -1);

        assertEquals(1, c1.deskNo);  // First auto desk is 1
        assertEquals(2, c2.deskNo);
    }

    @Test
    @DisplayName("CLERK-02: Explicit desk number is kept as provided")
    void constructorExplicitDesk_usesProvidedNumber() {
        Clerk c = new Clerk(-1, "Clara", "Addr", 789, 700.0, 10);

        assertEquals(10, c.deskNo);
    }

    @Test
    @DisplayName("CLERK-03: getSalary returns the assigned salary value")
    void getSalary_returnsAssignedSalary() {
        Clerk c = new Clerk(-1, "Dave", "Addr", 111, 550.0, -1);

        assertEquals(550.0, c.getSalary());
    }
}