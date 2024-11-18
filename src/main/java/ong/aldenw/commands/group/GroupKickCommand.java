package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.SimpleGroups;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbIntFormat;
import ong.aldenw.managers.DataManager;

import java.util.Objects;
import java.util.UUID;

public class GroupKickCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbIntFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        DataManager state = DataManager.getServerState(server);
        PlayerEntity leader = context.getSource().getPlayer();
        PlayerData leaderState = DataManager.getPlayerState(leader);
        GroupData groupData = state.groupList.get(leaderState.getGroupName());
        UUID kickedPlayerUuid = state.playerUuids.get(StringArgumentType.getString(context, "player"));
        PlayerData kickedPlayerState = DataManager.getPlayerState(kickedPlayerUuid, server);

        if (!leaderState.isInAGroup()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not currently in a group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (!groupData.isLeader(leader.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to kick members from this group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (Objects.isNull(kickedPlayerUuid)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (!groupData.getPlayers().contains(kickedPlayerUuid)) {
            if (kickedPlayerState.getGroupName().equals(groupData.getName())) {
                kickedPlayerState.leaveGroup();
            }
            context.getSource().sendFeedback(() -> Text.literal("Player has been removed").formatted(Formatting.YELLOW), false);
            SimpleGroups.LOGGER.error("Player group data did not match server group data (GROUP.KICK)");
            return 1;
        }

        groupData.removePlayer(kickedPlayerUuid, server);
        return 1;
    }
}
