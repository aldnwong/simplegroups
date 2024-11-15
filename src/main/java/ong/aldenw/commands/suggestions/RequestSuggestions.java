package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.managers.DataManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;

import java.util.concurrent.CompletableFuture;

public class RequestSuggestions implements SuggestionProvider<ServerCommandSource> {
    public static final String ALL_REQUESTS_PARAMETER = "ALL_JOIN_REQUESTS";
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();
        DataManager state = DataManager.getServerState(server);
        PlayerEntity player = source.getPlayer();
        PlayerData playerData = DataManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.getGroupName());

        if (!source.isExecutedByPlayer() || !playerData.isInAGroup() || !player.getUuid().equals(groupData.getLeader()))
            return builder.buildFuture();

        if (groupData.getRequestsSize() != 1)
            builder.suggest(ALL_REQUESTS_PARAMETER);

        groupData.getRequests().forEach(uuid -> builder.suggest(state.players.get(uuid).getUsername()));

        return builder.buildFuture();
    }
}