package ong.aldenw.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class ClientDisplayNameMixin {
	@Inject(at = @At("RETURN"), method = "getDisplayName", cancellable = true)
	private void injected(CallbackInfoReturnable<Text> cir) {
		cir.setReturnValue(Text.literal("caw"));
	}
}