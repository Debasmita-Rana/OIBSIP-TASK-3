import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.text.SimpleDateFormat;

/**
 * 1. Transaction Class: Records details of each account activity.
 */
class Transaction {
    private String type;
    private double amount;
    private Date timestamp;
    private String targetAccount; // Used for Transfer transactions

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Transaction(String type, double amount, String targetAccount) {
        this.type = type;
        this.amount = amount;
        this.timestamp = new Date();
        this.targetAccount = targetAccount;
    }

    @Override
    public String toString() {
        String base = String.format("%s [%s]: $%.2f", DATE_FORMAT.format(timestamp), type, amount);
        if (targetAccount != null && !targetAccount.isEmpty()) {
            return base + " (Target ID: " + targetAccount + ")";
        }
        return base;
    }
}

/**
 * 2. Account Class: Holds user's state, balance, and transaction history.
 */
class Account {
    private String userId;
    private int userPin;
    private double balance;
    private List<Transaction> transactionHistory;

    public Account(String userId, int userPin, double initialBalance) {
        this.userId = userId;
        this.userPin = userPin;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }

    // Getters
    public String getUserId() { return userId; }
    public int getUserPin() { return userPin; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }

    // Mutators (Protected access via ATMMachine)
    public void deposit(double amount) {
        this.balance += amount;
        transactionHistory.add(new Transaction("Deposit", amount, null));
    }

    public void withdraw(double amount) {
        this.balance -= amount;
        transactionHistory.add(new Transaction("Withdraw", amount, null));
    }

    public void recordTransfer(String type, double amount, String targetId) {
        // Type is either "Transfer Out" or "Transfer In"
        if (type.equals("Transfer Out")) {
            this.balance -= amount;
        } else {
            this.balance += amount;
        }
        transactionHistory.add(new Transaction(type, amount, targetId));
    }
}

/**
 * 3. UserAuthentication Class: Handles initial login logic.
 */
class UserAuthentication {
    private static Account currentAccount = null;
    private static Scanner scanner = new Scanner(System.in);
    // Simulated database of accounts
    private static final List<Account> accountDatabase = new ArrayList<>();

    static {
        // Initialize dummy accounts
        accountDatabase.add(new Account("12345", 1111, 5000.00));
        accountDatabase.add(new Account("67890", 2222, 1200.50));
    }

    public static Account authenticate() {
        System.out.println("\n--- ATM Login ---");
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter User ID: ");
            String userId = scanner.nextLine();
            
            int userPin = -1;
            try {
                System.out.print("Enter User PIN: ");
                userPin = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println("Invalid PIN format.");
                scanner.nextLine(); // Clear invalid input
                attempts++;
                continue;
            }

            for (Account account : accountDatabase) {
                if (account.getUserId().equals(userId) && account.getUserPin() == userPin) {
                    System.out.println("\nLogin Successful! Welcome, " + userId + ".");
                    currentAccount = account;
                    return currentAccount;
                }
            }

            attempts++;
            System.out.println("Invalid User ID or PIN. Attempts left: " + (MAX_ATTEMPTS - attempts));
        }
        System.out.println("\nMaximum attempts reached. ATM locked.");
        return null;
    }

    public static Account findAccountById(String id) {
        for (Account account : accountDatabase) {
            if (account.getUserId().equals(id)) {
                return account;
            }
        }
        return null;
    }
}

/**
 * 4. ATMMachine Class: Implements the core banking operations.
 */
class ATMMachine {
    private Account account;
    private Scanner scanner;

    public ATMMachine(Account account, Scanner scanner) {
        this.account = account;
        this.scanner = scanner;
    }

    public void showTransactionHistory() {
        System.out.println("\n--- Transaction History (Account ID: " + account.getUserId() + ") ---");
        List<Transaction> history = account.getTransactionHistory();
        
        if (history.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }

        // Displaying in reverse chronological order
        for (int i = history.size() - 1; i >= 0; i--) {
            System.out.println("  > " + history.get(i));
        }
        System.out.printf("\nCURRENT BALANCE: $%.2f\n", account.getBalance());
    }

    public void withdraw() {
        System.out.println("\n--- Withdraw Money ---");
        double amount = readAmount("Enter amount to withdraw: ");
        
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        
        if (amount > account.getBalance()) {
            System.out.println("Error: Insufficient balance. Current balance: $" + account.getBalance());
        } else {
            account.withdraw(amount);
            System.out.printf("Success: Withdrew $%.2f. New balance: $%.2f\n", amount, account.getBalance());
        }
    }

    public void deposit() {
        System.out.println("\n--- Deposit Money ---");
        double amount = readAmount("Enter amount to deposit: ");

        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return;
        }

        account.deposit(amount);
        System.out.printf("Success: Deposited $%.2f. New balance: $%.2f\n", amount, account.getBalance());
    }

    public void transfer() {
        System.out.println("\n--- Transfer Funds ---");
        
        System.out.print("Enter target Account ID: ");
        String targetId = scanner.nextLine();
        
        // Cannot transfer to self
        if (targetId.equals(account.getUserId())) {
            System.out.println("Error: Cannot transfer funds to your own account.");
            return;
        }

        Account targetAccount = UserAuthentication.findAccountById(targetId);
        
        if (targetAccount == null) {
            System.out.println("Error: Target Account ID not found.");
            return;
        }

        double amount = readAmount("Enter amount to transfer: ");
        
        if (amount <= 0) {
            System.out.println("Transfer amount must be positive.");
            return;
        }

        if (amount > account.getBalance()) {
            System.out.println("Error: Insufficient balance. Current balance: $" + account.getBalance());
        } else {
            // Perform the transfer on both accounts
            account.recordTransfer("Transfer Out", amount, targetId);
            targetAccount.recordTransfer("Transfer In", amount, account.getUserId());
            
            System.out.printf("Success: Transferred $%.2f to Account %s. New balance: $%.2f\n", 
                               amount, targetId, account.getBalance());
        }
    }
    
    // Helper method to safely read double input
    private double readAmount(String prompt) {
        System.out.print(prompt);
        try {
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
            return amount;
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a numerical amount.");
            scanner.nextLine(); // Clear invalid input
            return -1.0; 
        }
    }
}

/**
 * 5. ATMInterface Class: Main entry point and menu display.
 */
public class ATMInterface {
    
    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // --- COMMAND LINE INSTRUCTIONS FOR RUNNING ---
        // 1. Navigate to the directory containing ATMInterface.java in your CMD.
        // 2. Compile: javac ATMInterface.java
        // 3. Run: java -cp . ATMInterface
        // ---------------------------------------------
        ATMInterface app = new ATMInterface();
        app.run();
    }

    public void run() {
        Account userAccount = UserAuthentication.authenticate();

        if (userAccount == null) {
            System.out.println("System Shutdown.");
            scanner.close();
            return;
        }

        ATMMachine atmMachine = new ATMMachine(userAccount, scanner);
        boolean running = true;

        while (running) {
            displayMenu();
            System.out.print("Enter choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        atmMachine.showTransactionHistory();
                        break;
                    case 2:
                        atmMachine.withdraw();
                        break;
                    case 3:
                        atmMachine.deposit();
                        break;
                    case 4:
                        atmMachine.transfer();
                        break;
                    case 5:
                        running = false;
                        System.out.println("\nThank you for using the ATM. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numerical choice.");
                scanner.nextLine(); // Clear the invalid input
            }
            System.out.println("\n------------------------------------------------\n");
        }
        scanner.close();
    }

    private void displayMenu() {
        System.out.println("\n--- ATM Functionality ---");
        System.out.println("1. Transactions History (Check Balance)");
        System.out.println("2. Withdraw");
        System.out.println("3. Deposit");
        System.out.println("4. Transfer");
        System.out.println("5. Quit");
    }
}
