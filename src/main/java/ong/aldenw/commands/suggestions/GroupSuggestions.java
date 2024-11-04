package ong.aldenw.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import ong.aldenw.managers.NbtManager;

import java.util.concurrent.CompletableFuture;

public class GroupSuggestions implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        NbtManager state = NbtManager.getServerState(context.getSource().getServer());

        state.groupList.forEach((id, groupData) -> {
            if (groupData.getVisibility() != 0) {
                builder.suggest(groupData.getName());
            }
        });

        return builder.buildFuture();
    }
}