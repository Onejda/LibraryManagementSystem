package LMS;

import java.sql.SQLException;

import static LMS.Library.librarian;
import static LMS.Library.persons;

/**
 * Librarian class representing the library's librarian.
 * Extends Staff with office number information.
 * Only one librarian is allowed per library (enforced by addLibrarian method).
 */
public class Librarian extends Staff {

    public int officeNo;
    public static int currentOfficeNumber = 0;

    public Librarian(int id, String n, String a, int p, double s, int of) {
        super(id, n, a, p, s);

        if (of == -1) {
            currentOfficeNumber++;
            officeNo = currentOfficeNumber;
        } else {
            officeNo = of;
            if (of > currentOfficeNumber) {
                currentOfficeNumber = of;
            }
        }
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("Office Number: " + officeNo);
    }

    public int getOfficeNo() {
        return officeNo;
    }

    /**
     * Adds a librarian to the library system.
     * Only one librarian is allowed per library.
     * @param lib The librarian to add
     * @return true if successfully added, false if a librarian already exists
     */
    public static boolean addLibrarian(Librarian lib) {
        if (librarian == null) {
            librarian = lib;
            persons.add(librarian);
            return true;
        } else {
            System.out.println("\nSorry, the library already has one librarian. New Librarian can't be created.");
            return false;
        }
    }

    /**
     * Saves this librarian to the database
     */
    public void saveToDatabase() {
        DatabaseManager db = DatabaseManager.getInstance();

        try {
            db.insertPersonWithId(id, name, password, address, phoneNo);
            db.insertStaff(id, "Librarian", salary);
            db.insertLibrarian(id, officeNo);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save librarian to database", e);
        }
    }

}