package ong.aldenw;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import ong.aldenw.data.GroupData;
import ong.aldenw.formats.RgbFormat;
import ong.aldenw.network.UpdateDisplayNamePayload;
import ong.aldenw.network.SyncDisplayNamePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleGroups implements ModInitializer {
	public static final String MOD_ID = "simple-groups";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier INITIAL_SYNC = Identifier.of(MOD_ID, "initial_sync");
	public static final Identifier DISPLAY_NAME_UPDATE = Identifier.of(MOD_ID, "display_name_update");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(SyncDisplayNamePayload.ID, SyncDisplayNamePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateDisplayNamePayload.ID, UpdateDisplayNamePayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			GroupManager.getServerState(server).players.forEach((uuid, playerData) -> {
				if (playerData.groupName.isEmpty()) {
					SyncDisplayNamePayload data = new SyncDisplayNamePayload(uuid.toString(), "", RgbFormat.WHITE);
					server.execute(() -> {
						ServerPlayNetworking.send(handler.getPlayer(), data);
					});
				}
				else {
					GroupManager state = GroupManager.getServerState(server);
					GroupData groupData = state.groupList.get(playerData.groupName);

					SyncDisplayNamePayload data = new SyncDisplayNamePayload(uuid.toString(), groupData.prefix, groupData.color);
					server.execute(() -> {
						ServerPlayNetworking.send(handler.getPlayer(), data);
					});
				}
			});
		});

		CommandRegistrationCallback.EVENT.register(CommandManager::initialize);
		LOGGER.info("Simple Groups plugin initialized. :3");
	}
}