package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbFormat;

import java.util.Objects;

public class GroupOfCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        String playerArg = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getWorld().getServer();
        GroupManager serverState = GroupManager.getServerState(server);
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerArg);

        if(!Objects.nonNull(player) && !serverState.playerUuids.containsKey(playerArg)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found.").withColor(RgbFormat.DARK_RED), false);
            return 1;
        }

        PlayerData playerState = (Objects.nonNull(player)) ? GroupManager.getPlayerState(player) : GroupManager.getPlayerState(serverState.playerUuids.get(playerArg), server);
        String result = !playerState.groupName.isEmpty() ? playerArg + " is in group " + playerState.groupName : playerArg + " is not in a group";

        context.getSource().sendFeedback(() -> Text.literal(result), false);
        return 1;
    }
}
