/**
 * Processes requests from client by fowarding requests to the server.
 */

package csci4311.chat;

import java.util.*;
import java.io.*;
import java.net.*;

public class TextMsgpClient extends Thread implements MsgpClient {

    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String request;
    private String response;
    private String userName;
    CLIUserAgent client;

    /**
     *
     * @param clientSocket
     * @throws Exception
     */
    public TextMsgpClient(CLIUserAgent client, Socket clientSocket, String userName) throws Exception {
        // Instantiate instance variables
        this.dataOutputStream = new DataOutputStream(clientSocket.getOutputStream()) ;
        this.dataInputStream = new DataInputStream(clientSocket.getInputStream()) ;
        this.request = "";
        this.response = "";
        this.client = client;
        this.userName = userName;
        // Starts a thread
        this.start();
    }

    /**
     * Listen to input stream for messages and replies.
     */
    public void run() {
        String incomingMessage;
        try {
            while (true) {
                // Wait for incoming messages
                incomingMessage = dataInputStream.readUTF();
                // If the message was sent from another user
                if (incomingMessage.startsWith("msgp send")) {
                    Message message = decodeMessage(incomingMessage);
                    if (!message.getFrom().equals(userName)) {
                        client.deliverMessage(message);
                    }
                } else { // If it is not a message it is a response to a request
                    response = incomingMessage;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request to add a user to a chat group.
     *
     * @param user
     * @param group
     * @return
     */
    public String join(String user, String group) {
        request = "msgp join " + user + " " + group;
        return processRequest(request);
    }

    /**
     * Request to remove a user from a chat group.
     *
     * @param user
     * @param group
     * @return
     */
    public String leave(String user, String group) {
        request = "msgp leave " + user + " " + group;
        return processRequest(request);
    }

    /**
     * Request to send message.
     *
     * @param parsedInput
     * @param userName
     * @return
     */
    public String send(String[] parsedInput, String userName) {
        return processRequest(encodeMessage(parsedInput, userName));
    }

    /**
     * Adds user to server.
     *
     * @return
     */
    public String addUser() {
        return processRequest("msgp addUser " + userName);
    }

    /**
     * Request for current list of groups.
     *
     * @return
     */
    public ArrayList<String> groups() {
        ArrayList<String> groups = new ArrayList<>();
        request = "msgp groups";
        // Process request
        try {
            dataOutputStream.writeUTF(request);
            this.setResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response.startsWith("msg 400")) {
                client.deliverError(response);
            } else {
                // Split the response into group names using delimiter "\n" and save them to the array list
                groups = new ArrayList<>(Arrays.asList(response.split("\n")));
                // Remove the first string in the list, which is the protocol's response message
                groups.remove(0);
            }
            return groups;
        }
    }

    /**
     * Request list of users in a group.
     *
     * @param group
     * @return
     */
    public ArrayList<String> users(String group) {
        ArrayList<String> users = new ArrayList<>();
        request = "msgp users " + group;
        // Process request
        try {
            dataOutputStream.writeUTF(request);
            this.setResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // If the group is empty or does not exist
            if (response.startsWith("msgp 201") || response.startsWith("msgp 400")) {
                client.deliverError(response);
            } else {
                // Split the response into user names using delimiter "\n" and save them to the array list
                users = new ArrayList<>( Arrays.asList(response.split("\n")));
                // Remove protocol's response message
                users.remove(0);
            }
            return users;
        }
    }

    /**
     * Request group history.
     *
     * @param group
     * @return
     */
    public ArrayList<Message> history(String group) {
        ArrayList<Message> history = new ArrayList<>();
        request = "msgp history " + group;
        try {
            dataOutputStream.writeUTF(request);
            this.setResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response.startsWith("msgp 201") || response.startsWith("msgp 400"))
                client.deliverError(response);
            else {
                // Parse response so that each index is a message
                String[] parsedResponse = response.split("\n\nmsgp send");
                // Decode all messages in the list and save them to the group history
                for (String message: parsedResponse) {
                    history.add(decodeMessage(message));
                }
            }
            return history;
        }
    }

    /**
     * Process request by writing to output stream.
     *
     * @param request
     * @return
     */
    public String processRequest(String request) {
        try {
            dataOutputStream.writeUTF(request);
            this.setResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return response;
        }
    }

    /**
     * Wait for response from server. response will be set in run().
     */
    public void setResponse() {
        response = "";
        while (response.equals("")) {
            try {
                Thread.sleep(20);
            }
            catch( InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * Formats message to send into:
     *
     * msgp send \n
     * from: name \n
     * to: @name \n\n
     * to: @name \n\n
     * message \n\n
     *
     * @param parsedUserInput
     * @param userName
     * @return
     */
    public String encodeMessage(String[] parsedUserInput, String userName) {
        ArrayList<String> recipients = new ArrayList<>();
        String message = "";
        // Get recipients and message
        for (String word: parsedUserInput) {
            if(word.equals("send")) {
                continue;
            } else if (word.startsWith("@")) {
                recipients.add(word);
            } else if (word.startsWith("#")) {
                recipients.add(word);
            } else {
                message += word + " ";
            }
        }

        request = "msgp send\nfrom: " + userName + "\n";
        for (String recipient: recipients) {
            request += ("to: " + recipient + "\n");
        }
        request += "\n" + message + "\n\n";
        return request;
    }

    /**
     * Deformats message from:
     *
     * msgp send \n
     * from: name \n
     * to: @name \n\n
     * to: @name \n\n
     * message \n\n
     *
     * to obtain sender and message.
     *
     * @param msg
     * @return
     */
    public Message decodeMessage(String msg) {
        String sender = "";
        String message = "";
        String[] parsedMsg = msg.split("\n");

        for (String index: parsedMsg) {
            if (index.startsWith("from: ")) {
                // get sender
                sender = index.substring(6);
            } else if (!index.startsWith("to:") &&  !index.startsWith("msgp send")) {
                // get message
                message = index;
            }
        }
        return new Message(sender,null, message);
    }
}