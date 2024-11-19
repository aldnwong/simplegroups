package ong.aldenw;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.managers.CommandManager;
import ong.aldenw.managers.DataManager;
import ong.aldenw.managers.NetworkManager;
import ong.aldenw.network.ClearCachePayload;
import ong.aldenw.network.UpdateColorPayload;
import ong.aldenw.network.UpdatePrefixPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SimpleGroups implements ModInitializer {
	public static final String MOD_ID = "simple-groups";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier UPDATE_PREFIX_CACHE = Identifier.of(MOD_ID, "update_prefix_cache");
	public static final Identifier UPDATE_COLOR_CACHE = Identifier.of(MOD_ID, "update_color_cache");
	public static final Identifier CLEAR_CACHE = Identifier.of(MOD_ID, "clear_cache");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(UpdatePrefixPayload.ID, UpdatePrefixPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateColorPayload.ID, UpdateColorPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ClearCachePayload.ID, ClearCachePayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			DataManager state = DataManager.getServerState(server);
			ServerPlayerEntity player = handler.getPlayer();
			PlayerData playerData = DataManager.getPlayerState(player);
			GroupData groupData = state.groupList.get(playerData.getGroupName());

			if (!Objects.isNull(groupData)) {
				if (!groupData.getPrefix().isEmpty())
					NetworkManager.updatePrefixCache(player.getUuid(), groupData.getPrefix(), server);

				NetworkManager.updateColorCache(player.getUuid(), groupData.getColor(), server);

				if (groupData.getLeader().equals(player.getUuid()) && groupData.hasRequests()) {
					int requestsSize = groupData.getRequestsSize();
					player.sendMessageToClient(Text.empty().append(Text.literal("Your group has ").formatted(Formatting.GOLD)).append(Text.literal(requestsSize + "").formatted(Formatting.AQUA)).append(Text.literal((requestsSize == 1) ? " join request!" : "join requests!").formatted(Formatting.GOLD)), false);
				}
			}

			NetworkManager.syncPlayerCache(player);
		});

		CommandRegistrationCallback.EVENT.register(CommandManager::initialize);
		LOGGER.info("Simple Groups plugin initialized.");
	}
}