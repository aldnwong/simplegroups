package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbIntFormat;
import ong.aldenw.managers.DataManager;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class GroupTransferCommand {
    public static HashMap<UUID, Long> transferRequests = new HashMap<>();
    public static final int TRANSFER_WINDOW_SECONDS = 15;

    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbIntFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        DataManager state = DataManager.getServerState(server);
        PlayerEntity oldLeader = context.getSource().getPlayer();
        PlayerData oldLeaderState = DataManager.getPlayerState(oldLeader);
        GroupData groupData = state.groupList.get(oldLeaderState.getGroupName());
        UUID newLeaderUuid = state.playerUuids.get(StringArgumentType.getString(context, "player"));

        if (!oldLeaderState.isInAGroup()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not currently in a group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if(oldLeader.getUuid().equals(newLeaderUuid)) {
            context.getSource().sendFeedback(() -> Text.literal("You cannot transfer to yourself").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (!groupData.isLeader(oldLeader.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to delete this group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (Objects.isNull(newLeaderUuid)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (!groupData.getPlayers().contains(newLeaderUuid)) {
            context.getSource().sendFeedback(() -> Text.literal("Player is not a group member").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        if (!transferRequests.containsKey(oldLeader.getUuid())) {
            context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Are you sure you want to transfer your group? This action is ").formatted(Formatting.GOLD)).append(Text.literal("irreversible").formatted(Formatting.DARK_RED, Formatting.UNDERLINE)), false);
            context.getSource().sendFeedback(() -> Text.literal("Run this command again within " + TRANSFER_WINDOW_SECONDS + " seconds to confirm.").formatted(Formatting.GOLD), false);
            transferRequests.put(oldLeader.getUuid(), System.currentTimeMillis() + (TRANSFER_WINDOW_SECONDS * 1000));
        }
        else {
            long windowEnd = transferRequests.get(oldLeader.getUuid());
            if (System.currentTimeMillis() > windowEnd) {
                context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Are you sure you want to transfer your group? This action is ").formatted(Formatting.GOLD)).append(Text.literal("irreversible").formatted(Formatting.DARK_RED, Formatting.UNDERLINE)), false);
                context.getSource().sendFeedback(() -> Text.literal("Run this command again within " + TRANSFER_WINDOW_SECONDS + " seconds to confirm.").formatted(Formatting.GOLD), false);
                transferRequests.put(oldLeader.getUuid(), System.currentTimeMillis() + (TRANSFER_WINDOW_SECONDS * 1000));
            }
            else {
                transferRequests.remove(oldLeader.getUuid());
                groupData.changeLeader(newLeaderUuid, context.getSource().getServer());
            }
        }

        return 1;
    }
}
