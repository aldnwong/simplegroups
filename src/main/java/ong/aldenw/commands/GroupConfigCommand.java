package ong.aldenw.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbFormat;
import ong.aldenw.network.UpdateDisplayNamePayload;

import static ong.aldenw.GroupManager.MAX_GROUP_NAME_LENGTH;

public class GroupConfigCommand {
    public static boolean checkExecuteRequirements(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbFormat.DARK_RED), false);
            return false;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = GroupManager.getPlayerState(player);
        GroupData groupState = state.groupList.get(playerState.groupName);
        if (playerState.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are not in a group.").withColor(RgbFormat.DARK_RED), false);
            return false;
        }
        if (!player.getUuid().equals(groupState.leader)) {
            context.getSource().sendFeedback(() -> Text.literal("You do not have permission to edit this group.").withColor(RgbFormat.DARK_RED), false);
            return false;
        }
        return true;
    }

    public static void updateClientDisplayNames(MinecraftServer server, GroupData groupData) {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            String prefix = groupData.prefix;
            int color = groupData.color;

            groupData.players.forEach(uuid -> {
                UpdateDisplayNamePayload data = new UpdateDisplayNamePayload(uuid.toString(), prefix, color);

                server.execute(() -> {
                    ServerPlayNetworking.send(player, data);
                });
            });
        });
    }

    public static int nameExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = GroupManager.getPlayerState(player);
        GroupData groupState = state.groupList.get(playerState.groupName);
        String newName = StringArgumentType.getString(context, "name");
        String oldName = playerState.groupName;

        if (newName.equals(oldName)) {
            context.getSource().sendFeedback(() -> Text.literal(".").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }
        if (state.groupList.containsKey(newName)) {
            context.getSource().sendFeedback(() -> Text.literal("A group with this name exists already.").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }
        if (newName.length() > MAX_GROUP_NAME_LENGTH) {
            context.getSource().sendFeedback(() -> Text.literal("Name must be shorter than " + MAX_GROUP_NAME_LENGTH + " characters.").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }

        state.groupList.put(newName, groupState);
        state.groupList.remove(oldName);
        groupState.players.forEach(uuid -> {
            state.players.get(uuid).groupName = newName;
        });

        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Changed ").withColor(RgbFormat.GOLD)).append(Text.literal(oldName).withColor(groupState.color)).append(Text.literal("'s name to ").withColor(RgbFormat.GOLD)).append(Text.literal(newName).withColor(groupState.color)), false);

        groupState.players.forEach(uuid -> {
            context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                    serverPlayer.sendMessage(Text.empty().append(Text.literal("Your group's name changed from ").withColor(RgbFormat.GOLD)).append(Text.literal(oldName).withColor(groupState.color)).append(Text.literal(" to ").withColor(RgbFormat.GOLD)).append(Text.literal(newName).withColor(groupState.color)));
                }
            });
        });
        
        return 1;
    }

    public static int colorExecute(CommandContext<ServerCommandSource> context) {
        if (!checkExecuteRequirements(context)) {
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        PlayerEntity player = context.getSource().getPlayer();
        PlayerData playerState = GroupManager.getPlayerState(player);
        GroupData groupState = state.groupList.get(playerState.groupName);
        int r = IntegerArgumentType.getInteger(context, "r");
        int g = IntegerArgumentType.getInteger(context, "g");
        int b = IntegerArgumentType.getInteger(context, "b");


        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            context.getSource().sendFeedback(() -> Text.literal("Invalid color. Values must be from 0-255.").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }
        if (groupState.color == RgbFormat.fromThree(r, g, b)) {
            context.getSource().sendFeedback(() -> Text.literal("Group already has this color.").withColor(RgbFormat.YELLOW), false);
            return 1;
        }

        groupState.color = RgbFormat.fromThree(r, g, b);
        context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Changed ").withColor(RgbFormat.GOLD)).append(Text.literal(playerState.groupName).withColor(groupState.color)).append(Text.literal("'s color to ").withColor(RgbFormat.GOLD)).append(Text.literal("(" + r + ", " + g + ", " + b + ")").withColor(groupState.color)), false);

        groupState.players.forEach(uuid -> {
            context.getSource().getServer().getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                if (serverPlayer.getUuid().equals(uuid) && !player.getUuid().equals(uuid)) {
                    serverPlayer.sendMessage(Text.empty().append(Text.literal("Your group's color has changed to ").withColor(RgbFormat.GOLD)).append(Text.literal("this").withColor(groupState.color)));
                }
            });
        });

        updateClientDisplayNames(context.getSource().getServer(), groupState);

        return 1;
    }
}