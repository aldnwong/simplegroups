package ong.aldenw;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ong.aldenw.data.PlayerData;
import ong.aldenw.network.SyncPayload;
import ong.aldenw.network.UpdatePayload;

public class SimpleGroupsClient implements ClientModInitializer {

	public static PlayerData playerData = new PlayerData();

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			playerData.groupName = payload.groupName();
			client.execute(() -> {
				if (GroupManager.getServerState(context.client().getServer()).groupList.containsKey(payload.groupName())) {
					int color = GroupManager.getServerState(context.client().getServer()).groupList.get(payload.groupName()).color;
					client.player.sendMessage(Text.literal("Group sync received: Group " + payload.groupName()).withColor(color));
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(UpdatePayload.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			playerData.groupName = payload.groupName();
			client.execute(() -> {
				if (GroupManager.getServerState(context.client().getServer()).groupList.containsKey(payload.groupName())) {
					int color = GroupManager.getServerState(context.client().getServer()).groupList.get(payload.groupName()).color;
					client.player.sendMessage(Text.literal("Group sync received: Group " + payload.groupName()).withColor(color));
				}
			});
		});
	}
}