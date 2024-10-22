package ong.aldenw;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ong.aldenw.network.SyncDisplayNamePayload;
import ong.aldenw.network.UpdateDisplayNamePayload;

public class SimpleGroupsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncDisplayNamePayload.ID, ClientCacheManager::initialSync);
		ClientPlayNetworking.registerGlobalReceiver(UpdateDisplayNamePayload.ID, ClientCacheManager::update);
	}
}