package ong.aldenw.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.GroupManager;
import ong.aldenw.NetworkManager;
import ong.aldenw.formats.RgbIntFormat;

import java.util.*;

public class GroupData {
    private String name;
    private String prefix;
    private int color;
    private int visibility;
    private UUID leader;
    private ArrayList<UUID> players = new ArrayList<>();
    private ArrayList<UUID> requests = new ArrayList<>();

    // New group constructor
    public GroupData(String name, UUID creator) {
        this.name = name;
        this.prefix = "";
        this.color = RgbIntFormat.fromThree(255, 255, 255);
        this.visibility = 0;
        this.leader = creator;
        this.players.add(creator);
    }

    // Load from NBT constructor
    public GroupData(String provName, String provPrefix, int provColor, int visibility, NbtCompound playersNbt, NbtCompound requestsNbt) {
        this.name = provName;
        this.prefix = provPrefix;
        this.color = provColor;
        this.visibility = visibility;

        playersNbt.getKeys().forEach(playerKey -> {
            String role = playersNbt.getString(playerKey);
            if (role.equals("LEADER")) {
                this.leader = UUID.fromString(playerKey);
            }
            this.players.add(UUID.fromString(playerKey));
        });

        requestsNbt.getKeys().forEach(playerKey -> this.requests.add(UUID.fromString(playerKey)));
    }

    public void notifyOnlineMembers(Text text, MinecraftServer server) {
        players.forEach(groupPlayer -> {
            ServerPlayerEntity groupPlayerEntity = server.getPlayerManager().getPlayer(groupPlayer);
            if (!Objects.isNull(groupPlayerEntity)) {
                groupPlayerEntity.sendMessage(text);
            }
        });
    }

    public void notifyGroupLeader(Text text, MinecraftServer server) {
        ServerPlayerEntity groupPlayerEntity = server.getPlayerManager().getPlayer(leader);
        if (!Objects.isNull(groupPlayerEntity)) {
            groupPlayerEntity.sendMessage(text);
        }
    }

    public String getName() {
        return name;
    }

    public void changeName(String name, MinecraftServer server) {
        GroupManager state = GroupManager.getServerState(server);
        if (this.name.equals(name) || state.isNameValid(name)) return;

        state.groupList.remove(this.name);
        state.groupList.put(name, this);
        this.name = name;
        players.forEach(groupPlayer -> state.players.get(groupPlayer).groupName = name);

        notifyOnlineMembers(Text.empty().append(Text.literal("Your group's name has changed to ").formatted(Formatting.GOLD)).append(Text.literal(name).withColor(color)), server);
    }

    public String getPrefix() {
        return prefix;
    }

    public void changePrefix(String prefix, MinecraftServer server) {
        GroupManager state = GroupManager.getServerState(server);
        if (this.prefix.equals(prefix) || !state.isPrefixValid(prefix)) return;

        this.prefix = prefix;
        updateOnlinePrefixCache(server);
        if (prefix.isEmpty())
            notifyOnlineMembers(Text.empty().append(Text.literal("Your group's prefix has been removed").formatted(Formatting.GOLD)), server);
        else
            notifyOnlineMembers(Text.empty().append(Text.literal("Your group's prefix has changed to ").formatted(Formatting.GOLD)).append(Text.literal(prefix).withColor(color)), server);
    }

    public int getColor() {
        return color;
    }

    public void changeColor(int color, MinecraftServer server) {
        color = RgbIntFormat.boundInt(color);
        if (this.color == color) return;

        this.color = color;
        notifyOnlineMembers(Text.empty().append(Text.literal("Your group's color has changed to ").formatted(Formatting.GOLD)).append(Text.literal("this").withColor(color)), server);
        updateOnlineColorCache(server);
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility, MinecraftServer server) {
        if (visibility < 0 || visibility > 2 || this.visibility == visibility) return;

        this.visibility = visibility;
        notifyOnlineMembers(switch (visibility) {
            case 0 -> Text.literal("Your group is now invite only").formatted(Formatting.GOLD);
            case 1 -> Text.literal("Your group is now request and invite only").formatted(Formatting.GOLD);
            case 2 -> Text.literal("Your group is now publicly joinable").formatted(Formatting.GOLD);
            default -> Text.literal("Something has gone wrong. You shouldn't see this message :(").formatted(Formatting.DARK_RED);
        }, server);
    }

    public UUID getLeader() {
        return leader;
    }

    public void changeLeader(UUID uuid, MinecraftServer server) {
        if (leader.equals(uuid) || !players.contains(uuid)) return;

        this.leader = uuid;
        notifyOnlineMembers(Text.empty().append(Text.literal("Your group's leader has changed to ").formatted(Formatting.GOLD)).append(Text.literal(GroupManager.getPlayerState(uuid, server).username).withColor(color)), server);
    }

    public List<UUID> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public void addPlayer(UUID playerUuid, MinecraftServer server) {
        requests.remove(playerUuid);
        GroupManager state = GroupManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);
        if (Objects.isNull(playerData) || !playerData.groupName.isEmpty() || players.contains(playerUuid)) return;

        players.add(playerUuid);
        playerData.groupName = name;
        notifyOnlineMembers(Text.empty().append(Text.literal(playerData.username).withColor(color)).append(Text.literal(" has joined the group").formatted(Formatting.GOLD)), server);

        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(playerUuid);
        if (!Objects.isNull(playerEntity)) {
            if (!prefix.isEmpty())
                NetworkManager.updatePrefixCache(playerEntity, prefix, server);
            NetworkManager.updateColorCache(playerEntity, color, server);
        }
    }

    public void removePlayer(UUID playerUuid, MinecraftServer server) {
        GroupManager state = GroupManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);
        if (leader.equals(playerUuid) || Objects.isNull(playerData) || !playerData.groupName.equals(name)) return;

        players.remove(playerUuid);
        playerData.groupName = "";
        notifyOnlineMembers(Text.empty().append(Text.literal(playerData.username)).append(Text.literal(" has left the group").formatted(Formatting.GOLD)), server);

        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(playerUuid);
        if (!Objects.isNull(playerEntity)) {
            NetworkManager.clearCache(playerEntity, server);
        }
    }

    public boolean isGroupOf(UUID playerUuid) {
        return players.contains(playerUuid);
    }

    public void updateOnlineColorCache(MinecraftServer server) {
        players.forEach(groupPlayer -> {
            ServerPlayerEntity onlineGroupPlayer = server.getPlayerManager().getPlayer(groupPlayer);
            if (!Objects.isNull(onlineGroupPlayer)) {
                NetworkManager.updateColorCache(onlineGroupPlayer, color, server);
            }
        });
    }

    public void updateOnlinePrefixCache(MinecraftServer server) {
        players.forEach(groupPlayer -> {
            ServerPlayerEntity onlineGroupPlayer = server.getPlayerManager().getPlayer(groupPlayer);
            if (!Objects.isNull(onlineGroupPlayer)) {
                NetworkManager.updatePrefixCache(onlineGroupPlayer, prefix, server);
            }
        });
    }

    public List<UUID> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    public boolean hasRequests() {
        return !requests.isEmpty();
    }

    public boolean requestsContains(UUID uuid) {
        return requests.contains(uuid);
    }

    public int getRequestsSize() {
        return requests.size();
    }

    public void addRequest(UUID playerUuid, MinecraftServer server) {
        GroupManager state = GroupManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);
        if (requests.contains(playerUuid) || players.contains(playerUuid) || visibility != 1 || !playerData.groupName.isEmpty()) return;

        requests.add(playerUuid);
        notifyGroupLeader(Text.empty().append(Text.literal(playerData.username)).append(Text.literal(" has requested to join your group").formatted(Formatting.YELLOW)), server);
    }

    public void denyRequest(UUID playerUuid, MinecraftServer server) {
        requests.remove(playerUuid);
        GroupManager state = GroupManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);

        if (!playerData.groupName.isEmpty()) return;

        ServerPlayerEntity requestingPlayerEntity = server.getPlayerManager().getPlayer(playerUuid);
        if (!Objects.isNull(requestingPlayerEntity)) {
            requestingPlayerEntity.sendMessage(Text.empty().append(Text.literal("You have been denied from ").formatted(Formatting.DARK_RED)).append(Text.literal(name).withColor(color)));
        }
    }

    public void acceptAllRequests(MinecraftServer server) {
        while(!requests.isEmpty()) {
            addPlayer(requests.getFirst(), server);
        }
    }

    public void denyAllRequests(MinecraftServer server) {
        while(!requests.isEmpty()) {
            denyRequest(requests.getFirst(), server);
        }
    }
}
