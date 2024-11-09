package ong.aldenw.managers;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import ong.aldenw.SimpleGroups;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.GroupFormat;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class NbtManager extends PersistentState {
    // TODO: Make variables private and only access/modify through accessor and modifier methods
    public HashMap<String, GroupData> groupList = new HashMap<>();
    public HashMap<UUID, PlayerData> players = new HashMap<>();
    public HashMap<String, UUID> playerUuids = new HashMap<>();

    private static final Type<NbtManager> type = new Type<>(
            NbtManager::new,
            NbtManager::createFromNbt,
            null
    );

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound groupsNbt = new NbtCompound();
        groupList.forEach(((id, groupData) -> {
            NbtCompound groupNbt = new NbtCompound();

            groupNbt.putString("name", groupData.getName());
            groupNbt.putString("prefix", groupData.getPrefix());
            groupNbt.putInt("visibility", groupData.getVisibility());
            groupNbt.putInt("color", groupData.getColor());

            NbtCompound groupPlayersNbt = new NbtCompound();
            groupData.getPlayers().forEach((uuid -> {
                if (groupData.getLeader().equals(uuid))
                    groupPlayersNbt.putString(uuid.toString(), "LEADER");
                else
                    groupPlayersNbt.putString(uuid.toString(), "MEMBER");
            }));
            groupNbt.put("players", groupPlayersNbt);

            NbtList groupRequestsNbt = new NbtList();
            groupData.getRequests().forEach((uuid -> groupRequestsNbt.add(NbtString.of(uuid.toString()))));
            groupNbt.put("requests", groupRequestsNbt);

            groupsNbt.put(id, groupNbt);
        }));

        nbt.put("groups", groupsNbt);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach(((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putString("groupId", playerData.getGroupName());
            playerNbt.putString("username", playerData.getUsername());
            playersNbt.put(uuid.toString(), playerNbt);
        }));

        nbt.put("players", playersNbt);

        NbtCompound playerUuidsNbt = new NbtCompound();
        playerUuids.forEach(((username, uuid) -> playerUuidsNbt.putString(username, uuid.toString())));

        nbt.put("playerUuids", playerUuidsNbt);

        NbtCompound groupGlobalConfig = new NbtCompound();
        groupGlobalConfig.putInt("maxGroupNameLength", GroupFormat.MAX_GROUP_NAME_LENGTH);
        groupGlobalConfig.putInt("maxPrefixNameLength", GroupFormat.MAX_PREFIX_NAME_LENGTH);

        nbt.put("globalConfig", groupGlobalConfig);

        return nbt;
    }

    public static NbtManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtManager state = new NbtManager();

        NbtCompound groupsNbt = tag.getCompound("groups");
        groupsNbt.getKeys().forEach(key -> {
            GroupData groupData = new GroupData(
                    groupsNbt.getCompound(key).getString("name"),
                    groupsNbt.getCompound(key).getString("prefix"),
                    groupsNbt.getCompound(key).getInt("color"),
                    groupsNbt.getCompound(key).getInt("visibility"),
                    groupsNbt.getCompound(key).getCompound("players"),
                    groupsNbt.getCompound(key).getList("requests", NbtElement.STRING_TYPE)
            );
            state.groupList.put(key, groupData);
        });

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData(
                playersNbt.getCompound(key).getString("groupId"),
                playersNbt.getCompound(key).getString("username")
            );
            state.players.put(UUID.fromString(key), playerData);
        });

        NbtCompound playerUuidsNbt = tag.getCompound("playerUuids");
        playerUuidsNbt.getKeys().forEach(key -> state.playerUuids.put(key, UUID.fromString(playerUuidsNbt.getString(key))));

        NbtCompound globalConfig = tag.getCompound("globalConfig");
        int maxGroupNameNbt = globalConfig.getInt("maxGroupNameLength");
        int maxPrefixNameNbt = globalConfig.getInt("maxPrefixNameLength");
        GroupFormat.MAX_GROUP_NAME_LENGTH = (maxGroupNameNbt > 0) ? maxGroupNameNbt : 25;
        GroupFormat.MAX_PREFIX_NAME_LENGTH = (maxPrefixNameNbt > 0) ? maxPrefixNameNbt : 20;

        return state;
    }

    public static NbtManager getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        NbtManager state = persistentStateManager.getOrCreate(type, SimpleGroups.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        NbtManager serverState = getServerState(player.getWorld().getServer());
        String playerName = player.getName().getString();
        if (!serverState.playerUuids.containsKey(playerName) || !serverState.playerUuids.get(playerName).equals(player.getUuid())) {
            serverState.playerUuids.put(playerName, player.getUuid());
        }
        PlayerData playerData = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData(playerName));
        if (Objects.isNull(serverState.groupList.get(playerData.getGroupName()))) {
            playerData.leaveGroup();
        }
        if (!playerData.getUsername().equals(playerName)) {
            playerData.updateUsername(playerName);
        }
        return playerData;
    }

    public static PlayerData getPlayerState(UUID playerUuid, MinecraftServer server) {
        NbtManager serverState = getServerState(server);
        PlayerData playerData = serverState.players.computeIfAbsent(playerUuid, uuid -> {
            SimpleGroups.LOGGER.warn("Creating new PlayerData without a username. (MANAGERS.NBT.GETPLAYERSTATE");
            return new PlayerData("<UNKNOWN USERNAME>");
        });
        if (Objects.isNull(serverState.groupList.get(playerData.getGroupName()))) {
            playerData.leaveGroup();
        }
        return playerData;
    }
}
