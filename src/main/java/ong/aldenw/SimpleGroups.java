package ong.aldenw;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import ong.aldenw.network.UpdateDisplayNamePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleGroups implements ModInitializer {
	public static final String MOD_ID = "simple-groups";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier DISPLAY_NAME_UPDATE = Identifier.of(MOD_ID, "display_name_update");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(UpdateDisplayNamePayload.ID, UpdateDisplayNamePayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CacheManager.updatePlayerCache(handler.getPlayer(), GroupManager.getServerState(server)));

		CommandRegistrationCallback.EVENT.register(CommandManager::initialize);
		LOGGER.info("Simple Groups plugin initialized. :3");
	}
}