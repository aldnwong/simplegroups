package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.concurrent.CompletableFuture;

public class RequestSuggestions implements SuggestionProvider<ServerCommandSource> {
    public static final String ALL_REQUESTS_PARAMETER = "ALL_JOIN_REQUESTS";
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();
        GroupManager state = GroupManager.getServerState(server);
        PlayerEntity player = source.getPlayer();
        PlayerData playerData = GroupManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.groupName);

        if (!source.isExecutedByPlayer() || playerData.groupName.isEmpty() || !player.getUuid().equals(groupData.getLeader()))
            return builder.buildFuture();

        if (groupData.getRequestsSize() != 1)
            builder.suggest(ALL_REQUESTS_PARAMETER);

        groupData.getRequests().forEach(uuid -> builder.suggest(state.players.get(uuid).username));

        return builder.buildFuture();
    }
}