package ong.aldenw.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.formats.GroupFormat;
import ong.aldenw.managers.DataManager;
import ong.aldenw.managers.NetworkManager;
import ong.aldenw.formats.RgbIntFormat;

import java.util.*;

public class GroupData {
    private String name;
    private String prefix;
    private int color;
    private int visibility;
    private UUID leader;
    private final ArrayList<UUID> players = new ArrayList<>();
    private final ArrayList<UUID> requests = new ArrayList<>();
    private final ArrayList<UUID> invites = new ArrayList<>();

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
    public GroupData(String name, String prefix, int color, int visibility, NbtCompound playersNbt, NbtList requestsNbt) {
        this.name = name;
        this.prefix = prefix;
        this.color = color;
        this.visibility = visibility;

        playersNbt.getKeys().forEach(playerKey -> {
            String role = playersNbt.getString(playerKey);
            if (role.equals("LEADER")) {
                this.leader = UUID.fromString(playerKey);
            }
            this.players.add(UUID.fromString(playerKey));
        });

        requestsNbt.forEach(playerUuid -> requests.add(UUID.fromString(playerUuid.asString())));
    }

    public void notifyOnlineMembers(Text text, MinecraftServer server) {
        players.forEach(groupPlayer -> {
            ServerPlayerEntity groupPlayerEntity = server.getPlayerManager().getPlayer(groupPlayer);
            if (!Objects.isNull(groupPlayerEntity)) {
                groupPlayerEntity.sendMessageToClient(text, false);
            }
        });
    }

    public void notifyGroupLeader(Text text, MinecraftServer server) {
        ServerPlayerEntity groupPlayerEntity = server.getPlayerManager().getPlayer(leader);
        if (!Objects.isNull(groupPlayerEntity)) {
            groupPlayerEntity.sendMessageToClient(text, false);
        }
    }

    public String getName() {
        return name;
    }

    public void changeName(String name, MinecraftServer server) {
        DataManager state = DataManager.getServerState(server);
        if (this.name.equals(name) || !GroupFormat.isNameValid(name, state)) return;

        state.groupList.remove(this.name);
        this.name = name;
        state.groupList.put(name, this);

        players.forEach(groupPlayer -> DataManager.getPlayerState(groupPlayer, server).joinGroup(name));

        notifyOnlineMembers(Text.empty().append(Text.literal("Your group's name has changed to ").formatted(Formatting.GOLD)).append(Text.literal(name).withColor(color)), server);
    }

    public String getPrefix() {
        return prefix;
    }

    public void changePrefix(String prefix, MinecraftServer server) {
        DataManager state = DataManager.getServerState(server);
        if (this.prefix.equals(prefix) || !GroupFormat.isPrefixValid(prefix, state)) return;

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

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public void changeLeader(UUID uuid, MinecraftServer server) {
        if (leader.equals(uuid) || !players.contains(uuid)) return;
        this.leader = uuid;
        notifyOnlineMembers(Text.empty().append(Text.literal("Your group's leader has changed to ").formatted(Formatting.GOLD)).append(Text.literal(DataManager.getPlayerState(uuid, server).getUsername()).withColor(color)), server);
    }

    public List<UUID> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public void addPlayer(UUID playerUuid, MinecraftServer server) {
        requests.remove(playerUuid);
        invites.remove(playerUuid);
        DataManager state = DataManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);
        if (Objects.isNull(playerData) || playerData.isInAGroup() || players.contains(playerUuid)) return;

        players.add(playerUuid);
        playerData.joinGroup(name);
        notifyOnlineMembers(Text.empty().append(Text.literal(playerData.getUsername()).withColor(color)).append(Text.literal(" has joined the group").formatted(Formatting.GOLD)), server);

        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(playerUuid);
        if (!Objects.isNull(playerEntity)) {
            if (!prefix.isEmpty())
                NetworkManager.updatePrefixCache(playerEntity.getUuid(), prefix, server);
            NetworkManager.updateColorCache(playerEntity.getUuid(), color, server);
        }
    }

    public void removePlayer(UUID playerUuid, MinecraftServer server) {
        DataManager state = DataManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);
        if (leader.equals(playerUuid) || Objects.isNull(playerData) || !playerData.isInGroup(name)) return;

        players.remove(playerUuid);
        playerData.leaveGroup();
        notifyOnlineMembers(Text.empty().append(Text.literal(playerData.getUsername())).append(Text.literal(" has left the group").formatted(Formatting.GOLD)), server);

        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(playerUuid);
        if (!Objects.isNull(playerEntity)) {
            NetworkManager.clearCache(playerEntity.getUuid(), server);
        }
    }

    public boolean isGroupOf(UUID playerUuid) {
        return players.contains(playerUuid);
    }

    public void updateOnlineColorCache(MinecraftServer server) {
        players.forEach(groupPlayer -> {
            ServerPlayerEntity onlineGroupPlayer = server.getPlayerManager().getPlayer(groupPlayer);
            if (!Objects.isNull(onlineGroupPlayer)) {
                NetworkManager.updateColorCache(onlineGroupPlayer.getUuid(), color, server);
            }
        });
    }

    public void updateOnlinePrefixCache(MinecraftServer server) {
        players.forEach(groupPlayer -> {
            ServerPlayerEntity onlineGroupPlayer = server.getPlayerManager().getPlayer(groupPlayer);
            if (!Objects.isNull(onlineGroupPlayer)) {
                NetworkManager.updatePrefixCache(onlineGroupPlayer.getUuid(), prefix, server);
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
        DataManager state = DataManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);
        if (requests.contains(playerUuid) || players.contains(playerUuid) || visibility != 1 || playerData.isInAGroup()) return;

        requests.add(playerUuid);
        notifyGroupLeader(Text.empty().append(Text.literal(playerData.getUsername())).append(Text.literal(" has requested to join your group").formatted(Formatting.YELLOW)), server);
    }

    public void denyRequest(UUID playerUuid, MinecraftServer server) {
        requests.remove(playerUuid);
        DataManager state = DataManager.getServerState(server);
        PlayerData playerData = state.players.get(playerUuid);

        if (playerData.isInAGroup()) return;

        ServerPlayerEntity requestingPlayerEntity = server.getPlayerManager().getPlayer(playerUuid);
        if (!Objects.isNull(requestingPlayerEntity)) {
            requestingPlayerEntity.sendMessageToClient(Text.empty().append(Text.literal("You have been denied from ").formatted(Formatting.DARK_RED)).append(Text.literal(name).withColor(color)), false);
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

    public boolean invited(UUID playerUuid) {
        return invites.contains(playerUuid);
    }

    public void addInvite(UUID playerUuid) {
        if (!invites.contains(playerUuid))
            invites.add(playerUuid);
    }

    public void deleteGroup(MinecraftServer server) {
        for (UUID playerUuid : players) {
            PlayerData playerData = DataManager.getPlayerState(playerUuid, server);
            playerData.leaveGroup();
            NetworkManager.clearCache(playerUuid, server);
            ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(playerUuid);
            if (!Objects.isNull(playerEntity)) {
                playerEntity.sendMessageToClient(Text.literal("Your group has been deleted").formatted(Formatting.GOLD), false);
            }
        }
        DataManager.getServerState(server).groupList.remove(this.getName());
    }
}
