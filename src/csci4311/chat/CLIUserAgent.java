/**
 * Reads in user input and uses Msgp protocol to process requests.
 */

package csci4311.chat;

import java.util.*;
import java.net.*;

public class CLIUserAgent implements UserAgent {
    private String userName;
    private TextMsgpClient textMsgpClient;
    private Socket clientSocket;
    private String response;

    /**
     * Ensure correct arguments are provided and begin client application.
     *
     * @param args
     * @throws Exception
     */
    public static void main( String args[]) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Please provide the correct number of arguments.");
            System.exit(0);
        }
        // Save arguments
        String userName = args[0];
        String server = args[1];
        int port = args.length == 3 ? Integer.parseInt(args[2]) : 4311 ;
        new CLIUserAgent(userName, server, port);
    }

    /**
     * Instantiate instance variables and begin thread.
     *
     * @param userName
     * @param server
     * @param port
     * @throws Exception
     */
    public CLIUserAgent (String userName, String server, int port) throws Exception {
        this.userName = userName;
        clientSocket = new Socket(server, port);
        textMsgpClient = new TextMsgpClient(this, clientSocket, userName);
        this.start();
    }

    /**
     * Continuously read, process and respond to user input.
     */
    public void start() {
        String[] parsedUserInput;
        Scanner scanner = new Scanner( System.in );
        String userInput;
        // Add user so users do not have to be part of a group to send a message
        textMsgpClient.addUser();

        while(true) {
            // Prompt the user for input
            System.out.print("\n@" + userName + " >>");
            // Read user input
            userInput = scanner.nextLine();
            // Parse user input into an array of strings
            parsedUserInput = userInput.split(" ");
            if (parsedUserInput[0].equals("join")) {
                this.join(userName, parsedUserInput[1]);
            } else if (parsedUserInput[0].equals("leave")) {
                this.leave(userName, parsedUserInput[1]);
            } else if (parsedUserInput[0].equals("groups")) {
                this.groups();
            } else if (parsedUserInput[0].equals("users")) {
                this.users(parsedUserInput[1]);
            } else if (parsedUserInput[0].equals("history")) {
                this.history(parsedUserInput[1]);
            } else if (parsedUserInput[0].equals("send")) {
                this.send(parsedUserInput);
            } else {
                continue;
            }
        }
    }

    /**
     * Request to add a user to a group.
     */
    public void join(String userName, String group) {
        response = textMsgpClient.join(userName, group);
        // If userName is part of a group
        if (response.startsWith("msgp 201"))
            System.out.println(userName + " is already a member of " + group);
        else {
            // Request for group members
            ArrayList<String> members = textMsgpClient.users(group);
            // Display group size
            System.out.println("Joined # " + group + " with " + members.size() + " current member(s)");
        }
    }

    /**
     * Request to remove a user from a group.
     */
    public void leave(String userName, String group) {
        response = textMsgpClient.leave(userName, group);
        if (response.startsWith("msgp 200")) {
            System.out.println(userName + " is no longer part of " + group);
        } else if (response.startsWith("msgp 201")) {
            System.out.println(userName + " is not a member of the group");
        } else if (response.startsWith("msgp 400")) {
            System.out.println(group + " does not exist");
        }
    }

    /**
     * Request a list of existing groups.
     */
    public void groups() {
        ArrayList<String> groups = textMsgpClient.groups();
        ArrayList<String> members;
        if (groups != null) {
            for (String group: groups) {
                members = textMsgpClient.users(group);
                System.out.println("#" + group + " has " + members.size() + " members");
            }
        }
    }

    /**
     * Request to get users of a group.
     */
    public void users(String group) {
        List<String> members = textMsgpClient.users(group);
        if (members != null) {
            for (String userName: members) {
                System.out.println("@" + userName);
            }
        }
    }

    /**
     * Request for group history and print results.
     */
    public void history(String group) {
        ArrayList<Message> history = textMsgpClient.history(group);
        for (Message message : history) {
            System.out.println("[" + message.getFrom() + "] " + message.getMessage());
        }
    }

    /**
     * Request to send message.
     */
    public void send(String[] parsedUserInput) {
        response = textMsgpClient.send(parsedUserInput, userName);
        if (response.startsWith("msgp 400")) {
            System.out.println("One or more recipients do not exist.");
        }
    }

    /**
     * Used by TextMsgpClient to deliver message to client.
     *
     * @param message
     */
    public void deliverMessage(Message message) {
        System.out.println("[" + message.getFrom() + "] " + message.getMessage());
        System.out.print("\n@" + userName + " >>");
    }

    /**
     * Used by TextMsgpClient to deliver error to client.
     *
     * @param err
     */
    public void deliverError(String err) {
        System.out.println(err);
    }
}