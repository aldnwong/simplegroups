package ong.aldenw;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class GroupManager extends PersistentState {
    public HashMap<String, GroupData> groupList = new HashMap<>();
    public HashMap<UUID, PlayerData> players = new HashMap<>();
    public HashMap<String, UUID> playerUuidCache = new HashMap<>();

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

            groupNbt.putString("displayName", groupData.displayName);
            groupNbt.putString("prefix", groupData.prefix);
            groupNbt.putBoolean("listed", groupData.listed);
            groupNbt.putBoolean("open", groupData.open);
            groupNbt.putInt("color", groupData.color);

            NbtCompound groupPlayersNbt = new NbtCompound();
            groupData.players.forEach((uuid -> {
                if (groupData.leader.equals(uuid))
                    groupPlayersNbt.putString(uuid.toString(), "LEADER");
                else
                    groupPlayersNbt.putString(uuid.toString(), "MEMBER");
            }));
            groupNbt.put("players", groupPlayersNbt);

            groupsNbt.put(id, groupNbt);
        }));

        nbt.put("groups", groupsNbt);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach(((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putString("groupId", playerData.groupName);
            playersNbt.put(uuid.toString(), playerNbt);
        }));

        nbt.put("players", playersNbt);

        NbtCompound playerUuidCacheNbt = new NbtCompound();
        playerUuidCache.forEach(((username, uuid) -> {
            playerUuidCacheNbt.putString(username, uuid.toString());
        }));

        nbt.put("playerUuidCache", playerUuidCacheNbt);

        return nbt;
    }

    public static GroupManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        GroupManager state = new GroupManager();

        NbtCompound groupsNbt = tag.getCompound("groups");
        groupsNbt.getKeys().forEach(key -> {
            GroupData groupData = new GroupData();

            groupData.displayName = groupsNbt.getCompound(key).getString("displayName");
            groupData.prefix = groupsNbt.getCompound(key).getString("prefix");
            groupData.listed = groupsNbt.getCompound(key).getBoolean("listed");
            groupData.open = groupsNbt.getCompound(key).getBoolean("open");
            groupData.color = groupsNbt.getCompound(key).getInt("color");

            NbtCompound groupPlayersNbt = groupsNbt.getCompound(key).getCompound("players");
            groupPlayersNbt.getKeys().forEach(playerKey -> {
                String role = groupPlayersNbt.getString(playerKey);
                SimpleGroups.LOGGER.info("UNLOADING GROUP PLAYER {}; IS {}", playerKey, role);
                if (role.equals("LEADER")) {
                    groupData.leader = UUID.fromString(playerKey);
                    groupData.players.add(UUID.fromString(playerKey));
                    SimpleGroups.LOGGER.info("FOUND GROUP LEADER");
                }
                else {
                    groupData.players.add(UUID.fromString(playerKey));
                    SimpleGroups.LOGGER.info("FOUND GROUP MEMBER");
                }
            });

            state.groupList.put(key, groupData);
        });

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.groupName = playersNbt.getCompound(key).getString("groupId");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        NbtCompound playerUuidCacheNbt = tag.getCompound("playerUuidCache");
        playerUuidCacheNbt.getKeys().forEach(key -> {
            state.playerUuidCache.put(key, UUID.fromString(playerUuidCacheNbt.getString(key)));
        });

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
        if (!serverState.playerUuidCache.containsKey(playerName) || !serverState.playerUuidCache.get(playerName).equals(player.getUuid())) {
            serverState.playerUuidCache.put(playerName, player.getUuid());
        }
        PlayerData playerData = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
        if (Objects.isNull(serverState.groupList.get(playerData.groupName))) {
            playerData.groupName = "";
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
}
