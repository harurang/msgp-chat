/**
 * Responsible for processing requests from msgp.
 */

package csci4311.chat;

import java.util.*;
import java.io.*;
import java.net.*;

public class ChatServer implements Runnable {

    // List of group names and their corresponding Group object
    private Hashtable<String, Group> groups;
    // List of users and their corresponding DataOutputStream
    private Hashtable<String, DataOutputStream> users;
    private ServerSocket socket;
    private ArrayList<String> recipients;

    /**
     * Set port and create ChatServer.
     *
     * @param argv
     * @throws Exception
     */
    public static void main(String argv[]) throws Exception {
        // If port is not provided default to 4311
        int port = argv.length > 0 ? Integer.parseInt(argv[0]) : 4311 ;
        new ChatServer(port);
    }

    /**
     * Instantiate instance variables and bind to port.
     * Begin program.
     *
     * @param port
     * @throws Exception
     */
    public ChatServer(int port) throws Exception {
        // Instantiate instance variables
        recipients = new ArrayList<>();
        groups = new Hashtable<>();
        users = new Hashtable<>();
        // Bind to port
        try {
            socket = new ServerSocket(port);
            run();
        } catch(IOException ioe) {
            System.out.println(ioe);
        }
    }

    /**
     * Wait for client requests to connect.
     * Once request is received create thread for client.
     */
    public void run() {
        ChatServerThread client;
        while (true) {
            // Wait for client's request to connect
            try {
                // Create and start thread for each client
                client = new ChatServerThread(this, socket.accept());
                client.start();
            } catch(IOException ie) {
                System.out.println("Acceptance Error: " + ie);
            }
        }
    }

    /**
     * Attempt to add user to group and return status code.
     *
     * @param user
     * @param group
     * @return
     */
    public Response join(String user, String group, DataOutputStream outStream) {
        // If user does not exist
        if (!users.containsKey(user))
            // Add user
            users.put(user, outStream);
        // If group does not exist
        if (!groups.containsKey(group)) {
            // Add group
            groups.put(group, new Group(group));
        }
        // If user is already a member of the group
        if (groups.get(group).existsUser(user)) {
            return new Response(201);
        } else {
            groups.get(group).addMember(user);
            return new Response(200);
        }
    }

    /**
     * Attempt to remove user from group and send status code.
     *
     * @param user
     * @param group
     * @return
     */
    public Response leave(String user, String group) {
        // If group does not exist
        if (!groups.containsKey(group))
            return new Response(400);
        // If user is not a member
        else if (!groups.get(group).existsUser(user))
            return new Response(201);
        else {
            groups.get(group).removeMember(user);
            return new Response(200);
        }
    }

    /**
     * Return status code and list of available groups if status code is 200.
     *
     * @return
     */
    public Response groups() {
        String members = "";
        Response response;

        if (groups.isEmpty()) {
            response = new Response(201);
        } else {
            // Append group names
            for (Group group : groups.values()) {
                members += group.getName() + "\n";
            }
            response = new Response(200 , members);
        }
        return response;
    }

    /**
     * Attempt to get users of a group and return status code.
     *
     * @param group
     * @return
     */
    public Response users(String group) {
        String users = "";
        Response response;

        // If the group does not exist
        if (!groups.containsKey(group)) {
            return new Response(400);
        }
        // If the group does not have any members
        if (groups.get(group).getMembers().isEmpty()) {
            return new Response(201);
        } else {
            // Append user names to reply
            for (String user : groups.get(group).getMembers() ) {
                users += user + "\n";
            }
            response = new Response(200, users);
        }
        return response;
    }

    /**
     * Attempt to get group history and return status code.
     *
     * @param group
     * @return
     */
    public Response history(String group) {
        String history = "";
        Response response;

        // If the group does not exist
        if (!groups.containsKey(group)) {
            response = new Response(400);
        // If the group's history is empty
        } else if (groups.get(group).getHistory().isEmpty()) {
            response = new Response(201);
        } else {
            // Append each message to reply
            for (String message: groups.get(group).getHistory() ) {
                history += message;
            }
            response = new Response(200, history);
        }
        return response;
    }

    /**
     * Send a message to user(s) and group(s).
     * Messages sent to a group will be added to group history.
     *
     * @param message
     * @return
     */
    public Response send(String message) {
        ArrayList<DataOutputStream> outputStream = new ArrayList<>();

        setRecipients(message);
        if((validateRecipients(recipients, message)).getReplyCode() == 400) {
            return new Response(400);
        }

        // Get DataOutputStream of all recipient users
        for (String recipient: recipients) {
            outputStream.add(users.get(recipient));
        }
        // Send the message to all recipient users
        for (DataOutputStream dataOutputStream: outputStream) {
            try {
                dataOutputStream.writeUTF(message);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Response(200);
    }

    /**
     * Add user to list of users.
     *
     * @param userName
     * @param dataOutputStream
     * @return
     */
    public Response addUserToServer(String userName, DataOutputStream dataOutputStream) {
        users.put(userName, dataOutputStream);
        return new Response(200);
    }

    /**
     * Ensure all users and groups exist.
     * Get users from groups.
     * Add message to each group history.
     *
     * @param nonValidatedRecipients
     * @param message
     * @return
     */
    private Response validateRecipients(ArrayList<String> nonValidatedRecipients, String message) {
        Response response = new Response(200);
        ArrayList<String> validatedRecipients = new ArrayList<>();

        for (String recipient: nonValidatedRecipients) {
            // If the recipient is a user
            if (recipient.startsWith("@")) {
                // Validate user existence
                if (users.containsKey(recipient.substring(1))) {
                    validatedRecipients.add(recipient.substring(1));
                } else {
                    return new Response(400);
                }
            // If recipient is a group
            } else {
                // Validate group existence and add history if valid
                if(groups.containsKey(recipient.substring(1))) {
                    groups.get(recipient.substring(1)).getHistory().add(message);
                    validatedRecipients.addAll(groups.get(recipient.substring(1)).getMembers());
                } else {
                    return new Response(400);
                }
            }
        }
        recipients = validatedRecipients;
        return response;
    }

    /**
     *  Set recipients instance variable by getting group and user recipients from request.
     *
     * @param request
     * @return
     */
    private void setRecipients(String request) {
        ArrayList<String> recipients = new ArrayList<>();

        /**
         * According to the following format:
         *
         * msgp send \n
         * from: name \n
         * to: @name \n\n
         * to: @name \n\n
         * message \n\n
         *
         * By splitting the request by \n we can look at the indices that start with 'to:'
         */
        String[] parsedRequest = request.split("\n");

        for (int i = 0; i < parsedRequest.length; i++) {
            // If the string is at least 4 characters and starts with 'to:'
            if (parsedRequest[i].length() > 4 && parsedRequest[i].substring(0, 3).equals("to:")) {
                // Remove 'to:' and add user or group to recipients list
                recipients.add(parsedRequest[i].substring(4));
            }
        }
        this.recipients = recipients;
    }
}