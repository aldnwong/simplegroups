package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.data.GroupData;
import ong.aldenw.managers.DataManager;

import java.util.Objects;

public class GroupInviteCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        DataManager state = DataManager.getServerState(server);
        PlayerEntity player = context.getSource().getPlayer();
        ServerPlayerEntity invitedPlayer = server.getPlayerManager().getPlayer(StringArgumentType.getString(context, "player"));
        GroupData groupData = state.groupList.get(DataManager.getPlayerState(player).getGroupName());

        if (!DataManager.getPlayerState(player).isInAGroup()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not in a group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (!groupData.isLeader(player.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to invite people to this group").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (Objects.isNull(invitedPlayer)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (groupData.getPlayers().contains(invitedPlayer.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("Player is already in the group").formatted(Formatting.YELLOW), false);
            return 1;
        }
        if (groupData.invited(invitedPlayer.getUuid())) {
            context.getSource().sendFeedback(() -> Text.literal("Player has already been invited").formatted(Formatting.YELLOW), false);
            return 1;
        }

        groupData.addInvite(invitedPlayer.getUuid());
        invitedPlayer.sendMessage(Text.empty().append(Text.literal("You have been invited to join ").formatted(Formatting.GOLD)).append(Text.literal(groupData.getName()).withColor(groupData.getColor())));
        invitedPlayer.sendMessage(Text.empty().append(Text.literal("Use ").formatted(Formatting.GOLD)).append("/group join \"" + groupData.getName() + "\"").append(Text.literal(" to join the group!").formatted(Formatting.GOLD)));
        context.getSource().sendFeedback(() -> Text.literal("Player has been invited").formatted(Formatting.GOLD), false);
        return 1;
    }
}
