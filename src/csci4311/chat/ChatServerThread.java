/**
 * ChatServerThread is a thread that processes client requests using the ChatServer and returns a Response.
 * Each thread refers to the same ChatServer, but a different Client.
 */

package csci4311.chat;

import java.io.*;
import java.net.*;

public class ChatServerThread extends Thread {

    private ChatServer chatServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public ChatServerThread(ChatServer server, Socket connectionSocket) {
        // Instantiate instance variables
        this.chatServer = server;
        try {
            dataInputStream = new DataInputStream(connectionSocket.getInputStream());
            dataOutputStream = new DataOutputStream(connectionSocket.getOutputStream());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String response;
        String request;
        try {
            while(true) {
                // Get the client request
                request = dataInputStream.readUTF();
                // Parse client request
                String[] parsedRequest = request.split(" ");
                // Process request
                if(parsedRequest[1].equals("join")) {
                    response = evaluateResponse(chatServer.join(parsedRequest[2], parsedRequest[3], dataOutputStream));
                } else if(parsedRequest[1].equals("leave")) {
                    response = evaluateResponse(chatServer.leave(parsedRequest[2], parsedRequest[3]));
                } else if(parsedRequest[1].equals("groups")) {
                    response = evaluateResponseWithBody(chatServer.groups());
                } else if(parsedRequest[1].equals("users")) {
                    response = evaluateResponseWithBody(chatServer.users(parsedRequest[2]));
                } else if(parsedRequest[1].equals("history")) {
                    response = evaluateResponseWithBody(chatServer.history(parsedRequest[2]));
                } else if (parsedRequest[1].equals("addUser")) {
                    response = evaluateResponseWithBody(chatServer.addUserToServer(parsedRequest[2], dataOutputStream));
                } else {
                    response = evaluateResponse(chatServer.send(request));
                }
                // Write response to DataOutputStream
                dataOutputStream.writeUTF(response);
            }
        } catch( IOException ex) {
            System.out.println("The follow error occurred: " + ex);
        }
    }

    /**
     * Based on response ChatServer response, return response to message protocol.
     *
     * @param response
     * @return
     */
    private String evaluateResponse(Response response) {
        if(response.getReplyCode() == 400) {
            return "msgp 400 Error";
        } else if(response.getReplyCode() == 201) {
            return "msgp 201 No result";
        } else {
            return "msgp 200 OK";
        }
    }

    /**
     * Based on response from ChatServer resopnse, return response to message protocol with body.
     *
     * @param response
     * @return
     */
    private String evaluateResponseWithBody(Response response) {
        if(response.getReplyCode() == 400) {
            return "msgp 400 Error";
        } else if(response.getReplyCode() == 201) {
            return "msgp 201 No result";
        } else {
            return "msgp 200 OK\n" + response.getBody();
        }
    }
}