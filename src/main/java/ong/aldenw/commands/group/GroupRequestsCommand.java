package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.Objects;
import java.util.UUID;

public class GroupRequestsCommand {
    public static boolean checkExecuteRequirements(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return false;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);

        if (playerData.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not in a group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        if (!player.getUuid().equals(groupData.leader)) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to view or modify requests for this group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        return true;
    }

    public static int viewExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);

        if (groupData.requests.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("There are no join requests").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        int requestsSize = groupData.requests.size();
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("\n").append(Text.literal("There " + ((requestsSize ==1) ? "is" : "are") + " " + requestsSize + " join " + ((requestsSize ==1) ? "request" : "requests") +":").formatted(Formatting.GOLD))), false);

        groupData.requests.forEach(uuid -> context.getSource().sendFeedback(() -> Text.literal(state.players.get(uuid).username).formatted(Formatting.YELLOW), false));

        return 1;
    }
    public static int acceptExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        String playerArg = StringArgumentType.getString(context, "player");
        UUID playerUuid = state.playerUuids.get(playerArg);

        if (Objects.isNull(playerUuid) || !groupData.requests.contains(playerUuid)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        groupData.requests.remove(playerUuid);
        groupData.players.add(playerUuid);
        state.players.get(playerUuid).groupName = groupData.name;

        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Added ").formatted(Formatting.GOLD)).append(Text.literal(playerArg).withColor(groupData.color)).append(Text.literal(" to ").formatted(Formatting.GOLD).append(Text.literal(groupData.name).withColor(groupData.color))), false);

        return 1;
    }
    public static int denyExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        String playerArg = StringArgumentType.getString(context, "player");
        UUID playerUuid = state.playerUuids.get(playerArg);

        if (Objects.isNull(playerUuid) || !groupData.requests.contains(playerUuid)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        groupData.requests.remove(playerUuid);

        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Removed ").formatted(Formatting.GOLD)).append(Text.literal(playerArg)).append(Text.literal("'s join request").formatted(Formatting.GOLD)), false);

        return 1;
    }
}
