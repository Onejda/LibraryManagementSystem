package LMS;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseManager {

    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:database/library.db";
    private Connection conn;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /* ==================== CONNECTION ==================== */

    public Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            createTables();

            if (isDatabaseEmpty()) {
                seedDatabase();
            }

            System.out.println("Connected to SQLite database.");
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }


    public void closeConnection() {
        try {
            if (conn != null) conn.close();
            System.out.println("Database connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ==================== TABLE CREATION ==================== */

    public void createTables() {
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Person (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    password TEXT NOT NULL,
                    address TEXT,
                    phoneNo INTEGER,
                    type TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Staff (
                    personId INTEGER PRIMARY KEY,
                    salary REAL,
                    FOREIGN KEY (personId) REFERENCES Person(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Librarian (
                    personId INTEGER PRIMARY KEY,
                    officeNo INTEGER,
                    FOREIGN KEY (personId) REFERENCES Person(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Clerk (
                    personId INTEGER PRIMARY KEY,
                    deskNo INTEGER,
                    FOREIGN KEY (personId) REFERENCES Person(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Book (
                    id INTEGER PRIMARY KEY,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    subject TEXT,
                    isIssued INTEGER DEFAULT 0
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Loan (
                    id INTEGER PRIMARY KEY,
                    borrowerId INTEGER,
                    bookId INTEGER,
                    issuerId INTEGER,
                    issueDate INTEGER,
                    receiverId INTEGER,
                    returnDate INTEGER,
                    finePaid INTEGER DEFAULT 0
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS HoldRequest (
                    id INTEGER PRIMARY KEY,
                    bookId INTEGER,
                    borrowerId INTEGER,
                    requestDate INTEGER
                )
            """);

            System.out.println("Database tables ready.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables", e);
        }
    }

    /* ==================== DATABASE CHECK ==================== */

    public boolean isDatabaseEmpty() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Person")) {

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check database state", e);
        }
    }


    /* ==================== SEED DATA ==================== */

    public void seedDatabase() {
        try {
            /* ==================== PERSON ==================== */

            // Librarian (Admin)
            insertPersonWithId(1, "Admin", "1", "Library Office", 5550000, "Librarian");
            insertStaff(1, "Librarian", 50000);
            insertLibrarian(1, 101);

            // Clerks
            insertPersonWithId(2, "Jane Doe", "2", "Front Desk", 5552345, "Clerk");
            insertStaff(2, "Clerk", 25000);
            insertClerk(2, 1);

            insertPersonWithId(3, "Mike Johnson", "3", "Help Desk", 5553456, "Clerk");
            insertStaff(3, "Clerk", 25000);
            insertClerk(3, 2);

            // Borrowers
            insertPersonWithId(4, "Alice Brown", "4", "123 Student Ave", 5554567, "Borrower");
            insertPersonWithId(5, "Bob Wilson", "5", "456 College St", 5555678, "Borrower");

            /* ==================== BOOK ==================== */

            int book1 = insertBook("Clean Code", "Robert C. Martin", "Software Engineering", true);
            int book2 = insertBook("Design Patterns", "Gang of Four", "Software Engineering", false);
            int book3 = insertBook("Database Systems", "Ramez Elmasri", "Databases", true);

            /* ==================== LOAN ==================== */
            /* Dates stored as INTEGER (milliseconds since epoch) */

            PreparedStatement loanStmt = conn.prepareStatement(
                    """
                    INSERT INTO Loan (
                        borrowerId,
                        bookId,
                        issuerId,
                        issueDate,
                        receiverId,
                        returnDate,
                        finePaid
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """
            );

            /* ---------- Active Loan (NOT returned) ---------- */
            // 2026-01-10 as timestamp (8 days ago from 2026-01-18)
            long issueDateLoan1 = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000);

            loanStmt.setInt(1, 4);                 // borrowerId
            loanStmt.setInt(2, book1);             // bookId
            loanStmt.setInt(3, 2);                 // issuerId (clerk)
            loanStmt.setLong(4, issueDateLoan1);   // issueDate (INTEGER)
            loanStmt.setNull(5, java.sql.Types.INTEGER); // receiverId
            loanStmt.setNull(6, java.sql.Types.INTEGER); // returnDate
            loanStmt.setInt(7, 0);                 // finePaid = false
            loanStmt.executeUpdate();

            /* ---------- Returned Loan ---------- */
            // 2026-01-05 as timestamp (13 days ago from 2026-01-18)
            // 2026-01-12 as timestamp (6 days ago from 2026-01-18)
            long issueDateLoan2 = System.currentTimeMillis() - (13L * 24 * 60 * 60 * 1000);
            long returnDateLoan2 = System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000);

            loanStmt.setInt(1, 5);                 // borrowerId
            loanStmt.setInt(2, book3);             // bookId
            loanStmt.setInt(3, 3);                 // issuerId
            loanStmt.setLong(4, issueDateLoan2);   // issueDate
            loanStmt.setInt(5, 2);                 // receiverId
            loanStmt.setLong(6, returnDateLoan2);  // returnDate
            loanStmt.setInt(7, 1);                 // finePaid = true
            loanStmt.executeUpdate();


            /* ==================== HOLD REQUEST ==================== */

            PreparedStatement holdStmt = conn.prepareStatement(
                    """
                    INSERT INTO HoldRequest (bookId, borrowerId, requestDate)
                    VALUES (?, ?, ?)
                    """
            );

            // 2026-01-14 as timestamp (4 days ago from 2026-01-18)
            long requestDate = System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000);

            holdStmt.setInt(1, book2);
            holdStmt.setInt(2, 5);
            holdStmt.setLong(3, requestDate);
            holdStmt.executeUpdate();

            System.out.println("Database seeded successfully (ALL tables filled).");

        } catch (Exception e) {
            System.out.println("Seeding failed:");
            e.printStackTrace();
        }
    }



    /* ==================== PERSON ==================== */

    public int insertPerson(String name, String password, String address, int phoneNo, String type) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Person(name, password, address, phoneNo, type) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, password);
            ps.setString(3, address);
            ps.setInt(4, phoneNo);
            ps.setString(5, type);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /* ==================== STAFF ==================== */

    public void insertLibrarian(int personId, int officeNo) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO Librarian VALUES (?, ?)");
        ps.setInt(1, personId);
        ps.setInt(2, officeNo);
        ps.executeUpdate();
    }

    public void insertClerk(int personId, int deskNo) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO Clerk VALUES (?, ?)")) {

            ps.setInt(1, personId);
            ps.setInt(2, deskNo);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert clerk", e);
        }
    }


    /* ==================== BOOK ==================== */

    public int insertBook(String title, String author, String subject, boolean isIssued) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Book(title, author, subject, isIssued) VALUES (?, ?, ?, ?)")) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, subject);
            ps.setInt(4, isIssued ? 1 : 0);
            ps.executeUpdate();
            return getLastId("Book");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ==================== LOAN ==================== */

    public int insertLoan(int borrowerId, int bookId, int issuerId, Date issueDate) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Loan(borrowerId, bookId, issuerId, issueDate) VALUES (?, ?, ?, ?)")) {

            ps.setInt(1, borrowerId);
            ps.setInt(2, bookId);
            ps.setInt(3, issuerId);
            ps.setLong(4, issueDate.getTime());
            ps.executeUpdate();
            return getLastId("Loan");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ==================== UTIL ==================== */

    private int getLastId(String table) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public void updatePerson(int id, String name, String address, int phoneNo) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Person SET name = ?, address = ?, phoneNo = ? WHERE id = ?")) {

            ps.setString(1, name);
            ps.setString(2, address);
            ps.setInt(3, phoneNo);
            ps.setInt(4, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update person", e);
        }
    }

    public int insertHoldRequest(int bookId, int borrowerId, Date requestDate) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO HoldRequest(bookId, borrowerId, requestDate) VALUES (?, ?, ?)")) {

            ps.setInt(1, bookId);
            ps.setInt(2, borrowerId);
            ps.setLong(3, requestDate.getTime());

            ps.executeUpdate();
            return getLastId("HoldRequest");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert hold request", e);
        }
    }

    public void deleteHoldRequest(int bookId, int borrowerId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM HoldRequest WHERE bookId = ? AND borrowerId = ?")) {

            ps.setInt(1, bookId);
            ps.setInt(2, borrowerId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete hold request", e);
        }
    }


    public void insertBorrower(int borrowerId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Person SET type = 'Borrower' WHERE id = ?")) {

            ps.setInt(1, borrowerId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert borrower", e);
        }
    }

    private void insertPersonWithId(
            int id,
            String name,
            String password,
            String address,
            int phoneNo,
            String type
    ) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO Person VALUES (?, ?, ?, ?, ?, ?)")) {

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, password);
            ps.setString(4, address);
            ps.setInt(5, phoneNo);
            ps.setString(6, type);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert person", e);
        }
    }


    public void insertPersonWithId(
            int id,
            String name,
            String password,
            String address,
            int phoneNo
    ) {
        insertPersonWithId(id, name, password, address, phoneNo, "Borrower");
    }

    public void updateBook(int bookId, String title, String author, String subject) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Book SET title = ?, author = ?, subject = ? WHERE id = ?")) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, subject);
            ps.setInt(4, bookId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update book", e);
        }
    }

    public void updateBookIssuedStatus(int bookId, boolean isIssued) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Book SET isIssued = ? WHERE id = ?")) {

            ps.setInt(1, isIssued ? 1 : 0);
            ps.setInt(2, bookId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update book issued status", e);
        }
    }

    public void deleteBook(int bookId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Book WHERE id = ?")) {

            ps.setInt(1, bookId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete book", e);
        }
    }

    public void updateLoanIssueDate(int bookId, int borrowerId, Date newIssueDate) {
        try (PreparedStatement ps = conn.prepareStatement(
                """
                UPDATE Loan
                SET issueDate = ?
                WHERE bookId = ? AND borrowerId = ? AND receiverId IS NULL
                """
        )) {
            ps.setLong(1, newIssueDate.getTime());
            ps.setInt(2, bookId);
            ps.setInt(3, borrowerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update loan issue date", e);
        }
    }

    public void insertBorrowedBook(int bookId, int borrowerId) {
        // Borrowed books are tracked via Loan table
        // No action needed here
    }

    public int getLoanIdForActiveBook(int bookId, int borrowerId) {
        try (PreparedStatement ps = conn.prepareStatement(
                """
                SELECT id FROM Loan
                WHERE bookId = ? AND borrowerId = ? AND receiverId IS NULL
                """
        )) {
            ps.setInt(1, bookId);
            ps.setInt(2, borrowerId);

            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get active loan", e);
        }
    }

    public void updateLoanReturn(
            int loanId,
            int receiverId,
            Date returnDate,
            boolean finePaid
    ) {
        try (PreparedStatement ps = conn.prepareStatement(
                """
                UPDATE Loan
                SET receiverId = ?, returnDate = ?, finePaid = ?
                WHERE id = ?
                """
        )) {
            ps.setInt(1, receiverId);
            ps.setLong(2, returnDate.getTime());
            ps.setInt(3, finePaid ? 1 : 0);
            ps.setInt(4, loanId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update loan return", e);
        }
    }

    public void deleteBorrowedBook(int bookId) {
        // Borrowed books are handled via Loan records
        // No separate table needed
    }

    public void insertStaff(int personId, String type, double salary) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO Staff(personId, salary) VALUES (?, ?)")) {

            ps.setInt(1, personId);
            ps.setDouble(2, salary);
            ps.executeUpdate();

            // Also update the person type
            try (PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE Person SET type = ? WHERE id = ?")) {

                ps2.setString(1, type);
                ps2.setInt(2, personId);
                ps2.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert staff", e);
        }
    }

    public ArrayList<Object[]> loadAllBooks() {
        ArrayList<Object[]> list = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Book")) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("subject"),
                        rs.getInt("isIssued") == 1
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public ArrayList<Object[]> loadAllClerks() {
        ArrayList<Object[]> list = new ArrayList<>();

        String sql = """
        SELECT p.id, p.name, p.address, p.phoneNo, s.salary, c.deskNo
        FROM Person p
        JOIN Clerk c ON p.id = c.personId
        JOIN Staff s ON p.id = s.personId
    """;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getInt(4),
                        rs.getDouble(5),
                        rs.getInt(6)
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public ArrayList<Object[]> loadAllBorrowers() {
        ArrayList<Object[]> list = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, name, address, phoneNo FROM Person WHERE type='Borrower'")) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getInt(4)
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public ArrayList<Object[]> loadAllLoans() {
        ArrayList<Object[]> list = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Loan")) {

            while (rs.next()) {
                Integer receiverId = (Integer) rs.getObject("receiverId"); // keeps NULL
                Long returnDate = (Long) rs.getObject("returnDate");       // keeps NULL

                list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("borrowerId"),
                        rs.getInt("bookId"),
                        rs.getInt("issuerId"),
                        rs.getLong("issueDate"),
                        receiverId,
                        returnDate,
                        rs.getInt("finePaid") == 1
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }


    public ArrayList<Object[]> loadAllHoldRequests() {
        ArrayList<Object[]> list = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM HoldRequest ORDER BY requestDate")) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("bookId"),
                        rs.getInt("borrowerId"),
                        rs.getLong("requestDate")
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public int getMaxPersonId() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(id) FROM Person")) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Object[] loadLibrarian() {
        String sql = """
        SELECT p.id, p.name, p.password, p.address, p.phoneNo, s.salary, l.officeNo
        FROM Person p
        JOIN Librarian l ON p.id = l.personId
        JOIN Staff s ON p.id = s.personId
    """;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("address"),
                        rs.getInt("phoneNo"),
                        rs.getDouble("salary"),
                        rs.getInt("officeNo")
                };
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}