package LMS;

import java.io.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Book class representing a book in the library.
 * Handles book information, hold requests, issuing, and returning.
 */
public class Book {

    private int bookID;
    private String title;
    private String subject;
    private String author;
    public boolean isIssued;
    private HoldRequestOperations holdRequestsOperations;
    static int currentIdNumber = 0;

    public Book(int id, String t, String s, String a, boolean issued) {
        currentIdNumber++;
        if (id == -1) {
            bookID = currentIdNumber;
        } else {
            bookID = id;
        }

        title = t;
        subject = s;
        author = a;
        isIssued = issued;
        holdRequestsOperations = new HoldRequestOperations();
    }

    /**
     * Prints all hold requests for this book
     */
    public void printHoldRequests() {
        if (!holdRequestsOperations.holdRequests.isEmpty()) {
            System.out.println("\nHold Requests are: ");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("No.\t\tBook's Title\t\t\tBorrower's Name\t\t\tRequest Date");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");

            for (int i = 0; i < holdRequestsOperations.holdRequests.size(); i++) {
                System.out.print(i + "-" + "\t\t");
                holdRequestsOperations.holdRequests.get(i).print();
            }
        } else {
            System.out.println("\nNo Hold Requests.");
        }
    }

    /**
     * Prints basic book information
     */
    public void printInfo() {
        System.out.println(title + "\t\t\t" + author + "\t\t\t" + subject);
    }

    /**
     * Interactive method to change book information
     */
    public void changeBookInfo() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String input;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("\nUpdate Author? (y/n)");
        input = scanner.next();
        if (input.equals("y")) {
            System.out.println("\nEnter new Author: ");
            author = reader.readLine();
        }

        System.out.println("\nUpdate Subject? (y/n)");
        input = scanner.next();
        if (input.equals("y")) {
            System.out.println("\nEnter new Subject: ");
            subject = reader.readLine();
        }

        System.out.println("\nUpdate Title? (y/n)");
        input = scanner.next();
        if (input.equals("y")) {
            System.out.println("\nEnter new Title: ");
            title = reader.readLine();
        }

        // Update in database
        DatabaseManager.getInstance().updateBook(bookID, title, author, subject);

        System.out.println("\nBook is successfully updated.");
    }

    // Getter Methods
    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getAuthor() {
        return author;
    }

    public boolean getIssuedStatus() {
        return isIssued;
    }

    public int getID() {
        return bookID;
    }

    public ArrayList<HoldRequest> getHoldRequests() {
        return holdRequestsOperations.holdRequests;
    }

    public HoldRequestOperations getHoldRequestOperations() {
        return holdRequestsOperations;
    }

    // Setter Methods
    public void setIssuedStatus(boolean s) {
        isIssued = s;
        // Update in database
        DatabaseManager.getInstance().updateBookIssuedStatus(bookID, s);
    }

    public static void setIDCount(int n) {
        currentIdNumber = n;
    }

    public static int getIDCount() {
        return currentIdNumber;
    }

    /**
     * Places a hold request on this book for a borrower
     * @param bor The borrower requesting the hold
     */
    public void placeBookOnHold(Borrower bor) {
        HoldRequest hr = new HoldRequest(bor, this, new Date());

        holdRequestsOperations.addHoldRequest(hr);
        bor.addHoldRequest(hr);

        // Save to database
        hr.saveToDatabase();

        System.out.println("\nThe book " + title + " has been successfully placed on hold by borrower " + bor.getName() + ".\n");
    }

    /**
     * Creates a hold request for this book
     * @param borrower The borrower making the request
     */
    public void makeHoldRequest(Borrower borrower) {
        boolean makeRequest = true;

        // Check if borrower already has this book
        for (int i = 0; i < borrower.getBorrowedBooks().size(); i++) {
            if (borrower.getBorrowedBooks().get(i).getBook() == this) {
                System.out.println("\n" + "You have already borrowed " + title);
                return;
            }
        }

        // Check if borrower already has a hold request for this book
        for (int i = 0; i < holdRequestsOperations.holdRequests.size(); i++) {
            if (holdRequestsOperations.holdRequests.get(i).getBorrower() == borrower) {
                makeRequest = false;
                break;
            }
        }

        if (makeRequest) {
            placeBookOnHold(borrower);
        } else {
            System.out.println("\nYou already have one hold request for this book.\n");
        }
    }

    /**
     * Services (removes) a hold request
     * @param hr The hold request to service
     */
    public void serviceHoldRequest(HoldRequest hr) {
        holdRequestsOperations.removeHoldRequest();
        hr.getBorrower().removeHoldRequest(hr);
    }

    /**
     * Issues this book to a borrower
     * @param borrower The borrower to issue the book to
     * @param staff The staff member processing the issue
     */
    public void issueBook(Borrower borrower, Staff staff) {
        // First delete expired hold requests
        Date today = new Date();
        ArrayList<HoldRequest> hRequests = holdRequestsOperations.holdRequests;

        for (int i = hRequests.size() - 1; i >= 0; i--) {
            HoldRequest hr = hRequests.get(i);
            long days = ChronoUnit.DAYS.between(hr.getRequestDate().toInstant(), today.toInstant());

            if (days > Library.getInstance().getHoldRequestExpiry()) {
                hr.getBorrower().removeHoldRequest(hr);
                holdRequestsOperations.removeSpecificHoldRequest(hr);
            }
        }

        if (isIssued) {
            System.out.println("\nThe book " + title + " is already issued.");
            System.out.println("Would you like to place the book on hold? (y/n)");

            Scanner sc = new Scanner(System.in);
            String choice = sc.next();

            if (choice.equals("y")) {
                makeHoldRequest(borrower);
            }
        } else {
            if (!holdRequestsOperations.holdRequests.isEmpty()) {
                boolean hasRequest = false;

                for (int i = 0; i < holdRequestsOperations.holdRequests.size() && !hasRequest; i++) {
                    if (holdRequestsOperations.holdRequests.get(i).getBorrower() == borrower) {
                        hasRequest = true;
                    }
                }

                if (hasRequest) {
                    if (holdRequestsOperations.holdRequests.get(0).getBorrower() == borrower) {
                        serviceHoldRequest(holdRequestsOperations.holdRequests.get(0));
                    } else {
                        System.out.println("\nSorry some other users have requested for this book earlier than you. So you have to wait until their hold requests are processed.");
                        return;
                    }
                } else {
                    System.out.println("\nSome users have already placed this book on request and you haven't, so the book can't be issued to you.");
                    System.out.println("Would you like to place the book on hold? (y/n)");

                    Scanner sc = new Scanner(System.in);
                    String choice = sc.next();

                    if (choice.equals("y")) {
                        makeHoldRequest(borrower);
                    }
                    return;
                }
            }

            // Issue the book
            setIssuedStatus(true);

            Loan iHistory = new Loan(borrower, this, staff, null, new Date(), null, false);
            iHistory.saveToDatabase();

            Library.getInstance().addLoan(iHistory);
            borrower.addBorrowedBook(iHistory);

            System.out.println("\nThe book " + title + " is successfully issued to " + borrower.getName() + ".");
            System.out.println("\nIssued by: " + staff.getName());
        }
    }

    /**
     * Returns this book from a borrower
     * @param borrower The borrower returning the book
     * @param l The loan record for this book
     * @param staff The staff member processing the return
     */
    public void returnBook(Borrower borrower, Loan l, Staff staff) {
        l.getBook().setIssuedStatus(false);
        l.setReturnedDate(new Date());
        l.setReceiver(staff);

        borrower.removeBorrowedBook(l);

        l.payFine();

        // Update database
        l.updateReturnInDatabase();

        System.out.println("\nThe book " + l.getBook().getTitle() + " is successfully returned by " + borrower.getName() + ".");
        System.out.println("\nReceived by: " + staff.getName());
    }

    /**
     * Saves this book to the database
     */
    public void saveToDatabase() {
        int newId = DatabaseManager.getInstance().insertBook(title, author, subject, isIssued);
        if (newId != -1) {
            bookID = newId;
        }
    }

    /**
     * Deletes this book from the database
     */
    public void deleteFromDatabase() {
        DatabaseManager.getInstance().deleteBook(bookID);
    }
}