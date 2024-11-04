package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.formats.GroupFormat;
import ong.aldenw.managers.NbtManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbIntFormat;

public class GroupConfigCommand {
    public static boolean checkExecuteRequirements(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return false;
        }

        NbtManager state = NbtManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();

        if (NbtManager.getPlayerState(player).groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not in a group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        if (!player.getUuid().equals(state.groupList.get(NbtManager.getPlayerState(player).groupName).getLeader())) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to edit this group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        return true;
    }

    public static int nameExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        NbtManager state = NbtManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = NbtManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        String newName = StringArgumentType.getString(context, "name");
        String oldName = groupData.getName();

        if (newName.equals(oldName)) {
            context.getSource().sendFeedback(() -> Text.literal("Your group already has that name").formatted(Formatting.YELLOW), false);
            return 1;
        }
        if (state.groupList.containsKey(newName)) {
            context.getSource().sendFeedback(() -> Text.literal("A group with this name exists already").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (newName.length() < GroupFormat.MIN_GROUP_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Name must be longer than " + GroupFormat.MIN_GROUP_NAME_LENGTH + " characters.").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (newName.length() > GroupFormat.MAX_GROUP_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Name must be shorter than " + GroupFormat.MAX_GROUP_NAME_LENGTH + " characters").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        groupData.changeName(newName, context.getSource().getServer());
        return 1;
    }

    public static int prefixExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        NbtManager state = NbtManager.getServerState(server);
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = NbtManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        String newPrefix = StringArgumentType.getString(context, "prefix");

        if (newPrefix.equals(groupData.getPrefix())) {
            if (groupData.getPrefix().isEmpty())
                context.getSource().sendFeedback(() -> Text.literal("Your group's prefix is already empty").formatted(Formatting.YELLOW), false);
            else
                context.getSource().sendFeedback(() -> Text.literal("Your group already has that prefix").formatted(Formatting.YELLOW), false);
            return 1;
        }
        if (newPrefix.length() > GroupFormat.MAX_PREFIX_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Prefix must be shorter than " + GroupFormat.MAX_PREFIX_NAME_LENGTH + " characters").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (GroupFormat.isPrefixInUse(newPrefix, state)) {
            context.getSource().sendFeedback(() -> Text.literal("A different group is already using that prefix").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        groupData.changePrefix(newPrefix, server);
        return 1;
    }

    public static int colorExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        NbtManager state = NbtManager.getServerState(server);
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = NbtManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        int r = IntegerArgumentType.getInteger(context, "r");
        int g = IntegerArgumentType.getInteger(context, "g");
        int b = IntegerArgumentType.getInteger(context, "b");
        int rgb = RgbIntFormat.fromThree(r, g, b);

        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            context.getSource().sendFeedback(() -> Text.literal("Invalid color. Values must be from 0-255").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (groupData.getColor() == rgb) {
            context.getSource().sendFeedback(() -> Text.literal("Your group already has this color").formatted(Formatting.YELLOW), false);
            return 1;
        }

        groupData.changeColor(rgb, server);
        return 1;
    }

    public static int joinOptionExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        MinecraftServer server = context.getSource().getServer();
        NbtManager state = NbtManager.getServerState(server);
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = NbtManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);

        switch (StringArgumentType.getString(context, "joinOption")) {
            case "byInviteOnly":
                if (groupData.getVisibility() == 0)
                    context.getSource().sendFeedback(() -> Text.literal("Your group is already set to invite only").formatted(Formatting.YELLOW), false);
                else
                    groupData.setVisibility(0, server);
                break;

            case "anyoneCanRequest":
                if (groupData.getVisibility() == 1)
                    context.getSource().sendFeedback(() -> Text.literal("Your group is already set to request and invite only").formatted(Formatting.YELLOW), false);
                else
                    groupData.setVisibility(1, server);
                break;

            case "anyoneCanJoin":
                if (groupData.getVisibility() == 2)
                    context.getSource().sendFeedback(() -> Text.literal("Your group is already publicly joinable").formatted(Formatting.YELLOW), false);
                else
                    groupData.setVisibility(2, server);
                break;

            default:
                context.getSource().sendFeedback(() -> Text.literal("Invalid option").formatted(Formatting.DARK_RED), false);
                break;
        }

        return 1;
    }
}
