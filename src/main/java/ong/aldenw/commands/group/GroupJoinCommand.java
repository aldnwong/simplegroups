package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.NetworkManager;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

public class GroupJoinCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = GroupManager.getPlayerState(player);
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
        if (!groupState.listed) {
            context.getSource().sendFeedback(() -> Text.literal("This group is invite only").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (!groupState.open) {
            if (groupState.requests.contains(player.getUuid())) {
                context.getSource().sendFeedback(() -> Text.literal("You have already requested to join this group").formatted(Formatting.YELLOW), false);
                return 1;
            }
            else {
                groupState.requests.add(player.getUuid());
                context.getSource().sendFeedback(() -> Text.literal("A join request has been sent to the group").formatted(Formatting.YELLOW), false);
                context.getSource().getServer().getPlayerManager().getPlayerList().forEach(playerInList -> {
                    if (playerInList.getUuid().equals(groupState.leader)) {
                        playerInList.sendMessage(Text.empty().append(Text.literal(player.getName().getString())).append(Text.literal(" has requested to join your group").formatted(Formatting.YELLOW)));
                    }
                });
                return 1;
            }
        }

        groupState.addPlayer(player.getUuid(), state);
        NetworkManager.updateCache(groupState, context.getSource().getServer());
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("You have joined ").formatted(Formatting.GOLD).append(Text.literal(groupName).withColor(groupState.color))), false);

        groupState.players.forEach(uuid -> {
            context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                    serverPlayer.sendMessage(Text.empty().append(Text.literal(player.getName().getString()).withColor(groupState.color)).append(Text.literal(" has joined the group").formatted(Formatting.GOLD)));
                }
            });
        });

        return 1;
    }
}
