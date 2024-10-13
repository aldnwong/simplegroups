package ong.aldenw;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import ong.aldenw.persistentdata.PlayerGroupData;

import java.util.HashMap;
import java.util.UUID;

public class GroupManager extends PersistentState {
    public String currentGroup = "";
    public HashMap<UUID, PlayerGroupData> players = new HashMap<>();

    private static final Type<GroupManager> type = new Type<>(
            GroupManager::new,
            GroupManager::createFromNbt,
            null
    );

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("sgCurrentGroup", currentGroup);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach(((uuid, playerGroupData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putString("sgCurrentGroup", playerGroupData.GroupName);

            playersNbt.put(uuid.toString(), playerNbt);
        }));

        playersNbt.put("players", playersNbt);

        return nbt;
    }

    public static GroupManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        GroupManager state = new GroupManager();
        state.currentGroup = tag.getString("sgCurrentGroup");

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerGroupData playerData = new PlayerGroupData();

            playerData.GroupName = playersNbt.getCompound(key).getString("sgCurrentGroup");

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
}
