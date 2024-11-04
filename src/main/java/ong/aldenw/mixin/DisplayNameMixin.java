package ong.aldenw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import ong.aldenw.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class DisplayNameMixin {
    @ModifyVariable(method = "getDisplayName", at = @At("STORE"), ordinal = 0)
    private MutableText injected(MutableText mutableText) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        UUID playerUuid = player.getUuid();
        String playerName = player.getName().getString();
        String prefix = (!Objects.isNull(NetworkManager.playerPrefixDataHashMap.get(playerUuid))) ? NetworkManager.playerPrefixDataHashMap.get(playerUuid) : "";
        int color = (!Objects.isNull(NetworkManager.playerColorDataHashMap.get(playerUuid))) ? NetworkManager.playerColorDataHashMap.get(playerUuid) : 16777215;

        if (prefix.isEmpty()) {
            return Text.literal(playerName).withColor(color);
        }
        else {
            return Text.literal(prefix + " " + playerName).withColor(color);
        }
    }
}
