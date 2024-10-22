package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import ong.aldenw.CacheManager;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbFormat;

public class GroupJoinCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = GroupManager.getPlayerState(player);
        String groupName = StringArgumentType.getString(context, "groupName");
        GroupData groupState = state.groupList.get(groupName);

        if (!playerState.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are already in a group").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }
        if (!state.groupList.containsKey(groupName)) {
            context.getSource().sendFeedback(() -> Text.literal("Group not found").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }
        if (!groupState.listed) {
            context.getSource().sendFeedback(() -> Text.literal("This group is invite only").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }
        if (!groupState.open) {
            groupState.requests.add(player.getUuid());
            context.getSource().sendFeedback(() -> Text.literal("A join request has been sent to the group").withColor(RgbFormat.YELLOW), false);
            return 1;
        }

        groupState.players.add(player.getUuid());
        playerState.groupName = groupName;

        CacheManager.updateCache(groupState, context.getSource().getServer());

        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("You have joined ").withColor(RgbFormat.GOLD).append(Text.literal(groupName).withColor(groupState.color))), false);

        groupState.players.forEach(uuid -> {
            context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                    serverPlayer.sendMessage(Text.empty().append(Text.literal(player.getName().getString()).withColor(groupState.color)).append(Text.literal(" has joined the group").withColor(RgbFormat.GOLD)));
                }
            });
        });

        return 1;
    }
}
