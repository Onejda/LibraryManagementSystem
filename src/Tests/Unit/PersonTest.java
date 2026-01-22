package Tests.Unit;

import LMS.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PersonTests {

    // Concrete test implementation
    private static class TestPerson extends Person {
        public TestPerson(int idNum, String name, String address, int phoneNum) {
            super(idNum, name, address, phoneNum);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Person.setIDCount(0);
    }

    // ============================================================================
    // Constructor Tests
    // ============================================================================

    @Test
    @DisplayName("Constructor with -1 auto-generates ID")
    void testConstructor_autoGenerateId() {
        TestPerson person = new TestPerson(-1, "John", "Address", 12345);

        assertEquals(1, person.getID());
    }

    @Test
    @DisplayName("Constructor with explicit ID uses that ID")
    void testConstructor_explicitId() {
        TestPerson person = new TestPerson(100, "John", "Address", 12345);

        assertEquals(100, person.getID());
    }

    @Test
    @DisplayName("Constructor sets password to string of ID")
    void testConstructor_setsPasswordToId() {
        TestPerson person = new TestPerson(50, "John", "Address", 12345);

        assertEquals("50", person.getPassword());
    }

    @Test
    @DisplayName("Constructor increments static counter")
    void testConstructor_incrementsCounter() {
        new TestPerson(-1, "Person1", "Addr", 111);

        assertEquals(1, Person.getIDCount());
    }

    // ============================================================================
    // Getter Tests
    // ============================================================================

    @Test
    @DisplayName("getName returns name")
    void testGetName() {
        TestPerson person = new TestPerson(-1, "Alice", "Address", 12345);

        assertEquals("Alice", person.getName());
    }

    @Test
    @DisplayName("getAddress returns address")
    void testGetAddress() {
        TestPerson person = new TestPerson(-1, "Name", "123 Main St", 12345);

        assertEquals("123 Main St", person.getAddress());
    }

    @Test
    @DisplayName("getPhoneNumber returns phone")
    void testGetPhoneNumber() {
        TestPerson person = new TestPerson(-1, "Name", "Address", 5551234);

        assertEquals(5551234, person.getPhoneNumber());
    }

    @Test
    @DisplayName("getID returns ID")
    void testGetId() {
        TestPerson person = new TestPerson(999, "Name", "Address", 12345);

        assertEquals(999, person.getID());
    }

    @Test
    @DisplayName("getPassword returns password")
    void testGetPassword() {
        TestPerson person = new TestPerson(777, "Name", "Address", 12345);

        assertEquals("777", person.getPassword());
    }


    // ============================================================================
    // Static Methods
    // ============================================================================

    @Test
    @DisplayName("setIDCount updates counter")
    void testSetIDCount() {
        Person.setIDCount(50);

        assertEquals(50, Person.getIDCount());
    }

    @Test
    @DisplayName("getIDCount returns counter")
    void testGetIDCount() {
        Person.setIDCount(10);

        assertEquals(10, Person.getIDCount());
    }
}