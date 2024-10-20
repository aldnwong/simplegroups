package ong.aldenw.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerListEntry.class)
public abstract class ClientDisplayNameMixin {
	@Shadow public abstract GameProfile getProfile();

	@Inject(at = @At("RETURN"), method = "getDisplayName", cancellable = true)
	private void injected(CallbackInfoReturnable<Text> cir) {
		MinecraftServer server = MinecraftClient.getInstance().world.getServer();
		String playerName = this.getProfile().getName();

		PlayerEntity player = (PlayerEntity)(Object)this;
		GroupManager state = GroupManager.getServerState(Objects.requireNonNull(player.getServer()));
		String groupName = GroupManager.getPlayerState(player).groupName;
		GroupData groupState = state.groupList.get(groupName);
		if(groupName.isEmpty()) {
			cir.setReturnValue(Text.literal(playerName));
		}
		else if (groupState.prefix.isEmpty()) {
			cir.setReturnValue(Text.literal(playerName).withColor(groupState.color));
		}
		else {
			cir.setReturnValue(Text.empty().append(groupState.prefix).append(" ").append(playerName).withColor(groupState.color));
		}
	}
}