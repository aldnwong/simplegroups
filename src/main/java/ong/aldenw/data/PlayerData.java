package ong.aldenw.data;

import java.util.ArrayList;

public class PlayerData {
    private String groupName;
    private String username;

    public PlayerData(String username) {
        this.groupName = "";
        this.username = username;
    }

    public PlayerData(String groupName, String username) {
        this.groupName = groupName;
        this.username = username;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getUsername() {
        return this.username;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public boolean isInAGroup() {
        return (!this.groupName.isEmpty());
    }

    public boolean isInGroup(String groupName) {
        return (this.groupName.equals(groupName));
    }

    public void joinGroup(String groupName) {
        if (isInAGroup()) return;
        this.groupName = groupName;
    }

    public void leaveGroup() {
        this.groupName = "";
    }
}
