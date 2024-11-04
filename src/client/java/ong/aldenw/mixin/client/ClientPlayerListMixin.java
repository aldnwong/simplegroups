package ong.aldenw.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import ong.aldenw.ClientCacheManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerListEntry.class)
public abstract class ClientPlayerListMixin {
	@Shadow public abstract GameProfile getProfile();

	@Inject(at = @At("RETURN"), method = "getDisplayName", cancellable = true)
	private void injected(CallbackInfoReturnable<Text> cir) {
		UUID playerUuid = this.getProfile().getId();
		String playerName = this.getProfile().getName();
		String prefix = (!Objects.isNull(ClientCacheManager.playerPrefixDataHashMap.get(playerUuid))) ? ClientCacheManager.playerPrefixDataHashMap.get(playerUuid) : "";
		int color = (!Objects.isNull(ClientCacheManager.playerColorDataHashMap.get(playerUuid))) ? ClientCacheManager.playerColorDataHashMap.get(playerUuid) : 16777215;

		if (prefix.isEmpty()) {
			cir.setReturnValue(Text.literal(playerName).withColor(color));
		}
		else {
			cir.setReturnValue(Text.literal(prefix + " " + playerName).withColor(color));
		}
	}
}