package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.NetworkManager;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbIntFormat;

public class GroupConfigCommand {
    public static boolean checkExecuteRequirements(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players").formatted(Formatting.DARK_RED), false);
            return false;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);

        if (playerData.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not in a group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        if (!player.getUuid().equals(groupData.leader)) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to edit this group").formatted(Formatting.DARK_RED), false);
            return false;
        }
        return true;
    }

    public static int nameExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        String newName = StringArgumentType.getString(context, "name");
        String oldName = playerData.groupName;

        if (newName.equals(oldName)) {
            context.getSource().sendFeedback(() -> Text.literal("Your group already has that name").formatted(Formatting.YELLOW), false);
            return 1;
        }
        if (state.groupList.containsKey(newName)) {
            context.getSource().sendFeedback(() -> Text.literal("A group with this name exists already").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (newName.length() > state.MAX_GROUP_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Name must be shorter than " + state.MAX_GROUP_NAME_LENGTH + " characters").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        state.groupList.put(newName, groupData);
        state.groupList.remove(oldName);
        groupData.players.forEach(uuid -> {
            state.players.get(uuid).groupName = newName;
        });

        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Changed ").formatted(Formatting.GOLD)).append(Text.literal(oldName).withColor(groupData.color)).append(Text.literal("'s name to ").formatted(Formatting.GOLD)).append(Text.literal(newName).withColor(groupData.color)), false);

        groupData.players.forEach(uuid -> {
            context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                    serverPlayer.sendMessage(Text.empty().append(Text.literal("Your group's name changed from ").formatted(Formatting.GOLD)).append(Text.literal(oldName).withColor(groupData.color)).append(Text.literal(" to ").formatted(Formatting.GOLD)).append(Text.literal(newName).withColor(groupData.color)));
                }
            });
        });
        
        return 1;
    }

    public static int prefixExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        String newPrefix = StringArgumentType.getString(context, "prefix");
        String oldPrefix = groupData.prefix;

        if (newPrefix.equals(oldPrefix)) {
            if (oldPrefix.isEmpty())
                context.getSource().sendFeedback(() -> Text.literal("Your group's prefix is already empty").formatted(Formatting.YELLOW), false);
            else
                context.getSource().sendFeedback(() -> Text.literal("Your group already has that prefix").formatted(Formatting.YELLOW), false);
            return 1;
        }
        if (newPrefix.length() > state.MAX_PREFIX_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Prefix must be shorter than " + state.MAX_PREFIX_NAME_LENGTH + " characters").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (state.getPrefixArray().contains(newPrefix)) {
            context.getSource().sendFeedback(() -> Text.literal("A different group is already using that prefix").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        groupData.prefix = newPrefix;
        if (oldPrefix.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Changed prefix to ").formatted(Formatting.GOLD)).append(Text.literal(newPrefix).withColor(groupData.color)), false);

            groupData.players.forEach(uuid -> {
                context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                    if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                        serverPlayer.sendMessage(Text.empty().append(Text.literal("Your group's prefix has changed to ").formatted(Formatting.GOLD)).append(Text.literal(newPrefix).withColor(groupData.color)));
                    }
                });
            });
        }
        else {
            context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Changed prefix from ").formatted(Formatting.GOLD)).append(Text.literal(oldPrefix).withColor(groupData.color)).append(Text.literal(" to ").formatted(Formatting.GOLD)).append(Text.literal(newPrefix).withColor(groupData.color)), false);

            groupData.players.forEach(uuid -> {
                context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                    if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                        serverPlayer.sendMessage(Text.empty().append(Text.literal("Your group's prefix has changed from ").formatted(Formatting.GOLD)).append(Text.literal(oldPrefix).withColor(groupData.color)).append(Text.literal(" to ").formatted(Formatting.GOLD)).append(Text.literal(newPrefix).withColor(groupData.color)));
                    }
                });
            });
        }

        NetworkManager.updateCache(groupData, context.getSource().getServer());

        return 1;
    }

    public static int colorExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        int r = IntegerArgumentType.getInteger(context, "r");
        int g = IntegerArgumentType.getInteger(context, "g");
        int b = IntegerArgumentType.getInteger(context, "b");
        int newColor = RgbIntFormat.fromThree(r, g, b);
        int oldColor = groupData.color;


        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            context.getSource().sendFeedback(() -> Text.literal("Invalid color. Values must be from 0-255").formatted(Formatting.DARK_RED), false);
            return 1;
        }
        if (groupData.color == RgbIntFormat.fromThree(r, g, b)) {
            context.getSource().sendFeedback(() -> Text.literal("Your group already has this color").formatted(Formatting.YELLOW), false);
            return 1;
        }

        groupData.color = RgbIntFormat.fromThree(r, g, b);
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Changed color from ").formatted(Formatting.GOLD)).append(Text.literal("this").withColor(oldColor)).append(Text.literal(" to ").formatted(Formatting.GOLD)).append(Text.literal("this").withColor(newColor)), false);

        groupData.players.forEach(uuid -> {
            context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                    serverPlayer.sendMessage(Text.empty().append(Text.literal("Your group's color has changed from ").formatted(Formatting.GOLD)).append(Text.literal("this").withColor(oldColor)).append(Text.literal(" to ").formatted(Formatting.GOLD)).append(Text.literal("this").withColor(newColor)));
                }
            });
        });

        NetworkManager.updateCache(groupData, context.getSource().getServer());

        return 1;
    }

    public static int joinOptionExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);
        String option = StringArgumentType.getString(context, "joinOption");

        switch (option) {
            case "byInviteOnly":
                if (!groupData.listed && !groupData.open) {
                    context.getSource().sendFeedback(() -> Text.literal("Your group is already set to invite only").formatted(Formatting.YELLOW), false);
                }
                else {
                    groupData.listed = false;
                    groupData.open = false;
                    context.getSource().sendFeedback(() -> Text.literal("Your group is now invite only").formatted(Formatting.GOLD), false);
                }
                break;
            case "anyoneCanRequest":
                if (groupData.listed && !groupData.open) {
                    context.getSource().sendFeedback(() -> Text.literal("Your group is already set to request and invite only").formatted(Formatting.YELLOW), false);
                }
                else {
                    groupData.listed = true;
                    groupData.open = false;
                    context.getSource().sendFeedback(() -> Text.literal("Your group is now request and invite only").formatted(Formatting.GOLD), false);
                }
                break;
            case "anyoneCanJoin":
                if (groupData.listed && groupData.open) {
                    context.getSource().sendFeedback(() -> Text.literal("Your group is already publicly joinable").formatted(Formatting.YELLOW), false);
                }
                else {
                    groupData.listed = true;
                    groupData.open = true;
                    context.getSource().sendFeedback(() -> Text.literal("Your group is now publicly joinable").formatted(Formatting.GOLD), false);
                    if (!groupData.requests.isEmpty()) {
                        if (groupData.requests.size() == 1)
                            context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("There is 1 request to join the group.").formatted(Formatting.GRAY)).append(Text.literal("Would you like to add these players to the group?").formatted(Formatting.GOLD)), false);
                        else
                            context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("There are " + groupData.requests.size() + " requests to join the group.").formatted(Formatting.GRAY)).append(Text.literal(" Would you like to add these players to the group?").formatted(Formatting.GOLD)), false);
                        context.getSource().sendFeedback(() -> Text.literal("[YES]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group requests accept @all"))).formatted(Formatting.GREEN), false);
                        context.getSource().sendFeedback(() -> Text.literal("[NO]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group requests deny @all"))).formatted(Formatting.RED), false);
                    }
                }
                break;
            default:
                context.getSource().sendFeedback(() -> Text.literal("Invalid option").formatted(Formatting.DARK_RED), false);
                break;
        }
        return 1;
    }
}
