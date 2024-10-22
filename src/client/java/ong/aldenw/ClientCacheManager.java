package ong.aldenw;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ong.aldenw.network.SyncDisplayNamePayload;
import ong.aldenw.network.UpdateDisplayNamePayload;

import java.util.HashMap;
import java.util.UUID;

public class ClientCacheManager {
    public static HashMap<UUID, String> playerPrefixDataHashMap = new HashMap<>();
    public static HashMap<UUID, Integer> playerColorDataHashMap = new HashMap<>();

    public static void initialSync(SyncDisplayNamePayload payload, ClientPlayNetworking.Context context) {
        playerPrefixDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.prefix());
        playerColorDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.color());
    }

    public static void update(UpdateDisplayNamePayload payload, ClientPlayNetworking.Context context) {
        playerPrefixDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.prefix());
        playerColorDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.color());
    }
}
