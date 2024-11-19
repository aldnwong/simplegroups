package ong.aldenw.commands.group;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbIntFormat;
import ong.aldenw.managers.DataManager;
import ong.aldenw.managers.NetworkManager;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


public class GroupDeleteCommand {
    public static HashMap<String, Long> deleteRequests = new HashMap<>();
    public static final int DELETE_WINDOW_SECONDS = 15;

    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbIntFormat.fromThree(255, 0, 0)), false);
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
        if (!groupData.isLeader(player.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to delete this group").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        if (!deleteRequests.containsKey(groupData.getName())) {
            context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Are you sure you want to delete your group? This action is ").formatted(Formatting.GOLD)).append(Text.literal("irreversible").formatted(Formatting.DARK_RED, Formatting.UNDERLINE)), false);
            context.getSource().sendFeedback(() -> Text.literal("Run this command again within " + DELETE_WINDOW_SECONDS + " seconds to confirm.").formatted(Formatting.GOLD), false);
            deleteRequests.put(groupData.getName(), System.currentTimeMillis() + (DELETE_WINDOW_SECONDS * 1000));
        }
        else {
            long windowEnd = deleteRequests.get(groupData.getName());
            if (System.currentTimeMillis() > windowEnd) {
                context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Are you sure you want to delete your group? This action is ").formatted(Formatting.GOLD)).append(Text.literal("irreversible").formatted(Formatting.DARK_RED, Formatting.UNDERLINE)), false);
                context.getSource().sendFeedback(() -> Text.literal("Run this command again within " + DELETE_WINDOW_SECONDS + " seconds to confirm.").formatted(Formatting.GOLD), false);
                deleteRequests.put(groupData.getName(), System.currentTimeMillis() + (DELETE_WINDOW_SECONDS * 1000));
            }
            else {
                deleteRequests.remove(groupData.getName());
                groupData.deleteGroup(server);
            }
        }

        return 1;
    }
}
