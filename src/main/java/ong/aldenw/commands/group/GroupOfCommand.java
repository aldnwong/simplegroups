package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.managers.NbtManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.Objects;

public class GroupOfCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        String playerArg = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getWorld().getServer();
        NbtManager serverState = NbtManager.getServerState(server);
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerArg);

        if(!Objects.nonNull(player) && !serverState.playerUuids.containsKey(playerArg)) {
            context.getSource().sendFeedback(() -> Text.literal("Player not found.").formatted(Formatting.DARK_RED), false);
            return 1;
        }

        PlayerData playerState = (Objects.nonNull(player)) ? NbtManager.getPlayerState(player) : NbtManager.getPlayerState(serverState.playerUuids.get(playerArg), server);
        if (playerState.groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal(playerArg + " is not in a group").formatted(Formatting.YELLOW), false);
        }
        else {
            GroupData groupData = serverState.groupList.get(playerState.groupName);
            context.getSource().sendFeedback(() -> Text.empty().append(Text.literal(playerArg).withColor(groupData.getColor())).append(Text.literal(" is in group ").formatted(Formatting.GOLD)).append(Text.literal(groupData.getName()).withColor(groupData.getColor())), false);
        }
        return 1;
    }
}
