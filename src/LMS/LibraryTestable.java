package LMS;

import java.util.*;

/**
 * Testable version of Library's searchForBooks method.
 * Refactored to accept parameters instead of reading from System.in
 * This allows for proper testing.
 */
public class LibraryTestable {

    private ArrayList<Book> booksInLibrary;

    public LibraryTestable() {
        booksInLibrary = new ArrayList<>();
    }

    public void addBookinLibrary(Book b) {
        booksInLibrary.add(b);
    }

    public ArrayList<Book> getBooks() {
        return booksInLibrary;
    }

    /**
     * Testable version of searchForBooks
     * 
     * @param choice     The search type: "1" for title, "2" for subject, "3" for
     *                   author
     * @param searchTerm The term to search for
     * @return ArrayList of matched books, or null if no matches found
     */
    public ArrayList<Book> searchForBooks(String choice, String searchTerm) {
        String title = "", subject = "", author = "";

        // Validate choice
        if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
            return null; // Invalid choice
        }

        // Set search term based on choice
        if (choice.equals("1")) {
            title = searchTerm;
        } else if (choice.equals("2")) {
            subject = searchTerm;
        } else {
            author = searchTerm;
        }

        ArrayList<Book> matchedBooks = new ArrayList<>();

        // Search through books
        for (int i = 0; i < booksInLibrary.size(); i++) {
            Book b = booksInLibrary.get(i);

            if (choice.equals("1")) {
                if (b.getTitle().toLowerCase().contains(title.toLowerCase())) {
                    matchedBooks.add(b);
                }
            } else if (choice.equals("2")) {
                if (b.getSubject().toLowerCase().contains(subject.toLowerCase())) {
                    matchedBooks.add(b);
                }
            } else {
                if (b.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                    matchedBooks.add(b);
                }
            }
        }

        // Return results
        if (!matchedBooks.isEmpty()) {
            return matchedBooks;
        } else {
            return null;
        }
    }

    /*
     * =====================================================
     * TESTABLE VERSION OF createPerson(char)
     * =====================================================
     */

    /**
     * Testable version of createPerson(char)
     *
     * @param x       role selector ('c', 'l', or default borrower)
     * @param name    person's name
     * @param address person's address
     * @param phone   phone number
     * @param salary  salary (used only for staff)
     * @return created Person object
     */
    public Person createPersonTestable(
            char x,
            String name,
            String address,
            int phone,
            double salary) {

        if (x == 'c') {
            Clerk c = new Clerk(-1, name, address, phone, salary, -1);
            return c;

        } else if (x == 'l') {
            Librarian l = new Librarian(-1, name, address, phone, salary, -1);
            return l;

        } else {
            Borrower b = new Borrower(-1, name, address, phone);
            return b;
        }
    }

       /* =====================================================
       TESTABLE VERSION OF login()
       ===================================================== */

    /**
     *
     * @param id user ID
     * @param password user password
     * @return authenticated Person or null
     */
    public Person loginTestable(int id, String password) {

        // Librarian authentication
        if (Library.librarian != null) {
            if (Library.librarian.getID() == id &&
                    Library.librarian.getPassword().equals(password)) {
                return Library.librarian;
            }
        }

        // Borrower / Staff authentication
        for (Person p : Library.persons) {
            if (p.getID() == id &&
                    p.getPassword().equals(password)) {
                return p;
            }
        }

        return null;
    }

}
