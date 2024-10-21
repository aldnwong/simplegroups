package ong.aldenw;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import ong.aldenw.network.SyncDisplayNamePayload;
import ong.aldenw.network.UpdateDisplayNamePayload;

import java.util.HashMap;
import java.util.UUID;

public class SimpleGroupsClient implements ClientModInitializer {

	public static HashMap<UUID, String> playerPrefixDataHashMap = new HashMap<>();
	public static HashMap<UUID, Integer> playerColorDataHashMap = new HashMap<>();

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncDisplayNamePayload.ID, (payload, context) -> {
			playerPrefixDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.prefix());
			playerColorDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.color());
		});
		ClientPlayNetworking.registerGlobalReceiver(UpdateDisplayNamePayload.ID, (payload, context) -> {
			playerPrefixDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.prefix());
			playerColorDataHashMap.put(UUID.fromString(payload.playerUuid()), payload.color());
		});
	}
}