package LMS;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Library class - Singleton that manages the entire library system.
 * Handles all operations including books, persons, loans, and hold requests.
 * Integrates with SQLite database for persistent storage.
 */
public class Library {

    private String name;
    public static Librarian librarian;
    public static ArrayList<Person> persons;
    private ArrayList<Book> booksInLibrary;
    private ArrayList<Loan> loans;

    public int book_return_deadline;
    public double per_day_fine;
    public int hold_request_expiry;

    private HoldRequestOperations holdRequestsOperations;
    private DatabaseManager dbManager;

    // Singleton instance
    private static Library obj;

    public static Library getInstance() {
        if (obj == null) {
            obj = new Library();
        }
        return obj;
    }

    /**
     * Resets the singleton instance - useful for testing
     */
    public static void resetInstance() {
        obj = null;
        librarian = null;
        persons = null;
    }

    private Library() {
        name = null;
        librarian = null;
        persons = new ArrayList<>();
        booksInLibrary = new ArrayList<>();
        loans = new ArrayList<>();
        holdRequestsOperations = new HoldRequestOperations();
        dbManager = DatabaseManager.getInstance();
    }

    // Setter Methods
    public void setReturnDeadline(int deadline) {
        book_return_deadline = deadline;
    }

    public void setFine(double perDayFine) {
        per_day_fine = perDayFine;
    }

    public void setRequestExpiry(int hrExpiry) {
        hold_request_expiry = hrExpiry;
    }

    public void setName(String n) {
        name = n;
    }

    // Getter Methods
    public int getHoldRequestExpiry() {
        return hold_request_expiry;
    }

    public ArrayList<Person> getPersons() {
        return persons;
    }

    public Librarian getLibrarian() {
        return librarian;
    }

    public String getLibraryName() {
        return name;
    }

    public ArrayList<Book> getBooks() {
        return booksInLibrary;
    }

    public ArrayList<Loan> getLoans() {
        return loans;
    }

    // Adding Methods
    public void addClerk(Clerk c) {
        persons.add(c);
    }

    public void addBorrower(Borrower b) {
        persons.add(b);
    }

    public void addLoan(Loan l) {
        loans.add(l);
    }

    public void addBookinLibrary(Book b) {
        booksInLibrary.add(b);
    }

    // ==================== FIND METHODS ====================

    public Borrower findBorrower() {
        System.out.println("\nEnter Borrower's ID: ");
        int id = 0;
        Scanner scanner = new Scanner(System.in);

        try {
            id = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("\nInvalid Input");
        }

        return findBorrowerById(id);
    }

    public Borrower findBorrowerById(int id) {
        for (int i = 0; i < persons.size(); i++) {
            if (persons.get(i).getID() == id && persons.get(i) instanceof Borrower) {
                return (Borrower) persons.get(i);
            }
        }
        return null;
    }

    public Clerk findClerk() {
        System.out.println("\nEnter Clerk's ID: ");
        int id = 0;
        Scanner scanner = new Scanner(System.in);

        try {
            id = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("\nInvalid Input");
        }

        return findClerkById(id);
    }

    public Clerk findClerkById(int id) {
        for (int i = 0; i < persons.size(); i++) {
            if (persons.get(i).getID() == id && persons.get(i) instanceof Clerk) {
                return (Clerk) persons.get(i);
            }
        }
        return null;
    }

    public Staff findStaffById(int id) {
        if (librarian != null && librarian.getID() == id) {
            return librarian;
        }
        for (Person p : persons) {
            if (p.getID() == id && p instanceof Staff) {
                return (Staff) p;
            }
        }
        return null;
    }

    public Book findBookById(int id) {
        for (Book b : booksInLibrary) {
            if (b.getID() == id) {
                return b;
            }
        }
        return null;
    }

    // ==================== BOOK OPERATIONS ====================

    public void removeBookfromLibrary(Book b) {
        boolean delete = true;

        for (int i = 0; i < persons.size() && delete; i++) {
            if (persons.get(i) instanceof Borrower) {
                ArrayList<Loan> borBooks = ((Borrower) persons.get(i)).getBorrowedBooks();
                for (int j = 0; j < borBooks.size() && delete; j++) {
                    if (borBooks.get(j).getBook() == b) {
                        delete = false;
                        System.out.println("This particular book is currently borrowed by some borrower.");
                    }
                }
            }
        }

        if (delete) {
            System.out.println("\nCurrently this book is not borrowed by anyone.");
            ArrayList<HoldRequest> hRequests = b.getHoldRequests();

            if (!hRequests.isEmpty()) {
                System.out.println("\nThis book might be on hold requests by some borrowers. Deleting this book will delete the relevant hold requests too.");
                System.out.println("Do you still want to delete the book? (y/n)");

                Scanner sc = new Scanner(System.in);

                while (true) {
                    String choice = sc.next();

                    if (choice.equals("y") || choice.equals("n")) {
                        if (choice.equals("n")) {
                            System.out.println("\nDelete Unsuccessful.");
                            return;
                        } else {
                            for (int i = hRequests.size() - 1; i >= 0; i--) {
                                HoldRequest hr = hRequests.get(i);
                                hr.getBorrower().removeHoldRequest(hr);
                            }
                            break;
                        }
                    } else {
                        System.out.println("Invalid Input. Enter (y/n): ");
                    }
                }
            } else {
                System.out.println("This book has no hold requests.");
            }

            b.deleteFromDatabase();
            booksInLibrary.remove(b);
            System.out.println("The book is successfully removed.");
        } else {
            System.out.println("\nDelete Unsuccessful.");
        }
    }

    public ArrayList<Book> searchForBooks() throws IOException {
        String choice;
        String title = "", subject = "", author = "";

        Scanner sc = new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("\nEnter either '1' or '2' or '3' for search by Title, Subject or Author of Book respectively: ");
            choice = sc.next();

            if (choice.equals("1") || choice.equals("2") || choice.equals("3")) {
                break;
            } else {
                System.out.println("\nWrong Input!");
            }
        }

        if (choice.equals("1")) {
            System.out.println("\nEnter the Title of the Book: ");
            title = reader.readLine();
        } else if (choice.equals("2")) {
            System.out.println("\nEnter the Subject of the Book: ");
            subject = reader.readLine();
        } else {
            System.out.println("\nEnter the Author of the Book: ");
            author = reader.readLine();
        }

        ArrayList<Book> matchedBooks = new ArrayList<>();

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

        if (!matchedBooks.isEmpty()) {
            System.out.println("\nThese books are found: \n");
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("No.\t\tTitle\t\t\tAuthor\t\t\tSubject");
            System.out.println("------------------------------------------------------------------------------");

            for (int i = 0; i < matchedBooks.size(); i++) {
                System.out.print(i + "-" + "\t\t");
                matchedBooks.get(i).printInfo();
                System.out.print("\n");
            }

            return matchedBooks;
        } else {
            System.out.println("\nSorry. No Books were found related to your query.");
            return null;
        }
    }

    public void viewAllBooks() {
        if (!booksInLibrary.isEmpty()) {
            System.out.println("\nBooks are: ");
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("No.\t\tTitle\t\t\tAuthor\t\t\tSubject");
            System.out.println("------------------------------------------------------------------------------");

            for (int i = 0; i < booksInLibrary.size(); i++) {
                System.out.print(i + "-" + "\t\t");
                booksInLibrary.get(i).printInfo();
                System.out.print("\n");
            }
        } else {
            System.out.println("\nCurrently, Library has no books.");
        }
    }

    public double computeFine2(Borrower borrower) {
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("No.\t\tBook's Title\t\tBorrower's Name\t\t\tIssued Date\t\t\tReturned Date\t\t\t\tFine(Rs)");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        double totalFine = 0;
        double per_loan_fine = 0;

        for (int i = 0; i < loans.size(); i++) {
            Loan l = loans.get(i);

            if (l.getBorrower() == borrower) {
                per_loan_fine = l.computeFine1();
                System.out.print(i + "-" + "\t\t" + loans.get(i).getBook().getTitle() + "\t\t\t" +
                        loans.get(i).getBorrower().getName() + "\t\t" + loans.get(i).getIssuedDate() +
                        "\t\t\t" + loans.get(i).getReturnDate() + "\t\t\t\t" + per_loan_fine + "\n");

                totalFine += per_loan_fine;
            }
        }

        return totalFine;
    }

    public void createPerson(char x) {
        Scanner sc = new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("\nEnter Name: ");
        String n = "";
        try {
            n = reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Enter Address: ");
        String address = "";
        try {
            address = reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }

        int phone = 0;
        try {
            System.out.println("Enter Phone Number: ");
            phone = sc.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("\nInvalid Input.");
        }

        if (x == 'c') {
            double salary = 0;
            try {
                System.out.println("Enter Salary: ");
                salary = sc.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid Input.");
            }

            Clerk c = new Clerk(-1, n, address, phone, salary, -1);
            c.saveToDatabase();
            addClerk(c);

            System.out.println("\nClerk with name " + n + " created successfully.");
            System.out.println("\nYour ID is : " + c.getID());
            System.out.println("Your Password is : " + c.getPassword());
        } else if (x == 'l') {
            double salary = 0;
            try {
                System.out.println("Enter Salary: ");
                salary = sc.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid Input.");
            }

            Librarian l = new Librarian(-1, n, address, phone, salary, -1);
            if (Librarian.addLibrarian(l)) {
                l.saveToDatabase();
                System.out.println("\nLibrarian with name " + n + " created successfully.");
                System.out.println("\nYour ID is : " + l.getID());
                System.out.println("Your Password is : " + l.getPassword());
            }
        } else {
            Borrower b = new Borrower(-1, n, address, phone);
            b.saveToDatabase();
            addBorrower(b);
            System.out.println("\nBorrower with name " + n + " created successfully.");
            System.out.println("\nYour ID is : " + b.getID());
            System.out.println("Your Password is : " + b.getPassword());
        }
    }

    public void createBook(String title, String subject, String author) {
        Book b = new Book(-1, title, subject, author, false);
        b.saveToDatabase();
        addBookinLibrary(b);
        System.out.println("\nBook with Title " + b.getTitle() + " is successfully created.");
    }

    public Person login() {
        Scanner input = new Scanner(System.in);
        int id;
        String password;

        System.out.println("\nEnter ID: ");
        try {
            id = input.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("\nInvalid Input");
            return null;
        }

        System.out.println("Enter Password: ");
        password = input.next();

        if (librarian != null) {
            if (librarian.getID() == id &&
                    librarian.getPassword().equals(password)) {
                System.out.println("\nLogin Successful");
                return librarian;
            }
        }

        for (Person p : persons) {
            if (p.getID() == id &&
                    p.getPassword().equals(password)) {
                System.out.println("\nLogin Successful");
                return p;
            }
        }

        System.out.println("\nSorry! Wrong ID or Password");
        return null;
    }


    public void viewHistory() {
        if (!loans.isEmpty()) {
            System.out.println("\nIssued Books are: ");
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("No.\tBook's Title\tBorrower's Name\t  Issuer's Name\t\tIssued Date\t\t\tReceiver's Name\t\tReturned Date\t\tFine Paid");
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");

            for (int i = 0; i < loans.size(); i++) {
                if (loans.get(i).getIssuer() != null) {
                    System.out.print(i + "-" + "\t" + loans.get(i).getBook().getTitle() + "\t\t\t" +
                            loans.get(i).getBorrower().getName() + "\t\t" +
                            loans.get(i).getIssuer().getName() + "\t    " +
                            loans.get(i).getIssuedDate());
                }

                if (loans.get(i).getReceiver() != null) {
                    System.out.print("\t" + loans.get(i).getReceiver().getName() + "\t\t" +
                            loans.get(i).getReturnDate() + "\t   " + loans.get(i).getFineStatus() + "\n");
                } else {
                    System.out.print("\t\t" + "--" + "\t\t\t" + "--" + "\t\t" + "--" + "\n");
                }
            }
        } else {
            System.out.println("\nNo issued books.");
        }
    }

    // ==================== DATABASE OPERATIONS ====================

    public Object makeConnection() {
        Object con = dbManager.connect();
        if (con != null) {
            dbManager.createTables();
            if (dbManager.isDatabaseEmpty()) {
                dbManager.seedDatabase();
            }
        }
        return con;
    }

    public void populateLibrary(Object con) throws IOException {
        // Load Books
        ArrayList<Object[]> bookData = dbManager.loadAllBooks();
        int maxBookId = 0;
        for (Object[] data : bookData) {
            int id = (Integer) data[0];
            String title = (String) data[1];
            String author = (String) data[2];
            String subject = (String) data[3];
            boolean isIssued = (Boolean) data[4];

            Book b = new Book(id, title, subject, author, isIssued);
            addBookinLibrary(b);

            if (id > maxBookId) maxBookId = id;
        }
        Book.setIDCount(maxBookId);

        if (bookData.isEmpty()) {
            System.out.println("No Books Found in Library");
        }

        // Load Clerks
        ArrayList<Object[]> clerkData = dbManager.loadAllClerks();
        int maxDeskNo = 0;
        for (Object[] data : clerkData) {
            int id = (Integer) data[0];
            String cname = (String) data[1];
            String adrs = (String) data[2];
            int phn = (Integer) data[3];
            double sal = (Double) data[4];
            int desk = (Integer) data[5];

            Clerk c = new Clerk(id, cname, adrs, phn, sal, desk);
            addClerk(c);

            if (desk > maxDeskNo) maxDeskNo = desk;
        }
        Clerk.setDeskCount(maxDeskNo);

        if (clerkData.isEmpty()) {
            System.out.println("No clerks Found in Library");
        }

        // Load Librarian
        // Load Librarian
        Object[] libData = dbManager.loadLibrarian();
        if (libData != null) {
            int id = (Integer) libData[0];
            String lname = (String) libData[1];
            String password = (String) libData[2];
            String adrs = (String) libData[3];
            int phn = (Integer) libData[4];
            double sal = (Double) libData[5];
            int off = (Integer) libData[6];

            Librarian l = new Librarian(id, lname, adrs, phn, sal, off);
            librarian = l;
        } else {
            System.out.println("No Librarian Found in Library");
        }


        // Load Borrowers
        ArrayList<Object[]> borrowerData = dbManager.loadAllBorrowers();
        for (Object[] data : borrowerData) {
            int id = (Integer) data[0];
            String bname = (String) data[1];
            String adrs = (String) data[2];
            int phn = (Integer) data[3];

            Borrower b = new Borrower(id, bname, adrs, phn);
            addBorrower(b);
        }

        if (borrowerData.isEmpty()) {
            System.out.println("No Borrower Found in Library");
        }

        // Set Person ID Count
        int maxPersonId = dbManager.getMaxPersonId();
        Person.setIDCount(maxPersonId);

        // Load Loans
        ArrayList<Object[]> loanData = dbManager.loadAllLoans();

        for (Object[] data : loanData) {
            int loanId = (Integer) data[0];
            int borrowerId = (Integer) data[1];
            int bookId = (Integer) data[2];
            int issuerId = (Integer) data[3];
            Long issDateLong = (Long) data[4];
            Object receiverObj = data[5];
            Long retDateLong = (Long) data[6];
            boolean finePaid = (Boolean) data[7];

            Borrower borrower = findBorrowerById(borrowerId);
            Book book = findBookById(bookId);
            Staff issuer = findStaffById(issuerId);
            Staff receiver = null;
            Date issDate = null;
            Date retDate = null;

            // Convert Long timestamps to Date objects
            if (issDateLong != null && issDateLong != 0) {
                issDate = new Date(issDateLong);
            }
            if (retDateLong != null && retDateLong != 0) {
                retDate = new Date(retDateLong);
            }

            if (receiverObj != null) {
                receiver = findStaffById((Integer) receiverObj);
            }

            if (borrower != null && book != null && issuer != null) {
                Loan loan = new Loan(borrower, book, issuer, receiver, issDate, retDate, finePaid);
                loan.setLoanId(loanId);
                loans.add(loan);

                // If book is not returned, add to borrower's borrowed books
                if (receiver == null) {
                    borrower.addBorrowedBook(loan);
                }
            }
        }

        if (loanData.isEmpty()) {
            System.out.println("No Books Issued Yet!");
        }

        // Load Hold Requests
        ArrayList<Object[]> holdData = dbManager.loadAllHoldRequests();

        for (Object[] data : holdData) {
            int bookId = (Integer) data[1];
            int borrowerId = (Integer) data[2];
            Long reqDateLong = (Long) data[3];

            Borrower borrower = findBorrowerById(borrowerId);
            Book book = findBookById(bookId);
            Date reqDate = null;

            // Convert Long timestamp to Date object
            if (reqDateLong != null && reqDateLong != 0) {
                reqDate = new Date(reqDateLong);
            }

            if (borrower != null && book != null && reqDate != null) {
                HoldRequest hr = new HoldRequest(borrower, book, reqDate);
                book.getHoldRequestOperations().addHoldRequest(hr);
                borrower.addHoldRequest(hr);
            }
        }

        if (holdData.isEmpty()) {
            System.out.println("No Books on Hold Yet!");
        }
    }

    public void fillItBack(Object con) {
        // Database is updated in real-time, no need to fill back
        System.out.println("\nAll changes have been saved to database.");
    }
}