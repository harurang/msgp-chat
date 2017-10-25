package csci4311.chat;

public interface UserAgent {

    /**
     * Continuously read, process and respond to user input.
     */
    void start();

    /**
     * Request to add a user to a group.
     */
    void join(String userName, String group);

    /**
     * Request to remove a user from a group.
     */
    void leave(String userName, String group);

    /**
     * Request a list of existing groups.
     */
    void groups();

    /**
     * Request to get users of a group.
     */
    void users(String group);

    /**
     * Request for group history and print results.
     */
    void history(String group);

    /**
     * Request to send message.
     */
    void send(String[] parsedUserInput);
}
