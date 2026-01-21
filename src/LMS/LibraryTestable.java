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
     * @param choice The search type: "1" for title, "2" for subject, "3" for author
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
}