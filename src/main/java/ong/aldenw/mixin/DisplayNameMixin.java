package ong.aldenw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import ong.aldenw.CacheManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class DisplayNameMixin {
    @Shadow public abstract Text getName();

    @ModifyVariable(method = "getDisplayName", at = @At("STORE"), ordinal = 0)
    private MutableText injected(MutableText mutableText) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        UUID playerUuid = player.getUuid();
        String playerName = player.getName().getString();
        if (Objects.isNull(CacheManager.playerPrefixDataHashMap.get(playerUuid)) || Objects.isNull(CacheManager.playerColorDataHashMap.get(playerUuid))) {
            return Text.literal(playerName);
        }

        String prefix = CacheManager.playerPrefixDataHashMap.get(playerUuid);
        int color = CacheManager.playerColorDataHashMap.get(playerUuid);
        if (prefix.isEmpty()) {
            return  Text.literal(playerName).withColor(color);
        }
        else {
            return Text.literal(prefix + " " + playerName).withColor(color);
        }
    }
}
