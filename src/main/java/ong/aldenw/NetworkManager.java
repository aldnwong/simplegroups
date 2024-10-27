package ong.aldenw;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.data.GroupData;
import ong.aldenw.network.UpdateDisplayNamePayload;

import java.util.HashMap;
import java.util.UUID;

public class NetworkManager {
    public static HashMap<UUID, String> playerPrefixDataHashMap = new HashMap<>();
    public static HashMap<UUID, Integer> playerColorDataHashMap = new HashMap<>();

    /*
    * TODO: Update everything to handle players being removed from a group
    * It would probably be better to have updateCache handle the update of an individual player
    * Need to go through code to see where player data is being changed as well
    */

    public static void createCache(GroupManager state) {
        state.groupList.forEach((name, groupData) -> {
            groupData.players.forEach(uuid -> {
                playerPrefixDataHashMap.put(uuid, groupData.prefix);
                playerColorDataHashMap.put(uuid, groupData.color);
            });
        });
    }

    public static void updateCache(GroupData groupData, MinecraftServer server) {
        groupData.players.forEach(uuid -> {
            playerPrefixDataHashMap.put(uuid, groupData.prefix);
            playerColorDataHashMap.put(uuid, groupData.color);

            server.getPlayerManager().getPlayerList().forEach(player -> {
                ServerPlayNetworking.send(player, new UpdateDisplayNamePayload(uuid.toString(), groupData.prefix, groupData.color));
            });
        });
    }

    public static void updatePlayerCache(ServerPlayerEntity player, GroupManager state) {
        state.groupList.forEach((name, groupData) -> {
            groupData.players.forEach(uuid -> {
                ServerPlayNetworking.send(player, new UpdateDisplayNamePayload(uuid.toString(), groupData.prefix, groupData.color));
                if (groupData.leader.equals(player.getUuid()) && !groupData.requests.isEmpty()) {
                    player.sendMessage(Text.empty().append(Text.literal("Your group has ").formatted(Formatting.GOLD)).append(Text.literal(groupData.requests.size() + "").formatted(Formatting.AQUA)).append(Text.literal((groupData.requests.size() == 1) ? " join request!" : "join requests!").formatted(Formatting.GOLD)));
                }
            });
        });
    }
}
