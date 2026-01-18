package LMS;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Scanner;

/**
 * Loan class representing a book loan transaction.
 * Tracks borrower, book, issuer, dates, and fine status.
 */
public class Loan {

    private int loanId;
    private Borrower borrower;
    private Book book;
    private Staff issuer;
    private Date issuedDate;
    private Date dateReturned;
    private Staff receiver;
    private boolean finePaid;

    public Loan(Borrower bor, Book b, Staff i, Staff r, Date iDate, Date rDate, boolean fPaid) {
        loanId = -1;
        borrower = bor;
        book = b;
        issuer = i;
        receiver = r;
        issuedDate = iDate;
        dateReturned = rDate;
        finePaid = fPaid;
    }

    // Getter Methods
    public int getLoanId() {
        return loanId;
    }

    public Book getBook() {
        return book;
    }

    public Staff getIssuer() {
        return issuer;
    }

    public Staff getReceiver() {
        return receiver;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public Date getReturnDate() {
        return dateReturned;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public boolean getFineStatus() {
        return finePaid;
    }

    // Setter Methods
    public void setLoanId(int id) {
        loanId = id;
    }

    public void setReturnedDate(Date dReturned) {
        dateReturned = dReturned;
    }

    public void setFineStatus(boolean fStatus) {
        finePaid = fStatus;
    }

    public void setReceiver(Staff r) {
        receiver = r;
    }

    /**
     * Computes the fine for this loan based on overdue days
     * @return The calculated fine amount
     */
    public double computeFine1() {
        double totalFine = 0;

        if (!finePaid) {
            Date iDate = issuedDate;
            Date rDate = (dateReturned != null) ? dateReturned : new Date();

            long days = ChronoUnit.DAYS.between(iDate.toInstant(), rDate.toInstant());
            days = days - Library.getInstance().book_return_deadline;

            if (days > 0) {
                totalFine = days * Library.getInstance().per_day_fine;
            } else {
                totalFine = 0;
            }
        }
        return totalFine;
    }

    /**
     * Handles fine payment for this loan
     */
    public void payFine() {
        double totalFine = computeFine1();

        if (totalFine > 0) {
            System.out.println("\nTotal Fine generated: Rs " + totalFine);
            System.out.println("Do you want to pay? (y/n)");

            Scanner input = new Scanner(System.in);
            String choice = input.next();

            if (choice.equals("y") || choice.equals("Y")) {
                finePaid = true;
            }
            if (choice.equals("n") || choice.equals("N")) {
                finePaid = false;
            }
        } else {
            System.out.println("\nNo fine is generated.");
            finePaid = true;
        }
    }

    /**
     * Renews this loan by updating the issue date
     * @param iDate The new issue date
     */
    public void renewIssuedBook(Date iDate) {
        issuedDate = iDate;

        // Update in database
        DatabaseManager.getInstance().updateLoanIssueDate(book.getID(), borrower.getID(), iDate);

        System.out.println("\nThe deadline of the book " + getBook().getTitle() + " has been extended.");
        System.out.println("Issued Book is successfully renewed!\n");
    }

    /**
     * Saves this loan to the database
     */
    public void saveToDatabase() {
        loanId = DatabaseManager.getInstance().insertLoan(
                borrower.getID(),
                book.getID(),
                issuer.getID(),
                issuedDate
        );
        // Also record in borrowed_book table
        DatabaseManager.getInstance().insertBorrowedBook(book.getID(), borrower.getID());
    }

    /**
     * Updates the return information in the database
     */
    public void updateReturnInDatabase() {
        if (loanId == -1) {
            // Try to find the loan ID
            loanId = DatabaseManager.getInstance().getLoanIdForActiveBook(book.getID(), borrower.getID());
        }
        if (loanId != -1) {
            DatabaseManager.getInstance().updateLoanReturn(loanId, receiver.getID(), dateReturned, finePaid);
            DatabaseManager.getInstance().deleteBorrowedBook(book.getID());
        }
    }
}