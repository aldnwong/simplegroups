package ong.aldenw.managers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import ong.aldenw.network.ClearCachePayload;
import ong.aldenw.network.UpdateColorPayload;
import ong.aldenw.network.UpdatePrefixPayload;

import java.util.HashMap;
import java.util.UUID;

public class NetworkManager {
    public static HashMap<UUID, String> playerPrefixDataHashMap = new HashMap<>();
    public static HashMap<UUID, Integer> playerColorDataHashMap = new HashMap<>();

    public static void updatePrefixCache(UUID playerUuid, String prefix, MinecraftServer server) {
        playerPrefixDataHashMap.put(playerUuid, prefix);
        server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
            ServerPlayNetworking.send(serverPlayer, new UpdatePrefixPayload(playerUuid.toString(), prefix));
        });
    }

    public static void updateColorCache(UUID playerUuid, int color, MinecraftServer server) {
        playerColorDataHashMap.put(playerUuid, color);
        server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
            ServerPlayNetworking.send(serverPlayer, new UpdateColorPayload(playerUuid.toString(), color));
        });
    }

    public static void clearCache(UUID playerUuid, MinecraftServer server) {
        playerColorDataHashMap.remove(playerUuid);
        playerPrefixDataHashMap.remove(playerUuid);
        server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
            ServerPlayNetworking.send(serverPlayer, new ClearCachePayload(playerUuid.toString()));
        });
    }

    public static void syncPlayerCache(ServerPlayerEntity player) {
        playerPrefixDataHashMap.forEach((uuid, prefix) -> ServerPlayNetworking.send(player, new UpdatePrefixPayload(uuid.toString(), prefix)));
        playerColorDataHashMap.forEach((uuid, color) -> ServerPlayNetworking.send(player, new UpdateColorPayload(uuid.toString(), color)));
    }
}
