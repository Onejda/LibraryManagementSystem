package LMS;

import java.util.Date;

/**
 * HoldRequest class representing a request to hold a book.
 * Contains information about the borrower, book, and request date.
 */
public class HoldRequest {

    private Borrower borrower;
    private Book book;
    private Date requestDate;

    public HoldRequest(Borrower bor, Book b, Date reqDate) {
        borrower = bor;
        book = b;
        requestDate = reqDate;
    }

    // Getter Methods
    public Borrower getBorrower() {
        return borrower;
    }

    public Book getBook() {
        return book;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    /**
     * Prints information about this hold request
     */
    public void print() {
        System.out.print(book.getTitle() + "\t\t\t\t" + borrower.getName() + "\t\t\t\t" + requestDate + "\n");
    }

    /**
     * Saves this hold request to the database
     */
    public void saveToDatabase() {
        DatabaseManager.getInstance().insertHoldRequest(book.getID(), borrower.getID(), requestDate);
    }

    /**
     * Deletes this hold request from the database
     */
    public void deleteFromDatabase() {
        DatabaseManager.getInstance().deleteHoldRequest(book.getID(), borrower.getID());
    }
}