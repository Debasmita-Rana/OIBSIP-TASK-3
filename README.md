ATM Interface 

This project is a console-based ATM (Automated Teller Machine) interface developed in Java. It is designed using strong Object-Oriented Principles (OOP), separating concerns across five distinct classes to simulate core banking functionalities, user authentication, and transaction logging.

Architecture and Class Structure

The application is built on a five-class model to ensure modularity and maintainability:

Transaction: Models a single financial movement (Deposit, Withdraw, Transfer). Stores type, amount, timestamp, and target account (if applicable).

Account: Represents the user's banking profile. Manages the balance, user ID, PIN, and stores the List of Transaction objects (history).

UserAuthentication: Handles the initial login process, validates user credentials, and enforces an attempt limit before locking the system. Also manages the simulated database of accounts.

ATMMachine: The core logic class. It performs all banking operations (Withdraw, Deposit, Transfer) by interacting directly with the Account object.

ATMInterface (Main Class): The entry point of the application. Manages the main menu display and controls the program flow after successful authentication.

Core Functionalities

After successful login, the user can perform the following operations:

1. Transactions History: Displays a detailed, reverse chronological list of all deposits, withdrawals, and transfers, along with the current account balance.

2. Withdraw: Allows the user to deduct funds, with validation against the current balance to prevent overdrafts.

3. Deposit: Adds funds to the user's account balance.

4. Transfer: Allows funds to be moved to another valid, existing account ID within the simulated system. The transaction is recorded on both the source and target accounts.

5. Quit: Logs the user out and shuts down the ATM simulation.
