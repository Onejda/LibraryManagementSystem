package LMS;

import java.io.*;
import java.util.*;

/**
 * Borrower class representing library members who can borrow books.
 * Extends Person and maintains lists of borrowed books and hold requests.
 */
public class Borrower extends Person {

    private ArrayList<Loan> borrowedBooks;
    private ArrayList<HoldRequest> onHoldBooks;

    public Borrower(int id, String name, String address, int phoneNum) {
        super(id, name, address, phoneNum);
        borrowedBooks = new ArrayList<>();
        onHoldBooks = new ArrayList<>();
    }

    @Override
    public void printInfo() {
        super.printInfo();
        printBorrowedBooks();
        printOnHoldBooks();
    }

    /**
     * Prints all books currently borrowed by this borrower
     */
    public void printBorrowedBooks() {
        if (!borrowedBooks.isEmpty()) {
            System.out.println("\nBorrowed Books are: ");
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("No.\t\tTitle\t\t\tAuthor\t\t\tSubject");
            System.out.println("------------------------------------------------------------------------------");

            for (int i = 0; i < borrowedBooks.size(); i++) {
                System.out.print(i + "-" + "\t\t");
                borrowedBooks.get(i).getBook().printInfo();
                System.out.print("\n");
            }
        } else {
            System.out.println("\nNo borrowed books.");
        }
    }

    /**
     * Prints all books on hold for this borrower
     */
    public void printOnHoldBooks() {
        if (!onHoldBooks.isEmpty()) {
            System.out.println("\nOn Hold Books are: ");
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("No.\t\tTitle\t\t\tAuthor\t\t\tSubject");
            System.out.println("------------------------------------------------------------------------------");

            for (int i = 0; i < onHoldBooks.size(); i++) {
                System.out.print(i + "-" + "\t\t");
                onHoldBooks.get(i).getBook().printInfo();
                System.out.print("\n");
            }
        } else {
            System.out.println("\nNo On Hold books.");
        }
    }

    /**
     * Interactive method to update borrower's information
     */
    public void updateBorrowerInfo() throws IOException {
        String choice;
        Scanner sc = new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("\nDo you want to update " + getName() + "'s Name ? (y/n)");
        choice = sc.next();
        updateBorrowerName(choice, reader);

        System.out.println("\nDo you want to update " + getName() + "'s Address ? (y/n)");
        choice = sc.next();
        updateBorrowerAddress(choice, reader);

        System.out.println("\nDo you want to update " + getName() + "'s Phone Number ? (y/n)");
        choice = sc.next();
        updateBorrowerPhoneNumber(choice, sc);

        System.out.println("\nBorrower is successfully updated.");
    }

    private void updateBorrowerPhoneNumber(String choice, Scanner sc) {
        if (choice.equals("y")) {
            System.out.println("\nType New Phone Number: ");
            setPhone(sc.nextInt());
            System.out.println("\nThe phone number is successfully updated.");
        }
    }

    private void updateBorrowerAddress(String choice, BufferedReader reader) throws IOException {
        if (choice.equals("y")) {
            System.out.println("\nType New Address: ");
            setAddress(reader.readLine());
            System.out.println("\nThe address is successfully updated.");
        }
    }

    private void updateBorrowerName(String choice, BufferedReader reader) throws IOException {
        if (choice.equals("y")) {
            System.out.println("\nType New Name: ");
            setName(reader.readLine());
            System.out.println("\nThe name is successfully updated.");
        }
    }

    // Borrowed Books Management
    public void addBorrowedBook(Loan iBook) {
        borrowedBooks.add(iBook);
    }

    public void removeBorrowedBook(Loan iBook) {
        borrowedBooks.remove(iBook);
    }

    // Hold Request Management
    public void addHoldRequest(HoldRequest hr) {
        onHoldBooks.add(hr);
    }

    public void removeHoldRequest(HoldRequest hr) {
        onHoldBooks.remove(hr);
    }

    // Getter Methods
    public ArrayList<Loan> getBorrowedBooks() {
        return borrowedBooks;
    }

    public ArrayList<HoldRequest> getOnHoldBooks() {
        return onHoldBooks;
    }

    /**
     * Saves this borrower to the database
     */
    public void saveToDatabase() {
        DatabaseManager db = DatabaseManager.getInstance();
        db.insertPersonWithId(id, name, password, address, phoneNo);
        db.insertBorrower(id);
    }


}