package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbIntFormat;

public class GroupCreateCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbIntFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = GroupManager.getPlayerState(player);
        String groupName = StringArgumentType.getString(context, "groupName");

        if (!state.players.get(player.getUuid()).groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are already in a group.").withColor(RgbIntFormat.fromThree(255, 0, 0)), false);
            return 1;
        }
        if (state.groupList.containsKey(groupName)) {
            context.getSource().sendFeedback(() -> Text.literal("A group with this name already exists.").withColor(RgbIntFormat.fromThree(255, 0, 0)), false);
            return 1;
        }
        if (groupName.length() < state.MIN_GROUP_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Name must be longer than " + state.MIN_GROUP_NAME_LENGTH + " characters.").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (groupName.length() > state.MAX_GROUP_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Name must be shorter than " + state.MAX_GROUP_NAME_LENGTH + " characters.").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        playerState.groupName = groupName;
        GroupData newGroupData = new GroupData(groupName, player.getUuid());
        state.groupList.put(groupName, newGroupData);

        context.getSource().sendFeedback(() -> Text.literal("Created new group "+groupName).formatted(Formatting.GOLD), false);
        return 1;
    }
}
