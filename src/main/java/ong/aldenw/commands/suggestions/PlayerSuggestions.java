package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.managers.DataManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerSuggestions implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        Collection<String> playerNames = context.getSource().getPlayerNames();
        for (String playerName : playerNames) {
            builder.suggest(playerName);
        }

        DataManager.getServerState(context.getSource().getServer()).playerUuids.forEach((username, uuid) -> {
            if (!playerNames.contains(username)) {
                builder.suggest(username);
            }
        });

        return builder.buildFuture();
    }
}