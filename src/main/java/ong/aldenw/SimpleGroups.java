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
import ong.aldenw.network.UpdateDisplayNamePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class SimpleGroups implements ModInitializer {
	public static final String MOD_ID = "simple-groups";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier DISPLAY_NAME_UPDATE = Identifier.of(MOD_ID, "display_name_update");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(UpdateDisplayNamePayload.ID, UpdateDisplayNamePayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			NetworkManager.updateCache(player, server);
			NetworkManager.updatePlayerCache(player, GroupManager.getServerState(server));

			GroupManager state = GroupManager.getServerState(server);
			GroupData groupData = state.groupList.get(state.players.get(player.getUuid()).groupName);

			if (!Objects.isNull(groupData) && groupData.getLeader().equals(player.getUuid())) {
				if (groupData.hasRequests()) {
					int requestsSize = groupData.getRequestsSize();
					player.sendMessage(Text.empty().append(Text.literal("Your group has ").formatted(Formatting.GOLD)).append(Text.literal(requestsSize + "").formatted(Formatting.AQUA)).append(Text.literal((requestsSize == 1) ? " join request!" : "join requests!").formatted(Formatting.GOLD)));
				}
			}
		});

		CommandRegistrationCallback.EVENT.register(CommandManager::initialize);
		LOGGER.info("Simple Groups plugin initialized. :3");
	}
}