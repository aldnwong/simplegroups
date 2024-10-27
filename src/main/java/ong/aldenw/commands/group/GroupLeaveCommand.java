package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.GroupManager;
import ong.aldenw.NetworkManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.Objects;

public class GroupLeaveCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = GroupManager.getPlayerState(player);

        if (playerState.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not currently in a group").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        GroupData groupData = state.groupList.get(playerState.groupName);

        if (Objects.isNull(groupData)) {
            context.getSource().sendFeedback(() -> Text.literal("Left group").formatted(Formatting.YELLOW), false);
            playerState.groupName = "";
            return 1;
        }
        if (groupData.leader.equals(player.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("You are group leader. Transfer ownership before leaving or delete the group").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        groupData.removePlayer(player.getUuid(), state);
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Left ").formatted(Formatting.YELLOW).append(Text.literal(groupData.name).withColor(groupData.color))).formatted(Formatting.YELLOW), false);

        return 1;
    }
}
