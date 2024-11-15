package ong.aldenw.commands.group;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.managers.DataManager;
import ong.aldenw.SimpleGroups;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.Objects;

public class GroupLeaveCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        DataManager state = DataManager.getServerState(server);
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = DataManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerState.getGroupName());

        if (!playerState.isInAGroup()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not currently in a group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (Objects.isNull(groupData)) {
            context.getSource().sendFeedback(() -> Text.literal("Left group").formatted(Formatting.YELLOW), false);
            playerState.leaveGroup();
            SimpleGroups.LOGGER.warn("Player is in group that doesn't exist (GROUP.LEAVE.EXECUTE)");
            return 1;
        }
        if (groupData.getLeader().equals(player.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("You are group leader. Transfer ownership before leaving or delete the group").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        groupData.removePlayer(player.getUuid(), server);
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Left ").formatted(Formatting.YELLOW).append(Text.literal(groupData.getName()).withColor(groupData.getColor()))).formatted(Formatting.YELLOW), false);
        return 1;
    }
}
