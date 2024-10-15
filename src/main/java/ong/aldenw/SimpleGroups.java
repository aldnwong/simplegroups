package ong.aldenw;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import ong.aldenw.data.PlayerData;
import ong.aldenw.network.UpdatePayload;
import ong.aldenw.network.SyncPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleGroups implements ModInitializer {
	public static final String MOD_ID = "simple-groups";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier INITIAL_SYNC = Identifier.of(MOD_ID, "initial_sync");
	public static final Identifier GROUP_UPDATE = Identifier.of(MOD_ID, "group_update");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(SyncPayload.ID, SyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdatePayload.ID, UpdatePayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PlayerData playerState = GroupManager.getPlayerState(handler.getPlayer());
			SyncPayload data = new SyncPayload(playerState.GroupId);

			server.execute(() -> {
				ServerPlayNetworking.send(handler.getPlayer(), data);
			});
		});

		CommandRegistrationCallback.EVENT.register(CommandManager::initialize);
		LOGGER.info("Simple Groups plugin initialized. :3");
	}
}