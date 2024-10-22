package ong.aldenw;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import ong.aldenw.data.GroupData;
import ong.aldenw.network.SyncDisplayNamePayload;
import ong.aldenw.network.UpdateDisplayNamePayload;

import java.util.HashMap;
import java.util.UUID;

public class CacheManager {
    public static HashMap<UUID, String> playerPrefixDataHashMap = new HashMap<>();
    public static HashMap<UUID, Integer> playerColorDataHashMap = new HashMap<>();

    public static void createCache(GroupManager state) {
        state.groupList.forEach((name, groupData) -> {
            groupData.players.forEach(uuid -> {
                playerPrefixDataHashMap.put(uuid, groupData.prefix);
                playerColorDataHashMap.put(uuid, groupData.color);
            });
        });
    }

    public static void updateCache(GroupData groupData, MinecraftServer server) {
        groupData.players.forEach(uuid -> {
            playerPrefixDataHashMap.put(uuid, groupData.prefix);
            playerColorDataHashMap.put(uuid, groupData.color);

            server.getPlayerManager().getPlayerList().forEach(player -> {
                ServerPlayNetworking.send(player, new UpdateDisplayNamePayload(uuid.toString(), groupData.prefix, groupData.color));
            });
        });
    }

    public static void updatePlayerCache(ServerPlayerEntity player, GroupManager state) {
        state.groupList.forEach((name, groupData) -> {
            groupData.players.forEach(uuid -> {
                ServerPlayNetworking.send(player, new SyncDisplayNamePayload(uuid.toString(), groupData.prefix, groupData.color));
            });
        });
    }
}
