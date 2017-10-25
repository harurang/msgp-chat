package csci4311.chat;

import java.util.List;

public interface MsgpClient {

    /**
     * Listen to input stream for messages and replies.
     */
    void run();

    /**
     * Request to add a user to a chat group.
     *
     * @param user
     * @param group
     * @return
     */
    String join(String user, String group);

    /**
     * Request to remove a user from a chat group.
     *
     * @param user
     * @param group
     * @return
     */
    String leave(String user, String group);

    /**
     * Request for current list of groups.
     *
     * @return
     */
    List<String> groups();

    /**
     * Request list of users in a group.
     *
     * @param group
     * @return
     */
    List<String> users(String group);

    /**
     * Request group history.
     *
     * @param group
     * @return
     */
    List<Message> history(String group);

    /**
     * Request to send message.
     *
     * @param parsedInput
     * @param userName
     * @return
     */
    String send(String[] parsedInput, String userName);

    /**
     * Send request to the server.
     *
     * @param request
     * @return
     */
    String processRequest(String request);

    /**
     * Adds user to server.
     *
     * @return
     */
    String addUser();

    /**
     * Wait for response from server. response will be set in run().
     */
    void setResponse();

    /**
     * Formats message to send into:
     *
     * msgp send \n
     * from: name \n
     * to: @name \n\n
     * to: @name \n\n
     * message \n\n
     *
     * @param parsedInput
     * @param userName
     * @return
     */
    String encodeMessage(String[] parsedInput, String userName);

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
    Message decodeMessage(String msg);
}
