package ong.aldenw.data;

import ong.aldenw.GroupManager;
import ong.aldenw.formats.RgbIntFormat;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class GroupData {
    public String name = "";
    public String prefix = "";
    public int color = RgbIntFormat.fromThree(255, 255, 255);
    public boolean listed = false;
    public boolean open = false;
    public UUID leader;
    public ArrayList<UUID> players = new ArrayList<>();
    public ArrayList<UUID> requests = new ArrayList<>();

    public void addPlayer(UUID playerUuid, GroupManager state) {
        PlayerData playerData = state.players.get(playerUuid);
        if (!Objects.isNull(playerData) && playerData.groupName.isEmpty()) {
            players.add(playerUuid);
            playerData.groupName = name;
        }
    }

    public void removePlayer(UUID playerUuid, GroupManager state) {
        PlayerData playerData = state.players.get(playerUuid);
        if (!Objects.isNull(playerData) && playerData.groupName.equals(name)) {
            players.remove(playerUuid);
            playerData.groupName = "";
        }
    }
}
