package ong.aldenw;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class GroupManager extends PersistentState {
    public int MAX_GROUP_NAME_LENGTH = 25;
    public int MAX_PREFIX_NAME_LENGTH = 20;
    public HashMap<String, GroupData> groupList = new HashMap<>();
    public HashMap<UUID, PlayerData> players = new HashMap<>();
    public HashMap<String, UUID> playerUuids = new HashMap<>();

    private static final Type<GroupManager> type = new Type<>(
            GroupManager::new,
            GroupManager::createFromNbt,
            null
    );

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound groupsNbt = new NbtCompound();
        groupList.forEach(((id, groupData) -> {
            NbtCompound groupNbt = new NbtCompound();

            groupNbt.putString("name", groupData.getName());
            groupNbt.putString("prefix", groupData.getPrefix());
            groupNbt.putBoolean("listed", groupData.isListed());
            groupNbt.putBoolean("open", groupData.isOpen());
            groupNbt.putInt("color", groupData.getColor());

            NbtCompound groupPlayersNbt = new NbtCompound();
            groupData.getPlayers().forEach((uuid -> {
                if (groupData.getLeader().equals(uuid))
                    groupPlayersNbt.putString(uuid.toString(), "LEADER");
                else
                    groupPlayersNbt.putString(uuid.toString(), "MEMBER");
            }));
            groupNbt.put("players", groupPlayersNbt);

            NbtCompound groupRequestsNbt = new NbtCompound();
            groupData.getRequests().forEach((uuid -> groupRequestsNbt.putString(uuid.toString(), ":3")));
            groupNbt.put("requests", groupRequestsNbt);

            groupsNbt.put(id, groupNbt);
        }));

        nbt.put("groups", groupsNbt);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach(((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putString("groupId", playerData.groupName);
            playerNbt.putString("username", playerData.username);
            playersNbt.put(uuid.toString(), playerNbt);
        }));

        nbt.put("players", playersNbt);

        NbtCompound playerUuidsNbt = new NbtCompound();
        playerUuids.forEach(((username, uuid) -> {
            playerUuidsNbt.putString(username, uuid.toString());
        }));

        nbt.put("playerUuids", playerUuidsNbt);

        NbtCompound groupGlobalConfig = new NbtCompound();
        groupGlobalConfig.putInt("maxGroupNameLength", MAX_GROUP_NAME_LENGTH);
        groupGlobalConfig.putInt("maxPrefixNameLength", MAX_PREFIX_NAME_LENGTH);

        nbt.put("globalConfig", groupGlobalConfig);

        return nbt;
    }

    public static GroupManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        GroupManager state = new GroupManager();

        NbtCompound groupsNbt = tag.getCompound("groups");
        groupsNbt.getKeys().forEach(key -> {
            GroupData groupData = new GroupData(
                    groupsNbt.getCompound(key).getString("name"),
                    groupsNbt.getCompound(key).getString("prefix"),
                    groupsNbt.getCompound(key).getInt("color"),
                    groupsNbt.getCompound(key).getBoolean("listed"),
                    groupsNbt.getCompound(key).getBoolean("open"),
                    groupsNbt.getCompound(key).getCompound("players"),
                    groupsNbt.getCompound(key).getCompound("requests")
            );
            state.groupList.put(key, groupData);
        });

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.groupName = playersNbt.getCompound(key).getString("groupId");
            playerData.username = playersNbt.getCompound(key).getString("username");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        NbtCompound playerUuidsNbt = tag.getCompound("playerUuids");
        playerUuidsNbt.getKeys().forEach(key -> {
            state.playerUuids.put(key, UUID.fromString(playerUuidsNbt.getString(key)));
        });

        NbtCompound globalConfig = tag.getCompound("globalConfig");
        int maxGroupNameNbt = globalConfig.getInt("maxGroupNameLength");
        int maxPrefixNameNbt = globalConfig.getInt("maxPrefixNameLength");
        state.MAX_GROUP_NAME_LENGTH = (maxGroupNameNbt > 0) ? maxGroupNameNbt : 25;
        state.MAX_PREFIX_NAME_LENGTH = (maxPrefixNameNbt > 0) ? maxPrefixNameNbt : 20;

        NetworkManager.createCache(state);

        return state;
    }

    public static GroupManager getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        GroupManager state = persistentStateManager.getOrCreate(type, SimpleGroups.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        GroupManager serverState = getServerState(player.getWorld().getServer());
        String playerName = player.getName().getString();
        if (!serverState.playerUuids.containsKey(playerName) || !serverState.playerUuids.get(playerName).equals(player.getUuid())) {
            serverState.playerUuids.put(playerName, player.getUuid());
        }
        PlayerData playerData = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
        if (Objects.isNull(serverState.groupList.get(playerData.groupName))) {
            playerData.groupName = "";
        }
        if (!playerData.username.equals(playerName)) {
            playerData.username = playerName;
        }
        return playerData;
    }

    public static PlayerData getPlayerState(UUID playerUuid, MinecraftServer server) {
        GroupManager serverState = getServerState(server);
        PlayerData playerData = serverState.players.computeIfAbsent(playerUuid, uuid -> new PlayerData());
        if (Objects.isNull(serverState.groupList.get(playerData.groupName))) {
            playerData.groupName = "";
        }
        return playerData;
    }

    public ArrayList<String> getPrefixArray() {
        ArrayList<String> prefixCache = new ArrayList<>();
        groupList.forEach((name, groupData) -> {
            prefixCache.add(groupData.getPrefix());
        });
        return prefixCache;
    }

    public boolean checkNameValidity(String groupName) {
        return groupName.length() <= MAX_GROUP_NAME_LENGTH;
    }
}
