package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.commands.subcommands.GroupCreateCommand;
import ong.aldenw.commands.subcommands.GroupGetCommand;
import ong.aldenw.commands.suggestions.PlayerSuggestions;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.network.UpdatePayload;
import ong.aldenw.util.RgbFormat;

public class GroupCommand {
    public final static String commandName = "group";
    public final static int permissionLevel = 0;

    public final static String getCommandName = "get";
    public final static String createCommandName = "create";

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.literal(getCommandName)
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(new PlayerSuggestions())
                                .executes(GroupGetCommand::execute)
                        )
                )
                .then(CommandManager.literal(createCommandName)
                        .then(CommandManager.argument("groupName", StringArgumentType.string())
                                .executes(GroupCreateCommand::execute))
                );
    }
}
