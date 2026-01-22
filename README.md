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

## Final Project Structure

```
LibraryManagementSystem/
├── .idea/
├── .vscode/
├── database/
├── lib/
├── src/
│   ├── LMS/
│   │   ├── Book.java
│   │   ├── Borrower.java
│   │   ├── Clerk.java
│   │   ├── DatabaseManager.java
│   │   ├── HoldRequest.java
│   │   ├── HoldRequestOperations.java
│   │   ├── Librarian.java
│   │   ├── Library.java
│   │   ├── Loan.java
│   │   ├── Main.java
│   │   ├── Person.java
│   │   └── Staff.java
│   └── tests/
│       ├── Analysis/
│       ├── System/
│       ├── BookTests.java
│       ├── BorrowerTests.java
│       ├── ClerkTest.java
│       ├── HoldRequestOperationsTest.java
│       ├── HoldRequestTests.java
│       ├── IntegrationTests_DB.java
│       ├── IntegrationTests_Emi.java
│       ├── IntegrationTests_Onejda.java
│       ├── LibrarianTest.java
│       ├── LibraryTests_Denisa.java
│       ├── LibraryTests_Emi.java
│       ├── LibraryTests_Onejda.java
│       ├── LoanTest.java
│       ├── PersonTest.java
│       └── StaffTests.java
├── .gitignore
├── README.md
└── SoftwareTesting.iml
```

## Default Configuration

- **Book Return Deadline**: 5 days
- **Per Day Fine**: Rs. 20
- **Hold Request Expiry**: 7 days

## Functional Requirements by User Role

### Administrator Responsibilities
- The system shall restrict administrative access to authorized roles only (administrator/librarian).
- The system shall allow an administrator to create new staff accounts (e.g., clerks).
- The system shall generate unique credentials and store staff data persistently.
- The system shall enforce role-based access control to prevent unauthorized administrative actions.

### Librarian Responsibilities
- The system shall allow a librarian to log in and access administrative functions.
- The system shall allow a librarian to view the complete history of issued and returned books.
- The system shall allow a librarian to add new books to the library catalog.
- The system shall allow a librarian to remove books from the catalog, including handling existing hold requests safely.
- The system shall allow a librarian to update existing book information.

### Clerk Responsibilities
- The system shall allow a clerk to issue an available book to a borrower.
- The system shall require a clerk to respect FIFO priority when issuing books with hold requests.
- The system shall prevent a clerk from issuing a book that is already issued and allow placing a hold instead.
- The system shall allow a clerk to accept on-time book returns without applying fines.
- The system shall allow a clerk to process overdue returns and handle fine calculation and payment.
- The system shall allow a clerk to create new borrower accounts and generate login credentials.
- The system shall allow a clerk to update existing borrower profile information.
- The system shall allow a clerk to renew issued books and extend return deadlines.

### Borrower Responsibilities
- The system shall allow a borrower to authenticate and access the borrower portal.
- The system shall allow a borrower to search for books using different criteria.
- The system shall allow a borrower to place a hold request on an issued book.
- The system shall allow a borrower to view personal information, borrowed books, and hold requests.
- The system shall allow a borrower to view the total fine for overdue books.
- The system shall prevent a borrower from placing duplicate hold requests on the same book.
