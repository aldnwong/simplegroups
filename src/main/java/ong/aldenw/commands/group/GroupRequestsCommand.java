package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.GroupManager;
import ong.aldenw.commands.suggestions.RequestSuggestions;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.Objects;
import java.util.UUID;

public class GroupRequestsCommand {
    // TODO: Optimize mutual variable usage

    public static boolean checkExecuteRequirements(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return false;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity caller = context.getSource().getPlayer();
        PlayerData callerData = GroupManager.getPlayerState(caller);
        GroupData groupData = state.groupList.get(callerData.groupName);

        if (callerData.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not in a group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        if (!caller.getUuid().equals(groupData.getLeader())) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to view or modify requests for this group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        if (!groupData.hasRequests()) {
            context.getSource().sendFeedback(() -> Text.literal("There are no join requests").formatted(Formatting.DARK_RED), false);
            return false;
        }
        return true;
    }

    public static boolean checkArgumentRequirements(CommandContext<ServerCommandSource> context) {
        String playerArg = StringArgumentType.getString(context, "player");
        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity caller = context.getSource().getPlayer();
        PlayerData callerData = GroupManager.getPlayerState(caller);
        GroupData groupData = state.groupList.get(callerData.groupName);
        UUID playerUuid = state.playerUuids.get(playerArg);

        if (Objects.isNull(playerUuid) || !groupData.requestsContains(playerUuid)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found").formatted(Formatting.DARK_RED), false);
            return false;
        }

        return true;
    }

    public static int viewExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity caller = context.getSource().getPlayer();
        PlayerData callerData = GroupManager.getPlayerState(caller);
        GroupData groupData = state.groupList.get(callerData.groupName);

        if (!groupData.hasRequests()) {
            context.getSource().sendFeedback(() -> Text.literal("There are no join requests").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        int requestsSize = groupData.getRequestsSize();
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("\n").append(Text.literal("There " + ((requestsSize ==1) ? "is" : "are") + " " + requestsSize + " join " + ((requestsSize ==1) ? "request" : "requests") +":").formatted(Formatting.GOLD))), false);

        groupData.getRequests().forEach(uuid -> context.getSource().sendFeedback(() -> Text.literal(state.players.get(uuid).username).formatted(Formatting.YELLOW), false));

        return 1;
    }

    public static int acceptExecute(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        GroupManager state = GroupManager.getServerState(server);
        String playerArg = StringArgumentType.getString(context, "player");
        GroupData groupData = state.groupList.get(GroupManager.getPlayerState(context.getSource().getPlayer()).groupName);

        if (!checkExecuteRequirements(context))
            return 1;


        if (playerArg.equals(RequestSuggestions.ALL_REQUESTS_PARAMETER)) {
            context.getSource().sendFeedback(() -> Text.literal("Accepted all join requests").formatted(Formatting.GOLD), false);
            groupData.acceptAllRequests(server);
            return 1;
        }

        if (!checkArgumentRequirements(context))
            return 1;

        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Accepted ").formatted(Formatting.GOLD)).append(Text.literal(playerArg)).append(Text.literal("'s join request").formatted(Formatting.GOLD)), false);
        groupData.addPlayer(state.playerUuids.get(playerArg), server);

        return 1;
    }

    public static int denyExecute(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        GroupManager state = GroupManager.getServerState(server);
        GroupData groupData = state.groupList.get(GroupManager.getPlayerState(context.getSource().getPlayer()).groupName);
        String playerArg = StringArgumentType.getString(context, "player");
        if (!checkExecuteRequirements(context))
            return 1;

        if (playerArg.equals(RequestSuggestions.ALL_REQUESTS_PARAMETER)) {
            context.getSource().sendFeedback(() -> Text.literal("Removed all join requests").formatted(Formatting.GOLD), false);
            groupData.denyAllRequests(server);
            return 1;
        }

        if (!checkArgumentRequirements(context)) {
            return 1;
        }

        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Removed ").formatted(Formatting.GOLD)).append(Text.literal(playerArg)).append(Text.literal("'s join request").formatted(Formatting.GOLD)), false);
        groupData.denyRequest(state.playerUuids.get(playerArg), server);
        return 1;
    }
}
