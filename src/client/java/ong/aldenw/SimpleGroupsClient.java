package ong.aldenw;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import ong.aldenw.data.PlayerGroupData;
import ong.aldenw.network.GroupSyncPayload;
import ong.aldenw.network.GroupUpdatePayload;

public class SimpleGroupsClient implements ClientModInitializer {

	public static PlayerGroupData playerData = new PlayerGroupData();

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(GroupSyncPayload.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			playerData.GroupId = payload.groupId();
			client.execute(() -> {
				if (GroupManager.getServerState(context.client().getServer()).groupList.containsKey(payload.groupId())) {
					int color = GroupManager.getServerState(context.client().getServer()).groupList.get(payload.groupId()).color;
					client.player.sendMessage(Text.literal("Group sync received: Group " + payload.groupId()).withColor(color));
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(GroupUpdatePayload.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			playerData.GroupId = payload.groupId();
			client.execute(() -> {
				if (GroupManager.getServerState(context.client().getServer()).groupList.containsKey(payload.groupId())) {
					int color = GroupManager.getServerState(context.client().getServer()).groupList.get(payload.groupId()).color;
					client.player.sendMessage(Text.literal("Group sync received: Group " + payload.groupId()).withColor(color));
				}
			});
		});
	}
}