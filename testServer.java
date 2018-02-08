import java.io.*;
import java.net.*;
import java.util.Scanner;

class User {
    public static int totalUsers = 0;
    private String name;
    private String password;
    private String post;

    User() {
        this.name = "";
        this.password = "";
        this.post = "";
    }

    User(String userName, String userPassword) {
        this.name = userName;
        this.password = userPassword;
        this.totalUsers++;
    }

    public void setPassword(String userPassword) {
        this.password = userPassword;
    }

    public void setPost(String userPost) {
        this.post = userPost;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public String getPost() {
        return this.post;
    }
}

public class testServer {
    User[] userList;
    public static void main(String[] args) {
        new testServer();
    }

    public testServer() {
        System.out.println("Server started.");
        userList = new User[100];
        // Load file if it exists.
        try {
            init(userList);
        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        while(true) {
            try (ServerSocket serverSocket = new ServerSocket(8000);
            Socket socket = serverSocket.accept();
            PrintWriter toClient = new PrintWriter(socket.getOutputStream());
            Scanner fromClient = new Scanner(socket.getInputStream());)
            {
                // Temporary string to receive from client.
                String name;
                String password;
                String post;

                String currentLine = fromClient.nextLine();

                System.out.println("\n\nAccepted connection.\nCommand: "+currentLine);
                // Send user list to client.
                if(currentLine.equals("## Load.")) {
                    if(User.totalUsers == 0) {
                        toClient.println("## None.");
                        toClient.flush();
                    } else {
                        // Send user list.
                        for(int i = 0; i < User.totalUsers; i++) {
                            toClient.println("## User.");
                            toClient.println(userList[i].getName());
                            toClient.println(userList[i].getPassword());
                            toClient.println(userList[i].getPost());
                            System.out.println("Sent: "+userList[i].getName());
                        }
                        toClient.println("## Done.");
                        toClient.flush();
                    }
                } else if(currentLine.equals("## Save.")) {
                    // The user index in the list.
                    int index = -1;
                    while(!currentLine.equals("## Done.")) {
                        if(currentLine.equals("## User.")) {
                            name = fromClient.nextLine();
                            password = fromClient.nextLine();
                            post = fromClient.nextLine();
                            index = findUser(userList, name);
                            if(index == -1) {
                                index = User.totalUsers;
                                // Adding a user updates totalUsers.
                                userList[index] = new User(name, password);
                            } else {
                                userList[index].setPassword(password);
                            }
                            userList[index].setPost(post);
                            System.out.println("Received: "+name);
                        }
                        currentLine = fromClient.nextLine();
                    }

                    try {
                        saveFile(userList);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    System.out.println("Received an unknown command: "+currentLine);
                    System.out.println("Exiting.");
                    System.exit(1);
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private int findUser(User[] userList, String name) {
        for(int i = 0; i < User.totalUsers; i++) {
            if(userList[i].getName().equals(name))
                return i;
        }
        return -1;
    }

    private void saveFile(User[] userList) throws Exception {
        java.io.File diskFile = new java.io.File("GladE.data");
        PrintWriter fileOutput = new PrintWriter(diskFile);
        System.out.println("\nSaving to file GladE.data.");

        for(int i = 0; i < User.totalUsers; i++) {
            fileOutput.println("## User.");
            fileOutput.println(userList[i].getName());
            fileOutput.println(userList[i].getPassword());
            fileOutput.println(userList[i].getPost());
            System.out.println("Wrote user: "+userList[i].getName());
        }
        fileOutput.close();
    }
    private void init(User[] userList) throws Exception {
        java.io.File diskFile = new java.io.File("GladE.data");

        // If the file does not exist.
        if(!diskFile.exists()) {
            System.out.println("Data file does not exist.\nStarting with blank database.");
            return;
        }

        Scanner fileInput = new Scanner(diskFile);
        // Temporary string for file input.
        String name;
        String password;
        String post;
        int index;

        String currentLine;

        System.out.println("Loading data from file GladE.data.");
        // Get the data from the disk.
        while(fileInput.hasNext()) {
            currentLine = fileInput.nextLine();
            if(!currentLine.equals("## User.")) {
                System.out.println("Disk file is corrupted.\nStarting with blank list.");
                userList = new User[100];
            }
            name = fileInput.nextLine();
            password = fileInput.nextLine();
            post = fileInput.nextLine();

            index = User.totalUsers;
            // totalUsers always updates itself.
            userList[index] = new User(name, password);
            userList[index].setPost(post);

            System.out.println("Added User: "+name);
        }
    }
}
