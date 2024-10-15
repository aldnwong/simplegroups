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
import java.util.Random;
import java.util.UUID;

public class GroupManager extends PersistentState {
    public HashMap<String, GroupData> groupList = new HashMap<>();
    public HashMap<UUID, PlayerData> players = new HashMap<>();

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

            groupNbt.putString("name", groupData.name);
            groupNbt.putString("prefix", groupData.prefix);
            groupNbt.putBoolean("listed", groupData.listed);
            groupNbt.putBoolean("open", groupData.open);
            groupNbt.putInt("color", groupData.color);

            NbtCompound groupPlayersNbt = new NbtCompound();
            groupData.players.forEach((uuid -> {
                if (groupData.leader == uuid)
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

            playerNbt.putString("groupId", playerData.GroupId);

            playersNbt.put(uuid.toString(), playerNbt);
        }));

        nbt.put("players", playersNbt);

        return nbt;
    }

    public static GroupManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        GroupManager state = new GroupManager();

        NbtCompound groupsNbt = tag.getCompound("groups");
        groupsNbt.getKeys().forEach(key -> {
            GroupData groupData = new GroupData();

            groupData.name = groupsNbt.getCompound(key).getString("name");
            groupData.prefix = groupsNbt.getCompound(key).getString("prefix");
            groupData.listed = groupsNbt.getCompound(key).getBoolean("listed");
            groupData.open = groupsNbt.getCompound(key).getBoolean("open");
            groupData.color = groupsNbt.getCompound(key).getInt("color");

            NbtCompound groupPlayersNbt = groupsNbt.getCompound("players");
            groupPlayersNbt.getKeys().forEach(playerKey -> {
                UUID playerUUID = UUID.fromString(groupPlayersNbt.getString(playerKey));
                if (groupPlayersNbt.getString(playerKey).equals("LEADER")) {
                    groupData.leader = playerUUID;
                    groupData.players.add(playerUUID);
                } else {
                    groupData.players.add(playerUUID);
                }
            });

            state.groupList.put(key, groupData);
        });

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.GroupId = playersNbt.getCompound(key).getString("groupId");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
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
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }

    public static String generateGroupId(MinecraftServer server) {
        GroupManager serverState = getServerState(server);
        while(true) {
            StringBuilder id = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                Random rand = new Random();
                char randomChar = (char)('A' + rand.nextInt(26));

                id.append(randomChar);
            }
            if (!serverState.groupList.containsKey(id.toString()))
                return id.toString();
        }
    }
}
