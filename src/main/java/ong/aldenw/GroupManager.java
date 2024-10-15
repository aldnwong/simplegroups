package ong.aldenw;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import ong.aldenw.data.Group;
import ong.aldenw.data.PlayerGroupData;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class GroupManager extends PersistentState {
    public HashMap<String, Group> groupList = new HashMap<>();
    public HashMap<UUID, PlayerGroupData> players = new HashMap<>();

    private static final Type<GroupManager> type = new Type<>(
            GroupManager::new,
            GroupManager::createFromNbt,
            null
    );

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound groupsNbt = new NbtCompound();
        groupList.forEach(((id, group) -> {
            NbtCompound groupNbt = new NbtCompound();

            groupNbt.putString("name", group.name);
            groupNbt.putString("prefix", group.prefix);
            groupNbt.putBoolean("listed", group.listed);
            groupNbt.putBoolean("open", group.open);
            groupNbt.putInt("color", group.color);

            NbtCompound groupPlayersNbt = new NbtCompound();
            group.players.forEach((uuid -> {
                if (group.leader == uuid)
                    groupPlayersNbt.putString(uuid.toString(), "LEADER");
                else
                    groupPlayersNbt.putString(uuid.toString(), "MEMBER");
            }));

            groupNbt.put("players", groupPlayersNbt);

            groupsNbt.put(id, groupNbt);
        }));

        nbt.put("groups", groupsNbt);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach(((uuid, playerGroupData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putString("groupId", playerGroupData.GroupId);

            playersNbt.put(uuid.toString(), playerNbt);
        }));

        nbt.put("players", playersNbt);

        return nbt;
    }

    public static GroupManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        GroupManager state = new GroupManager();

        NbtCompound groupsNbt = tag.getCompound("groups");
        groupsNbt.getKeys().forEach(key -> {
            Group group = new Group();

            group.name = groupsNbt.getCompound(key).getString("name");
            group.prefix = groupsNbt.getCompound(key).getString("prefix");
            group.listed = groupsNbt.getCompound(key).getBoolean("listed");
            group.open = groupsNbt.getCompound(key).getBoolean("open");
            group.color = groupsNbt.getCompound(key).getInt("color");

            NbtCompound groupPlayersNbt = groupsNbt.getCompound("players");
            groupPlayersNbt.getKeys().forEach(playerKey -> {
                UUID playerUUID = UUID.fromString(groupPlayersNbt.getString(playerKey));
                if (groupPlayersNbt.getString(playerKey).equals("LEADER")) {
                    group.leader = playerUUID;
                    group.players.add(playerUUID);
                } else {
                    group.players.add(playerUUID);
                }
            });

            state.groupList.put(key, group);
        });

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerGroupData playerData = new PlayerGroupData();

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

    public static PlayerGroupData getPlayerState(LivingEntity player) {
        GroupManager serverState = getServerState(player.getWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerGroupData());
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

    public static int getTextColorFromRGB(int red, int green, int blue) {
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return rgb;
    }
}
