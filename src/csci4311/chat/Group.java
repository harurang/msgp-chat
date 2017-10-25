package csci4311.chat;

import java.util.*;

public class Group {

    private String name;
    private ArrayList<String> members;
    private ArrayList<String> history;

    public Group(String name) {
        this.name = name;
        members = new ArrayList<>();
        history = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getMembers() {
        return this.members;
    }

    public int getSize() {
        return this.members.size();
    }

    public ArrayList<String> getHistory() {
        return this.history;
    }

    /**
     * Checks if group contains user.
     *
     * @param user
     * @return
     */
    public boolean existsUser(String user) {
        return members.contains(user);
    }

    /**
     * Remove member from group.
     *
     * @param user
     */
    public void removeMember (String user) {
        members.remove(user);
    }

    /**
     * Add member to group
     *
     * @param user
     */
    public void addMember (String user) {
        members.add(user);
    }
}