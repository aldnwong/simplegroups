package ong.aldenw;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ong.aldenw.network.ClearCachePayload;
import ong.aldenw.network.UpdateColorPayload;
import ong.aldenw.network.UpdatePrefixPayload;

import java.util.HashMap;
import java.util.UUID;

public class ClientCacheManager {
    public static HashMap<UUID, String> playerPrefixDataHashMap = new HashMap<>();
    public static HashMap<UUID, Integer> playerColorDataHashMap = new HashMap<>();

    public static void updatePrefixCache(UpdatePrefixPayload payload, ClientPlayNetworking.Context context) {
        playerPrefixDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.prefix());
    }

    public static void updateColorCache(UpdateColorPayload payload, ClientPlayNetworking.Context context) {
        playerColorDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.color());
    }

    public static void clearCache(ClearCachePayload payload, ClientPlayNetworking.Context context) {
        playerPrefixDataHashMap.remove(UUID.fromString(payload.playerUuid()));
        playerColorDataHashMap.remove(UUID.fromString(payload.playerUuid()));
    }
}
