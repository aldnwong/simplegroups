package ong.aldenw;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ong.aldenw.network.ClearCachePayload;
import ong.aldenw.network.UpdateColorPayload;
import ong.aldenw.network.UpdatePrefixPayload;

public class SimpleGroupsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(UpdatePrefixPayload.ID, ClientCacheManager::updatePrefixCache);
		ClientPlayNetworking.registerGlobalReceiver(UpdateColorPayload.ID, ClientCacheManager::updateColorCache);
		ClientPlayNetworking.registerGlobalReceiver(ClearCachePayload.ID, ClientCacheManager::clearCache);
	}
}