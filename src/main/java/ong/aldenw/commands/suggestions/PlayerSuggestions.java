package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.GroupManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

// Copied from FabricMC documentation

public class PlayerSuggestions implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Collection<String> playerNames = source.getPlayerNames();

        for (String playerName : playerNames) {
            builder.suggest(playerName);
        }

        GroupManager.getServerState(source.getServer()).playerUuidCache.forEach((username, uuid) -> {
            if (!playerNames.contains(username)) {
                builder.suggest(username);
            }
        });

        return builder.buildFuture();
    }
}