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
		if (Objects.isNull(ClientCacheManager.playerPrefixDataHashMap.get(playerUuid)) || Objects.isNull(ClientCacheManager.playerColorDataHashMap.get(playerUuid))) {
			cir.setReturnValue(Text.literal(playerName));
			return;
		}

		String prefix = ClientCacheManager.playerPrefixDataHashMap.get(playerUuid);
		int color = ClientCacheManager.playerColorDataHashMap.get(playerUuid);
		if (prefix.isEmpty()) {
			cir.setReturnValue(Text.literal(playerName).withColor(color));
		}
		else {
			cir.setReturnValue(Text.literal(prefix + " " + playerName).withColor(color));
		}
	}
}