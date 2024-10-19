package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.data.PlayerData;

public class GroupOfCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        String playerArg = StringArgumentType.getString(context, "player");
        ServerPlayerEntity player = context.getSource().getWorld().getServer().getPlayerManager().getPlayer(playerArg);

        PlayerData playerState = GroupManager.getPlayerState(player);

        String result = !playerState.groupName.isEmpty() ? playerArg + " is in group " + playerState.groupName : playerArg + " is not in a group";

        context.getSource().sendFeedback(() -> Text.literal(result), false);
        return 1;
    }
}
