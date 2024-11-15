package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.managers.DataManager;

import java.util.concurrent.CompletableFuture;

public class GroupMemberSuggestions implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer())
            return builder.buildFuture();

        DataManager state = DataManager.getServerState(context.getSource().getServer());
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = DataManager.getPlayerState(player);
        GroupData groupData = state.groupList.get(playerData.getGroupName());

        if (!playerData.isInAGroup())
            return builder.buildFuture();
        if (!groupData.isLeader(player.getUuid()))
            return builder.buildFuture();

        groupData.getPlayers().forEach(uuid -> {
            if (!uuid.equals(player.getUuid()))
                builder.suggest(DataManager.getPlayerState(uuid, context.getSource().getServer()).getUsername());
        });

        return builder.buildFuture();
    }
}
