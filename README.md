# Library Management System - Database Enhanced Version

## Overview

This is an enhanced version of the Library Management System with full database support using file-based persistent storage. The system maintains all data between sessions and provides a complete library management solution.

## Features

- **Persistent Storage**: All data is automatically saved and loaded from files
- **User Management**: Support for Librarians, Clerks, and Borrowers
- **Book Management**: Add, remove, update, and search books
- **Loan System**: Issue and return books with automatic tracking
- **Hold Request System**: Queue system for requested books
- **Fine Calculation**: Automatic fine calculation for overdue books

## Project Structure

```
lms-db/
├── src/
│   └── LMS/
│       ├── Book.java              # Book entity with hold request management
│       ├── Borrower.java          # Borrower entity extending Person
│       ├── Clerk.java             # Clerk entity extending Staff
│       ├── DatabaseManager.java   # File-based database management
│       ├── HoldRequest.java       # Hold request entity
│       ├── HoldRequestOperations.java  # Hold request queue management
│       ├── Librarian.java         # Librarian entity extending Staff
│       ├── Library.java           # Main library singleton with all operations
│       ├── Loan.java              # Loan transaction entity
│       ├── Main.java              # Entry point with CLI interface
│       ├── Person.java            # Base class for all persons
│       └── Staff.java             # Base class for library staff
├── src/
│   └── Tests/                     # Unit tests (to be added)
├── database/                      # Database storage folder
├── build.sh                       # Build script
├── run.sh                         # Run script
└── README.md                      # This file
```

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- No external dependencies required

## Building the Project

### On Linux/Mac:
```bash
chmod +x build.sh
./build.sh
```

### On Windows:
```cmd
mkdir out\LMS
javac -d out src\LMS\*.java
```

### Manual Compilation:
```bash
mkdir -p out/LMS database
javac -d out src/LMS/*.java
```

## Running the Application

### On Linux/Mac:
```bash
./run.sh
```

### On Windows:
```cmd
cd out
java LMS.Main
```

### Manual Run:
```bash
cd out
java LMS.Main
```

## Database System

The system uses file-based serialization for data persistence. Data is stored in `database/library_data.ser`.

### Initial Data

On first run, the system automatically seeds the database with sample data:

**Users:**
| ID | Name | Type | Password |
|----|------|------|----------|
| 1 | John Smith | Librarian | 1 |
| 2 | Jane Doe | Clerk | 2 |
| 3 | Mike Johnson | Clerk | 3 |
| 4 | Alice Brown | Borrower | 4 |
| 5 | Bob Wilson | Borrower | 5 |
| 6 | Carol Davis | Borrower | 6 |
| 7 | David Lee | Borrower | 7 |
| 8 | Eva Martinez | Borrower | 8 |

**Books:**
| ID | Title | Author | Subject |
|----|-------|--------|---------|
| 1 | Clean Code | Robert C. Martin | Software Engineering |
| 2 | Design Patterns | Gang of Four | Software Engineering |
| 3 | Database Systems | Ramez Elmasri | Databases |
| 4 | Introduction to Algorithms | Thomas Cormen | Algorithms |
| 5 | Operating Systems | Abraham Silberschatz | Operating Systems |
| 6 | Computer Networks | Andrew Tanenbaum | Networking |
| 7 | Artificial Intelligence | Stuart Russell | AI |
| 8 | Software Testing | Ron Patton | Software Engineering |
| 9 | Java Programming | Herbert Schildt | Programming |
| 10 | Python Basics | Eric Matthes | Programming |
| 11 | Data Structures | Mark Weiss | Computer Science |
| 12 | Machine Learning | Tom Mitchell | AI |

## Default Configuration

- **Book Return Deadline**: 5 days
- **Per Day Fine**: Rs. 20
- **Hold Request Expiry**: 7 days

## Login Credentials

- **Librarian**: ID: `1`, Password: `1`
- **Clerk 1**: ID: `2`, Password: `2`
- **Clerk 2**: ID: `3`, Password: `3`
- **Borrower 1**: ID: `4`, Password: `4`
- (And so on for other borrowers)

## User Portals

### Borrower Portal
1. Search a Book
2. Place a Book on hold
3. Check Personal Info
4. Check Total Fine
5. Check Hold Requests Queue

### Clerk Portal
All Borrower features plus:
6. Check out a Book
7. Check in a Book
8. Renew a Book
9. Add a new Borrower
10. Update Borrower's Info

### Librarian Portal
All Clerk features plus:
11. Add new Book
12. Remove a Book
13. Change Book's Info
14. Check Personal Info of Clerk

### Admin Portal (Password: lib)
1. Add Clerk
2. Add Librarian
3. View Issued Books History
4. View All Books in Library

## Database Tables (Conceptual)

### PERSON
- ID (Primary Key)
- PNAME
- PASSWORD
- ADDRESS
- PHONE_NO

### BOOK
- ID (Primary Key)
- TITLE
- AUTHOR
- SUBJECT
- IS_ISSUED

### LOAN
- L_ID (Primary Key)
- BORROWER (Foreign Key)
- BOOK (Foreign Key)
- ISSUER (Foreign Key)
- ISS_DATE
- RECEIVER (Foreign Key, nullable)
- RET_DATE (nullable)
- FINE_PAID

### ON_HOLD_BOOK
- REQ_ID (Primary Key)
- BOOK (Foreign Key)
- BORROWER (Foreign Key)
- REQ_DATE

## Testing

For integration and system testing, you can:

1. **Integration Tests**: Test interactions between components (e.g., Book-Loan-Borrower)
2. **System Tests**: Test complete workflows (e.g., full book borrowing cycle)

### Test Scenarios for Integration Testing:

1. **Book Issue Integration**: Librarian/Clerk issues book to Borrower
    - Involves: Book, Borrower, Loan, Staff, DatabaseManager

2. **Book Return Integration**: Borrower returns book to Staff
    - Involves: Book, Borrower, Loan, Staff, Fine calculation

3. **Hold Request Integration**: Borrower places hold on unavailable book
    - Involves: Book, Borrower, HoldRequest, HoldRequestOperations

4. **Search and Issue Integration**: Search for book and issue it
    - Involves: Library search, Book, Loan creation

### Test Scenarios for System Testing:

1. **Complete Book Borrowing Cycle**
    - Login as Clerk → Issue Book → Login as Borrower → Check borrowed books → Return book

2. **Hold Request Workflow**
    - Issue book to Borrower A → Borrower B places hold → Return book → Issue to Borrower B

3. **Fine Calculation Workflow**
    - Issue book → Wait past deadline → Return book → Verify fine calculation

## Changes from Original Project

1. Replaced Derby database with file-based serialization
2. Added real-time persistence (all changes saved immediately)
3. Enhanced search functionality (partial matching)
4. Improved error handling
5. Added comprehensive sample data

## Troubleshooting

**Database file not loading:**
- Delete `database/library_data.ser` to reset to initial data

**Compilation errors:**
- Ensure JDK 11+ is installed
- Check that all source files are present

**Runtime errors:**
- Ensure `database` folder exists
- Check file permissions

## License

This project is for educational purposes as part of SWE 303 Software Testing course.