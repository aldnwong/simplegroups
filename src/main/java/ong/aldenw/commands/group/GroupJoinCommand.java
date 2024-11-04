package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.managers.NbtManager;
import ong.aldenw.SimpleGroups;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

public class GroupJoinCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        NbtManager state = NbtManager.getServerState(server);
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = NbtManager.getPlayerState(player);
        String groupName = StringArgumentType.getString(context, "groupName");
        GroupData groupState = state.groupList.get(groupName);

        if (!playerState.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are already in a group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (!state.groupList.containsKey(groupName)) {
            context.getSource().sendFeedback(() -> Text.literal("Group not found").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        switch (groupState.getVisibility()) {
            case 0:
                context.getSource().sendFeedback(() -> Text.literal("This group is invite only").formatted(Formatting.DARK_RED), false);
                return 1;
            case 1:
                if (groupState.requestsContains(player.getUuid())) {
                    context.getSource().sendFeedback(() -> Text.literal("You have already requested to join this group").formatted(Formatting.YELLOW), false);
                } else {
                    groupState.addRequest(player.getUuid(), server);
                    context.getSource().sendFeedback(() -> Text.literal("A join request has been sent to the group").formatted(Formatting.YELLOW), false);
                }
                return 1;
            case 2:
                groupState.addPlayer(player.getUuid(), server);
                return 1;
            default:
                SimpleGroups.LOGGER.error("Switch entered default case (GROUP.JOIN.EXECUTE)");
                return 1;
        }
    }
}
