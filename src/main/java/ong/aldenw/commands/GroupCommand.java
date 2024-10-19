package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.commands.suggestions.PlayerSuggestions;

public class GroupCommand {
    public final static String commandName = "group";
    public final static int permissionLevel = 0;

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.literal("of")
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(new PlayerSuggestions())
                                .executes(GroupOfCommand::execute)
                        )
                )
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("groupName", StringArgumentType.string())
                                .executes(GroupCreateCommand::execute))
                );
    }
}
