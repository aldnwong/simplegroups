package ong.aldenw;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.formats.RgbIntFormat;
import ong.aldenw.network.UpdateDisplayNamePayload;

import java.util.HashMap;
import java.util.UUID;

public class NetworkManager {
    public static HashMap<UUID, String> playerPrefixDataHashMap = new HashMap<>();
    public static HashMap<UUID, Integer> playerColorDataHashMap = new HashMap<>();

    // TODO: Split into updateColorCache and updatePrefixCache

    public static void updateCache(PlayerEntity player, MinecraftServer server) {
        UUID playerUuid = player.getUuid();
        PlayerData playerData = GroupManager.getPlayerState(player);
        String prefix;
        int color;

        if (playerData.groupName.isEmpty()) {
            prefix = "";
            color = RgbIntFormat.fromThree(255, 255, 255);
        }
        else {
            GroupData groupData = GroupManager.getServerState(server).groupList.get(playerData.groupName);
            prefix = groupData.getPrefix();
            color = groupData.getColor();
        }

        playerPrefixDataHashMap.put(playerUuid, prefix);
        playerColorDataHashMap.put(playerUuid, color);

        server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
            ServerPlayNetworking.send(serverPlayer, new UpdateDisplayNamePayload(playerUuid.toString(), prefix, color));
        });
    }

    public static void updatePlayerCache(ServerPlayerEntity player, GroupManager state) {
        state.groupList.forEach((name, groupData) -> {
            groupData.getPlayers().forEach(uuid -> {
                ServerPlayNetworking.send(player, new UpdateDisplayNamePayload(uuid.toString(), groupData.getPrefix(), groupData.getColor()));
            });
        });
    }
}
