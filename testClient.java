import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.InputMismatchException;

public class testClient {
    public static void main(String[] args) {
        User[] userList = new User[100];
        String host = "localhost";
        if(args.length != 0)
            host = args[0];

        try {
            init(userList, host);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // Print welcome.
        System.out.println("\nWelcome to GladE.");
        System.out.println("The most exclusive social media platform.");
        // Do main menu.
        int choice;
        do {
            System.out.println("\n1: Sign up as a new user.");
            System.out.println("2: Sign in as an existing user.");
            System.out.println("3: Exit GladE.");
            choice = menuChoice(3);

            // Do menu choices.
            switch(choice) {
                case(1):
                    signUp(userList);
                    break;
                case(2):
                    signIn(userList);
                    break;
            }
        } while(choice != 3);
        System.out.println("\nHave a good day.");
        try {
            finalize(userList, host);
            System.out.println("Exiting.");
        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void signUp(User[] userList) {
        Scanner keyBoard = new Scanner(System.in);
        String name;
        String password;
        String post;
        int index;

        System.out.print("\nEnter a user name: ");
        name = keyBoard.nextLine();
        System.out.print("Enter a password: ");
        password = keyBoard.nextLine();

        index = findUser(userList, name);
        // See if the user name is already taken.
        if(index != -1) {
            System.out.println("\n** Sorry, that name is taken. **");
            return;
        }
        // Else is not needed here.
        // totalUsers auto increments when you add a new user, so get the index first.
        index = User.totalUsers;
        userList[index] = new User(name, password);
        System.out.println("\n** Success. **");
        System.out.print("\nEnter a post: ");
        post = keyBoard.nextLine();
        userList[index].setPost(post);
    }

    private static void signIn(User[] userList) {
        Scanner keyBoard = new Scanner(System.in);
        String name;
        String password;
        String post;
        int userIndex;

        System.out.print("\nEnter a user name: ");
        name = keyBoard.nextLine();
        System.out.print("Enter a password: ");
        password = keyBoard.nextLine();

        userIndex = findUser(userList, name);
        if(userIndex == -1 || !userList[userIndex].getPassword().equals(password)) {
            System.out.println("\n** Sorry, wrong user name or password. **");
            return;
        }
        System.out.println("\nUser posts:");
        // Print out all the user posts.
        for(int i = 0; i < User.totalUsers; i++) {
            System.out.println(userList[i].getName()+" says "+userList[i].getPost());
        }

        // System.out.println("\nTODO:\nChange post.\nChange password.\nLog out.");
        int choice = -1;
        do {
            System.out.println("\n1: Change post.");
            System.out.println("2: Change password.");
            System.out.println("3: Print all posts.");
            System.out.println("4: Log out.");
            choice = menuChoice(4);
            switch(choice) {
                case(1):
                    System.out.print("\nEnter a new post: ");
                    post = keyBoard.nextLine();
                    userList[userIndex].setPost(post);
                    break;
                case(2):
                    System.out.print("\nEnter a new password: ");
                    password = keyBoard.nextLine();
                    userList[userIndex].setPassword(password);
                    break;
                case(3):
                    System.out.println("\nUser posts:");
                    // Print out all the user posts.
                    for(int i = 0; i < User.totalUsers; i++) {
                        System.out.println(userList[i].getName()+" says "+userList[i].getPost());
                    }
                    break;
            }
        } while(choice != 4);
        System.out.println("\nLogging out "+name);
    }

    private static int findUser(User[] userList, String name) {
        for(int i = 0; i < User.totalUsers; i++) {
            if(userList[i].getName().equals(name))
                return i;
        }
        return -1;
    }

    private static void init(User[] userList, String host) throws Exception {
        try (Socket socket = new Socket(host, 8000);
            Scanner hostScanner = new Scanner(socket.getInputStream());
            PrintWriter toHost = new PrintWriter(socket.getOutputStream());) {
                System.out.println("Connecting to "+host);
                toHost.println("## Load.");
                toHost.flush();

                String name;
                String password;
                String post;
                int index;

                String currentLine = hostScanner.nextLine();
                while(!currentLine.equals("## Done.")) {
                    if(currentLine.equals("## None.")) {
                        System.out.println("No database.\nStarting new database.");
                        userList = new User[100];
                        return;
                    }
                    if(currentLine.equals("## User.")) {
                        name = hostScanner.nextLine();
                        password = hostScanner.nextLine();
                        post = hostScanner.nextLine();
                        index = User.totalUsers;
                        userList[index] = new User(name, password);
                        // totalUsers auto increments.
                        userList[index].setPost(post);
                        System.out.println("Received: "+name);
                    }
                    currentLine = hostScanner.nextLine();
                }
                System.out.println("Ending connection to host.");
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void finalize(User[] userList, String host) throws Exception {
        try (Socket socket = new Socket(host, 8000);
        Scanner hostScanner = new Scanner(socket.getInputStream());
        PrintWriter toHost = new PrintWriter(socket.getOutputStream());) {
            if(User.totalUsers == 0) {
                // No users to save, exit.
                System.out.println("\nDatabase empty.\nNothing to send to server.");
                return;
            }
            System.out.println("\n\nConnecting to "+host);
            toHost.println("## Save.");
            toHost.flush();

            for(int i = 0; i < User.totalUsers; i++) {
                toHost.println("## User.");
                toHost.println(userList[i].getName());
                toHost.println(userList[i].getPassword());
                toHost.println(userList[i].getPost());
                System.out.println("Sent to server: "+userList[i].getName());
            }
            toHost.println("## Done.");
            toHost.flush();
            System.out.println("Ending connection to host.");
        }
    }

    private static int menuChoice(int numberOfChoices) {
        int choice = -1; // An invalide choice;

        do {
            choice = safeIntInput("Enter a choice (1-"+numberOfChoices+"): ");
        } while (choice < 1 || choice > numberOfChoices);
        return choice;
    }

    private static int safeIntInput(String prompt) {
        Scanner userInput = new Scanner(System.in);
        int input = 0;
        boolean success = false;

        while(!success) {
            System.out.print(prompt);
            try {
                input = userInput.nextInt();
                success = true;
            } catch(InputMismatchException e) {
                System.out.println("Please try again with a number.");
                userInput.nextLine(); // Flush input.
                success = false; // Try again.
            }
        }

        return input;
    }
}
