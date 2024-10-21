package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbFormat;

public class GroupCreateCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        ServerPlayerEntity player = context.getSource().getPlayer();
        String groupName = StringArgumentType.getString(context, "groupName");
        GroupData newGroupData = new GroupData();
        PlayerData playerState = GroupManager.getPlayerState(player);

        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }
        if (!state.players.get(player.getUuid()).groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are already in a group.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }
        if (state.groupList.containsKey(groupName)) {
            context.getSource().sendFeedback(() -> Text.literal("A group with this name already exists.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }
        if (groupName.length() > state.MAX_GROUP_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Name must be shorter than " + state.MAX_GROUP_NAME_LENGTH + " characters.").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }

        playerState.groupName = groupName;

        newGroupData.displayName = groupName;
        newGroupData.leader = player.getUuid();
        newGroupData.players.add(player.getUuid());

        state.groupList.put(groupName, newGroupData);

        context.getSource().sendFeedback(() -> Text.literal("Created new group "+groupName).withColor(RgbFormat.GOLD), false);
        return 1;
    }
}
