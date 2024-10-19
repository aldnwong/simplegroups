package ong.aldenw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class DisplayNameMixin {
    @Shadow public abstract Text getName();

    @ModifyVariable(method = "getDisplayName", at = @At("STORE"), ordinal = 0)
    private MutableText injected(MutableText mutableText) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        GroupManager state = GroupManager.getServerState(Objects.requireNonNull(player.getServer()));
        String groupName = GroupManager.getPlayerState(player).groupName;
        if(groupName.isEmpty()) {
            return Text.empty().append(this.getName());
        } else {
            return Text.empty().append(state.groupList.get(groupName).prefix).append(this.getName()).withColor(state.groupList.get(groupName).color);
        }
    }
}
