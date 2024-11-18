package ong.aldenw.commands.group;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.commands.suggestions.*;

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
                )
                .then(CommandManager.literal("config")
                        .then(CommandManager.literal("name")
                                .then(CommandManager.argument("name", StringArgumentType.string())
                                        .executes(GroupConfigCommand::nameExecute))
                        )
                        .then(CommandManager.literal("prefix")
                                .then(CommandManager.argument("prefix", StringArgumentType.string())
                                        .executes(GroupConfigCommand::prefixExecute))
                        )
                        .then(CommandManager.literal("color")
                                .then(CommandManager.argument("r", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("g", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("b", IntegerArgumentType.integer())
                                                        .executes(GroupConfigCommand::colorExecute)
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("joinOptions")
                                .then(CommandManager.argument("joinOption", StringArgumentType.string())
                                        .suggests(new JoinOptionSuggestions())
                                        .executes(GroupConfigCommand::joinOptionExecute))
                        )
                )
                .then(CommandManager.literal("join")
                        .then(CommandManager.argument("groupName", StringArgumentType.string())
                                .suggests(new GroupSuggestions())
                                .executes(GroupJoinCommand::execute)))
                .then(CommandManager.literal("leave")
                        .executes(GroupLeaveCommand::execute))
                .then(CommandManager.literal("delete")
                        .executes(GroupDeleteCommand::execute))
                .then(CommandManager.literal("transfer")
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(new GroupMemberSuggestions())
                                .executes(GroupTransferCommand::execute)))
                .then(CommandManager.literal("invite")
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(new OnlinePlayerSuggestions())
                                .executes(GroupInviteCommand::execute)))
                .then(CommandManager.literal("requests")
                        .then(CommandManager.literal("view")
                                .executes(GroupRequestsCommand::viewExecute))
                        .then(CommandManager.literal("accept")
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .suggests(new RequestSuggestions())
                                        .executes(GroupRequestsCommand::acceptExecute)))
                        .then(CommandManager.literal("deny")
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .suggests(new RequestSuggestions())
                                        .executes(GroupRequestsCommand::denyExecute))));

    }
}
