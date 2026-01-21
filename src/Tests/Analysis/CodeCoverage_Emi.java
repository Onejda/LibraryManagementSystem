/* ************************************************************************** */
/*                                                                            */
/*                                                        :::      ::::::::   */
/*   CodeCoverage_Emi.java                              :+:      :+:    :+:   */
/*                                                    +:+ +:+         +:+     */
/*   By: emi.baloshi <emi.baloshi@learner.42.tec    +#+  +:+       +#+        */
/*                                                +#+#+#+#+#+   +#+           */
/*   Created: 2026/01/21 17:03:11 by emi.baloshi       #+#    #+#             */
/*   Updated: 2026/01/21 17:18:21 by emi.baloshi      ###   ########.fr       */
/*                                                                            */
/* ************************************************************************** */

package Tests.Analysis;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Code Coverage & MC/DC Tests for testable login() method
 *
 * Coverage:
 * - Statement Coverage: 100%
 * - Branch Coverage: 100%
 * - Condition Coverage: 100%
 * - MC/DC Coverage: 100%
 *
 * Author: Denisa
 */
public class CodeCoverage_Emi {

    private LibraryTestable library;
    private Librarian librarian;
    private Borrower borrower;

    @BeforeEach
    void setUp() {

        library = new LibraryTestable();

        // Reset static Library state
        Library.librarian = null;
        Library.persons.clear();

        librarian = new Librarian(
                1001,
                "Librarian",
                "Office",
                123456789,
                2500.0,
                -1
        );

        borrower = new Borrower(
                2001,
                "Borrower",
                "Address",
                987654321
        );

        Librarian.addLibrarian(librarian);
        Library.persons.add(borrower);
    }

    /* =========================
       STATEMENT COVERAGE
       ========================= */

    // SC-01: Valid librarian ID & password
    @Test
    void testLogin_ValidLibrarian() {
        Person result = library.login("1001", "1001");
        assertNotNull(result);
        assertTrue(result instanceof Librarian);
    }

    // SC-02: Librarian ID with wrong password
    @Test
    void testLogin_LibrarianWrongPassword() {
        Person result = library.login("1001", "wrong");
        assertNull(result);
    }

    // SC-03: Valid borrower ID & password
    @Test
    void testLogin_ValidBorrower() {
        Person result = library.login("2001", "2001");
        assertNotNull(result);
        assertTrue(result instanceof Borrower);
    }

    // SC-04: Borrower ID with wrong password
    @Test
    void testLogin_BorrowerWrongPassword() {
        Person result = library.login("2001", "wrong");
        assertNull(result);
    }

    // SC-05: Non-numeric ID
    @Test
    void testLogin_NonNumericID() {
        Person result = library.login("ABC", "any");
        assertNull(result);
    }

    /* =========================
       MC/DC – Decision 1
       librarian != null && librarian.getID() == id
       ========================= */

    // MC-01: True && True → TRUE
    @Test
    void testMCDC_Librarian_TT() {
        assertNotNull(library.login("1001", "1001"));
    }

    // MC-02: True && False → FALSE
    @Test
    void testMCDC_Librarian_TF() {
        assertNull(library.login("9999", "1001"));
    }

    // MC-03: False && True → FALSE
    @Test
    void testMCDC_Librarian_FT() {
        Library.librarian = null;
        assertNull(library.login("1001", "1001"));
    }

    /* =========================
       MC/DC – Decision 2
       p.getID() == id && password matches borrower
       ========================= */

    // MC-04: True && True → TRUE
    @Test
    void testMCDC_Borrower_TT() {
        assertNotNull(library.login("2001", "2001"));
    }

    // MC-05: True && False → FALSE
    @Test
    void testMCDC_Borrower_TF() {
        assertNull(library.login("2001", "wrong"));
    }

    // MC-06: False && True → FALSE
    @Test
    void testMCDC_Borrower_FT() {
        assertNull(library.login("9999", "2001"));
    }
}
